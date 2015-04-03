package org.molgenis.autobetes.controller;

import static org.molgenis.autobetes.controller.HomeController.URI;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.molgenis.autobetes.MovesConnector;
import org.molgenis.autobetes.MovesConnectorImpl;
import org.molgenis.autobetes.autobetes.BasalProfileDefinition;
import org.molgenis.autobetes.autobetes.BasalProfileDefinitionGroup;
import org.molgenis.autobetes.autobetes.BasalProfileStart;
import org.molgenis.autobetes.autobetes.BasalSetting;
import org.molgenis.autobetes.autobetes.BgMeter;
import org.molgenis.autobetes.autobetes.BgSensor;
import org.molgenis.autobetes.autobetes.BolusNormal;
import org.molgenis.autobetes.autobetes.BolusSquare;
import org.molgenis.autobetes.autobetes.ChangeCarbRatio;
import org.molgenis.autobetes.autobetes.ChangeCarbRatioGroup;
import org.molgenis.autobetes.autobetes.ChangeInsulinSensitivity;
import org.molgenis.autobetes.autobetes.ChangeInsulinSensitivityGroup;
import org.molgenis.autobetes.autobetes.ChangeSuspendEnable;
import org.molgenis.autobetes.autobetes.ChangeTempBasal;
import org.molgenis.autobetes.autobetes.ChangeTempBasalPercent;
import org.molgenis.autobetes.autobetes.CurrentBasalProfileGroup;
import org.molgenis.autobetes.autobetes.CurrentInsulinSensitivityGroup;
import org.molgenis.autobetes.autobetes.CurrentBasalProfile;
import org.molgenis.autobetes.autobetes.CurrentInsulinSensitivityGroup;
import org.molgenis.autobetes.autobetes.CurrentInsulinSensitivity;
import org.molgenis.autobetes.autobetes.CurrentCarbRatioGroup;
import org.molgenis.autobetes.autobetes.CurrentCarbRatio;
import org.molgenis.autobetes.autobetes.IdentificationServer;
import org.molgenis.autobetes.autobetes.IdentificationServerMetaData;
import org.molgenis.autobetes.autobetes.MovesActivity;
import org.molgenis.autobetes.autobetes.TimeChange;
import org.molgenis.autobetes.autobetes.TimeChangeMetaData;
import org.molgenis.autobetes.pumpobjectsparser.BasalProfileDefinitionGroupParser;
import org.molgenis.autobetes.pumpobjectsparser.BasalProfileDefinitionParser;
import org.molgenis.autobetes.pumpobjectsparser.BasalProfileStartParser;
import org.molgenis.autobetes.pumpobjectsparser.BgMeterParser;
import org.molgenis.autobetes.pumpobjectsparser.BgSensorParser;
import org.molgenis.autobetes.pumpobjectsparser.BolusNormalParser;
import org.molgenis.autobetes.pumpobjectsparser.BolusSquareParser;
import org.molgenis.autobetes.pumpobjectsparser.ChangeCarbRatioGroupParser;
import org.molgenis.autobetes.pumpobjectsparser.ChangeCarbRatioParser;
import org.molgenis.autobetes.pumpobjectsparser.ChangeInsulinSensitivityGroupParser;
import org.molgenis.autobetes.pumpobjectsparser.ChangeInsulinSensitivityParser;
import org.molgenis.autobetes.pumpobjectsparser.ChangeSuspendEnableParser;
import org.molgenis.autobetes.pumpobjectsparser.ChangeTempBasalParser;
import org.molgenis.autobetes.pumpobjectsparser.ChangeTempBasalPercentParser;
import org.molgenis.autobetes.pumpobjectsparser.CurrentBasalProfileGroupParser;
import org.molgenis.autobetes.pumpobjectsparser.CurrentBasalProfileParser;
import org.molgenis.autobetes.pumpobjectsparser.CurrentCarbRatioGroupParser;
import org.molgenis.autobetes.pumpobjectsparser.CurrentCarbRatioParser;
import org.molgenis.autobetes.pumpobjectsparser.CurrentInsulinSensitivityGroupParser;
import org.molgenis.autobetes.pumpobjectsparser.CurrentInsulinSensitivityParser;
import org.molgenis.autobetes.pumpobjectsparser.TimeChangeParser;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.csv.CsvRepository;
import org.molgenis.data.meta.EntityMetaDataMetaData;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.framework.ui.MolgenisPluginController;
import org.molgenis.auth.MolgenisUser;
import org.molgenis.security.core.utils.SecurityUtils;
import org.molgenis.security.runas.RunAsSystem;
import org.molgenis.security.token.TokenExtractor;
import org.molgenis.util.FileStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

