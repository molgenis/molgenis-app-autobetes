package org.molgenis.autobetes.pumpobjectsparser;

import org.molgenis.autobetes.autobetes.CurrentBasalProfile;
import org.molgenis.autobetes.autobetes.BgSensor;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.auth.MolgenisUser;

public class CurrentBasalProfileParser extends ObjectParser
{
	private final static String PATTERN_DATUM = "PATTERN_DATUM_ID";
	private final static String PROFILE_INDEX = "PROFILE_INDEX";
	private final static String RATE = "RATE";
	private final static String START_TIME = "START_TIME";
	private CurrentBasalProfile cbp = new CurrentBasalProfile();

	public CurrentBasalProfileParser(Entity csvEntity, DataService dataService, MolgenisUser molgenisUser)
	{
		super(csvEntity, dataService, molgenisUser);
		cbp.setOwner(molgenisUser);
		cbp.setDateTimeString(getDateTimeString());
		cbp.setUnixtimeOriginal(getDateTimeLong());
		cbp.setIdOnPump(getIdOnPump());
		cbp.setUploadId(getUploadId());
		cbp.setFollowNumber(getFollowNumber());
		
		cbp.setGroupId(getString(PATTERN_DATUM));
		cbp.setProfileIndex(getInteger(PROFILE_INDEX));
		cbp.setRate(getDouble(RATE));
		cbp.setStartTime(getLong(START_TIME));
		
//		save(bp.ENTITY_NAME, bp);
	}
	
	public CurrentBasalProfile getCBPD()
	{
		return cbp;
	}

}
