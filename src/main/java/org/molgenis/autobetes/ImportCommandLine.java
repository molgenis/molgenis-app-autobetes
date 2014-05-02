package org.molgenis.autobetes;

import javax.persistence.EntityManager;
import javax.persistence.Persistence;

import org.molgenis.autobetes.autobetes.Event;
import org.molgenis.autobetes.autobetes.EventRepository;
import org.molgenis.data.DataService;
import org.molgenis.data.support.DataServiceImpl;
import org.molgenis.data.support.QueryResolver;
import org.molgenis.data.validation.DefaultEntityValidator;
import org.molgenis.data.validation.EntityAttributesValidator;

public class ImportCommandLine
{
	public static void main(String[] args)
	{
		new ImportCommandLine().importExample();
	}

	public void importExample()
	{
		DataService dataService = new DataServiceImpl();
		EntityManager entityManager = Persistence.createEntityManagerFactory("autobetes-commandline")
				.createEntityManager();

		EventRepository eventRepo = new EventRepository(entityManager, new DefaultEntityValidator(dataService,
				new EntityAttributesValidator()), new QueryResolver(dataService));
		dataService.addRepository(eventRepo);

		entityManager.getTransaction().begin();
		try
		{
			Event event = new Event();
			event.setEventType("food");
			event.setName("Apple");
			dataService.add(Event.ENTITY_NAME, event);

			entityManager.getTransaction().commit();
			System.out.println("Event added");
		}
		catch (Exception e)
		{
			e.printStackTrace();
			entityManager.getTransaction().rollback();
		}
		finally
		{
			entityManager.close();
		}

	}

}
