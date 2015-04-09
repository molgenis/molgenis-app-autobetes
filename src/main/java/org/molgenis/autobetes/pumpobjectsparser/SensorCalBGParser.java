package org.molgenis.autobetes.pumpobjectsparser;

import org.molgenis.auth.MolgenisUser;
import org.molgenis.autobetes.autobetes.ChangeCarbRatio;
import org.molgenis.autobetes.autobetes.SensorCalBG;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;

public class SensorCalBGParser extends ObjectParser
{

	private final static String ORIGIN_TYPE = "ORIGIN_TYPE";
	private final static String AMOUNT = "AMOUNT";
	private SensorCalBG scbg = new SensorCalBG();
	

	public SensorCalBGParser(Entity csvEntity, DataService dataService, MolgenisUser molgenisUser)
	{
		super(csvEntity, dataService, molgenisUser);

		
		scbg.setOwner(molgenisUser);
		scbg.setDateTimeString(getDateTimeString());
		scbg.setUnixtimeOriginal(getDateTimeLong());
		scbg.setIdOnPump(getIdOnPump());
		scbg.setUploadId(getUploadId());
		scbg.setFollowNumber(getFollowNumber());
		scbg.setOrigin(getOrigin());
		
		scbg.setAmount(getDouble(AMOUNT));
		scbg.setOrigin_Type(getString(ORIGIN_TYPE));
		
//		save(e.ENTITY_NAME, e);
	}
	
	public SensorCalBG getScbg()
	{
		return scbg;
	}
}