/**
 * Controller that handles home page requests
 */
@Controller
@RequestMapping(URI)
public class HomeController extends MolgenisPluginController
{
	public static final String ID = "home";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;
	public static final String BASE_URI = "";
	private static final Logger LOG = LoggerFactory.getLogger(HomeController.class);
	// private static int BASALTIMESTEP = 3 * 60 * 1000; // 3 min
	private static String HEADER = "HEADER";
	private static String BODY = "BODY";
	private static String CSVSEPARATOR = "CSVSEPARATOR";


	private final static String RAWTYPE = "Onbewerkt: type";
	//	private static String RAWVALUES = "Onbewerkt: waarden";

	private final static String ChangeTimeGHString = "ChangeTimeGH";
	private final static String CalBGForPHString = "CalBGForPH";
	private final static String GlucoseSensorDataString = "GlucoseSensorData";
	private final static String BolusNormalString = "BolusNormal";
	private final static String BolusSquareString = "BolusSquare";
	private final static String ChangeBasalProfilePatternString = "ChangeBasalProfilePattern";
	private final static String ChangeBasalProfileString = "ChangeBasalProfile";
	private final static String BasalProfileStartString = "BasalProfileStart";
	private final static String ChangeTempBasalString = "ChangeTempBasal";
	private final static String ChangeTempBasalPercentString = "ChangeTempBasalPercent";
	private final static String ChangeCarbRatioPatternString = "ChangeCarbRatioPattern";
	private final static String ChangeCarbRatioString = "ChangeCarbRatio";
	private final static String ChangeInsulinSensitivityPatternString = "ChangeInsulinSensitivityPattern";
	private final static String ChangeInsulinSensitivityString = "ChangeInsulinSensitivity";
	private final static String ChangeSuspendEnableString = "ChangeSuspendEnable";
	private final static String CurrentBasalProfilePatternString = "CurrentBasalProfilePattern";
	private final static String CurrentBasalProfileString = "CurrentBasalProfile";
	private final static String CurrentInsulinSensitivityPatternString = "CurrentInsulinSensitivityPattern";
	private final static String CurrentInsulinSensitivityString = "CurrentInsulinSensitivity";
	private final static String CurrentCarbRatioPatternString = "CurrentCarbRatioPattern";
	private final static String CurrentCarbRatioString = "CurrentCarbRatio";
	
	@Value("${movesClientId}")
	private String CLIENT_ID_PARAM_VALUE;
	@Value("${movesClientSecret}")
	private String CLIENT_SECRET_PARAM_VALUE;

	@Autowired
	private DataService dataService;

	@Autowired
	private JavaMailSender mailSender;

	@Autowired
	private FileStore fileStore;

	public HomeController()
	{
		super(URI);
	}

	@RequestMapping
	public String init()
	{
		return "view-home";
	}
	
	@RunAsSystem
	@RequestMapping(value = "/uploadCSV", method = RequestMethod.POST, headers = "Content-Type=multipart/form-data")
	public String uploadCSV( @RequestParam
			Part file, Model model, HttpServletRequest servletRequest, Principal principal)
	{
		
		MolgenisUser user = dataService.findOne(MolgenisUser.ENTITY_NAME, new QueryImpl().eq(MolgenisUser.USERNAME, principal.getName()), MolgenisUser.class);
		try
		{
			File pumpCsvFile = fileStore.store(file.getInputStream(), file.getName());

			String tmpDir = System.getProperty("java.io.tmpdir") + "autobetesCsv" + File.separatorChar;

			// import pump csv data
			importPumpCsvFile(user, pumpCsvFile, new File(tmpDir), tmpDir);

			// Now also import activities from Moves-app!
			MovesConnector movesConnector = new MovesConnectorImpl();
			movesConnector.manageActivities(dataService, user, CLIENT_ID_PARAM_VALUE, CLIENT_SECRET_PARAM_VALUE);
			model.addAttribute("message", "Import success!");
		}
		catch (Exception e)
		{
			model.addAttribute("message", "Error uploading CSV!" + e);

			LOG.error(">> Error uploading CSV!"+e.toString());
		}

		return "view-home";
	}
	
