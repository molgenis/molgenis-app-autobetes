package org.molgenis.autobetes.pumpobjectsparser;

import org.molgenis.autobetes.autobetes.ChangeCarbRatioGroup;
import org.molgenis.autobetes.autobetes.ChangeInsulinSensitivityGroup;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.omx.auth.MolgenisUser;

public class ChangeInsulinSensitivityGroupParser extends ObjectParser
{
//	private final static String ORIGINAL_UNITS = "ORIGINAL_UNITS";
	private final static String SIZE = "SIZE";
	
	public ChangeInsulinSensitivityGroupParser(Entity csvEntity, DataService dataService, MolgenisUser molgenisUser)
	{
		super(csvEntity, dataService, molgenisUser);

		ChangeInsulinSensitivityGroup e = new ChangeInsulinSensitivityGroup();
		e.setDateTimeString(getDateTimeString());
		e.setUnixtimeOriginal(getDateTimeLong());
		e.setIdOnPump(getIdOnPump());
		e.setUploadId(getUploadId());
		
		e.setNSensitivities(getInteger(SIZE));
		
		save(e.ENTITY_NAME, e);
	}

}
