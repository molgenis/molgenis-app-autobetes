package org.molgenis.autobetes.controller;

import static org.mockito.Mockito.mock;
import static org.hamcrest.CoreMatchers.*;
import static org.testng.Assert.*;

import java.util.List;

import org.molgenis.autobetes.MovesConnector;
import org.molgenis.autobetes.MovesConnectorImpl;
import org.molgenis.autobetes.autobetes.MovesActivity;
import org.molgenis.autobetes.autobetes.MovesToken;
import org.molgenis.autobetes.autobetes.MovesUserProfile;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.support.MapEntity;
import org.molgenis.framework.server.MolgenisSettings;
import org.molgenis.auth.MolgenisUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.context.web.WebAppConfiguration;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@WebAppConfiguration
@ContextConfiguration
public class MovesConnectorTest extends AbstractTestNGSpringContextTests{

	@Autowired
	private MolgenisSettings molgenisSettings;

	@Autowired
	private DataService dataService;


	private MolgenisUser user;
	private MovesToken movesEntity;
	private MovesConnector movesConnector = new MovesConnectorImpl();
	
	private String refresh_token = "kYtxAijz2ITEXN843r5bWkEelq8cBs9KKGHtGcjx43rfW3Y60Tjb9494NCU7ZeqJ";
	
	@BeforeMethod
	public void setUp()
	{
		user = new MolgenisUser();
		movesEntity = new MovesToken();
		movesEntity.set(MovesToken.ID, 1);
		movesEntity.set(MovesToken.ACCESSTOKEN, "al4M5w43HevoqV86paqB5rYM874zO6cLS9GRh0vcd_R6eDIu7RINH7f5Y5sxkt_t");
	}
	
	 /*
	public boolean accessTokenIsValid(String accessToken);
	
	public MapEntity refreshToken(MolgenisUser user, Entity movesEntity, String clientId, String clientSecret);
	
	public MapEntity exchangeAutorizationcodeForAccesstoken(MolgenisUser user, String token, String authorizationcode, String clientId, String secretId);

	 */
	/*
	@Test
	public void accessTokenIsValidTest(){
		//test with invalid token
		boolean isValid = movesConnector.accessTokenIsValid("invalidtoken");
		assertEquals(isValid, false);
		//test with valid token
		isValid = movesConnector.accessTokenIsValid("al4M5w43HevoqV86paqB5rYM874zO6cLS9GRh0vcd_R6eDIu7RINH7f5Y5sxkt_t");
		assertEquals(isValid, true);
		
	}
	
	@Test
	public void refreshTokenTest(){
		//test with invalid token
		MovesToken newEntity = movesConnector.refreshToken(refresh_token, user);
		System.out.println(newEntity.toString());
		//assertThat(newEntity, not(null));
	}
	
	@Test
	public void getUserProfileTest(){
		MovesUserProfile userProfile = movesConnector.getUserProfile(movesEntity);
		System.out.println(userProfile.toString());
	}
	
	@Test
	public void getActivitiesTest(){
		List<MovesActivity> isValid = movesConnector.getActivities(movesEntity, 20141119, 20141208);
		System.out.println(isValid.toString());
	}
	
	*/

	@Configuration
	public static class Config
	{
		@Bean
		public DataService dataService()
		{
			return mock(DataService.class);
		}

		@Bean
		public MolgenisSettings molgenisSettings()
		{
			return mock(MolgenisSettings.class);
		}

	}

	
}
