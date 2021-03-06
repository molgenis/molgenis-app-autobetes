package org.molgenis.autobetes.pumpobjectsparser;

import org.molgenis.autobetes.autobetes.ChangeTempBasal;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.auth.MolgenisUser;

public class ChangeTempBasalParser extends ObjectParser
{
	private ChangeTempBasal ctbp = new ChangeTempBasal();

	private final static String RATE = "RATE";
	private final static String DURATION = "DURATION";

	public ChangeTempBasalParser(Entity csvEntity, DataService dataService, MolgenisUser molgenisUser)
	{
		super(csvEntity, dataService, molgenisUser);
		ctbp.setOwner(molgenisUser);
		ctbp.setDateTimeString(getDateTimeString());
		ctbp.setUnixtimeOriginal(getDateTimeLong());
		ctbp.setIdOnPump(getIdOnPump());
		ctbp.setUploadId(getUploadId());
		ctbp.setFollowNumber(getFollowNumber());
		ctbp.setOrigin(getOrigin());
		
		ctbp.setRate(getDouble(RATE));
		ctbp.setDuration(getInteger(DURATION));
		
//		save(ctbp.ENTITY_NAME, ctbp);
	}

	public ChangeTempBasal getCtbp()
	{
		return ctbp;
	}

}
