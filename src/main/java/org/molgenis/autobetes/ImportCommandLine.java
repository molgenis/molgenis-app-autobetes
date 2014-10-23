package org.molgenis.autobetes;

import org.molgenis.autobetes.autobetes.ActivityEventInstance;
import org.molgenis.autobetes.autobetes.Event;
import org.molgenis.data.DataService;
import org.molgenis.data.jpa.standalone.JpaStandaloneDataService;
import org.springframework.beans.factory.annotation.Value;

public class ImportCommandLine
{
	DataService dataService;

	public static void main(String[] args)
	{
		new ImportCommandLine().importExample();
	}

	public void importExample()
	{
		try
		{
			dataService = new JpaStandaloneDataService(
					"jdbc:mysql://localhost/autobetes?innodb_autoinc_lock_mode=2&amp;rewriteBatchedStatements=true",
					"molgenis", "molgenis");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		Event bikeEvent = new Event();
		bikeEvent.setEventType("activity");
		bikeEvent.setName("biking");
		bikeEvent.setLastchanged(Long.MIN_VALUE);
		dataService.add(Event.ENTITY_NAME, bikeEvent);

		ActivityEventInstance bikeToWork = new ActivityEventInstance();
		bikeToWork.setSEvent(bikeEvent);
		bikeToWork.setBeginTime(0L);
		bikeToWork.setEndTime(1L);
		bikeToWork.setLastchanged(Long.MIN_VALUE);
		bikeToWork.setIntensity(4d);
		dataService.add(ActivityEventInstance.ENTITY_NAME, bikeToWork);

		for (Event event : dataService.findAll(Event.ENTITY_NAME, Event.class))
		{
			System.out.println(event);
		}
		System.out.println("---");
		for (ActivityEventInstance actEvent : dataService.findAll(ActivityEventInstance.ENTITY_NAME,
				ActivityEventInstance.class))
		{
			System.out.println(actEvent);
		}

	}
}
