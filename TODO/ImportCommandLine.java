package org.pompgemak.importer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.DataFormatException;

import org.apache.commons.io.FileUtils;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.Query;
import org.molgenis.io.csv.CsvReader;
import org.molgenis.util.tuple.Tuple;

import pompgemak.Basal;
import pompgemak.BasalProgrammed;
import pompgemak.BasalSetting;
import pompgemak.BasalTemp;
import pompgemak.Bgsensor;
import pompgemak.Bolus;
import pompgemak.Carbs;
import pompgemak.Exercise;
import pompgemak.Identification;

public class ImportCommandLine
{
	private static int BASALTIMESTEP = 3 * 60 * 1000; // 3 min
	private static String HEADER = "HEADER";
	private static String BODY = "BODY";

	private static String DATE = "Datum";
	private static String TIME = "Tijd";

	private static String RAWTYPE = "Onbewerkt: type";
	private static String JournalEntryMealMarker = "JournalEntryMealMarker";
	// multiple values including carbs:
	private static String BolusWizardBolusEstimate = "BolusWizardBolusEstimate";
	private static String GlucoseSensorData = "GlucoseSensorData";
	private static String BolusNormal = "BolusNormal";
	private static String BolusSquare = "BolusSquare";
	private static String BasalProfileStart = "BasalProfileStart";
	private static String ChangeTempBasalPercent = "ChangeTempBasalPercent";
	private static String JournalEntryExerciseMarker = "JournalEntryExerciseMarker";

	// raw types:
	private static String RAWVALUES = "Onbewerkt: waarden";
	private static String CARB_INPUT = "CARB_INPUT";
	private static String PROGRAMMED_AMOUNT = "PROGRAMMED_AMOUNT";
	private static String AMOUNT = "AMOUNT";
	private static String DURATION = "DURATION";
	private static String PATTERNNAME = "PATTERN_NAME";
	private static String PROFILE_INDEX = "PROFILE_INDEX";
	private static String RATE = "RATE";
	private static String START_TIME = "START_TIME";
	private static String PERCENT_OF_RATE = "PERCENT_OF_RATE";
	private static String BASAL_TEMP_DURATION = "DURATION";

	private static String CARBWIZARD = "Boluswizard KH-invoer (gram)";
	private static String BGSENSOR = "Sensorglucose (mmol/l)";

