package org.molgenis.autobetes.pumpobjectsparser;

import org.molgenis.autobetes.autobetes.ChangeTempBasalPercent;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.auth.MolgenisUser;

public class ChangeTempBasalPercentParser extends ObjectParser
{
	private ChangeTempBasalPercent ctb = new ChangeTempBasalPercent();
	
	private final static String PERCENT_OF_RATE = "PERCENT_OF_RATE";
	private final static String DURATION = "DURATION";

	public ChangeTempBasalPercentParser(Entity csvEntity, DataService dataService, MolgenisUser molgenisUser)
	{
		super(csvEntity, dataService, molgenisUser);
		ctb.setOwner(molgenisUser);
		ctb.setDateTimeString(getDateTimeString());
		ctb.setUnixtimeOriginal(getDateTimeLong());
		ctb.setIdOnPump(getIdOnPump());
		ctb.setUploadId(getUploadId());
		ctb.setFollowNumber(getFollowNumber());

		ctb.setFraction(getDouble(PERCENT_OF_RATE) / 100D);
		ctb.setDuration(getInteger(DURATION));
		
//		save(ctb.ENTITY_NAME, ctb);
	}

	public ChangeTempBasalPercent getCtb()
	{
		return ctb;
	}
}
