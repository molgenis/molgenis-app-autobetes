package org.molgenis.autobetes.controller;

import static org.molgenis.autobetes.controller.HomeController.URI;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.data.DataService;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.framework.ui.MolgenisPluginController;
import org.molgenis.omx.auth.MolgenisUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * Controller that handles home page requests
 */
@Controller
@RequestMapping(URI)
public class HomeController extends MolgenisPluginController
{
	public static final String ID = "home";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;

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

	@RequestMapping(value = "/activate/{activationCode}", method = RequestMethod.GET)
	@ResponseBody
	public Map<String, Object> activateUser(@PathVariable String activationCode)
	{
		System.out.println(">> Activation code: " + activationCode);

		MolgenisUser mu = dataService.findOne(MolgenisUser.ENTITY_NAME,
				new QueryImpl().eq(MolgenisUser.ACTIVATIONCODE, activationCode), MolgenisUser.class);

		if (null == mu)
		{
			return response(false, "Registration failed. No user with this activation code.");
		}

		mu.setActive(true);

		dataService.update(MolgenisUser.ENTITY_NAME, mu);

		return response(true, "You're account is now active!");
	}

	@RequestMapping(value = "/registerUser", method = RequestMethod.POST, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	public Map<String, Object> registerUser(@RequestBody RegistrationRequest registrationRequest,
			HttpServletRequest servletRequest)
	{
		// validate email + pw
		if (StringUtils.isBlank(registrationRequest.getEmail())
				|| StringUtils.isBlank(registrationRequest.getPassword()))
		{
			return response(false, "Registration failed. Please provide a valid email and password!");
		}

		MolgenisUser existingUser = dataService.findOne(MolgenisUser.ENTITY_NAME,
				new QueryImpl().eq(MolgenisUser.EMAIL, registrationRequest.getEmail()), MolgenisUser.class);

		if (null != existingUser)
		{
			return response(false,
					"Registration failed. Email already exists. Please click 'Forgotten' to get a new password.");
		}
		MolgenisUser mu = new MolgenisUser();
		mu.setUsername(registrationRequest.getEmail());
		mu.setPassword(registrationRequest.getPassword());
		mu.setEmail(registrationRequest.getEmail());
		String activationCode = UUID.randomUUID().toString();
		mu.setActivationCode(activationCode);
		mu.setActive(false);
		try
		{
			dataService.add(MolgenisUser.ENTITY_NAME, mu);
		}
		catch (Exception e)
		{
			return response(false, "Registration failed. Please contact the developers.");
		}

		// send activation email
		try
		{
			String activationUriBase = null;
			if (StringUtils.isEmpty(servletRequest.getHeader("X-Forwarded-Host")))
			{
				activationUriBase = ServletUriComponentsBuilder.fromCurrentRequest().replacePath(URI + "/activate")
						.build().toUriString();
			}
			else
			{
				activationUriBase = servletRequest.getScheme() + "://" + servletRequest.getHeader("X-Forwarded-Host")
						+ URI + "/activate";
			}
			URI activationUri = java.net.URI.create(activationUriBase + '/' + activationCode);

			SimpleMailMessage mailMessage = new SimpleMailMessage();
			mailMessage.setTo(registrationRequest.getEmail());
			mailMessage.setSubject("Registration Autobetes");
			mailMessage.setText("To activate your account, please visit " + activationUri);
			System.out.println(">> Message: " + mailMessage.getText());
			mailSender.send(mailMessage);
		}
		catch (Exception e)
		{
			Object typedId = dataService.getRepositoryByEntityName(MolgenisUser.ENTITY_NAME).getEntityMetaData()
					.getIdAttribute().getDataType().convert(mu.getIdValue());

			dataService.delete(MolgenisUser.ENTITY_NAME, typedId);

			System.err.println(">> ERRROR >> " + e);
			return response(false,
					"Registration failed. Sending email with activation link failed. Please contact the developers if "
							+ registrationRequest.getEmail() + " is really your email address.");
		}

		return response(
				true,
				"Registration successful! We have sent you an email with a link to activate your account. NB The email may have ended up in your spam folder.");
	}

	private Map<String, Object> response(boolean success, String msg)
	{
		Map<String, Object> result = new HashMap<String, Object>();

		result.put("success", success);
		result.put("message", msg);

		return result;
	}
}
