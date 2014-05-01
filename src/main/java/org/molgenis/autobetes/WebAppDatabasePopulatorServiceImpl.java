package org.molgenis.autobetes;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.molgenis.autobetes.controller.HomeController;
import org.molgenis.data.DataService;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.dataexplorer.controller.DataExplorerController;
import org.molgenis.framework.db.WebAppDatabasePopulatorService;
import org.molgenis.omx.auth.GroupAuthority;
import org.molgenis.omx.auth.MolgenisGroup;
import org.molgenis.omx.auth.MolgenisUser;
import org.molgenis.omx.auth.UserAuthority;
import org.molgenis.omx.core.RuntimeProperty;
import org.molgenis.security.account.AccountService;
import org.molgenis.security.core.utils.SecurityUtils;
import org.molgenis.security.runas.RunAsSystem;
import org.molgenis.security.user.UserAccountController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WebAppDatabasePopulatorServiceImpl implements WebAppDatabasePopulatorService
{
	private static final String USERNAME_ADMIN = "admin";

	public static final String INITLOCATION = "initLocation";
	public static final String COORDSYSTEM = "coordSystem";
	public static final String CHAINS = "chains";
	public static final String SOURCES = "sources";
	public static final String BROWSERLINKS = "browserLinks";
	public static final String SEARCHENDPOINT = "searchEndpoint";
	public static final String KARYOTYPEENDPOINT = "karyotypeEndpoint";
	public static final String GENOMEBROWSERTABLE = "genomeBrowserTable";

	private final DataService dataService;

	@Value("${admin.password:@null}")
	private String adminPassword;
	@Value("${admin.email:molgenis+admin@gmail.com}")
	private String adminEmail;
	@Value("${anonymous.email:molgenis+anonymous@gmail.com}")
	private String anonymousEmail;

	@Autowired
	public WebAppDatabasePopulatorServiceImpl(DataService dataService)
	{
		if (dataService == null) throw new IllegalArgumentException("DataService is null");
		this.dataService = dataService;
	}

	@Override
	@Transactional
	@RunAsSystem
	public void populateDatabase()
	{
		if (adminPassword == null) throw new RuntimeException(
				"please configure the admin.password property in your molgenis-server.properties");

		MolgenisUser userAdmin = new MolgenisUser();
		userAdmin.setUsername(USERNAME_ADMIN);
		userAdmin.setPassword(new BCryptPasswordEncoder().encode(adminPassword));
		userAdmin.setEmail(adminEmail);
		userAdmin.setActive(true);
		userAdmin.setSuperuser(true);
		dataService.add(MolgenisUser.ENTITY_NAME, userAdmin);

		MolgenisUser anonymousUser = new MolgenisUser();
		anonymousUser.setUsername(SecurityUtils.ANONYMOUS_USERNAME);
		anonymousUser.setPassword(new BCryptPasswordEncoder().encode(SecurityUtils.ANONYMOUS_USERNAME));
		anonymousUser.setEmail(anonymousEmail);
		anonymousUser.setActive(true);
		anonymousUser.setSuperuser(false);
		dataService.add(MolgenisUser.ENTITY_NAME, anonymousUser);

		UserAuthority anonymousAuthority = new UserAuthority();
		anonymousAuthority.setMolgenisUser(anonymousUser);
		anonymousAuthority.setRole(SecurityUtils.AUTHORITY_ANONYMOUS);
		dataService.add(UserAuthority.ENTITY_NAME, anonymousAuthority);

		MolgenisGroup usersGroup = new MolgenisGroup();
		usersGroup.setName(AccountService.ALL_USER_GROUP);
		dataService.add(MolgenisGroup.ENTITY_NAME, usersGroup);
		usersGroup.setName(AccountService.ALL_USER_GROUP);

		GroupAuthority usersGroupHomeAuthority = new GroupAuthority();
		usersGroupHomeAuthority.setMolgenisGroup(usersGroup);
		usersGroupHomeAuthority.setRole(SecurityUtils.AUTHORITY_PLUGIN_READ_PREFIX + HomeController.ID.toUpperCase());
		dataService.add(GroupAuthority.ENTITY_NAME, usersGroupHomeAuthority);

		GroupAuthority usersGroupUserAccountAuthority = new GroupAuthority();
		usersGroupUserAccountAuthority.setMolgenisGroup(usersGroup);
		usersGroupUserAccountAuthority.setRole(SecurityUtils.AUTHORITY_PLUGIN_WRITE_PREFIX
				+ UserAccountController.ID.toUpperCase());
		dataService.add(GroupAuthority.ENTITY_NAME, usersGroupUserAccountAuthority);

		for (String entityName : dataService.getEntityNames())
		{
			GroupAuthority entityAuthority = new GroupAuthority();
			entityAuthority.setMolgenisGroup(usersGroup);
			entityAuthority.setRole(SecurityUtils.AUTHORITY_ENTITY_READ_PREFIX + entityName.toUpperCase());
			dataService.add(GroupAuthority.ENTITY_NAME, entityAuthority);
		}

		// Genomebrowser stuff
		Map<String, String> runtimePropertyMap = new HashMap<String, String>();

		// Charts include/exclude charts
		runtimePropertyMap.put(DataExplorerController.KEY_MOD_AGGREGATES, String.valueOf(true));
		runtimePropertyMap.put(DataExplorerController.KEY_MOD_CHARTS, String.valueOf(true));
		runtimePropertyMap.put(DataExplorerController.KEY_MOD_DATA, String.valueOf(true));

		// Annotators include files/tools
		String molgenisHomeDir = System.getProperty("molgenis.home");

		if (molgenisHomeDir == null)
		{
			throw new IllegalArgumentException("missing required java system property 'molgenis.home'");
		}

		if (!molgenisHomeDir.endsWith("/")) molgenisHomeDir = molgenisHomeDir + '/';

		for (Entry<String, String> entry : runtimePropertyMap.entrySet())
		{
			RuntimeProperty runtimeProperty = new RuntimeProperty();
			String propertyKey = entry.getKey();
			runtimeProperty.setIdentifier(RuntimeProperty.class.getSimpleName() + '_' + propertyKey);
			runtimeProperty.setName(propertyKey);
			runtimeProperty.setValue(entry.getValue());
			dataService.add(RuntimeProperty.ENTITY_NAME, runtimeProperty);
		}
	}

	@Override
	@Transactional
	@RunAsSystem
	public boolean isDatabasePopulated()
	{
		return dataService.count(MolgenisUser.ENTITY_NAME, new QueryImpl()) > 0;
	}
}