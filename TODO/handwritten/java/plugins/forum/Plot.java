package plugins.forum;

import java.io.File;
import java.io.IOException;

import org.molgenis.util.FileLink;

public class Plot
{
	ForumModel model;
	FileLink fl = null;
	String defaultFigure = "";
	String loadingFigure = "myres/img/ajax-loader.gif";
	boolean showDefault = true;

	public Plot(FileLink fl)
	{
		this.fl = fl;
	}

	private FileLink getNewTempFile()
	{
		FileLink newFL = null;
		try
		{
			newFL = model.controller.getTempFile();
		} catch (IOException e)
		{
			System.err.println(">> Not able to create tempFile... ");
			e.printStackTrace();
		}
		
		return newFL;
	}

	public Plot(ForumModel model, String defaultFigure)
	{
		this.model = model;

		this.fl = null;

		this.defaultFigure = defaultFigure;
	}

	public boolean exists()
	{
		return null != fl;
	}

	public void erase()
	{
		fl = null;
	}

	public String getRealPath()
	{
		if (exists())
			return fl.getLocalpath().getAbsolutePath().replace("\\", "/") + ".png";
		else
			return null; // should not happen
	}

	public String getWebPath()
	{
		if (exists())
			return fl.getLink() + ".png";
		else if (showDefault)
			return defaultFigure;
		else
			return loadingFigure;
	}

	public void showDefault()
	{
		this.showDefault = true;
	}

	public void showLoading()
	{
		this.showDefault = false;
	}

	public void refreshPath()
	{
		this.fl = getNewTempFile();
	}

//	public void setRealPath(String file)
//	{
//		this.refreshPath();
//		this.fl.setLink(null);
//		this.fl.setLocalpath(new File(file));
//	}

	public void setWebPath(String file)
	{
		this.refreshPath();
		this.fl.setLink(file.substring(0, file.length() - 4));
		this.fl.setLocalpath(null);
	}
}
