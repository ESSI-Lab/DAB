/**
 * 
 */
package eu.essi_lab.api.database.test;

import java.net.URISyntaxException;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.api.database.Database.DatabaseImpl;
import eu.essi_lab.api.database.Database.OpenSearchServiceType;
import eu.essi_lab.api.database.cfg.DatabaseSourceUrl;
import eu.essi_lab.model.StorageInfo;

/**
 * @author Fabrizio
 */
public class StorageInfoTest {

    @Test
    public void isStartupUriTest() throws URISyntaxException {

	{
	    String uri = "xdbc://user:password@hostname:8000,8004/dbName/folder/";
	    Assert.assertTrue(DatabaseSourceUrl.check(uri));
	}

	{
	    String uri = "osm://awsaccesskey:awssecretkey@productionhost/prod/prodConfig";
	    Assert.assertTrue(DatabaseSourceUrl.check(uri));
	}

	{
	    String uri = "oss://awsaccesskey:awssecretkey@productionhost/prod/prodConfig";
	    Assert.assertTrue(DatabaseSourceUrl.check(uri));
	}

	{
	    String uri = "osl://awsaccesskey:awssecretkey@localhost/test/testConfig";
	    Assert.assertTrue(DatabaseSourceUrl.check(uri));
	}

	{
	    String uri = "http://awsaccesskey:awssecretkey@productionhost/prod/prodConfig";
	    Assert.assertFalse(DatabaseSourceUrl.check(uri));
	}

	{
	    String uri = "https://awsaccesskey:awssecretkey@productionhost/prod/prodConfig";
	    Assert.assertFalse(DatabaseSourceUrl.check(uri));
	}

	{
	    String uri = "productionhost/prod/prodConfig";
	    Assert.assertFalse(DatabaseSourceUrl.check(uri));
	}
    }

    @Test
    public void implTest() throws URISyntaxException {

	{
	    String uri = "xdbc://user:password@hostname:8000,8004/dbName/folder/";
	    Assert.assertEquals(DatabaseImpl.MARK_LOGIC, DatabaseSourceUrl.detectImpl(uri));
	}

	{
	    String uri = "osm://awsaccesskey:awssecretkey@productionhost/prod/prodConfig";
	    Assert.assertEquals(DatabaseImpl.OPENSEARCH, DatabaseSourceUrl.detectImpl(uri));
	}

	{
	    String uri = "oss://awsaccesskey:awssecretkey@productionhost/prod/prodConfig";
	    Assert.assertEquals(DatabaseImpl.OPENSEARCH, DatabaseSourceUrl.detectImpl(uri));
	}

	{
	    String uri = "osl://awsaccesskey:awssecretkey@localhost:9200/test/testConfig";
	    Assert.assertEquals(DatabaseImpl.OPENSEARCH, DatabaseSourceUrl.detectImpl(uri));
	}

	{
	    String uri = "os://awsaccesskey:awssecretkey@productionhost/prod/prodConfig";
	    Assert.assertNull(DatabaseSourceUrl.detectImpl(uri));
	}

	{
	    String uri = "http://awsaccesskey:awssecretkey@productionhost/prod/prodConfig";
	    Assert.assertNull(DatabaseSourceUrl.detectImpl(uri));
	}

	{
	    String uri = "user:password@hostname:8000,8004/dbName/folder/";
	    Assert.assertNull(DatabaseSourceUrl.detectImpl(uri));
	}
    }

    @Test
    public void markLogicTest() throws URISyntaxException {

	String uri = "xdbc://user:password@hostname:8000,8004/dbName/folder/";

	StorageInfo info = DatabaseSourceUrl.build(uri);

	Assert.assertEquals("user", info.getUser());
	Assert.assertEquals("password", info.getPassword());
	Assert.assertEquals("xdbc://hostname:8000,8004", info.getUri());
	Assert.assertEquals("folder", info.getIdentifier());
	Assert.assertEquals("dbName", info.getName());
	Assert.assertTrue(info.getType().isEmpty());
    }

    @Test
    public void openSearchManagedTest() throws URISyntaxException {

	String uri = "osm://awsaccesskey:awssecretkey@productionhost/prod/prodConfig";

	StorageInfo info = DatabaseSourceUrl.build(uri);

	Assert.assertEquals("awsaccesskey", info.getUser());
	Assert.assertEquals("awssecretkey", info.getPassword());

	Assert.assertEquals("https://productionhost", info.getUri());

	Assert.assertEquals("prod", info.getIdentifier());

	Assert.assertEquals("prodConfig", info.getName());

	Assert.assertEquals(OpenSearchServiceType.OPEN_SEARCH_MANAGED.getProtocol(), info.getType().get());
    }

    @Test
    public void openSearchServerlessTest() throws URISyntaxException {

	String uri = "oss://awsaccesskey:awssecretkey@productionhost/preprod/preProdConfig";

	StorageInfo info = DatabaseSourceUrl.build(uri);

	Assert.assertEquals("awsaccesskey", info.getUser());
	Assert.assertEquals("awssecretkey", info.getPassword());

	Assert.assertEquals("https://productionhost", info.getUri());

	Assert.assertEquals("preprod", info.getIdentifier());

	Assert.assertEquals("preProdConfig", info.getName());

	Assert.assertEquals(OpenSearchServiceType.OPEN_SEARCH_SERVERLESS.getProtocol(), info.getType().get());
    }

    @Test
    public void openSearchLocalTest() throws URISyntaxException {

	String uri = "osl://awsaccesskey:awssecretkey@localhost:9200/test/testConfig";

	StorageInfo info = DatabaseSourceUrl.build(uri);

	Assert.assertEquals("awsaccesskey", info.getUser());
	Assert.assertEquals("awssecretkey", info.getPassword());

	Assert.assertEquals("http://localhost:9200", info.getUri());

	Assert.assertEquals("test", info.getIdentifier());

	Assert.assertEquals("testConfig", info.getName());

	Assert.assertEquals(OpenSearchServiceType.OPEN_SEARCH_LOCAL.getProtocol(), info.getType().get());
    }
}
