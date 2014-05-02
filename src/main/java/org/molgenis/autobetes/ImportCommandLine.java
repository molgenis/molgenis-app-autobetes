package org.molgenis.autobetes;

import org.molgenis.autobetes.autobetes.Event;
import org.molgenis.data.DataService;
import org.molgenis.data.jpa.standalone.JpaStandaloneDataService;

public class ImportCommandLine
{
	public static void main(String[] args)
	{
		new ImportCommandLine().importExample();
	}

	public void importExample()
	{

		try
		{
			DataService dataService = new JpaStandaloneDataService(
					"jdbc:mysql://localhost/autobetes?innodb_autoinc_lock_mode=2&amp;rewriteBatchedStatements=true",
					"molgenis", "molgenis");

			Event pearEvent = new Event();
			pearEvent.setEventType("food");
			pearEvent.setName("Pear");
			dataService.add(Event.ENTITY_NAME, pearEvent);

			for (Event event : dataService.findAll(Event.ENTITY_NAME, Event.class))
			{
				System.out.println(event);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

	}
}