	private void importPumpCsvFile(MolgenisUser molgenisUser, File inputFile, File outputDir, String tmpDir)
	{
		// First put stuff in hashmap with IdOnPump as key, then check entities in db if those IdOnPump are in Hashmap(remove if so)
		//then add list at once! (Performance optimization)
		List<BasalProfileDefinitionGroup> basalProfileDefinitionGroupList = new ArrayList<BasalProfileDefinitionGroup>();
		List<BasalProfileDefinition> basalProfileDefinitionList = new ArrayList<BasalProfileDefinition>();
		List<BasalProfileStart> basalProfileStartList = new ArrayList<BasalProfileStart>();
		List<BgMeter> bgMeterList = new ArrayList<BgMeter>();
		List<ChangeCarbRatio> changeCarbRatioList = new ArrayList<ChangeCarbRatio>();
		List<ChangeCarbRatioGroup> changeCarbRatioGroupList = new ArrayList<ChangeCarbRatioGroup>();
		List<ChangeInsulinSensitivity> changeInsulinSensitivityList = new ArrayList<ChangeInsulinSensitivity>();
		List<ChangeInsulinSensitivityGroup> changeInsulinSensitivityGroupList = new ArrayList<ChangeInsulinSensitivityGroup>();
		List<TimeChange> timeChangeList = new ArrayList<TimeChange>();
		List<BgSensor> bgSensorList = new ArrayList<BgSensor>();
		List<BolusNormal> bolusNormalList = new ArrayList<BolusNormal>();
		List<BolusSquare> bolusSquareList = new ArrayList<BolusSquare>();
		List<ChangeTempBasal> changeTempBasalList = new ArrayList<ChangeTempBasal>();
		List<ChangeTempBasalPercent> changeTempBasalPercentList = new ArrayList<ChangeTempBasalPercent>();
		List<ChangeSuspendEnable> changeSuspendEnableList = new ArrayList<ChangeSuspendEnable>();	 
		List<CurrentBasalProfileGroup> currentBasalProfileGroupList = new ArrayList<CurrentBasalProfileGroup>();
		List<CurrentBasalProfile> currentBasalProfileList = new ArrayList<CurrentBasalProfile>();
		List<CurrentInsulinSensitivityGroup> currentInsulinSensitivityGroupList = new ArrayList<CurrentInsulinSensitivityGroup>();
		List<CurrentInsulinSensitivity> currentInsulinSensitivityList = new ArrayList<CurrentInsulinSensitivity>();
		List<CurrentCarbRatioGroup> currentCarbRatioGroupList = new ArrayList<CurrentCarbRatioGroup>();
		List<CurrentCarbRatio> currentCarbRatioList = new ArrayList<CurrentCarbRatio>();
		// define 'unique' body file name
		String random = Long.toHexString(Double.doubleToLongBits(Math.random())).substring(0, 4);
		File bodyFile = new File(tmpDir + random + ".txt");
		Character separator = null;

		// split header and body
		try
		{
			LinkedHashMap<String, String> fsplit;
			fsplit = splitInHeaderTail(inputFile);
			separator = fsplit.get(CSVSEPARATOR).charAt(0);;
			// work around: save as file so that we can read it in again with csvReader..
			FileUtils.writeStringToFile(bodyFile, fsplit.get(BODY));
		}
		catch (IOException e)
		{
			LOG.error(">> ERROR >> when (1) reading input (2) splitting in header/body (3) and save body. error: "+e.toString());
			e.printStackTrace();
		}

		// Read and separate bodyFile 
		CsvRepository csvRepo = new CsvRepository(bodyFile, null, separator);

		// Set stores which entities cannot be loaded so that we do not show duplicates
		Set<String> rawTypeSet = new HashSet<String>();

		//Iterable<Entity> extendedEntities = dataService.query(EntityMetaDataMetaData.ENTITY_NAME).eq(EntityMetaDataMetaData.EXTENDS, IdentificationServer.ENTITY_NAME);
		//Iterable<Entity> extendedEntities = dataService.findAll(EntityMetaDataMetaData.ENTITY_NAME, new QueryImpl().eq(EntityMetaDataMetaData.EXTENDS, IdentificationServer.ENTITY_NAME));
		HashSet<String> existingIDs = getExistingIDS(molgenisUser);




		System.out.println(existingIDs.toString());
		for (Entity e : csvRepo)
		{
			String rawType = (String) e.get(RAWTYPE);
			//			System.out.println(">> Parsing: " + rawType + ": " + e.toString());
			switch (rawType)
			{
				case ChangeTimeGHString:
					TimeChange tc = new TimeChangeParser(e, dataService, molgenisUser).getTc();
					if (!alreadyExists(tc.getIdOnPump(), existingIDs))
						timeChangeList.add(tc);
					break;

				case CalBGForPHString:
					BgMeter bm = new BgMeterParser(e, dataService, molgenisUser).getBm();
					if (!alreadyExists(bm.getIdOnPump(), existingIDs))
						bgMeterList.add(bm);
					break;

				case GlucoseSensorDataString:
					BgSensor bg = new BgSensorParser(e, dataService, molgenisUser).getBgSensor();
					if (!alreadyExists(bg.getIdOnPump(), existingIDs))
						bgSensorList.add(bg);
					break;

				case BolusNormalString:
					BolusNormal bn = new BolusNormalParser(e, dataService, molgenisUser).getBn();
					if (!alreadyExists(bn.getIdOnPump(), existingIDs))
						bolusNormalList.add(bn);
					break;

				case BolusSquareString:
					BolusSquare bs = new BolusSquareParser(e, dataService, molgenisUser).getBs();
					if (!alreadyExists( bs.getIdOnPump(), existingIDs))
						bolusSquareList.add(bs);
					//bolusSquareList.put(bs.getIdOnPump(), bs);
					break;

				case ChangeTempBasalString:
					ChangeTempBasal ctbp = new ChangeTempBasalParser(e, dataService, molgenisUser).getCtbp();
					if (!alreadyExists( ctbp.getIdOnPump(), existingIDs))
						changeTempBasalList.add(ctbp);
					break;

				case ChangeTempBasalPercentString:
					ChangeTempBasalPercent ctb = new ChangeTempBasalPercentParser(e, dataService, molgenisUser).getCtb();
					if (!alreadyExists( ctb.getIdOnPump(), existingIDs))
						changeTempBasalPercentList.add(ctb);
					break;

				case BasalProfileStartString:
					BasalProfileStart bps = new BasalProfileStartParser(e, dataService, molgenisUser).getBps();
					if (!alreadyExists( bps.getIdOnPump(), existingIDs))
						basalProfileStartList.add(bps);
					break;					

				case ChangeSuspendEnableString:
					ChangeSuspendEnable cse = new ChangeSuspendEnableParser(e, dataService, molgenisUser).getSuspend();
					if (!alreadyExists( cse.getIdOnPump(), existingIDs))
						changeSuspendEnableList.add(cse);
					break;					

				case ChangeBasalProfilePatternString: // postfix 'Pre' means 'previous'
					BasalProfileDefinitionGroup bpdg = new BasalProfileDefinitionGroupParser(e, dataService, molgenisUser).getBpdg();
					if (!alreadyExists( bpdg.getIdOnPump(), existingIDs))
						basalProfileDefinitionGroupList.add(bpdg);
					break;

				case ChangeBasalProfileString: // postfix 'Pre' means 'previous'
					BasalProfileDefinition bpd = new BasalProfileDefinitionParser(e, dataService, molgenisUser).getBPD();
					if (!alreadyExists( bpd.getIdOnPump(), existingIDs))
						basalProfileDefinitionList.add(bpd);
					break;

				case ChangeCarbRatioPatternString:
					ChangeCarbRatioGroup ccrg = new ChangeCarbRatioGroupParser(e, dataService, molgenisUser).getCcrg();
					if (!alreadyExists( ccrg.getIdOnPump(), existingIDs))
						changeCarbRatioGroupList.add(ccrg);
					break;

				case ChangeCarbRatioString:
					ChangeCarbRatio ccr = new ChangeCarbRatioParser(e, dataService, molgenisUser).getCcr();
					if (!alreadyExists( ccr.getIdOnPump(), existingIDs))
						changeCarbRatioList.add(ccr);
					break;

				case ChangeInsulinSensitivityPatternString:
					ChangeInsulinSensitivityGroup cisg = new ChangeInsulinSensitivityGroupParser(e, dataService, molgenisUser).getCisg();
					if (!alreadyExists( cisg.getIdOnPump(), existingIDs))
						changeInsulinSensitivityGroupList.add(cisg);
					break;

				case ChangeInsulinSensitivityString:
					ChangeInsulinSensitivity cis = new ChangeInsulinSensitivityParser(e, dataService, molgenisUser).getCis();
					if (!alreadyExists( cis.getIdOnPump(), existingIDs))
						changeInsulinSensitivityList.add(cis);
					break;
				case CurrentBasalProfilePatternString:
					CurrentBasalProfileGroup cbpdg = new CurrentBasalProfileGroupParser(e, dataService, molgenisUser).getCbpdg();
					if (!alreadyExists( cbpdg.getIdOnPump(), existingIDs))
						currentBasalProfileGroupList.add(cbpdg);
					break;
					
				case CurrentBasalProfileString:
					CurrentBasalProfile cbpd = new CurrentBasalProfileParser(e, dataService, molgenisUser).getCBPD();
					if (!alreadyExists( cbpd.getIdOnPump(), existingIDs))
						currentBasalProfileList.add(cbpd);
					break;
				case CurrentInsulinSensitivityPatternString:
					CurrentInsulinSensitivityGroup cisg2 = new CurrentInsulinSensitivityGroupParser(e, dataService, molgenisUser).getCisg();
					if (!alreadyExists( cisg2.getIdOnPump(), existingIDs))
						currentInsulinSensitivityGroupList.add(cisg2);
					break;
				case CurrentInsulinSensitivityString:
					CurrentInsulinSensitivity cis2 = new CurrentInsulinSensitivityParser(e, dataService, molgenisUser).getCis();
					if (!alreadyExists( cis2.getIdOnPump(), existingIDs))
						currentInsulinSensitivityList.add(cis2);
					break;
				case CurrentCarbRatioPatternString:
					CurrentCarbRatioGroup ccrgp = new CurrentCarbRatioGroupParser(e, dataService, molgenisUser).getCcrg();
					if (!alreadyExists( ccrgp.getIdOnPump(), existingIDs))
						currentCarbRatioGroupList.add(ccrgp);
					break;
				case CurrentCarbRatioString:
					CurrentCarbRatio ccr2 = new CurrentCarbRatioParser(e, dataService, molgenisUser).getCcr();
					if (!alreadyExists( ccr2.getIdOnPump(), existingIDs))
						currentCarbRatioList.add(ccr2);
					break;
				default: // print if not parsed
					if (!rawTypeSet.contains(rawType))
					{
						System.out.println(rawType);// + ":   " + e.toString());
						rawTypeSet.add(rawType);
					}
					break;
			}
		}


		dataService.add(BasalProfileDefinitionGroup.ENTITY_NAME, basalProfileDefinitionGroupList);
		dataService.add(BasalProfileDefinition.ENTITY_NAME, basalProfileDefinitionList);
		dataService.add(BasalProfileStart.ENTITY_NAME, basalProfileStartList);
		dataService.add(BgMeter.ENTITY_NAME, bgMeterList);
		dataService.add(ChangeCarbRatio.ENTITY_NAME, changeCarbRatioList);
		dataService.add(ChangeCarbRatioGroup.ENTITY_NAME, changeCarbRatioGroupList);
		dataService.add(ChangeInsulinSensitivity.ENTITY_NAME, changeInsulinSensitivityList);
		dataService.add(ChangeInsulinSensitivityGroup.ENTITY_NAME, changeInsulinSensitivityGroupList);
		dataService.add(TimeChange.ENTITY_NAME, timeChangeList);
		dataService.add(BgSensor.ENTITY_NAME, bgSensorList);
		dataService.add(BolusNormal.ENTITY_NAME, bolusNormalList);
		dataService.add(BolusSquare.ENTITY_NAME, bolusSquareList);
		dataService.add(ChangeTempBasal.ENTITY_NAME, changeTempBasalList);
		dataService.add(ChangeTempBasalPercent.ENTITY_NAME, changeTempBasalPercentList);
		dataService.add(ChangeSuspendEnable.ENTITY_NAME, changeSuspendEnableList);
		dataService.add(CurrentBasalProfileGroup.ENTITY_NAME, currentBasalProfileGroupList); 
		dataService.add(CurrentBasalProfile.ENTITY_NAME, currentBasalProfileList);
		dataService.add(CurrentInsulinSensitivityGroup.ENTITY_NAME, currentInsulinSensitivityGroupList); 
		dataService.add(CurrentInsulinSensitivity.ENTITY_NAME, currentInsulinSensitivityList); 
		dataService.add(CurrentCarbRatioGroup.ENTITY_NAME, currentCarbRatioGroupList); 
		dataService.add(CurrentCarbRatio.ENTITY_NAME, currentCarbRatioList);

		// TODO SUSPEND
		IOUtils.closeQuietly(csvRepo);
	}
	
