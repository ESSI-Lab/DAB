package eu.essi_lab.accessor.csw.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import eu.essi_lab.accessor.csw.CSWConnector;
import eu.essi_lab.jaxb.ows._1_0_0.DCP;
import eu.essi_lab.jaxb.ows._1_0_0.DomainType;
import eu.essi_lab.jaxb.ows._1_0_0.HTTP;
import eu.essi_lab.jaxb.ows._1_0_0.Operation;
import eu.essi_lab.jaxb.ows._1_0_0.RequestMethodType;

/**
 * @author ilsanto
 */
public class CSWOperationParserTest {

    @Test
    public void testGetWellConfigured() {

	Operation operation = Mockito.mock(Operation.class);

	String sourceUrl = "http://example.com";

	String href = "http://example.com/Get";

	initMethodTypeMock(operation, Arrays.asList("Get"), Arrays.asList(href));

	CSWConnector connector = Mockito.mock(CSWConnector.class);

	Mockito.doReturn(sourceUrl).when(connector).normalizeURL(Mockito.any());

	CSWOperationParser parser = Mockito.spy(new CSWOperationParser(operation, connector));

	parser.parse();

	Assert.assertEquals(href, parser.getRecordsURLGET());

    }

    @Test
    public void testGetPostSoapWellConfigured() {

	Operation operation = Mockito.mock(Operation.class);

	String sourceUrl = "http://example.com";

	String hrefGet = "http://example.com/Get";
	String hrefPost = "http://example.com/Post";
	String hrefSoap = "http://example.com/Soap";

	initMethodTypeMock(operation, Arrays.asList("Get", "Post", "Post"), Arrays.asList(hrefGet, hrefPost, hrefSoap),
		Arrays.asList(null, "xml", "soap"));

	CSWConnector connector = Mockito.mock(CSWConnector.class);

	Mockito.doReturn(sourceUrl).when(connector).normalizeURL(Mockito.any());

	CSWOperationParser parser = Mockito.spy(new CSWOperationParser(operation, connector));

	parser.parse();

	Assert.assertEquals(hrefGet, parser.getRecordsURLGET());

	Assert.assertEquals(hrefPost, parser.getRecordsURLPOSTXML());

	Assert.assertEquals(hrefSoap, parser.getRecordsURLPOSTSOAP());

    }

    @Test
    public void testGetPostSoapWellConfiguredNoXmlPostEncoding() {

	Operation operation = Mockito.mock(Operation.class);

	String sourceUrl = "http://example.com";

	String hrefGet = "http://example.com/Get";
	String hrefPost = "http://example.com/Post";
	String hrefSoap = "http://example.com/Soap";

	initMethodTypeMock(operation, Arrays.asList("Get", "Post", "Post"), Arrays.asList(hrefGet, hrefPost, hrefSoap),
		Arrays.asList(null, null, "soap"));

	CSWConnector connector = Mockito.mock(CSWConnector.class);

	Mockito.doReturn(sourceUrl).when(connector).normalizeURL(Mockito.any());

	CSWOperationParser parser = Mockito.spy(new CSWOperationParser(operation, connector));

	parser.parse();

	Assert.assertEquals(hrefGet, parser.getRecordsURLGET());

	Assert.assertEquals(hrefPost, parser.getRecordsURLPOSTXML());

	Assert.assertEquals(hrefSoap, parser.getRecordsURLPOSTSOAP());

    }

    @Test
    public void testGetPostWellConfigured() {

	Operation operation = Mockito.mock(Operation.class);

	String sourceUrl = "http://example.com";

	String hrefGet = "http://example.com/Get";
	String hrefPost = "http://example.com/Post";

	initMethodTypeMock(operation, Arrays.asList("Get", "Post"), Arrays.asList(hrefGet, hrefPost));

	CSWConnector connector = Mockito.mock(CSWConnector.class);

	Mockito.doReturn(sourceUrl).when(connector).normalizeURL(Mockito.any());

	CSWOperationParser parser = Mockito.spy(new CSWOperationParser(operation, connector));

	parser.parse();

	Assert.assertNotEquals(parser.getRecordsURLGET(), parser.getRecordsURLPOSTXML());

	Assert.assertEquals(hrefGet, parser.getRecordsURLGET());

	Assert.assertEquals(hrefPost, parser.getRecordsURLPOSTXML());

    }

