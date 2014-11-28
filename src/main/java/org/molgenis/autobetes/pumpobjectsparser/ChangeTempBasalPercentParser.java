package org.molgenis.autobetes.pumpobjectsparser;

import org.molgenis.autobetes.autobetes.ChangeTempBasalPercent;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.omx.auth.MolgenisUser;

public class ChangeTempBasalPercentParser extends ObjectParser
{
	private final static String PERCENT_OF_RATE = "PERCENT_OF_RATE";
	private final static String DURATION = "DURATION";

	public ChangeTempBasalPercentParser(Entity csvEntity, DataService dataService, MolgenisUser molgenisUser)
	{
		super(csvEntity, dataService, molgenisUser);

		ChangeTempBasalPercent ctb = new ChangeTempBasalPercent();
		ctb.setDateTimeString(getDateTimeString());
		ctb.setUnixtimeOriginal(getDateTimeLong());
		ctb.setIdOnPump(getIdOnPump());
		ctb.setUploadId(getUploadId());

		ctb.setFraction(getDouble(PERCENT_OF_RATE) / 100D);
		ctb.setDuration(getInteger(DURATION));
		
		save(ctb.ENTITY_NAME, ctb);
	}

}
