package org.molgenis.autobetes;

import org.molgenis.autobetes.controller.AnonymousController;
import org.molgenis.autobetes.controller.HomeController;
import org.molgenis.autobetes.controller.MovesController;
import org.molgenis.data.DataService;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.framework.db.WebAppDatabasePopulatorService;
import org.molgenis.omx.auth.MolgenisUser;
import org.molgenis.omx.auth.UserAuthority;
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
		//make anonymous user in order to access AnonymousController without being admin
		MolgenisUser anonymousUser = molgenisSecurityWebAppDatabasePopulatorService.getAnonymousUser();
		UserAuthority anonymousHomeAuthority = new UserAuthority();
		anonymousHomeAuthority.setMolgenisUser(anonymousUser);
		anonymousHomeAuthority.setRole(SecurityUtils.AUTHORITY_PLUGIN_WRITE_PREFIX + AnonymousController.ID.toUpperCase());
		
		dataService.add(UserAuthority.ENTITY_NAME, anonymousHomeAuthority);
		
		//same goes for movescontroller
		anonymousHomeAuthority.setRole(SecurityUtils.AUTHORITY_PLUGIN_WRITE_PREFIX + MovesController.ID.toUpperCase());
		dataService.add(UserAuthority.ENTITY_NAME, anonymousHomeAuthority);
		
	}

	@Override
	@Transactional
	@RunAsSystem
	public boolean isDatabasePopulated()
	{
		return dataService.count(MolgenisUser.ENTITY_NAME, new QueryImpl()) > 0;
	}
}