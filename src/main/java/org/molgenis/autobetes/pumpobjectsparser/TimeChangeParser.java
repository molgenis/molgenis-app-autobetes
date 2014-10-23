package org.molgenis.autobetes.pumpobjectsparser;

import org.molgenis.autobetes.autobetes.TimeChange;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.omx.auth.MolgenisUser;

public class TimeChangeParser extends ObjectParser
{
	private final static String NEW_TIME = "NEW_TIME";
	public TimeChangeParser(Entity csvEntity, DataService dataService, MolgenisUser molgenisUser)
	{
		super(csvEntity, dataService, molgenisUser);
		
		TimeChange tc = new TimeChange();
		tc.setDateTimeString(getDateTimeString());
		tc.setUnixtimeOriginal(getDateTimeLong());
		tc.setIdOnPump(getIdOnPump());
		tc.setUploadId(getUploadId());
		
		tc.setNewTime(getLong(NEW_TIME));
	}

}
