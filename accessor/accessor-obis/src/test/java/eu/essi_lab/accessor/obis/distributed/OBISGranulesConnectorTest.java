//package eu.essi_lab.accessor.obis.distributed;
//
//import java.io.IOException;
//import java.io.InputStream;
//import java.net.http.HttpResponse;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Optional;
//
//import org.json.JSONObject;
//import org.junit.Assert;
//import org.junit.Test;
//import org.mockito.Mockito;
//import org.mockito.invocation.InvocationOnMock;
//import org.mockito.stubbing.Answer;
//import org.slf4j.Logger;
//
//import eu.essi_lab.lib.utils.GSLoggerFactory;
//import eu.essi_lab.messages.Page;
//import eu.essi_lab.messages.ReducedDiscoveryMessage;
//import eu.essi_lab.messages.ResultSet;
//import eu.essi_lab.messages.bond.parser.DiscoveryBondParser;
//import eu.essi_lab.messages.count.DiscoveryCountResponse;
//import eu.essi_lab.model.exceptions.GSException;
//import eu.essi_lab.model.resource.GSResource;
//import eu.essi_lab.model.resource.OriginalMetadata;
//
///**
// * @author ilsanto
// */
//public class OBISGranulesConnectorTest {
//
//    private Logger logger = GSLoggerFactory.getLogger(getClass());
//
//    @Test
//    public void testCountMethod() throws Exception {
//
//	OBISGranulesConnector connector = Mockito.spy(new OBISGranulesConnector());
//
//	String endpoint = "http://endpoint";
//
//	Optional<String> origid = Optional.of("origid");
//
//	Mockito.doAnswer(new Answer() {
//	    @Override
//	    public Object answer(InvocationOnMock invocation) throws Throwable {
//
//		Page page = (Page) invocation.getArguments()[1];
//
//		if (1 - page.getSize() != 0)
//		    throw new Exception("Expected page count is " + 1 + " Found " + page.getSize());
//
//		if (1 - page.getStart() != 0)
//		    throw new Exception("Expected page start is " + 1 + " Found " + page.getStart());
//
//		String pid = (String) invocation.getArguments()[2];
//
//		if (!pid.equalsIgnoreCase(origid.get()))
//		    throw new Exception("Expected origid is " + origid + " Found " + pid);
//
//		return endpoint;
//	    }
//	}).when(connector).createRequest(Mockito.any(), Mockito.any(), Mockito.any());
//
//	HttpResponse response = Mockito.mock(HttpResponse.class);
//
//	Mockito.doReturn(response).when(connector).executeGet(Mockito.any());
//
//	ReducedDiscoveryMessage message = Mockito.mock(ReducedDiscoveryMessage.class);
//
//	GSResource gsParent = Mockito.mock(GSResource.class);
//
//	Mockito.doReturn(origid).when(gsParent).getOriginalId();
//
//	Optional<GSResource> parent = Optional.of(gsParent);
//
//	Mockito.doReturn(parent).when(message).getParentGSResource(Mockito.any());
//
//	String parentid = "parentid";
//
//	Mockito.doReturn(parentid).when(connector).getParentId(Mockito.any());
//
//	String surl = "http://surl";
//
//	try {
//
//	    Integer expectedCount = 34;
//
//	    Mockito.doReturn(expectedCount).when(connector).count((HttpResponse) Mockito.any());
//
//	    DiscoveryCountResponse resp = connector.count(message);
//
//	    Assert.assertEquals(expectedCount, (Integer) resp.getCount());
//
//	    Mockito.verify(connector, Mockito.times(1)).executeGet(Mockito.any());
//
//	    Mockito.verify(connector, Mockito.times(1)).count((HttpResponse) Mockito.any());
//
//	    Mockito.verify(connector, Mockito.times(1)).createRequest(Mockito.any(), Mockito.any(), Mockito.any());
//
//	} catch (GSException e) {
//	    e.log();
//	}
//    }
//
//    @Test
//    public void testQueryMethod() throws Exception {
//
//	OBISGranulesConnector connector = Mockito.spy(new OBISGranulesConnector());
//
//	String endpoint = "http://endpoint";
//
//	Optional<String> origid = Optional.of("origid");
//
//	Mockito.doAnswer(new Answer() {
//	    @Override
//	    public Object answer(InvocationOnMock invocation) throws Throwable {
//
//		String pid = (String) invocation.getArguments()[2];
//
//		if (!pid.equalsIgnoreCase(origid.get()))
//		    throw new Exception("Expected origid is " + origid + " Found " + pid);
//
//		return endpoint;
//	    }
//	}).when(connector).createRequest(Mockito.any(), Mockito.any(), Mockito.any());
//
//	HttpResponse response = Mockito.mock(HttpResponse.class);
//
//	Mockito.doReturn(response).when(connector).executeGet(Mockito.any());
//
//	List<OriginalMetadata> omList = new ArrayList<>();
//
//	omList.add(new OriginalMetadata());
//
//	ReducedDiscoveryMessage message = Mockito.mock(ReducedDiscoveryMessage.class);
//	Page page = Mockito.mock(Page.class);
//
//	GSResource gsParent = Mockito.mock(GSResource.class);
//
//	Mockito.doReturn(origid).when(gsParent).getOriginalId();
//
//	Optional<GSResource> parent = Optional.of(gsParent);
//
//	Mockito.doReturn(parent).when(message).getParentGSResource(Mockito.any());
//
//	String parentid = "parentid";
//
//	Mockito.doReturn(parentid).when(connector).getParentId(Mockito.any());
//
//	String surl = "http://surl";
//
//	try {
//	    Mockito.doReturn(omList).when(connector).convertResponseToOriginalMD(Mockito.any());
//
//	    ResultSet<OriginalMetadata> resp = connector.query(message, page);
//
//	    Assert.assertEquals(1, resp.getResultsList().size());
//
//	    Mockito.verify(connector, Mockito.times(1)).executeGet(Mockito.any());
//
//	    Mockito.verify(connector, Mockito.times(1)).convertResponseToOriginalMD(Mockito.any());
//
//	    Mockito.verify(connector, Mockito.times(1)).createRequest(Mockito.any(), Mockito.any(), Mockito.any());
//
//	} catch (GSException e) {
//	    e.log();
//	}
//    }
//
//    @Test
//    public void testCreateRequestMethod() throws IOException {
//
//	OBISGranulesConnector connector = Mockito.spy(new OBISGranulesConnector());
//
//	String endpoint = "http://endpoint";
//
//	Mockito.doReturn(endpoint).when(connector).getSourceURL();
//
//	ReducedDiscoveryMessage message = Mockito.mock(ReducedDiscoveryMessage.class);
//
//	Page page = Mockito.mock(Page.class);
//
//	int count = 3;
//	int start = 1;
//
//	Mockito.doReturn(start).when(page).getStart();
//
//	Mockito.doReturn(count).when(page).getSize();
//
//	OBISGranulesBondHandler handler = Mockito.mock(OBISGranulesBondHandler.class);
//
//	String baseurl = "https://api.obis.org/v3";
//
//	Mockito.doReturn(baseurl).when(connector).getSourceURL();
//
//	String handledQuery = "key1=param1&key2=param2";
//
//	Mockito.doReturn(handledQuery).when(handler).getQueryString();
//
//	Mockito.doReturn(handler).when(connector).parse(Mockito.any(), Mockito.anyInt(), Mockito.anyInt(), Mockito.any());
//
//	String req = connector.createRequest(message, page, "");
//
//	logger.debug("Created request {}", req);
//
//	Assert.assertEquals(baseurl + "/occurrence?" + handledQuery, req);
//
//    }
//
//    @Test
//    public void testConvertResponseToOriginalMD() throws IOException {
//
//	OBISGranulesConnector connector = Mockito.spy(new OBISGranulesConnector());
//
//	HttpResponse<InputStream> response = Mockito.mock(HttpResponse.class);
//
//	InputStream stream = OBISGranulesConnectorTest.class.getClassLoader().getResourceAsStream("obisGranulesOriginal.json");
//
//	Mockito.doReturn(stream).when(response).body();
//	try {
//
//	    List<OriginalMetadata> converted = connector.convertResponseToOriginalMD(response);
//
//	    Assert.assertEquals(10, converted.size());
//
//	    Assert.assertEquals(converted.get(0).getSchemeURI(), OBISGranulesResultMapper.OBIS_GRANULES_SCHEME_URI.toString());
//
//	    JSONObject j = new JSONObject(converted.get(0).getMetadata());
//
//	    Assert.assertEquals("urn:lsid:marinespecies.org:taxname:149153", j.getString("scientificNameID"));
//
//	} catch (GSException e) {
//
//	    e.log();
//	}
//    }
//
//    @Test
//    public void testCountResponse() throws Exception {
//
//	OBISGranulesConnector connector = Mockito.spy(new OBISGranulesConnector());
//
//	HttpResponse<InputStream> response = Mockito.mock(HttpResponse.class);
//
//	InputStream stream = OBISGranulesConnectorTest.class.getClassLoader().getResourceAsStream("obisGranulesOriginal.json");
//
//	Mockito.doReturn(stream).when(response).body();
//	try {
//
//	    Integer matches = connector.count(response);
//
//	    Assert.assertEquals((Integer) 56783837, (Integer) matches);
//
//	} catch (GSException e) {
//
//	    e.log();
//	}
//    }
//
//    @Test
//    public void testParse() throws IOException {
//
//	OBISGranulesConnector connector = Mockito.spy(new OBISGranulesConnector());
//
//	ReducedDiscoveryMessage message = Mockito.mock(ReducedDiscoveryMessage.class);
//
//	DiscoveryBondParser parser = Mockito.mock(DiscoveryBondParser.class);
//
//	Mockito.doReturn(parser).when(connector).getParser(Mockito.any());
//
//	int count = 3;
//	int start = 1;
//	OBISGranulesBondHandler handler = connector.parse(message, start, count, "");
//
//	Assert.assertNotNull(handler);
//
//	Mockito.verify(parser, Mockito.times(1)).parse(Mockito.any());
//
//    }
//}