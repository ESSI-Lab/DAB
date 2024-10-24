package eu.essi_lab.accessor.fedeo.distributed;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.xml.xpath.XPathExpressionException;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.xml.sax.SAXException;

import eu.essi_lab.accessor.fedeo.bond.FEDEOGranulesBondHandler;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.ReducedDiscoveryMessage;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.bond.parser.DiscoveryBondParser;
import eu.essi_lab.messages.count.DiscoveryCountResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * @author ilsanto
 */
public class FEDEOGranulesConnectorTest {

    private Logger logger = GSLoggerFactory.getLogger(getClass());

    @Test
    public void testSourceURL() {

	FEDEOGranulesConnector connector = new FEDEOGranulesConnector();

	GSSource source = new GSSource();

	source.setEndpoint("http://arcgis");
	Assert.assertFalse(connector.supports(source));
    }

    @Test
    public void testCountMethod() throws Exception {

	FEDEOGranulesConnector connector = Mockito.spy(new FEDEOGranulesConnector());

	String endpoint = "http://endpoint";

	Mockito.doAnswer(new Answer() {
	    @Override
	    public Object answer(InvocationOnMock invocation) throws Throwable {

		Page page = (Page) invocation.getArguments()[1];

		// if (1 - page.getCount() != 0)
		// throw new Exception("Expected page count is " + 1 + " Found " + page.getCount());

		if (1 - page.getStart() != 0)
		    throw new Exception("Expected page start is " + 1 + " Found " + page.getStart());

		return endpoint;
	    }
	}).when(connector).createRequest(Mockito.any(), Mockito.any(), Mockito.any());

	HttpResponse<InputStream> response = Mockito.mock(HttpResponse.class);

	Mockito.doReturn(response).when(connector).executeGet(Mockito.any());

	ReducedDiscoveryMessage message = Mockito.mock(ReducedDiscoveryMessage.class);

	GSResource gsParent = Mockito.mock(GSResource.class);

	Optional<GSResource> parent = Optional.of(gsParent);

	Mockito.doReturn(parent).when(message).getParentGSResource(Mockito.any());

	String parentid = "parentid";

	Mockito.doReturn(parentid).when(connector).readParentId(Mockito.any());

	String surl = "http://surl";

	Optional<String> searhcOptional = Optional.of(surl);

	Mockito.doReturn(searhcOptional).when(connector).readSearchUrlFromParent(Mockito.any());

	String oUlr = "http://optionalURL";

	Mockito.doReturn(oUlr).when(connector).extractTemplateURL(Mockito.any());

	try {

	    Integer expectedCount = 34;

	    Mockito.doReturn(expectedCount).when(connector).count((HttpResponse) Mockito.any());

	    String endpoint2 = "http://endpoint";

	    Mockito.doReturn(endpoint).when(connector).getSourceURL();

	    DiscoveryCountResponse resp = connector.count(message);

	    Assert.assertEquals(expectedCount, (Integer) resp.getCount());

	    Mockito.verify(connector, Mockito.times(1)).executeGet(Mockito.any());

	    Mockito.verify(connector, Mockito.times(1)).count((HttpResponse) Mockito.any());

	    Mockito.verify(connector, Mockito.times(1)).createRequest(Mockito.any(), Mockito.any(), Mockito.any());

	} catch (GSException e) {
	    e.log();
	}
    }

    @Test
    public void testQueryMethod() throws Exception {

	FEDEOGranulesConnector connector = Mockito.spy(new FEDEOGranulesConnector());

	String endpoint = "http://endpoint";

	Mockito.doReturn(endpoint).when(connector).createRequest(Mockito.any(), Mockito.any(), Mockito.any());

	HttpResponse<InputStream> response = Mockito.mock(HttpResponse.class);

	Mockito.doReturn(response).when(connector).executeGet(Mockito.any());

	List<OriginalMetadata> omList = new ArrayList<>();

	omList.add(new OriginalMetadata());

	ReducedDiscoveryMessage message = Mockito.mock(ReducedDiscoveryMessage.class);
	Page page = Mockito.mock(Page.class);

	GSResource gsParent = Mockito.mock(GSResource.class);

	Optional<GSResource> parent = Optional.of(gsParent);

	Mockito.doReturn(parent).when(message).getParentGSResource(Mockito.any());

	String parentid = "parentid";

	Mockito.doReturn(parentid).when(connector).readParentId(Mockito.any());

	String surl = "http://surl";

	Optional<String> searhcOptional = Optional.of(surl);

	Mockito.doReturn(searhcOptional).when(connector).readSearchUrlFromParent(Mockito.any());

	String oUlr = "http://optionalURL";

	Mockito.doReturn(oUlr).when(connector).extractTemplateURL(Mockito.any());

	try {
	    Mockito.doReturn(omList).when(connector).convertResponseToOriginalMD(Mockito.any());

	    Mockito.doReturn(endpoint).when(connector).getSourceURL();

	    ResultSet<OriginalMetadata> resp = connector.query(message, page);

	    Assert.assertEquals(1, resp.getResultsList().size());

	    Mockito.verify(connector, Mockito.times(1)).executeGet(Mockito.any());

	    Mockito.verify(connector, Mockito.times(1)).convertResponseToOriginalMD(Mockito.any());

	    Mockito.verify(connector, Mockito.times(1)).createRequest(Mockito.any(), Mockito.any(), Mockito.any());

	} catch (GSException e) {

	    e.log();
	}
    }

