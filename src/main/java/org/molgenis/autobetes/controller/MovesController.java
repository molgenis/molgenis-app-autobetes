package org.molgenis.autobetes.controller;

import static org.molgenis.autobetes.controller.MovesController.URI;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.util.zip.GZIPInputStream;

import org.codehaus.jettison.json.JSONObject;
import org.molgenis.autobetes.autobetes.Event;
import org.molgenis.autobetes.autobetes.MovesToken;
import org.molgenis.data.DataService;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.framework.ui.MolgenisPluginController;
import org.molgenis.omx.auth.MolgenisUser;
import org.molgenis.security.token.MolgenisToken;
import org.molgenis.util.FileStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import 	javax.net.ssl.HttpsURLConnection;

import org.molgenis.data.Entity;

/**
 * Controller that handles home page requests
 */
@Controller
@RequestMapping(URI)
public class MovesController extends MolgenisPluginController
{
	public static final String ID = "moves";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;
	public static final String BASE_URI = "";
	public static final String CLIENT_ID = "Da6TIHoVori74lacfuVk9QxzlIM5xy9E";
	public static final String CLIENT_SECRET = "4jLntt7PFe8c9K05YSh_S3_jA2n7GlnDeIeqwL4EwGrE0G824u97xpS38g21nC2k";
	private final String USER_AGENT = "Mozilla/5.0";


	// private static int BASALTIMESTEP = 3 * 60 * 1000; // 3 min
	private static String HEADER = "HEADER";
	private static String BODY = "BODY";


	@Autowired
	private DataService dataService;

	@Autowired
	private JavaMailSender mailSender;

	@Autowired
	private FileStore fileStore;

	public MovesController()
	{
		super(URI);
	}

	@RequestMapping
	public String init()
	{
		//System.out.println(SecurityUtils.)
		return "view-home";
	}

	@RequestMapping(value = "/connect", method = RequestMethod.GET)
	public String connectToMoves(@RequestParam(value = "code") String authorizationcode, Model model,@RequestParam(value = "token") String token )
	{
		try{
			MolgenisUser user = getUserFromToken(token);
			MapEntity entity = exchangeAutorizationcodeForAccesstoken(user, token, authorizationcode, CLIENT_ID, CLIENT_SECRET);
			
			Entity entityFromDB= dataService.findOne(MovesToken.ENTITY_NAME,
					new QueryImpl().eq(MovesToken.OWNER, user));
			
			System.out.println("entity from DB:"+ entityFromDB);
			
			if(entityFromDB == null){
				
				dataService.add(MovesToken.ENTITY_NAME, entity);
			}
			else{
				entity.set(MovesToken.ID, entityFromDB.get(MovesToken.ID));
				dataService.update(MovesToken.ENTITY_NAME, entity);
			}
			
			model.addAttribute("code", authorizationcode);
			model.addAttribute("token", token);
			model.addAttribute("username", user.getUsername());
			model.addAttribute("message", "Congratulations, you are now connected to moves.");
			return "view-moves";
		}
		catch(Exception e){
			System.out.println(e.toString());
			return "view-home";
		}

	}
	
	private MapEntity exchangeAutorizationcodeForAccesstoken(MolgenisUser user, String token, String authorizationcode, String clientId, String secretId){
		try{
			String url = "https://api.moves-app.com/oauth/v1/access_token?grant_type=authorization_code&code="+authorizationcode+"&client_id="+clientId+"&client_secret="+secretId+"&redirect_uri=http://autobetes.nl?token="+token;
			//String url = "https://api.moves-app.com/api/1.1/user/activities/daily?from=20141119&to=20141128&access_token=_MJnP57s9Bto6h9qNFyubozuI24y3UI3fZ2q755uVDx1nf8xyV77255YHUEXd9o2";
			URL obj = new URL(url);
			HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

			//add reuqest header
			con.setDoOutput(true);
			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Type", "application/json; charset=utf8");
			con.setRequestProperty("User-Agent", "Mozilla/5.0");
			con.setRequestProperty("Accept" ,"*/*");
			con.setRequestProperty("Accept-Encoding", "gzip,deflate");
			con.setRequestProperty("Accept-Language", "en-US,en;q=0.8");
			
			String input = "{}";
			// Send post request
	        OutputStream os = con.getOutputStream();
	        os.write(input.getBytes("UTF-8"));
	        os.close();
	        
			int responseCode = con.getResponseCode();
			if(responseCode == 400){
				throw new Exception("Unauthorized");
			}
			else{
				//read response
				BufferedReader in = new BufferedReader(
						new InputStreamReader(new GZIPInputStream(con.getInputStream())));

				String inputLine;
				StringBuffer response = new StringBuffer();
				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				
				}
				in.close();
				JSONObject jObject = new JSONObject(response.toString());	
				MapEntity entity = new MapEntity(MovesToken.ENTITY_NAME);
				entity.set(MovesToken.OWNER, user);
				entity.set(MovesToken.ACCESSTOKEN, jObject.get("access_token").toString());
				entity.set(MovesToken.TOKEN_TYPE, jObject.get("token_type").toString());
				entity.set(MovesToken.EXPIRES_IN, jObject.get("expires_in").toString() );
				entity.set(MovesToken.REFRESH_TOKEN, jObject.get("refresh_token").toString() );
				entity.set(MovesToken.USER_ID, jObject.get("user_id").toString() );
				return(entity);
			}
			
		}
		catch(Exception e){
			return null;
		}
	}

	/**
	 * Declares user according to the given token
	 * 
	 * @param token
	 * @return
	 */
	public MolgenisUser getUserFromToken(String token)
	{

		MolgenisToken tokenEntity = dataService.findOne(MolgenisToken.ENTITY_NAME,
				new QueryImpl().eq(MolgenisToken.TOKEN, token), MolgenisToken.class);
		return tokenEntity.getMolgenisUser();
	}


}
