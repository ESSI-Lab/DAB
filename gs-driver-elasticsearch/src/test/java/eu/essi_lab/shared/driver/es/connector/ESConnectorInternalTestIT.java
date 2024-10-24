package eu.essi_lab.shared.driver.es.connector;

import static com.amazonaws.util.StringUtils.UTF8;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.model.StorageInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.shared.SharedContent.SharedContentType;
import eu.essi_lab.shared.driver.es.query.ESQueryMapper;
import eu.essi_lab.shared.messages.SharedContentQuery;

/**
 * @author ilsanto
 */
public class ESConnectorInternalTestIT {

    // docker run -p 9200:9200 -p 9600:9600 -e "discovery.type=single-node" opensearchproject/opensearch:1.3.5

    // public static final StorageUri ES_STORAGE = new StorageUri("http://localhost:9200");

    public static final StorageInfo ES_STORAGE = new StorageInfo(System.getProperty("es.host"));

    private String storageName = UUID.randomUUID().toString();

    @Before
    public void before() {

	ES_STORAGE.setUser(System.getProperty("es.user"));

	ES_STORAGE.setPassword(System.getProperty("es.password"));

	ES_STORAGE.setName(storageName);
    }

    @Test
    public void testConnection() {

	ESConnector connector = new ESConnector();

	connector.setEsStorageUri(ES_STORAGE);

	Assert.assertTrue(connector.testConnection());

    }

    @Test
    public void testInitialize() throws InterruptedException {

	ESConnector connector = Mockito.spy(new ESConnector());

	connector.setEsStorageUri(ES_STORAGE);

	try {
	    connector.initializePersistentStorage();
	} catch (GSException e) {
	    e.log();

	    Assert.fail("Failed with exception is initialize method call");
	}

	Thread.sleep(1000L);

	String index = "gsserviceinitialized" + ES_STORAGE.getName().toLowerCase();

	String url = ES_STORAGE.getUri() + "/" + index + "/initialize/_search?pretty";

	HttpGet get = new HttpGet(url);

	CredentialsProvider credentialsProvider = new BasicCredentialsProvider();

	Credentials credentials = new UsernamePasswordCredentials(ES_STORAGE.getUser(), ES_STORAGE.getPassword());
	credentialsProvider.setCredentials(AuthScope.ANY, credentials);

	HttpClient c = HttpClientBuilder.create().setDefaultCredentialsProvider(credentialsProvider).build();

	try {
	    HttpResponse r = c.execute(get);

	    int code = r.getStatusLine().getStatusCode();

	    GSLoggerFactory.getLogger(ESConnectorInternalTestIT.class).trace("Received code {} from {}", code, url);

	    assertEquals(0, 200 - code);

	    String theString = IOUtils.toString(r.getEntity().getContent(), StandardCharsets.UTF_8);

	    System.out.println(theString);

	    JSONObject json = new JSONObject(theString);

	    int total = json.getJSONObject("hits").getJSONObject("total").getInt("value");

	    System.out.println("total " + total);

	    assertEquals(0, 1 - total);

	} catch (IOException e) {

	    GSLoggerFactory.getLogger(ESConnectorInternalTestIT.class).warn("IOException executing {}", url, e);

	}

    }

    @Test
    public void testInitialize2() throws InterruptedException {

	ESConnector connector = Mockito.spy(new ESConnector());

	connector.setEsStorageUri(ES_STORAGE);

	try {
	    connector.initializePersistentStorage();
	} catch (GSException e) {
	    e.log();

	    Assert.fail("Failed with exception is initialize method call");
	}

	Thread.sleep(1000L);

	String index = "gsserviceinitialized" + ES_STORAGE.getName().toLowerCase();

	String url = ES_STORAGE.getUri() + "/" + index + "/initialize/_search?pretty";

	HttpGet get = new HttpGet(url);

	CredentialsProvider credentialsProvider = new BasicCredentialsProvider();

	Credentials credentials = new UsernamePasswordCredentials(ES_STORAGE.getUser(), ES_STORAGE.getPassword());
	credentialsProvider.setCredentials(AuthScope.ANY, credentials);

	HttpClient c = HttpClientBuilder.create().setDefaultCredentialsProvider(credentialsProvider).build();

	try {
	    HttpResponse r = c.execute(get);

	    int code = r.getStatusLine().getStatusCode();

	    GSLoggerFactory.getLogger(ESConnectorInternalTestIT.class).trace("Received code {} from {}", code, url);

	    assertEquals(0, 200 - code);

	    String theString = IOUtils.toString(r.getEntity().getContent(), StandardCharsets.UTF_8);

	    System.out.println(theString);

	    JSONObject json = new JSONObject(theString);

	    int total = json.getJSONObject("hits").getJSONObject("total").getInt("value");

	    System.out.println("total " + total);

	    assertEquals(0, 1 - total);

	} catch (IOException e) {

	    GSLoggerFactory.getLogger(ESConnectorInternalTestIT.class).warn("IOException executing {}", url, e);

	}

    }

