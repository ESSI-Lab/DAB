//package eu.essi_lab.api.database.marklogic;
//
//import org.junit.Assert;
//import org.junit.Before;
//import org.junit.Test;
//
//import eu.essi_lab.model.StorageUri;
//
//public class MarkLogicConfigurationStorageTest {
//
//    private MarkLogicConfigurationStorage storage;
//
//    @Before
//    public void init() {
//	this.storage = new MarkLogicConfigurationStorage();
//    }
//
//    @Test
//    public void testSupport() {
//	Assert.assertTrue(storage.supports(new StorageUri("xdbc://pippo")));
//
//	Assert.assertFalse(storage.supports(new StorageUri("http://pippo")));
//    }
//
//}
