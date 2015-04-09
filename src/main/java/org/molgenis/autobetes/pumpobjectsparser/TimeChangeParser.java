package org.molgenis.autobetes.pumpobjectsparser;

import org.molgenis.autobetes.autobetes.TimeChange;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.auth.MolgenisUser;

public class TimeChangeParser extends ObjectParser
{
	private final static String NEW_TIME = "NEW_TIME";
	private TimeChange tc = new TimeChange();
	
	public TimeChangeParser(Entity csvEntity, DataService dataService, MolgenisUser molgenisUser)
	{
		super(csvEntity, dataService, molgenisUser);
		
		tc.setOwner(molgenisUser);
		tc.setDateTimeString(getDateTimeString());
		tc.setUnixtimeOriginal(getDateTimeLong());
		tc.setIdOnPump(getIdOnPump());
		tc.setUploadId(getUploadId());
		tc.setFollowNumber(getFollowNumber());
		tc.setOrigin(getOrigin());
		
		tc.setNewTime(getLong(NEW_TIME));
	}
	
	public TimeChange getTc()
	{
		return tc;
	}
}
