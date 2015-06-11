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
import org.molgenis.auth.MolgenisUser;
import org.molgenis.autobetes.autobetes.MovesActivity;
import org.molgenis.autobetes.autobetes.MovesToken;
import org.molgenis.autobetes.autobetes.MovesUserProfile;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataConverter;
import org.molgenis.data.DataService;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.framework.ui.MolgenisPluginController;
import org.molgenis.security.runas.RunAsSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
public class MovesConnectorImpl implements MovesConnector
{
	private static final Logger LOG = LoggerFactory.getLogger(MovesConnectorImpl.class);
	public static final String ID = "moves";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;
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
	public static final String TOKEN_PARAM = "?token=";
	public static final String TOKEN = "token";
	public static final String TOKEN_INFO ="tokeninfo";
	public static final String REFRESH_TOKEN = "refresh_token";
	public static final String FROM = "from=";
	public static final String TO = "to=";
	private static final String USER_AGENT = "Mozilla/5.0";

	private static final long ONE_DAY_IN_MILLISEC = 86400000;
	private static final long TWENTY_NINE_DAYS_IN_MILLISEC = 2505600000L;
	private static final String DATE_FORMAT_STRING = "yyyyMMdd";
	private static final DateFormat DATE_FORMAT = new SimpleDateFormat(DATE_FORMAT_STRING);
	/**
	 * This method updates the activities of a certain user.
	 * 
	 * @param dataService for accessing database
	 * @param user for which activities need to be managed
	 * @return boolean, true is success
	 */
	public void manageActivities(DataService dataService, MolgenisUser user, String CLIENT_ID_PARAM_VALUE, String CLIENT_SECRET_PARAM_VALUE){
		try{
			Thread thread = new Thread(){
				public void run(){
					//get movestoken from db
					MovesToken movesToken = dataService.findOne(MovesToken.ENTITY_NAME, new QueryImpl().eq(MovesToken.OWNER, user), MovesToken.class);

					if(movesToken != null){
						boolean isValid = accessTokenIsValid(movesToken.getAccessToken());
						if(!isValid){
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

						//max 31 of days allowed and the requested range must be between user profiles first date and today
						//retrieve activities from moves per 29 days

						long fromInMillisec = convertDateformatToUnixTimestamp(from);
						long toInMillisec = convertDateformatToUnixTimestamp(to);

						while(fromInMillisec+TWENTY_NINE_DAYS_IN_MILLISEC < toInMillisec){
							//get activities from fromInMillisec to fromInMillisec+29 days
							getActivitiesAndAddToDB(user,dataService,movesToken,convertUnixTimestampToDateFormat(fromInMillisec), convertUnixTimestampToDateFormat(fromInMillisec+TWENTY_NINE_DAYS_IN_MILLISEC));
							//add 29 days to fromInMillisec
							fromInMillisec += TWENTY_NINE_DAYS_IN_MILLISEC;
						}
						//while loop is stopped, so fromInMillisec+29 days is now greater than current date.
						//get activities from fromInMillisec to current date
						getActivitiesAndAddToDB(user,dataService,movesToken,convertUnixTimestampToDateFormat(fromInMillisec), convertUnixTimestampToDateFormat(toInMillisec));
					}
				}
			};
			thread.start();
		}
		catch(Exception e){
			LOG.error(e.toString());
			throw new RuntimeException(e);
		}


	}
	/**
	 * Checks if access token from moves is still valid
	 * @param accessToken
	 * @return boolean true if valid
	 */
	public boolean accessTokenIsValid(String accessToken){
		try{
			//ask moves server if token is valid
			//define url
			String url = MOVES_BASE_URL+OAUTH_URL+TOKEN_INFO+"?"+ACCESS_TOKEN+"="+accessToken;
			//set up get request
			HttpsURLConnection connection = doGetRequest(url);
			//if response is "not found" then accesstoken is not valid
			int responseCode = connection.getResponseCode();
			if(responseCode == HttpsURLConnection.HTTP_NOT_FOUND){
				//status code not found, token is not valid
				return false;
			}
			else{
				return true;
			}
		}
		catch(Exception e){
			LOG.error(e.toString());
			throw new RuntimeException(e);
		}

	}
	/**
	 * This method refreshes the access token.
	 * @param refreshToken
	 * @param user
	 * @param clientId
	 * @param clientSecret
	 * @return new MovesToken
	 */
	public MovesToken refreshToken(String refreshToken, MolgenisUser user, String CLIENT_ID_PARAM_VALUE, String CLIENT_SECRET_PARAM_VALUE){
		//define url
		String url = MOVES_BASE_URL+OAUTH_URL+ACCESS_TOKEN+"?"+GRANT_TYPE+"="+REFRESH_TOKEN+"&"+REFRESH_TOKEN+"="+refreshToken+"&"+CLIENT_ID_PARAM+"="+CLIENT_ID_PARAM_VALUE+"&"+CLIENT_SECRET_PARAM+"="+CLIENT_SECRET_PARAM_VALUE;
		String content = "{}";
		//set up connection
		HttpsURLConnection connection = doPostRequest(url, content);
		//read and return response
		JSONObject jObject = readJsonObjectFromResponse(connection);	
		MovesToken movesToken = getMovesTokenEntityFromJson(jObject, user);
		return movesToken;
	}
	/**
	 * Once user authorizes this server to connect with moves an authorization code is retrieved. Together with the client id, secret id and token
	 * this method retrieves the MovesToken which allows accessing personal data. 
	 * @param user
	 * @param token
	 * @param authorizationcode
	 * @param CLIENT_SECRET_PARAM_VALUE2 
	 * @param CLIENT_ID_PARAM_VALUE 
	 * @param clientId
	 * @param secretId
	 * @return MovesToken
	 */
	public MovesToken exchangeAutorizationcodeForAccesstoken(MolgenisUser user, String token, String authorizationcode, String client_id_param_value, String client_secret_param_value, String moves_redirect_url){
		try{
			//define url
			String url = MOVES_BASE_URL+OAUTH_URL+ACCESS_TOKEN+"?"+GRANT_TYPE+"="+AUTHORIZATION_CODE+"&"+CODE+"="+authorizationcode+"&"+CLIENT_ID_PARAM+"="+client_id_param_value+"&"+CLIENT_SECRET_PARAM+"="+client_secret_param_value+"&"+REDIRECT_URI_PARAM+"="+moves_redirect_url+TOKEN_PARAM+token;
			String content = "{}";
			//set up connection
			HttpsURLConnection connection = doPostRequest(url, content);
			//read and return response
			JSONObject jObject = readJsonObjectFromResponse(connection);	
			MovesToken movesToken = getMovesTokenEntityFromJson(jObject, user);

			return(movesToken);


		}
		catch(Exception e){
			LOG.error(e.toString());
			throw new RuntimeException(e);
		}
	}
	/**
	 * Retrieves user profile from Moves
	 * @param movesToken
	 * @return MovesUserProfile see https://dev.moves-app.com/docs/api_profile 
	 */
	public MovesUserProfile getUserProfile(MovesToken movesToken)
	{
		try{
			//define url
			String url = MOVES_BASE_URL+MOVES_API_BASE_URL+MOVES_GET_USER_PROFILE_URL+"?"+ACCESS_TOKEN+"="+movesToken.getAccessToken();
			//set up connection
			HttpsURLConnection connection = doGetRequest(url);
			//parse and return
			JSONObject jsonObject = readJsonObjectFromResponse(connection);
			MovesUserProfile userProfile = getMovesUserProfileFromJson(jsonObject, movesToken);
			return userProfile;
		}
		catch(Exception e){
			return null;
		}
	}
	/**
	 * Retrieves all activities that occured in specified range of time
	 * @param movesToken
	 * @param from date in yyyyMMdd format
	 * @param to date in yyyyMMdd format
	 * @return list of activities
	 */
	public List<MovesActivity> getActivities(MovesToken movesToken, int from, int to){
		try{
			//define url
			String url = MOVES_BASE_URL+MOVES_API_BASE_URL+MOVES_GET_ACTIVITIES_URL+"?"+FROM+from+"&"+TO+to+"&"+ACCESS_TOKEN+"="+movesToken.getAccessToken();
			//set up connection
			HttpsURLConnection connection = doGetRequest(url);
			//parse response and return
			JSONArray jArray = readJsonArrayFromResponse(connection);
			List<MovesActivity> activities = getActivitiesFromJson(jArray, movesToken);
			return activities;
		}
		catch(Exception e){
			LOG.error(e.toString());
			throw new RuntimeException(e);
		}

	}
	/**
	 * Retrieves activities and add it to the db
	 * @param user
	 * @param dataService
	 * @param movesToken
	 * @param from
	 * @param to
	 * @return 
	 */
	@Async
	private void getActivitiesAndAddToDB(MolgenisUser user, DataService dataService, MovesToken movesToken, int from, int to){
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
	}
	/**
	 * Get get the last day that activities are stored in db
	 * @param dataService
	 * @param user
	 * @param movesToken
	 * @return date in yyyyMMdd format
	 */
	private int getFromDate(DataService dataService, MolgenisUser user, MovesToken movesToken){
		//get the last day that activities are stored in db
		//sot.setSort(MovesActivity.STARTTIME);

		Iterator<MovesActivity> activities = dataService.findAll(MovesActivity.ENTITY_NAME, new QueryImpl().eq(MovesActivity.OWNER, user).and().sort(Direction.DESC, MovesActivity.STARTTIME), MovesActivity.class).iterator();
		if(activities.hasNext()){
			//get the first activity, because it is sorted on starttime descending, this will be the last day of recordings
			MovesActivity activity = activities.next();
			Long startTime = activity.getStartTime();
			//starttime minus one day as a security
			startTime -= ONE_DAY_IN_MILLISEC;
			Date date = new Date(startTime);
			return Integer.parseInt(DATE_FORMAT.format(date));
		}
		else{
			//no activities stored in db
			//get the date that user started recording
			//get profile
			MovesUserProfile profile =  dataService.findOne(MovesUserProfile.ENTITY_NAME, new QueryImpl().eq(MovesUserProfile.MOVESTOKEN, movesToken), MovesUserProfile.class);
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
	/**
	 * Iterates json and parses activities 
	 * @param jArray
	 * @param movesToken
	 * @return list with activities
	 */
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
									LOG.error(e.toString());
									throw new RuntimeException(e);

								}
							}
							entity.set(MovesActivity.OWNER, movesToken.get(MovesToken.OWNER));
							entities.add(entity);

						}
					}
				}


			}
			return entities;
		}
		catch(JSONException e){
			LOG.error(e.toString());
			throw new RuntimeException(e);
		}
	}
	/**
	 * Parses json to MovesUserProfile
	 * @param jObject
	 * @param movesToken
	 * @return MovesUserProfile
	 */
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
			LOG.error(e.toString());
			throw new RuntimeException(e);
		}
	}
	/**
	 * Parses json to movestoken
	 * @param jObject
	 * @param user
	 * @return MovesToken
	 */
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
			LOG.error(e.toString());
			throw new RuntimeException(e);
		}
	}
	/**
	 * Converts input stream to json object.
	 * @param connection
	 * @return json object
	 */
	private static JSONObject readJsonObjectFromResponse(HttpsURLConnection connection){
		try{
			String responseAsString = convertInputStreamToString(connection);
			JSONObject jObject = new JSONObject(responseAsString);
			return jObject;
		}
		catch(Exception e){
			LOG.error(e.toString());
			throw new RuntimeException(e);
		}

	}
	/**
	 * Converts input stream to json array.
	 * @param connection
	 * @return json array
	 */
	private static JSONArray readJsonArrayFromResponse(HttpsURLConnection connection){
		try{
			String responseAsString = convertInputStreamToString(connection);
			JSONArray jArray = new JSONArray(responseAsString);
			return jArray;
		}
		catch(Exception e){
			LOG.error(e.toString());
			throw new RuntimeException(e);
		}
	}
	/**
	 * Converts input stream to string.
	 * @param connection
	 * @return string
	 */
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
			LOG.error(e.toString());
			throw new RuntimeException(e);
		}
	}
	/**
	 * Given the url, this method sets up the get request
	 * @param url
	 * @return HttpsURLConnection
	 */
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
			LOG.error(e.toString());
			throw new RuntimeException(e);
		}
	}
	/**
	 * Given the url, this method sets up the post request
	 * @param url
	 * @param content
	 * @return HttpsURLConnection
	 */
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
			LOG.error(e.toString());
			throw new RuntimeException(e);
		}

	}

	/**
	 * Gets current date in yyyyMMdd format
	 * @return date in yyyyMMdd format
	 */
	private static int getCurrentDate(){
		//get current date in yyyyMMdd format
		Date date = new Date();
		return Integer.parseInt(DATE_FORMAT.format(date));
	}
	/**
	 * Converts unix timestamp to date in yyyyMMdd format
	 * @param unixTimestamp
	 * @return date in yyyyMMdd format
	 */
	private static int convertUnixTimestampToDateFormat(long unixTimestamp){
		//get date in yyyyMMdd format
		Date date = new Date(unixTimestamp);
		return Integer.parseInt(DATE_FORMAT.format(date));
	}
	/**
	 * Converts date in yyyyMMdd format to Unix timestamp
	 * @param dateInFormat
	 * @return unixTimestamp
	 */
	private static long convertDateformatToUnixTimestamp(int dateInFormat){
		Date date =null;
		try
		{
			date = DATE_FORMAT.parse(Integer.toString(dateInFormat));
		}
		catch (ParseException e)
		{
			LOG.error(e.toString());
			throw new RuntimeException(e);
		}
		return date.getTime();
	}


}
