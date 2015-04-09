package org.molgenis.autobetes.pumpobjectsparser;

import org.molgenis.autobetes.autobetes.UnabsorbedInsulin;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.auth.MolgenisUser;

public class UnabsorbedInsulinParser extends ObjectParser
{
	private UnabsorbedInsulin bgr = new UnabsorbedInsulin();

	private final static String AMOUNT = "AMOUNT";
	private final static String BOLUS_ESTIMATE_DATUM_ID = "BOLUS_ESTIMATE_DATUM_ID";
	private final static String INDEX = "INDEX";
	private final static String RECORD_AGE = "RECORD_AGE";


	public UnabsorbedInsulinParser(Entity rawvalues, DataService dataService, MolgenisUser molgenisUser)
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
		bgr.setBolus_Estimate_Datum_Id(getString(BOLUS_ESTIMATE_DATUM_ID));
		bgr.setIns_Index(getDouble(INDEX));
		bgr.setRecord_Age(getInteger(RECORD_AGE));
	}

	public UnabsorbedInsulin getUi()
	{
		return bgr;
	}
}
