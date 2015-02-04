package org.molgenis.autobetes;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.HttpsURLConnection;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.molgenis.autobetes.autobetes.MovesActivity;
import org.molgenis.autobetes.autobetes.MovesToken;
import org.molgenis.autobetes.autobetes.MovesUserProfile;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataConverter;
import org.molgenis.data.DataService;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.framework.ui.MolgenisPluginController;
import org.molgenis.auth.MolgenisUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;

public class MovesConnectorImpl implements MovesConnector
{
	public static final String ID = "moves";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;
	private static final String REDIRECT_URL = "http:%3A//195.169.22.237%3Ftoken%3D";
	public static final String MOVES_BASE_URL = "https://api.moves-app.com/";
	public static final String MOVES_API_BASE_URL =  "api/1.1/";
	public static final String MOVES_GET_ACTIVITIES_URL = "user/activities/daily";
	public static final String MOVES_GET_USER_PROFILE_URL = "user/profile";
	public static final String OAUTH_URL = "oauth/v1/";
	public static final String ACCESS_TOKEN ="access_token";
	public static final String GRANT_TYPE ="grant_type";
	public static final String AUTHORIZATION_CODE = "authorization_code";
	public static final String CODE = "code";
	public static final String CLIENT_ID_PARAM = "client_id";
	public static final String CLIENT_SECRET_PARAM = "client_secret";
	public static final String REDIRECT_URI_PARAM = "redirect_uri";
	public static final String TOKEN = "token";
	public static final String TOKEN_INFO ="tokeninfo";
	public static final String REFRESH_TOKEN = "refresh_token";
	public static final String FROM = "from=";
	public static final String TO = "to=";
	private static final String USER_AGENT = "Mozilla/5.0";
	

	private static final long ONEDAYINMILLISEC = 86400000;
	private static final String DATEFORMATSTRING = "yyyyMMdd";
	private static final DateFormat DATEFORMAT = new SimpleDateFormat(DATEFORMATSTRING);
	
	public boolean manageActivities(DataService dataService, MolgenisUser user, String CLIENT_ID_PARAM_VALUE, String CLIENT_SECRET_PARAM_VALUE){
		try{
			//get movestoken from db
			
			MovesToken movesToken = dataService.findOne(MovesToken.ENTITY_NAME, new QueryImpl().eq(MovesToken.OWNER, user), MovesToken.class);
			
			if(movesToken != null){
				boolean isValid = accessTokenIsValid(movesToken.getAccessToken());
				if(isValid == false){
					//token is not valid anymore
					//get new token
					MovesToken newMovesToken = refreshToken(movesToken.getRefresh_Token(), user, CLIENT_ID_PARAM_VALUE, CLIENT_SECRET_PARAM_VALUE);
					//update token
					newMovesToken.setId(movesToken.getId());
					dataService.update(MovesToken.ENTITY_NAME, newMovesToken);
					movesToken = newMovesToken;
					
				}
				//we now have a valid token that enables us to retrieve activities
				//get the from date 
				int from = getFromDate(dataService, user, movesToken);
				int to = getCurrentDate();
				//get activities from Moves
				List<MovesActivity> activities = getActivities(movesToken, from, to);
				for(MovesActivity activity : activities){
					//check if user has activity in db with the same start time, if so we asume that it are the same activity
					MovesActivity dbCheck = dataService.findOne(MovesActivity.ENTITY_NAME,  new QueryImpl().eq(MovesActivity.OWNER, user).and().eq(MovesActivity.STARTTIME, activity.getStartTime()), MovesActivity.class);
					if(dbCheck == null){
						//no activity in db
						dataService.add(MovesActivity.ENTITY_NAME, activity);
					}
					else{
						activity.setId(dbCheck.getId());
						dataService.update(MovesActivity.ENTITY_NAME, activity);
					}
				}
				return true;
			}
			else{
				//user is not connected to moves.
				return false;
			}
		}
		catch(Exception e){
			System.out.println(e.toString());
			return false;
		}
		
	}

	public boolean accessTokenIsValid(String accessToken){
		try{
			String url = MOVES_BASE_URL+OAUTH_URL+TOKEN_INFO+"?"+ACCESS_TOKEN+"="+accessToken;
			HttpsURLConnection connection = doGetRequest(url);
			int responseCode = connection.getResponseCode();
			if(responseCode == 404){
				//status code not found, token is not valid
				return false;
			}
			else{
				return true;
			}
		}
		catch(Exception e){
			System.out.println(e.toString());
			return false;
		}

	}

