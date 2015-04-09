package org.molgenis.autobetes.pumpobjectsparser;

import org.molgenis.auth.MolgenisUser;
import org.molgenis.autobetes.autobetes.SensorPacket;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;

public class SensorPacketParser extends ObjectParser
{

	private SensorPacket spt = new SensorPacket();
	private final static String PACKET_TYPE = "PACKET_TYPE";
	private final static String ISIG = "ISIG";	
	private final static String VCNTR = "VCNTR";	
	private final static String BACKFILL_INDICATOR = "BACKFILL_INDICATOR";	
	
	public SensorPacketParser(Entity csvEntity, DataService dataService, MolgenisUser molgenisUser)
	{
		super(csvEntity, dataService, molgenisUser);

		spt.setOwner(molgenisUser);
		spt.setDateTimeString(getDateTimeString());
		spt.setUnixtimeOriginal(getDateTimeLong());
		spt.setIdOnPump(getIdOnPump());
		spt.setUploadId(getUploadId());
		spt.setFollowNumber(getFollowNumber());
		spt.setOrigin(getOrigin());
		
		spt.setVcntr(getDouble(VCNTR));
		spt.setBackfill_Indicator(getBoolean(BACKFILL_INDICATOR));
		spt.setPacket_Type(getString(PACKET_TYPE));
//		save(bpdg.ENTITY_NAME, bpdg);
	}

	public SensorPacket getSpt()
	{
		return spt;
	}
}
