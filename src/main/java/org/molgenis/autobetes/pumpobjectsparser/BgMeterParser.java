package org.molgenis.autobetes.pumpobjectsparser;

import org.molgenis.autobetes.autobetes.BgMeter;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.auth.MolgenisUser;

public class BgMeterParser extends ObjectParser
{
	private final static String AMOUNT = "AMOUNT";
	private final static String PARADIGMLINKID = "PARADIGMLINKID";
	private BgMeter bm = new BgMeter();

	public BgMeterParser(Entity rawvalues, DataService dataService, MolgenisUser molgenisUser)
	{
		super(rawvalues, dataService, molgenisUser);
		bm.setOwner(molgenisUser);
		bm.setDateTimeString(getDateTimeString());
		bm.setUnixtimeOriginal(getDateTimeLong());
		bm.setIdOnPump(getIdOnPump());
		bm.setUploadId(getUploadId());
		bm.setFollowNumber(getFollowNumber());
		
		bm.setAmount(getDouble(AMOUNT));
		bm.setParadigmLinkId(getInteger(PARADIGMLINKID));
//		save(BgMeter.ENTITY_NAME, bm);
	}

	public BgMeter getBm()
	{
		return bm;
	}

}
