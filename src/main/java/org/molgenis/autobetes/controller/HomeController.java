package org.molgenis.autobetes.controller;

import static org.molgenis.autobetes.controller.HomeController.URI;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;

import org.molgenis.data.DataService;
import org.molgenis.framework.ui.MolgenisPluginController;
import org.molgenis.omx.converters.ValueConverterException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

/**
 * Controller that handles home page requests
 */
@Controller
@RequestMapping(URI)
public class HomeController extends MolgenisPluginController
{
	public static final String ID = "home";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;
	public static final String BASE_URI = "";

	@Autowired
	private DataService dataService;

	@Autowired
	private JavaMailSender mailSender;

	public HomeController()
	{
		super(URI);
	}

	@RequestMapping
	public String init()
	{
		return "view-home";
	}

	@RequestMapping("upload-csv")
	public String uploadForm() throws InterruptedException
	{
		return "view-upload-pump-csv";
	}

	@RequestMapping("view-report")
	public String viewLogo() throws InterruptedException
	{
		return "view-report";
	}

	@RequestMapping("upload")
	public String upload() throws InterruptedException
	{
		return "view-upload";
	}

	@RequestMapping(method = RequestMethod.POST, value = "/validate")
	@PreAuthorize("hasAnyRole('ROLE_SU')")
	public String validate(HttpServletRequest request, @RequestParam("csvFile") MultipartFile csvFile, Model model)
			throws IOException, ValueConverterException, MessagingException, Exception
	{
		System.err.println("in validate...");
		boolean submitState = false;
		String action = "/validate";
		String enctype = "multipart/form-data";

		final List<String> messages = new ArrayList<String>();
		if (!csvFile.isEmpty())
		{
			try
			{
				System.err.println("Gelukt!");
			}
			catch (Exception e)
			{
				System.err.println("FOUT!");
			}
		}
		else
		{
			String errorMessage = "The file you try to upload is empty! Filename: " + csvFile.getOriginalFilename();
			// messages.add(errorMessage);
			System.err.println(errorMessage);
		}
		model.addAttribute("action", action);
		model.addAttribute("enctype", enctype);
		model.addAttribute("submit_state", submitState);
		model.addAttribute("messages", messages);
		return "view-upload";
	}
}
