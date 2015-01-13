package org.molgenis.autobetes.pumpobjectsparser;

import org.molgenis.autobetes.autobetes.BolusNormal;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.omx.auth.MolgenisUser;

public class BolusNormalParser extends ObjectParser
{
	private BolusNormal bn = new BolusNormal();

	private final static String AMOUNT = "AMOUNT";
	private final static String PROGRAMMED_AMOUNT = "PROGRAMMED_AMOUNT";
	
	public BolusNormalParser(Entity rawvalues, DataService dataService, MolgenisUser molgenisUser)
	{
		super(rawvalues, dataService, molgenisUser);
		
		bn.setDateTimeString(getDateTimeString());
		bn.setUnixtimeOriginal(getDateTimeLong());
		bn.setIdOnPump(getIdOnPump());
		bn.setUploadId(getUploadId());
		
		bn.setAmount(getDouble(AMOUNT));
		bn.setProgrammedAmount(getDouble(PROGRAMMED_AMOUNT));
		
//		save(BolusNormal.ENTITY_NAME, bn);
	}
	
	public BolusNormal getBn()
	{
		return bn;
	}
}