    @Test
    public void testGetWellConfiguredPostLocalhost() {

	Operation operation = Mockito.mock(Operation.class);

	String sourceUrl = "http://example.com";

	String hrefGet = "http://example.com/Get";
	String hrefPost = "http://localhost/Post";

	initMethodTypeMock(operation, Arrays.asList("Get", "Post"), Arrays.asList(hrefGet, hrefPost));

	CSWConnector connector = Mockito.mock(CSWConnector.class);

	Mockito.doReturn(sourceUrl).when(connector).normalizeURL(Mockito.any());

	CSWOperationParser parser = Mockito.spy(new CSWOperationParser(operation, connector));

	parser.parse();

	Assert.assertNotEquals(parser.getRecordsURLGET(), parser.getRecordsURLPOSTXML());

	Assert.assertEquals(hrefGet, parser.getRecordsURLGET());

	Assert.assertEquals(sourceUrl, parser.getRecordsURLPOSTXML());

    }

    @Test
    public void testPostWellConfiguredGetLocalhost() {

	Operation operation = Mockito.mock(Operation.class);

	String sourceUrl = "http://example.com";

	String hrefGet = "http://localhost/Get";
	String hrefPost = "http://example.com/Post";

	initMethodTypeMock(operation, Arrays.asList("Get", "Post"), Arrays.asList(hrefGet, hrefPost));

	CSWConnector connector = Mockito.mock(CSWConnector.class);

	Mockito.doReturn(sourceUrl).when(connector).normalizeURL(Mockito.any());

	CSWOperationParser parser = Mockito.spy(new CSWOperationParser(operation, connector));

	parser.parse();

	Assert.assertNotEquals(parser.getRecordsURLGET(), parser.getRecordsURLPOSTXML());

	Assert.assertEquals(sourceUrl, parser.getRecordsURLGET());

	Assert.assertEquals(hrefPost, parser.getRecordsURLPOSTXML());

    }

    @Test
    public void testGetlocalhost() {

	Operation operation = Mockito.mock(Operation.class);

	String sourceUrl = "http://example.com";

	String href = "http://localhost:8080/geonetwork";

	initMethodTypeMock(operation, Arrays.asList("Get"), Arrays.asList(href));

	CSWConnector connector = Mockito.mock(CSWConnector.class);

	Mockito.doReturn(sourceUrl).when(connector).normalizeURL(Mockito.any());

	CSWOperationParser parser = Mockito.spy(new CSWOperationParser(operation, connector));

	parser.parse();

	Assert.assertEquals(sourceUrl, parser.getRecordsURLGET());

    }

    @Test
    public void testPostWellConfigured() {

	Operation operation = Mockito.mock(Operation.class);

	String sourceUrl = "http://example.com";

	String href = "http://example.com/Post";

	initMethodTypeMock(operation, Arrays.asList("Post"), Arrays.asList(href));

	CSWConnector connector = Mockito.mock(CSWConnector.class);

	Mockito.doReturn(sourceUrl).when(connector).normalizeURL(Mockito.any());

	CSWOperationParser parser = Mockito.spy(new CSWOperationParser(operation, connector));

	parser.parse();

	Assert.assertEquals(href, parser.getRecordsURLPOSTXML());

    }

    @Test
    public void testSoapWellConfigured() {

	Operation operation = Mockito.mock(Operation.class);

	String sourceUrl = "http://example.com";

	String href = "http://example.com/Soap";

	initMethodTypeMock(operation, Arrays.asList("Post"), Arrays.asList(href), Arrays.asList("soap"));

	CSWConnector connector = Mockito.mock(CSWConnector.class);

	Mockito.doReturn(sourceUrl).when(connector).normalizeURL(Mockito.any());

	CSWOperationParser parser = Mockito.spy(new CSWOperationParser(operation, connector));

	parser.parse();

	Assert.assertEquals(href, parser.getRecordsURLPOSTSOAP());
	Assert.assertEquals(href, parser.getRecordsURLPOSTXML());

    }

