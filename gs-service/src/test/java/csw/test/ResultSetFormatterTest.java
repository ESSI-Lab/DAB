package csw.test;

import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.RuntimeDelegate;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Element;

import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.DefaultConfiguration;
import eu.essi_lab.iso.datamodel.ISOMetadata;
import eu.essi_lab.iso.datamodel.classes.MIMetadata;
import eu.essi_lab.jaxb.common.CommonContext;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.jaxb.csw._2_0_2.AbstractRecordType;
import eu.essi_lab.jaxb.csw._2_0_2.ElementSetName;
import eu.essi_lab.jaxb.csw._2_0_2.ElementSetType;
import eu.essi_lab.jaxb.csw._2_0_2.GetRecords;
import eu.essi_lab.jaxb.csw._2_0_2.GetRecordsResponse;
import eu.essi_lab.jaxb.csw._2_0_2.ObjectFactory;
import eu.essi_lab.jaxb.csw._2_0_2.QueryType;
import eu.essi_lab.jaxb.csw._2_0_2.ResultType;
import eu.essi_lab.jaxb.csw._2_0_2.SearchResultsType;
import eu.essi_lab.jaxb.filter._1_1_0.PropertyNameType;
import eu.essi_lab.jaxb.filter._1_1_0.SortByType;
import eu.essi_lab.jaxb.filter._1_1_0.SortOrderType;
import eu.essi_lab.jaxb.filter._1_1_0.SortPropertyType;
import eu.essi_lab.jaxb.iso19139_2.gmi.v_1_0.MIMetadataType;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.count.CountSet;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;
import eu.essi_lab.pdk.rsm.impl.xml.iso19139.GMD_ResultSetMapper;
import eu.essi_lab.profiler.csw.handler.discover.CSWResultSetFormatter;
 
public class ResultSetFormatterTest {

    static {

	RuntimeDelegate.setInstance(new org.apache.cxf.jaxrs.impl.RuntimeDelegateImpl());

	DefaultConfiguration configuration = new DefaultConfiguration();
	configuration.clean();

	ConfigurationWrapper.setConfiguration(configuration);
    }

