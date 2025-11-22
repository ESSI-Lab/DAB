package eu.essi_lab.authentication;

import static org.junit.Assert.assertEquals;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.cfga.gs.setting.oauth.OAuthSetting;
import eu.essi_lab.cfga.gs.setting.oauth.OAuthSetting.OAuthProvider;
import eu.essi_lab.model.exceptions.GSException;

/**
 * @author Fabrizio
 */
public class OAuthAuthenticatorFactoryTest {

    @Test
    public void keycloakTest() throws GSException {

	OAuthSetting setting = new OAuthSetting();
	setting.selectProvider(OAuthProvider.KEYCLOAK);

	setting.setClientId("clientId");
	setting.setClientSecret("clientSecret");

	OAuth2Authenticator oAuthAuthenticator = OAuth2AuthenticatorFactory.get(setting);

	String clientId = oAuthAuthenticator.getClientId();
	String clientSecret = oAuthAuthenticator.getClientSecret();

	Assert.assertEquals(setting.getClientId().get(), clientId);
	Assert.assertEquals(setting.getClientSecret().get(), clientSecret);

	assertEquals(KeycloackOAuthAuthenticator.class, oAuthAuthenticator.getClass());
    }

    @Test
    public void googleTest() throws GSException {

	OAuthSetting setting = new OAuthSetting();
	setting.selectProvider(OAuthProvider.GOOGLE);

	setting.setClientId("clientId");
	setting.setClientSecret("clientSecret");

	OAuth2Authenticator oAuthAuthenticator = OAuth2AuthenticatorFactory.get(setting);

	String clientId = oAuthAuthenticator.getClientId();
	String clientSecret = oAuthAuthenticator.getClientSecret();

	Assert.assertEquals(setting.getClientId().get(), clientId);
	Assert.assertEquals(setting.getClientSecret().get(), clientSecret);

	assertEquals(GoogleOAuth2Authenticator.class, oAuthAuthenticator.getClass());
    }

}