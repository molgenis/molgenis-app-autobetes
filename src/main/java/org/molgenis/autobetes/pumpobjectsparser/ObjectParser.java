package org.molgenis.autobetes.pumpobjectsparser;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.auth.MolgenisUser;

public class ObjectParser
{
	private final static String DeviceDateTime = "Tijdstempel";
	private final static String RAWVALUES = "Onbewerkt: waarden";
	private final static String DeviceID = "Onbewerkt: ID";
	private final static String UploadID = "Onbewerkt: upload-ID";
	private final static String FOLLOWNUMBER = "Onbewerkt: volgnummer";

	
	private DataService dataService = null;
	private Map<String, String> keyValueMap = new HashMap<String, String>();

	private MolgenisUser molgenisUser;
	private String dateTimeString;
	private Date dateTime;
	private String idOnPump;
	private String uploadId;
	private String followNumber;
	private String origin = "medtronic";

	public ObjectParser(Entity csvEntity, DataService dataService, MolgenisUser molgenisUser)
	{
		this.dataService = dataService;
		this.molgenisUser = molgenisUser;

		// show progress?
//		System.out.println(">> #line nr:" + csvEntity.get("Index"));

		// parse date, time
		dateTimeString = (String) csvEntity.get(DeviceDateTime);
		followNumber = (String) csvEntity.get(FOLLOWNUMBER);
		// find correct format and parse
		try
		{
			dateTime = new SimpleDateFormat("dd-MM-yy HH:mm:ss").parse(dateTimeString);
		}
		catch (ParseException exeption)
		{
			try
			{
				dateTime = new SimpleDateFormat("dd/MM/yy HH:mm").parse(dateTimeString);
			}
			catch (ParseException e)
			{
				e.printStackTrace();
			}
		}

		// parse pumpId, uploadId
		idOnPump = csvEntity.getString(DeviceID);
		uploadId = csvEntity.getString(UploadID);

		// parse raw values
		Object rawvalues = csvEntity.get(RAWVALUES);
		String raw = (String) rawvalues;
		String[] keyValuePairs = raw.split(", ");

		for (String keyValuePair : keyValuePairs)
		{
			String[] pair = keyValuePair.split("=");
			String key = pair[0];
			String value = pair[1];

			keyValueMap.put(key, value);
		}
	}

	String getString(String key)
	{
		String value = keyValueMap.get(key);
		if ("null".equals(value)) return null;
		else return value;
	}

	Double getDouble(String key)
	{
		String value = getString(key);
		if (null == value) return null;
		else return Double.parseDouble(value.replace(',', '.'));
	}

	Integer getInteger(String key)
	{
		String value = getString(key);
		if (null == value) return null;
		else return Integer.parseInt(value);
	}

	Long getLong(String key)
	{
		String value = getString(key);
		if (null == value) return null;
		else return Long.parseLong(value);
	}

	Boolean getBoolean(String key)
	{
		String value = getString(key);
		if (null == value) return null;
		else return Boolean.parseBoolean(value);
	}

	String getDateTimeString()
	{
		return this.dateTimeString;
	}

	Long getDateTimeLong()
	{
		return this.dateTime.getTime();
	}

	String getIdOnPump()
	{
		return this.idOnPump;
	}

	String getUploadId()
	{
		return this.uploadId;
	}
	String getFollowNumber()
	{
		return this.followNumber;
	}
	public String getOrigin()
	{
		return origin;
	}

	/* WE SAVE ENTITIES AS LIST AND THUS DON'T USE THIS FUNCTION ANYMORE! 
	 * // Save entity in DB void save(String entityName, Entity entity) { entity.set("owner", molgenisUser);
	 * dataService.add(entityName, entity); }
	 */
}
