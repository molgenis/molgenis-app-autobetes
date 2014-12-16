package org.molgenis.autobetes;

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
import org.apache.poi.util.IOUtils;
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

public class SplitCSV
{
	@Value("${admin.password:@null}")
	private static String adminPassword;
	
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

	// todo
	// private final static String JournalEntryMealMarker = "JournalEntryMealMarker";
	// private final static String BolusWizardBolusEstimate = "BolusWizardBolusEstimate";
	// private final static String JournalEntryExerciseMarker = "JournalEntryExerciseMarker";

	public static void main(String[] args)
	{
		String baseDir = "/Users/mdijkstra/Documents/git/molgenis-autobetes/src/main/java/org/molgenis/autobetes/data/";
		String inputFile = baseDir + "original/CareLink-Export-1353854792761.csv";
		String tmpDir = baseDir + "tmp/";
		String outputDir = baseDir + "split/";

		split(new File(inputFile), new File(outputDir), tmpDir);
	}

	private static void split(File inputFile, File outputDir, String tmpDir)
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

		// Create connection with DB and get current MolgenisUser
		DataService dataService = null;
		try
		{
			// TODO: get usern + pw from .settings file
			dataService = new JpaStandaloneDataService(
					"jdbc:mysql://localhost/autobetes?innodb_autoinc_lock_mode=2&amp;rewriteBatchedStatements=true",
					"molgenis", "molgenis");
			
		}
		catch (ClassNotFoundException | NoSuchMethodException | SecurityException e1)
		{
			e1.printStackTrace();
		}
		if (null == dataService) throw new RuntimeException("Unable to initialize data service; cannot store data.");

		
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
