package eu.essi_lab.accessor.arcgis;

import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;

import eu.essi_lab.accessor.arcgis.handler.AGOLBondHandler;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.ReducedDiscoveryMessage;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.bond.parser.DiscoveryBondParser;
import eu.essi_lab.messages.count.DiscoveryCountResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * @author ilsanto
 */
public class AGOLConnectorTest {

    private Logger logger = GSLoggerFactory.getLogger(getClass());

    @Test
    public void testCreateRequestMethod() throws Exception {

	AGOLConnector connector = Mockito.spy(new AGOLConnector());

	String endpoint = "http://endpoint";

	Mockito.doReturn(endpoint).when(connector).getSourceURL();

	ReducedDiscoveryMessage message = Mockito.mock(ReducedDiscoveryMessage.class);

	Page page = Mockito.mock(Page.class);

	int count = 3;
	int start = 1;

	Mockito.doReturn(start).when(page).getStart();

	Mockito.doReturn(count).when(page).getSize();

	AGOLBondHandler handler = Mockito.mock(AGOLBondHandler.class);

	String handledQuery = "q=pippo";
	Mockito.doReturn(handledQuery).when(handler).getQueryString();
	Mockito.doReturn(true).when(handler).isSupported();

	Mockito.doReturn(handler).when(connector).parse(Mockito.any());

	Optional<String> req = connector.createRequest(message, page);

	logger.debug("Created request {}", req);

	Assert.assertEquals("http://endpoint?num=3&start=0&" + handledQuery + "&", req.get());
    }

    @Test
    public void testSourceURL() {

	AGOLConnector connector = new AGOLConnector();

	GSSource source = new GSSource();

	source.setEndpoint("http://arcgis");
	Assert.assertFalse(connector.supports(source));

	source.setEndpoint("http://www.arcgis.com/sharing/rest/search");
	Assert.assertTrue(connector.supports(source));
    }

    @Test
    public void testCountMethod() throws Exception {

	AGOLConnector connector = Mockito.spy(new AGOLConnector());

	String endpoint = "http://endpoint";

	Mockito.doReturn(Optional.of(endpoint)).when(connector).createRequest(Mockito.any(), Mockito.any());

	HttpResponse response = Mockito.mock(HttpResponse.class);

	Mockito.doReturn(response).when(connector).executeGet(Mockito.any());

	ReducedDiscoveryMessage message = Mockito.mock(ReducedDiscoveryMessage.class);

	try {

	    Integer expectedCount = 34;

	    Mockito.doReturn(expectedCount).when(connector).count((HttpResponse) Mockito.any());

	    DiscoveryCountResponse resp = connector.count(message);

	    Assert.assertEquals(expectedCount, (Integer) resp.getCount());

	    Mockito.verify(connector, Mockito.times(1)).executeGet(Mockito.any());

	    Mockito.verify(connector, Mockito.times(1)).count((HttpResponse) Mockito.any());

	    Mockito.verify(connector, Mockito.times(1)).createRequest(Mockito.any(), Mockito.any());

	} catch (GSException e) {
	    e.log();
	}
    }

    @Test
    public void testQueryMethod() throws Exception {

	AGOLConnector connector = Mockito.spy(new AGOLConnector());

	String endpoint = "http://endpoint";

	Mockito.doReturn(Optional.of(endpoint)).when(connector).createRequest(Mockito.any(), Mockito.any());

	HttpResponse response = Mockito.mock(HttpResponse.class);

	Mockito.doReturn(response).when(connector).executeGet(Mockito.any());

	List<OriginalMetadata> omList = new ArrayList<>();

	omList.add(new OriginalMetadata());

	ReducedDiscoveryMessage message = Mockito.mock(ReducedDiscoveryMessage.class);
	Page page = Mockito.mock(Page.class);

	try {
	    Mockito.doReturn(omList).when(connector).convertResponseToOriginalMD(Mockito.any());
	    ResultSet<OriginalMetadata> resp = connector.query(message, page);

	    Assert.assertEquals(1, resp.getResultsList().size());

	    Mockito.verify(connector, Mockito.times(1)).executeGet(Mockito.any());

	    Mockito.verify(connector, Mockito.times(1)).convertResponseToOriginalMD(Mockito.any());

	    Mockito.verify(connector, Mockito.times(1)).createRequest(Mockito.any(), Mockito.any());

	} catch (GSException e) {

	    e.log();
	}
    }

    @Test
    public void testQueryMethodIOEx() throws Exception {

	AGOLConnector connector = Mockito.spy(new AGOLConnector());

	String endpoint = "http://endpoint";

	Mockito.doReturn(Optional.of(endpoint)).when(connector).createRequest(Mockito.any(), Mockito.any());

	Mockito.doThrow(new IOException()).when(connector).executeGet(Mockito.any());

	List<OriginalMetadata> omList = new ArrayList<>();

	omList.add(new OriginalMetadata());

	ReducedDiscoveryMessage message = Mockito.mock(ReducedDiscoveryMessage.class);
	Page page = Mockito.mock(Page.class);

	try {
	    Mockito.doReturn(omList).when(connector).convertResponseToOriginalMD(Mockito.any());
	    ResultSet<OriginalMetadata> resp = connector.query(message, page);

	    Assert.assertEquals(1, resp.getResultsList().size());

	    Mockito.verify(connector, Mockito.times(1)).executeGet(Mockito.any());

	    Mockito.verify(connector, Mockito.times(1)).convertResponseToOriginalMD(Mockito.any());

	    Mockito.verify(connector, Mockito.times(1)).createRequest(Mockito.any(), Mockito.any());

	} catch (GSException e) {

	    Assert.assertEquals("AGOLConnectorRetrieveError", e.getErrorInfoList().get(0).getErrorId());
	}
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testConvertResponseToOriginalMD() throws Exception {

	AGOLConnector connector = Mockito.spy(new AGOLConnector());

	HttpResponse<InputStream> response = Mockito.mock(HttpResponse.class);

	InputStream stream = AGOLConnectorTest.class.getClassLoader().getResourceAsStream("agolresponse.json");

	Mockito.doReturn(stream).when(response).body();
	try {

	    List<OriginalMetadata> converted = connector.convertResponseToOriginalMD(response);

	    Assert.assertEquals(10, converted.size());

	    Assert.assertEquals(converted.get(0).getSchemeURI(), AGOLMetadataSchemas.AGOL_JSON.toString());

	} catch (GSException e) {

	    e.log();
	}
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCountResponse() throws Exception {

	AGOLConnector connector = Mockito.spy(new AGOLConnector());

	HttpResponse<InputStream> response = Mockito.mock(HttpResponse.class);

	InputStream stream = AGOLConnectorTest.class.getClassLoader().getResourceAsStream("agolresponse.json");

	Mockito.doReturn(stream).when(response).body();
	try {

	    Integer matches = connector.count(response);

	    Assert.assertEquals((Integer) 192, (Integer) matches);

	} catch (GSException e) {

	    e.log();
	}
    }

    @Test
    public void testParse() throws Exception {

	AGOLConnector connector = Mockito.spy(new AGOLConnector());

	ReducedDiscoveryMessage message = Mockito.mock(ReducedDiscoveryMessage.class);

	DiscoveryBondParser parser = Mockito.mock(DiscoveryBondParser.class);

	Mockito.doReturn(parser).when(connector).getParser(Mockito.any());

	AGOLBondHandler handler = connector.parse(message);

	Assert.assertNotNull(handler);

	Mockito.verify(parser, Mockito.times(1)).parse(Mockito.any());

    }
}