    @Test
    public void testQueryMethodIOEx() throws Exception {

	FEDEOGranulesConnector connector = Mockito.spy(new FEDEOGranulesConnector());

	String endpoint = "http://endpoint";

	Mockito.doReturn(endpoint).when(connector).createRequest(Mockito.any(), Mockito.any(), Mockito.any());

	Mockito.doThrow(new IOException()).when(connector).executeGet(Mockito.any());

	List<OriginalMetadata> omList = new ArrayList<>();

	omList.add(new OriginalMetadata());

	ReducedDiscoveryMessage message = Mockito.mock(ReducedDiscoveryMessage.class);
	Page page = Mockito.mock(Page.class);

	GSResource gsParent = Mockito.mock(GSResource.class);

	Optional<GSResource> parent = Optional.of(gsParent);

	Mockito.doReturn(parent).when(message).getParentGSResource(Mockito.any());

	String parentid = "parentid";

	Mockito.doReturn(parentid).when(connector).readParentId(Mockito.any());

	String surl = "http://surl";

	Optional<String> searhcOptional = Optional.of(surl);

	Mockito.doReturn(searhcOptional).when(connector).readSearchUrlFromParent(Mockito.any());

	String oUlr = "http://optionalURL";

	Mockito.doReturn(oUlr).when(connector).extractTemplateURL(Mockito.any());

	try {
	    Mockito.doReturn(omList).when(connector).convertResponseToOriginalMD(Mockito.any());

	    Mockito.doReturn(endpoint).when(connector).getSourceURL();

	    ResultSet<OriginalMetadata> resp = connector.query(message, page);

	    Assert.assertEquals(1, resp.getResultsList().size());

	    Mockito.verify(connector, Mockito.times(1)).executeGet(Mockito.any());

	    Mockito.verify(connector, Mockito.times(1)).convertResponseToOriginalMD(Mockito.any());

	    Mockito.verify(connector, Mockito.times(1)).createRequest(Mockito.any(), Mockito.any(), Mockito.any());

	} catch (GSException e) {

	    Assert.assertEquals("FEDEOGRANULES_CONNECTOR_ERR_RETRIEVE", e.getErrorInfoList().get(0).getErrorId());
	}
    }

    @Test
    public void testCreateRequestMethod() throws IOException {

	FEDEOGranulesConnector connector = Mockito.spy(new FEDEOGranulesConnector());

	String endpoint = "http://endpoint";

	Mockito.doReturn(endpoint).when(connector).getSourceURL();

	ReducedDiscoveryMessage message = Mockito.mock(ReducedDiscoveryMessage.class);

	Page page = Mockito.mock(Page.class);

	int count = 3;
	int start = 1;

	Mockito.doReturn(start).when(page).getStart();

	Mockito.doReturn(count).when(page).getSize();

	FEDEOGranulesBondHandler handler = Mockito.mock(FEDEOGranulesBondHandler.class);

	String handledQuery = "https://fedeo.ceos.org/opensearch/request?httpAccept=application%2Fatom%2Bxml&parentIdentifier=EOP:NASA:CWIC:C179003214-ORNL_DAAC&startRecord=1&maximumRecords=10";

	Mockito.doReturn(handledQuery).when(handler).getQueryString();

	Mockito.doReturn(handler).when(connector).parse(Mockito.any(), Mockito.anyInt(), Mockito.anyInt(), Mockito.any());

	String req = connector.createRequest(message, page, "");

	logger.debug("Created request {}", req);

	Assert.assertEquals(handledQuery, req);

    }

