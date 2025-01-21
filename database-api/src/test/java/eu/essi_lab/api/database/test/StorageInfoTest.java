/**
 * 
 */
package eu.essi_lab.api.database.test;

import java.net.URISyntaxException;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.api.database.Database;
import eu.essi_lab.api.database.Database.DatabaseImpl;
import eu.essi_lab.api.database.Database.OpenSearchServiceType;
import eu.essi_lab.model.StorageInfo;

/**
 * @author Fabrizio
 */
public class StorageInfoTest {
    
    @Test
    public void isTest() throws URISyntaxException {

	{
	    String uri = "xdbc://user:password@hostname:8000,8004/dbName/folder/";
	    Assert.assertTrue(Database.isStartupUri(uri));
	}

	{
	    String uri = "osm://productionhost/prod/prodConfig";
	    Assert.assertTrue(Database.isStartupUri(uri));
	}

	{
	    String uri = "oss://productionhost/prod/prodConfig";
	    Assert.assertTrue(Database.isStartupUri(uri));
	}
	
	{
	    String uri = "http://productionhost/prod/prodConfig";
	    Assert.assertFalse(Database.isStartupUri(uri));
	}
	
	{
	    String uri = "https://productionhost/prod/prodConfig";
	    Assert.assertFalse(Database.isStartupUri(uri));
	}
	
	{
	    String uri = "productionhost/prod/prodConfig";
	    Assert.assertFalse(Database.isStartupUri(uri));
	}
    }

    @Test
    public void implTest() throws URISyntaxException {

	{
	    String uri = "xdbc://user:password@hostname:8000,8004/dbName/folder/";
	    Assert.assertEquals(DatabaseImpl.MARK_LOGIC, Database.getImpl(uri));
	}

	{
	    String uri = "osm://productionhost/prod/prodConfig";
	    Assert.assertEquals(DatabaseImpl.OPENSEARCH,  Database.getImpl(uri));
	}

	{
	    String uri = "oss://productionhost/prod/prodConfig";
	    Assert.assertEquals(DatabaseImpl.OPENSEARCH,  Database.getImpl(uri));
	}
	
	{
	    String uri = "os://productionhost/prod/prodConfig";
	    Assert.assertNull(Database.getImpl(uri));
	}
	
	{
	    String uri = "http://productionhost/prod/prodConfig";
	    Assert.assertNull(Database.getImpl(uri));
	}
	
	{
	    String uri = "user:password@hostname:8000,8004/dbName/folder/";
	    Assert.assertNull(Database.getImpl(uri));
	}
    }

    @Test
    public void markLogicTest() throws URISyntaxException {

	String uri = "xdbc://user:password@hostname:8000,8004/dbName/folder/";

	StorageInfo info = Database.getInfo(uri);

	Assert.assertEquals("user", info.getUser());
	Assert.assertEquals("password", info.getPassword());
	Assert.assertEquals("xdbc://hostname:8000,8004", info.getUri());
	Assert.assertEquals("folder", info.getIdentifier());
	Assert.assertEquals("dbName", info.getName());
	Assert.assertTrue(info.getType().isEmpty());
    }

    @Test
    public void openSearchManagedTest() throws URISyntaxException {

	String uri = "osm://productionhost/prod/prodConfig";

	StorageInfo info = Database.getInfo(uri);

	Assert.assertEquals("prodConfig", info.getUser());
	Assert.assertNull(info.getPassword());
	Assert.assertEquals("https://productionhost", info.getUri());
	Assert.assertEquals("prod", info.getIdentifier());
	Assert.assertEquals(Database.CONFIGURATION_FOLDER, info.getName());
	Assert.assertEquals(OpenSearchServiceType.OPEN_SEARCH_MANAGED.getProtocol(), info.getType().get());
    }

    @Test
    public void openSearchServerlessTest() throws URISyntaxException {

	String uri = "oss://productionhost/prod/prodConfig";

	StorageInfo info = Database.getInfo(uri);

	Assert.assertEquals("prodConfig", info.getUser());
	Assert.assertNull(info.getPassword());
	Assert.assertEquals("https://productionhost", info.getUri());
	Assert.assertEquals("prod", info.getIdentifier());
	Assert.assertEquals(Database.CONFIGURATION_FOLDER, info.getName());
	Assert.assertEquals(OpenSearchServiceType.OPEN_SEARCH_SERVERLESS.getProtocol(), info.getType().get());
    }
    
    @Test
    public void openSearchLocalTest() throws URISyntaxException {

	String uri = "osl://localhost/test/testConfig";

	StorageInfo info = Database.getInfo(uri);

	Assert.assertEquals("testConfig", info.getUser());
	Assert.assertNull(info.getPassword());
	Assert.assertEquals("http://localhost", info.getUri());
	Assert.assertEquals("test", info.getIdentifier());
	Assert.assertEquals(Database.CONFIGURATION_FOLDER, info.getName());
	Assert.assertEquals(OpenSearchServiceType.OPEN_SEARCH_LOCAL.getProtocol(), info.getType().get());
    }
}
