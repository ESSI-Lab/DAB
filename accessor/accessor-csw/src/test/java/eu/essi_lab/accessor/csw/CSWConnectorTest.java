package eu.essi_lab.accessor.csw;

import static eu.essi_lab.accessor.csw.CSWConnector.CSW_EXCEPTIO_REPORT_ERROR;
import static eu.essi_lab.accessor.csw.CSWConnector.CSW_GETRECORDS_UNMARSHALL_ERROR;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.http.HttpRequest;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import eu.essi_lab.accessor.csw.parser.CSWOperationParser;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.jaxb.csw._2_0_2.AbstractRecordType;
import eu.essi_lab.jaxb.csw._2_0_2.Capabilities;
import eu.essi_lab.jaxb.csw._2_0_2.GetRecords;
import eu.essi_lab.jaxb.csw._2_0_2.ResultType;
import eu.essi_lab.jaxb.csw._2_0_2.SearchResultsType;
import eu.essi_lab.jaxb.ows._1_0_0.Operation;
import eu.essi_lab.jaxb.ows._1_0_0.OperationsMetadata;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * @author ilsanto
 */
public class CSWConnectorTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testGetCapabilitiesWithMalformedURL() throws GSException, IOException {

	expectedException.expect(GSException.class);

	CSWConnector connector = Mockito.spy(new CSWConnector());