	/*
	 * Save file in DB
	 */
	public static void fileToDB(Database db, File f) throws IOException, ParseException, DatabaseException, DataFormatException
	{
		List<Exercise> exerciseListFile = new ArrayList<Exercise>();
		List<Carbs> carbListFile = new ArrayList<Carbs>();
		List<Bgsensor> bgsensorListFile = new ArrayList<Bgsensor>();
		List<Bolus> bolusListFile = new ArrayList<Bolus>();
		List<BasalProgrammed> basalProgrammedListFile = new ArrayList<BasalProgrammed>();
		List<BasalTemp> basalTempListFile = new ArrayList<BasalTemp>();
		List<BasalSetting> basalList = new ArrayList<BasalSetting>();
		List<Basal> basalAsReleased = new ArrayList<Basal>();

		LinkedHashMap<String, String> fsplit = splitInHeaderTail(f);
		// work around: save as file so that we can read it in again with csvReader..
		File file_body = new File("body.txt");
		FileUtils.writeStringToFile(file_body, fsplit.get(BODY));
		CsvReader reader = new CsvReader(file_body, ';');

		Iterator<String> headers = reader.colNamesIterator();
		
		// load data from body of file
		for (Tuple tuple : reader)
		{
			// Determine data type of tuple
			if (JournalEntryMealMarker.equals(tuple.getString(RAWTYPE)) || BolusWizardBolusEstimate.equals(tuple.getString(RAWTYPE)))
			{ // carbs
				loadCarb(carbListFile, tuple);
			} else if (GlucoseSensorData.equals(tuple.getString(RAWTYPE)))
			{
				loadBgsensor(bgsensorListFile, tuple);
			} else if (BolusNormal.equals(tuple.getString(RAWTYPE)) || BolusSquare.equals(tuple.getString(RAWTYPE)))
			{ // bolus
				loadBolus(bolusListFile, tuple);
			} else if (BasalProfileStart.equals(tuple.getString(RAWTYPE)))
			{
				loadBasal(basalProgrammedListFile, tuple);
			} else if (ChangeTempBasalPercent.equals(tuple.getString(RAWTYPE)))
			{
				loadBasalTemp(basalTempListFile, tuple);
			} else if (JournalEntryExerciseMarker.equals(tuple.getString(RAWTYPE)))
			{
				loadExercise(exerciseListFile, tuple);
			}

			// addBG(db, tuple);
			// addCarb(db, tuple);
			// addSensorBG(db, tuple);
		}
		
		reader.close();

		// curate data
		// curateCarb(carbListFile);
		bgsensorListFile = curateBgsensor(bgsensorListFile);

		// Combine basal and basaltemp insulin to effective basal release

		// Check consistency data from file with db, and add if new
		db.beginTx();
		System.out.println(">> Trying to add exercise...");
		checkAdd(db, asIdentificationList(exerciseListFile));
		System.out.println(">> Trying to add carbs...");
		checkAdd(db, asIdentificationList(carbListFile));
		System.out.println(">> Trying to add bgsensor...");
		checkAdd(db, asIdentificationList(bgsensorListFile));
		System.out.println(">> Trying to add bolus...");
		checkAdd(db, asIdentificationList(bolusListFile));
		System.out.println(">> Trying to add basal as programmed...");
		checkAdd(db, asIdentificationList(basalProgrammedListFile));
		System.out.println(">> Trying to add basal temp change...");
		checkAdd(db, asIdentificationList(basalTempListFile));

		// for (pompgemak.BasalProgrammed bp : basalProgrammedListFile)
		// System.out.println(">> BasalProgrammed:\n" + bp);

		// for (BasalTemp bt : basalTempListFile)
		// System.out.println(">> BasalTemp:\n"+bt);

		System.out.println(">> Trying to derive basal changes...");
		// Derive basal insulin (as it was released) from basal + basalTemp
		basalList = getBasalList(db); // we pass on 'db' because we might need
										// all values

		// for (Basal b : basalList)
		// {
		// System.out.println(">> Basal:\n"+b);
		// }

		System.out.println(">> Trying to add basal changes...");
		checkAdd(db, asIdentificationList(basalList));

		System.out.println(">> Trying to derive basal as it was released...");

		basalAsReleased = asReleased(basalList, BASALTIMESTEP);

		System.out.println(">> Trying to add basal as released...");
		checkAdd(db, asIdentificationList(basalAsReleased));

		db.commitTx();

		// save new items in db
		// checkAddCarb(db);

		System.out.println(">> DONE: importing file " + f + " into db.");
	}

	/**
	 * Convert basal pump setting in actual series of boli as released by pump
	 * 
	 * @param basalSettingsList
	 *            is a list of changes in basal pump settings
	 * @param basalTimeStep
	 *            period (ms) after which pump gives new small bolus
	 * @return Return list of actual basal boli, as released by pump
	 */
	private static List<Basal> asReleased(List<BasalSetting> basalSettingsList, int basalTimeStep)
	{
		List<Basal> basalBolusList = new ArrayList<Basal>();
		if (basalSettingsList.size() == 0)
			return basalBolusList;

		// validate whether stuff is sorted
		if (!isSorted(basalSettingsList))
		{
			System.err.println(">> ERROR >> basalList not sorted on unixtime. Please do so.");
			System.exit(1);
		}

		// select first basal setting
		BasalSetting basalSetting = basalSettingsList.get(0);

		// already set time and rate for first event
		long time = basalSetting.getUnixtime();
		double rate = basalSetting.getRate();

		// use this to store time point of next setting
		long nextTime = time; // set at time in case we only have one setting

		// iterate over all but last basal settings
		int i;
		for (i = 0; i < basalSettingsList.size() - 1; i++)
		{
			nextTime = basalSettingsList.get(i + 1).getUnixtime();

			while (time < nextTime)
			{
				Basal basBolus = new Basal();
				basBolus.setUnixtime(time); // we believe in unixtime and derive
											// 'date' from that
				int timeZoneOffset = TimeZone.getDefault().getOffset(time);
				basBolus.setMoment(new java.util.Date(time - timeZoneOffset));
				basBolus.setRate(asAmount(rate, basalTimeStep));
				basalBolusList.add(basBolus);
				time += basalTimeStep;
			}

			// already set rate for next event
			rate = basalSettingsList.get(i + 1).getRate();
		}

		// nextTime holds time point of last basalSettings item
		// continue with last setting for 24 hours
		while (time < nextTime + 24 * 3600 * 1000)
		{
			Basal basBolus = new Basal();
			basBolus.setUnixtime(time); // we believe in unixtime and derive
										// 'date' from that
			int timeZoneOffset = TimeZone.getDefault().getOffset(time);
			basBolus.setMoment(new java.util.Date(time - timeZoneOffset));
			basBolus.setRate(asAmount(rate, basalTimeStep));
			basalBolusList.add(basBolus);
			time += basalTimeStep;
		}

		return basalBolusList;
	}

