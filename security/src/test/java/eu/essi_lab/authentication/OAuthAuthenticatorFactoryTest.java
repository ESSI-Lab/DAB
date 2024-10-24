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
    public void faceBookTest() throws GSException {

	OAuthSetting setting = new OAuthSetting();
	setting.selectProvider(OAuthProvider.FACEBOOK);

	setting.setClientId("clientId");
	setting.setClientSecret("clientSecret");

	OAuthAuthenticator oAuthAuthenticator = OAuthAuthenticatorFactory.getOAuthAuthenticator(setting);

	String clientId = oAuthAuthenticator.getClientId();
	String clientSecret = oAuthAuthenticator.getClientSecret();

	Assert.assertEquals(setting.getClientId().get(), clientId);
	Assert.assertEquals(setting.getClientSecret().get(), clientSecret);

	assertEquals(FacebookOAuth2Authenticator.class, oAuthAuthenticator.getClass());
    }

    @Test
    public void twitterBookTest() throws GSException {

	OAuthSetting setting = new OAuthSetting();
	setting.selectProvider(OAuthProvider.TWITTER);

	setting.setClientId("clientId");
	setting.setClientSecret("clientSecret");

	OAuthAuthenticator oAuthAuthenticator = OAuthAuthenticatorFactory.getOAuthAuthenticator(setting);

	String clientId = oAuthAuthenticator.getClientId();
	String clientSecret = oAuthAuthenticator.getClientSecret();

	Assert.assertEquals(setting.getClientId().get(), clientId);
	Assert.assertEquals(setting.getClientSecret().get(), clientSecret);

	assertEquals(TwitterOAuthAuthenticator.class, oAuthAuthenticator.getClass());
    }

    @Test
    public void googleBookTest() throws GSException {

	OAuthSetting setting = new OAuthSetting();
	setting.selectProvider(OAuthProvider.GOOGLE);

	setting.setClientId("clientId");
	setting.setClientSecret("clientSecret");

	OAuthAuthenticator oAuthAuthenticator = OAuthAuthenticatorFactory.getOAuthAuthenticator(setting);

	String clientId = oAuthAuthenticator.getClientId();
	String clientSecret = oAuthAuthenticator.getClientSecret();

	Assert.assertEquals(setting.getClientId().get(), clientId);
	Assert.assertEquals(setting.getClientSecret().get(), clientSecret);

	assertEquals(GoogleOAuth2Authenticator.class, oAuthAuthenticator.getClass());
    }

}