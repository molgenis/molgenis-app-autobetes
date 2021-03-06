package org.molgenis.autobetes;

import java.util.EnumSet;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import javax.servlet.FilterRegistration.Dynamic;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.molgenis.security.CorsFilter;
import org.molgenis.ui.MolgenisWebAppInitializer;
import org.molgenis.util.ApplicationContextProvider;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.web.WebApplicationInitializer;

public class WebAppInitializer extends MolgenisWebAppInitializer implements WebApplicationInitializer
{
	@Override
	public void onStartup(ServletContext servletContext) throws ServletException
	{
		super.onStartup(servletContext, WebAppConfig.class, true);
//		FilterRegistration corsFilter = servletContext.getFilterRegistration("corsFilter");
//		corsFilter.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), true, "/plugin/*");
		Dynamic corsFilter = servletContext.addFilter("autobetesCorsFilter", AutobetesCorsFilter.class);
		corsFilter.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), true, "/plugin/anonymous/*");
		corsFilter.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), true, "/plugin/moves/*");
		corsFilter.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), true, "/scripts/*");
	}
}