	/**
	 * Convert rate into actual amount that is released each time
	 * 
	 * @param rate
	 *            (IE/hour)
	 * @param deltaT
	 *            period after which pump releases basal insulin again and again
	 *            (ms)
	 * @return actual insulin amount that is released each time
	 */
	private static Double asAmount(double rate, int deltaT)
	{
		return rate * deltaT / 3600 / 1000;
	}

	private static List<BasalSetting> getBasalList(Database db) throws DatabaseException
	{
		// get complete list 'basalProgrammed' + 'basalTemp' and combine into /
		// return 'basal'
		List<BasalProgrammed> bpLst = db.query(BasalProgrammed.class).find();
		List<BasalTemp> btLst = db.query(BasalTemp.class).find();
		List<BasalSetting> bLst = new ArrayList<BasalSetting>(); // this is the
																	// return
																	// list

		// validate whether stuff is sorted
		if (!isSorted(bpLst) || !isSorted(btLst))
		{
			System.err.println(">> ERROR >> Either bpLst or btList not sorted on unixtime. Please do so.");
			System.exit(1);
		}

		// System.out.println(">> Basal programmed:");
		// for (BasalProgrammed b : bpLst) System.out.println(b);
		//
		// System.out.println(">> Basal temp:");
		// for (BasalTemp b : btLst) System.out.println(b);

		Iterator<BasalProgrammed> bpIt = bpLst.iterator();
		BasalProgrammed bp = new BasalProgrammed();
		bp.setUnixtime(Long.MAX_VALUE);
		if (bpIt.hasNext())
			bp = bpIt.next();

		Iterator<BasalTemp> btIt = btLst.iterator();
		BasalTemp bt = new BasalTemp();
		bt.setUnixtime(Long.MAX_VALUE);
		if (btIt.hasNext())
			bt = btIt.next();

		// variables for in loop
		Double programmedRate = null, fraction = 1d;
		long currentTime, tempEnd = Long.MAX_VALUE;
		Date tempEndMoment = new java.util.Date(tempEnd); // used for nice
															// representation of
															// tempEnd

		boolean bpDone = bpLst.size() == 0, btDone = btLst.size() == 0;

		// first bt cannot start before first bp, so skip bt's until first bp
		while (!bpDone && !btDone && bt.getUnixtime() < bp.getUnixtime())
		{
			if (btIt.hasNext())
			{
				bt = btIt.next();
			} else
			{
				btDone = true;
				bt.setUnixtime(Long.MAX_VALUE);
			}
		}

		// coerce both lists into one 'basal list', bLst
		while (!bpDone || !btDone)
		{
			// determine first time point when st changes
			currentTime = Math.min(bp.getUnixtime(), Math.min(bt.getUnixtime(), tempEnd));

			BasalSetting b = new BasalSetting();
			b.setUnixtime(currentTime);

			if (bp.getUnixtime().equals(currentTime))
			{ // bp changes
				b.setMoment(bp.getMoment());
				programmedRate = bp.getRate();
				b.setRate(fraction * programmedRate);

				// move to next bp
				if (bpIt.hasNext())
				{
					bp = bpIt.next();
				} else
				{
					bp.setUnixtime(Long.MAX_VALUE);
					bpDone = true;
				}
			}

			if (bt.getUnixtime().equals(currentTime))
			{ // bt changes
				b.setMoment(bt.getMoment());
				fraction = bt.getFraction();
				b.setRate(fraction * programmedRate);

				// update tempEnd
				tempEnd = currentTime + bt.getDuration();

				// also update corresponding moment. This is nasty because java
				// makes use of summer times and time zones and so..
				tempEndMoment = (Date) bt.getMoment().clone();
				tempEndMoment.setTime(tempEndMoment.getTime() + bt.getDuration());

				// move to next bt
				if (btIt.hasNext())
				{
					bt = btIt.next();
				} else
				{
					bt.setUnixtime(Long.MAX_VALUE);
					btDone = true;
				}
			}

			if (tempEnd == currentTime)
			{ // temp ends

				if (b.getMoment() == null)
				{
					b.setMoment(tempEndMoment);
				}

				fraction = 1d;
				b.setRate(programmedRate);

				tempEnd = Long.MAX_VALUE;
			}

			addNewBasal(bLst, b);
		}

		return bLst;
	}