	private HashSet<String> getExistingIDS(MolgenisUser molgenisUser)
	{
		HashSet<String> hashSetIDs = new HashSet<String>();
		Iterable<Entity> extendedEntities = dataService.findAll(BgSensor.ENTITY_NAME, new QueryImpl().eq(BgSensor.OWNER, molgenisUser));
		for(Entity e : extendedEntities)
		{
			hashSetIDs.add((String) e.get(IdentificationServer.IDONPUMP));
		}
		extendedEntities = dataService.findAll(TimeChange.ENTITY_NAME, new QueryImpl().eq(TimeChange.OWNER, molgenisUser));
		for(Entity e : extendedEntities)
		{
			hashSetIDs.add((String) e.get(IdentificationServer.IDONPUMP));
		}
		extendedEntities = dataService.findAll(BgMeter.ENTITY_NAME, new QueryImpl().eq(BgMeter.OWNER, molgenisUser));
		for(Entity e : extendedEntities)
		{
			hashSetIDs.add((String) e.get(IdentificationServer.IDONPUMP));
		}

		extendedEntities = dataService.findAll(BolusNormal.ENTITY_NAME, new QueryImpl().eq(BolusNormal.OWNER, molgenisUser));
		for(Entity e : extendedEntities)
		{
			hashSetIDs.add((String) e.get(IdentificationServer.IDONPUMP));
		}
		extendedEntities = dataService.findAll(BolusSquare.ENTITY_NAME, new QueryImpl().eq(BolusSquare.OWNER, molgenisUser));
		for(Entity e : extendedEntities)
		{
			hashSetIDs.add((String) e.get(IdentificationServer.IDONPUMP));
		}
		extendedEntities = dataService.findAll(BasalProfileDefinitionGroup.ENTITY_NAME, new QueryImpl().eq(BasalProfileDefinitionGroup.OWNER, molgenisUser));
		for(Entity e : extendedEntities)
		{
			hashSetIDs.add((String) e.get(IdentificationServer.IDONPUMP));
		}
		extendedEntities = dataService.findAll(BasalProfileDefinition.ENTITY_NAME, new QueryImpl().eq(BasalProfileDefinition.OWNER, molgenisUser));
		for(Entity e : extendedEntities)
		{
			hashSetIDs.add((String) e.get(IdentificationServer.IDONPUMP));
		}
		extendedEntities = dataService.findAll(BasalProfileDefinition.ENTITY_NAME, new QueryImpl().eq(BasalProfileDefinition.OWNER, molgenisUser));
		for(Entity e : extendedEntities)
		{
			hashSetIDs.add((String) e.get(IdentificationServer.IDONPUMP));
		}
		extendedEntities = dataService.findAll(BasalProfileStart.ENTITY_NAME, new QueryImpl().eq(BasalProfileStart.OWNER, molgenisUser));
		for(Entity e : extendedEntities)
		{
			hashSetIDs.add((String) e.get(IdentificationServer.IDONPUMP));
		}

		extendedEntities = dataService.findAll(ChangeTempBasal.ENTITY_NAME, new QueryImpl().eq(ChangeTempBasal.OWNER, molgenisUser));
		for(Entity e : extendedEntities)
		{
			hashSetIDs.add((String) e.get(IdentificationServer.IDONPUMP));
		}
		extendedEntities = dataService.findAll(ChangeTempBasalPercent.ENTITY_NAME, new QueryImpl().eq(ChangeTempBasalPercent.OWNER, molgenisUser));
		for(Entity e : extendedEntities)
		{
			hashSetIDs.add((String) e.get(IdentificationServer.IDONPUMP));
		}
		extendedEntities = dataService.findAll(ChangeCarbRatioGroup.ENTITY_NAME, new QueryImpl().eq(ChangeCarbRatioGroup.OWNER, molgenisUser));
		for(Entity e : extendedEntities)
		{
			hashSetIDs.add((String) e.get(IdentificationServer.IDONPUMP));
		}
		extendedEntities = dataService.findAll(ChangeCarbRatio.ENTITY_NAME, new QueryImpl().eq(ChangeCarbRatio.OWNER, molgenisUser));
		for(Entity e : extendedEntities)
		{
			hashSetIDs.add((String) e.get(IdentificationServer.IDONPUMP));
		}
		extendedEntities = dataService.findAll(ChangeInsulinSensitivityGroup.ENTITY_NAME, new QueryImpl().eq(ChangeInsulinSensitivityGroup.OWNER, molgenisUser));
		for(Entity e : extendedEntities)
		{
			hashSetIDs.add((String) e.get(IdentificationServer.IDONPUMP));
		}
		extendedEntities = dataService.findAll(ChangeInsulinSensitivity.ENTITY_NAME, new QueryImpl().eq(ChangeInsulinSensitivity.OWNER, molgenisUser));
		for(Entity e : extendedEntities)
		{
			hashSetIDs.add((String) e.get(IdentificationServer.IDONPUMP));
		}
		extendedEntities = dataService.findAll(ChangeSuspendEnable.ENTITY_NAME, new QueryImpl().eq(ChangeSuspendEnable.OWNER, molgenisUser));
		for(Entity e : extendedEntities)
		{
			hashSetIDs.add((String) e.get(IdentificationServer.IDONPUMP));
		}
		extendedEntities = dataService.findAll(BasalSetting.ENTITY_NAME, new QueryImpl().eq(BasalSetting.OWNER, molgenisUser));
		for(Entity e : extendedEntities)
		{
			hashSetIDs.add((String) e.get(IdentificationServer.IDONPUMP));
		}
		extendedEntities = dataService.findAll(CurrentBasalProfileGroup.ENTITY_NAME, new QueryImpl().eq(CurrentBasalProfileGroup.OWNER, molgenisUser));
		for(Entity e : extendedEntities)
		{
			hashSetIDs.add((String) e.get(IdentificationServer.IDONPUMP));
		}
		extendedEntities = dataService.findAll(CurrentBasalProfile.ENTITY_NAME, new QueryImpl().eq(CurrentBasalProfile.OWNER, molgenisUser));
		for(Entity e : extendedEntities)
		{
			hashSetIDs.add((String) e.get(IdentificationServer.IDONPUMP));
		}
		extendedEntities = dataService.findAll(CurrentInsulinSensitivityGroup.ENTITY_NAME, new QueryImpl().eq(CurrentInsulinSensitivityGroup.OWNER, molgenisUser));
		for(Entity e : extendedEntities)
		{
			hashSetIDs.add((String) e.get(IdentificationServer.IDONPUMP));
		}
		extendedEntities = dataService.findAll(CurrentInsulinSensitivity.ENTITY_NAME, new QueryImpl().eq(CurrentInsulinSensitivity.OWNER, molgenisUser));
		for(Entity e : extendedEntities)
		{
			hashSetIDs.add((String) e.get(IdentificationServer.IDONPUMP));
		}
		extendedEntities = dataService.findAll(CurrentCarbRatioGroup.ENTITY_NAME, new QueryImpl().eq(CurrentCarbRatioGroup.OWNER, molgenisUser));
		for(Entity e : extendedEntities)
		{
			hashSetIDs.add((String) e.get(IdentificationServer.IDONPUMP));
		}
		extendedEntities = dataService.findAll(CurrentCarbRatio.ENTITY_NAME, new QueryImpl().eq(CurrentCarbRatio.OWNER, molgenisUser));
		for(Entity e : extendedEntities)
		{
			hashSetIDs.add((String) e.get(IdentificationServer.IDONPUMP));
		}

		return hashSetIDs;
	}

