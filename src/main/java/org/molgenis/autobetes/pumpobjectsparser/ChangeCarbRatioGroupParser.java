package org.molgenis.autobetes.pumpobjectsparser;

import org.molgenis.autobetes.autobetes.BasalProfileDefinitionGroup;
import org.molgenis.autobetes.autobetes.ChangeCarbRatioGroup;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.omx.auth.MolgenisUser;

public class ChangeCarbRatioGroupParser extends ObjectParser
{
	private final static String SIZE = "SIZE";
	
	public ChangeCarbRatioGroupParser(Entity csvEntity, DataService dataService, MolgenisUser molgenisUser)
	{
		super(csvEntity, dataService, molgenisUser);

		ChangeCarbRatioGroup ccrg = new ChangeCarbRatioGroup();
		ccrg.setDateTimeString(getDateTimeString());
		ccrg.setUnixtimeOriginal(getDateTimeLong());
		ccrg.setIdOnPump(getIdOnPump());
		ccrg.setUploadId(getUploadId());
		
		ccrg.setNRatios(getInteger(SIZE));
		
//		save(ccrg.ENTITY_NAME, ccrg);
	}

}
