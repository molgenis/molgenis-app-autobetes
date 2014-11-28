package org.molgenis.autobetes.pumpobjectsparser;

import org.molgenis.autobetes.autobetes.ChangeSuspendEnable;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.omx.auth.MolgenisUser;

public class ChangeSuspendEnableParser extends ObjectParser
{
	private final static String ENABLE = "ENABLE";
	private final static String normal_pumping = "normal_pumping";
	
	public ChangeSuspendEnableParser(Entity csvEntity, DataService dataService, MolgenisUser molgenisUser)
	{
		super(csvEntity, dataService, molgenisUser);

		ChangeSuspendEnable e = new ChangeSuspendEnable();
		e.setDateTimeString(getDateTimeString());
		e.setUnixtimeOriginal(getDateTimeLong());
		e.setIdOnPump(getIdOnPump());
		e.setUploadId(getUploadId());
		
		e.setEnable(getString(ENABLE));
		e.setSuspended(normal_pumping.equals(getString(ENABLE)));
		
		save(e.ENTITY_NAME, e);
	}

}
