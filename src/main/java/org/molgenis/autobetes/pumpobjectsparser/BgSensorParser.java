package org.molgenis.autobetes.pumpobjectsparser;

import org.molgenis.autobetes.autobetes.BgSensor;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.validation.MolgenisValidationException;
import org.molgenis.auth.MolgenisUser;

public class BgSensorParser extends ObjectParser
{
	private BgSensor bgSensor = new BgSensor();

	private final static String AMOUNT = "AMOUNT";
	private final static String ISIG = "ISIG";
	private final static String VCNTR = "VCNTR";
	private final static String BACKFILL_INDICATOR = "BACKFILL_INDICATOR";

	public BgSensorParser(Entity rawvalues, DataService dataService, MolgenisUser molgenisUser)
	{
		super(rawvalues, dataService, molgenisUser);
		bgSensor.setOwner(molgenisUser);
		bgSensor.setDateTimeString(getDateTimeString());
		bgSensor.setUnixtimeOriginal(getDateTimeLong());
		bgSensor.setIdOnPump(getIdOnPump());
		bgSensor.setUploadId(getUploadId());
		bgSensor.setFollowNumber(getFollowNumber());
		bgSensor.setOrigin(getOrigin());
		
		bgSensor.setAmount(getDouble(AMOUNT));
		bgSensor.setIsig(getDouble(ISIG));
		bgSensor.setVcntr(getDouble(VCNTR));
		bgSensor.setBackfill_Indicator(getBoolean(BACKFILL_INDICATOR));
		
//		save(BgSensor.ENTITY_NAME, bgSensor);
	}

	public BgSensor getBgSensor()
	{
		return bgSensor;
	}
}
