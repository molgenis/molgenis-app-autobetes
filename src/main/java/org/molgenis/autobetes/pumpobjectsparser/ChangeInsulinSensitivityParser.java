package org.molgenis.autobetes.pumpobjectsparser;

import org.molgenis.autobetes.autobetes.ChangeInsulinSensitivity;
import org.molgenis.autobetes.autobetes.ChangeInsulinSensitivityGroup;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.omx.auth.MolgenisUser;

public class ChangeInsulinSensitivityParser extends ObjectParser
{
	private final static String PATTERN_DATUM = "PATTERN_DATUM";
	private final static String INDEX = "INDEX";
	private final static String AMOUNT = "AMOUNT";
	private final static String START_TIME = "START_TIME";
	
	public ChangeInsulinSensitivityParser(Entity csvEntity, DataService dataService, MolgenisUser molgenisUser)
	{
		super(csvEntity, dataService, molgenisUser);

		ChangeInsulinSensitivity e = new ChangeInsulinSensitivity();
		e.setDateTimeString(getDateTimeString());
		e.setUnixtimeOriginal(getDateTimeLong());
		e.setIdOnPump(getIdOnPump());
		e.setUploadId(getUploadId());
		
		e.setGroupId(getString(PATTERN_DATUM));
		e.setSensitivityIndex(getInteger(INDEX));
		e.setAmount(getDouble(AMOUNT));
		e.setStartTime(getLong(START_TIME));
		
		save(e.ENTITY_NAME, e);
	}

}
