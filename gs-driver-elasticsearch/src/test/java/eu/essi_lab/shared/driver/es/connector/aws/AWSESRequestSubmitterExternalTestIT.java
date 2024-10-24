package eu.essi_lab.shared.driver.es.connector.aws;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.model.StorageInfo;
import eu.essi_lab.model.exceptions.GSException;

/**
 * @author ilsanto
 */
public class AWSESRequestSubmitterExternalTestIT {

    /**
     * In all integration test launches, the env vars below are used to allow docker image download from essi registry
     * (GIProjectIT iam
     * user). They are the same which can be used to access es in our aws account
     */
    private String awsClient = System.getProperty("docker.username");
    private String awsSecret = System.getProperty("docker.password");
    private static final String ES_DABLOD_DOMAIM = "https://search-esdablog-no37oojoxwuimt6nd7m6aao4v4.us-east-1.es.amazonaws.com/_cat/health?v";

    @Test
    public void test() throws IOException {

	HttpGet get = new HttpGet(ES_DABLOD_DOMAIM);

	AWSESRequestSubmitter submitter = new AWSESRequestSubmitter();

	submitter.setPwd(awsSecret);
	submitter.setUser(awsClient);
	submitter.setRegion("us-east-1");

	HttpResponse response = submitter.submit(get);

	System.out.println(IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8));

	assertEquals(0, response.getStatusLine().getStatusCode() - 200);

    }

    @Test
    public void testConnection() {

	AWSESConnector connector = new AWSESConnector();

	StorageInfo storageURI = new StorageInfo();

	storageURI.setUri("https://search-esdablog-no37oojoxwuimt6nd7m6aao4v4.us-east-1.es.amazonaws.com");

	storageURI.setPassword(awsSecret);
	storageURI.setUser(awsClient);

	connector.setEsStorageUri(storageURI);

	Assert.assertTrue(connector.testConnection());

    }

    @Test
    public void testInitialize() throws IOException {

	AWSESConnector connector = new AWSESConnector();

	StorageInfo storageURI = new StorageInfo();

	storageURI.setUri("https://search-esdablog-no37oojoxwuimt6nd7m6aao4v4.us-east-1.es.amazonaws.com");

	storageURI.setPassword(awsSecret);
	storageURI.setUser(awsClient);

	storageURI.setName("ittest");

	connector.setEsStorageUri(storageURI);

	try {

	    connector.initializePersistentStorage();

	    HttpDelete delete = new HttpDelete(
		    "https://search-esdablog-no37oojoxwuimt6nd7m6aao4v4.us-east-1.es.amazonaws.com/gsserviceinitialized"
			    + storageURI.getName().toLowerCase());

	    AWSESRequestSubmitter submitter = new AWSESRequestSubmitter();

	    submitter.setPwd(awsSecret);
	    submitter.setUser(awsClient);
	    submitter.setRegion("us-east-1");

	    HttpResponse response = submitter.submit(delete);

	    System.out.println(IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8));

	    assertEquals(0, response.getStatusLine().getStatusCode() - 200);

	} catch (GSException e) {

	    e.log();
	    Assert.fail("Can't init " + connector.getESUrl());

	}

    }
}