	private static void addNewBasal(List<BasalSetting> bLst, BasalSetting b)
	{
		// only add if the rate is different than the rate of the last item
		// (it is of no use to say that we continue with same rate...
		if (bLst.size() == 0 || !bLst.get(bLst.size() - 1).getRate().equals(b.getRate()))
		{
			// Check whether two consecutive time points are equal!
			if (0 < bLst.size() && b.getUnixtime() <= bLst.get(bLst.size() - 1).getUnixtime())
			{
				System.err.println(">> TIME STAMPS not increasing!");
				System.err.println(">> " + b);
				System.exit(1);
			}

			bLst.add(b);

			// System.out.println(">> BASAL >> " + b);
		}
	}

	@SuppressWarnings("unchecked")
	private static boolean isSorted(Object o)
	{
		Iterator<Identification> it = ((List<Identification>) o).iterator();

		Long prevTime = Long.MIN_VALUE;
		Identification id;
		while (it.hasNext())
		{
			id = it.next();

			if (id.getUnixtime() <= prevTime)
				return false;

			prevTime = id.getUnixtime();
		}

		return true;
	}

	@SuppressWarnings("unchecked")
	private static List<Identification> asIdentificationList(Object o)
	{
		return (List<Identification>) o;
	}

	/**
	 * Check consistency with DB, and add if non-existent
	 */
	private static void checkAdd(Database db, List<Identification> itemListFile) throws DatabaseException
	{
		if (itemListFile.size() == 0)
		{
			System.err.println(">> No items added.");
			return;
		}

		// create list with items we want to add
		List<Identification> addList = new ArrayList<Identification>();

		// get map unixtimes -> entity (File)
		LinkedHashMap<Long, Identification> itemMapFile = getUnixtimesMap(itemListFile);

		// get carbs from db that have the same unixtime in file and in db
		// and put them in a map unixtimes -> entity (DB)
		Query q = db.query(itemListFile.get(0).getClass());
		q.in("unixtime", new ArrayList<Long>(itemMapFile.keySet()));
		LinkedHashMap<Long, Identification> itemMapDB = getUnixtimesMap(q.find());

		// Loop through items that come from file
		Iterator<Long> it = itemMapFile.keySet().iterator();
		while (it.hasNext())
		{
			Long unixtime = it.next();

			Identification itemDb = itemMapDB.get(unixtime);
			Identification itemFile = itemMapFile.get(unixtime);
			if (itemDb == null) // non-existent, so add
			{
				// only add if we don't go into/out of summer time
				if (!summertimeIssue(itemFile))
				{
					addList.add(itemFile);

					// fail, this shouldn't happen
					if (itemFile.getMoment().toString().equals("Sun Oct 28 02:00:00 CEST 2012"))
						System.out.println(">> ADDED >> " + itemFile.getMoment()); // fail, this shouldn't happen
				} else
				{
					// Ignore... duplicate values due to summer time issue
					//System.out.println(">> SUMMER TIME ISSUE >> " + itemFile.getMoment());
				}
			} else
			// check consistency
			{
				boolean consistent = false;
				if (itemFile instanceof Carbs)
				{
					Carbs castF = (Carbs) itemFile;
					Carbs castDb = (Carbs) itemDb;
					consistent = castF.getValue().equals(castDb.getValue());
				} else if (itemFile instanceof Bgsensor)
				{
					Bgsensor castF = (Bgsensor) itemFile;
					Bgsensor castDb = (Bgsensor) itemDb;
					consistent = castF.getValue().equals(castDb.getValue());
				} else if (itemFile instanceof Bolus)
				{
					Bolus castF = (Bolus) itemFile;
					Bolus castDb = (Bolus) itemDb;
					consistent = castF.getSquare().equals(castDb.getSquare());
					consistent = consistent && castF.getAmount().equals(castDb.getAmount());
					consistent = consistent && castF.getProgrammedAmount().equals(castDb.getProgrammedAmount());
				} else if (itemFile instanceof BasalProgrammed)
				{
					BasalProgrammed castF = (BasalProgrammed) itemFile;
					BasalProgrammed castDb = (BasalProgrammed) itemDb;
					consistent = castF.getPatternName().equals(castDb.getPatternName());
					consistent = consistent && castF.getProfileIndex().equals(castDb.getProfileIndex());
					consistent = consistent && castF.getRate().equals(castDb.getRate());
					consistent = consistent && castF.getStartTime().equals(castDb.getStartTime());
				} else if (itemFile instanceof BasalTemp)
				{
					BasalTemp castF = (BasalTemp) itemFile;
					BasalTemp castDb = (BasalTemp) itemDb;
					consistent = castF.getFraction().equals(castDb.getFraction());
					consistent = consistent && castF.getDuration().equals(castDb.getDuration());
				} else if (itemFile instanceof BasalSetting)
				{
					BasalSetting castF = (BasalSetting) itemFile;
					BasalSetting castDb = (BasalSetting) itemDb;
					consistent = castF.getRate().equals(castDb.getRate());
				} else if (itemFile instanceof Basal)
				{
					Basal castF = (Basal) itemFile;
					Basal castDb = (Basal) itemDb;
					consistent = castF.getRate().equals(castDb.getRate());
				} else if (itemFile instanceof Exercise)
				{
					// nothing to check
					consistent = true;
				}

				if (!consistent)
				{
					System.err.println(">> Inconsistency Identification in File with DB");
					System.err.println(">> System exit with status 1.");
					System.exit(1);
				}
			}
		}

		// let's add items that were not yet in DB
		db.add(addList);
	}

