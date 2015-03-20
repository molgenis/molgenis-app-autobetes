package org.molgenis.autobetes.controller;

import static org.molgenis.autobetes.controller.MovesController.URI;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.molgenis.auth.MolgenisUser;
import org.molgenis.autobetes.MovesConnector;
import org.molgenis.autobetes.MovesConnectorImpl;
import org.molgenis.autobetes.autobetes.MovesToken;
import org.molgenis.autobetes.autobetes.MovesUserProfile;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.framework.ui.MolgenisPluginController;
import org.molgenis.security.runas.RunAsSystem;
import org.molgenis.security.token.MolgenisToken;
import org.molgenis.security.token.TokenExtractor;
import org.molgenis.util.FileStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

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
	@Value("${movesClientId}")
	private String CLIENT_ID_PARAM_VALUE;
	@Value("${movesClientSecret}")
	private String CLIENT_SECRET_PARAM_VALUE;
	@Value("${movesRedirect_URL}")
	private String MOVES_REDIRECT_URL;
	private static final Logger LOG = LoggerFactory.getLogger(MovesController.class);

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
		return "view-home";
	}

	@RequestMapping(value = "/connect", method = RequestMethod.GET)
	@RunAsSystem
	public String connectToMoves(@RequestParam(value = "code") String authorizationcode, Model model,@RequestParam(value = "token") String token )
	{	
		try{
			
			MolgenisUser user = getUserFromToken(token);
			MovesConnector movesConnector = new MovesConnectorImpl();
			//get moves token 
			MovesToken movesToken = movesConnector.exchangeAutorizationcodeForAccesstoken(user, token, authorizationcode, CLIENT_ID_PARAM_VALUE, CLIENT_SECRET_PARAM_VALUE, MOVES_REDIRECT_URL);
			//check if there allready is a token for the user. If so, overwrite.
			MovesToken entityFromDB = dataService.findOne(MovesToken.ENTITY_NAME, new QueryImpl().eq(MovesToken.OWNER, user), MovesToken.class);
			Entity userProfile;
			if(entityFromDB == null){
				//no MovesToken in db
				//add MovesToken
				dataService.add(MovesToken.ENTITY_NAME, movesToken);
				//get user profile 
				userProfile = movesConnector.getUserProfile(movesToken);
				//add to db
				dataService.add(MovesUserProfile.ENTITY_NAME, userProfile);
			}
			else{
				//allready a MovesToken in db
				//update MovesToken
				movesToken.set(MovesToken.ID, entityFromDB.get(MovesToken.ID));
				dataService.update(MovesToken.ENTITY_NAME, movesToken);
				//check if there allready is a user profile for the
				userProfile = dataService.findOne(MovesUserProfile.ENTITY_NAME, new QueryImpl().eq(MovesUserProfile.MOVESTOKEN,movesToken));
				
				if(userProfile == null){
					//no user profile in db
					//get user profile from moves
					userProfile = movesConnector.getUserProfile(movesToken);
					//add to db
					dataService.add(MovesUserProfile.ENTITY_NAME, userProfile);
				}
			}
			
			movesConnector.manageActivities(dataService, user, CLIENT_ID_PARAM_VALUE, CLIENT_SECRET_PARAM_VALUE);
			
			model.addAttribute("message", "Congratulations, you are now connected to moves.");
			return "view-moves";
		}
		catch(Exception e){
			
			model.addAttribute("message", "Oops something went wrong, please try again later.");
			LOG.error(e.toString());
			return "view-moves";
		}
		

	}
	
	
	/**
	 * Checks if moves is connected
	 * 
	 * Example url: /api/v1/person/99
	 * 
	 * @param entityName
	 * @param id
	 * @param entityMap
	 */

	@RequestMapping(value = "/checkIfMovesIsConnected", method = RequestMethod.GET, produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	public Map<String, Object> checkIfMovesIsConnected(HttpServletRequest servletRequest)
			{
		MolgenisUser user = getUserFromToken(TokenExtractor.getToken(servletRequest));
		MovesToken movesToken = dataService.findOne(MovesToken.ENTITY_NAME, new QueryImpl().eq(MovesToken.OWNER, user), MovesToken.class);

		if(movesToken == null){
			return response(
					false,
					"not connected");


		}

		else{
			return response(
					true,
					"connected");


		}
			}

	private static int getCurrentDate(){
		//get current date in yyyyMMdd format
		DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
		Date date = new Date();
		return Integer.parseInt(dateFormat.format(date));
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
	
	/**
	 * Returns response object.
	 * 
	 * @param success
	 * @param msg
	 * @return
	 */
	private Map<String, Object> response(boolean success, String msg)
	{
		Map<String, Object> result = new HashMap<String, Object>();

		result.put("success", success);
		result.put("message", msg);

		return result;
	}


}
