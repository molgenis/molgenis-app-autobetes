package org.molgenis.autobetes.pumpobjectsparser;

import org.molgenis.autobetes.autobetes.BGReceived;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.auth.MolgenisUser;

public class BGReceivedParser extends ObjectParser
{
	private BGReceived bgr = new BGReceived();

	private final static String AMOUNT = "AMOUNT";
	private final static String ACTION_REQUESTOR = "ACTION_REQUESTOR";


	public BGReceivedParser(Entity rawvalues, DataService dataService, MolgenisUser molgenisUser)
	{
		super(rawvalues, dataService, molgenisUser);
		bgr.setOwner(molgenisUser);
		bgr.setDateTimeString(getDateTimeString());
		bgr.setUnixtimeOriginal(getDateTimeLong());
		bgr.setIdOnPump(getIdOnPump());
		bgr.setUploadId(getUploadId());
		bgr.setFollowNumber(getFollowNumber());
		bgr.setOrigin(getOrigin());
			
		bgr.setAmount(getDouble(AMOUNT));
		bgr.setAction_Requestor(ACTION_REQUESTOR);
		

	}

	public BGReceived getBGR()
	{
		return bgr;
	}
}
