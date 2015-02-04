package org.molgenis.autobetes.controller;

import static org.molgenis.autobetes.controller.HomeController.URI;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
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
import org.molgenis.autobetes.autobetes.BasalProfileStart;
import org.molgenis.autobetes.autobetes.BgSensor;
import org.molgenis.autobetes.autobetes.BolusNormal;
import org.molgenis.autobetes.autobetes.BolusSquare;
import org.molgenis.autobetes.autobetes.ChangeSuspendEnable;
import org.molgenis.autobetes.autobetes.ChangeTempBasal;
import org.molgenis.autobetes.autobetes.ChangeTempBasalPercent;
import org.molgenis.autobetes.pumpobjectsparser.BasalProfileStartParser;
import org.molgenis.autobetes.pumpobjectsparser.BgSensorParser;
import org.molgenis.autobetes.pumpobjectsparser.BolusNormalParser;
import org.molgenis.autobetes.pumpobjectsparser.BolusSquareParser;
import org.molgenis.autobetes.pumpobjectsparser.ChangeSuspendEnableParser;
import org.molgenis.autobetes.pumpobjectsparser.ChangeTempBasalParser;
import org.molgenis.autobetes.pumpobjectsparser.ChangeTempBasalPercentParser;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.csv.CsvRepository;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.framework.ui.MolgenisPluginController;
import org.molgenis.auth.MolgenisUser;
import org.molgenis.security.core.utils.SecurityUtils;
import org.molgenis.util.FileStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.access.prepost.PreAuthorize;
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

	private final static String ChangeTimeGH = "ChangeTimeGH";
	private final static String CalBGForPH = "CalBGForPH";
	private final static String GlucoseSensorData = "GlucoseSensorData";
	private final static String BolusNormal = "BolusNormal";
	private final static String BolusSquare = "BolusSquare";
	private final static String ChangeBasalProfilePattern = "ChangeBasalProfilePattern";
	private final static String ChangeBasalProfile = "ChangeBasalProfile";
	private final static String BasalProfileStart = "BasalProfileStart";
	private final static String ChangeTempBasal = "ChangeTempBasal";
	private final static String ChangeTempBasalPercent = "ChangeTempBasalPercent";
	private final static String ChangeCarbRatioPattern = "ChangeCarbRatioPattern";
	private final static String ChangeCarbRatio = "ChangeCarbRatio";
	private final static String ChangeInsulinSensitivityPattern = "ChangeInsulinSensitivityPattern";
	private final static String ChangeInsulinSensitivity = "ChangeInsulinSensitivity";
	private final static String ChangeSuspendEnable = "ChangeSuspendEnable";
	
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

	@RequestMapping(value = "/uploadCSV", method = RequestMethod.POST, headers = "Content-Type=multipart/form-data")
	public String uploadCSV( @RequestParam
	Part file, Model model, HttpServletRequest servletRequest)
	{
		String username = SecurityUtils.getCurrentUsername();
		MolgenisUser user = dataService.findOne(MolgenisUser.ENTITY_NAME, new QueryImpl().eq(MolgenisUser.USERNAME, username), MolgenisUser.class);
		try
		{
			File pumpCsvFile = fileStore.store(file.getInputStream(), file.getName());
			
	        String tmpDir = System.getProperty("java.io.tmpdir") + "autobetesCsv" + File.separatorChar;

	        // import pump csv data
	        importPumpCsvFile(user, pumpCsvFile, new File(tmpDir), tmpDir);

	        // Now also import activities from Moves-app!
			MovesConnector movesConnector = new MovesConnectorImpl();
			movesConnector.manageActivities(dataService, user, CLIENT_ID_PARAM_VALUE, CLIENT_SECRET_PARAM_VALUE);
			model.addAttribute("message", "Import moves success??");
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
		// First put stuff in list, then add list at once! (Performance optimization)
		 List<BgSensor> bgSensorList = new ArrayList<BgSensor>();
		 List<BolusNormal> bolusNormalList = new ArrayList<BolusNormal>();
		 List<BolusSquare> bolusSquareList = new ArrayList<BolusSquare>();
		 List<ChangeTempBasal> changeTempBasalList = new ArrayList<ChangeTempBasal>();
		 List<ChangeTempBasalPercent> changeTempBasalPercentList = new ArrayList<ChangeTempBasalPercent>();
		 List<BasalProfileStart> basalProfileStartList = new ArrayList<BasalProfileStart>();
		 List<ChangeSuspendEnable> changeSuspendEnableList = new ArrayList<ChangeSuspendEnable>();
//		 List<Bolus> bolusListFile = new ArrayList<Bolus>();
//		 List<BasalProgrammed> basalProgrammedListFile = new ArrayList<BasalProgrammed>();
//		 List<BasalTemp> basalTempListFile = new ArrayList<BasalTemp>();
//		 List<BasalSetting> basalList = new ArrayList<BasalSetting>();
//		 List<Basal> basalAsReleased = new ArrayList<Basal>();
//		 List<BgSensor> bgSensorList = new ArrayList<BgSensor>();

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
		System.out.println("Entities that are not yet parsed into db:");
		Set<String> rawTypeSet = new HashSet<String>();
		for (Entity e : csvRepo)
		{
			String rawType = (String) e.get(RAWTYPE);
//			System.out.println(">> Parsing: " + rawType + ": " + e.toString());
			switch (rawType)
			{
				// TODO Probably this is not the right variable! Use 'volgnummer' or 'pumpID' to determine which one to take!
/*
				case ChangeTimeGH:
					new TimeChangeParser(e, dataService, molgenisUser);
					break;

				case CalBGForPH:
					new BgMeterParser(e, dataService, molgenisUser);
					break;
*/				
				case GlucoseSensorData:
					bgSensorList.add(new BgSensorParser(e, dataService, molgenisUser).getBgSensor());
					break;

				case BolusNormal:
					bolusNormalList.add(new BolusNormalParser(e, dataService, molgenisUser).getBn());
					break;

				case BolusSquare:
					bolusSquareList.add(new BolusSquareParser(e, dataService, molgenisUser).getBs());
					break;

				case ChangeTempBasal:
					changeTempBasalList.add(new ChangeTempBasalParser(e, dataService, molgenisUser).getCtbp());
					break;

				case ChangeTempBasalPercent:
					changeTempBasalPercentList.add(new ChangeTempBasalPercentParser(e, dataService, molgenisUser).getCtb());
					break;

				case BasalProfileStart:
					basalProfileStartList.add(new BasalProfileStartParser(e, dataService, molgenisUser).getBps());
					break;					
					
				case ChangeSuspendEnable:
					changeSuspendEnableList.add(new ChangeSuspendEnableParser(e, dataService, molgenisUser).getE());
					break;					
/*
				case ChangeBasalProfilePattern: // postfix 'Pre' means 'previous'
					new BasalProfileDefinitionGroupParser(e, dataService, molgenisUser);
					break;

				case ChangeBasalProfile: // postfix 'Pre' means 'previous'
					new BasalProfileDefinitionParser(e, dataService, molgenisUser);
					break;

				case ChangeCarbRatioPattern:
					new ChangeCarbRatioGroupParser(e, dataService, molgenisUser);
					break;

				case ChangeCarbRatio:
					new ChangeCarbRatioParser(e, dataService, molgenisUser);
					break;

				case ChangeInsulinSensitivityPattern:
					new ChangeInsulinSensitivityGroupParser(e, dataService, molgenisUser);
					break;

				case ChangeInsulinSensitivity:
					new ChangeInsulinSensitivityParser(e, dataService, molgenisUser);
					break;
*/				
				default: // print if not parsed
					if (!rawTypeSet.contains(rawType))
					{
						System.out.println(rawType);// + ":   " + e.toString());
						rawTypeSet.add(rawType);
					}
					break;
			}
		}
		System.out.println(">> Done parsing!");
		dataService.add(BgSensor.ENTITY_NAME, bgSensorList);
		dataService.add(org.molgenis.autobetes.autobetes.BolusNormal.ENTITY_NAME, bolusNormalList);
		dataService.add(org.molgenis.autobetes.autobetes.BolusSquare.ENTITY_NAME, bolusSquareList);
		dataService.add(org.molgenis.autobetes.autobetes.ChangeTempBasal.ENTITY_NAME, changeTempBasalList);
		dataService.add(org.molgenis.autobetes.autobetes.ChangeTempBasalPercent.ENTITY_NAME, changeTempBasalPercentList);
		dataService.add(org.molgenis.autobetes.autobetes.BasalProfileStart.ENTITY_NAME, basalProfileStartList);
		dataService.add(org.molgenis.autobetes.autobetes.ChangeSuspendEnable.ENTITY_NAME, changeSuspendEnableList);
		// TODO SUSPEND
		
		System.out.println(">> Done importing in db!");
		IOUtils.closeQuietly(csvRepo);
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