	private static boolean summertimeIssue(Identification itemFile)
	{
		return (itemFile.getMoment().after(asDate("28-10-12", "01:59:00")) && itemFile.getMoment().before(asDate("28-10-12", "03:00:00")));
	}

	private static ArrayList<Bgsensor> curateBgsensor(List<Bgsensor> lst)
	{
		// create the map that will contain the eventual curated items, convert
		// back to list 'lst'.
		LinkedHashMap<Long, Bgsensor> map = new LinkedHashMap<Long, Bgsensor>();

		Iterator<Bgsensor> it = lst.iterator();
		while (it.hasNext())
		{
			Bgsensor item = it.next();
			Long unixtime = item.getUnixtime();
			Bgsensor mapItem = map.get(unixtime);

			if (mapItem == null)
			{
				map.put(unixtime, item);
			} else
			{
				Double v1 = mapItem.getValue(), v2 = item.getValue();
				// if the two have a different value, then average them
				if (!v1.equals(v2))
				{
					System.err.println(">> Bgsensor clash (unixtime " + unixtime + "), values " + v1 + " and " + v2 + ". Neither one will be added.");
					map.remove(unixtime);
				} else
				{
					// do nothing
				}
			}
		}

		return new ArrayList<Bgsensor>(map.values());
	}

	/**
	 * Return map<unixtime, Identification>
	 */
	@SuppressWarnings("unchecked")
	private static LinkedHashMap<Long, Identification> getUnixtimesMap(Object olst)
	{
		LinkedHashMap<Long, Identification> map = new LinkedHashMap<Long, Identification>();

		List<Identification> lst = (List<Identification>) olst;

		Iterator<Identification> it = lst.iterator();
		while (it.hasNext())
		{
			Identification i = it.next();

			if (i.getUnixtime().toString().equals("9223372036854775807"))
			{
				System.out.println(i);
			}

			if (map.keySet().contains(i.getUnixtime()))
			{
				System.err.println(">> Unixtime " + i.getUnixtime() + " is twice in Identification-list that you want to import...");
				System.err.println(">> System exit with status 1.");
				System.exit(1);
			} else
			{
				map.put(i.getUnixtime(), i);
			}
		}

		return map;
	}

