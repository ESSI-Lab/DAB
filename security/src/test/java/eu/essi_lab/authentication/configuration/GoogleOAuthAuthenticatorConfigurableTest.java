//package eu.essi_lab.authentication.configuration;
//
//import static org.junit.Assert.assertEquals;
//
//import org.junit.Assert;
//import org.junit.Test;
//
//import eu.essi_lab.authentication.GoogleOAuth2Authenticator;
//import eu.essi_lab.authentication.OAuthAuthenticator;
//import eu.essi_lab.cfga.Configurable;
//import eu.essi_lab.cfga.gs.setting.oauth.OAuthSetting;
//import eu.essi_lab.model.exceptions.GSException;
//
///**
// * @author Fabrizio
// */
//public class GoogleOAuthAuthenticatorConfigurableTest {
//
//    @Test
//    public void createConfigurableTest() throws Exception {
//
//	GoogleOAuthAuthenticatorConfigurable conf = new GoogleOAuthAuthenticatorConfigurable();
//
//	OAuthSetting setting = conf.getSetting();
//
//	@SuppressWarnings("rawtypes")
//	Configurable configurable = setting.createConfigurable();
//
//	Assert.assertEquals(configurable.getClass(), conf.getClass());
//    }
//
//    @Test
//    public void createAuthenticatorTest() throws GSException {
//
//	GoogleOAuthAuthenticatorConfigurable conf = new GoogleOAuthAuthenticatorConfigurable();
//
//	assertEquals("google", conf.getSetting().getSelectedProviderName());
//
//	OAuthAuthenticator authenticator = conf.getAuthenticator();
//
//	assertEquals(GoogleOAuth2Authenticator.class, authenticator.getClass());
//    }
//
//    @Test
//    public void authenticatorInitTest() throws GSException {
//
//	GoogleOAuthAuthenticatorConfigurable conf = new GoogleOAuthAuthenticatorConfigurable();
//
//	conf.getSetting().setClientId("clientId");
//	conf.getSetting().setClientSecret("clientSecret");
//
//	OAuthAuthenticator authenticator = conf.getAuthenticator();
//
//	String clientId = authenticator.getClientId();
//	String clientSecret = authenticator.getClientSecret();
//
//	assertEquals(clientId, conf.getSetting().getClientId());
//	assertEquals(clientSecret, conf.getSetting().getClientSecret());
//    }
//}