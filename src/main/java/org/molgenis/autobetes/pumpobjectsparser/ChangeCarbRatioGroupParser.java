package org.molgenis.autobetes.pumpobjectsparser;

import org.molgenis.autobetes.autobetes.BasalProfileDefinitionGroup;
import org.molgenis.autobetes.autobetes.BolusSquare;
import org.molgenis.autobetes.autobetes.ChangeCarbRatioGroup;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.auth.MolgenisUser;

public class ChangeCarbRatioGroupParser extends ObjectParser
{
	private final static String SIZE = "SIZE";

	ChangeCarbRatioGroup ccrg = new ChangeCarbRatioGroup();
	
	public ChangeCarbRatioGroupParser(Entity csvEntity, DataService dataService, MolgenisUser molgenisUser)
	{
		super(csvEntity, dataService, molgenisUser);

		ccrg.setOwner(molgenisUser);
		ccrg.setDateTimeString(getDateTimeString());
		ccrg.setUnixtimeOriginal(getDateTimeLong());
		ccrg.setIdOnPump(getIdOnPump());
		ccrg.setUploadId(getUploadId());
		ccrg.setFollowNumber(getFollowNumber());
		ccrg.setOrigin(getOrigin());
		
		ccrg.setNRatios(getInteger(SIZE));
		
//		save(ccrg.ENTITY_NAME, ccrg);
	}
	
	public ChangeCarbRatioGroup getCcrg()
	{
		return ccrg;
	}

}