	private boolean alreadyExists(String id, HashSet<String> existingIDs)
	{
		return existingIDs.contains(id);
	}

	/*
	 * Split file in header and body
	 */
	private static LinkedHashMap<String, String> splitInHeaderTail(File f) throws IOException
	{
		String content = fileToString(f);

		int lineIndex = 0, positionNewline = 0, nHeaderLines = 11;

		// TODO do this smarter; e.g. assume header ends when number of separaters is
		// big (or maybe even equal to a certain number)

		// Replace all \r carriage return, which is ^M on Windows, by \n
		String newlineChar = "\n";
		content = content.replace('\r', '\n');

		for (positionNewline = content.indexOf(newlineChar); positionNewline != -1 && lineIndex < nHeaderLines - 1; positionNewline = content
				.indexOf(newlineChar, positionNewline + 1))
		{
			lineIndex++;
		}

		String header = content.substring(0, positionNewline + 1);
		String body = content.substring(positionNewline + 1);

		LinkedHashMap<String, String> fsplit = new LinkedHashMap<String, String>();
		fsplit.put(HEADER, header);
		fsplit.put(BODY, body);

		// guess sep based on number of occurences of ',' and ';' in header
		int nComma = StringUtils.countOccurrencesOf(header, ",");
		int nSemiColon = StringUtils.countOccurrencesOf(header, ";");

		if (nSemiColon < nComma) fsplit.put(CSVSEPARATOR, ",");
		else fsplit.put(CSVSEPARATOR, ";");

		return fsplit;
	}

