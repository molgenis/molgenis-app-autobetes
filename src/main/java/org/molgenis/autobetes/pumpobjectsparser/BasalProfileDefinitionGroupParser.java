package org.molgenis.autobetes.pumpobjectsparser;

import org.molgenis.autobetes.autobetes.BasalProfileDefinitionGroup;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.omx.auth.MolgenisUser;

public class BasalProfileDefinitionGroupParser extends ObjectParser
{
	private final static String PATTERN_NAME = "PATTERN_NAME";
	private final static String NUM_PROFILES = "NUM_PROFILES";	
	
	public BasalProfileDefinitionGroupParser(Entity csvEntity, DataService dataService, MolgenisUser molgenisUser)
	{
		super(csvEntity, dataService, molgenisUser);

		BasalProfileDefinitionGroup bpdg = new BasalProfileDefinitionGroup();
		bpdg.setDateTimeString(getDateTimeString());
		bpdg.setUnixtimeOriginal(getDateTimeLong());
		bpdg.setIdOnPump(getIdOnPump());
		bpdg.setUploadId(getUploadId());
		
		bpdg.setName(getString(PATTERN_NAME));
		bpdg.setNumberOfProfiles(getInteger(NUM_PROFILES));
		
//		save(bpdg.ENTITY_NAME, bpdg);
	}

}
