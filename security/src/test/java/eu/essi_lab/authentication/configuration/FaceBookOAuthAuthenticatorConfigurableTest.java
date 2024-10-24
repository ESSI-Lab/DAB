//package eu.essi_lab.authentication.configuration;
//
//import static org.junit.Assert.assertEquals;
//
//import org.junit.Assert;
//import org.junit.Test;
//
//import eu.essi_lab.authentication.FacebookOAuth2Authenticator;
//import eu.essi_lab.authentication.OAuthAuthenticator;
//import eu.essi_lab.cfga.Configurable;
//import eu.essi_lab.cfga.gs.setting.oauth.OAuthSetting;
//import eu.essi_lab.model.exceptions.GSException;
//
///**
// * @author Fabrizio
// */
//public class FaceBookOAuthAuthenticatorConfigurableTest {
//
//    @Test
//    public void createConfigurableTest() throws Exception {
//
//	FaceBookOAuthAuthenticatorConfigurable conf = new FaceBookOAuthAuthenticatorConfigurable();
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
//	FaceBookOAuthAuthenticatorConfigurable conf = new FaceBookOAuthAuthenticatorConfigurable();
//
//	assertEquals("facebook", conf.getSetting().getSelectedProviderName());
//
//	OAuthAuthenticator authenticator = conf.getAuthenticator();
//
//	assertEquals(FacebookOAuth2Authenticator.class, authenticator.getClass());
//    }
//}