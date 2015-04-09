package org.molgenis.autobetes.pumpobjectsparser;

import org.molgenis.autobetes.autobetes.BGCapturedOnPump;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.validation.MolgenisValidationException;
import org.molgenis.auth.MolgenisUser;

public class BGCapturedOnPumpParser extends ObjectParser
{
	private BGCapturedOnPump bgcp = new BGCapturedOnPump();

	private final static String AMOUNT = "AMOUNT";
	private final static String ACTION_REQUESTOR = "ACTION_REQUESTOR";


	public BGCapturedOnPumpParser(Entity rawvalues, DataService dataService, MolgenisUser molgenisUser)
	{
		super(rawvalues, dataService, molgenisUser);
		bgcp.setOwner(molgenisUser);
		bgcp.setDateTimeString(getDateTimeString());
		bgcp.setUnixtimeOriginal(getDateTimeLong());
		bgcp.setIdOnPump(getIdOnPump());
		bgcp.setUploadId(getUploadId());
		bgcp.setFollowNumber(getFollowNumber());
		bgcp.setOrigin(getOrigin());
			
		bgcp.setAmount(getDouble(AMOUNT));
		bgcp.setAction_Requestor(ACTION_REQUESTOR);
		

	}

	public BGCapturedOnPump getBgcp()
	{
		return bgcp;
	}
}
