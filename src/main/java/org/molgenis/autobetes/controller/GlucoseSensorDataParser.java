package org.molgenis.autobetes.controller;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.simple.JSONArray;

public class GlucoseSensorDataParser
{
	List<GlucoseSensorData> lst = new ArrayList<GlucoseSensorData>();

	public GlucoseSensorDataParser(Object j)
	{
		// if j array, then for each el append lst
		if (j instanceof JSONArray)
		{
			JSONArray array = (JSONArray) j;

			@SuppressWarnings("unchecked")
			Iterator<Object> it = array.iterator();
			while (it.hasNext())
			{
				Object o = it.next();
				GlucoseSensorDataParser glucParser = new GlucoseSensorDataParser(o);
				List<GlucoseSensorData> subLst = glucParser.getList();
				lst.addAll(subLst);
			}
		}
		else
		{
			// else if j is gluc sensor, then parse j and add to list, return
			GlucoseSensorData g = null;
			try
			{
				g = new GlucoseSensorData(j);
				this.lst.add(g);
			}
			catch (Exception e)
			{
				// other record, do nothing
			}
		}
	}

	public List<GlucoseSensorData> getList()
	{
		return lst;
	}
}