	public MovesToken refreshToken(String refreshToken, MolgenisUser user, String CLIENT_ID_PARAM_VALUE, String CLIENT_SECRET_PARAM_VALUE){

		String url = MOVES_BASE_URL+OAUTH_URL+ACCESS_TOKEN+"?"+GRANT_TYPE+"="+REFRESH_TOKEN+"&"+REFRESH_TOKEN+"="+refreshToken+"&"+CLIENT_ID_PARAM+"="+CLIENT_ID_PARAM_VALUE+"&"+CLIENT_SECRET_PARAM+"="+CLIENT_SECRET_PARAM_VALUE;
		System.out.println("the url:"+ url);
		String content = "{}";
		HttpsURLConnection connection = doPostRequest(url, content);
		//read response
		JSONObject jObject = readJsonObjectFromResponse(connection);	
		MovesToken movesToken = getMovesTokenEntityFromJson(jObject, user);
		return movesToken;
	}

	public MovesToken exchangeAutorizationcodeForAccesstoken(MolgenisUser user, String token, String authorizationcode, String CLIENT_ID_PARAM_VALUE, String CLIENT_SECRET_PARAM_VALUE){
		try{
			String url = MOVES_BASE_URL+OAUTH_URL+ACCESS_TOKEN+"?"+GRANT_TYPE+"="+AUTHORIZATION_CODE+"&"+CODE+"="+authorizationcode+"&"+CLIENT_ID_PARAM+"="+CLIENT_ID_PARAM_VALUE+"&"+CLIENT_SECRET_PARAM+"="+CLIENT_SECRET_PARAM_VALUE+"&"+REDIRECT_URI_PARAM+"="+REDIRECT_URL+token;
			//String url = "https://api.moves-app.com/api/1.1/user/activities/daily?from=20141119&to=20141128&access_token=_MJnP57s9Bto6h9qNFyubozuI24y3UI3fZ2q755uVDx1nf8xyV77255YHUEXd9o2";
			String content = "{}";
			HttpsURLConnection connection = doPostRequest(url, content);
			//read response
			JSONObject jObject = readJsonObjectFromResponse(connection);	
			MovesToken movesToken = getMovesTokenEntityFromJson(jObject, user);

			return(movesToken);


		}
		catch(Exception e){
			System.out.println(e.toString());
			return null;
		}
	}

	public MovesUserProfile getUserProfile(MovesToken movesToken)
	{
		try{
			String url = MOVES_BASE_URL+MOVES_API_BASE_URL+MOVES_GET_USER_PROFILE_URL+"?"+ACCESS_TOKEN+"="+movesToken.getAccessToken();
			HttpsURLConnection connection = doGetRequest(url);
			JSONObject jsonObject = readJsonObjectFromResponse(connection);
			MovesUserProfile userProfile = getMovesUserProfileFromJson(jsonObject, movesToken);
			return userProfile;
		}
		catch(Exception e){
			return null;
		}
	}

	public List<MovesActivity> getActivities(MovesToken movesToken, int from, int to){
		try{
			String url = MOVES_BASE_URL+MOVES_API_BASE_URL+MOVES_GET_ACTIVITIES_URL+"?"+FROM+from+"&"+TO+to+"&"+ACCESS_TOKEN+"="+movesToken.getAccessToken();
			HttpsURLConnection connection = doGetRequest(url);
			JSONArray jArray = readJsonArrayFromResponse(connection);
			List<MovesActivity> activities = getActivitiesFromJson(jArray, movesToken);
			return activities;
		}
		catch(Exception e){
			System.out.println(e.toString());
			return null;
		}

	}
	
	private int getFromDate(DataService dataService, MolgenisUser user, MovesToken movesToken){
		//get the last day that activities are stored in db
		//sot.setSort(MovesActivity.STARTTIME);
		
		Iterable<MovesActivity> activities = dataService.findAll(MovesActivity.ENTITY_NAME, new QueryImpl().eq(MovesActivity.OWNER, user).and().sort(Direction.DESC, MovesActivity.STARTTIME), MovesActivity.class);
		if(activities != null){
			
			
			//get the first activity, because it is sorted on starttime descending, this will be the last day of recordings
			MovesActivity activity = activities.iterator().next();
			Long startTime = activity.getStartTime();
			//starttime minus one day as a security
			startTime -= ONEDAYINMILLISEC;
			Date date = new Date(startTime);
			return Integer.parseInt(DATEFORMAT.format(date));
		}
		else{
			//no activities stored in db
			//get the date that user started recording
			//get profile
			MovesUserProfile profile =  dataService.findOne(MovesUserProfile.ENTITY_NAME, new QueryImpl().eq(MovesActivity.OWNER, user), MovesUserProfile.class);
			if(profile == null){
				//no profile stored in db
				//get profile from Moves
				profile = getUserProfile(movesToken);
				//store profile
				dataService.add(MovesUserProfile.ENTITY_NAME, profile);	
			}
			return profile.getFirstdate();
			
		}
		
	}