    @Test
    public void testGetRecordsHITSEmptyResultSetFULLElementSetName() {

	GetRecords getRecords = new GetRecords();

	QueryType queryType = new QueryType();

	// set the query type name
	queryType.getTypeNames().add(new QName(CommonNameSpaceContext.GMI_NS_URI, "MD_Metadata", "gmd"));

	// set the element set name
	ElementSetName elementSetName = new ElementSetName();
	elementSetName.getTypeNames().add(new QName(CommonNameSpaceContext.GMI_NS_URI, "MD_Metadata", "gmd"));
	elementSetName.setValue(ElementSetType.FULL);
	queryType.setElementSetName(elementSetName);

	// set result type and output schema
	getRecords.setResultType(ResultType.HITS);
	getRecords.setOutputSchema(CommonNameSpaceContext.GMI_NS_URI);

	ObjectFactory cswFactory = new eu.essi_lab.jaxb.csw._2_0_2.ObjectFactory();
	JAXBElement<QueryType> query = cswFactory.createQuery(queryType);
	getRecords.setAbstractQuery(query);

	try {

	    InputStream inputStream = asInputStream(getRecords);
	    WebRequest webRequest = WebRequest.createPOST("http://localhost/cwiso", inputStream);

	    DiscoveryMessage message = new DiscoveryMessage();
	    message.setWebRequest(webRequest);

	    ResultSet<Element> resultSet = new ResultSet<Element>();
	    CountSet countSet = new CountSet() {
		public int getCount() {
		    return 0;
		}
	    };

	    resultSet.setCountResponse(countSet);

	    CSWResultSetFormatter formatter = new CSWResultSetFormatter();
	    Response response = formatter.format(message, resultSet);

	    String entity = (String) response.getEntity();

	    GetRecordsResponse grResponse = CommonContext.unmarshal(new ByteArrayInputStream(entity.getBytes(StandardCharsets.UTF_8)),
		    GetRecordsResponse.class);

	    SearchResultsType searchResults = grResponse.getSearchResults();

	    BigInteger numberOfRecordsMatched = searchResults.getNumberOfRecordsMatched();
	    Assert.assertEquals(0, numberOfRecordsMatched.intValue());

	    BigInteger numberOfRecordsReturned = searchResults.getNumberOfRecordsReturned();
	    Assert.assertEquals(0, numberOfRecordsReturned.intValue());

	    BigInteger nextRecord = searchResults.getNextRecord();
	    Assert.assertEquals(0, nextRecord.intValue());

	    ElementSetType elementSet = searchResults.getElementSet();
	    Assert.assertEquals(ElementSetType.FULL, elementSet);

	    String recordSchema = searchResults.getRecordSchema();
	    Assert.assertEquals(CommonNameSpaceContext.GMI_NS_URI, recordSchema);

	    List<JAXBElement<? extends AbstractRecordType>> abstractRecords = searchResults.getAbstractRecords();
	    Assert.assertEquals(0, abstractRecords.size());

	    List<Object> anies = searchResults.getAnies();
	    Assert.assertEquals(0, anies.size());

	} catch (Exception e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void testGetRecordsRESULTSEmptyResultSetFULLElementSetName() {

	GetRecords getRecords = new GetRecords();

	QueryType queryType = new QueryType();

	// set the query type name
	queryType.getTypeNames().add(new QName(CommonNameSpaceContext.GMI_NS_URI, "MD_Metadata", "gmd"));

	// set the element set name
	ElementSetName elementSetName = new ElementSetName();
	elementSetName.getTypeNames().add(new QName(CommonNameSpaceContext.GMI_NS_URI, "MD_Metadata", "gmd"));
	elementSetName.setValue(ElementSetType.FULL);
	queryType.setElementSetName(elementSetName);

	// set result type and output schema
	getRecords.setResultType(ResultType.RESULTS);
	getRecords.setOutputSchema(CommonNameSpaceContext.GMI_NS_URI);

	ObjectFactory cswFactory = new eu.essi_lab.jaxb.csw._2_0_2.ObjectFactory();
	JAXBElement<QueryType> query = cswFactory.createQuery(queryType);
	getRecords.setAbstractQuery(query);

	try {

	    InputStream inputStream = asInputStream(getRecords);
	    WebRequest webRequest = WebRequest.createPOST("http://localhost/cwiso", inputStream);

	    DiscoveryMessage message = new DiscoveryMessage();
	    message.setWebRequest(webRequest);

	    ResultSet<Element> resultSet = new ResultSet<Element>();
	    CountSet countSet = new CountSet() {
		public int getCount() {
		    return 0;
		}
	    };

	    resultSet.setCountResponse(countSet);

	    CSWResultSetFormatter formatter = new CSWResultSetFormatter();
	    Response response = formatter.format(message, resultSet);

	    String entity = (String) response.getEntity();

	    GetRecordsResponse grResponse = CommonContext.unmarshal(new ByteArrayInputStream(entity.getBytes(StandardCharsets.UTF_8)),
		    GetRecordsResponse.class);

	    SearchResultsType searchResults = grResponse.getSearchResults();

	    BigInteger numberOfRecordsMatched = searchResults.getNumberOfRecordsMatched();
	    Assert.assertEquals(0, numberOfRecordsMatched.intValue());

	    BigInteger numberOfRecordsReturned = searchResults.getNumberOfRecordsReturned();
	    Assert.assertEquals(0, numberOfRecordsReturned.intValue());

	    BigInteger nextRecord = searchResults.getNextRecord();
	    Assert.assertEquals(0, nextRecord.intValue());

	    ElementSetType elementSet = searchResults.getElementSet();
	    Assert.assertEquals(ElementSetType.FULL, elementSet);

	    String recordSchema = searchResults.getRecordSchema();
	    Assert.assertEquals(CommonNameSpaceContext.GMI_NS_URI, recordSchema);

	    List<JAXBElement<? extends AbstractRecordType>> abstractRecords = searchResults.getAbstractRecords();
	    Assert.assertEquals(0, abstractRecords.size());

	    List<Object> anies = searchResults.getAnies();
	    Assert.assertEquals(0, anies.size());

	} catch (Exception e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void testGetRecordsRESULTSEmptyResultSetElementName() {

	GetRecords getRecords = new GetRecords();

	QueryType queryType = new QueryType();

	// set the query type name
	queryType.getTypeNames().add(new QName(CommonNameSpaceContext.GMI_NS_URI, "MD_Metadata", "gmd"));

	// set an element name
	queryType.getElementNames().add(new QName("Identifier"));

	// set result type and output schema
	getRecords.setResultType(ResultType.RESULTS);
	getRecords.setOutputSchema(CommonNameSpaceContext.GMI_NS_URI);

	ObjectFactory cswFactory = new eu.essi_lab.jaxb.csw._2_0_2.ObjectFactory();
	JAXBElement<QueryType> query = cswFactory.createQuery(queryType);
	getRecords.setAbstractQuery(query);

	try {

	    InputStream inputStream = asInputStream(getRecords);
	    WebRequest webRequest = WebRequest.createPOST("http://localhost/cwiso", inputStream);

	    DiscoveryMessage message = new DiscoveryMessage();
	    message.setWebRequest(webRequest);

	    ResultSet<Element> resultSet = new ResultSet<Element>();
	    CountSet countSet = new CountSet() {
		public int getCount() {
		    return 0;
		}
	    };

	    resultSet.setCountResponse(countSet);

	    CSWResultSetFormatter formatter = new CSWResultSetFormatter();
	    Response response = formatter.format(message, resultSet);

	    String entity = (String) response.getEntity();

	    GetRecordsResponse grResponse = CommonContext.unmarshal(new ByteArrayInputStream(entity.getBytes(StandardCharsets.UTF_8)),
		    GetRecordsResponse.class);

	    SearchResultsType searchResults = grResponse.getSearchResults();

	    BigInteger numberOfRecordsMatched = searchResults.getNumberOfRecordsMatched();
	    Assert.assertEquals(0, numberOfRecordsMatched.intValue());

	    BigInteger numberOfRecordsReturned = searchResults.getNumberOfRecordsReturned();
	    Assert.assertEquals(0, numberOfRecordsReturned.intValue());

	    BigInteger nextRecord = searchResults.getNextRecord();
	    Assert.assertEquals(0, nextRecord.intValue());

	    ElementSetType elementSet = searchResults.getElementSet();
	    Assert.assertEquals(null, elementSet);

	    String recordSchema = searchResults.getRecordSchema();
	    Assert.assertEquals(CommonNameSpaceContext.GMI_NS_URI, recordSchema);

	    List<JAXBElement<? extends AbstractRecordType>> abstractRecords = searchResults.getAbstractRecords();
	    Assert.assertEquals(0, abstractRecords.size());

	    List<Object> anies = searchResults.getAnies();
	    Assert.assertEquals(0, anies.size());

	} catch (Exception e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void testGetRecordsRESULTSOneResultFULLElementSetName() {

	GetRecords getRecords = new GetRecords();

	QueryType queryType = new QueryType();

	// set the query type name
	queryType.getTypeNames().add(new QName(CommonNameSpaceContext.GMI_NS_URI, "MD_Metadata", "gmd"));

	// set the element set name
	ElementSetName elementSetName = new ElementSetName();
	elementSetName.getTypeNames().add(new QName(CommonNameSpaceContext.GMI_NS_URI, "MD_Metadata", "gmd"));
	elementSetName.setValue(ElementSetType.FULL);
	queryType.setElementSetName(elementSetName);

	// set result type and output schema
	getRecords.setResultType(ResultType.RESULTS);
	getRecords.setOutputSchema(CommonNameSpaceContext.GMI_NS_URI);

	ObjectFactory cswFactory = new eu.essi_lab.jaxb.csw._2_0_2.ObjectFactory();
	JAXBElement<QueryType> query = cswFactory.createQuery(queryType);
	getRecords.setAbstractQuery(query);

	try {

	    InputStream inputStream = asInputStream(getRecords);
	    WebRequest webRequest = WebRequest.createPOST("http://localhost/cwiso", inputStream);

	    DiscoveryMessage message = new DiscoveryMessage();
	    message.setWebRequest(webRequest);

	    ResultSet<Element> resultSet = new ResultSet<Element>();
	    CountSet countSet = new CountSet() {
		public int getCount() {
		    return 1;
		}
	    };
	    resultSet.setCountResponse(countSet);

	    // ---------------------------------------------

	    InputStream stream = CSWElementNameTest.class.getClassLoader()
		    .getResourceAsStream("cswelsetname/metadata.xml");

	    ArrayList<Element> arrayList = new ArrayList<>();
	    arrayList.add(new MIMetadata(stream).asDocument(true).getDocumentElement());
	    resultSet.setResultsList(arrayList);

	    // ---------------------------------------------

	    CSWResultSetFormatter formatter = new CSWResultSetFormatter();
	    Response response = formatter.format(message, resultSet);

	    String entity = (String) response.getEntity();

	    // ---------------------------------------------

	    GetRecordsResponse grResponse = CommonContext.unmarshal(new ByteArrayInputStream(entity.getBytes(StandardCharsets.UTF_8)),
		    GetRecordsResponse.class);

	    SearchResultsType searchResults = grResponse.getSearchResults();

	    BigInteger numberOfRecordsMatched = searchResults.getNumberOfRecordsMatched();
	    Assert.assertEquals(1, numberOfRecordsMatched.intValue());

	    BigInteger numberOfRecordsReturned = searchResults.getNumberOfRecordsReturned();
	    Assert.assertEquals(1, numberOfRecordsReturned.intValue());

	    BigInteger nextRecord = searchResults.getNextRecord();
	    Assert.assertEquals(0, nextRecord.intValue());

	    ElementSetType elementSet = searchResults.getElementSet();
	    Assert.assertEquals(ElementSetType.FULL, elementSet);

	    String recordSchema = searchResults.getRecordSchema();
	    Assert.assertEquals(CommonNameSpaceContext.GMI_NS_URI, recordSchema);

	    List<JAXBElement<? extends AbstractRecordType>> abstractRecords = searchResults.getAbstractRecords();
	    Assert.assertEquals(0, abstractRecords.size());

	    List<Object> anies = searchResults.getAnies();
	    Assert.assertEquals(1, anies.size());

	    @SuppressWarnings("unchecked")
	    JAXBElement<MIMetadataType> doc = (JAXBElement<MIMetadataType>) anies.get(0);
	    MIMetadataType value = doc.getValue();
	    XMLDocumentReader reader = new XMLDocumentReader(new MIMetadata(value).asDocument(true));

	    int intValue = reader.evaluateNumber("count(//*)").intValue();
	    Assert.assertEquals(1103, intValue);

	} catch (Exception e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void testGetRecordsRESULTSOneResultSetElementName() {

	GetRecords getRecords = new GetRecords();

	QueryType queryType = new QueryType();

	// set the query type name
	queryType.getTypeNames().add(new QName(CommonNameSpaceContext.GMI_NS_URI, "MD_Metadata", "gmd"));

	// set an element name
	queryType.getElementNames().add(new QName("Identifier"));

	// set result type and output schema
	getRecords.setResultType(ResultType.RESULTS);
	getRecords.setOutputSchema(CommonNameSpaceContext.GMI_NS_URI);

	ObjectFactory cswFactory = new eu.essi_lab.jaxb.csw._2_0_2.ObjectFactory();
	JAXBElement<QueryType> query = cswFactory.createQuery(queryType);
	getRecords.setAbstractQuery(query);

	try {

	    InputStream inputStream = asInputStream(getRecords);
	    WebRequest webRequest = WebRequest.createPOST("http://localhost/cwiso", inputStream);

	    DiscoveryMessage message = new DiscoveryMessage();
	    message.setWebRequest(webRequest);

	    ResultSet<GSResource> resultSet = new ResultSet<GSResource>();
	    CountSet countSet = new CountSet() {
		public int getCount() {
		    return 1;
		}
	    };
	    resultSet.setCountResponse(countSet);

	    InputStream stream = CSWElementNameTest.class.getClassLoader()
		    .getResourceAsStream("cswelsetname/metadata.xml");

	    ArrayList<GSResource> arrayList = new ArrayList<>();
	    GSResource res = new Dataset();
	    OriginalMetadata originalMetadata = new OriginalMetadata();
	    originalMetadata.setSchemeURI(CommonNameSpaceContext.GMD_NS_URI);
	    MIMetadata metadata = new MIMetadata(stream);
	    originalMetadata.setMetadata(metadata.asString(true));
	    res.setOriginalMetadata(originalMetadata);
	    CoreMetadata coreMetadata = res.getHarmonizedMetadata().getCoreMetadata();
	    coreMetadata.setMIMetadata(metadata);
	    arrayList.add(res);
	    resultSet.setResultsList(arrayList);

	    // ---------------------------------------------

	    GMD_ResultSetMapper mapper = new GMD_ResultSetMapper(queryType.getElementNames());

	    // ---------------------------------------------

	    ResultSet<Element> resultSetString = mapper.map(message, resultSet);

	    // ---------------------------------------------

	    CSWResultSetFormatter formatter = new CSWResultSetFormatter();
	    Response response = formatter.format(message, resultSetString);

	    String entity = (String) response.getEntity();

	    // ---------------------------------------------

	    GetRecordsResponse grResponse = CommonContext.unmarshal(new ByteArrayInputStream(entity.getBytes(StandardCharsets.UTF_8)),
		    GetRecordsResponse.class);

	    SearchResultsType searchResults = grResponse.getSearchResults();

	    BigInteger numberOfRecordsMatched = searchResults.getNumberOfRecordsMatched();
	    Assert.assertEquals(1, numberOfRecordsMatched.intValue());

	    BigInteger numberOfRecordsReturned = searchResults.getNumberOfRecordsReturned();
	    Assert.assertEquals(1, numberOfRecordsReturned.intValue());

	    BigInteger nextRecord = searchResults.getNextRecord();
	    Assert.assertEquals(0, nextRecord.intValue());

	    ElementSetType elementSet = searchResults.getElementSet();
	    Assert.assertEquals(null, elementSet);

	    String recordSchema = searchResults.getRecordSchema();
	    Assert.assertEquals(CommonNameSpaceContext.GMI_NS_URI, recordSchema);

	    List<JAXBElement<? extends AbstractRecordType>> abstractRecords = searchResults.getAbstractRecords();
	    Assert.assertEquals(0, abstractRecords.size());

	    List<Object> anies = searchResults.getAnies();
	    Assert.assertEquals(1, anies.size());

	    @SuppressWarnings("unchecked")
	    JAXBElement<MIMetadataType> doc = (JAXBElement<MIMetadataType>) anies.get(0);
	    MIMetadataType value = doc.getValue();
	    XMLDocumentReader reader = new XMLDocumentReader(new MIMetadata(value).asDocument(true));

	    int intValue = reader.evaluateNumber("count(//*)").intValue();
	    Assert.assertEquals(3, intValue);

	} catch (Exception e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void testSort() {

	testSort(SortOrderType.ASC, "de.pangaea.dataset712421", "de.pangaea.dataset712421_2");
	testSort(SortOrderType.DESC, "de.pangaea.dataset712421_2", "de.pangaea.dataset712421");

    }

    public void testSort(SortOrderType type, String id1, String id2) {

	GetRecords getRecords = new GetRecords();

	QueryType queryType = new QueryType();

	// set the query type name
	queryType.getTypeNames().add(new QName(CommonNameSpaceContext.GMI_NS_URI, "MD_Metadata", "gmd"));

	// set the element set name
	ElementSetName elementSetName = new ElementSetName();
	elementSetName.getTypeNames().add(new QName(CommonNameSpaceContext.GMI_NS_URI, "MD_Metadata", "gmd"));
	elementSetName.setValue(ElementSetType.FULL);
	queryType.setElementSetName(elementSetName);

	// set result type and output schema
	getRecords.setResultType(ResultType.RESULTS);
	getRecords.setOutputSchema(CommonNameSpaceContext.GMI_NS_URI);

	SortByType sortByType = new SortByType();

	SortPropertyType sortPropertyType = new SortPropertyType();
	PropertyNameType propertyNameType = new PropertyNameType();
	propertyNameType.getContent().add("gmd:title");
	sortPropertyType.setPropertyName(propertyNameType);
	sortPropertyType.setSortOrder(type);
	sortByType.getSortProperty().add(sortPropertyType);

	queryType.setSortBy(sortByType);

	ObjectFactory cswFactory = new eu.essi_lab.jaxb.csw._2_0_2.ObjectFactory();
	JAXBElement<QueryType> query = cswFactory.createQuery(queryType);
	getRecords.setAbstractQuery(query);

	try {

	    InputStream inputStream = asInputStream(getRecords);
	    WebRequest webRequest = WebRequest.createPOST("http://localhost/cwiso", inputStream);

	    DiscoveryMessage message = new DiscoveryMessage();
	    message.setWebRequest(webRequest);

	    ResultSet<GSResource> resultSet = new ResultSet<GSResource>();
	    CountSet countSet = new CountSet() {
		public int getCount() {
		    return 1;
		}
	    };
	    resultSet.setCountResponse(countSet);

	    ArrayList<GSResource> arrayList = new ArrayList<>();

	    {

		InputStream stream = CSWElementNameTest.class.getClassLoader()
			.getResourceAsStream("cswelsetname/metadata.xml");

		GSResource res = new Dataset();
		OriginalMetadata originalMetadata = new OriginalMetadata();
		originalMetadata.setSchemeURI(CommonNameSpaceContext.GMD_NS_URI);
		MIMetadata metadata = new MIMetadata(stream);
		originalMetadata.setMetadata(metadata.asString(true));
		res.setOriginalMetadata(originalMetadata);
		res.getHarmonizedMetadata().getCoreMetadata().setMIMetadata(metadata);
		arrayList.add(res);
	    }

	    {

		InputStream stream = CSWElementNameTest.class.getClassLoader()
			.getResourceAsStream("cswelsetname/metadata2.xml");

		GSResource res = new Dataset();
		OriginalMetadata originalMetadata = new OriginalMetadata();
		originalMetadata.setSchemeURI(CommonNameSpaceContext.GMD_NS_URI);
		MIMetadata metadata = new MIMetadata(stream);
		originalMetadata.setMetadata(metadata.asString(true));
		res.setOriginalMetadata(originalMetadata);
		res.getHarmonizedMetadata().getCoreMetadata().setMIMetadata(metadata);
		arrayList.add(res);
	    }

	    resultSet.setResultsList(arrayList);

	    // ---------------------------------------------

	    GMD_ResultSetMapper mapper = new GMD_ResultSetMapper(queryType.getElementNames());

	    // ---------------------------------------------

	    ResultSet<Element> resultSetString = mapper.map(message, resultSet);

	    // ---------------------------------------------

	    CSWResultSetFormatter formatter = new CSWResultSetFormatter();
	    Response response = formatter.format(message, resultSetString);

	    String entity = (String) response.getEntity();

	    // ---------------------------------------------

	    GetRecordsResponse grResponse = CommonContext.unmarshal(new ByteArrayInputStream(entity.getBytes(StandardCharsets.UTF_8)),
		    GetRecordsResponse.class);

	    SearchResultsType searchResults = grResponse.getSearchResults();

	    List<Object> anies = searchResults.getAnies();

	    {
		@SuppressWarnings("unchecked")
		JAXBElement<MIMetadataType> doc = (JAXBElement<MIMetadataType>) anies.get(0);
		MIMetadataType value = doc.getValue();
		String fileId = ISOMetadata.getStringFromCharacterString(value.getFileIdentifier());
		Assert.assertEquals(id1, fileId);
	    }

	    {
		@SuppressWarnings("unchecked")
		JAXBElement<MIMetadataType> doc = (JAXBElement<MIMetadataType>) anies.get(1);
		MIMetadataType value = doc.getValue();
		String fileId = ISOMetadata.getStringFromCharacterString(value.getFileIdentifier());
		Assert.assertEquals(id2, fileId);
	    }

	} catch (Exception e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    private InputStream asInputStream(Object getRecords) throws Exception {

	return CommonContext.asInputStream(getRecords, true);
    }
}
