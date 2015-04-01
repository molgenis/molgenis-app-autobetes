package org.molgenis.autobetes.pumpobjectsparser;

import org.molgenis.autobetes.autobetes.CurrentBasalProfileGroup;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.auth.MolgenisUser;

import scala.annotation.meta.setter;

public class CurrentBasalProfileGroupParser extends ObjectParser
{
	private CurrentBasalProfileGroup cbpdg = new CurrentBasalProfileGroup();
	private final static String PATTERN_NAME = "PATTERN_NAME";
	private final static String NUM_PROFILES = "NUM_PROFILES";	
	
	public CurrentBasalProfileGroupParser(Entity csvEntity, DataService dataService, MolgenisUser molgenisUser)
	{
		super(csvEntity, dataService, molgenisUser);
		cbpdg.setOwner(molgenisUser);
		cbpdg.setDateTimeString(getDateTimeString());
		cbpdg.setUnixtimeOriginal(getDateTimeLong());
		cbpdg.setIdOnPump(getIdOnPump());
		cbpdg.setUploadId(getUploadId());
		cbpdg.setFollowNumber(getFollowNumber());
		
		cbpdg.setName(getString(PATTERN_NAME));
		cbpdg.setNumberOfProfiles(getInteger(NUM_PROFILES));
		System.out.println(cbpdg.toString());
//		save(bpdg.ENTITY_NAME, bpdg);
	}

	public CurrentBasalProfileGroup getCbpdg()
	{
		return cbpdg;
	}

}
