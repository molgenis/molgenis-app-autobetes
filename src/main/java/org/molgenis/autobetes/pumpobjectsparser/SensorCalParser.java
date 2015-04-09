package org.molgenis.autobetes.pumpobjectsparser;

import org.molgenis.auth.MolgenisUser;
import org.molgenis.autobetes.autobetes.SensorCal;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;

public class SensorCalParser extends ObjectParser
{

	private SensorCal sc = new SensorCal();
	private final static String CAL_TYPE = "CAL_TYPE";
	private final static String ISIG = "ISIG";	
	private final static String VCNTR = "VCNTR";	
	private final static String BACKFILL_INDICATOR = "BACKFILL_INDICATOR";	
	
	public SensorCalParser(Entity csvEntity, DataService dataService, MolgenisUser molgenisUser)
	{
		super(csvEntity, dataService, molgenisUser);

		sc.setOwner(molgenisUser);
		sc.setDateTimeString(getDateTimeString());
		sc.setUnixtimeOriginal(getDateTimeLong());
		sc.setIdOnPump(getIdOnPump());
		sc.setUploadId(getUploadId());
		sc.setFollowNumber(getFollowNumber());
		sc.setOrigin(getOrigin());
		
		sc.setCal_Type(getString(CAL_TYPE));
		sc.setIsig(getDouble(ISIG));
		sc.setVcntr(getDouble(VCNTR));
		sc.setBackfill_Indicator(getBoolean(BACKFILL_INDICATOR));
		
//		save(bpdg.ENTITY_NAME, bpdg);
	}

	public SensorCal getSc()
	{
		return sc;
	}
}
