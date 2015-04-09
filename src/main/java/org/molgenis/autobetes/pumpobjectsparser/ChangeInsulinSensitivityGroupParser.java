package org.molgenis.autobetes.pumpobjectsparser;

import org.molgenis.autobetes.autobetes.ChangeInsulinSensitivityGroup;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.auth.MolgenisUser;

public class ChangeInsulinSensitivityGroupParser extends ObjectParser
{
//	private final static String ORIGINAL_UNITS = "ORIGINAL_UNITS";
	private final static String SIZE = "SIZE";
	ChangeInsulinSensitivityGroup cisg = new ChangeInsulinSensitivityGroup();
	
	

	public ChangeInsulinSensitivityGroupParser(Entity csvEntity, DataService dataService, MolgenisUser molgenisUser)
	{
		super(csvEntity, dataService, molgenisUser);
		cisg.setOwner(molgenisUser);
		cisg.setDateTimeString(getDateTimeString());
		cisg.setUnixtimeOriginal(getDateTimeLong());
		cisg.setIdOnPump(getIdOnPump());
		cisg.setUploadId(getUploadId());
		cisg.setFollowNumber(getFollowNumber());
		cisg.setOrigin(getOrigin());
			
		cisg.setDateTimeString(getDateTimeString());
		cisg.setUnixtimeOriginal(getDateTimeLong());
		cisg.setIdOnPump(getIdOnPump());
		cisg.setUploadId(getUploadId());
		
		cisg.setNSensitivities(getInteger(SIZE));
		
//		save(e.ENTITY_NAME, e);
	}

	public ChangeInsulinSensitivityGroup getCisg()
	{
		return cisg;
	}
}
