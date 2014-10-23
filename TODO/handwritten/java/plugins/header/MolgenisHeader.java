/* Date:        November 11, 2009
 * Template:	PluginScreenJavaTemplateGen.java.ftl
 * generator:   org.molgenis.generators.ui.PluginScreenJavaTemplateGen 3.3.2-testing
 * 
 * THIS FILE IS A TEMPLATE. PLEASE EDIT :-)
 */

package plugins.header;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.ui.ApplicationModel;
import org.molgenis.framework.ui.PluginModel;
import org.molgenis.framework.ui.ScreenController;
import org.molgenis.util.Tuple;

import plugins.forum.ForumModel;

/**
 * A simple plugin to create the header of the MOLGENIS application. This
 * includes the header logo as well as the top level menu items for
 * documentation, services etc (replaces the hardcoded header).
 * 
 * @author Morris Swertz
 */
public class MolgenisHeader extends PluginModel
{
	public MolgenisHeader(String name, ScreenController<?> parent)
	{
		super(name, parent);
	}

	@Override
	public String getViewName()
	{
		return "plugins_header_MolgenisHeader";
	}

	@Override
	public String getViewTemplate()
	{
		return "plugins/header/MolgenisHeader.ftl";
	}

	@Override
	public void handleRequest(Database db, Tuple request)
	{
		// static
	}

	@Override
	public void reload(Database db)
	{
		// static
	}

	@Override
	public boolean isVisible()
	{
		return true;
	}

	@Override
	public void reset()
	{
		// TODO Auto-generated method stub

	}

	private String addBootstrap(String header)
	{
		header += "\n" + "<link href=\"myres/bootstrap/css/bootstrap.css\" rel=\"stylesheet\">";
		header = addCss(header, "navbar.css", "myres/css/");
		header += "\n" + "<link href=\"myres/bootstrap/css/bootstrap-responsive.css\" rel=\"stylesheet\">";
		return header;
	}

	private String addCss(String header, String file, String cssPath)
	{
		String prefix = "<link rel=\"stylesheet\" style=\"text/css\" href=\"" + cssPath;
		String postfix = "\">";
		return header + "\n" + prefix + file + postfix;
	}

	private String addJs(String header, String file, String jsPath)
	{
		String prefix = "<script src=\"" + jsPath;
		String postfix = "\" type=\"text/javascript\"></script>\n";
		return header + "\n" + prefix + file + postfix;
	}
	
//	private String addValidationJs(String header)
//	{
//		header = addCss(header, "validation.css", "myres/validation/css/");
//		header = addJs(header, "jquery-1.7.1.min.js", "myres/validation/js/");
//		header = addJs(header, "jquery.validate.min.js", "myres/validation/js/");
//		
//		return header;
//	}

	@Override
	public String getCustomHtmlHeaders()
	{

		String imgPath = "myres/img/";

		String header =  "<link rel=\"shortcut icon\" type=\"image/x-icon\" href=\"" + imgPath + "favicon.ico\"> \n";

		header = addBootstrap(header);
		
//		header = header + "\n <script src=\"//ajax.googleapis.com/ajax/libs/jquery/1.9.1/jquery.min.js\"></script>";
//		header = header + "\n <script src=\"http://code.jquery.com/jquery.js\"></script>";
//		header = header + "\n <script src=\"http://code.jquery.com/ui/jquery-ui-git.js\"></script>";
//		header = addJs(header, "chosen.jquery.js", "myres/js/");
		
		// header = addCss(header, "voedingsdagboek.css", "myres/css/");

		header = addJs(header, "bootstrap.js", "myres/bootstrap/js/");
		
		header = addJs(header, "bootstrap-rowlink.js", "myres/bootstrap-rowlink/js/");
		header = addCss(header, "bootstrap-rowlink.css", "myres/bootstrap-rowlink/css/");
		
//		header = addCss(header, "fiximg.css", "myres/fiximg/");
//		header = addJs(header, "fiximg.js", "myres/fiximg/");

		header = addCss(header, "datepicker.css", "myres/datepicker/css/");
		header = addJs(header, "bootstrap-datepicker.js", "myres/datepicker/js/");
		header = addJs(header, "bootstrap-datepicker.nl.js", "myres/datepicker/js/locales/");

//		header = addCss(header, "bootstrap.icon-large.css", "myres/glyphicons/css/");
		
		header = addCss(header, "footer.css", "myres/css/");
		
		header = addCss(header, "wizard.css", "myres/wizard/css/");
		
//		header = header + "<script type='text/javascript'>var switchTo5x=true;</script>" +
//				"<script type='text/javascript' src='http://w.sharethis.com/button/buttons.js'></script>" +
//				"<script type='text/javascript' src='http://s.sharethis.com/loader.js'></script>";
		
//		header = addCss(header, "enlargeImage.css", "myres/css/");
		
		header = addCss(header, "lmd.css", "myres/css/");

		
//		header = addJs(header, "ajax-chosen.js", "myres/js/");
//		header = addJs(header, "chosen.js", "myres/js/");
		
		// validation:
//		header = addValidationJs(header);
		
		
		if (true)
			return header;
		
		
		
		
		
		
		
		header = addCss(header, "jquery.bt.css", "myres/css/");
		// add google analytics
		header = header + "<script type=\"text/javascript\">\n\n" + "var _gaq = _gaq || [];\n" + "_gaq.push(['_setAccount', 'UA-26947024-1']);\n" + "_gaq.push(['_setDomainName', 'voedingsdagboek.nl']);\n" + "_gaq.push(['_trackPageview']);\n" + "(function() {\n" + "var ga = document.createElement('script'); ga.type = 'text/javascript'; ga.async = true;\n" + "ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';\n"
				+ "var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(ga, s);\n" + "})();" + "</script>\n";

		header = addJs(header, "jquery-latest.js", "myres/js/");
		header = addJs(header, "jquery.bt.js", "myres/js/");
		// header = addJs(header, "jquery.functions.js", "myres/js/");

		// header = addJs(header, "bootstrap-tooltip.js",
		// "myres/bootstrap/js/");
		// header = addJs(header, "bootstrap-popover.js",
		// "myres/bootstrap/js/");

		header = addCss(header, "standalone.css", "myres/jquerytools/css/");
		header = addCss(header, "skin1.css", "myres/jquerytools/css/");
		header = addJs(header, "jquery.tools.min.js", "myres/jquerytools/js/");

		header = addJs(header, "bootstrap.js", "myres/bootstrap/js/");

		header = addJs(header, "voedingsdagboek.jquery.js", "myres/js/");
		// header = addJs(header, "chosen.js",
		// "generated-res/lib/jquery-plugins/");
		header = addJs(header, "chosen.js", "myres/molgenis/js/");
		header = addJs(header, "ajax-chosen.js", "generated-res/lib/jquery-plugins/");

		header = addJs(header, "jquery.validate.js", "myres/js/");
		header = addCss(header, "jquery.validate.password.css", "myres/password/");
		header = addJs(header, "jquery.validate.password.js", "myres/password/");
		header = addJs(header, "dish.js", "myres/js/");

		return header;
	}
}
