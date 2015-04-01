package org.molgenis.autobetes.pumpobjectsparser;

import org.molgenis.autobetes.autobetes.CurrentCarbRatioGroup;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.auth.MolgenisUser;

public class CurrentCarbRatioGroupParser extends ObjectParser
{
	private final static String SIZE = "SIZE";

	CurrentCarbRatioGroup ccrg = new CurrentCarbRatioGroup();
	
	public CurrentCarbRatioGroupParser(Entity csvEntity, DataService dataService, MolgenisUser molgenisUser)
	{
		super(csvEntity, dataService, molgenisUser);

		ccrg.setOwner(molgenisUser);
		ccrg.setDateTimeString(getDateTimeString());
		ccrg.setUnixtimeOriginal(getDateTimeLong());
		ccrg.setIdOnPump(getIdOnPump());
		ccrg.setUploadId(getUploadId());
		ccrg.setFollowNumber(getFollowNumber());
		
		ccrg.setNRatios(getInteger(SIZE));
		
//		save(ccrg.ENTITY_NAME, ccrg);
	}
	
	public CurrentCarbRatioGroup getCcrg()
	{
		return ccrg;
	}

}
