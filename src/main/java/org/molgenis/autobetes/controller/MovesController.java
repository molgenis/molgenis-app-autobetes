package org.molgenis.autobetes.controller;

import static org.molgenis.autobetes.controller.MovesController.URI;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.util.zip.GZIPInputStream;

import org.codehaus.jettison.json.JSONObject;
import org.elasticsearch.http.HttpStats;
import org.molgenis.autobetes.MovesConnector;
import org.molgenis.autobetes.MovesConnectorImpl;
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
	private static final String CLIENT_ID_PARAM_VALUE = "Da6TIHoVori74lacfuVk9QxzlIM5xy9E";
	private static final String CLIENT_SECRET_PARAM_VALUE = "4jLntt7PFe8c9K05YSh_S3_jA2n7GlnDeIeqwL4EwGrE0G824u97xpS38g21nC2k";
	

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

	@RequestMapping(value = "/getInfo", method = RequestMethod.GET)
	public String getInfo()
	{
		MolgenisUser user = dataService.findOne(MolgenisUser.ENTITY_NAME, new QueryImpl().eq(MolgenisUser.USERNAME, "admin"), MolgenisUser.class);
		Entity movesEntityFromDB= dataService.findOne(MovesToken.ENTITY_NAME,
				new QueryImpl().eq(MovesToken.OWNER, user));
		MovesConnector movesConnector = new MovesConnectorImpl();
		boolean isValid = movesConnector.accessTokenIsValid(movesEntityFromDB.get(MovesToken.ACCESSTOKEN).toString());
		if(isValid){
			//retrieve info
			System.out.println("retrieve info!");
		}
		else{
			//access token not valid
			//refresh token
			MapEntity newEntity = movesConnector.refreshToken(movesEntityFromDB.getString(MovesToken.REFRESH_TOKEN) ,user, CLIENT_ID_PARAM_VALUE, CLIENT_SECRET_PARAM_VALUE);
			//save token
			newEntity.set(MovesToken.ID, movesEntityFromDB.get(MovesToken.ID));
			dataService.update(MovesToken.ENTITY_NAME, newEntity);
			//retrieve info
		}
		return "view-home";
	}
	@RequestMapping(value = "/connect", method = RequestMethod.GET)
	public String connectToMoves(@RequestParam(value = "code") String authorizationcode, Model model,@RequestParam(value = "token") String token )
	{
		try{
			MolgenisUser user = getUserFromToken(token);
			MovesConnector movesConnector = new MovesConnectorImpl();
			MapEntity entity = movesConnector.exchangeAutorizationcodeForAccesstoken(user, token, authorizationcode, CLIENT_ID_PARAM_VALUE, CLIENT_SECRET_PARAM_VALUE);

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