    @Test
    public void testConvertResponseToOriginalMD() throws IOException, SAXException, XPathExpressionException {

	FEDEOGranulesConnector connector = Mockito.spy(new FEDEOGranulesConnector());

	HttpResponse<InputStream> response = Mockito.mock(HttpResponse.class);

	InputStream stream = FEDEOGranulesConnectorTest.class.getClassLoader()
		.getResourceAsStream("eu/essi_lab/accessor/fedeo/test/fedeo_granules-response.xml");

	Mockito.doReturn(stream).when(response).body();
	try {

	    List<OriginalMetadata> converted = connector.convertResponseToOriginalMD(response);

	    Assert.assertEquals(19, converted.size());

	    // Assert.assertEquals(converted.get(0).getSchemeURI(), CMRGranulesMetadataSchemas.ATOM_ENTRY.toString());

	    XMLDocumentReader r = new XMLDocumentReader(new ByteArrayInputStream(

		    converted.get(0).getMetadata().getBytes(StandardCharsets.UTF_8)

	    ));

	    Assert.assertEquals(1, r.evaluateOriginalNodesList("//*:entry").size());

	} catch (GSException e) {

	    e.log();
	}
    }

    @Test
    public void testCountResponse() throws IOException {

	FEDEOGranulesConnector connector = Mockito.spy(new FEDEOGranulesConnector());

	HttpResponse<InputStream> response = Mockito.mock(HttpResponse.class);

	InputStream stream = FEDEOGranulesConnectorTest.class.getClassLoader()
		.getResourceAsStream("eu/essi_lab/accessor/fedeo/test/fedeo_granules-response.xml");

	Mockito.doReturn(stream).when(response).body();
	try {

	    Integer matches = connector.count(response);

	    Assert.assertEquals((Integer) 19, (Integer) matches);

	} catch (GSException e) {

	    e.log();
	}

    }

    @Test
    public void testParse() throws IOException {

	FEDEOGranulesConnector connector = Mockito.spy(new FEDEOGranulesConnector());

	ReducedDiscoveryMessage message = Mockito.mock(ReducedDiscoveryMessage.class);

	DiscoveryBondParser parser = Mockito.mock(DiscoveryBondParser.class);

	Mockito.doReturn(parser).when(connector).getParser(Mockito.any());

	int count = 3;
	int start = 1;
	FEDEOGranulesBondHandler handler = connector.parse(message, start, count, "");

	Assert.assertNotNull(handler);

	Mockito.verify(parser, Mockito.times(1)).parse(Mockito.any());

    }

    @Test
    public void testRequestURL() throws IOException, GSException {

	FEDEOGranulesConnector connector = new FEDEOGranulesConnector();

	connector.setSourceURL("https://fedeo.ceos.org/opensearch/request?");

	ReducedDiscoveryMessage message = Mockito.mock(ReducedDiscoveryMessage.class);

	Page page = Mockito.mock(Page.class);

	int count = 0;
	int start = 1;

	Mockito.doReturn(start).when(page).getStart();

	Mockito.doReturn(count).when(page).getSize();

	String templateURL = "https://fedeo.ceos.org/opensearch/request?httpAccept=application%2Fatom%2Bxml&parentIdentifier=EOP%3ANASA%3ACWIC%3AC179003214-ORNL_DAAC&startRecord={startRecord?}&maximumRecords={maximumRecords?}&startDate={time:start}&endDate={time:end}&bbox={geo:box}";

	// FEDEOGranulesBondHandler handler = new FEDEOGranulesBondHandler(handledQuery);

	FEDEOGranulesBondHandler bondHandler = connector.parse(message, page.getStart(), page.getSize(), templateURL);

	String resp = bondHandler.getQueryString();

	logger.debug("Query String:", resp);

	Assert.assertEquals(
		"https://fedeo.ceos.org/opensearch/request?httpAccept=application%2Fatom%2Bxml&parentIdentifier=EOP%3ANASA%3ACWIC%3AC179003214-ORNL_DAAC&startRecord=1&clientId=geo-dab&",
		resp);
	// parentIdentifier=EOP:DLR:EOWEB:IRS-P6.LISS-IV.P-MONO&startDate=2000-08-01T00:00:00Z&endDate=2013-08-12T00:00:00Z&maximumRecords=10&

	Mockito.doReturn(10).when(page).getSize();

	String templateURL2 = "https://fedeo.ceos.org/opensearch/request?httpAccept=application%2Fatom%2Bxml&parentIdentifier=EOP%3ADLR%3AEOWEB%3AIRS-P6.LISS-IV.P-MONO&startRecord={startRecord?}&maximumRecords={maximumRecords?}&startDate={time:start}&endDate={time:end}&bbox={geo:box}";

	bondHandler = connector.parse(message, page.getStart(), page.getSize(), templateURL2);

	String resp2 = bondHandler.getQueryString();

	logger.debug("Query String:", resp2);

	Assert.assertEquals(
		"https://fedeo.ceos.org/opensearch/request?httpAccept=application%2Fatom%2Bxml&parentIdentifier=EOP%3ADLR%3AEOWEB%3AIRS-P6.LISS-IV.P-MONO&maximumRecords=10&startRecord=1&clientId=geo-dab&",
		resp2);
    }

}