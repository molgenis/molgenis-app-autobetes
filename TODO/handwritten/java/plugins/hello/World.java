/* Date:        July 12, 2010
 * Template:	PluginScreenJavaTemplateGen.java.ftl
 * generator:   org.molgenis.generators.ui.PluginScreenJavaTemplateGen 3.3.2-testing
 * 
 * THIS FILE IS A TEMPLATE. PLEASE EDIT :-)
 */

package plugins.hello;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.ui.PluginModel;
import org.molgenis.framework.ui.ScreenController;
import org.molgenis.framework.ui.html.ActionInput;
import org.molgenis.framework.ui.html.HtmlInput;
import org.molgenis.framework.ui.html.StringInput;
import org.molgenis.util.Tuple;

public class World extends PluginModel
{
	String name = "world";
	
	public World(String name, ScreenController<?> parent)
	{
		super(name, parent);
	}
	
	public HtmlInput getHello()
	{
		return new StringInput("Hello",this.name);
	}
	
	public HtmlInput getSubmit()
	{
		return new ActionInput("submit");
	}

	@Override
	public String getViewName()
	{
		return "plugins_hello_World";
	}

	@Override
	public String getViewTemplate()
	{
		return "plugins/hello/World.ftl";
	}

	@Override
	public void handleRequest(Database db, Tuple request)
	{
		if(request.getString("Hello") != null)
		{
			this.name = "Hello "+request.getString("Hello");
			
			
//			Experiment e = new Experiment();
//			e.setName(request.getString("Hello"));
//			try
//			{
//				db.add(e);
//			}
//			catch (DatabaseException e1)
//			{
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			}
//			catch (IOException e1)
//			{
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			}
		}
	}

	@Override
	public void reload(Database db)
	{
//		try
//		{
//			Database db = this.getDatabase();
//			Query q = db.query(Experiment.class);
//			q.like("name", "test");
//			List<Experiment> recentExperiments = q.find();
//			
//			//do something
//		}
//		catch(Exception e)
//		{
//			//...
//		}
	}
	
	@Override
	public boolean isVisible()
	{
		//you can use this to hide this plugin, e.g. based on user rights.
		//e.g.
		//if(!this.getLogin().hasEditPermission(myEntity)) return false;
		return true;
	}
}
