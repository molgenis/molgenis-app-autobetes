package org.molgenis.autobetes.pumpobjectsparser;

import org.molgenis.autobetes.autobetes.CurrentInsulinSensitivity;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.auth.MolgenisUser;

public class CurrentInsulinSensitivityParser extends ObjectParser
{
	private final static String PATTERN_DATUM = "PATTERN_DATUM_ID";
	private final static String INDEX = "INDEX";
	private final static String AMOUNT = "AMOUNT";
	private final static String START_TIME = "START_TIME";
	CurrentInsulinSensitivity cis = new CurrentInsulinSensitivity();
	
	public CurrentInsulinSensitivityParser(Entity csvEntity, DataService dataService, MolgenisUser molgenisUser)
	{
		super(csvEntity, dataService, molgenisUser);
		cis.setOwner(molgenisUser);
		cis.setDateTimeString(getDateTimeString());
		cis.setUnixtimeOriginal(getDateTimeLong());
		cis.setIdOnPump(getIdOnPump());
		cis.setUploadId(getUploadId());
		cis.setFollowNumber(getFollowNumber());
		cis.setOrigin(getOrigin());
		
		cis.setGroupId(getString(PATTERN_DATUM));
		cis.setSensitivityIndex(getInteger(INDEX));
		cis.setAmount(getDouble(AMOUNT));
		cis.setStartTime(getLong(START_TIME));
		
//		save(e.ENTITY_NAME, e);
	}
	public CurrentInsulinSensitivity getCis()
	{
		return cis;
	}

}