	private List<MovesActivity> getActivitiesFromJson(JSONArray jArray, MovesToken movesToken)
	{
		try{
			EntityMetaData metadata = new MovesActivity().getEntityMetaData();
			List<MovesActivity> entities = new ArrayList<MovesActivity>();
			//iterate array
			for(int i = 0; i < jArray.length(); i++){
				JSONObject jObject = jArray.getJSONObject(i);
				JSONArray segments = null;
				//get segments
				if(jObject.get("segments").equals(null) == false){
					segments = jObject.getJSONArray("segments");
					//iterate segments
					for(int o = 0; o < segments.length(); o++){
						JSONObject segment = segments.getJSONObject(o);
						//get activity
						JSONArray activities = segment.getJSONArray("activities");
						//iterate activities
						for(int q = 0; q < activities.length(); q++){
							MovesActivity entity = new MovesActivity();
							JSONObject activity = activities.getJSONObject(q);
							//iterate values per activity and add them to entity
							for(AttributeMetaData key : metadata.getAtomicAttributes()){
								try{
								if(activity.has(key.getName())|| key.getName() == MovesActivity.GROUPNAME){
									if(key.getName() == "startTime" || key.getName() == "endTime"){
										//startTime and endTime need to be converted to unix timestamp
										try
										{
											Date time = new SimpleDateFormat("yyyyMMdd'T'HHmmssX").parse(activity.getString(key.getName()));
											entity.set(key.getName(), time.getTime());
										}
										catch (ParseException e)
										{
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
									}
									else if(key.getName() == MovesActivity.GROUPNAME){
										//group is a reserved JAVA and/or SQL word, therefore it is named groupName in datamodel
										entity.set(MovesActivity.GROUPNAME, activity.get("group"));
									}
									else if(key.getName() == MovesActivity.ID){
										//id need to be auto_generated, don't do nothing
									}
									else{
										
										Object value = DataConverter.convert(activity.get(key.getName()), key);
										entity.set(key.getName(), value);
										
										
									}
								}
							}
							catch(Exception e){
								System.out.println(e.toString());
								System.out.println(activity.toString());
								System.out.println(key.getName());
							}
							}
							/*
							catch(Exception e){
								System.out.println(e.toString());
								System.out.println(activity.toString());
								System.out.println(key.getName());
							}
							*/
							entity.set(MovesActivity.OWNER, movesToken.get(MovesToken.OWNER));
							entities.add(entity);

						}
					}
				}


			}
			return entities;
		}
		catch(JSONException e){
			System.out.println(e.toString());
			return null;
		}
	}

	private static MovesUserProfile getMovesUserProfileFromJson(JSONObject jObject, MovesToken movesToken){
		try{
			//TODO there might be a better way to implement this. The problem with this json is the three structure, because the nodes are not all on the same level.
			//Json is documented at https://dev.moves-app.com/docs/api_profile
			
			
			MovesUserProfile entity = new MovesUserProfile();
			//Object value = DataConverter.convert(activity.get(key.getName()), key);
			entity.set(MovesUserProfile.MOVESTOKEN, movesToken);
			entity.set(MovesUserProfile.FIRSTDATE, Integer.parseInt(jObject.getJSONObject("profile").get("firstDate").toString()));
			entity.set(MovesUserProfile.CURRENTTIMEZONEID, jObject.getJSONObject("profile").getJSONObject("currentTimeZone").get("id").toString());
			entity.set(MovesUserProfile.CURRENTTIMEZONEOFFSET, Integer.parseInt(jObject.getJSONObject("profile").getJSONObject("currentTimeZone").get("offset").toString()));
			entity.set(MovesUserProfile.LANGUAGE, jObject.getJSONObject("profile").getJSONObject("localization").get("language").toString());
			entity.set(MovesUserProfile.LOCALE, jObject.getJSONObject("profile").getJSONObject("localization").get("locale").toString());
			entity.set(MovesUserProfile.FIRSTWEEKDAY, Integer.parseInt(jObject.getJSONObject("profile").getJSONObject("localization").get("firstWeekDay").toString()));
			entity.set(MovesUserProfile.METRIC, jObject.getJSONObject("profile").getJSONObject("localization").get("metric"));
			entity.set(MovesUserProfile.CALORIESAVAILABLE, jObject.getJSONObject("profile").get("caloriesAvailable"));
			entity.set(MovesUserProfile.PLATFORM, jObject.getJSONObject("profile").get("platform").toString());
			return entity;
		}
		catch(Exception e){
			System.out.println(e.toString());
			return null;
		}
	}

	private static MovesToken getMovesTokenEntityFromJson(JSONObject jObject, MolgenisUser user){
		try{
			MovesToken movesToken = new MovesToken();
			movesToken.set(MovesToken.OWNER, user);
			movesToken.set(MovesToken.ACCESSTOKEN, jObject.get("access_token").toString());
			movesToken.set(MovesToken.TOKEN_TYPE, jObject.get("token_type").toString());
			movesToken.set(MovesToken.EXPIRES_IN, jObject.get("expires_in").toString() );
			movesToken.set(MovesToken.REFRESH_TOKEN, jObject.get("refresh_token").toString() );
			movesToken.set(MovesToken.USER_ID, Long.parseLong(jObject.get("user_id").toString()) );
			return movesToken;
		}
		catch(Exception e){
			System.out.println(e.toString());
			return null;
		}
	}

	private static JSONObject readJsonObjectFromResponse(HttpsURLConnection connection){
		try{
			String responseAsString = convertInputStreamToString(connection);
			JSONObject jObject = new JSONObject(responseAsString);
			return jObject;
		}
		catch(Exception e){
			System.out.println(e.toString());
			return null;
		}

	}

	private static JSONArray readJsonArrayFromResponse(HttpsURLConnection connection){
		try{
			String responseAsString = convertInputStreamToString(connection);
			JSONArray jArray = new JSONArray(responseAsString);
			return jArray;
		}
		catch(Exception e){
			System.out.println(e.toString());
			return null;
		}
	}

	private static String convertInputStreamToString(HttpsURLConnection connection){
		try{
			BufferedReader in = new BufferedReader(
					new InputStreamReader(new GZIPInputStream(connection.getInputStream())));

			String inputLine;
			StringBuffer response = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
			return response.toString();
		}
		catch(Exception e){
			System.out.println(e.toString());
			return null;
		}
	}

	private static HttpsURLConnection doGetRequest(String url){
		try{
			URL obj = new URL(url);
			HttpsURLConnection connection = (HttpsURLConnection) obj.openConnection();

			// optional default is GET
			connection.setRequestMethod("GET");
			connection.setRequestProperty("Content-Type", "text/plain; charset=utf-8");
			connection.setRequestProperty("User-Agent", "Mozilla/5.0");
			connection.setRequestProperty("Accept" ,"*/*");
			connection.setRequestProperty("Accept-Encoding", "gzip,deflate");
			//add request header
			connection.setRequestProperty("User-Agent", USER_AGENT);

			return connection;

		}
		catch(Exception e){
			System.out.println(e.toString());
			return null;
		}
	}

	private static HttpsURLConnection doPostRequest(String url, String content){
		try{
			URL obj = new URL(url);
			HttpsURLConnection connection = (HttpsURLConnection) obj.openConnection();

			//add reuqest header
			connection.setDoOutput(true);
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/json; charset=utf8");
			connection.setRequestProperty("User-Agent", "Mozilla/5.0");
			connection.setRequestProperty("Accept" ,"*/*");
			connection.setRequestProperty("Accept-Encoding", "gzip,deflate");
			connection.setRequestProperty("Accept-Language", "en-US,en;q=0.8");

			// Send post request
			OutputStream os = connection.getOutputStream();
			os.write(content.getBytes("UTF-8"));
			os.close();

			int responseCode = connection.getResponseCode();
			if(responseCode == 400){
				throw new Exception("Unauthorized");
			}
			else{
				return connection;
			}
		}
		catch(Exception e){
			System.out.println(e.toString());
			return null;
		}

	}
	
	private static int getCurrentDate(){
		//get current date in yyyyMMdd format
		Date date = new Date();
		return Integer.parseInt(DATEFORMAT.format(date));
	}


}
