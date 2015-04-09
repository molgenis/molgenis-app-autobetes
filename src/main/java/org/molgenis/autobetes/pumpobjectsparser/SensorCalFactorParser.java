package org.molgenis.autobetes.pumpobjectsparser;

import org.molgenis.auth.MolgenisUser;
import org.molgenis.autobetes.autobetes.SensorCalFactor;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;

public class SensorCalFactorParser extends ObjectParser
{

	private final static String CAL_FACTOR = "CAL_FACTOR";

	private SensorCalFactor scf = new SensorCalFactor();
	

	public SensorCalFactorParser(Entity csvEntity, DataService dataService, MolgenisUser molgenisUser)
	{
		super(csvEntity, dataService, molgenisUser);

		
		scf.setOwner(molgenisUser);
		scf.setDateTimeString(getDateTimeString());
		scf.setUnixtimeOriginal(getDateTimeLong());
		scf.setIdOnPump(getIdOnPump());
		scf.setUploadId(getUploadId());
		scf.setFollowNumber(getFollowNumber());
		scf.setOrigin(getOrigin());
		
		scf.setCal_Factor(getDouble(CAL_FACTOR));
		
//		save(e.ENTITY_NAME, e);
	}
	
	public SensorCalFactor getScf()
	{
		return scf;
	}
}
