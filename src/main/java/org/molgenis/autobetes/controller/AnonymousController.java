package org.molgenis.autobetes.controller;

import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.BOOL;
import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.CATEGORICAL;
import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.COMPOUND;
import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.DATE;
import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.DATE_TIME;
import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.DECIMAL;
import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.INT;
import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.LONG;
import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.MREF;
import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.XREF;
import static org.molgenis.autobetes.controller.AnonymousController.URI;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.security.Principal;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.zip.GZIPInputStream;

import sun.net.www.protocol.http.HttpURLConnection;

import javax.net.ssl.HttpsURLConnection;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import javax.validation.Valid;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONValue;
import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.auth.MolgenisGroup;
import org.molgenis.auth.MolgenisGroupMember;
import org.molgenis.auth.MolgenisUser;
import org.molgenis.autobetes.WebAppDatabasePopulatorServiceImpl;
import org.molgenis.autobetes.autobetes.ActivityEvent;
import org.molgenis.autobetes.autobetes.ActivityEventInstance;
import org.molgenis.autobetes.autobetes.BgSensorRpi;
import org.molgenis.autobetes.autobetes.BinaryData;
import org.molgenis.autobetes.autobetes.Event;
import org.molgenis.autobetes.autobetes.EventInstance;
import org.molgenis.autobetes.autobetes.FoodEvent;
import org.molgenis.autobetes.autobetes.FoodEventInstance;
import org.molgenis.autobetes.autobetes.ServerExceptionLog;
import org.molgenis.autobetes.autobetes.TestEvent;
import org.molgenis.autobetes.autobetes.UserInfo;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataConverter;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.rest.AttributeMetaDataResponse;
import org.molgenis.data.rest.EntityCollectionRequest;
import org.molgenis.data.rest.EntityCollectionResponse;
import org.molgenis.data.rest.EntityPager;
import org.molgenis.data.rest.LoginRequest;
import org.molgenis.data.rest.LoginResponse;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.framework.ui.MolgenisPluginController;
import org.molgenis.script.SavedScriptRunner;
import org.molgenis.script.ScriptResult;
import org.molgenis.security.runas.RunAsSystem;
import org.molgenis.security.token.MolgenisToken;
import org.molgenis.security.token.TokenExtractor;
import org.molgenis.security.token.TokenService;
import org.molgenis.util.FileStore;
import org.molgenis.util.MolgenisDateFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.google.common.collect.Lists;

/**
 * Controller that handles anonymous requests
 */
@Controller
@RequestMapping(URI)
public class AnonymousController extends MolgenisPluginController
{
	private static final Logger LOG = LoggerFactory.getLogger(AnonymousController.class);
	public static final String ID = "anonymous";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;
	public static final String BASE_URI = "";
	public static final String TIME_STAMP_LAST_SYNC = "timeStampLastSync";
	public static final String NOT_IN_REQUEST_CONTENT = "notInRequestContent";
	public static final String TRUE = "True";
	public static final String FOOD = "Food";
	public static final int MAX_LENGTH_STRING = 254;
	public static final String ADMIN = "admin";
	public static final String ALLUSERS = "All Users";
	private static final String USER_AGENT = "Mozilla/5.0";
	private final SavedScriptRunner savedScriptRunner;

	//@Autowired
	private DataService dataService;
	private JavaMailSender mailSender;

	@Autowired
	private FileStore fileStore;

	@Autowired
	private SavedScriptRunner scriptRunner;

	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private TokenService tokenService;


	@Autowired
	public AnonymousController(DataService dataService, JavaMailSender mailSender, SavedScriptRunner savedScriptRunner,
			AuthenticationManager authenticationManager, TokenService tokenService)
	{
		super(URI);
		if (dataService == null) throw new IllegalArgumentException("DataService is null!");
		if (mailSender == null) throw new IllegalArgumentException("JavaMailSender is null!");
		if (savedScriptRunner == null) throw new IllegalArgumentException("SavedScriptRunner is null!");
		if (authenticationManager == null) throw new IllegalArgumentException("AuthenticationManager is null!");
		if (tokenService == null) throw new IllegalArgumentException("TokenService is null!");
		this.dataService = dataService;
		this.mailSender = mailSender;
		this.savedScriptRunner = savedScriptRunner;
		this.authenticationManager = authenticationManager;
		this.tokenService = tokenService;
	}

	@RequestMapping
	public String init()
	{
		return "view-home";
	}