    @Test
    public void testPostlocalhost() {

	Operation operation = Mockito.mock(Operation.class);

	String sourceUrl = "http://example.com";

	String href = "http://localhost:8080/geonetwork";

	initMethodTypeMock(operation, Arrays.asList("Post"), Arrays.asList(href));

	CSWConnector connector = Mockito.mock(CSWConnector.class);

	Mockito.doReturn(sourceUrl).when(connector).normalizeURL(Mockito.any());

	CSWOperationParser parser = Mockito.spy(new CSWOperationParser(operation, connector));

	parser.parse();

	Assert.assertEquals(sourceUrl, parser.getRecordsURLPOSTXML());

    }

    private void populateMethodTypeList(List<JAXBElement<RequestMethodType>> methodTypeList, String type, String href) {

	JAXBElement<RequestMethodType> methodType = Mockito.mock(JAXBElement.class);

	methodTypeList.add(methodType);

	RequestMethodType jaxbvalue = Mockito.mock(RequestMethodType.class);

	Mockito.doReturn(href).when(jaxbvalue).getHref();

	QName qname = new QName("", type, "");

	Mockito.doReturn(qname).when(methodType).getName();

	Mockito.doReturn(jaxbvalue).when(methodType).getValue();
    }

    private void initMethodTypeMock(Operation operation, List<String> types, List<String> hrefs) {

	List<DCP> dcpList = new ArrayList<>();

	DCP dcp = Mockito.mock(DCP.class);

	dcpList.add(dcp);

	HTTP http = Mockito.mock(HTTP.class);

	List<JAXBElement<RequestMethodType>> methodTypeList = new ArrayList<>();

	for (int i = 0; i < types.size(); i++)
	    populateMethodTypeList(methodTypeList, types.get(i), hrefs.get(i));

	Mockito.doReturn(methodTypeList).when(http).getGetOrPost();

	Mockito.doReturn(http).when(dcp).getHTTP();

	Mockito.doReturn(dcpList).when(operation).getDCP();

    }

    private void initMethodTypeMock(Operation operation, List<String> types, List<String> hrefs, List<String> postEncodings) {

	List<DCP> dcpList = new ArrayList<>();

	DCP dcp = Mockito.mock(DCP.class);

	dcpList.add(dcp);

	HTTP http = Mockito.mock(HTTP.class);

	List<JAXBElement<RequestMethodType>> methodTypeList = new ArrayList<>();

	for (int i = 0; i < types.size(); i++) {
	    populateMethodTypeList(methodTypeList, types.get(i), hrefs.get(i), postEncodings.get(i));
	}
	Mockito.doReturn(methodTypeList).when(http).getGetOrPost();

	Mockito.doReturn(http).when(dcp).getHTTP();

	Mockito.doReturn(dcpList).when(operation).getDCP();

    }

    private void populateMethodTypeList(List<JAXBElement<RequestMethodType>> methodTypeList, String type, String href,
	    String postEncoding) {

	JAXBElement<RequestMethodType> methodType = Mockito.mock(JAXBElement.class);

	methodTypeList.add(methodType);

	RequestMethodType jaxbvalue = Mockito.mock(RequestMethodType.class);

	Mockito.doReturn(href).when(jaxbvalue).getHref();

	QName qname = new QName("", type, "");

	Mockito.doReturn(qname).when(methodType).getName();

	Mockito.doReturn(jaxbvalue).when(methodType).getValue();

	if (postEncoding != null) {

	    List<DomainType> domainTypeList = new ArrayList<>();

	    DomainType domainType = Mockito.mock(DomainType.class);

	    Mockito.doReturn("PostEncoding").when(domainType).getName();

	    List<String> values = new ArrayList<>();

	    values.add(postEncoding);

	    Mockito.doReturn(values).when(domainType).getValue();

	    domainTypeList.add(domainType);

	    Mockito.doReturn(domainTypeList).when(jaxbvalue).getConstraint();

	}
    }

