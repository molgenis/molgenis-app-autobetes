package org.molgenis.autobetes.pumpobjectsparser;

import org.molgenis.autobetes.autobetes.ChangeCarbRatio;
import org.molgenis.autobetes.autobetes.ChangeCarbRatioGroup;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.auth.MolgenisUser;

public class ChangeCarbRatioParser extends ObjectParser
{
	private final static String PATTERN_DATUM = "PATTERN_DATUM";
	private final static String INDEX = "INDEX";
	private final static String AMOUNT = "AMOUNT";
	private final static String UNITS = "UNITS";
	private final static String START_TIME = "START_TIME";
	private ChangeCarbRatio ccr = new ChangeCarbRatio();
	

	public ChangeCarbRatioParser(Entity csvEntity, DataService dataService, MolgenisUser molgenisUser)
	{
		super(csvEntity, dataService, molgenisUser);

		
		ccr.setOwner(molgenisUser);
		ccr.setDateTimeString(getDateTimeString());
		ccr.setUnixtimeOriginal(getDateTimeLong());
		ccr.setIdOnPump(getIdOnPump());
		ccr.setUploadId(getUploadId());
		ccr.setFollowNumber(getFollowNumber());
		ccr.setOrigin(getOrigin());
		
		ccr.setGroupId(getString(PATTERN_DATUM));
		ccr.setRatioIndex(getInteger(INDEX));
		ccr.setAmount(getDouble(AMOUNT));
		ccr.setUnit(getString(UNITS));
		ccr.setStartTime(getLong(START_TIME));
		
//		save(e.ENTITY_NAME, e);
	}
	
	public ChangeCarbRatio getCcr()
	{
		return ccr;
	}
	

}
