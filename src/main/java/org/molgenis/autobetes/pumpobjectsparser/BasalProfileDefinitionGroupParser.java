package org.molgenis.autobetes.pumpobjectsparser;

import org.molgenis.autobetes.autobetes.BasalProfileDefinitionGroup;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.auth.MolgenisUser;


public class BasalProfileDefinitionGroupParser extends ObjectParser
{
	private BasalProfileDefinitionGroup bpdg = new BasalProfileDefinitionGroup();
	private final static String PATTERN_NAME = "PATTERN_NAME";
	private final static String NUM_PROFILES = "NUM_PROFILES";	
	
	public BasalProfileDefinitionGroupParser(Entity csvEntity, DataService dataService, MolgenisUser molgenisUser)
	{
		super(csvEntity, dataService, molgenisUser);

		bpdg.setOwner(molgenisUser);
		bpdg.setDateTimeString(getDateTimeString());
		bpdg.setUnixtimeOriginal(getDateTimeLong());
		bpdg.setIdOnPump(getIdOnPump());
		bpdg.setUploadId(getUploadId());
		bpdg.setFollowNumber(getFollowNumber());
		bpdg.setOrigin(getOrigin());
		
		bpdg.setName(getString(PATTERN_NAME));
		bpdg.setNumberOfProfiles(getInteger(NUM_PROFILES));
		
//		save(bpdg.ENTITY_NAME, bpdg);
	}

	public BasalProfileDefinitionGroup getBpdg()
	{
		return bpdg;
	}

}
