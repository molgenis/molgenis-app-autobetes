package org.molgenis.autobetes.pumpobjectsparser;

import org.molgenis.autobetes.autobetes.BgMeter;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.omx.auth.MolgenisUser;

public class BgMeterParser extends ObjectParser
{
	private final static String AMOUNT = "AMOUNT";

	public BgMeterParser(Entity rawvalues, DataService dataService, MolgenisUser molgenisUser)
	{
		super(rawvalues, dataService, molgenisUser);

		BgMeter bm = new BgMeter();
		bm.setDateTimeString(getDateTimeString());
		bm.setUnixtimeOriginal(getDateTimeLong());
		bm.setIdOnPump(getIdOnPump());
		bm.setUploadId(getUploadId());

		bm.setAmount(getDouble(AMOUNT));

//		save(BgMeter.ENTITY_NAME, bm);
	}

}
