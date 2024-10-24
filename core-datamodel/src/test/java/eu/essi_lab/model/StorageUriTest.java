//package eu.essi_lab.model;
//
//import org.junit.Assert;
//import org.junit.Test;
//
///**
// * @author ilsanto
// */
//public class StorageUriTest {
//
//    @Test
//    public void parse1() {
//	String relativepath = "test.json";
//
//	StorageUri s = new StorageUri(relativepath);
//
//	Assert.assertEquals(relativepath, s.getUri());
//    }
//
//    @Test
//    public void parse2() {
//	String xdbcpath = "xdbc://user:pd@test.com:8004/storagename/folder/";
//
//	StorageUri s = new StorageUri(xdbcpath);
//
//	Assert.assertEquals("pd", s.getPassword());
//	Assert.assertEquals("user", s.getUser());
//	Assert.assertEquals("xdbc://test.com:8004", s.getUri());
//	Assert.assertEquals("storagename", s.getStorageName());
//	Assert.assertEquals("folder", s.getConfigFolder());
//    }
//}