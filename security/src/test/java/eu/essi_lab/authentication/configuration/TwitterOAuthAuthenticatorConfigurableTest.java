//package eu.essi_lab.authentication.configuration;
//
//import static org.junit.Assert.assertEquals;
//
//import org.junit.Assert;
//import org.junit.Test;
//
//import eu.essi_lab.authentication.OAuthAuthenticator;
//import eu.essi_lab.authentication.TwitterOAuthAuthenticator;
//import eu.essi_lab.cfga.Configurable;
//import eu.essi_lab.cfga.gs.setting.oauth.OAuthSetting;
//import eu.essi_lab.model.exceptions.GSException;
//
///**
// * @author Fabrizio
// */
//public class TwitterOAuthAuthenticatorConfigurableTest {
//
//    @Test
//    public void createConfigurableTest() throws Exception {
//
//	TwitterOAuthAuthenticatorConfigurable conf = new TwitterOAuthAuthenticatorConfigurable();
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
//	TwitterOAuthAuthenticatorConfigurable conf = new TwitterOAuthAuthenticatorConfigurable();
//
//	assertEquals("twitter", conf.getSetting().getSelectedProviderName());
//
//	OAuthAuthenticator authenticator = conf.getAuthenticator();
//
//	assertEquals(TwitterOAuthAuthenticator.class, authenticator.getClass());
//    }
//}