    @Test
    public void testEmptyMethodType() {

	Operation operation = Mockito.mock(Operation.class);

	String sourceUrl = "http://example.com";

	initEmptyMethodTypeMock(operation);

	CSWConnector connector = Mockito.mock(CSWConnector.class);

	Mockito.doReturn(sourceUrl).when(connector).normalizeURL(Mockito.any());

	CSWOperationParser parser = Mockito.spy(new CSWOperationParser(operation, connector));

	parser.parse();

	Assert.assertEquals(sourceUrl, parser.getRecordsURLPOSTXML());

	Assert.assertEquals(sourceUrl, parser.getRecordsURLGET());

	Assert.assertEquals(sourceUrl, parser.getRecordsURLPOSTSOAP());

    }

    private void initEmptyMethodTypeMock(Operation operation) {

	List<DCP> dcpList = new ArrayList<>();

	DCP dcp = Mockito.mock(DCP.class);

	dcpList.add(dcp);

	HTTP http = Mockito.mock(HTTP.class);

	List<JAXBElement<RequestMethodType>> methodTypeList = new ArrayList<>();

	Mockito.doReturn(methodTypeList).when(http).getGetOrPost();

	Mockito.doReturn(http).when(dcp).getHTTP();

	Mockito.doReturn(dcpList).when(operation).getDCP();

    }

    private void initSchemas(Operation operation, List<String> schemas) {

	List<DomainType> parameters = new ArrayList<>();

	DomainType domainType = Mockito.mock(DomainType.class);

	parameters.add(domainType);

	Mockito.doReturn(schemas).when(domainType).getValue();

	Mockito.doReturn("outputSchema").when(domainType).getName();

	Mockito.doReturn(parameters).when(operation).getParameter();

    }

    private void initTypeNames(String typeName, Operation operation, CSWOperationParser parser, List<String> nsList, List<String> localList,
	    List<String> uriList) {

	List<DomainType> parameters = new ArrayList<>();

	DomainType domainType = Mockito.mock(DomainType.class);

	parameters.add(domainType);

	List<String> values = new ArrayList<>();

	StringBuilder sb = new StringBuilder();

	sb.append("<Capabilities ");

	for (int i = 0; i < nsList.size(); i++) {

	    values.add(nsList.get(i) + ":" + localList.get(i));

	    sb.append(" ").append("xmlns:").append(nsList.get(i)).append("=").append("\"").append(uriList.get(i)).append("\"").append(" ");

	}

	sb.append("></Capabilities>");

	Mockito.doReturn(sb.toString()).when(parser).capabilitiesString();

	Mockito.doReturn(values).when(domainType).getValue();

	Mockito.doReturn(typeName).when(domainType).getName();

	Mockito.doReturn(parameters).when(operation).getParameter();

    }

    @Test
    public void testOneTypeName() {
	Operation operation = Mockito.mock(Operation.class);

	String namespacePrefix1 = "namespacePrefix1";
	String localName1 = "localName1";

	String typeNameValue = namespacePrefix1 + ":" + localName1;

	String uri1 = "http://test/uri1";

	QName qname1 = new QName(uri1, localName1, namespacePrefix1);

	CSWConnector connector = Mockito.mock(CSWConnector.class);

	CSWOperationParser parser = Mockito.spy(new CSWOperationParser(operation, connector));

	initTypeNames("TypeName", operation, parser, Arrays.asList(namespacePrefix1), Arrays.asList(localName1), Arrays.asList(uri1));

	parser.parse();

	Assert.assertEquals(1, parser.getSupportedTypesBySchema().size());

	Assert.assertEquals(qname1, parser.getSupportedTypesBySchema().get(uri1));

	Assert.assertEquals(qname1.getPrefix(), parser.getSupportedTypesBySchema().get(uri1).getPrefix());

    }

