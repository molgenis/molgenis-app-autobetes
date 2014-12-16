package org.molgenis.autobetes.controller;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.simple.JSONObject;

public class GlucoseSensorData
{
	private final String dateTimeString;
	private final Date dateTime;
	private final Integer amount;

	public GlucoseSensorData(Object obj) throws ParseException, ClassCastException
	{
		// convert
		JSONObject j = (JSONObject) obj;
		
		// set date
		this.dateTimeString = (String) j.get("date");
		dateTime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(dateTimeString);
		
		// set amount
		this.amount = Integer.valueOf(j.get("sgv").toString());
	}
	
	public Date getDateTime()
	{
		return dateTime;
	}
	
	public Integer getAmount()
	{
		return amount;
	}
}
