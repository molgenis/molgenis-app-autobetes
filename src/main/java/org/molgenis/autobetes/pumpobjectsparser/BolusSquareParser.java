package org.molgenis.autobetes.pumpobjectsparser;

import org.molgenis.autobetes.autobetes.BolusSquare;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.omx.auth.MolgenisUser;

public class BolusSquareParser extends ObjectParser
{
	private BolusSquare bs = new BolusSquare();

	private final static String AMOUNT = "AMOUNT";
	private final static String PROGRAMMED_AMOUNT = "PROGRAMMED_AMOUNT";
	private final static String DURATION = "DURATION";

	public BolusSquareParser(Entity rawvalues, DataService dataService, MolgenisUser molgenisUser)
	{
		super(rawvalues, dataService, molgenisUser);

		bs.setDateTimeString(getDateTimeString());
		bs.setUnixtimeOriginal(getDateTimeLong());
		bs.setIdOnPump(getIdOnPump());
		bs.setUploadId(getUploadId());

		bs.setAmount(getDouble(AMOUNT));
		bs.setProgrammedAmount(getDouble(PROGRAMMED_AMOUNT));
		bs.setDuration(getLong(DURATION));

//		save(BolusSquare.ENTITY_NAME, bs);
	}

	public BolusSquare getBs()
	{
		return bs;
	}
}
