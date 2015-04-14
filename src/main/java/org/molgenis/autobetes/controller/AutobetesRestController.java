package org.molgenis.autobetes.controller;

import static org.molgenis.autobetes.controller.AutobetesRestController.BASE_URI;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.molgenis.auth.MolgenisUser;
import org.molgenis.data.DataService;
import org.molgenis.data.meta.WritableMetaDataService;
import org.molgenis.data.rest.LoginRequest;
import org.molgenis.data.rest.LoginResponse;
import org.molgenis.data.rest.RestController;
import org.molgenis.data.rsql.MolgenisRSQL;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.security.core.MolgenisPermissionService;
import org.molgenis.security.runas.SystemSecurityToken;
import org.molgenis.security.token.TokenService;
import org.molgenis.util.ResourceFingerprintRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(BASE_URI)
public class AutobetesRestController extends RestController {

	public static final String BASE_URI = "/api/v2";
	
	private AuthenticationManager authenticationManager;
	private TokenService tokenService;
	private DataService dataService;

	@Autowired
	public AutobetesRestController(DataService dataService,
			WritableMetaDataService metaDataService, TokenService tokenService,
			AuthenticationManager authenticationManager,
			MolgenisPermissionService molgenisPermissionService,
			MolgenisRSQL molgenisRSQL,
			ResourceFingerprintRegistry resourceFingerprintRegistry) {
		super(dataService, metaDataService, tokenService, authenticationManager,
				molgenisPermissionService, molgenisRSQL, resourceFingerprintRegistry);
	
		this.authenticationManager = authenticationManager;
		this.tokenService = tokenService;
		this.dataService = dataService;
	}

	/**
	 * Login to the api.
	 * 
	 * Returns a json object with a token on correct login else throws an AuthenticationException. Clients can use this
	 * token when calling the api.
	 * 
	 * Example:
	 * 
	 * Request: {username:admin,password:xxx}
	 * 
	 * Response: {token: b4fd94dc-eae6-4d9a-a1b7-dd4525f2f75d}
	 * 
	 * @param login
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/login", method = POST, produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	public LoginResponse login(@Valid @RequestBody LoginRequest login, HttpServletRequest request)
	{
		if (login == null)
		{
			throw new HttpMessageNotReadableException("Missing login");
		}

		UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(login.getUsername(),
				login.getPassword());
		authToken.setDetails(new WebAuthenticationDetails(request));

		// Authenticate the login
		Authentication authentication = authenticationManager.authenticate(authToken);
		if (!authentication.isAuthenticated())
		{
			throw new BadCredentialsException("Unknown username or password");
		}

		// User authenticated, log the user in
		SecurityContextHolder.getContext().setAuthentication(authentication);

		// Generate a new token for the user
		String token = tokenService.generateAndStoreToken(authentication.getName(), "Rest api login");

		// Remember the original context
		SecurityContext origCtx = SecurityContextHolder.getContext();
		MolgenisUser user;
		try
		{
			// Set a SystemSecurityToken
			SecurityContextHolder.setContext(SecurityContextHolder.createEmptyContext());
			SecurityContextHolder.getContext().setAuthentication(new SystemSecurityToken());
			
			user = dataService.findOne(MolgenisUser.ENTITY_NAME,
					new QueryImpl().eq(MolgenisUser.USERNAME, authentication.getName()), MolgenisUser.class);

		}
		finally
		{
			// Set the original context back when method is finished
			SecurityContextHolder.setContext(origCtx);
		}		

		return new LoginResponse(token, user.getUsername(), user.getFirstName(), user.getLastName());
	}
}
