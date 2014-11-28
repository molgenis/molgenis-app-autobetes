package org.molgenis.autobetes.controller;

import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.CATEGORICAL;
import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.COMPOUND;
import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.DATE;
import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.DATE_TIME;
import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.MREF;
import static org.molgenis.MolgenisFieldTypes.FieldTypeEnum.XREF;
import static org.molgenis.autobetes.controller.HomeController.URI;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.autobetes.autobetes.Event;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataConverter;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.rest.AttributeMetaDataResponse;
import org.molgenis.data.rest.EntityCollectionRequest;
import org.molgenis.data.rest.EntityCollectionResponse;
import org.molgenis.data.rest.EntityPager;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.framework.ui.MolgenisPluginController;
import org.molgenis.omx.auth.MolgenisUser;
import org.molgenis.security.core.utils.SecurityUtils;
import org.molgenis.security.token.MolgenisToken;
import org.molgenis.security.token.TokenExtractor;
import org.molgenis.util.FileStore;
import org.molgenis.util.MolgenisDateFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;

import org.apache.commons.io.FileUtils;
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
import org.molgenis.autobetes.pumpobjectsparser.TimeChangeParser;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.csv.CsvRepository;
import org.molgenis.data.jpa.standalone.JpaStandaloneDataService;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.omx.auth.MolgenisUser;
import org.molgenis.security.core.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Value;

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
	

	
	// private static int BASALTIMESTEP = 3 * 60 * 1000; // 3 min
	private static String HEADER = "HEADER";
	private static String BODY = "BODY";

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
		System.out.println(SecurityUtils.getCurrentUsername());
		try
		{
			File uploadFile = fileStore.store(file.getInputStream(), file.getName());
			
			
			String baseDir = "/Users/dionkoolhaas/PumpCSVFiles/";
			String tmpDir = baseDir + "tmp/";
			String outputDir = baseDir + "split/";
			split(uploadFile, new File(outputDir), tmpDir);
			//System.out.println(IOUtils.toString(file.getInputStream(), "UTF-8"));
			
			//List<File> uploadedFiles = ZipFileUtil.unzip(uploadFile);
			//if (uploadedFiles.size() > 0) ontologyIndexer.index(new OntologyLoader(ontologyName, uploadedFiles.get(0)));
			//model.addAttribute("isIndexRunning", true);
		}
		catch (Exception e)
		{
			model.addAttribute("message", "Please upload a valid zip file!");
			model.addAttribute("isCorrectZipFile", false);
		}
		model.addAttribute("message", "great succes!!");
		return "view-home";
	}

	private void split(File inputFile, File outputDir, String tmpDir)
	{
		// List<Exercise> exerciseListFile = new ArrayList<Exercise>();
		// List<Carbs> carbListFile = new ArrayList<Carbs>();
		// List<BgSensor> bgsensorListFile = new ArrayList<BgSensor>();
		// List<Bolus> bolusListFile = new ArrayList<Bolus>();
		// List<BasalProgrammed> basalProgrammedListFile = new ArrayList<BasalProgrammed>();
		// List<BasalTemp> basalTempListFile = new ArrayList<BasalTemp>();
		// List<BasalSetting> basalList = new ArrayList<BasalSetting>();
		// List<Basal> basalAsReleased = new ArrayList<Basal>();
		// List<BgSensor> bgSensorList = new ArrayList<BgSensor>();

		// define 'unique' body file name
		String random = Long.toHexString(Double.doubleToLongBits(Math.random())).substring(0, 4);
		File bodyFile = new File(tmpDir + random + ".txt");

		// split header and body
		try
		{
			LinkedHashMap<String, String> fsplit;
			fsplit = splitInHeaderTail(inputFile);
			// work around: save as file so that we can read it in again with csvReader..
			FileUtils.writeStringToFile(bodyFile, fsplit.get(BODY));
		}
		catch (IOException e)
		{
			System.err.println(">> ERROR >> when (1) reading input (2) splitting in header/body (3) and save body");
			e.printStackTrace();
		}

		// read in body
		CsvRepository csvRepo = new CsvRepository(bodyFile, null, ';');

		
		// get current owner
		String userName = SecurityUtils.getCurrentUsername();
		MolgenisUser molgenisUser = (MolgenisUser) dataService.findOne(MolgenisUser.ENTITY_NAME, new QueryImpl().eq(MolgenisUser.USERNAME, userName));
		
		// list stores which entities cannot be loaded so that we do not show duplicates
		Set<String> rawTypeSet = new HashSet<String>();
		System.out.println("To do:");
		for (Entity e : csvRepo)
		{
			String rawType = (String) e.get(RAWTYPE);
//			System.out.println(">> Parsing: " + rawType + ": " + e.toString());
			switch (rawType)
			{
				// TODO Probably this is not the right variable! Use 'volgnummer' or 'pumpID' to determine which one to take!
				case ChangeTimeGH:
					new TimeChangeParser(e, dataService, molgenisUser);
					break;

				case CalBGForPH:
					new BgMeterParser(e, dataService, molgenisUser);
					break;
					
				case GlucoseSensorData:
					new BgSensorParser(e, dataService, molgenisUser);
					break;

				case BolusNormal:
					new BolusNormalParser(e, dataService, molgenisUser);
					break;

				case BolusSquare:
					new BolusSquareParser(e, dataService, molgenisUser);
					break;

				case ChangeBasalProfilePattern: // postfix 'Pre' means 'previous'
					new BasalProfileDefinitionGroupParser(e, dataService, molgenisUser);
					break;

				case ChangeBasalProfile: // postfix 'Pre' means 'previous'
					new BasalProfileDefinitionParser(e, dataService, molgenisUser);
					break;

				case BasalProfileStart:
					new BasalProfileStartParser(e, dataService, molgenisUser);
					break;

				case ChangeTempBasal:
					new ChangeTempBasalParser(e, dataService, molgenisUser);
					break;

				case ChangeTempBasalPercent:
					new ChangeTempBasalPercentParser(e, dataService, molgenisUser);
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

				case ChangeSuspendEnable:
					new ChangeSuspendEnableParser(e, dataService, molgenisUser);
					break;					
					
				default:
					if (!rawTypeSet.contains(rawType))
					{
						System.out.println(rawType);// + ":   " + e.toString());
						rawTypeSet.add(rawType);
					}
					break;
			}
		}

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

		for (positionNewline = content.indexOf("\n"); positionNewline != -1 && lineIndex < nHeaderLines - 1; positionNewline = content
				.indexOf("\n", positionNewline + 1))
		{
			lineIndex++;
		}

		String header = content.substring(0, positionNewline + 1);
		String body = content.substring(positionNewline + 1);

		LinkedHashMap<String, String> fsplit = new LinkedHashMap<String, String>();
		fsplit.put(HEADER, header);
		fsplit.put(BODY, body);

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
}
