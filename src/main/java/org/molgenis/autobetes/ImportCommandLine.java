package org.molgenis.autobetes;

import javax.persistence.EntityManager;
import javax.persistence.Persistence;

import org.apache.commons.io.IOUtils;
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

		EventRepository repo = new EventRepository(entityManager, new DefaultEntityValidator(dataService,
				new EntityAttributesValidator()), new QueryResolver(dataService));

		entityManager.getTransaction().begin();
		try
		{
			Event event = new Event();
			event.setEventType("food");
			event.setName("Apple");
			repo.add(event);
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
			IOUtils.closeQuietly(repo);
		}

	}

}
