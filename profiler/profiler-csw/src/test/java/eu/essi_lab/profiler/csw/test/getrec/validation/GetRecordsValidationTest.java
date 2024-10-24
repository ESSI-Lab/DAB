package eu.essi_lab.profiler.csw.test.getrec.validation;

import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import eu.essi_lab.jaxb.common.CommonContext;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.jaxb.common.ObjectFactories;
import eu.essi_lab.jaxb.csw._2_0_2.Constraint;
import eu.essi_lab.jaxb.csw._2_0_2.ElementSetName;
import eu.essi_lab.jaxb.csw._2_0_2.ElementSetType;
import eu.essi_lab.jaxb.csw._2_0_2.ExceptionCode;
import eu.essi_lab.jaxb.csw._2_0_2.GetRecords;
import eu.essi_lab.jaxb.csw._2_0_2.QueryType;
import eu.essi_lab.jaxb.filter._1_1_0.BBOXType;
import eu.essi_lab.jaxb.filter._1_1_0.FilterType;
import eu.essi_lab.jaxb.filter._1_1_0.PropertyNameType;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.profiler.csw.CSWProfiler;
import eu.essi_lab.profiler.csw.handler.discover.CSWRequestValidator;

 
public class GetRecordsValidationTest {

    @Test
    public void noQueryGetRecordsTest() {

	GetRecords getRecords = new GetRecords();

	InputStream inputStream = null;
	try {
	    inputStream = asInputStream(getRecords);
	} catch (Exception e) {

	    e.printStackTrace();
	    fail("Exception thrown");
	}

	CSWRequestValidator validator = new CSWRequestValidator();
	WebRequest webRequest = WebRequest.createPOST("http://localhost/cwiso", inputStream);

	try {
	    ValidationMessage message = validator.validate(webRequest);

	    ValidationResult result = message.getResult();
	    Assert.assertEquals(ValidationResult.VALIDATION_FAILED, result);

	    String errorCode = message.getErrorCode();
	    Assert.assertEquals(ExceptionCode.NO_APPLICABLE_CODE.getCode(), errorCode);

	    String locator = message.getLocator();
	    Assert.assertNull(locator);

	} catch (GSException e) {

	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void invalidGetRecordsTest() {

	String doc = "<doc></doc>";
	ByteArrayInputStream inputStream = new ByteArrayInputStream(doc.getBytes(StandardCharsets.UTF_8));

	CSWRequestValidator validator = new CSWRequestValidator();
	WebRequest webRequest = WebRequest.createPOST("http://localhost/cwiso", inputStream);

	try {
	    ValidationMessage message = validator.validate(webRequest);

	    ValidationResult result = message.getResult();
	    Assert.assertEquals(ValidationResult.VALIDATION_FAILED, result);

	    String errorCode = message.getErrorCode();
	    Assert.assertEquals(ExceptionCode.NO_APPLICABLE_CODE.getCode(), errorCode);

	    String locator = message.getLocator();
	    Assert.assertNull(locator);

	    String error = message.getError();
	    Assert.assertEquals("Unrecognized request", error);

	} catch (GSException e) {

	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void unsupportedOutputFormatGetRecordsTest() {

	GetRecords getRecords = new GetRecords();
	getRecords.setOutputFormat("X");

	QueryType queryType = new QueryType();

	ElementSetName elementSetName = new ElementSetName();
	elementSetName.setValue(ElementSetType.FULL);
	queryType.setElementSetName(elementSetName);

	queryType.getTypeNames().add(new QName(CommonNameSpaceContext.GMD_NS_URI, "MD_Metadata", "gmd"));

	Constraint constraint = new Constraint();
	constraint.setVersion("1.1.0");

	JAXBElement<QueryType> query = ObjectFactories.CSW().createQuery(queryType);

	FilterType filterType = createFilter();
	constraint.setFilter(filterType);
	queryType.setConstraint(constraint);

	getRecords.setAbstractQuery(query);

	InputStream inputStream = null;
	try {
	    inputStream = asInputStream(getRecords);
	} catch (Exception e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}

	CSWRequestValidator validator = new CSWRequestValidator();
	WebRequest webRequest = WebRequest.createPOST("http://localhost/cwiso", inputStream);

	try {
	    ValidationMessage message = validator.validate(webRequest);

	    ValidationResult result = message.getResult();
	    Assert.assertEquals(ValidationResult.VALIDATION_FAILED, result);

	    String errorCode = message.getErrorCode();
	    Assert.assertEquals(ExceptionCode.INVALID_PARAMETER.getCode(), errorCode);

	    String locator = message.getLocator();
	    Assert.assertEquals("outputFormat", locator);

	} catch (GSException e) {

	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    private FilterType createFilter() {

	FilterType filterType = new FilterType();
	eu.essi_lab.jaxb.filter._1_1_0.ObjectFactory filterFactory = new eu.essi_lab.jaxb.filter._1_1_0.ObjectFactory();
	BBOXType bboxType = new BBOXType();
	PropertyNameType propertyNameType = new PropertyNameType();
	propertyNameType.getContent().add("ows:BoundingBox");
	bboxType.setPropertyName(propertyNameType);

	String env = "<gml:Envelope xmlns:gml =\"http://www.opengis.net/gml\"><gml:lowerCorner>14.05 46.46</gml:lowerCorner><gml:upperCorner>17.24 48.42</gml:upperCorner></gml:Envelope>";
	Element envelope;
	try {
	    XMLDocumentReader reader = new XMLDocumentReader(env);
	    reader.setNamespaceContext(new CommonNameSpaceContext());
	    envelope = reader.getDocument().getDocumentElement();
	    bboxType.setEnvelope(envelope);

	} catch (SAXException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}

	JAXBElement<BBOXType> bbox = filterFactory.createBBOX(bboxType);
	filterType.setSpatialOps(bbox);

	return filterType;
    }

    @Test
    public void unsupportedOutputSchemaGetRecordsTest() {

	GetRecords getRecords = new GetRecords();
	getRecords.setOutputSchema("X");

	QueryType queryType = new QueryType();
	queryType.getTypeNames().add(new QName(CommonNameSpaceContext.GMD_NS_URI, "MD_Metadata", "gmd"));

	ElementSetName elementSetName = new ElementSetName();
	elementSetName.setValue(ElementSetType.FULL);
	queryType.setElementSetName(elementSetName);

	Constraint constraint = new Constraint();
	constraint.setVersion("1.1.0");

	JAXBElement<QueryType> query = ObjectFactories.CSW().createQuery(queryType);

	FilterType filterType = createFilter();

	constraint.setFilter(filterType);
	queryType.setConstraint(constraint);

	getRecords.setAbstractQuery(query);

	InputStream inputStream = null;
	try {
	    inputStream = asInputStream(getRecords);
	} catch (Exception e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}

	CSWRequestValidator validator = new CSWRequestValidator();
	WebRequest webRequest = WebRequest.createPOST("http://localhost/cwiso", inputStream);

	try {
	    ValidationMessage message = validator.validate(webRequest);

	    ValidationResult result = message.getResult();
	    Assert.assertEquals(ValidationResult.VALIDATION_FAILED, result);

	    String errorCode = message.getErrorCode();
	    Assert.assertEquals(ExceptionCode.INVALID_PARAMETER.getCode(), errorCode);

	} catch (GSException e) {

	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void missingQueryTypeNameGetRecordsTest() {

	GetRecords getRecords = new GetRecords();
	getRecords.setOutputFormat(CSWProfiler.SUPPORTED_OUTPUT_FORMATS.get(0));

	QueryType queryType = new QueryType();

	ElementSetName setName = new ElementSetName();
	setName.setValue(ElementSetType.FULL);

	queryType.setElementSetName(new ElementSetName());

	Constraint constraint = new Constraint();
	constraint.setVersion("1.1.0");

	JAXBElement<QueryType> query = ObjectFactories.CSW().createQuery(queryType);

	FilterType filterType = createFilter();

	constraint.setFilter(filterType);
	queryType.setConstraint(constraint);

	getRecords.setAbstractQuery(query);
	//
	// --------------------------

	InputStream inputStream = null;
	try {
	    inputStream = asInputStream(getRecords);
	} catch (Exception e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}

	CSWRequestValidator validator = new CSWRequestValidator();
	WebRequest webRequest = WebRequest.createPOST("http://localhost/cwiso", inputStream);

	try {
	    ValidationMessage message = validator.validate(webRequest);

	    ValidationResult result = message.getResult();
	    Assert.assertEquals(ValidationResult.VALIDATION_FAILED, result);

	    String errorCode = message.getErrorCode();
	    Assert.assertEquals(ExceptionCode.NO_APPLICABLE_CODE.getCode(), errorCode);

	} catch (GSException e) {

	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void bothElementNameElementSetNameSetGetRecordsTest() {

	GetRecords getRecords = new GetRecords();
	getRecords.setOutputFormat(CSWProfiler.SUPPORTED_OUTPUT_FORMATS.get(0));
	//
	// ---------------------------------------

	// --------------------------
	//
	// properly set the query type
	//
	QueryType queryType = new QueryType();
	queryType.getTypeNames().add(new QName(CommonNameSpaceContext.GMD_NS_URI, "MD_Metadata", "gmd"));

	// set the element set name to FULL
	ElementSetName setName = new ElementSetName();
	setName.setValue(ElementSetType.FULL);
	queryType.setElementSetName(setName);

	// set also an element set name
	queryType.getElementNames().add(new QName(CommonNameSpaceContext.DC_NS_URI, "identifier", "dc"));

	//
	// --------------------------

	Constraint constraint = new Constraint();
	constraint.setVersion("1.1.0");

	JAXBElement<QueryType> query = ObjectFactories.CSW().createQuery(queryType);

	FilterType filterType = createFilter();

	constraint.setFilter(filterType);
	queryType.setConstraint(constraint);

	getRecords.setAbstractQuery(query);

	InputStream inputStream = null;
	try {
	    inputStream = asInputStream(getRecords);
	} catch (Exception e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}

	CSWRequestValidator validator = new CSWRequestValidator();
	WebRequest webRequest = WebRequest.createPOST("http://localhost/cswiso", inputStream);

	try {
	    ValidationMessage message = validator.validate(webRequest);

	    ValidationResult result = message.getResult();
	    Assert.assertEquals(ValidationResult.VALIDATION_FAILED, result);

	    String errorCode = message.getErrorCode();
	    Assert.assertEquals(ExceptionCode.NO_APPLICABLE_CODE.getCode(), errorCode);

	} catch (GSException e) {

	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void missingCSWSetNameGetRecordsTest() {

	GetRecords getRecords = new GetRecords();
	getRecords.setOutputFormat(CSWProfiler.SUPPORTED_OUTPUT_FORMATS.get(0));

	// --------------------------
	//
	// properly set the query type
	//
	QueryType queryType = new QueryType();
	// this is OK for a CSWCORE
	queryType.getTypeNames().add(new QName(CommonNameSpaceContext.GMD_NS_URI, "MD_Metadata", "gmd"));

	// ElementSetName setName = new ElementSetName();
	// setName.setValue(ElementSetType.FULL);
	// queryType.setElementSetName(new ElementSetName());

	// --------------------------

	Constraint constraint = new Constraint();
	constraint.setVersion("1.1.0");

	JAXBElement<QueryType> query = ObjectFactories.CSW().createQuery(queryType);

	FilterType filterType = createFilter();

	constraint.setFilter(filterType);
	queryType.setConstraint(constraint);

	getRecords.setAbstractQuery(query);

	InputStream inputStream = null;
	try {
	    inputStream = asInputStream(getRecords);
	} catch (Exception e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}

	CSWRequestValidator validator = new CSWRequestValidator();
	WebRequest webRequest = WebRequest.createPOST("http://localhost/cswiso", inputStream);

	try {
	    ValidationMessage message = validator.validate(webRequest);

	    ValidationResult result = message.getResult();
	    Assert.assertEquals(ValidationResult.VALIDATION_FAILED, result);

	    String errorCode = message.getErrorCode();
	    Assert.assertEquals(ExceptionCode.NO_APPLICABLE_CODE.getCode(), errorCode);

	} catch (GSException e) {

	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void invalidElementSetNameGetRecordsTest() {

	GetRecords getRecords = new GetRecords();
	getRecords.setOutputFormat(CSWProfiler.SUPPORTED_OUTPUT_FORMATS.get(0));

	// --------------------------
	//
	// properly set the query type
	//
	QueryType queryType = new QueryType();
	// this is OK for a CSWCORE
	queryType.getTypeNames().add(new QName(CommonNameSpaceContext.GMD_NS_URI, "MD_Metadata", "gmd"));

	// set an element set name
	ElementSetName setName = new ElementSetName();
	setName.setValue(ElementSetType.FULL);

	// this is not included in the above QueryType typeNames attribute
	setName.getTypeNames().add(new QName(CommonNameSpaceContext.GMI_NS_URI, "MI_Metadata", "gmi"));
	queryType.setElementSetName(setName);

	// --------------------------

	Constraint constraint = new Constraint();
	constraint.setVersion("1.1.0");

	JAXBElement<QueryType> query = ObjectFactories.CSW().createQuery(queryType);

	FilterType filterType = createFilter();

	constraint.setFilter(filterType);
	queryType.setConstraint(constraint);

	getRecords.setAbstractQuery(query);

	InputStream inputStream = null;
	try {
	    inputStream = asInputStream(getRecords);
	} catch (Exception e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}

	CSWRequestValidator validator = new CSWRequestValidator();
	WebRequest webRequest = WebRequest.createPOST("http://localhost/cswiso", inputStream);

	try {
	    ValidationMessage message = validator.validate(webRequest);

	    ValidationResult result = message.getResult();
	    Assert.assertEquals(ValidationResult.VALIDATION_FAILED, result);

	    String errorCode = message.getErrorCode();
	    Assert.assertEquals(ExceptionCode.INVALID_PARAMETER.getCode(), errorCode);

	    String error = message.getError();
	    Assert.assertEquals(
		    "The names specified for the ElementSetName typeName attribute shall be a subset of the names specfied in the typeNames attribute of the Query element",
		    error);

	    String locator = message.getLocator();
	    Assert.assertEquals("ElementName/ElementSetName", locator);

	} catch (GSException e) {

	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void validTypeNamesSubsetGetRecordsTest() {

	GetRecords getRecords = new GetRecords();
	getRecords.setOutputFormat(CSWProfiler.SUPPORTED_OUTPUT_FORMATS.get(0));

	// --------------------------
	//
	// properly set the query type
	//
	QueryType queryType = new QueryType();
	// this is OK for a CSWISO
	queryType.getTypeNames().add(new QName(CommonNameSpaceContext.GMD_NS_URI, "MD_Metadata", "gmd"));

	// set an element set name
	queryType.getElementNames().add(new QName(CommonNameSpaceContext.DC_NS_URI, "identifier", "dc"));

	// --------------------------

	Constraint constraint = new Constraint();
	constraint.setVersion("1.1.0");

	JAXBElement<QueryType> query = ObjectFactories.CSW().createQuery(queryType);

	FilterType filterType = createFilter();

	constraint.setFilter(filterType);
	queryType.setConstraint(constraint);

	getRecords.setAbstractQuery(query);

	InputStream inputStream = null;
	try {
	    inputStream = asInputStream(getRecords);
	} catch (Exception e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}

	CSWRequestValidator validator = new CSWRequestValidator();
	WebRequest webRequest = WebRequest.createPOST("http://localhost/cswiso", inputStream);

	try {
	    ValidationMessage message = validator.validate(webRequest);

	    ValidationResult result = message.getResult();
	    Assert.assertEquals(ValidationResult.VALIDATION_SUCCESSFUL, result);

	    String errorCode = message.getErrorCode();
	    Assert.assertNull(errorCode);

	    String error = message.getError();
	    Assert.assertNull(error);

	    String locator = message.getLocator();
	    Assert.assertNull(locator);

	} catch (GSException e) {

	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void invalidStartPositionGetRecordsTest() {

	GetRecords getRecords = new GetRecords();
	getRecords.setOutputFormat(CSWProfiler.SUPPORTED_OUTPUT_FORMATS.get(0));

	getRecords.setStartPosition(new BigInteger("-1"));

	// --------------------------
	//
	// properly set the query type
	//
	QueryType queryType = new QueryType();
	// this is OK for a CSWISO
	queryType.getTypeNames().add(new QName(CommonNameSpaceContext.GMD_NS_URI, "MD_Metadata", "gmd"));

	// set an element set name
	queryType.getElementNames().add(new QName(CommonNameSpaceContext.DC_NS_URI, "identifier", "dc"));

	// --------------------------

	Constraint constraint = new Constraint();
	constraint.setVersion("1.1.0");

	JAXBElement<QueryType> query = ObjectFactories.CSW().createQuery(queryType);

	FilterType filterType = createFilter();

	constraint.setFilter(filterType);
	queryType.setConstraint(constraint);

	getRecords.setAbstractQuery(query);

	InputStream inputStream = null;
	try {
	    inputStream = asInputStream(getRecords);
	} catch (Exception e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}

	CSWRequestValidator validator = new CSWRequestValidator();
	WebRequest webRequest = WebRequest.createPOST("http://localhost/cswiso", inputStream);

	try {
	    ValidationMessage message = validator.validate(webRequest);

	    ValidationResult result = message.getResult();
	    Assert.assertEquals(ValidationResult.VALIDATION_FAILED, result);

	    String errorCode = message.getErrorCode();
	    Assert.assertEquals(errorCode, ExceptionCode.NO_APPLICABLE_CODE.getCode());

	} catch (GSException e) {

	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void invalidMaxRecordsGetRecordsTest() {

	GetRecords getRecords = new GetRecords();
	getRecords.setOutputFormat(CSWProfiler.SUPPORTED_OUTPUT_FORMATS.get(0));

	getRecords.setMaxRecords(new BigInteger("-1"));

	// --------------------------
	//
	// properly set the query type
	//
	QueryType queryType = new QueryType();
	// this is OK for a CSWISO
	queryType.getTypeNames().add(new QName(CommonNameSpaceContext.GMD_NS_URI, "MD_Metadata", "gmd"));

	// set an element set name
	queryType.getElementNames().add(new QName(CommonNameSpaceContext.DC_NS_URI, "identifier", "dc"));

	// --------------------------

	Constraint constraint = new Constraint();
	constraint.setVersion("1.1.0");

	JAXBElement<QueryType> query = ObjectFactories.CSW().createQuery(queryType);

	FilterType filterType = createFilter();

	constraint.setFilter(filterType);
	queryType.setConstraint(constraint);

	getRecords.setAbstractQuery(query);

	InputStream inputStream = null;
	try {
	    inputStream = asInputStream(getRecords);
	} catch (Exception e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}

	CSWRequestValidator validator = new CSWRequestValidator();
	WebRequest webRequest = WebRequest.createPOST("http://localhost/cswiso", inputStream);

	try {
	    ValidationMessage message = validator.validate(webRequest);

	    ValidationResult result = message.getResult();
	    Assert.assertEquals(ValidationResult.VALIDATION_FAILED, result);

	    String errorCode = message.getErrorCode();
	    Assert.assertEquals(errorCode, ExceptionCode.NO_APPLICABLE_CODE.getCode());

	} catch (GSException e) {

	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void missingDublnCoreNameSpaceDeclarationTest() {

	String query = "	<csw:GetRecords xmlns:csw=\"http://www.opengis.net/cat/csw/2.0.2\" " + //
		"	    xmlns:ogc=\"http://www.opengis.net/ogc\" xmlns:gml=\"http://www.opengis.net/gml\" " + //
		"	     xmlns:dcterms=\"http://purl.org/dc/terms/\" " + //
		"	    xmlns:ows=\"http://www.opengis.net/ows\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" " + //
		"	    outputFormat=\"application/xml\" service=\"CSW\" version=\"2.0.2\"> " + //
		"	    <csw:Query xmlns:gmd=\"http://www.isotc211.org/2005/gmd\" typeNames=\"gmd:MD_Metadata\"> " + //
		"	        <csw:ElementName>dc:identifier</csw:ElementName> " + //
		"	        <csw:Constraint version=\"1.1.0\"> " + //
		"	            <ogc:Filter/> " + //
		"	        </csw:Constraint> " + //
		"	    </csw:Query> " + //
		"	</csw:GetRecords> "; //

	ByteArrayInputStream inputStream = new ByteArrayInputStream(query.getBytes(StandardCharsets.UTF_8));
	try {

	    CSWRequestValidator validator = new CSWRequestValidator();
	    WebRequest webRequest = WebRequest.createPOST("http://localhost/cswiso", inputStream);

	    ValidationMessage message = validator.validate(webRequest);

	    ValidationResult result = message.getResult();
	    Assert.assertEquals(ValidationResult.VALIDATION_FAILED, result);

	    String errorCode = message.getErrorCode();
	    Assert.assertEquals(errorCode, ExceptionCode.NO_APPLICABLE_CODE.getCode());

	} catch (Exception e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    private InputStream asInputStream(GetRecords getRecords) throws Exception, UnsupportedEncodingException {

	return CommonContext.asInputStream(getRecords, true);
    }
}
