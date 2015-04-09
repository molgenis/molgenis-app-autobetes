package org.molgenis.autobetes.pumpobjectsparser;

import org.molgenis.auth.MolgenisUser;
import org.molgenis.autobetes.autobetes.SensorWeakSignal;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;

public class SensorWeakSignalParser extends ObjectParser
{


	private SensorWeakSignal sws = new SensorWeakSignal();
	

	public SensorWeakSignalParser(Entity csvEntity, DataService dataService, MolgenisUser molgenisUser)
	{
		
		super(csvEntity, dataService, molgenisUser);

		
		sws.setOwner(molgenisUser);
		sws.setDateTimeString(getDateTimeString());
		sws.setUnixtimeOriginal(getDateTimeLong());
		sws.setIdOnPump(getIdOnPump());
		sws.setUploadId(getUploadId());
		sws.setFollowNumber(getFollowNumber());
		sws.setOrigin(getOrigin());
		
		
//		save(e.ENTITY_NAME, e);
	}
	
	public SensorWeakSignal getSws()
	{
		return sws;
	}
}
