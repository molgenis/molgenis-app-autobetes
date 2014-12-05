package org.molgenis.autobetes;

import org.molgenis.autobetes.autobetes.MovesToken;
import org.molgenis.data.Entity;
import org.molgenis.data.support.MapEntity;
import org.molgenis.omx.auth.MolgenisUser;

public interface MovesConnector
{

	public boolean accessTokenIsValid(String accessToken);
	
	public MapEntity refreshToken(String refreshToken, MolgenisUser user, String clientId, String clientSecret);
	
	public MapEntity exchangeAutorizationcodeForAccesstoken(MolgenisUser user, String token, String authorizationcode, String clientId, String secretId);
	
	public MapEntity getUserProfile(MovesToken movesToken);
	
}
