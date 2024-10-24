//package eu.essi_lab.accessor.canada;
//
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.util.ArrayList;
//import java.util.List;
//
//import javax.xml.transform.Transformer;
//import javax.xml.transform.TransformerFactory;
//import javax.xml.transform.dom.DOMResult;
//import javax.xml.transform.sax.SAXSource;
//
//import org.apache.cxf.helpers.IOUtils;
//import org.junit.Assert;
//import org.junit.Before;
//import org.junit.Rule;
//import org.junit.Test;
//import org.junit.rules.ExpectedException;
//import org.mockito.Mockito;
//import org.op4j.Op;
//import org.w3c.dom.Document;
//import org.xml.sax.InputSource;
//import org.xml.sax.XMLReader;
//import org.xml.sax.helpers.XMLReaderFactory;
//
//import eu.essi_lab.cdk.utils.MockedDownloader;
//import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
//import eu.essi_lab.lib.xml.XMLDocumentReader;
//import eu.essi_lab.messages.listrecords.ListRecordsRequest;
//import eu.essi_lab.messages.listrecords.ListRecordsResponse;
//import eu.essi_lab.model.Source;
//import eu.essi_lab.model.exceptions.GSException;
//import eu.essi_lab.model.resource.OriginalMetadata;
//import junit.framework.TestCase;
//
//public class CANADAMSCConnectorTestIT {
//
//    private CANADAMSCConnector connector;
//    private Source source;
//    private XMLReader tagsoupReader;
//
//    @Rule
//    public ExpectedException expectedException = ExpectedException.none();
//
//    @Before
//    public void init() throws Exception {
//	this.connector = new CANADAMSCConnector();
//
//	this.source = Mockito.mock(Source.class);
//	this.tagsoupReader = XMLReaderFactory.createXMLReader("org.ccil.cowan.tagsoup.Parser");
//	this.tagsoupReader.setFeature("http://xml.org/sax/features/namespace-prefixes", true);
//    }
//
//    @Test
//    public void testMetadataSupport() throws GSException {
//	TestCase.assertTrue(connector.listMetadataFormats().contains(CommonNameSpaceContext.ENVIRONMENT_CANADA_URI));
//    }
//
//    @Test
//    public void testSupport1() throws GSException {
//	Mockito.when(source.getEndpoint()).thenReturn("http://www.google.com");
//	connector.setSourceURL("http://www.google.com");
//	this.connector.setDownloader(new MockedDownloader("<html>Some html content</html>"));
//	// List<String> value = new ArrayList<String>();
//	// value.add("https://mail.google.com");
//	// Mockito.when(connector.getWebConnector()).thenReturn(Mockito.mock(WebConnector.class));
//	// Mockito.when(connector.getWebConnector().getHrefs("http://www.google.com")).thenReturn(value);
//	TestCase.assertEquals(false, connector.supports(source));
//    }
//
//    @Test
//    public void testSupport2() throws GSException, IOException, Exception {
//	InputStream stream = CANADAMSCConnectorTestIT.class.getClassLoader().getResourceAsStream("wafhtml");
//	InputSource input = new InputSource(stream);
//
//	SAXSource source = new SAXSource(tagsoupReader, input);
//	DOMResult result = new DOMResult();
//	Transformer transformer = TransformerFactory.newInstance().newTransformer();
//	transformer.transform(source, result);
//
//	XMLDocumentReader xdoc = new XMLDocumentReader((Document) result.getNode());
//	List<String> hrefs = xdoc.evaluateTextContent("//@href");
//	hrefs = Op.on(hrefs).map(WebConnector.resolveHref("http://dd.weather.gc.ca/hydrometric/")).removeAllNull().get();
//	TestCase.assertEquals(true, hrefs.size() == 2);
//
//    }
//
//    @Test
//    public void testListRecords() throws GSException, IOException {
//	Mockito.when(source.getEndpoint()).thenReturn("http://dd.weather.gc.ca/hydrometric/");
//	connector.setSourceURL("http://dd.weather.gc.ca/hydrometric/");
//	String canadaHTML = IOUtils.toString(CANADAMSCConnectorTestIT.class.getClassLoader().getResourceAsStream("wafhtml"));
//	this.connector.setDownloader(new MockedDownloader(canadaHTML));
//
//	// List<String> value = new ArrayList<String>();
//	// value.add("http://dd.weather.gc.ca/hydrometric/csv/");
//	// value.add("http://dd.weather.gc.ca/hydrometric/doc/");
//	//
//	// Mockito.when(connector.getWebConnector().getHrefs(canadaHTML)).thenReturn(value);
//
//	connector.createStationsMap();
//
//	String secondId = "50";
//
//	// first record
//	ListRecordsRequest listRecords = new ListRecordsRequest();
//	ListRecordsResponse<OriginalMetadata> response = connector.listRecords(listRecords);
//	Assert.assertEquals(secondId, response.getResumptionToken());
//
//	int stationsnumber = connector.getStationCount();
//	int var = stationsnumber / 10;
//	var = var * 10;
//	listRecords.setResumptionToken(String.valueOf(var));
//	response = connector.listRecords(listRecords);
//	Assert.assertEquals(String.valueOf(stationsnumber), response.getResumptionToken());
//
//	listRecords.setResumptionToken(String.valueOf(stationsnumber));
//	response = connector.listRecords(listRecords);
//	Assert.assertEquals(null, response.getResumptionToken());
//
//	// add max number of records
//	connector.setMaxRecords(50);
//	listRecords.setResumptionToken("51");
//	response = connector.listRecords(listRecords);
//	Assert.assertEquals(null, response.getResumptionToken());
//
//	connector.setMaxRecords(10);
//	listRecords.setResumptionToken(null);
//	response = connector.listRecords(listRecords);
//	Assert.assertEquals("10", response.getResumptionToken());
//
//    }
//
//    // @Test
//    // public void testVariables() throws GSException, IOException {
//    //
//    // Mockito.when(source.getEndpoint()).thenReturn("http://dd.weather.gc.ca/hydrometric/");
//    // connector.setSourceURL("http://dd.weather.gc.ca/hydrometric/");
//    // String canadaHTML =
//    // IOUtils.toString(CANADAMSCConnectorTest.class.getClassLoader().getResourceAsStream("wafhtml"));
//    // this.connector.setDownloader(new MockedDownloader(canadaHTML));
//    //
//    // connector.getCANADAStations();
//    // List<ECStation> res = connector.getStations();
//    // System.out.println(res.size());
//    //
//    // int result = 0;
//    //
//    // for(ECStation station: res) {
//    //
//    // List<String> variables = connector.checkVariable(station);
//    // result += variables.size();
//    // }
//    //
//    // System.out.println(result);
//    //
//    // }
//
//    @Test
//    public void testWaterVariable() throws GSException, IOException {
//
//	InputStream stream = CANADAMSCConnectorTestIT.class.getClassLoader().getResourceAsStream("AB_05EC005_daily_hydrometric.csv");
//
//	TestCase.assertNotNull(stream);
//
//	BufferedReader bfReader = null;
//
//	bfReader = new BufferedReader(new InputStreamReader(stream));
//	// String temp = null;
//	bfReader.readLine(); // skip header line
//	String temp = bfReader.readLine();
//	int i = 0;
//	boolean waterVar = false;
//	boolean dischargeVar = false;
//
//	while ((temp = bfReader.readLine()) != null && i < 10) {
//	    String[] split = temp.split(",", -1);
//	    String waterVariable = split[2];
//	    String dischargeVariable = split[6];
//
//	    if (waterVariable != null && !waterVariable.isEmpty()) {
//		waterVar = true;
//
//	    }
//
//	    if (dischargeVariable != null && !dischargeVariable.isEmpty()) {
//		dischargeVar = true;
//
//	    }
//	    i++;
//	}
//
//	if (bfReader != null)
//	    bfReader.close();
//	Assert.assertTrue(waterVar);
//	Assert.assertFalse(dischargeVar);
//
//    }
//
//    @Test
//    public void testAllVariable() throws GSException, IOException {
//
//	InputStream stream = CANADAMSCConnectorTestIT.class.getClassLoader().getResourceAsStream("BC_08HD018_daily_hydrometric.csv");
//
//	TestCase.assertNotNull(stream);
//
//	BufferedReader bfReader = null;
//
//	bfReader = new BufferedReader(new InputStreamReader(stream));
//	// String temp = null;
//	bfReader.readLine(); // skip header line
//	String temp = bfReader.readLine();
//	int i = 0;
//	boolean waterVar = false;
//	boolean dischargeVar = false;
//
//	while ((temp = bfReader.readLine()) != null && i < 10) {
//	    String[] split = temp.split(",", -1);
//	    String waterVariable = split[2];
//	    String dischargeVariable = split[6];
//
//	    if (waterVariable != null && !waterVariable.isEmpty()) {
//		waterVar = true;
//
//	    }
//
//	    if (dischargeVariable != null && !dischargeVariable.isEmpty()) {
//		dischargeVar = true;
//
//	    }
//	    i++;
//	}
//
//	if (bfReader != null)
//	    bfReader.close();
//	Assert.assertTrue(waterVar);
//	Assert.assertTrue(dischargeVar);
//
//    }
//
//    @Test
//    public void testFileNotFoundExceptionVariable() throws GSException, IOException {
//
//	ECStation station = Mockito.mock(ECStation.class);
//	List<String> values = new ArrayList<String>();
//	values.add("http://dd.weather.gc.ca/hydrometric/csv/AB/daily/AB_07OA001_hourly_hydrometric.csv");
//
//	Mockito.when(station.getValues()).thenReturn(values);
//	// Mockito.doReturn(values).when(station.getValues());
//
//	List<String> variables = connector.checkVariable(station);
//
//	Assert.assertTrue(variables.size() == 0);
//
//    }
//
//}
