//package eu.essi_lab.authentication.configuration;
//
//import static org.junit.Assert.assertEquals;
//
//import java.util.Iterator;
//
//import org.junit.Rule;
//import org.junit.Test;
//import org.junit.rules.ExpectedException;
//import org.mockito.Mockito;
//
//import eu.essi_lab.model.configuration.option.GSConfOptionBoolean;
//import eu.essi_lab.model.exceptions.GSException;
//
///**
// * @author ilsanto
// */
//public class OAuthConfigurableTest {
//
//    @Rule
//    public ExpectedException expected = ExpectedException.none();
//
//    @Test
//    public void setUnknownAuthenticator() throws GSException {
//
//	expected.expect(GSException.class);
//	String unknownKey = "unknownKey";
//
//	OAuthConfigurable oauthconf = new OAuthConfigurable();
//
//	GSConfOptionBoolean opt = Mockito.mock(GSConfOptionBoolean.class);
//
//	Mockito.doReturn(true).when(opt).getValue();
//
//	Mockito.doReturn(unknownKey).when(opt).getKey();
//
//	oauthconf.onOptionSet(opt);
//
//    }
//
//    @Test
//    public void testEnableDisableOAuth() throws GSException {
//
//	String testkey = "testkey";
//
//	OAuthConfigurable c = new OAuthConfigurable();
//
//	OAuthConfigurable oauthconf = Mockito.spy(c);
//
//	assertEquals(0, oauthconf.getConfigurableComponents().size());
//
//	Iterator<IOAuthAuthenticatorConfigurable> it = Mockito.mock(Iterator.class);
//
//	Mockito.doReturn(true).when(it).hasNext();
//
//	IOAuthAuthenticatorConfigurable authenticator = Mockito.mock(IOAuthAuthenticatorConfigurable.class);
//
//	Mockito.doReturn(testkey).when(authenticator).getKey();
//
//	Mockito.doReturn(authenticator).when(it).next();
//
//	Mockito.doReturn(it).when(oauthconf).getLoaderIterator();
//
//	GSConfOptionBoolean opt = Mockito.mock(GSConfOptionBoolean.class);
//
//	Mockito.doReturn(true).when(opt).getValue();
//
//	Mockito.doReturn(testkey).when(opt).getKey();
//
//	oauthconf.onOptionSet(opt);
//
//	assertEquals(1, oauthconf.getConfigurableComponents().size());
//
//	GSConfOptionBoolean opt2 = Mockito.mock(GSConfOptionBoolean.class);
//
//	Mockito.doReturn(false).when(opt2).getValue();
//
//	Mockito.doReturn(testkey).when(opt2).getKey();
//
//	oauthconf.onOptionSet(opt2);
//
//	assertEquals(0, oauthconf.getConfigurableComponents().size());
//    }
//
//}