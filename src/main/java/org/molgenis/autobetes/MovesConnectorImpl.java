package org.molgenis.autobetes;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.HttpsURLConnection;

import org.codehaus.jettison.json.JSONObject;
import org.molgenis.autobetes.autobetes.MovesToken;
import org.molgenis.autobetes.autobetes.MovesUserProfile;
import org.molgenis.data.Entity;
import org.molgenis.data.support.MapEntity;
import org.molgenis.framework.ui.MolgenisPluginController;
import org.molgenis.omx.auth.MolgenisUser;

public class MovesConnectorImpl implements MovesConnector
{
	
	public static final String ID = "moves";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;
	public static final String BASE_URI = "";
	private static final String REDIRECT_URL = "http://autobetes.nl";
	
	public static final String MOVES_BASE_URL = "https://api.moves-app.com/";
	public static final String MOVES_API_BASE_URL =  "api/1.1/";
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

	private static final String USER_AGENT = "Mozilla/5.0";
	
	
	
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
	
	public MapEntity refreshToken(String refreshToken, MolgenisUser user, String clientId, String clientSecret){

		String url = MOVES_BASE_URL+OAUTH_URL+ACCESS_TOKEN+"?"+GRANT_TYPE+"="+REFRESH_TOKEN+"&"+REFRESH_TOKEN+"="+refreshToken+"&"+CLIENT_ID_PARAM+"="+clientId+"&"+CLIENT_SECRET_PARAM+"="+clientSecret;
		String content = "{}";
		HttpsURLConnection connection = doPostRequest(url, content);
		//read response
		JSONObject jObject = readJsonObjectFromResponse(connection);	
		MapEntity entity = getMovesTokenEntityFromJson(jObject, user);
		return entity;
	}

	public MapEntity exchangeAutorizationcodeForAccesstoken(MolgenisUser user, String token, String authorizationcode, String clientId, String secretId){
		try{
			String url = MOVES_BASE_URL+OAUTH_URL+ACCESS_TOKEN+"?"+GRANT_TYPE+"="+AUTHORIZATION_CODE+"&"+CODE+"="+authorizationcode+"&"+CLIENT_ID_PARAM+"="+clientId+"&"+CLIENT_SECRET_PARAM+"="+secretId+"&"+REDIRECT_URI_PARAM+"="+REDIRECT_URL+"?"+TOKEN+"="+token;
			//String url = "https://api.moves-app.com/api/1.1/user/activities/daily?from=20141119&to=20141128&access_token=_MJnP57s9Bto6h9qNFyubozuI24y3UI3fZ2q755uVDx1nf8xyV77255YHUEXd9o2";
			String content = "{}";
			HttpsURLConnection connection = doPostRequest(url, content);
			//read response
			JSONObject jObject = readJsonObjectFromResponse(connection);	
			MapEntity entity = getMovesTokenEntityFromJson(jObject, user);
			
			return(entity);


		}
		catch(Exception e){
			System.out.println(e.toString());
			return null;
		}
	}
	
	public MapEntity getUserProfile(MovesToken movesToken)
	{
		try{
			String url = MOVES_BASE_URL+MOVES_API_BASE_URL+MOVES_GET_USER_PROFILE_URL+"?"+ACCESS_TOKEN+"="+movesToken.getAccessToken();
			HttpsURLConnection connection = doGetRequest(url);
			JSONObject jsonObject = readJsonObjectFromResponse(connection);
			System.out.println(jsonObject);
			return null;
		}
		catch(Exception e){
			return null;
		}
	}
	
	private static MapEntity getMovesUserProfileFromJson(JSONObject jObject, MovesToken movesToken){
		try{
			MapEntity entity = new MapEntity(MovesUserProfile.ENTITY_NAME);
			//entity.set(MovesUserProfile.MOV, user);
			return null;
		}
		catch(Exception e){
			System.out.println(e.toString());
			return null;
		}
	}
	
	private static MapEntity getMovesTokenEntityFromJson(JSONObject jObject, MolgenisUser user){
		try{
		MapEntity entity = new MapEntity(MovesToken.ENTITY_NAME);
		entity.set(MovesToken.OWNER, user);
		entity.set(MovesToken.ACCESSTOKEN, jObject.get("access_token").toString());
		entity.set(MovesToken.TOKEN_TYPE, jObject.get("token_type").toString());
		entity.set(MovesToken.EXPIRES_IN, jObject.get("expires_in").toString() );
		entity.set(MovesToken.REFRESH_TOKEN, jObject.get("refresh_token").toString() );
		entity.set(MovesToken.USER_ID, jObject.get("user_id").toString() );
		return entity;
		}
		catch(Exception e){
			System.out.println(e.toString());
			return null;
		}
	}

	private static JSONObject readJsonObjectFromResponse(HttpsURLConnection connection){
		try{
			BufferedReader in = new BufferedReader(
					new InputStreamReader(new GZIPInputStream(connection.getInputStream())));

			String inputLine;
			StringBuffer response = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
			JSONObject jObject = new JSONObject(response.toString());
			return jObject;
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


}
