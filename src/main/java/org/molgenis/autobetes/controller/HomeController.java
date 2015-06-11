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
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
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
import org.molgenis.autobetes.autobetes.BGCapturedOnPump;
import org.molgenis.autobetes.autobetes.BGReceived;
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
import org.molgenis.autobetes.autobetes.CurrentSensorBGUnits;
import org.molgenis.autobetes.autobetes.IdentificationServer;
import org.molgenis.autobetes.autobetes.IdentificationServerMetaData;
import org.molgenis.autobetes.autobetes.MovesActivity;
import org.molgenis.autobetes.autobetes.SensorCal;
import org.molgenis.autobetes.autobetes.SensorCalBG;
import org.molgenis.autobetes.autobetes.SensorCalFactor;
import org.molgenis.autobetes.autobetes.SensorPacket;
import org.molgenis.autobetes.autobetes.SensorWeakSignal;
import org.molgenis.autobetes.autobetes.TimeChange;
import org.molgenis.autobetes.autobetes.TimeChangeMetaData;
import org.molgenis.autobetes.autobetes.UnabsorbedInsulin;
import org.molgenis.autobetes.autobetes.UserInfo;
import org.molgenis.autobetes.pumpobjectsparser.BGCapturedOnPumpParser;
import org.molgenis.autobetes.pumpobjectsparser.BGReceivedParser;
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
import org.molgenis.autobetes.pumpobjectsparser.CurrentSensorBGUnitsParser;
import org.molgenis.autobetes.pumpobjectsparser.SensorCalBGParser;
import org.molgenis.autobetes.pumpobjectsparser.SensorCalFactorParser;
import org.molgenis.autobetes.pumpobjectsparser.SensorCalParser;
import org.molgenis.autobetes.pumpobjectsparser.SensorPacketParser;
import org.molgenis.autobetes.pumpobjectsparser.SensorWeakSignalParser;
import org.molgenis.autobetes.pumpobjectsparser.TimeChangeParser;
import org.molgenis.autobetes.pumpobjectsparser.TimeOffset;
import org.molgenis.autobetes.pumpobjectsparser.UnabsorbedInsulinParser;
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

	private final static String ChangeTimeGHString = "ChangeTime";
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

	private final static String SensorCalString = "SensorCal";
	private final static String SensorCalBGString = "SensorCalBg";
	private final static String SensorCalFactorString = "SensorCalFactor";
	private final static String SensorWeakSignalString = "SensorWeakSignal";
	private final static String BGCapturedOnPumpString = "BGCapturedOnPump";
	private final static String BGReceivedString = "BGReceived";
	private final static String UnabsorbedInsulinString = "UnabsorbedInsulin";
	private final static String SensorPacketString = "SensorPacket";
	private final static String CurrentSensorBGUnitsString = "CurrentSensorBGUnits";


	private Long HOURS_IN_MILLISEC = 1000l*60l*60l;
	@Value("${movesClientId}")
	private String CLIENT_ID_PARAM_VALUE;
	@Value("${movesClientSecret}")
	private String CLIENT_SECRET_PARAM_VALUE;

	//@Autowired 
	private DataService dataService;

	//@Autowired
	private FileStore fileStore;
	/**
	 * Class constructor
	 * @param f
	 * @return
	 * @throws IOException
	 */
	@Autowired
	public HomeController(DataService dataService, FileStore fileStore)
	{
		super(URI);
		if (dataService == null) throw new IllegalArgumentException("DataService is null!");
		if (fileStore == null) throw new IllegalArgumentException("Filestore is null!");
		this.dataService = dataService;
		this.fileStore = fileStore;	
	}
	/**
	 * Default request mapping
	 * @return home page
	 */
	@RequestMapping
	public String init()
	{
		return "view-home";
	}
	/**
	 * Upload csv plugin
	 * @param file
	 * @param model
	 * @param servletRequest
	 * @param principal
	 * @return homepage
	 */
	@RunAsSystem
	@RequestMapping(value = "/uploadCSV", method = RequestMethod.POST, headers = "Content-Type=multipart/form-data")
	public String uploadCSV( @RequestParam
			Part file, Model model, HttpServletRequest servletRequest, Principal principal)
	{
		//get user
		MolgenisUser user = dataService.findOne(MolgenisUser.ENTITY_NAME, new QueryImpl().eq(MolgenisUser.USERNAME, principal.getName()), MolgenisUser.class);
		try
		{
			//get csv
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
	/**
	 * Iterates, parses and saves csv rows
	 * @param molgenisUser
	 * @param inputFile
	 * @param outputDir
	 * @param tmpDir
	 */
	private void importPumpCsvFile(MolgenisUser molgenisUser, File inputFile, File outputDir, String tmpDir)
	{
		List<IdentificationServer> tsToBeCorrected = new ArrayList<IdentificationServer>();
		//Put entities first in list and then add list at once! (Performance optimization)
		//TODO: make a generic list
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
		List<SensorCal> sensorCalList = new ArrayList<SensorCal>();
		List<SensorCalBG> sensorCalBGList = new ArrayList<SensorCalBG>();
		List<SensorCalFactor> sensorCalFactorList = new ArrayList<SensorCalFactor>();
		List<BGCapturedOnPump> bgCapturedOnPumpList = new ArrayList<BGCapturedOnPump>();
		List<BGReceived> bgReceivedList = new ArrayList<BGReceived>();
		List<UnabsorbedInsulin> unabsorbedInsulinList = new ArrayList<UnabsorbedInsulin>();
		List<SensorPacket> sensorPacketList = new ArrayList<SensorPacket>();
		List<SensorWeakSignal> sensorWeakSignalList = new ArrayList<SensorWeakSignal>();
		List<CurrentSensorBGUnits> currentSensorBGUnitsList = new ArrayList<CurrentSensorBGUnits>();

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

		// Make hashmap containing unparsed entity types
		HashMap<String, Integer> rawTypeSet = new HashMap<String, Integer>();
		//get ids in order to check if id is allready in db
		HashSet<String> existingIDs = getExistingIDS(molgenisUser);


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
					tsToBeCorrected.add(tc);
					break;
				case CalBGForPHString:
					BgMeter bm = new BgMeterParser(e, dataService, molgenisUser).getBm();
					if (!alreadyExists(bm.getIdOnPump(), existingIDs))
						bgMeterList.add(bm);
					tsToBeCorrected.add(bm);
					break;

				case GlucoseSensorDataString:
					BgSensor bg = new BgSensorParser(e, dataService, molgenisUser).getBgSensor();
					if (!alreadyExists(bg.getIdOnPump(), existingIDs))
						bgSensorList.add(bg);
					tsToBeCorrected.add(bg);
					break;

				case BolusNormalString:
					BolusNormal bn = new BolusNormalParser(e, dataService, molgenisUser).getBn();
					if (!alreadyExists(bn.getIdOnPump(), existingIDs))
						bolusNormalList.add(bn);
					tsToBeCorrected.add(bn);
					break;

				case BolusSquareString:
					BolusSquare bs = new BolusSquareParser(e, dataService, molgenisUser).getBs();
					if (!alreadyExists( bs.getIdOnPump(), existingIDs))
						bolusSquareList.add(bs);
					tsToBeCorrected.add(bs);
					//bolusSquareList.put(bs.getIdOnPump(), bs);
					break;

				case ChangeTempBasalString:
					ChangeTempBasal ctbp = new ChangeTempBasalParser(e, dataService, molgenisUser).getCtbp();
					if (!alreadyExists( ctbp.getIdOnPump(), existingIDs))
						changeTempBasalList.add(ctbp);
					tsToBeCorrected.add(ctbp);
					break;

				case ChangeTempBasalPercentString:
					ChangeTempBasalPercent ctb = new ChangeTempBasalPercentParser(e, dataService, molgenisUser).getCtb();
					if (!alreadyExists( ctb.getIdOnPump(), existingIDs))
						changeTempBasalPercentList.add(ctb);
					tsToBeCorrected.add(ctb);
					break;

				case BasalProfileStartString:
					BasalProfileStart bps = new BasalProfileStartParser(e, dataService, molgenisUser).getBps();
					if (!alreadyExists( bps.getIdOnPump(), existingIDs))
						basalProfileStartList.add(bps);
					tsToBeCorrected.add(bps);
					break;					

				case ChangeSuspendEnableString:
					ChangeSuspendEnable cse = new ChangeSuspendEnableParser(e, dataService, molgenisUser).getSuspend();
					if (!alreadyExists( cse.getIdOnPump(), existingIDs))
						changeSuspendEnableList.add(cse);
					tsToBeCorrected.add(cse);
					break;					

				case ChangeBasalProfilePatternString: // postfix 'Pre' means 'previous'
					BasalProfileDefinitionGroup bpdg = new BasalProfileDefinitionGroupParser(e, dataService, molgenisUser).getBpdg();
					if (!alreadyExists( bpdg.getIdOnPump(), existingIDs))
						basalProfileDefinitionGroupList.add(bpdg);
					tsToBeCorrected.add(bpdg);
					break;

				case ChangeBasalProfileString: // postfix 'Pre' means 'previous'
					BasalProfileDefinition bpd = new BasalProfileDefinitionParser(e, dataService, molgenisUser).getBPD();
					if (!alreadyExists( bpd.getIdOnPump(), existingIDs))
						basalProfileDefinitionList.add(bpd);
					tsToBeCorrected.add(bpd);
					break;

				case ChangeCarbRatioPatternString:
					ChangeCarbRatioGroup ccrg = new ChangeCarbRatioGroupParser(e, dataService, molgenisUser).getCcrg();
					if (!alreadyExists( ccrg.getIdOnPump(), existingIDs))
						changeCarbRatioGroupList.add(ccrg);
					tsToBeCorrected.add(ccrg);
					break;

				case ChangeCarbRatioString:
					ChangeCarbRatio ccr = new ChangeCarbRatioParser(e, dataService, molgenisUser).getCcr();
					if (!alreadyExists( ccr.getIdOnPump(), existingIDs))
						changeCarbRatioList.add(ccr);
					tsToBeCorrected.add(ccr);
					break;

				case ChangeInsulinSensitivityPatternString:
					ChangeInsulinSensitivityGroup cisg = new ChangeInsulinSensitivityGroupParser(e, dataService, molgenisUser).getCisg();
					if (!alreadyExists( cisg.getIdOnPump(), existingIDs))
						changeInsulinSensitivityGroupList.add(cisg);
					tsToBeCorrected.add(cisg);
					break;

				case ChangeInsulinSensitivityString:
					ChangeInsulinSensitivity cis = new ChangeInsulinSensitivityParser(e, dataService, molgenisUser).getCis();
					if (!alreadyExists( cis.getIdOnPump(), existingIDs))
						changeInsulinSensitivityList.add(cis);
					tsToBeCorrected.add(cis);
					break;
				case CurrentBasalProfilePatternString:
					CurrentBasalProfileGroup cbpdg = new CurrentBasalProfileGroupParser(e, dataService, molgenisUser).getCbpdg();
					if (!alreadyExists( cbpdg.getIdOnPump(), existingIDs))
						currentBasalProfileGroupList.add(cbpdg);
					tsToBeCorrected.add(cbpdg);
					break;

				case CurrentBasalProfileString:
					CurrentBasalProfile cbpd = new CurrentBasalProfileParser(e, dataService, molgenisUser).getCBPD();
					if (!alreadyExists( cbpd.getIdOnPump(), existingIDs))
						currentBasalProfileList.add(cbpd);
					tsToBeCorrected.add(cbpd);
					break;
				case CurrentInsulinSensitivityPatternString:
					CurrentInsulinSensitivityGroup cisg2 = new CurrentInsulinSensitivityGroupParser(e, dataService, molgenisUser).getCisg();
					if (!alreadyExists( cisg2.getIdOnPump(), existingIDs))
						currentInsulinSensitivityGroupList.add(cisg2);
					tsToBeCorrected.add(cisg2);
					break;
				case CurrentInsulinSensitivityString:
					CurrentInsulinSensitivity cis2 = new CurrentInsulinSensitivityParser(e, dataService, molgenisUser).getCis();
					if (!alreadyExists( cis2.getIdOnPump(), existingIDs))
						currentInsulinSensitivityList.add(cis2);
					tsToBeCorrected.add(cis2);
					break;
				case CurrentCarbRatioPatternString:
					CurrentCarbRatioGroup ccrgp = new CurrentCarbRatioGroupParser(e, dataService, molgenisUser).getCcrg();
					if (!alreadyExists( ccrgp.getIdOnPump(), existingIDs))
						currentCarbRatioGroupList.add(ccrgp);
					tsToBeCorrected.add(ccrgp);
					break;
				case CurrentCarbRatioString:
					CurrentCarbRatio ccr2 = new CurrentCarbRatioParser(e, dataService, molgenisUser).getCcr();
					if (!alreadyExists( ccr2.getIdOnPump(), existingIDs))
						currentCarbRatioList.add(ccr2);
					tsToBeCorrected.add(ccr2);
					break;
				case SensorCalString:
					SensorCal sc = new SensorCalParser(e, dataService, molgenisUser).getSc();
					if (!alreadyExists( sc.getIdOnPump(), existingIDs))
						sensorCalList.add(sc);
					tsToBeCorrected.add(sc);
					break;
				case SensorCalBGString:
					SensorCalBG scbg = new SensorCalBGParser(e, dataService, molgenisUser).getScbg();
					if (!alreadyExists( scbg.getIdOnPump(), existingIDs))
						sensorCalBGList.add(scbg);
					tsToBeCorrected.add(scbg);
					break;
				case SensorCalFactorString:
					SensorCalFactor scf = new SensorCalFactorParser(e, dataService, molgenisUser).getScf();
					if (!alreadyExists( scf.getIdOnPump(), existingIDs))
						sensorCalFactorList.add(scf);
					tsToBeCorrected.add(scf);
					break;
				case BGCapturedOnPumpString:
					BGCapturedOnPump bgcp = new BGCapturedOnPumpParser(e, dataService, molgenisUser).getBgcp();
					if (!alreadyExists( bgcp.getIdOnPump(), existingIDs))
						bgCapturedOnPumpList.add(bgcp);
					tsToBeCorrected.add(bgcp);
					break;
				case BGReceivedString:
					BGReceived bgr = new BGReceivedParser(e, dataService, molgenisUser).getBGR();
					if (!alreadyExists( bgr.getIdOnPump(), existingIDs))
						bgReceivedList.add(bgr);
					tsToBeCorrected.add(bgr);
					break;
				case UnabsorbedInsulinString:
					UnabsorbedInsulin ui = new UnabsorbedInsulinParser(e, dataService, molgenisUser).getUi();
					if (!alreadyExists( ui.getIdOnPump(), existingIDs))
						unabsorbedInsulinList.add(ui);
					tsToBeCorrected.add(ui);
					break;
				case SensorPacketString:
					SensorPacket sp = new SensorPacketParser(e, dataService, molgenisUser).getSpt();
					if (!alreadyExists( sp.getIdOnPump(), existingIDs))
						sensorPacketList.add(sp);
					tsToBeCorrected.add(sp);
					break;
				case SensorWeakSignalString:
					SensorWeakSignal sws = new SensorWeakSignalParser(e, dataService, molgenisUser).getSws();
					if (!alreadyExists( sws.getIdOnPump(), existingIDs))
						sensorWeakSignalList.add(sws);
					tsToBeCorrected.add(sws);
					break;
				case CurrentSensorBGUnitsString:
					CurrentSensorBGUnits csbu = new CurrentSensorBGUnitsParser(e, dataService, molgenisUser).getCsbu();
					if (!alreadyExists( csbu.getIdOnPump(), existingIDs))
						currentSensorBGUnitsList.add(csbu);
					tsToBeCorrected.add(csbu);
					break;
				default: // print if not parsed
					if (!rawTypeSet.containsKey(rawType))
					{

						rawTypeSet.put(rawType, 1);
					}
					else{
						int count = rawTypeSet.get(rawType);
						rawTypeSet.put(rawType, count+1);
					}
					break;
			}
		}
		for(String key: rawTypeSet.keySet())
		{
			System.out.println(key+": "+rawTypeSet.get(key));
		}
		performTimeCorrection(dataService, molgenisUser, tsToBeCorrected, timeChangeList);

		//add entities to db
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

		dataService.add(SensorCal.ENTITY_NAME, sensorCalList);
		dataService.add(SensorCalBG.ENTITY_NAME, sensorCalBGList);
		dataService.add(SensorCalFactor.ENTITY_NAME, sensorCalFactorList);
		dataService.add(SensorWeakSignal.ENTITY_NAME, sensorWeakSignalList);
		dataService.add(BGCapturedOnPump.ENTITY_NAME, bgCapturedOnPumpList);
		dataService.add(BGReceived.ENTITY_NAME, bgReceivedList);
		dataService.add(UnabsorbedInsulin.ENTITY_NAME, unabsorbedInsulinList);
		dataService.add(SensorPacket.ENTITY_NAME, sensorPacketList);
		dataService.add(CurrentSensorBGUnits.ENTITY_NAME,currentSensorBGUnitsList);



		// TODO SUSPEND
		IOUtils.closeQuietly(csvRepo);
	}

	/**
	 * Add all idonpump to hashset
	 * @param hashSetIDs
	 * @param molgenisUser
	 * @param entityname
	 */
	private void addIdsOfEntityToSet(HashSet<String> hashSetIDs, MolgenisUser molgenisUser,String entityname)
	{
		//get all entities
		Iterable<Entity> extendedEntities = dataService.findAll(entityname, new QueryImpl().eq(IdentificationServer.OWNER, molgenisUser));
		//iterate and add entities
		for(Entity e : extendedEntities)
		{
			hashSetIDs.add((String) e.get(IdentificationServer.IDONPUMP));
		}
	}
	/**
	 * Get existing ids and add to one hashset
	 * @param molgenisUser
	 * @return hashset containing all ids
	 */
	private HashSet<String> getExistingIDS(MolgenisUser molgenisUser)
	{
		HashSet<String> hashSetIDs = new HashSet<String>();
		addIdsOfEntityToSet(hashSetIDs, molgenisUser, BgSensor.ENTITY_NAME);
		addIdsOfEntityToSet(hashSetIDs, molgenisUser, TimeChange.ENTITY_NAME);
		addIdsOfEntityToSet(hashSetIDs, molgenisUser, BgMeter.ENTITY_NAME);
		addIdsOfEntityToSet(hashSetIDs, molgenisUser, BolusNormal.ENTITY_NAME);
		addIdsOfEntityToSet(hashSetIDs, molgenisUser, BolusSquare.ENTITY_NAME);
		addIdsOfEntityToSet(hashSetIDs, molgenisUser, BasalProfileDefinitionGroup.ENTITY_NAME);
		addIdsOfEntityToSet(hashSetIDs, molgenisUser, BasalProfileDefinition.ENTITY_NAME);
		addIdsOfEntityToSet(hashSetIDs, molgenisUser, BasalProfileStart.ENTITY_NAME);
		addIdsOfEntityToSet(hashSetIDs, molgenisUser, ChangeTempBasal.ENTITY_NAME);
		addIdsOfEntityToSet(hashSetIDs, molgenisUser, ChangeTempBasalPercent.ENTITY_NAME);
		addIdsOfEntityToSet(hashSetIDs, molgenisUser, ChangeCarbRatioGroup.ENTITY_NAME);
		addIdsOfEntityToSet(hashSetIDs, molgenisUser, ChangeCarbRatio.ENTITY_NAME);
		addIdsOfEntityToSet(hashSetIDs, molgenisUser, ChangeInsulinSensitivityGroup.ENTITY_NAME);
		addIdsOfEntityToSet(hashSetIDs, molgenisUser, ChangeInsulinSensitivity.ENTITY_NAME);
		addIdsOfEntityToSet(hashSetIDs, molgenisUser, ChangeSuspendEnable.ENTITY_NAME);
		addIdsOfEntityToSet(hashSetIDs, molgenisUser, BasalSetting.ENTITY_NAME);
		addIdsOfEntityToSet(hashSetIDs, molgenisUser, CurrentBasalProfileGroup.ENTITY_NAME);
		addIdsOfEntityToSet(hashSetIDs, molgenisUser, CurrentBasalProfile.ENTITY_NAME);
		addIdsOfEntityToSet(hashSetIDs, molgenisUser, CurrentInsulinSensitivityGroup.ENTITY_NAME);
		addIdsOfEntityToSet(hashSetIDs, molgenisUser, CurrentInsulinSensitivity.ENTITY_NAME);
		addIdsOfEntityToSet(hashSetIDs, molgenisUser, CurrentCarbRatioGroup.ENTITY_NAME);
		addIdsOfEntityToSet(hashSetIDs, molgenisUser, CurrentCarbRatio.ENTITY_NAME);
		addIdsOfEntityToSet(hashSetIDs, molgenisUser, SensorCal.ENTITY_NAME);
		addIdsOfEntityToSet(hashSetIDs, molgenisUser, SensorCalBG.ENTITY_NAME);
		addIdsOfEntityToSet(hashSetIDs, molgenisUser, SensorCalFactor.ENTITY_NAME);
		addIdsOfEntityToSet(hashSetIDs, molgenisUser, SensorWeakSignal.ENTITY_NAME);
		addIdsOfEntityToSet(hashSetIDs, molgenisUser, SensorCal.ENTITY_NAME);
		addIdsOfEntityToSet(hashSetIDs, molgenisUser, BGCapturedOnPump.ENTITY_NAME);
		addIdsOfEntityToSet(hashSetIDs, molgenisUser, BGReceived.ENTITY_NAME);
		addIdsOfEntityToSet(hashSetIDs, molgenisUser, UnabsorbedInsulin.ENTITY_NAME);
		addIdsOfEntityToSet(hashSetIDs, molgenisUser, SensorPacket.ENTITY_NAME);
		addIdsOfEntityToSet(hashSetIDs, molgenisUser, CurrentSensorBGUnits.ENTITY_NAME);
		return hashSetIDs;
	}
	/**
	 * Checks if id is in hashset
	 * @param id
	 * @param existingIDs
	 * @return
	 */
	private boolean alreadyExists(String id, HashSet<String> existingIDs)
	{
		return existingIDs.contains(id);
	}


	/**
	 * Split file in header and body
	 * @param f
	 * @return
	 * @throws IOException
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
	/**
	 * Convert file to string
	 * @param f
	 * @return file as string
	 * @throws IOException
	 */
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
	/**
	 * Upload csv request mapping
	 * @return string
	 * @throws InterruptedException
	 */
	@RequestMapping("upload-csv")
	public String uploadForm() throws InterruptedException
	{
		return "view-upload-pump-csv";
	}
	/**
	 * View report request mapping
	 * @return string
	 * @throws InterruptedException
	 */
	@RequestMapping("view-report")
	public String viewLogo() throws InterruptedException
	{
		return "view-report";
	}
	/**
	 * Upload request mapping
	 * @return string
	 * @throws InterruptedException
	 */
	@RequestMapping("upload")
	public String upload() throws InterruptedException
	{
		return "view-upload";
	}
	/**
	 * Validate csv file
	 * @param request
	 * @param csvFile
	 * @param model
	 * @return string
	 * @throws IOException
	 * @throws MessagingException
	 * @throws Exception
	 */
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

	/**
	 * Correct timestamp according to timezone recorded by app
	 * @param dataService
	 * @param tsToBeCorrected
	 * @param timeChangeList
	 * @return 
	 */
	public List<IdentificationServer> performTimeCorrection(DataService dataService, MolgenisUser molgenisUser, List<IdentificationServer> tsToBeCorrected,
			List<TimeChange> timeChangeList)
			{

		//get get first record app(ra1).
		UserInfo ra1 =  getFirstUserRecord(dataService, molgenisUser);
		TimeOffset ts0 = new TimeOffset(ra1.getLastchanged(), ra1.getTimeOffset());
		//get all timechanges after ts0
		List<TimeChange> timeChanges = getTimeCanges(dataService, molgenisUser, timeChangeList, ts0);
		//we now have an ordered list of timechanges that happened after ts0, each indicate the change in hours before and after a certain moment().
		//rm ts's that are in range(+/- timechange) of a certain timechange
		removeRecordsInTimechangeRange(tsToBeCorrected, timeChanges);
		//this list will contain all the timeOffsets recorded by the pump + the first timeoffset of the app(index = 0)
		List<TimeOffset> timeOffsets = makeListOffTimeOffsets(ts0, timeChanges);
		//according to the right timeoffset for each unixtimeoriginal, compute unixtimecorrected
		correctTimestamps(tsToBeCorrected, timeOffsets);
		return tsToBeCorrected;
			}

	/**
	 * Remove records that are near a time zone shift.
	 * @param tsToBeCorrected
	 * @param timeChanges
	 */
	private void removeRecordsInTimechangeRange(List<IdentificationServer> tsToBeCorrected, List<TimeChange> timeChanges)
	{
		//iterate records
		Iterator<IdentificationServer> it = tsToBeCorrected.iterator();
		while(it.hasNext())
		{
			IdentificationServer ts = it.next();
			for(TimeChange tc: timeChanges)
			{
				//specify range
				long diff = (tc.getUnixtimeOriginal()-tc.getNewTime())*2;
				long min;
				long max;
				if(diff<0){
					min = tc.getUnixtimeOriginal()+diff;
					max = tc.getUnixtimeOriginal()-diff;
				}
				else
				{
					min = tc.getUnixtimeOriginal()-diff;
					max = tc.getUnixtimeOriginal()+diff;
				}

				if(ts.getUnixtimeOriginal()>min && ts.getUnixtimeOriginal()<max)
				{
					//in range need to be deleted
					it.remove();
				}
			}
		}
	}
	/**
	 * Correct timestamps
	 * @param tsToBeCorrected
	 * @param timeOffsets
	 */
	private void correctTimestamps(List<IdentificationServer> tsToBeCorrected, List<TimeOffset> timeOffsets)
	{
		for(int o =0; o< tsToBeCorrected.size();o++)
		{
			long ts = tsToBeCorrected.get(o).getUnixtimeOriginal();

			if(ts<timeOffsets.get(0).tsOfCorrection){
				//was before first record of app
				//do not correct
			}
			else if(timeOffsets.size() == 1){
				//only one timeoffset record of app and this timestamp is after
				int timeoffset = timeOffsets.get(0).timeOffset;
				tsToBeCorrected.get(o).setUnixtimeCorrected( ts - (timeoffset*HOURS_IN_MILLISEC));
			}
			else if(ts>timeOffsets.get(timeOffsets.size()-1).tsOfCorrection)
			{
				//timestamp is after last timeoffset record
				int timeoffset = timeOffsets.get(timeOffsets.size()-1).timeOffset;
				tsToBeCorrected.get(o).setUnixtimeCorrected( ts - (timeoffset*HOURS_IN_MILLISEC));
			}
			else{
				for(int i =1; i< timeOffsets.size(); i++)
				{
					//timestamp is inbetween 2 timechange records
					TimeOffset to = timeOffsets.get(i);

					if(ts<to.tsOfCorrection)
					{
						int timeoffset = timeOffsets.get(i-1).timeOffset;
						tsToBeCorrected.get(o).setUnixtimeCorrected( ts - (timeoffset*HOURS_IN_MILLISEC));
					}

					i= timeOffsets.size();//end forloop
				}



			}
		}

	}
	/**
	 * Makes an timeline of timezone changes
	 * @param ts0
	 * @param timeChanges
	 * @return list of timezone changes
	 */
	private List<TimeOffset> makeListOffTimeOffsets(TimeOffset ts0, List<TimeChange> timeChanges)
	{
		List<TimeOffset> timeOffsets =  new ArrayList<TimeOffset>();
		timeOffsets.add(ts0);//add first record of app to the list
		for(int i =0; i< timeChanges.size(); i++)
		{
			TimeChange tc = timeChanges.get(i);
			//get previous offset in order to define new offset
			int prevOffset;
			if(i==0){
				prevOffset =ts0.timeOffset;
			}
			else{
				prevOffset =timeOffsets.get(i-1).timeOffset;
			}
			//get new timeOffset
			int changeInHours = Math.round((tc.getNewTime()-tc.getUnixtimeOriginal())/1000/60/60);
			int newOffset = prevOffset + changeInHours;
			//add to timeOffsets
			timeOffsets.add(new TimeOffset(tc.getUnixtimeOriginal(), newOffset));

		}
		return timeOffsets;
	}
	/**
	 * Get first userrecord of app and get the recorded timezone
	 * @param dataService
	 * @param molgenisUser
	 * @return UserInfo
	 */
	private UserInfo getFirstUserRecord(DataService dataService, MolgenisUser molgenisUser){
		Iterator<UserInfo> userrecords = dataService.findAll(UserInfo.ENTITY_NAME, new QueryImpl().eq(UserInfo.OWNER, molgenisUser).and().sort(Direction.ASC, UserInfo.LASTCHANGED), UserInfo.class).iterator();
		UserInfo ra1 = null;

		while(userrecords.hasNext()){
			//iterate through user records, get the first one that hase the time offset info
			UserInfo userrecord = userrecords.next();
			if(userrecord.getTimeOffset()!=null)
			{
				if(ra1==null)
				{
					ra1 = userrecord;
				}
			}
		}

		if(ra1==null){
			//no app records yet
			//we cannot correct
			throw new RuntimeException("An error occurred. We do not know you timezone, please go to the Autobetes app and press 'Force synchronization' at the settings screen.");
		}
		return ra1;
	}

	/**
	 * Get all timechanges
	 * @param dataService2
	 * @param molgenisUser
	 * @param timeChangeList
	 * @param ts0
	 * @return list of timechanges
	 */
	private ArrayList<TimeChange> getTimeCanges(DataService dataService2, MolgenisUser molgenisUser, List<TimeChange> timeChangeList, TimeOffset ts0)
	{
		ArrayList<TimeChange> timechanges = new ArrayList<TimeChange>();
		Iterable<TimeChange> dbTimechanges = dataService.findAll(TimeChange.ENTITY_NAME, new QueryImpl().eq(TimeChange.OWNER, molgenisUser).and().sort(Direction.ASC, TimeChange.UNIXTIMEORIGINAL), TimeChange.class);
		for(TimeChange tc : dbTimechanges)
		{
			//we do not consider timechanges before using the app, so check if timechange is after
			//the first app's offset
			if(tc.getUnixtimeOriginal() > ts0.tsOfCorrection)
			{
				timechanges.add(tc);
			}
		}
		for(TimeChange tc : timeChangeList)
		{
			//same as above
			if(tc.getUnixtimeOriginal() > ts0.tsOfCorrection)
			{
				timechanges.add(tc);
			}
		}
		timechanges.sort(new CompareTimeOffsets());

		return timechanges;
	}
	/**
	 * Compare 2 time changes
	 * 
	 */
	public class CompareTimeOffsets implements Comparator<TimeChange> {
		@Override
		public int compare(TimeChange to1, TimeChange to2) {
			long to1ts = to1.getUnixtimeOriginal();
			long to2ts = to1.getUnixtimeOriginal();
			return Long.compare(to1ts,to2ts);
		}
	}
}
