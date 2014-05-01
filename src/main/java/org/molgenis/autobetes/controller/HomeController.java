package org.molgenis.autobetes.controller;

import static org.molgenis.autobetes.controller.HomeController.URI;

import org.molgenis.framework.ui.MolgenisPluginController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller that handles home page requests
 */
@Controller
@RequestMapping(URI)
public class HomeController extends MolgenisPluginController
{
	public static final String ID = "home";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;
	
	public HomeController()
	{
		super(URI);
	}
	
	@RequestMapping
	public String init()
	{
		return "view-home";
	}
}
