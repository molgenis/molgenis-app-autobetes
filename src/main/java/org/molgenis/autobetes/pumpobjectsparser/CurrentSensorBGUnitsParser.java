package org.molgenis.autobetes.pumpobjectsparser;

import org.molgenis.auth.MolgenisUser;
import org.molgenis.autobetes.autobetes.CurrentSensorBGUnits;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;

public class CurrentSensorBGUnitsParser extends ObjectParser
{


	private CurrentSensorBGUnits csbu = new CurrentSensorBGUnits();
	private final static String UNITS = "UNITS";	
	public CurrentSensorBGUnitsParser(Entity csvEntity, DataService dataService, MolgenisUser molgenisUser)
	{
		
		super(csvEntity, dataService, molgenisUser);

		
		csbu.setOwner(molgenisUser);
		csbu.setDateTimeString(getDateTimeString());
		csbu.setUnixtimeOriginal(getDateTimeLong());
		csbu.setIdOnPump(getIdOnPump());
		csbu.setUploadId(getUploadId());
		csbu.setFollowNumber(getFollowNumber());
		csbu.setOrigin(getOrigin());
		
		csbu.setUnits(getString(UNITS));
//		save(e.ENTITY_NAME, e);
	}
	
	public CurrentSensorBGUnits getCsbu()
	{
		return csbu;
	}
}
