package org.molgenis.autobetes.pumpobjectsparser;

import org.molgenis.autobetes.autobetes.BasalProfileStart;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.omx.auth.MolgenisUser;

public class BasalProfileStartParser extends ObjectParser
{
	private final static String PATTERN_NAME = "PATTERN_NAME";
	private final static String PROFILE_INDEX = "PROFILE_INDEX";
	private final static String RATE = "RATE";
	private final static String START_TIME = "START_TIME";

	public BasalProfileStartParser(Entity csvEntity, DataService dataService, MolgenisUser molgenisUser)
	{
		super(csvEntity, dataService, molgenisUser);

		BasalProfileStart bps = new BasalProfileStart();
		bps.setDateTimeString(getDateTimeString());
		bps.setUnixtimeOriginal(getDateTimeLong());
		bps.setIdOnPump(getIdOnPump());
		bps.setUploadId(getUploadId());

		bps.setGroupName(getString(PATTERN_NAME));
		bps.setProfileIndex(getInteger(PROFILE_INDEX));
		bps.setRate(getDouble(RATE));
		bps.setStartTime(getLong(START_TIME));
		
		save(BasalProfileStart.ENTITY_NAME, bps);
	}

}