    @Test
    public void testIO() throws GSException, InterruptedException, IOException {

	ESConnector connector = new ESConnector();

	connector.setEsStorageUri(ES_STORAGE);

	String testid = "testid";

	/**
	 * the double write below lets me test that if I insert two documents with the same id, the count is still one
	 * and
	 * the returned document is the last inserted (update works)
	 */
	connector.write(testid, SharedContentType.JSON_TYPE, new ByteArrayInputStream("{\"field\":\"value\"}".getBytes()));
	Thread.sleep(1000L);

	connector.write(testid, SharedContentType.JSON_TYPE, new ByteArrayInputStream("{\"field\":\"value2\"}".getBytes()));
	Thread.sleep(1000L);

	String url = ES_STORAGE.getUri() + "/" + storageName.toLowerCase() + "/" + SharedContentType.JSON_TYPE.name().toLowerCase()
		+ "estype/_search?pretty";

	HttpGet get = new HttpGet(url);

	CredentialsProvider credentialsProvider = new BasicCredentialsProvider();

	Credentials credentials = new UsernamePasswordCredentials(ES_STORAGE.getUser(), ES_STORAGE.getPassword());
	credentialsProvider.setCredentials(AuthScope.ANY, credentials);

	HttpClient c = HttpClientBuilder.create().setDefaultCredentialsProvider(credentialsProvider).build();
	HttpResponse r = null;

	r = c.execute(get);

	int code = r.getStatusLine().getStatusCode();

	GSLoggerFactory.getLogger(ESConnectorInternalTestIT.class).trace("Received code {} from {}", code, url);

	assertEquals(0, 200 - code);

	String theString = IOUtils.toString(r.getEntity().getContent(), StandardCharsets.UTF_8);

	JSONObject json = new JSONObject(theString);
	System.out.println(json.toString(3));

	int total = json.getJSONObject("hits").getJSONObject("total").getInt("value");

	System.out.println("total " + total);

	assertEquals(0, 1 - total);

	String value = json.getJSONObject("hits").getJSONArray("hits").getJSONObject(0).getJSONObject("_source").getString("field");

	assertEquals("value2", value);

	Long count = connector.count(SharedContentType.JSON_TYPE);

	assertEquals(0L, 1L - count);

	Optional<InputStream> sharedContent = connector.get(testid, SharedContentType.JSON_TYPE);

	assertTrue(sharedContent.isPresent());

	String readString = IOUtils.toString(sharedContent.get(), UTF8);

	GSLoggerFactory.getLogger(ESConnectorInternalTestIT.class).debug("read {}", readString);

	json = new JSONObject(readString);

	value = json.getString("field");

	assertEquals("value2", value);

	JSONObject queryall = new ESQueryMapper().mapToQuery(new SharedContentQuery());

	GSLoggerFactory.getLogger(ESConnectorInternalTestIT.class).debug("Mapped query all: {}", queryall.toString());

	connector.query(SharedContentType.JSON_TYPE, queryall, false);

	String dateString = "2018-10-25T15:01:46Z";

	Long millis = ISO8601DateTimeUtils.parseISO8601(dateString).getTime();

	String text = "{\"fieldWithDate\":\"value3\",\"start\":" + millis.toString() + "}";

	connector.write(testid, SharedContentType.JSON_TYPE, new ByteArrayInputStream(text.getBytes()));

	Thread.sleep(1000L);

	GSLoggerFactory.getLogger(ESConnectorInternalTestIT.class).debug("Starting query");

	SharedContentQuery dateSCQ = new SharedContentQuery();

	dateSCQ.setFrom(0L, "start");

	JSONObject dateQuery = new ESQueryMapper().mapToQuery(dateSCQ);

	GSLoggerFactory.getLogger(ESConnectorInternalTestIT.class).debug("Mapped query: {}", dateQuery.toString());

	List<InputStream> streams = connector.query(SharedContentType.JSON_TYPE, dateQuery, false);

	Assert.assertEquals(1, streams.size());

	String queryResult = IOUtils.toString(streams.get(0), UTF8);

	JSONObject jqr = new JSONObject(queryResult);

	Assert.assertEquals("value3", jqr.getString("fieldWithDate"));

	Assert.assertEquals(millis, (Long) jqr.getLong("start"));

    }
}