	private static String fileToString(File f) throws IOException
	{
		FileInputStream stream = new FileInputStream(f);
		try
		{
			FileChannel fc = stream.getChannel();
			MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
			/* Instead of using default, pass in a decoder. */
			return Charset.defaultCharset().decode(bb).toString();
		}
		finally
		{
			stream.close();
		}
	}

	@RequestMapping("upload-csv")
	public String uploadForm() throws InterruptedException
	{
		return "view-upload-pump-csv";
	}

	@RequestMapping("view-report")
	public String viewLogo() throws InterruptedException
	{
		return "view-report";
	}

	@RequestMapping("upload")
	public String upload() throws InterruptedException
	{
		return "view-upload";
	}

	@RequestMapping(method = RequestMethod.POST, value = "/validate")
	@PreAuthorize("hasAnyRole('ROLE_SU')")
	public String validate(HttpServletRequest request, @RequestParam("csvFile") MultipartFile csvFile, Model model)
			throws IOException, MessagingException, Exception
	{
		System.err.println("in validate...");
		boolean submitState = false;
		String action = "/validate";
		String enctype = "multipart/form-data";

		final List<String> messages = new ArrayList<String>();
		if (!csvFile.isEmpty())
		{
			try
			{
				System.err.println("Gelukt!");
			}
			catch (Exception e)
			{
				LOG.error(e.toString());
			}
		}
		else
		{
			String errorMessage = "The file you try to upload is empty! Filename: " + csvFile.getOriginalFilename();
			// messages.add(errorMessage);
			System.err.println(errorMessage);
		}
		model.addAttribute("action", action);
		model.addAttribute("enctype", enctype);
		model.addAttribute("submit_state", submitState);
		model.addAttribute("messages", messages);
		return "view-upload";
	}
}