	@RequestMapping(value = "/activate/{activationCode}", method = RequestMethod.GET)
	@RunAsSystem
	public String activateUser(@PathVariable String activationCode, Model model)
	{
		MolgenisUser mu = dataService.findOne(MolgenisUser.ENTITY_NAME,
				new QueryImpl().eq(MolgenisUser.ACTIVATIONCODE, activationCode), MolgenisUser.class);

		if (null == mu)
		{
			return "registration-fail";
		}
		else{

			mu.setActive(true);

			dataService.update(MolgenisUser.ENTITY_NAME, mu);

			return "registration-success";
		}
	}
	/**
	 * Copied from RestController. Everything is the same, but for some reason the RunAsSystem does work here but not in the RestController.
	 * 
	 * @param login
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/login", method = POST, produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	@RunAsSystem
	public LoginResponse login(@Valid @RequestBody LoginRequest login, HttpServletRequest request)
	{
		if (login == null)
		{
			throw new HttpMessageNotReadableException("Missing login");
		}

		UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(login.getUsername(),
				login.getPassword());
		authToken.setDetails(new WebAuthenticationDetails(request));

		// Authenticate the login
		Authentication authentication = authenticationManager.authenticate(authToken);
		if (!authentication.isAuthenticated())
		{
			throw new BadCredentialsException("Unknown username or password");
		}

		MolgenisUser user = dataService.findOne(MolgenisUser.ENTITY_NAME,
				new QueryImpl().eq(MolgenisUser.USERNAME, authentication.getName()), MolgenisUser.class);

		// User authenticated, log the user in
		SecurityContextHolder.getContext().setAuthentication(authentication);

		// Generate a new token for the user
		String token = tokenService.generateAndStoreToken(authentication.getName(), "Rest api login");

		return new LoginResponse(token, user.getUsername(), user.getFirstName(), user.getLastName());
	}

	/**
	 * Copied from ScriptRunnerController, only thing changed is the RunAsSystem annotation, this way we can execute scripts without 
	 * them being readable.
	 * @param scriptName
	 * @param parameters
	 * @param response
	 * @throws IOException
	 */
	@RequestMapping(value= "/scripts/{name}/run")
	@RunAsSystem
	public void runScript(@PathVariable("name") String scriptName, @RequestParam Map<String, Object> parameters,
			HttpServletResponse response) throws IOException
	{
		ScriptResult result = savedScriptRunner.runScript(scriptName, parameters);

		if (result.getOutputFile() != null)
		{
			File f = new File(result.getOutputFile());
			if (f.exists())
			{
				String guessedContentType = URLConnection.guessContentTypeFromName(f.getName());
				if (guessedContentType != null)
				{
					response.setContentType(guessedContentType);
				}

				FileCopyUtils.copy(new FileInputStream(f), response.getOutputStream());
				f.delete();
			}
		}
		else if (StringUtils.isNotBlank(result.getOutput()))
		{
			response.setContentType("text/plain");

			PrintWriter pw = response.getWriter();
			pw.write(result.getOutput());
			pw.flush();
		}
	}



