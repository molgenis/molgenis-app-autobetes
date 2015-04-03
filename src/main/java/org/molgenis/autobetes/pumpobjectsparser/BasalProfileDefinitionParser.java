package org.molgenis.autobetes.pumpobjectsparser;

import org.molgenis.autobetes.autobetes.BasalProfileDefinition;
import org.molgenis.autobetes.autobetes.BgSensor;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.auth.MolgenisUser;

public class BasalProfileDefinitionParser extends ObjectParser
{
	private final static String PATTERN_DATUM = "PATTERN_DATUM";
	private final static String PROFILE_INDEX = "PROFILE_INDEX";
	private final static String RATE = "RATE";
	private final static String START_TIME = "START_TIME";
	private BasalProfileDefinition bp;

	public BasalProfileDefinitionParser(Entity csvEntity, DataService dataService, MolgenisUser molgenisUser)
	{
		super(csvEntity, dataService, molgenisUser);
		bp.setOwner(molgenisUser);
		bp.setDateTimeString(getDateTimeString());
		bp.setUnixtimeOriginal(getDateTimeLong());
		bp.setIdOnPump(getIdOnPump());
		bp.setUploadId(getUploadId());
		bp.setFollowNumber(getFollowNumber());
		bp.setOrigin(getOrigin());
		
		bp.setGroupId(getString(PATTERN_DATUM));
		bp.setProfileIndex(getInteger(PROFILE_INDEX));
		bp.setRate(getDouble(RATE));
		bp.setStartTime(getLong(START_TIME));
		
//		save(bp.ENTITY_NAME, bp);
	}
	
	public BasalProfileDefinition getBPD()
	{
		return bp;
	}

}