	private static void loadCarb(List<Carbs> lstFromFile, Tuple tuple)
	{
		Double carbRawAmount = getRawDouble(tuple, CARB_INPUT);

		// SANITY CHECK
		// In case BolusWizardBolusEstimate, the raw and the wizard value should
		// be equal
		if (BolusWizardBolusEstimate.equals(tuple.getString(RAWTYPE)))
		{
			Double carbWizardAmount = asDouble(tuple.getString(CARBWIZARD));
			if (!carbRawAmount.equals(carbWizardAmount))
			{
				System.err.println(">> ERROR in loadCarb: reading two carb amounts that sould have same value:");
				System.err.println(">> carbRawAmount = " + carbRawAmount + ", and carbWizardAmount = " + carbWizardAmount);
				System.err.println(">> Tuple: " + tuple);
				System.err.println(">> System exit with status 1.");
				System.exit(1);
			}
		}

		// if non-zero, add carbs to list
		if (carbRawAmount != 0)
		{
			Carbs c = new Carbs();
			setDateTime(c, tuple);
			c.setValue(carbRawAmount);
			lstFromFile.add(c);
		}
	}

	private static Double getRawDouble(Tuple tuple, String type)
	{
		String rawvalues = tuple.getString(RAWVALUES);

		// Possible weakness: we should seek for something ending on ", "!
		Pattern p = Pattern.compile("(" + type + "=)(\\d+,\\d)");
		Matcher m = p.matcher(rawvalues);

		if (m.find())
		{ // try with comma (1,2)
			return asDouble(m.group(2));
		} else
		{ // try without comma (e.g. 1)
			p = Pattern.compile("(" + type + "=)(\\d+)");
			m = p.matcher(rawvalues);

			if (m.find())
			{
				return asDouble(m.group(2));
			} else
			{
				System.err.println(">> ERROR in getRawValue, expected to find " + type + "=..., but we did not!");
				System.err.println(">> Tuple: " + tuple);
				System.err.println(">> System exit with status 1.");
				System.exit(1);
			}
		}

		return null;
	}

	private static Integer getRawInt(Tuple tuple, String type)
	{
		String rawvalues = tuple.getString(RAWVALUES);

		// Possible weakness: we should seek for something ending on ", "!
		Pattern p = Pattern.compile("(" + type + "=)(\\d+)");
		Matcher m = p.matcher(rawvalues);

		if (m.find())
		{
			return Integer.parseInt(m.group(2));
		} else
		{
			System.err.println(">> ERROR in getRawValue, expected to find '" + type + "=..., ', but we did not!");
			System.err.println(">> Tuple: " + tuple);
			System.err.println(">> System exit with status 1.");
			System.exit(1);
		}

		return null;
	}

	private static String getRawString(Tuple tuple, String type)
	{
		String rawvalues = tuple.getString(RAWVALUES);

		Pattern p = Pattern.compile("(" + type + "=)(\\w+)");
		Matcher m = p.matcher(rawvalues);

		if (m.find())
		{
			return m.group(2);
		} else
		{
			System.err.println(">> ERROR in getRawValue, expected to find '" + type + "=..., ', but we did not!");
			System.err.println(">> Tuple: " + tuple);
			System.err.println(">> System exit with status 1.");
			System.exit(1);
		}

		return null;
	}

	// private static Double getRawInputBolus(Tuple tuple)
	// {
	// String rawvalues = tuple.getString(RAWVALUE);
	//
	// Pattern p = Pattern.compile("(CARB_INPUT=)(\\d+)");
	// Matcher m = p.matcher(rawvalues);
	//
	// if (!m.find())
	// {
	// System.err.println(">> ERROR in getRawInputCarb, expected to find CARB_INPUT=..., but we did not!");
	// System.err.println(">> Tuple: " + tuple);
	// System.err.println(">> System exit with status 1.");
	// System.exit(1);
	// }
	// else
	// {
	// return asDouble(m.group(2));
	// }
	//
	// return null;
	// }

	private static void loadBolus(List<Bolus> bolusListFile, Tuple tuple)
	{
		Bolus bolus = new Bolus();
		setDateTime(bolus, tuple);

		boolean square = BolusSquare.equals(tuple.getString(RAWTYPE));
		bolus.setSquare(square);
		bolus.setProgrammedAmount(getRawDouble(tuple, PROGRAMMED_AMOUNT));
		bolus.setAmount(getRawDouble(tuple, AMOUNT));
		if (square)
			bolus.setDuration(getRawDouble(tuple, DURATION));
		else
			bolus.setDuration(null);

		bolusListFile.add(bolus);
	}

	private static void loadBasal(List<BasalProgrammed> basalListFile, Tuple tuple)
	{
		BasalProgrammed basal = new BasalProgrammed();
		setDateTime(basal, tuple);

		basal.setPatternName(getRawString(tuple, PATTERNNAME));
		basal.setProfileIndex(getRawInt(tuple, PROFILE_INDEX));
		basal.setRate(getRawDouble(tuple, RATE));
		basal.setStartTime(getRawInt(tuple, START_TIME));

		basalListFile.add(basal);
	}

