package org.molgenis.autobetes;

import org.molgenis.autobetes.autobetes.ActivityEvent;
import org.molgenis.autobetes.controller.AnonymousController;
import org.molgenis.autobetes.controller.HomeController;
import org.molgenis.autobetes.controller.MovesController;
import org.molgenis.data.DataService;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.framework.db.WebAppDatabasePopulatorService;
import org.molgenis.auth.MolgenisUser;
import org.molgenis.auth.UserAuthority;
import org.molgenis.security.MolgenisSecurityWebAppDatabasePopulatorService;
import org.molgenis.security.core.utils.SecurityUtils;
import org.molgenis.security.runas.RunAsSystem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WebAppDatabasePopulatorServiceImpl implements WebAppDatabasePopulatorService
{
	private final DataService dataService;
	private final MolgenisSecurityWebAppDatabasePopulatorService molgenisSecurityWebAppDatabasePopulatorService;
	public static String ADMIN_ID_PREPOSITION = "adminsdf7897dfjgjfug8dfug89ur234sdf";
	private static String ACTIVITY = "Activity";
	
	@Autowired
	public WebAppDatabasePopulatorServiceImpl(DataService dataService,
			MolgenisSecurityWebAppDatabasePopulatorService molgenisSecurityWebAppDatabasePopulatorService)
	{
		if (dataService == null) throw new IllegalArgumentException("DataService is null");
		this.dataService = dataService;

		if (molgenisSecurityWebAppDatabasePopulatorService == null) throw new IllegalArgumentException(
				"MolgenisSecurityWebAppDatabasePopulator is null");
		this.molgenisSecurityWebAppDatabasePopulatorService = molgenisSecurityWebAppDatabasePopulatorService;

	}

	@Override
	@Transactional
	@RunAsSystem
	public void populateDatabase()
	{
		molgenisSecurityWebAppDatabasePopulatorService.populateDatabase(dataService, HomeController.ID);
		//make anonymous user in order to read and write from AnonymousController without being admin
		MolgenisUser anonymousUser = molgenisSecurityWebAppDatabasePopulatorService.getAnonymousUser();
		UserAuthority anonymousHomeAuthority = new UserAuthority();
		anonymousHomeAuthority.setMolgenisUser(anonymousUser);
		anonymousHomeAuthority.setRole(SecurityUtils.AUTHORITY_PLUGIN_WRITE_PREFIX + AnonymousController.ID.toUpperCase());
		//anonymousHomeAuthority.setRole(SecurityUtils.AUTHORITY_PLUGIN_READ_PREFIX + AnonymousController.ID.toUpperCase());
		dataService.add(UserAuthority.ENTITY_NAME, anonymousHomeAuthority);
		
		anonymousHomeAuthority = new UserAuthority();
		anonymousHomeAuthority.setMolgenisUser(anonymousUser);
		anonymousHomeAuthority.setRole(SecurityUtils.AUTHORITY_PLUGIN_READ_PREFIX + AnonymousController.ID.toUpperCase());
		dataService.add(UserAuthority.ENTITY_NAME, anonymousHomeAuthority);
		
		//same goes for movescontroller
		//anonymousUser = molgenisSecurityWebAppDatabasePopulatorService.getAnonymousUser();
		anonymousHomeAuthority = new UserAuthority();
		anonymousHomeAuthority.setMolgenisUser(anonymousUser);
		anonymousHomeAuthority.setRole(SecurityUtils.AUTHORITY_PLUGIN_WRITE_PREFIX + MovesController.ID.toUpperCase());
		dataService.add(UserAuthority.ENTITY_NAME, anonymousHomeAuthority);
		
		anonymousHomeAuthority = new UserAuthority();
		anonymousHomeAuthority.setMolgenisUser(anonymousUser);
		anonymousHomeAuthority.setRole(SecurityUtils.AUTHORITY_PLUGIN_READ_PREFIX + MovesController.ID.toUpperCase());
		dataService.add(UserAuthority.ENTITY_NAME, anonymousHomeAuthority);
		
		//populate with standard activity events 
		populateDBWithStandardEvents(molgenisSecurityWebAppDatabasePopulatorService.getUserAdmin(), dataService);
		
	}

	private void populateDBWithStandardEvents(MolgenisUser userAdmin, DataService dataservice)
	{
		int idappender = 0;
		
		ActivityEvent event =  new ActivityEvent();
		event.setName("Ill");
		event.setOwner(userAdmin);
		event.setId(ADMIN_ID_PREPOSITION+idappender);
		//event.setSpecial(true);
		event.setEventType("activity");
		event.setLastchanged(0l);
		dataservice.add(ActivityEvent.ENTITY_NAME, event);
		
		idappender++;
		event =  new ActivityEvent();
		event.setName("Stress");
		event.setOwner(userAdmin);
		event.setId(ADMIN_ID_PREPOSITION+idappender);
		//event.setSpecial(true);
		event.setEventType(ACTIVITY);
		event.setLastchanged(0l);
		dataservice.add(ActivityEvent.ENTITY_NAME, event);
		
		idappender++;
		event =  new ActivityEvent();
		event.setName("Do not track");
		event.setOwner(userAdmin);
		event.setId(ADMIN_ID_PREPOSITION+idappender);
		//event.setSpecial(true);
		event.setEventType(ACTIVITY);
		event.setLastchanged(0l);
		dataservice.add(ActivityEvent.ENTITY_NAME, event);
		
		idappender++;
		event =  new ActivityEvent();
		event.setName("Sensor inacurrate");
		event.setOwner(userAdmin);
		event.setId(ADMIN_ID_PREPOSITION+idappender);
		//event.setSpecial(true);
		event.setEventType(ACTIVITY);
		event.setLastchanged(0l);
		dataservice.add(ActivityEvent.ENTITY_NAME, event);
		
		idappender++;
		event =  new ActivityEvent();
		event.setName("Holiday");
		event.setOwner(userAdmin);
		event.setId(ADMIN_ID_PREPOSITION+idappender);
		//event.setSpecial(true);
		event.setEventType(ACTIVITY);
		event.setLastchanged(0l);
		dataservice.add(ActivityEvent.ENTITY_NAME, event);
		
		idappender++;
		event =  new ActivityEvent();
		event.setName("Party");
		event.setOwner(userAdmin);
		event.setId(ADMIN_ID_PREPOSITION+idappender);
		//event.setSpecial(true);
		event.setEventType(ACTIVITY);
		event.setLastchanged(0l);
		dataservice.add(ActivityEvent.ENTITY_NAME, event);
		
	}

	@Override
	@Transactional
	@RunAsSystem
	public boolean isDatabasePopulated()
	{
		return dataService.count(MolgenisUser.ENTITY_NAME, new QueryImpl()) > 0;
	}
}