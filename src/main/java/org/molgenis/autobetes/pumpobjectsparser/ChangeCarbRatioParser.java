package org.molgenis.autobetes.pumpobjectsparser;

import org.molgenis.autobetes.autobetes.ChangeCarbRatio;
import org.molgenis.autobetes.autobetes.ChangeCarbRatioGroup;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.omx.auth.MolgenisUser;

public class ChangeCarbRatioParser extends ObjectParser
{
	private final static String PATTERN_DATUM = "PATTERN_DATUM";
	private final static String INDEX = "INDEX";
	private final static String AMOUNT = "AMOUNT";
	private final static String UNITS = "UNITS";
	private final static String START_TIME = "START_TIME";
	
	public ChangeCarbRatioParser(Entity csvEntity, DataService dataService, MolgenisUser molgenisUser)
	{
		super(csvEntity, dataService, molgenisUser);

		ChangeCarbRatio e = new ChangeCarbRatio();
		e.setDateTimeString(getDateTimeString());
		e.setUnixtimeOriginal(getDateTimeLong());
		e.setIdOnPump(getIdOnPump());
		e.setUploadId(getUploadId());
		
		e.setGroupId(getString(PATTERN_DATUM));
		e.setRatioIndex(getInteger(INDEX));
		e.setAmount(getDouble(AMOUNT));
		e.setUnit(getString(UNITS));
		e.setStartTime(getLong(START_TIME));
		
		save(e.ENTITY_NAME, e);
	}

}