	connector.getCapabilities("malformed-url;", false);

    }

    @Test
    public void testGetCapabilitiesFailGet() throws Exception {

	expectedException.expect(GSException.class);

	CSWConnector connector = Mockito.spy(new CSWConnector());

	Mockito.doThrow(new IOException()).when(connector).doExecGetRequest(Mockito.any(), Mockito.anyBoolean());

	connector.getCapabilities("http://example.com", false);

    }

    @Test
    public void testGetCapabilitiesFailUnmarshal() throws Exception {

	expectedException.expect(GSException.class);

	CSWConnector connector = Mockito.spy(new CSWConnector());

	InputStream stream = Mockito.mock(InputStream.class);

	Mockito.doReturn(stream).when(connector).doExecGetRequest(Mockito.any(), Mockito.anyBoolean());

	Mockito.doThrow(new Exception("")).when(connector).doUnmarshallCapabiliesStream(Mockito.any(), Mockito.anyBoolean());

	connector.getCapabilities("http://example.com", false);

    }

    @Test
    public void testGetCapabilitiesOk() throws Exception {

	CSWConnector connector = Mockito.spy(new CSWConnector());

	InputStream stream = Mockito.mock(InputStream.class);

	Mockito.doReturn(stream).when(connector).doExecGetRequest(Mockito.any(), Mockito.anyBoolean());

	Capabilities capabilities = Mockito.mock(Capabilities.class);

	Mockito.doReturn(capabilities).when(connector).doUnmarshallCapabiliesStream(Mockito.any(), Mockito.anyBoolean());

	Capabilities c = connector.getCapabilities("http://example.com", false);

	Assert.assertNotNull(c);

    }

    @Test
    public void testGetCapabilitiesOkThirdURL() throws Exception {

	CSWConnector connector = Mockito.spy(new CSWConnector());

	InputStream stream = Mockito.mock(InputStream.class);

	Mockito.doThrow(new IOException()).doReturn(stream, stream).when(connector).doExecGetRequest(Mockito.any(), Mockito.anyBoolean());

	Capabilities capabilities = Mockito.mock(Capabilities.class);

	Mockito.doThrow(new Exception("")).doReturn(capabilities).when(connector).doUnmarshallCapabiliesStream(Mockito.any(),
		Mockito.anyBoolean());

	Capabilities c = connector.getCapabilities("http://example.com", false);

	Assert.assertNotNull(c);

    }

    @Test
    public void testReadNullCapabilitiesDocument() throws GSException {

	expectedException.expect(GSException.class);

	CSWConnector connector = Mockito.spy(new CSWConnector());

	Mockito.doThrow(GSException.createException(new ErrorInfo())).when(connector).getCapabilities(Mockito.any(), Mockito.anyBoolean());

	connector.readCapabilitiesDocument();

    }

    @Test
    public void testReadCapabilitiesDocument() throws GSException {

	CSWConnector connector = Mockito.spy(new CSWConnector());

	Capabilities capabilities = Mockito.mock(Capabilities.class);

	OperationsMetadata operationsMetadata = Mockito.mock(OperationsMetadata.class);

	Mockito.doReturn(operationsMetadata).when(capabilities).getOperationsMetadata();

	List<Operation> operations = new ArrayList<>();

	Operation getCapOp = Mockito.mock(Operation.class);

	String getCapOpName = "GetCapabilities";

	Mockito.doReturn(getCapOpName).when(getCapOp).getName();

	operations.add(getCapOp);

	Operation getRecordsOp = Mockito.mock(Operation.class);

	String getRecordsOpName = "GetRecords";

	Mockito.doReturn(getRecordsOpName).when(getRecordsOp).getName();

	operations.add(getRecordsOp);

	Mockito.doReturn(operations).when(operationsMetadata).getOperation();

	Mockito.doReturn(capabilities).when(connector).getCapabilities(Mockito.any(), Mockito.anyBoolean());

	CSWOperationParser parser = Mockito.mock(CSWOperationParser.class);

	Mockito.doNothing().when(parser).parse();

	String getRecordsURLPOST_SOAP = "getRecordsURLPOSTSOAP";

	Mockito.doReturn(getRecordsURLPOST_SOAP).when(parser).getRecordsURLPOSTSOAP();

	String getRecordsURLPOST_XML = "getRecordsURLPOSTXML";
	Mockito.doReturn(getRecordsURLPOST_XML).when(parser).getRecordsURLPOSTXML();

	String getRecordsURLGET = "getRecordsURLGET";
	Mockito.doReturn(getRecordsURLGET).when(parser).getRecordsURLGET();

	List<String> supportedOutputSchemas = Arrays.asList("schema1", "schema2");

	Mockito.doReturn(supportedOutputSchemas).when(parser).getSupportedOutputSchemas();

	Map<String, QName> supportedTypesBySchema = new HashMap<>();

	QName qname = new QName("uri", "local", "prefix");
	supportedTypesBySchema.put("schema1", qname);

	Mockito.doReturn(supportedTypesBySchema).when(parser).getSupportedTypesBySchema();

	Mockito.doReturn(parser).when(connector).getCSWOperationParser(Mockito.any());

	connector.readCapabilitiesDocument();

	Assert.assertEquals(getRecordsURLPOST_SOAP, connector.getRecordsURLPOSTSOAP);

	Assert.assertEquals(getRecordsURLPOST_XML, connector.getRecordsURLPOSTXML);

	Assert.assertEquals(getRecordsURLGET, connector.getRecordsURLGET);

	Assert.assertEquals(qname, connector.supportedTypesBySchema.get("schema1"));

    }

    @Test
    public void testReadCapabilitiesDocumentNoGetRecordsOperation() throws GSException {

	expectedException.expect(GSException.class);

	CSWConnector connector = Mockito.spy(new CSWConnector());

	Capabilities capabilities = Mockito.mock(Capabilities.class);

	OperationsMetadata operationsMetadata = Mockito.mock(OperationsMetadata.class);

	Mockito.doReturn(operationsMetadata).when(capabilities).getOperationsMetadata();

	List<Operation> operations = new ArrayList<>();

	Operation getCapOp = Mockito.mock(Operation.class);

	String getCapOpName = "GetCapabilities";

	Mockito.doReturn(getCapOpName).when(getCapOp).getName();

	operations.add(getCapOp);

	Mockito.doReturn(operations).when(operationsMetadata).getOperation();

	Mockito.doReturn(capabilities).when(connector).getCapabilities(Mockito.any(), Mockito.anyBoolean());

	CSWOperationParser parser = Mockito.mock(CSWOperationParser.class);

	Mockito.doReturn(parser).when(connector).getCSWOperationParser(Mockito.any());

	connector.readCapabilitiesDocument();

	Mockito.verify(parser, Mockito.times(0)).parse();

    }

    @Test
    public void testListMetadataFormats() throws GSException {

	CSWConnector connector = Mockito.spy(new CSWConnector());

	Mockito.doNothing().when(connector).readCapabilitiesDocument();
	connector.listMetadataFormats();

	Mockito.verify(connector, Mockito.times(1)).readCapabilitiesDocument();

    }

    @Test
    public void testGetSelectedSchemaUserSekected() throws GSException {

	CSWConnector connector = Mockito.spy(new CSWConnector());

	String userSchema = "userSchema";

	connector.selectedSchema = userSchema;

	String schema = connector.getSelectedSchema();

	Assert.assertEquals(userSchema, schema);
	Mockito.verify(connector, Mockito.times(0)).readCapabilitiesDocument();
    }

    @Test
    public void testGetSelectedSchemaDefaultCSW() throws GSException {

	CSWConnector connector = Mockito.spy(new CSWConnector());

	Mockito.doNothing().when(connector).readCapabilitiesDocument();

	String schema = connector.getSelectedSchema();

	Assert.assertEquals(CommonNameSpaceContext.CSW_NS_URI, schema);

	Mockito.verify(connector, Mockito.times(1)).readCapabilitiesDocument();
    }

    @Test
    public void testGetSelectedSchemaSupportedNonDefaultCSW() throws GSException {

	CSWConnector connector = Mockito.spy(new CSWConnector());

	String preferredSchema = CommonNameSpaceContext.GMI_NS_URI;

	connector.supportedOutputSchemas.add(preferredSchema);

	Mockito.doNothing().when(connector).readCapabilitiesDocument();

	String schema = connector.getSelectedSchema();

	Assert.assertEquals(preferredSchema, schema);

	Mockito.verify(connector, Mockito.times(1)).readCapabilitiesDocument();
    }

    @Test
    public void testGetSelectedSchemaUnsupportedNonDefaultCSW() throws GSException {

	CSWConnector connector = Mockito.spy(new CSWConnector());

	String preferredSchema = CommonNameSpaceContext.MCP_2_NS_URI;

	connector.supportedOutputSchemas.add(preferredSchema);

	Mockito.doNothing().when(connector).readCapabilitiesDocument();

	String schema = connector.getSelectedSchema();

	Assert.assertEquals(CommonNameSpaceContext.CSW_NS_URI, schema);

	Mockito.verify(connector, Mockito.times(1)).readCapabilitiesDocument();
    }

    @Test
    public void testCreateGetRecords() throws GSException {
	CSWConnector connector = Mockito.spy(new CSWConnector());

	Mockito.doNothing().when(connector).readCapabilitiesDocument();

	Mockito.doReturn(CommonNameSpaceContext.GMD_NS_URI).when(connector).getSelectedSchema();

	int startPosition = 12;

	int pageSize = 23;

	GetRecords getRecords = connector.createGetRecords(startPosition, pageSize);

	Assert.assertEquals(new BigInteger(startPosition + ""), getRecords.getStartPosition());

	Assert.assertEquals(CommonNameSpaceContext.GMD_NS_URI, getRecords.getOutputSchema());

	Assert.assertEquals(ResultType.RESULTS, getRecords.getResultType());

	Assert.assertEquals("application/xml", getRecords.getOutputFormat());

	Assert.assertEquals(new BigInteger("" + pageSize), getRecords.getMaxRecords());

    }

    @Test
    public void testCreateGetRecordsDefaultSize() throws GSException {
	CSWConnector connector = Mockito.spy(new CSWConnector());

	Mockito.doNothing().when(connector).readCapabilitiesDocument();

	Mockito.doReturn(CommonNameSpaceContext.GMD_NS_URI).when(connector).getSelectedSchema();

	Integer startPosition = 12;

	GetRecords getRecords = connector.createGetRecords(startPosition, null);

	Assert.assertEquals(new BigInteger(startPosition + ""), getRecords.getStartPosition());

	Assert.assertEquals(CommonNameSpaceContext.GMD_NS_URI, getRecords.getOutputSchema());

	Assert.assertEquals(ResultType.RESULTS, getRecords.getResultType());

	Assert.assertEquals("application/xml", getRecords.getOutputFormat());

	Assert.assertEquals(new BigInteger("" + 100), getRecords.getMaxRecords());

    }

    @Test
    public void testListRecordsNullToken() throws GSException {

	Integer maxRecords = 50;

	Integer numberOfRecordsMatched = 80;

	int nextFiled = maxRecords + 1;

	CSWConnector connector = Mockito.spy(new CSWConnector());

	Mockito.doReturn(maxRecords).when(connector).getRequestSize();

	Mockito.doNothing().when(connector).readCapabilitiesDocument();

	Mockito.doReturn(CommonNameSpaceContext.GMD_NS_URI).when(connector).getSelectedSchema();

	ListRecordsRequest request = Mockito.mock(ListRecordsRequest.class);

	CSWHttpGetRecordsRequestCreator creator = Mockito.mock(CSWHttpGetRecordsRequestCreator.class);

	Mockito.doReturn(creator).when(connector).getCreator(Mockito.any());

	HttpRequest baseRequest = Mockito.mock(HttpRequest.class);

	Mockito.doReturn(baseRequest).when(creator).getHttpRequest();

	SimpleEntry<Integer, File> stream = Mockito.mock(SimpleEntry.class);
	Mockito.when(stream.getKey()).thenReturn(new Integer(200));
	Mockito.doReturn(stream).when(connector).doExecHttpRequest(Mockito.any());

	SearchResultsType searchResults = Mockito.mock(SearchResultsType.class);

	Mockito.doReturn(new BigInteger("" + nextFiled)).when(searchResults).getNextRecord();

	Mockito.doReturn(new BigInteger("" + numberOfRecordsMatched)).when(searchResults).getNumberOfRecordsMatched();

	Mockito.doReturn(new BigInteger("" + maxRecords)).when(searchResults).getNumberOfRecordsReturned();

	Mockito.doReturn(searchResults).when(connector).toSearchResults(Mockito.any());

	ListRecordsResponse<OriginalMetadata> response = connector.listRecords(request);

	Assert.assertEquals(Integer.valueOf(51), Integer.valueOf(response.getResumptionToken()));

	Mockito.verify(connector, Mockito.times(1)).readCapabilitiesDocument();

//	

	Mockito.verify(connector, Mockito.times(1)).createGetRecords(Mockito.any(), Mockito.any());

    }

    @Test
    public void testListRecordsNullTokenNullNextField() throws GSException {

	Integer maxRecords = 50;

	Integer numberOfRecordsMatched = 80;

	CSWConnector connector = Mockito.spy(new CSWConnector());

	Mockito.doReturn(maxRecords).when(connector).getRequestSize();

	Mockito.doNothing().when(connector).readCapabilitiesDocument();

	Mockito.doReturn(CommonNameSpaceContext.GMD_NS_URI).when(connector).getSelectedSchema();

	ListRecordsRequest request = Mockito.mock(ListRecordsRequest.class);

	CSWHttpGetRecordsRequestCreator creator = Mockito.mock(CSWHttpGetRecordsRequestCreator.class);

	Mockito.doReturn(creator).when(connector).getCreator(Mockito.any());

	HttpRequest baseRequest = Mockito.mock(HttpRequest.class);

	Mockito.doReturn(baseRequest).when(creator).getHttpRequest();

	SimpleEntry<Integer, File> stream = Mockito.mock(SimpleEntry.class);
	Mockito.when(stream.getKey()).thenReturn(new Integer(200));
	Mockito.doReturn(stream).when(connector).doExecHttpRequest(Mockito.any());

	SearchResultsType searchResults = Mockito.mock(SearchResultsType.class);

	Mockito.doReturn(null).when(searchResults).getNextRecord();

	Mockito.doReturn(new BigInteger("" + numberOfRecordsMatched)).when(searchResults).getNumberOfRecordsMatched();

	Mockito.doReturn(new BigInteger("" + maxRecords)).when(searchResults).getNumberOfRecordsReturned();

	Mockito.doReturn(searchResults).when(connector).toSearchResults(Mockito.any());

	ListRecordsResponse<OriginalMetadata> response = connector.listRecords(request);

	Assert.assertEquals(Integer.valueOf(51), Integer.valueOf(response.getResumptionToken()));

	Mockito.verify(connector, Mockito.times(1)).readCapabilitiesDocument();

	

	Mockito.verify(connector, Mockito.times(1)).createGetRecords(Mockito.any(), Mockito.any());

    }

    @Test
    public void testListRecordsWithToken() throws GSException {

	Integer maxRecords = 50;

	Integer returned = 30;

	Integer numberOfRecordsMatched = 80;

	Integer tokenInt = 51;

	int nextFiled = numberOfRecordsMatched;

	CSWConnector connector = Mockito.spy(new CSWConnector());

	Mockito.doReturn(maxRecords).when(connector).getRequestSize();

	Mockito.doNothing().when(connector).readCapabilitiesDocument();

	Mockito.doReturn(CommonNameSpaceContext.GMD_NS_URI).when(connector).getSelectedSchema();

	ListRecordsRequest request = Mockito.mock(ListRecordsRequest.class);

	Mockito.doReturn("" + tokenInt).when(request).getResumptionToken();

	CSWHttpGetRecordsRequestCreator creator = Mockito.mock(CSWHttpGetRecordsRequestCreator.class);

	Mockito.doReturn(creator).when(connector).getCreator(Mockito.any());

	HttpRequest baseRequest = Mockito.mock(HttpRequest.class);

	Mockito.doReturn(baseRequest).when(creator).getHttpRequest();

	SimpleEntry<Integer, File> stream = Mockito.mock(SimpleEntry.class);
	Mockito.when(stream.getKey()).thenReturn(new Integer(200));
	Mockito.doReturn(stream).when(connector).doExecHttpRequest(Mockito.any());

	SearchResultsType searchResults = Mockito.mock(SearchResultsType.class);

	Mockito.doReturn(new BigInteger("" + nextFiled)).when(searchResults).getNextRecord();

	Mockito.doReturn(new BigInteger("" + numberOfRecordsMatched)).when(searchResults).getNumberOfRecordsMatched();

	Mockito.doReturn(new BigInteger("" + returned)).when(searchResults).getNumberOfRecordsReturned();

	Mockito.doReturn(searchResults).when(connector).toSearchResults(Mockito.any());

	ListRecordsResponse<OriginalMetadata> response = connector.listRecords(request);

	Assert.assertNull(response.getResumptionToken());

	Mockito.verify(connector, Mockito.times(1)).readCapabilitiesDocument();

	

	Mockito.verify(connector, Mockito.times(1)).createGetRecords(Mockito.any(), Mockito.any());

    }

    @Test
    public void testListRecordsWithTokenNullNextField() throws GSException {

	Integer maxRecords = 50;

	Integer returned = 30;

	Integer numberOfRecordsMatched = 80;

	Integer tokenInt = 51;

	CSWConnector connector = Mockito.spy(new CSWConnector());

	Mockito.doReturn(maxRecords).when(connector).getRequestSize();

	Mockito.doNothing().when(connector).readCapabilitiesDocument();

	Mockito.doReturn(CommonNameSpaceContext.GMD_NS_URI).when(connector).getSelectedSchema();

	ListRecordsRequest request = Mockito.mock(ListRecordsRequest.class);

	Mockito.doReturn("" + tokenInt).when(request).getResumptionToken();

	CSWHttpGetRecordsRequestCreator creator = Mockito.mock(CSWHttpGetRecordsRequestCreator.class);

	Mockito.doReturn(creator).when(connector).getCreator(Mockito.any());

	HttpRequest baseRequest = Mockito.mock(HttpRequest.class);

	Mockito.doReturn(baseRequest).when(creator).getHttpRequest();

	SimpleEntry<Integer, File> stream = Mockito.mock(SimpleEntry.class);
	Mockito.when(stream.getKey()).thenReturn(new Integer(200));
	Mockito.doReturn(stream).when(connector).doExecHttpRequest(Mockito.any());

	SearchResultsType searchResults = Mockito.mock(SearchResultsType.class);

	Mockito.doReturn(null).when(searchResults).getNextRecord();

	Mockito.doReturn(new BigInteger("" + numberOfRecordsMatched)).when(searchResults).getNumberOfRecordsMatched();

	Mockito.doReturn(new BigInteger("" + returned)).when(searchResults).getNumberOfRecordsReturned();

	Mockito.doReturn(searchResults).when(connector).toSearchResults(Mockito.any());

	ListRecordsResponse<OriginalMetadata> response = connector.listRecords(request);

	Assert.assertNull(response.getResumptionToken());

	Mockito.verify(connector, Mockito.times(1)).readCapabilitiesDocument();

	

	Mockito.verify(connector, Mockito.times(1)).createGetRecords(Mockito.any(), Mockito.any());

    }

    @Test
    public void testListRecordsLimitedNullToken() throws GSException {

	Integer maxRecords = 50;

	Integer numberOfRecordsMatched = 80;

	int nextFiled = maxRecords + 1;

	int limitRecords = 40;

	CSWConnector connector = Mockito.spy(new CSWConnector());

	Mockito.doReturn(maxRecords).when(connector).getRequestSize();

	Mockito.doNothing().when(connector).readCapabilitiesDocument();

	Mockito.doReturn(CommonNameSpaceContext.GMD_NS_URI).when(connector).getSelectedSchema();

	ListRecordsRequest request = Mockito.mock(ListRecordsRequest.class);

	CSWHttpGetRecordsRequestCreator creator = Mockito.mock(CSWHttpGetRecordsRequestCreator.class);

	Mockito.doReturn(creator).when(connector).getCreator(Mockito.any());

	HttpRequest baseRequest = Mockito.mock(HttpRequest.class);

	Mockito.doReturn(baseRequest).when(creator).getHttpRequest();

	SimpleEntry<Integer, File> stream = Mockito.mock(SimpleEntry.class);
	Mockito.when(stream.getKey()).thenReturn(new Integer(200));
	Mockito.doReturn(stream).when(connector).doExecHttpRequest(Mockito.any());

	SearchResultsType searchResults = Mockito.mock(SearchResultsType.class);

	Mockito.doReturn(new BigInteger("" + nextFiled)).when(searchResults).getNextRecord();

	Mockito.doReturn(new BigInteger("" + numberOfRecordsMatched)).when(searchResults).getNumberOfRecordsMatched();

	Mockito.doReturn(new BigInteger("" + maxRecords)).when(searchResults).getNumberOfRecordsReturned();

	Mockito.doReturn(searchResults).when(connector).toSearchResults(Mockito.any());

	ListRecordsResponse<OriginalMetadata> response = connector.listRecords(request);

	Assert.assertEquals("51", response.getResumptionToken());

	Mockito.verify(connector, Mockito.times(1)).readCapabilitiesDocument();

	

	Mockito.verify(connector, Mockito.times(1)).createGetRecords(Mockito.any(), Mockito.any());

    }

    @Test
    public void testToOriginalMetadataFromRecords() {

	CSWConnector connector = Mockito.spy(new CSWConnector());

	Mockito.doReturn(new OriginalMetadata()).when(connector).recordToOriginalMetadata(Mockito.any());

	ListRecordsResponse<OriginalMetadata> ret = new ListRecordsResponse<>();

	List<JAXBElement<? extends AbstractRecordType>> list = new ArrayList<>();

	JAXBElement<? extends AbstractRecordType> recordType = Mockito.mock(JAXBElement.class);

	list.add(recordType);
	list.add(recordType);
	list.add(recordType);
	list.add(recordType);
	list.add(recordType);

	Optional<List<JAXBElement<? extends AbstractRecordType>>> optional = Optional.of(list);

	connector.toOriginalMetadataFromAbstractRecords(ret, optional);

	Assert.assertEquals(list.size(), countRecords(ret));

	Mockito.verify(connector, Mockito.times(list.size())).recordToOriginalMetadata(Mockito.any());

    }

    @Test
    public void testToOriginalMetadataFromAnies() {

	CSWConnector connector = Mockito.spy(new CSWConnector());

	Mockito.doReturn(new OriginalMetadata()).when(connector).objectToOriginalMetadata(Mockito.any());

	ListRecordsResponse<OriginalMetadata> ret = new ListRecordsResponse<>();

	List<Object> list = new ArrayList<>();

	list.add(new Object());
	list.add(new Object());
	list.add(new Object());
	list.add(new Object());

	Optional<List<Object>> optional = Optional.of(list);

	connector.toOriginalMetadataFromAnies(ret, optional);

	Assert.assertEquals(list.size(), countRecords(ret));

	Mockito.verify(connector, Mockito.times(list.size())).objectToOriginalMetadata(Mockito.any());

    }

    @Test
    public void testToOriginalMetadataFromAniesWithError() {

	CSWConnector connector = Mockito.spy(new CSWConnector());

	Mockito.doReturn(new OriginalMetadata(), null, new OriginalMetadata(), new OriginalMetadata()).when(connector)
		.objectToOriginalMetadata(Mockito.any());

	ListRecordsResponse<OriginalMetadata> ret = new ListRecordsResponse<>();

	List<Object> list = new ArrayList<>();

	list.add(new Object());
	list.add(new Object());
	list.add(new Object());
	list.add(new Object());

	Optional<List<Object>> optional = Optional.of(list);

	connector.toOriginalMetadataFromAnies(ret, optional);

	Assert.assertEquals(list.size() - 1, countRecords(ret));

	Mockito.verify(connector, Mockito.times(list.size())).objectToOriginalMetadata(Mockito.any());

    }

    @Test
    public void testToOriginalMetadataFromARecordsWithError() {

	CSWConnector connector = Mockito.spy(new CSWConnector());

	Mockito.doReturn(new OriginalMetadata(), new OriginalMetadata(), null, new OriginalMetadata(), null).when(connector)
		.recordToOriginalMetadata(Mockito.any());

	ListRecordsResponse<OriginalMetadata> ret = new ListRecordsResponse<>();

	List<JAXBElement<? extends AbstractRecordType>> list = new ArrayList<>();

	JAXBElement<? extends AbstractRecordType> recordType = Mockito.mock(JAXBElement.class);

	list.add(recordType);
	list.add(recordType);
	list.add(recordType);
	list.add(recordType);
	list.add(recordType);

	Optional<List<JAXBElement<? extends AbstractRecordType>>> optional = Optional.of(list);

	connector.toOriginalMetadataFromAbstractRecords(ret, optional);

	Assert.assertEquals(list.size() - 2, countRecords(ret));

	Mockito.verify(connector, Mockito.times(list.size())).recordToOriginalMetadata(Mockito.any());

    }

    private int countRecords(ListRecordsResponse<OriginalMetadata> ret) {
	int count = 0;
	Iterator<OriginalMetadata> it = ret.getRecords();
	while (it.hasNext()) {
	    it.next();
	    count++;
	}

	return count;
    }

    @Test
    public void testToSearchResults() throws Exception {

	InputStream content = getClass().getClassLoader().getResourceAsStream("SimpleGetRecordsResponse.xml");

	File tmpFile = File.createTempFile(getClass().getSimpleName(), ".xml");
	tmpFile.deleteOnExit();
	FileOutputStream fos = new FileOutputStream(tmpFile);
	IOUtils.copy(content, fos);
	content.close();
	fos.close();
	CSWConnector connector = Mockito.spy(new CSWConnector());
	SearchResultsType searchResult = connector.toSearchResults(tmpFile);
	tmpFile.delete();

	Assert.assertEquals(1, searchResult.getNumberOfRecordsMatched().intValue());

	Assert.assertEquals(1, searchResult.getNumberOfRecordsReturned().intValue());

	Assert.assertEquals(0, searchResult.getNextRecord().intValue());

    }

    @Test
    public void testGetCapabilitiesUnmarshal() throws Exception {

	InputStream content = getClass().getClassLoader().getResourceAsStream("GetCapabilities.xml");

	CSWConnector connector = Mockito.spy(new CSWConnector());

	Capabilities capabilities = connector.doUnmarshallCapabiliesStream(content, false);

	Assert.assertEquals("ArcGIS Server Geoportal Extension 10 - OGC CSW 2.0.2 ISO AP",
		capabilities.getServiceIdentification().getTitle().trim());

    }

    @Test
    public void testToOriginalMDReal() throws Exception {

	InputStream content = getClass().getClassLoader().getResourceAsStream("SimpleGetRecordsResponse.xml");

	CSWConnector connector = Mockito.spy(new CSWConnector());

	Mockito.doReturn("http://www.opengis.net/cat/csw").when(connector).getSelectedSchema();

	File tmpFile = File.createTempFile(getClass().getSimpleName(), ".xml");
	tmpFile.deleteOnExit();
	FileOutputStream fos = new FileOutputStream(tmpFile);
	IOUtils.copy(content, fos);
	content.close();
	fos.close();
	SearchResultsType searchResult = connector.toSearchResults(tmpFile);
	tmpFile.delete();

	ListRecordsResponse<OriginalMetadata> ret = connector.toOriginalMetadata(searchResult);

	Assert.assertEquals(1, countRecords(ret));

    }

    @Test
    public void testSupportsException() throws GSException {

	CSWConnector connector = Mockito.spy(new CSWConnector());

	GSSource source = Mockito.mock(GSSource.class);

	ErrorInfo errorInfo = new ErrorInfo();
	errorInfo.setCaller(getClass());

	Mockito.doThrow(GSException.createException(errorInfo)).when(connector).getCapabilities(Mockito.any(), Mockito.anyBoolean());

	Assert.assertFalse(connector.supports(source));
    }

    @Test
    public void testExceptioReport() throws IOException {
	InputStream content = getClass().getClassLoader().getResourceAsStream("exReport.xml");

	CSWConnector connector = Mockito.spy(new CSWConnector());

	try {
	    File tmpFile = File.createTempFile(getClass().getSimpleName(), ".xml");
	    tmpFile.deleteOnExit();
	    FileOutputStream fos = new FileOutputStream(tmpFile);
	    IOUtils.copy(content, fos);
	    content.close();
	    fos.close();
	    connector.handleGetRecordsUnmarshallingException(tmpFile, null);
	    tmpFile.delete();
	} catch (GSException e) {
	    Assert.assertEquals(CSW_EXCEPTIO_REPORT_ERROR, e.getErrorInfoList().get(0).getErrorId());
	    return;
	}

	Assert.fail("Could not unmarshal exception report");

    }

    @Test
    public void testExceptioReport2() throws IOException {
	InputStream content = getClass().getClassLoader().getResourceAsStream("badError.xml");

	CSWConnector connector = Mockito.spy(new CSWConnector());

	try {
	    File tmpFile = File.createTempFile(getClass().getSimpleName(), ".xml");
	    tmpFile.deleteOnExit();
	    FileOutputStream fos = new FileOutputStream(tmpFile);
	    IOUtils.copy(content, fos);
	    content.close();
	    fos.close();
	    connector.handleGetRecordsUnmarshallingException(tmpFile, null);
	    tmpFile.delete();
	} catch (GSException e) {
	    Assert.assertEquals(CSW_GETRECORDS_UNMARSHALL_ERROR, e.getErrorInfoList().get(0).getErrorId());
	    return;
	}

	Assert.fail("Could not handdle bad exception report");

    }
}