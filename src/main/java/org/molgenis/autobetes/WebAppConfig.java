package org.molgenis.autobetes;

import org.molgenis.data.DataService;
import org.molgenis.elasticsearch.config.EmbeddedElasticSearchConfig;
import org.molgenis.framework.ui.MolgenisPluginController;
import org.molgenis.omx.OmxConfig;
import org.molgenis.omx.config.DataExplorerConfig;
import org.molgenis.security.user.MolgenisUserService;
import org.molgenis.ui.MolgenisWebAppConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;

@Configuration
@EnableTransactionManagement
@EnableWebMvc
@EnableAsync
@ComponentScan("org.molgenis")
@Import(
{  OmxConfig.class, EmbeddedElasticSearchConfig.class,
		DataExplorerConfig.class})
public class WebAppConfig extends MolgenisWebAppConfig
{
	@Autowired
	private DataService dataService;
	@Autowired
	private MolgenisUserService molgenisUserService;

	@Bean
	@Qualifier("autobetesService")
	public Object autobetesManagerService()
	{
		return new Object();
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry)
	{
		//enable cross origin
		String pluginInterceptPattern = MolgenisPluginController.PLUGIN_URI_PREFIX + "**";
		String corsInterceptPattern = "/api/**";
		registry.addInterceptor(molgenisPluginInterceptor()).addPathPatterns(pluginInterceptPattern);
		registry.addInterceptor(corsInterceptor()).addPathPatterns(corsInterceptPattern);
		
		corsInterceptPattern = "/plugin/anonymous/**";
		registry.addInterceptor(molgenisPluginInterceptor()).addPathPatterns(pluginInterceptPattern);
		registry.addInterceptor(corsInterceptor()).addPathPatterns(corsInterceptPattern);
		
		
		
	}
}