	/**
	 * Register user given the registration request
	 * @param registrationRequest
	 * @param servletRequest
	 * @return registrationresponse
	 */
	@RequestMapping(value = "/registerUser", method = RequestMethod.POST, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	@RunAsSystem
	public Map<String, Object> registerUser(@RequestBody RegistrationRequest registrationRequest,
			HttpServletRequest servletRequest)
			{

		MolgenisGroup mg = dataService.findOne(MolgenisGroup.ENTITY_NAME,
				new QueryImpl().eq(MolgenisGroup.NAME, ALLUSERS), MolgenisGroup.class);
		// validate email + pw
		if (StringUtils.isBlank(registrationRequest.getEmail())
				|| StringUtils.isBlank(registrationRequest.getPassword()))
		{
			return response(false, "Registration failed. Please provide a valid email and password!");
		}

		MolgenisUser existingUser = dataService.findOne(MolgenisUser.ENTITY_NAME,
				new QueryImpl().eq(MolgenisUser.EMAIL, registrationRequest.getEmail()), MolgenisUser.class);

		if (null != existingUser)
		{
			return response(false,
					"Registration failed. Email already exists. Please click 'Forgotten' to get a new password.");
		}
		MolgenisUser mu = new MolgenisUser();
		mu.setUsername(registrationRequest.getEmail());
		mu.setPassword(registrationRequest.getPassword());
		mu.setEmail(registrationRequest.getEmail());
		String activationCode = UUID.randomUUID().toString();
		mu.setActivationCode(activationCode);
		mu.setActive(false);

		try
		{
			//add user
			dataService.add(MolgenisUser.ENTITY_NAME, mu);
			//add user to 'All Users' group
			MolgenisGroupMember molgenisGroupMember = new MolgenisGroupMember();
			molgenisGroupMember.setMolgenisGroup(mg);
			molgenisGroupMember.setMolgenisUser(mu);
			dataService.add(MolgenisGroupMember.ENTITY_NAME, molgenisGroupMember);

		}
		catch (Exception e)
		{
			LOG.error("error: " + e.toString());
			return response(false, "Registration failed. Please contact the developers.");
		}

		// send activation email
		try
		{
			String activationUriBase = null;
			if (StringUtils.isEmpty(servletRequest.getHeader("X-Forwarded-Host")))
			{
				activationUriBase = ServletUriComponentsBuilder.fromCurrentRequest().replacePath(URI + "/activate")
						.build().toUriString();
			}
			else
			{
				activationUriBase = servletRequest.getScheme() + "://" + servletRequest.getHeader("X-Forwarded-Host")
						+ URI + "/activate";
			}
			URI activationUri = java.net.URI.create(activationUriBase + '/' + activationCode);

			SimpleMailMessage mailMessage = new SimpleMailMessage();
			mailMessage.setTo(registrationRequest.getEmail());
			mailMessage.setSubject("Registration Autobetes");
			mailMessage.setText("To activate your account, please visit " + activationUri);
			mailMessage.setFrom("info@diadvies.com");
			mailSender.send(mailMessage);
		}
		catch (Exception e)
		{
			Object typedId = dataService.getRepositoryByEntityName(MolgenisUser.ENTITY_NAME).getEntityMetaData()
					.getIdAttribute().getDataType().convert(mu.getIdValue());

			dataService.delete(MolgenisUser.ENTITY_NAME, typedId);

			LOG.error("error: " + e.toString());
			return response(false,
					"Registration failed. Sending email with activation link failed. Please contact the developers if "
							+ registrationRequest.getEmail() + " is really your email address.");
		}

		return response(
				true,
				"Registration successful! We have sent you an email with a link to activate your account. NB The email may have ended up in your spam folder.");

			}




	/**
	 * Updates an entity using PUT
	 * 
	 * Example url: /api/v1/person/99
	 * 
	 * @param entityName
	 * @param id
	 * @param entityMap
	 */
	@RunAsSystem
	@RequestMapping(value = "/syncUserInfo", method = RequestMethod.POST, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	public Map<String, Object> setUserInfo(@RequestBody List<Map<String, Object>> entityMap,
			HttpServletRequest servletRequest)
			{
		MolgenisUser user = getUserFromToken(TokenExtractor.getToken(servletRequest));
		EntityMetaData metaUserInfo = dataService.getEntityMetaData(UserInfo.ENTITY_NAME);
		//get userinfolist
		Iterable<Entity> userInfoList = dataService.findAll(UserInfo.ENTITY_NAME, new QueryImpl().eq(UserInfo.OWNER, user).sort(new Sort(Direction.DESC, UserInfo.LASTCHANGED)));
		Entity entityFromClient = toEntity(metaUserInfo, entityMap.get(0), user);
		if(!userInfoList.iterator().hasNext()){
			//no userinfo of user, add record
			dataService.add(UserInfo.ENTITY_NAME, entityFromClient);
			return getEntityAsMap(entityFromClient, metaUserInfo, null, null);
		}
		else{
			//get userinfo entity with highest timestamp
			Entity userInfo = userInfoList.iterator().next();
			if(userInfo.getDouble(TestEvent.LASTCHANGED) < entityFromClient.getDouble(TestEvent.LASTCHANGED)){
				//entity from client has higher timestamp
				//add client entity

				dataService.add(UserInfo.ENTITY_NAME, entityFromClient);
				return getEntityAsMap(entityFromClient, metaUserInfo, null, null);
			}
			else{
				return getEntityAsMap(userInfo, metaUserInfo, null, null);
			}
		}

			}

	/**
	 * Get timestamp of most recent sensor data of user to which this token belongs! 
	 * @return timestamp
	 */
	@RequestMapping(value = "/sensorLastTimeStamp", method = RequestMethod.GET, produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	public Long sensorLastTimeStamp(HttpServletRequest servletRequest)
	{
		MolgenisUser owner = getUserFromToken(TokenExtractor.getToken(servletRequest));

		//		new QueryImpl().sort(new Sort(Direction.DESC)).offset(0).pageSize(1);
		BgSensorRpi h = dataService.findOne(BgSensorRpi.ENTITY_NAME, new QueryImpl().sort(new Sort(Direction.DESC, BgSensorRpi.DATETIMEMS)).offset(0).pageSize(1), BgSensorRpi.class);

		return h.getDateTimeMs();
	}


	/**
	 * Parses sensor BINARY sensor data into db
	 * @return mm modulo 5 of last record's hh:mm
	 */
	@RequestMapping(value = "/upload/binary/sensor/{page}", method = RequestMethod.POST, headers = "Content-Type=multipart/form-data", produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	public Integer uploadBinary(@RequestParam Part file, HttpServletRequest servletRequest,
			@PathVariable Integer page)
	{
		MolgenisUser user = getUserFromToken(TokenExtractor.getToken(servletRequest));

		// create name
		// ownerId-type-page.binary
		String newFileName = user.getId() + "-sensor-page" + page + ".binary";

		File page2 = null;
		// save in file store
		try
		{
			page2 = fileStore.store(file.getInputStream(), newFileName);
		}
		catch (IOException e)
		{
			LOG.error("error: " + e.toString());
			e.printStackTrace();
			return null;
		}

		long timestamp = System.currentTimeMillis();

		// keep track of file in BinaryData table
		BinaryData bd = new BinaryData();
		bd.setReceived(timestamp);
		bd.setDataType("sensor"); // TODO: don't use hard coded string "sensor", but rather use field propertie if
		// possible
		bd.setPage(page);
		bd.setFileName(newFileName);
		bd.setOwner(user);

		// add or update
		BinaryData bdFromDb = dataService.findOne(BinaryData.ENTITY_NAME,
				new QueryImpl().eq(BinaryData.FILENAME, newFileName), BinaryData.class);
		if (null == bdFromDb)
		{
			dataService.add(BinaryData.ENTITY_NAME, bd);
			bdFromDb = bd;
		}
		else
		{
			// update date time received
			bdFromDb.setReceived(timestamp);
			dataService.update(BinaryData.ENTITY_NAME, bdFromDb);
		}

		// combine most recent 2 or 1 pages of user
		String twoPagesFileName = null;
		try
		{
			twoPagesFileName = concatTwoPages(bdFromDb, page2, user);
		}
		catch (IOException e)
		{
			LOG.error("error: " + e.toString());
			e.printStackTrace();
			return null;
		}

		Integer minute = saveSensorData(twoPagesFileName, user);
		if (null == minute) return -1;

		// TODO: Fix: should return most recent from DB
		return (minute % 5) + 1;
	}

	/**
	 * Saves sensor data in db
	 * @param twoPagesFileName
	 * @param user
	 * @return minute of time last record (HH:{MM} used for next sync)
	 */
	private Integer saveSensorData(String twoPagesFileName, MolgenisUser user)
	{
		// convert to json
		// sensorDataToJson(twoPagesFileName);
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("binaryFile", prependFileStoreDir(twoPagesFileName));

		// TODO don't hard code script name?
		ScriptResult scriptResult = scriptRunner.runScript("binaryToJson", map);
		String json = scriptResult.getOutput();

		// save json in db
		Object jsonObject = JSONValue.parse(json);

		java.util.Date lastRecTimestamp = null;
		GlucoseSensorDataParser gp = new GlucoseSensorDataParser(jsonObject);
		List<BgSensorRpi> lst = new ArrayList<BgSensorRpi>();
		for (int i = 0; i < gp.getList().size(); i++)
		{
			GlucoseSensorData g = gp.getList().get(i);
			BgSensorRpi rec = new BgSensorRpi();
			rec.setOwner(user);
			lastRecTimestamp = g.getDateTime();
			rec.setDateTimeMs(g.getDateTime().getTime());
			rec.setAmount(g.getAmount());

			BgSensorRpi h = dataService.findOne(BgSensorRpi.ENTITY_NAME,
					new QueryImpl().eq(BgSensorRpi.DATETIMEMS, g.getDateTime().getTime()), BgSensorRpi.class);

			if (null == h) // put json list (store list in db below)
				lst.add(rec);
		}

		// add list to db
		dataService.add(BgSensorRpi.ENTITY_NAME, lst);

		// return minute of hour
		if (null == lastRecTimestamp) return -1;
		Calendar cal = Calendar.getInstance();
		cal.setTime(lastRecTimestamp);
		cal.get(Calendar.MINUTE);
		return cal.get(Calendar.MINUTE);
	}

	private String concatTwoPages(BinaryData bd, File page2, MolgenisUser user) throws IOException
	{
		// check page and page minus 1 present
		// if last but one page is indeed the most recently uploaded, then parse last two
		Iterable<BinaryData> bdlst = dataService.findAll(BinaryData.ENTITY_NAME, new QueryImpl().eq(Event.OWNER, user),
				BinaryData.class);

		BinaryData bdPrevPage = null;
		for (BinaryData bdCandidate : bdlst)
		{
			if (null == bdPrevPage)
			{
				bdPrevPage = bdCandidate;
			}
			else
			{
				if (bdPrevPage.getReceived() < bdCandidate.getReceived()
						&& bdCandidate.getReceived() < bd.getReceived())
				{ // new candidate for prev page
					bdPrevPage = bdCandidate;
				}
			}
		}

		Boolean twoPages = bd.getPage().equals(bdPrevPage.getPage() + 1);

		// finalBinaryData file name: userId-page1(-page2).to.be.analyzed.binary
		String binaryDataToBeAnalyzedFileName = user.getId().toString();
		if (twoPages)
		{
			binaryDataToBeAnalyzedFileName += "-" + bdPrevPage.getPage();
		}
		// Create final file to be analyzed:
		binaryDataToBeAnalyzedFileName += "-" + bd.getPage() + ".to.be.analyzed.binary";

		File binaryDataToBeAnalyzedFile = new File(prependFileStoreDir(binaryDataToBeAnalyzedFileName));

		if (twoPages)
		{
			// concatenate two files
			File page1 = fileStore.getFile(bdPrevPage.getFileName());
			FileUtils.copyFile(page1, binaryDataToBeAnalyzedFile);
			FileOutputStream fos = new FileOutputStream(binaryDataToBeAnalyzedFile, true); // append = true
			FileInputStream fis = new FileInputStream(page2);
			byte[] fileBytes = new byte[(int) page2.length()];
			int bytesRead = fis.read(fileBytes, 0, (int) page2.length());
			assert (bytesRead == fileBytes.length);
			assert (bytesRead == (int) page2.length());
			fos.write(fileBytes);
			fos.flush();
			fis.close();
			fos.close();

		}
		else
		{
			// only copy file
			FileUtils.copyFile(page2, binaryDataToBeAnalyzedFile);
		}

		return binaryDataToBeAnalyzedFileName;
	}

	private String prependFileStoreDir(String fileName)
	{
		return fileStore.getStorageDir() + File.separatorChar + fileName;
	}

	@RequestMapping(value = "/getInsulinAdvice", method = RequestMethod.POST, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	public int getInsulinAdvice(@RequestBody Map<String, Object> carbs,
			HttpServletRequest servletRequest)
	{
		return -1;
	}


	/**
	 * Updates an entity using PUT
	 * 
	 * Example url: /api/v1/person/99
	 * 
	 * @param entityName
	 * @param id
	 * @param entityMap
	 */
	@RequestMapping(value = "/sync", method = RequestMethod.POST, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	@RunAsSystem
	public List<Map<String, Object>> sync(@RequestBody List<Map<String, Object>> entityMap,
			HttpServletRequest servletRequest)
			{

		// declare objects
		TimestampLastUpdate timeStampLastSync = new TimestampLastUpdate(0);// timestamp of the last sync of client,
		// send along in requestbody, if not it remains 0

		List<Map<String, Object>> responseData = new ArrayList<Map<String, Object>>();// response list that will be
		// returned to client as a json
		MolgenisUser user = getUserFromToken(TokenExtractor.getToken(servletRequest));
		// metadata is used to convert entity to Map<String, Object> and vice versa
		EntityMetaData metaFoodEvent = dataService.getEntityMetaData(FoodEvent.ENTITY_NAME);
		EntityMetaData metaActivityEvent = dataService.getEntityMetaData(ActivityEvent.ENTITY_NAME);
		EntityMetaData metaFoodEventInstance = dataService.getEntityMetaData(FoodEventInstance.ENTITY_NAME);
		EntityMetaData metaActivityEventInstance = dataService.getEntityMetaData(ActivityEventInstance.ENTITY_NAME);

		HashMap<String, String> reftoevent = new HashMap<String, String>();
		// iterate request list
		iterateListRecursively(reftoevent, 0, timeStampLastSync, user, entityMap, metaFoodEvent, metaActivityEvent,
				metaFoodEventInstance, metaActivityEventInstance);
		// get entities from db and put these in response data
		//standard activities
		getStandardEventsFromDBAndAppendToResponseData(responseData);
		//user specific activities
		getEntitiesFromDBAndAppendToResponseData(FoodEvent.ENTITY_NAME, user, timeStampLastSync.getTimestamp(),
				responseData, metaFoodEvent);
		getEntitiesFromDBAndAppendToResponseData(ActivityEvent.ENTITY_NAME, user, timeStampLastSync.getTimestamp(),
				responseData, metaActivityEvent);

		getEntitiesFromDBAndAppendToResponseData(FoodEventInstance.ENTITY_NAME, user, timeStampLastSync.getTimestamp(),
				responseData, metaFoodEventInstance);
		getEntitiesFromDBAndAppendToResponseData(ActivityEventInstance.ENTITY_NAME, user,
				timeStampLastSync.getTimestamp(), responseData, metaActivityEventInstance);

		return responseData;

			}
	/**
	 * Get standard events from db and append to response data.
	 * @param responseData
	 */
	private void getStandardEventsFromDBAndAppendToResponseData(List<Map<String, Object>> responseData)
	{

		//equals 1 of admin
		// entities from admin that have the particular string in id WebAppDatabasePopulatorServiceImpl.ADMINIDPREPOSITION
		//are standard events
		MolgenisUser adminUser = dataService.findOne(MolgenisUser.ENTITY_NAME, new QueryImpl().eq(MolgenisUser.USERNAME, "admin"),MolgenisUser.class);

		//Iterable<Entity> dbEntities = dataService.findAll(ActivityEvent.ENTITY_NAME,
		//		new QueryImpl().eq(Event.OWNER, adminUser).and().like(Event.ID, WebAppDatabasePopulatorServiceImpl.ADMIN_ID_PREPOSITION));
		//EntityMetaData meta = dataService.getEntityMetaData(ActivityEvent.ENTITY_NAME);
		//appendEntitiesToResponseData(responseData, dbEntities, meta);

		//like() stopt working unfortunately
		//TODO fix like() and use commented code above and rm code below
		Iterable<Entity> dbEntities = dataService.findAll(ActivityEvent.ENTITY_NAME,
				new QueryImpl().eq(Event.OWNER, adminUser).and().eq(Event.ID, WebAppDatabasePopulatorServiceImpl.ADMIN_ID_PREPOSITION+1));
		EntityMetaData meta = dataService.getEntityMetaData(ActivityEvent.ENTITY_NAME);
		appendEntitiesToResponseData(responseData, dbEntities, meta);
		dbEntities = dataService.findAll(ActivityEvent.ENTITY_NAME,
				new QueryImpl().eq(Event.OWNER, adminUser).and().eq(Event.ID, WebAppDatabasePopulatorServiceImpl.ADMIN_ID_PREPOSITION+2));
		appendEntitiesToResponseData(responseData, dbEntities, meta);
	}

	//.or().like(Event.ID, WebAppDatabasePopulatorServiceImpl.ADMINIDPREPOSITION)

	/**
	 * Retrieves entities from db with lastchanged timestamp higher then timeStampLastSync, and appends to responsedata
	 * 
	 * @param entityName
	 * @param user
	 * @param timeStampLastSync
	 * @param responseData
	 * @param meta
	 */
	private void getEntitiesFromDBAndAppendToResponseData(String entityName, MolgenisUser user, long timeStampLastSync,
			List<Map<String, Object>> responseData, EntityMetaData meta)
	{
		//equals 1 of admin
		// get all entities from a user + adminuser
		Iterable<Entity> dbEntities = dataService.findAll(entityName,
				new QueryImpl().eq(Event.OWNER, user).and().ge(Event.LASTCHANGED, timeStampLastSync));
		appendEntitiesToResponseData(responseData, dbEntities, meta);

	}
	/**
	 * Iterates list with entities and appends to response data
	 * @param responseData
	 * @param dbEntities
	 * @param meta
	 */
	private void appendEntitiesToResponseData(List<Map<String, Object>> responseData, Iterable<Entity> dbEntities, EntityMetaData meta){
		// iterate iterable
		for (Entity entity : dbEntities)
		{
			// append to responsedata
			Map<String, Object> entityAsMap = getEntityAsMap(entity, meta, null, null);

			responseData.add(entityAsMap);
		}
	}

	/**
	 * Iterate List with objects recursively and process them. A recursive fashion is chosen because then the list can
	 * be iterated only once and with the assurance that the event entities will be processed first
	 * 
	 * @param reftoevent
	 * @param index
	 * @param timeStampLastSync
	 * @param user
	 * @param entityMap
	 * @param metaFoodEvent
	 * @param metaActivityEvent
	 * @param metaFoodEventInstance
	 * @param metaActivityEventInstance
	 */
	private void iterateListRecursively(HashMap<String, String> reftoevent, int index,
			TimestampLastUpdate timeStampLastSync, MolgenisUser user, List<Map<String, Object>> entityMap,
			EntityMetaData metaFoodEvent, EntityMetaData metaActivityEvent, EntityMetaData metaFoodEventInstance,
			EntityMetaData metaActivityEventInstance)
	{
		try
		{
			if (entityMap.size() > index)
			{

				Map<String, Object> mapEntity = entityMap.get(index);
				if (mapEntity.containsKey("timeStampLastSync"))
				{
					// object is the timestamp
					timeStampLastSync.setTimestamp(Long.valueOf(mapEntity.get(TIME_STAMP_LAST_SYNC).toString())
							.longValue());
					iterateListRecursively(reftoevent, index + 1, timeStampLastSync, user, entityMap, metaFoodEvent,
							metaActivityEvent, metaFoodEventInstance, metaActivityEventInstance);
				}
				else if (mapEntity.containsKey(Event.NAME))
				{

					// object is an event entity
					// events need to be processed first, so first process this object and then proceed iteration

					if (mapEntity.get(Event.EVENTTYPE).equals(FOOD))
					{
						processMapEntity(mapEntity, metaFoodEvent, user);
						iterateListRecursively(reftoevent, index + 1, timeStampLastSync, user, entityMap,
								metaFoodEvent, metaActivityEvent, metaFoodEventInstance, metaActivityEventInstance);

					}
					else
					{

						processMapEntity(mapEntity, metaActivityEvent, user);
						iterateListRecursively(reftoevent, index + 1, timeStampLastSync, user, entityMap,
								metaFoodEvent, metaActivityEvent, metaFoodEventInstance, metaActivityEventInstance);

					}

				}

				else if (mapEntity.containsKey(ActivityEventInstance.INTENSITY))
				{
					// object is an activity entity
					// events need to be processed first, so first proceed iteration and then process this object
					iterateListRecursively(reftoevent, index + 1, timeStampLastSync, user, entityMap, metaFoodEvent,
							metaActivityEvent, metaFoodEventInstance, metaActivityEventInstance);
					processMapEntity(mapEntity, metaActivityEventInstance, user);

				}
				else if (mapEntity.containsKey(FoodEventInstance.AMOUNT))
				{
					// object is an food entity
					// same as activity entity
					iterateListRecursively(reftoevent, index + 1, timeStampLastSync, user, entityMap, metaFoodEvent,
							metaActivityEvent, metaFoodEventInstance, metaActivityEventInstance);
					processMapEntity(mapEntity, metaFoodEventInstance, user);

				}
			}
		}
		catch (Exception e)
		{
			writeExceptionToDB(user, entityMap.get(index).toString(), e.toString());
			LOG.error("error: " + e.toString());
			iterateListRecursively(reftoevent, index + 1, timeStampLastSync, user, entityMap, metaFoodEvent,
					metaActivityEvent, metaFoodEventInstance, metaActivityEventInstance);
		}

	}

	/**
	 * Makes entity from a Map<String, Object> and creates in db entity if not exists or updates entity if entity has a
	 * higher timestamp (more recently modified than the one from db)
	 * 
	 * @param meta
	 * @param request
	 * @param user
	 * @return
	 */
	private void processMapEntity(Map<String, Object> mapEntity, EntityMetaData meta, MolgenisUser user)
	{

		// make entity
		Entity entity = toEntity(meta, mapEntity, user);
		//get entityfromdb
		Entity storedEntity = dataService.findOne(meta.getName(),
				new QueryImpl().eq(Event.OWNER, user).and().eq(Event.ID, entity.get(Event.ID)));

		if (storedEntity == null)
		{

			// no entity in db, add entity
			try
			{
				dataService.add(meta.getName(), entity);
			}
			catch (Exception e)
			{
				writeExceptionToDB(user, entity.toString(), e.toString());
				LOG.error("error: " + e.toString());
			}
		}
		else if(isAdminId(storedEntity.getIdValue().toString())){
			//entity is special event from admin
			//entity is not editable
			//do nothing
		}
		else
		{
			// entity is in db
			// check if entity from client is more recently modified than the one from db
			if (storedEntity.getDouble(TestEvent.LASTCHANGED) < entity.getDouble(TestEvent.LASTCHANGED))
			{
				// indeed client entity more resent


				// set primary key
				entity.set(Event.PRIMARYKEY, storedEntity.get(Event.PRIMARYKEY));

				// entity received from app more recent than on server.
				dataService.update(meta.getName(), entity);
			}
		}

	}
	/**
	 * Checks if id is from admin
	 * @param string
	 * @return
	 */
	private boolean isAdminId(String string)
	{
		if(string.contains("admin")){
			return true;
		}
		else{
			return false;
		}

	}

	/**
	 * Creates a new MapEntity based from a HttpServletRequest copied from restController and slightly modified
	 * 
	 * @param meta
	 * @param mapEntity
	 * @param user
	 * @return
	 */
	private Entity toEntity(EntityMetaData meta, Map<String, Object> mapEntity, MolgenisUser user)
	{
		Entity entity = new MapEntity();

		if (meta.getIdAttribute() != null) entity = new MapEntity(meta.getIdAttribute().getName());

		for (AttributeMetaData attr : meta.getAtomicAttributes())
		{

			//no conversion of the auto_generated and server attributes(these result in nullpointer exceptions)
			if(attr.getName() != Event.PRIMARYKEY && attr.getName() != Event.MOMENT && attr.getName() != Event.OWNER && attr.getName() != Event.__TYPE && !attr.isAuto()){
				try
				{
					String paramName = attr.getName();
					Object paramValue = mapEntity.get(paramName);
					Object value = null;
					FieldTypeEnum dataType = attr.getDataType().getEnumType();
					// an undefined javascript object will be processed as a string "undefined"
					// if datatype is a number the conversion(toEntityValue) will result in an error.
					// therefore this check
					if (paramValue != null && paramValue.equals("undefined"))
					{
						if (dataType == INT || dataType == LONG || dataType == DECIMAL)
						{
							value = null;
						}
					}
					// websql has no true or false, instead it uses 0 and 1,
					// if datatype is a boolean than use the custom method convertDoubleToBoolean
					else if (dataType == BOOL)
					{
						if (paramValue.equals("undefined"))
						{
							value = null;
						}
						else
						{
							value = convertDoubleToBoolean((double) paramValue);
						}
					}
					else
					{
						// surround with try catch, if it fails then value will remain null
						try
						{
							value = toEntityValue(attr, paramValue);
						}
						catch (Exception e)
						{
							LOG.error("Failed to convert parameter value: " + paramValue + " to dataType: "
									+ dataType.toString());
							LOG.error(e.toString());
						}
					}
					entity.set(attr.getName(), value);
				}
				catch (Exception e)
				{
					writeExceptionToDB(user, mapEntity.toString(), e.toString());
					entity.set(attr.getName(), null);

					LOG.error("Could not convert parameter to entityValue: parameter=" + attr.getName() + ", map="
							+ mapEntity.toString());
					LOG.error(e.toString());

				}
			}

		}
		entity.set(Event.OWNER, user);
		return entity;
	}
	/**
	 * convert double to boolean 1 = true 0 = false
	 * @param input
	 * @return
	 */
	private boolean convertDoubleToBoolean(double input)
	{

		if (input > 0)
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	/**
	 * copied from restcontroller
	 * 
	 * @param attr
	 * @param paramValue
	 * @return
	 */
	private Object toEntityValue(AttributeMetaData attr, Object paramValue)
	{
		Object value = null;

		// Treat empty strings as null
		if ((paramValue != null) && (paramValue instanceof String) && StringUtils.isEmpty((String) paramValue))
		{
			paramValue = null;
		}

		if (paramValue != null)
		{
			if (attr.getDataType().getEnumType() == XREF || attr.getDataType().getEnumType() == CATEGORICAL)
			{
				value = dataService.findOne(attr.getRefEntity().getName(), paramValue);
				if (value == null)
				{
					throw new IllegalArgumentException("No " + attr.getRefEntity().getName() + " with id " + paramValue
							+ " found");
				}
			}
			else if (attr.getDataType().getEnumType() == MREF)
			{
				List<Object> ids = DataConverter.toObjectList(paramValue);
				if ((ids != null) && !ids.isEmpty())
				{
					Iterable<Entity> mrefs = dataService.findAll(attr.getRefEntity().getName(), ids);
					List<Entity> mrefList = Lists.newArrayList(mrefs);
					if (mrefList.size() != ids.size())
					{
						throw new IllegalArgumentException("Could not find all referencing ids for  " + attr.getName());
					}

					value = mrefList;
				}
			}
			else
			{
				value = DataConverter.convert(paramValue, attr);
			}
		}
		return value;
	}

	/**
	 * copied from restcontroller, slightly modified Transforms an entity to a Map so it can be transformed to json
	 * 
	 * @param entity
	 * @param meta
	 * @param attributesSet
	 * @param attributeExpandsSet
	 * @return
	 */

	public Map<String, Object> getEntityAsMap(Entity entity, EntityMetaData meta, Set<String> attributesSet,
			Map<String, Set<String>> attributeExpandsSet)
			{
		if (null == entity) throw new IllegalArgumentException("entity is null");

		if (null == meta) throw new IllegalArgumentException("meta is null");

		Map<String, Object> entityMap = new LinkedHashMap<String, Object>();

		for (AttributeMetaData attr : meta.getAtomicAttributes())
		{
			// filter fields
			if (attributesSet != null && !attributesSet.contains(attr.getName().toLowerCase())) continue;

			// TODO remove __Type from jpa entities
			if (attr.isVisible() && !attr.getName().equals("__Type"))
			{
				String attrName = attr.getName();
				FieldTypeEnum attrType = attr.getDataType().getEnumType();

				if (attrType == COMPOUND)
				{
					if (attributeExpandsSet != null && attributeExpandsSet.containsKey(attrName.toLowerCase()))
					{
						Set<String> subAttributesSet = attributeExpandsSet.get(attrName.toLowerCase());
						entityMap.put(attrName, new AttributeMetaDataResponse(meta.getName(), attr, subAttributesSet,
								null));
					}
					else
					{
						String attrHref = String.format(BASE_URI + "/%s/%s/%s", meta.getName(), entity.getIdValue(),
								attrName);
						entityMap.put(attrName, Collections.singletonMap("href", attrHref));
					}
				}
				else if (attrType == DATE)
				{
					Date date = entity.getDate(attrName);
					entityMap
					.put(attrName,
							date != null ? new SimpleDateFormat(MolgenisDateFormat.DATEFORMAT_DATE)
					.format(date) : null);
				}
				else if (attrType == DATE_TIME)
				{
					Date date = entity.getDate(attrName);
					entityMap
					.put(attrName,
							date != null ? new SimpleDateFormat(MolgenisDateFormat.DATEFORMAT_DATETIME)
					.format(date) : null);
				}
				else if (attrType != XREF && attrType != CATEGORICAL && attrType != MREF)
				{
					entityMap.put(attrName, entity.get(attr.getName()));
				}
				else if ((attrType == XREF || attrType == CATEGORICAL) && attributeExpandsSet != null
						&& attributeExpandsSet.containsKey(attrName.toLowerCase()))
				{
					Entity refEntity = entity.getEntity(attr.getName());
					if (refEntity != null)
					{
						Set<String> subAttributesSet = attributeExpandsSet.get(attrName.toLowerCase());
						EntityMetaData refEntityMetaData = dataService.getEntityMetaData(attr.getRefEntity().getName());
						Map<String, Object> refEntityMap = getEntityAsMap(refEntity, refEntityMetaData,
								subAttributesSet, null);
						entityMap.put(attrName, refEntityMap);
					}
				}
				else if (attrType == MREF && attributeExpandsSet != null
						&& attributeExpandsSet.containsKey(attrName.toLowerCase()))
				{
					EntityMetaData refEntityMetaData = dataService.getEntityMetaData(attr.getRefEntity().getName());
					Iterable<Entity> mrefEntities = entity.getEntities(attr.getName());

					Set<String> subAttributesSet = attributeExpandsSet.get(attrName.toLowerCase());
					List<Map<String, Object>> refEntityMaps = new ArrayList<Map<String, Object>>();
					for (Entity refEntity : mrefEntities)
					{
						Map<String, Object> refEntityMap = getEntityAsMap(refEntity, refEntityMetaData,
								subAttributesSet, null);
						refEntityMaps.add(refEntityMap);
					}

					EntityPager pager = new EntityPager(0, new EntityCollectionRequest().getNum(),
							(long) refEntityMaps.size(), mrefEntities);

					String uri = String.format(BASE_URI + "/%s/%s/%s", meta.getName(), entity.getIdValue(), attrName);
					EntityCollectionResponse ecr = new EntityCollectionResponse(pager, refEntityMaps, uri);
					entityMap.put(attrName, ecr);
				}
				else if (attrName == EventInstance.EVENTID)
				{
					Object eventId = entity.getEntity(EventInstance.EVENTID).get(Event.ID);
					entityMap.put(attrName, eventId);
				}
				else if (attrName == Event.OWNER)
				{
					// dont want to include owner data in response
					// do nothing
				}
				else if ((attrType == XREF && entity.get(attr.getName()) != null)
						|| (attrType == CATEGORICAL && entity.get(attr.getName()) != null) || attrType == MREF)
				{
					// Add href to ref field
					Map<String, String> ref = new LinkedHashMap<String, String>();
					ref.put("href",
							String.format(BASE_URI + "/%s/%s/%s", meta.getName(), entity.getIdValue(), attrName));
					entityMap.put(attrName, ref);
				}

			}

		}

		return entityMap;
			}

	/**
	 * Once registering succeeded, this method composes the message to the user.
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
	 * This method writes an exception that occurred during a certain sync, to db.
	 * 
	 * @param user
	 * @param entityAsString
	 * @param exceptionAsString
	 */
	private void writeExceptionToDB(MolgenisUser user, String entityAsString, String exceptionAsString)
	{
		// make a substring of strings if the string is longer than possible
		if (entityAsString.length() > MAX_LENGTH_STRING)
		{
			entityAsString = entityAsString.substring(0, MAX_LENGTH_STRING);
		}
		if (exceptionAsString.length() > MAX_LENGTH_STRING)
		{
			exceptionAsString = exceptionAsString.substring(0, MAX_LENGTH_STRING);
		}
		// first check if exception is allready in db
		Entity dbEntity = dataService.findOne(ServerExceptionLog.ENTITY_NAME,
				new QueryImpl().eq(ServerExceptionLog.OWNER, user).and().eq(ServerExceptionLog.ENTITY, entityAsString)
				.and().eq(ServerExceptionLog.EXCEPTION, exceptionAsString));
		if (dbEntity == null)
		{
			// exception not in db, write now to db
			// make entity
			EntityMetaData meta = dataService.getEntityMetaData(ServerExceptionLog.ENTITY_NAME);
			Map<String, Object> entityMap = new LinkedHashMap<String, Object>();
			entityMap.put(ServerExceptionLog.ENTITY, entityAsString);
			entityMap.put(ServerExceptionLog.EXCEPTION, exceptionAsString);
			Entity entity = toEntity(meta, entityMap, user);
			// write to db
			dataService.add(meta.getName(), entity);

		}
	}

}
