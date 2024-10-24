//package eu.essi_lab.configuration;
//
//import org.junit.Assert;
//import org.junit.Test;
//import org.mockito.Mockito;
//
//import eu.essi_lab.model.configuration.IGSConfigurable;
//import eu.essi_lab.model.exceptions.GSException;
//
///**
// * @author ilsanto
// */
//public class ConfigurableKeyTest {
//
//    @Test
//    public void testConstructor() {
//
//	IGSConfigurable configurable1 = Mockito.mock(IGSConfigurable.class);
//
//	Mockito.when(configurable1.getKey()).thenReturn("configurable1");
//
//	IGSConfigurable configurable2 = Mockito.mock(IGSConfigurable.class);
//
//	Mockito.when(configurable2.getKey()).thenReturn("configurable2");
//
//	ConfigurableKey key = new ConfigurableKey(
//		"root" + ConfigurableKey.SEPARATOR + "configurable1" + ConfigurableKey.SEPARATOR + "configurable2");
//
//	Assert.assertFalse(key.isRoot());
//
//	key.oneLevelDown();
//
//	Assert.assertFalse(key.match(configurable1));
//
//	key.oneLevelDown();
//
//	Assert.assertTrue(key.match(configurable2));
//
//    }
//
//    @Test
//    public void testWithResetBefore() throws GSException {
//
//	IGSConfigurable configurable1 = Mockito.mock(IGSConfigurable.class);
//
//	Mockito.when(configurable1.getKey()).thenReturn("configurable1");
//
//	IGSConfigurable configurable2 = Mockito.mock(IGSConfigurable.class);
//
//	Mockito.when(configurable2.getKey()).thenReturn("configurable2");
//
//	ConfigurableKey key = new ConfigurableKey(
//		"root" + ConfigurableKey.SEPARATOR + "configurable1" + ConfigurableKey.SEPARATOR + "configurable2");
//
//	Assert.assertFalse(key.isRoot());
//
//	key.oneLevelDown();
//	key.oneLevelUp();
//
//	Assert.assertFalse(key.match(configurable1));
//
//	key.oneLevelDown();
//	key.oneLevelDown();
//
//	Assert.assertTrue(key.match(configurable2));
//
//    }
//
//    @Test
//    public void testAddOneLevel() {
//	ConfigurableKey key = new ConfigurableKey("root" + ConfigurableKey.SEPARATOR + "configurable1");
//
//	Assert.assertTrue(key.oneLevelDown());
//	Assert.assertFalse(key.oneLevelDown());
//
//	key = new ConfigurableKey("root" + ConfigurableKey.SEPARATOR + "configurable1" + ConfigurableKey.SEPARATOR + "configurable2");
//
//	Assert.assertTrue(key.oneLevelDown());
//	Assert.assertTrue(key.oneLevelDown());
//	Assert.assertFalse(key.oneLevelDown());
//
//    }
//
//    @Test
//    public void testMatchRoot() {
//
//	ConfigurableKey key = new ConfigurableKey("root");
//
//	Assert.assertTrue(key.isRoot());
//
//	key = new ConfigurableKey("root" + ConfigurableKey.SEPARATOR + "configurable");
//
//	Assert.assertFalse(key.isRoot());
//
//    }
//
//}