    @Test
    public void testOneTypeNameBadQNamePrefix() {
	Operation operation = Mockito.mock(Operation.class);

	String namespacePrefix1 = "namespacePrefix1";
	String localName1 = "localName1";

	String typeNameValue = namespacePrefix1 + ":" + localName1;

	String uri1 = "http://test/uri1";

	QName qname1 = new QName(uri1, localName1, namespacePrefix1 + "l");

	CSWConnector connector = Mockito.mock(CSWConnector.class);

	CSWOperationParser parser = Mockito.spy(new CSWOperationParser(operation, connector));

	initTypeNames("TypeName", operation, parser, Arrays.asList(namespacePrefix1), Arrays.asList(localName1), Arrays.asList(uri1));

	parser.parse();

	Assert.assertEquals(1, parser.getSupportedTypesBySchema().size());

	Assert.assertNotEquals(qname1.getPrefix(), parser.getSupportedTypesBySchema().get(uri1).getPrefix());

    }

    @Test
    public void testOneTypeNameBadQNameLocal() {
	Operation operation = Mockito.mock(Operation.class);

	String namespacePrefix1 = "namespacePrefix1";
	String localName1 = "localName1";

	String typeNameValue = namespacePrefix1 + ":" + localName1;

	String uri1 = "http://test/uri1";

	QName qname1 = new QName(uri1, localName1 + "l", namespacePrefix1);

	CSWConnector connector = Mockito.mock(CSWConnector.class);

	CSWOperationParser parser = Mockito.spy(new CSWOperationParser(operation, connector));

	initTypeNames("TypeName", operation, parser, Arrays.asList(namespacePrefix1), Arrays.asList(localName1), Arrays.asList(uri1));

	parser.parse();

	Assert.assertEquals(1, parser.getSupportedTypesBySchema().size());

	Assert.assertNotEquals(qname1, parser.getSupportedTypesBySchema().get(uri1));

	Assert.assertEquals(qname1.getPrefix(), parser.getSupportedTypesBySchema().get(uri1).getPrefix());

    }

    @Test
    public void testOneTypeNameBadQNameURI() {
	Operation operation = Mockito.mock(Operation.class);

	String namespacePrefix1 = "namespacePrefix1";
	String localName1 = "localName1";

	String typeNameValue = namespacePrefix1 + ":" + localName1;

	String uri1 = "http://test/uri1";

	QName qname1 = new QName(uri1 + ":", localName1, namespacePrefix1);

	CSWConnector connector = Mockito.mock(CSWConnector.class);

	CSWOperationParser parser = Mockito.spy(new CSWOperationParser(operation, connector));

	initTypeNames("TypeName", operation, parser, Arrays.asList(namespacePrefix1), Arrays.asList(localName1), Arrays.asList(uri1));

	parser.parse();

	Assert.assertEquals(1, parser.getSupportedTypesBySchema().size());

	Assert.assertNotEquals(qname1, parser.getSupportedTypesBySchema().get(uri1));

	Assert.assertEquals(qname1.getPrefix(), parser.getSupportedTypesBySchema().get(uri1).getPrefix());
    }

    @Test
    public void testOneSchema() {
	Operation operation = Mockito.mock(Operation.class);

	String schema1 = "schema1";

	initSchemas(operation, Arrays.asList(schema1));

	CSWConnector connector = Mockito.mock(CSWConnector.class);

	CSWOperationParser parser = Mockito.spy(new CSWOperationParser(operation, connector));

	parser.parse();

	Assert.assertEquals(1, parser.getSupportedOutputSchemas().size());

	Assert.assertEquals(schema1, parser.getSupportedOutputSchemas().get(0));



    }

    @Test
    public void testTwoSchemaa() {
	Operation operation = Mockito.mock(Operation.class);

	String schema1 = "schema1";
	String schema2 = "schema2";

	initSchemas(operation, Arrays.asList(schema1, schema2));

	CSWConnector connector = Mockito.mock(CSWConnector.class);

	CSWOperationParser parser = Mockito.spy(new CSWOperationParser(operation, connector));

	parser.parse();

	Assert.assertEquals(2, parser.getSupportedOutputSchemas().size());

	Assert.assertEquals(schema1, parser.getSupportedOutputSchemas().get(0));
	Assert.assertEquals(schema2, parser.getSupportedOutputSchemas().get(1));

    }
}