	private static void loadBasalTemp(List<BasalTemp> basalTempListFile, Tuple tuple)
	{
		BasalTemp basalTemp = new BasalTemp();
		setDateTime(basalTemp, tuple);

		// convert from percentage to fraction:
		basalTemp.setFraction(((double) getRawInt(tuple, PERCENT_OF_RATE)) / 100);
		basalTemp.setDuration(getRawInt(tuple, BASAL_TEMP_DURATION));

		basalTempListFile.add(basalTemp);
	}

	private static void loadExercise(List<Exercise> exerciseListFile, Tuple tuple)
	{
		Exercise exercise = new Exercise();
		setDateTime(exercise, tuple);

		exerciseListFile.add(exercise);
	}

	private static void loadBgsensor(List<Bgsensor> lstFromFile, Tuple tuple)
	{
		Double value = asDouble(tuple.getString(BGSENSOR));
		if (value != null && value != 0)
		{
			Bgsensor item = new Bgsensor();
			setDateTime(item, tuple);
			item.setValue(value);
			lstFromFile.add(item);
		}
	}

	private static void setDateTime(Identification e, Tuple tuple)
	{
		Date moment = asDate(tuple.getString(DATE), tuple.getString(TIME));
		e.setMoment(moment);
		int timeZoneOffset = TimeZone.getDefault().getOffset(moment.getTime());
		// int offset = TimeZone.getDefault().getDSTSavings();
		e.setUnixtime(moment.getTime() + timeZoneOffset);
	}

	/*
	 * Convert date and time into java.util.Date object
	 */
	@SuppressWarnings("deprecation")
	private static Date asDate(String date, String time)
	{
		// parse date
		int dash1 = date.indexOf("-");
		int dash2 = date.indexOf("-", dash1 + 1);

		int day = Integer.parseInt(date.substring(0, dash1));
		int month = Integer.parseInt(date.substring(dash1 + 1, dash2));
		int year = 2000 + Integer.parseInt(date.substring(dash2 + 1));

		// parse time
		int colon1 = time.indexOf(":");
		int colon2 = time.indexOf(":", colon1 + 1);
		int hours = Integer.parseInt(time.substring(0, colon1));
		int min = Integer.parseInt(time.substring(colon1 + 1, colon2));
		int sec = Integer.parseInt(time.substring(colon2 + 1));

		return new Date(year - 1900, month - 1, day, hours, min, sec);
	}

	/*
	 * Return as Double. E.g., given "1,2", return 1.2
	 */
	private static Double asDouble(String d)
	{
		if (d == null)
			return null;

		NumberFormat format = NumberFormat.getInstance(Locale.FRANCE);
		Number number;
		try
		{
			number = format.parse(d);
			return number.doubleValue();
		} catch (ParseException e)
		{
			e.printStackTrace();
		}

		return null;
	}

	/*
	 * Split file in header and body
	 */
	private static LinkedHashMap<String, String> splitInHeaderTail(File f) throws IOException
	{
		String content = fileToString(f);

		int lineIndex = 0, positionNewline = 0, nHeaderLines = 11;

		// do this smarter; e.g. assume header ends when number of separaters is
		// big (or maybe even equal to a certain number)

		for (positionNewline = content.indexOf("\n"); positionNewline != -1 && lineIndex < nHeaderLines - 1; positionNewline = content.indexOf("\n", positionNewline + 1))
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
		} finally
		{
			stream.close();
		}
	}

	public static void main(String[] args) throws IOException, DatabaseException, ParseException, DataFormatException
	{
//		System.out.println('hoi');
		Database db = new SecuredJpaDatabase();

		fileToDB(db, new File("/Users/mdijkstra/Dropbox/Documents/personal/diabetes/pomp/data/CareLink-Export-1353854792761.csv"));
//		 "/Users/mdijkstra/Dropbox/Documents/personal/diabetes/pomp/data/CareLink-Export-1350505246082.csv"));
//		 "/Users/mdijkstra/Dropbox/Documents/personal/diabetes/pomp/data/testWithSpace.csv"));
//		 "/Users/mdijkstra/Dropbox/Documents/personal/diabetes/pomp/data/body.csv"));
	}

}
