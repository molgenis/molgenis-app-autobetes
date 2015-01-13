package org.molgenis.autobetes.pumpobjectsparser;

import org.molgenis.autobetes.autobetes.ChangeSuspendEnable;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.omx.auth.MolgenisUser;

public class ChangeSuspendEnableParser extends ObjectParser
{
	private ChangeSuspendEnable suspend = new ChangeSuspendEnable();
	
	private final static String STATE = "STATE";
	private final static String ACTIONREQUESTOR = "ACTIONREQUESTOR";
	private final static String PRESTATE = "PRESTATE";	
	private final static String normal_pumping = "normal_pumping"; // alternative: "user_suspend"
	
	public ChangeSuspendEnableParser(Entity csvEntity, DataService dataService, MolgenisUser molgenisUser)
	{
		super(csvEntity, dataService, molgenisUser);
		suspend.setDateTimeString(getDateTimeString());
		suspend.setUnixtimeOriginal(getDateTimeLong());
		suspend.setIdOnPump(getIdOnPump());
		suspend.setUploadId(getUploadId());
		
		suspend.setState(getString(STATE));
		suspend.setActionRequestor(getString(ACTIONREQUESTOR));
		suspend.setPreState(getString(PRESTATE));
		suspend.setSuspended(normal_pumping.equals(getString(STATE)));
		
//		save(e.ENTITY_NAME, e);
	}

	public ChangeSuspendEnable getE()
	{
		return suspend;
	}
}
