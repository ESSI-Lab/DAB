package eu.essi_lab.shared.driver.es.connector.aws;

import static cloud.localstack.TestUtils.getCredentialsProvider;

import java.io.IOException;
import java.util.function.Supplier;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.slf4j.Logger;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.elasticsearch.AWSElasticsearch;
import com.amazonaws.services.elasticsearch.AWSElasticsearchClientBuilder;
import com.amazonaws.services.elasticsearch.model.CreateElasticsearchDomainRequest;
import com.amazonaws.services.elasticsearch.model.ListDomainNamesRequest;
import com.amazonaws.services.elasticsearch.model.ListDomainNamesResult;

import cloud.localstack.docker.LocalstackDocker;
import cloud.localstack.docker.LocalstackDockerTestRunner;
import cloud.localstack.docker.annotation.LocalstackDockerProperties;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.StorageInfo;

/**
 * @author ilsanto
 */
@Ignore
@RunWith(LocalstackDockerTestRunner.class)
@LocalstackDockerProperties(randomizePorts = true, services = { "es" })
public class AWSESConnectorExternalTestIT {

    private AWSElasticsearch client;
    private static final String TEST_DOMAIN_NAME = "test";

    private Logger logger = GSLoggerFactory.getLogger(AWSESConnectorExternalTestIT.class);

    @Before
    public void before() throws IOException {

	client = AWSElasticsearchClientBuilder.standard().
		withEndpointConfiguration(createEndpointConfiguration(LocalstackDocker.INSTANCE::getEndpointElasticsearchService)).
		withCredentials(getCredentialsProvider()).build();

	ListDomainNamesRequest listRequest = new ListDomainNamesRequest();
	ListDomainNamesResult listResponse = client.listDomainNames(listRequest);

	boolean exists = !listResponse.getDomainNames().stream().noneMatch(
		domainInfo -> domainInfo.getDomainName().equals(TEST_DOMAIN_NAME));

	logger.debug("{} {}", TEST_DOMAIN_NAME, exists ? "already exists" : "does not exist");

	if (!exists) {
	    CreateElasticsearchDomainRequest createDomainRequest = new CreateElasticsearchDomainRequest();
	    createDomainRequest.setDomainName(TEST_DOMAIN_NAME);
	    client.createElasticsearchDomain(createDomainRequest);
	}

    }

    @Test
    public void testConnection() {

	AWSESConnector connector = Mockito.spy(new AWSESConnector());

	Mockito.doReturn(true).when(connector).awsElasticSearch();

	Mockito.doReturn(client).when(connector).getAWSClient();

	StorageInfo storage = new StorageInfo();

	storage.setUri("https://search-test-nocduimt6nd7m6aao4v4.us-east-1.es.amazonaws.com");

	connector.setEsStorageUri(storage);

	Assert.assertTrue(connector.testConnection());

    }

    @Test
    public void testConnectionNoDomain() {

	AWSESConnector connector = Mockito.spy(new AWSESConnector());

	Mockito.doReturn(true).when(connector).awsElasticSearch();

	Mockito.doReturn(client).when(connector).getAWSClient();

	StorageInfo storage = new StorageInfo();

	storage.setUri("https://search-test2-nocduimt6nd7m6aao4v4.us-east-1.es.amazonaws.com");

	connector.setEsStorageUri(storage);

	Assert.assertFalse(connector.testConnection());

    }

    private static AwsClientBuilder.EndpointConfiguration createEndpointConfiguration(Supplier<String> supplier) {

	return new AwsClientBuilder.EndpointConfiguration(supplier.get(), "us-east-1");

    }

}