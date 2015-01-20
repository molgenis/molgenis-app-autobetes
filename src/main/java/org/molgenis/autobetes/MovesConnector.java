package org.molgenis.autobetes;

import java.util.List;

import org.molgenis.autobetes.autobetes.MovesActivity;
import org.molgenis.autobetes.autobetes.MovesToken;
import org.molgenis.autobetes.autobetes.MovesUserProfile;
import org.molgenis.data.DataService;
import org.molgenis.data.support.MapEntity;
import org.molgenis.auth.MolgenisUser;

public interface MovesConnector
{
	/**
	 * This method updates the activities of a certain user.
	 * 
	 * @param dataService for accessing database
	 * @param user for which activities need to be managed
	 * @return boolean, true is success
	 */
	public boolean manageActivities(DataService dataService, MolgenisUser user);
	
	/**
	 * Checks if access token from moves is still valid
	 * @param accessToken
	 * @return boolean true if valid
	 */
	public boolean accessTokenIsValid(String accessToken);
	
	/**
	 * This method refreshes the access token.
	 * @param refreshToken
	 * @param user
	 * @param clientId
	 * @param clientSecret
	 * @return new MovesToken
	 */
	public MovesToken refreshToken(String refreshToken, MolgenisUser user);
	
	/**
	 * Once user authorizes this server to connect with moves an authorization code is retrieved. Together with the client id, secret id and token
	 * this method retrieves the MovesToken which allows accessing personal data. 
	 * @param user
	 * @param token
	 * @param authorizationcode
	 * @param clientId
	 * @param secretId
	 * @return
	 */
	public MovesToken exchangeAutorizationcodeForAccesstoken(MolgenisUser user, String token, String authorizationcode);
	/**
	 * Retrieves user profile from Moves
	 * @param movesToken
	 * @return MovesUserProfile see https://dev.moves-app.com/docs/api_profile 
	 */
	public MovesUserProfile getUserProfile(MovesToken movesToken);
	/**
	 * Retrieves all activities that occured in specified range of time
	 * @param movesToken
	 * @param from date in yyyyMMdd format
	 * @param to date in yyyyMMdd format
	 * @return list of activities
	 */
	public List<MovesActivity> getActivities(MovesToken movesToken, int from, int to);
	
}
