/**
 * 
 */
package eu.essi_lab.profiler.csw.test.converter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.List;

import javax.xml.namespace.QName;

import org.junit.Test;

import eu.essi_lab.jaxb.csw._2_0_2.DistributedSearchType;
import eu.essi_lab.jaxb.csw._2_0_2.ElementSetName;
import eu.essi_lab.jaxb.csw._2_0_2.ElementSetType;
import eu.essi_lab.jaxb.csw._2_0_2.GetRecords;
import eu.essi_lab.jaxb.csw._2_0_2.QueryType;
import eu.essi_lab.jaxb.csw._2_0_2.ResultType;
import eu.essi_lab.jaxb.filter._1_1_0.SortByType;
import eu.essi_lab.jaxb.filter._1_1_0.SortOrderType;
import eu.essi_lab.jaxb.filter._1_1_0.SortPropertyType;
import eu.essi_lab.messages.ValidationException;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.profiler.csw.CSWRequestConverter;
import eu.essi_lab.profiler.csw.handler.discover.CSWRequestValidator;

/**
 * @author Fabrizio
 */
public class CSWRequestConverterTest {

    @Test
    public void test1() throws Exception {

	String request = "http://localhost:9090/gs-service/services/csw?";

	request += "request=GetRecords&";

	request += "service=CSW&";

	request += "version=2.0.2&";

	request += "startPosition=3&";

	request += "maxRecords=11&";

	request += "outputFormat=text/xml&";

	request += "resultType=results&";

	request += "requestId=REQUEST_ID&";

	request += "typeNames=gmd:MD_Metadata&";

	request += "elementSetName=brief&";

	request += "sortBy=ELEMENT1:A,   ELEMENT2:D&";

	request += "outputSchema=http://www.isotc211.org/2005/gmd";

	//
	//
	//

	WebRequest webRequest = WebRequest.createGET(request);

	CSWRequestConverter converter = new CSWRequestConverter();

	GetRecords getRecords = converter.convert(webRequest);

	ResultType resultType = getRecords.getResultType();
	assertEquals(ResultType.RESULTS, resultType);

	int startPosition = getRecords.getStartPosition().intValue();
	assertEquals(3, startPosition);

	int maxRecords = getRecords.getMaxRecords().intValue();
	assertEquals(11, maxRecords);

	String outputFormat = getRecords.getOutputFormat();
	assertEquals("text/xml", outputFormat);

	String outputSchema = getRecords.getOutputSchema();
	assertEquals("http://www.isotc211.org/2005/gmd", outputSchema);

	String requestId = getRecords.getRequestId();
	assertEquals("REQUEST_ID", requestId);

	DistributedSearchType distributedSearch = getRecords.getDistributedSearch();
	assertNull(distributedSearch);

	QueryType queryType = ((QueryType) getRecords.getAbstractQuery().getValue());

	//
	// Type Names
	//

	List<QName> typeNames = queryType.getTypeNames();

	assertEquals(1, typeNames.size());

	assertEquals("gmd", typeNames.get(0).getPrefix());
	assertEquals("MD_Metadata", typeNames.get(0).getLocalPart());

	//
	// ElementSetName
	//

	ElementSetName elementSetName = queryType.getElementSetName();

	ElementSetType value = elementSetName.getValue();
	assertEquals(ElementSetType.BRIEF, value);

	List<QName> elementSetNameTypeNames = elementSetName.getTypeNames();
	assertEquals(1, elementSetNameTypeNames.size());

	assertEquals("gmd", elementSetNameTypeNames.get(0).getPrefix());
	assertEquals("MD_Metadata", elementSetNameTypeNames.get(0).getLocalPart());

	//
	// ElementNames
	//

	List<QName> elementNames = queryType.getElementNames();
	assertEquals(0, elementNames.size());

	//
	// SortBy
	//

	SortByType sortBy = queryType.getSortBy();

	List<SortPropertyType> sortProperty = sortBy.getSortProperty();
	assertEquals(2, sortProperty.size());

	// sort 0

	List<Object> content0 = sortProperty.get(0).getPropertyName().getContent();
	assertEquals(1, content0.size());

	assertEquals("ELEMENT1", content0.get(0).toString());
	assertEquals(SortOrderType.ASC, sortProperty.get(0).getSortOrder());

	// sort 1

	List<Object> content1 = sortProperty.get(1).getPropertyName().getContent();
	assertEquals(1, content0.size());

	assertEquals("ELEMENT2", content1.get(0).toString());
	assertEquals(SortOrderType.DESC, sortProperty.get(1).getSortOrder());

	//
	//
	//

	CSWRequestValidator validator = new CSWRequestValidator();
	ValidationMessage message = validator.validate(webRequest);

	List<ValidationException> exceptions = message.getExceptions();
	exceptions.forEach(e -> System.out.println(e.toString()));

	assertEquals(ValidationResult.VALIDATION_SUCCESSFUL, message.getResult());
    }

    @Test
    public void test2() throws Exception {

	String request = "http://localhost:9090/gs-service/services/csw?";

	request += "request=GetRecords&";

	request += "service=CSW&";

	request += "version=2.0.2&";

	request += "startPosition=3&";

	request += "maxRecords=11&";

	request += "outputFormat=text/xml&";

	request += "resultType=results&";

	request += "requestId=REQUEST_ID&";

	request += "typeNames=gmi:MI_Metadata&";

	request += "elementName=gmd:identifier&";

	request += "sortBy=ELEMENT1:A&";

	request += "outputSchema=http://www.isotc211.org/2005/gmd";

	//
	//
	//

	WebRequest webRequest = WebRequest.createGET(request);

	CSWRequestConverter converter = new CSWRequestConverter();

	GetRecords getRecords = converter.convert(webRequest);

	ResultType resultType = getRecords.getResultType();
	assertEquals(ResultType.RESULTS, resultType);

	int startPosition = getRecords.getStartPosition().intValue();
	assertEquals(3, startPosition);

	int maxRecords = getRecords.getMaxRecords().intValue();
	assertEquals(11, maxRecords);

	String outputFormat = getRecords.getOutputFormat();
	assertEquals("text/xml", outputFormat);

	String outputSchema = getRecords.getOutputSchema();
	assertEquals("http://www.isotc211.org/2005/gmd", outputSchema);

	String requestId = getRecords.getRequestId();
	assertEquals("REQUEST_ID", requestId);

	DistributedSearchType distributedSearch = getRecords.getDistributedSearch();
	assertNull(distributedSearch);

	QueryType queryType = ((QueryType) getRecords.getAbstractQuery().getValue());

	//
	// Type Names
	//

	List<QName> typeNames = queryType.getTypeNames();

	assertEquals(1, typeNames.size());

	assertEquals("gmi", typeNames.get(0).getPrefix());
	assertEquals("MI_Metadata", typeNames.get(0).getLocalPart());

	//
	// ElementSetName
	//

	ElementSetName elementSetName = queryType.getElementSetName();

	assertNull(elementSetName);

	//
	// ElementNames
	//

	List<QName> elementNames = queryType.getElementNames();

	assertEquals(1, elementNames.size());

	assertEquals("gmd", elementNames.get(0).getPrefix());
	assertEquals("identifier", elementNames.get(0).getLocalPart());

	//
	// SortBy
	//

	SortByType sortBy = queryType.getSortBy();

	List<SortPropertyType> sortProperty = sortBy.getSortProperty();
	assertEquals(1, sortProperty.size());

	List<Object> content0 = sortProperty.get(0).getPropertyName().getContent();
	assertEquals(1, content0.size());

	assertEquals("ELEMENT1", content0.get(0).toString());
	assertEquals(SortOrderType.ASC, sortProperty.get(0).getSortOrder());

	//
	//
	//

	CSWRequestValidator validator = new CSWRequestValidator();
	ValidationMessage message = validator.validate(webRequest);

	List<ValidationException> exceptions = message.getExceptions();
	exceptions.forEach(e -> System.out.println(e.toString()));

	assertEquals(ValidationResult.VALIDATION_SUCCESSFUL, message.getResult());
    }

    @Test
    public void test3() throws Exception {

	String request = "http://localhost:9090/gs-service/services/csw?";

	request += "request=GetRecords&";

	request += "service=CSW&";

	request += "version=2.0.2&";

	request += "startPosition=3&";

	request += "maxRecords=11&";

	request += "outputFormat=text/xml&";

	request += "resultType=results&";

	request += "requestId=REQUEST_ID&";

	request += "typeNames=gmd:MD_Metadata&";

	request += "elementName=gmd:identifier&";

	request += "outputSchema=http://www.isotc211.org/2005/gmd";

	//
	//
	//

	WebRequest webRequest = WebRequest.createGET(request);

	CSWRequestConverter converter = new CSWRequestConverter();

	GetRecords getRecords = converter.convert(webRequest);

	ResultType resultType = getRecords.getResultType();
	assertEquals(ResultType.RESULTS, resultType);

	int startPosition = getRecords.getStartPosition().intValue();
	assertEquals(3, startPosition);

	int maxRecords = getRecords.getMaxRecords().intValue();
	assertEquals(11, maxRecords);

	String outputFormat = getRecords.getOutputFormat();
	assertEquals("text/xml", outputFormat);

	String outputSchema = getRecords.getOutputSchema();
	assertEquals("http://www.isotc211.org/2005/gmd", outputSchema);

	String requestId = getRecords.getRequestId();
	assertEquals("REQUEST_ID", requestId);

	DistributedSearchType distributedSearch = getRecords.getDistributedSearch();
	assertNull(distributedSearch);

	QueryType queryType = ((QueryType) getRecords.getAbstractQuery().getValue());

	//
	// Type Names
	//

	List<QName> typeNames = queryType.getTypeNames();

	assertEquals(1, typeNames.size());

	assertEquals("gmd", typeNames.get(0).getPrefix());
	assertEquals("MD_Metadata", typeNames.get(0).getLocalPart());

	//
	// ElementSetName
	//

	ElementSetName elementSetName = queryType.getElementSetName();

	assertNull(elementSetName);

	//
	// ElementNames
	//

	List<QName> elementNames = queryType.getElementNames();

	assertEquals(1, elementNames.size());

	assertEquals("gmd", elementNames.get(0).getPrefix());
	assertEquals("identifier", elementNames.get(0).getLocalPart());

	//
	// SortBy
	//

	SortByType sortBy = queryType.getSortBy();

	assertNull(sortBy);

	//
	//
	//

	CSWRequestValidator validator = new CSWRequestValidator();
	ValidationMessage message = validator.validate(webRequest);

	List<ValidationException> exceptions = message.getExceptions();
	exceptions.forEach(e -> System.out.println(e.toString()));

	assertEquals(ValidationResult.VALIDATION_SUCCESSFUL, message.getResult());
    }

    @Test
    public void test4() throws Exception {

	String request = "http://localhost:9090/gs-service/services/csw?";

	request += "request=GetRecords&";

	request += "service=CSW&";

	request += "version=2.0.2&";

	request += "startPosition=3&";

	request += "maxRecords=11&";

	request += "outputFormat=text/xml&";

	request += "resultType=results&";

	request += "typeNames=gmd:MD_Metadata&";

	request += "elementName=gmd:identifier,      gmd:title&";

	request += "outputSchema=http://www.isotc211.org/2005/gmd";

	//
	//
	//

	WebRequest webRequest = WebRequest.createGET(request);

	CSWRequestConverter converter = new CSWRequestConverter();

	GetRecords getRecords = converter.convert(webRequest);

	ResultType resultType = getRecords.getResultType();
	assertEquals(ResultType.RESULTS, resultType);

	int startPosition = getRecords.getStartPosition().intValue();
	assertEquals(3, startPosition);

	int maxRecords = getRecords.getMaxRecords().intValue();
	assertEquals(11, maxRecords);

	String outputFormat = getRecords.getOutputFormat();
	assertEquals("text/xml", outputFormat);

	String outputSchema = getRecords.getOutputSchema();
	assertEquals("http://www.isotc211.org/2005/gmd", outputSchema);

	String requestId = getRecords.getRequestId();
	assertNull(requestId);

	DistributedSearchType distributedSearch = getRecords.getDistributedSearch();
	assertNull(distributedSearch);

	QueryType queryType = ((QueryType) getRecords.getAbstractQuery().getValue());

	//
	// Type Names
	//

	List<QName> typeNames = queryType.getTypeNames();

	assertEquals(1, typeNames.size());

	assertEquals("gmd", typeNames.get(0).getPrefix());
	assertEquals("MD_Metadata", typeNames.get(0).getLocalPart());

	//
	// ElementSetName
	//

	ElementSetName elementSetName = queryType.getElementSetName();

	assertNull(elementSetName);

	//
	// ElementNames
	//

	List<QName> elementNames = queryType.getElementNames();

	assertEquals(2, elementNames.size());

	assertEquals("gmd", elementNames.get(0).getPrefix());
	assertEquals("identifier", elementNames.get(0).getLocalPart());

	assertEquals("gmd", elementNames.get(1).getPrefix());
	assertEquals("title", elementNames.get(1).getLocalPart());

	//
	// SortBy
	//

	SortByType sortBy = queryType.getSortBy();
	assertNull(sortBy);

	//
	//
	//

	CSWRequestValidator validator = new CSWRequestValidator();
	ValidationMessage message = validator.validate(webRequest);

	List<ValidationException> exceptions = message.getExceptions();
	exceptions.forEach(e -> System.out.println(e.toString()));

	assertEquals(ValidationResult.VALIDATION_SUCCESSFUL, message.getResult());
    }

    @Test
    public void test5() throws Exception {

	String request = "http://localhost:9090/gs-service/services/csw?";

	request += "request=GetRecords&";

	request += "service=CSW&";

	request += "version=2.0.2&";

	request += "startPosition=3&";

	request += "maxRecords=11&";

	request += "outputFormat=text/xml&";

	request += "resultType=results&";

	request += "typeNames=gmd:MD_Metadata,   gmi:MI_Metadata&";

	request += "elementSetName=brief&";

	request += "outputSchema=http://www.isotc211.org/2005/gmd";

	//
	//
	//

	WebRequest webRequest = WebRequest.createGET(request);

	CSWRequestConverter converter = new CSWRequestConverter();

	GetRecords getRecords = converter.convert(webRequest);

	ResultType resultType = getRecords.getResultType();
	assertEquals(ResultType.RESULTS, resultType);

	int startPosition = getRecords.getStartPosition().intValue();
	assertEquals(3, startPosition);

	int maxRecords = getRecords.getMaxRecords().intValue();
	assertEquals(11, maxRecords);

	String outputFormat = getRecords.getOutputFormat();
	assertEquals("text/xml", outputFormat);

	String outputSchema = getRecords.getOutputSchema();
	assertEquals("http://www.isotc211.org/2005/gmd", outputSchema);

	String requestId = getRecords.getRequestId();
	assertNull(requestId);

	DistributedSearchType distributedSearch = getRecords.getDistributedSearch();
	assertNull(distributedSearch);

	QueryType queryType = ((QueryType) getRecords.getAbstractQuery().getValue());

	//
	// Type Names
	//

	List<QName> typeNames = queryType.getTypeNames();

	assertEquals(2, typeNames.size());

	assertEquals("gmd", typeNames.get(0).getPrefix());
	assertEquals("MD_Metadata", typeNames.get(0).getLocalPart());

	assertEquals("gmi", typeNames.get(1).getPrefix());
	assertEquals("MI_Metadata", typeNames.get(1).getLocalPart());

	//
	// ElementSetName
	//

	ElementSetName elementSetName = queryType.getElementSetName();

	ElementSetType value = elementSetName.getValue();
	assertEquals(ElementSetType.BRIEF, value);

	List<QName> elementSetNameTypeNames = elementSetName.getTypeNames();
	assertEquals(2, elementSetNameTypeNames.size());

	assertEquals("gmd", elementSetNameTypeNames.get(0).getPrefix());
	assertEquals("MD_Metadata", elementSetNameTypeNames.get(0).getLocalPart());

	assertEquals("gmi", elementSetNameTypeNames.get(1).getPrefix());
	assertEquals("MI_Metadata", elementSetNameTypeNames.get(1).getLocalPart());

	//
	// ElementNames
	//

	List<QName> elementNames = queryType.getElementNames();
	assertEquals(0, elementNames.size());

	//
	// SortBy
	//

	SortByType sortBy = queryType.getSortBy();
	assertNull(sortBy);

	//
	//
	//

	CSWRequestValidator validator = new CSWRequestValidator();
	ValidationMessage message = validator.validate(webRequest);

	List<ValidationException> exceptions = message.getExceptions();
	exceptions.forEach(e -> System.out.println(e.toString()));

	assertEquals(ValidationResult.VALIDATION_SUCCESSFUL, message.getResult());
    }

    @Test
    public void test6() throws Exception {

	String request = "http://localhost:9090/gs-service/services/csw?";

	request += "request=GetRecords&";

	request += "service=CSW&";

	request += "version=2.0.2&";

	request += "startPosition=3&";

	request += "maxRecords=11&";

	request += "outputFormat=text/xml&";

	request += "resultType=results&";

	request += "typeNames=gmx:MX_Metadata&";

	request += "elementSetName=brief&";

	request += "outputSchema=http://www.isotc211.org/2005/gmd";

	//
	//
	//

	WebRequest webRequest = WebRequest.createGET(request);

	CSWRequestConverter converter = new CSWRequestConverter();

	GetRecords getRecords = converter.convert(webRequest);

	ResultType resultType = getRecords.getResultType();
	assertEquals(ResultType.RESULTS, resultType);

	int startPosition = getRecords.getStartPosition().intValue();
	assertEquals(3, startPosition);

	int maxRecords = getRecords.getMaxRecords().intValue();
	assertEquals(11, maxRecords);

	String outputFormat = getRecords.getOutputFormat();
	assertEquals("text/xml", outputFormat);

	String outputSchema = getRecords.getOutputSchema();
	assertEquals("http://www.isotc211.org/2005/gmd", outputSchema);

	String requestId = getRecords.getRequestId();
	assertNull(requestId);

	DistributedSearchType distributedSearch = getRecords.getDistributedSearch();
	assertNull(distributedSearch);

	QueryType queryType = ((QueryType) getRecords.getAbstractQuery().getValue());

	//
	// Type Names
	//

	List<QName> typeNames = queryType.getTypeNames();

	assertEquals(1, typeNames.size());

	assertEquals("gmx", typeNames.get(0).getPrefix());
	assertEquals("MX_Metadata", typeNames.get(0).getLocalPart());

	//
	// ElementSetName
	//

	ElementSetName elementSetName = queryType.getElementSetName();

	ElementSetType value = elementSetName.getValue();
	assertEquals(ElementSetType.BRIEF, value);

	List<QName> elementSetNameTypeNames = elementSetName.getTypeNames();
	assertEquals(1, elementSetNameTypeNames.size());

	assertEquals("gmx", elementSetNameTypeNames.get(0).getPrefix());
	assertEquals("MX_Metadata", elementSetNameTypeNames.get(0).getLocalPart());

	//
	// ElementNames
	//

	List<QName> elementNames = queryType.getElementNames();
	assertEquals(0, elementNames.size());

	//
	// SortBy
	//

	SortByType sortBy = queryType.getSortBy();
	assertNull(sortBy);

	//
	//
	//

	CSWRequestValidator validator = new CSWRequestValidator();
	ValidationMessage message = validator.validate(webRequest);

	List<ValidationException> exceptions = message.getExceptions();
	exceptions.forEach(e -> System.out.println(e.toString()));

	assertEquals(ValidationResult.VALIDATION_FAILED, message.getResult());
    }

    @Test(expected = IllegalArgumentException.class)
    public void test7() throws Exception {

	String request = "http://localhost:9090/gs-service/services/csw?";

	request += "request=GetRecords&";

	request += "service=CSW&";

	request += "version=2.0.2&";

	request += "startPosition=3&";

	request += "maxRecords=11&";

	request += "outputFormat=text/xml&";

	request += "resultType=results&";

	request += "typeNames=gmd:MD_Metadata&";

	request += "elementSetName=xxx&";

	request += "outputSchema=http://www.isotc211.org/2005/gmd";

	//
	//
	//

	WebRequest webRequest = WebRequest.createGET(request);

	CSWRequestConverter converter = new CSWRequestConverter();

	converter.convert(webRequest);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void test7_2() throws Exception {

	String request = "http://localhost:9090/gs-service/services/csw?";

	request += "request=GetRecords&";

	request += "service=CSW&";

	request += "version=2.0.2&";

	request += "startPosition=3&";

	request += "maxRecords=11&";

	request += "outputFormat=text/xml&";

	request += "resultType=results&";

	request += "typeNames=gmd:MD_Metadata&";

	request += "elementSetName=brief&";

	request += "sortBy=x:x:x&";
	
	request += "outputSchema=http://www.isotc211.org/2005/gmd";

	//
	//
	//

	WebRequest webRequest = WebRequest.createGET(request);

	CSWRequestConverter converter = new CSWRequestConverter();

	converter.convert(webRequest);
    }
    
    @Test
    public void test8() throws Exception {

	String request = "http://localhost:9090/gs-service/services/csw?";

	request += "request=GetRecords&";

	request += "service=CSW&";

	request += "version=2.0.2&";

	request += "startPosition=3&";

	request += "maxRecords=11&";

	request += "outputFormat=xxx/xml&";

	request += "resultType=results&";

	request += "typeNames=gmd:MD_Metadata&";

	request += "elementSetName=brief&";

	request += "outputSchema=http://www.isotc211.org/2005/gmd";

	//
	//
	//

	WebRequest webRequest = WebRequest.createGET(request);

	CSWRequestConverter converter = new CSWRequestConverter();

	GetRecords getRecords = converter.convert(webRequest);

	ResultType resultType = getRecords.getResultType();
	assertEquals(ResultType.RESULTS, resultType);

	int startPosition = getRecords.getStartPosition().intValue();
	assertEquals(3, startPosition);

	int maxRecords = getRecords.getMaxRecords().intValue();
	assertEquals(11, maxRecords);

	String outputFormat = getRecords.getOutputFormat();
	assertEquals("xxx/xml", outputFormat);

	String outputSchema = getRecords.getOutputSchema();
	assertEquals("http://www.isotc211.org/2005/gmd", outputSchema);

	String requestId = getRecords.getRequestId();
	assertNull(requestId);

	DistributedSearchType distributedSearch = getRecords.getDistributedSearch();
	assertNull(distributedSearch);

	QueryType queryType = ((QueryType) getRecords.getAbstractQuery().getValue());

	//
	// Type Names
	//

	List<QName> typeNames = queryType.getTypeNames();

	assertEquals(1, typeNames.size());

	assertEquals("gmd", typeNames.get(0).getPrefix());
	assertEquals("MD_Metadata", typeNames.get(0).getLocalPart());

	//
	// ElementSetName
	//

	ElementSetName elementSetName = queryType.getElementSetName();

	ElementSetType value = elementSetName.getValue();
	assertEquals(ElementSetType.BRIEF, value);

	List<QName> elementSetNameTypeNames = elementSetName.getTypeNames();
	assertEquals(1, elementSetNameTypeNames.size());

	assertEquals("gmd", elementSetNameTypeNames.get(0).getPrefix());
	assertEquals("MD_Metadata", elementSetNameTypeNames.get(0).getLocalPart());

	//
	// ElementNames
	//

	List<QName> elementNames = queryType.getElementNames();
	assertEquals(0, elementNames.size());

	//
	// SortBy
	//

	SortByType sortBy = queryType.getSortBy();
	assertNull(sortBy);

	//
	//
	//

	CSWRequestValidator validator = new CSWRequestValidator();
	ValidationMessage message = validator.validate(webRequest);

	List<ValidationException> exceptions = message.getExceptions();
	exceptions.forEach(e -> System.out.println(e.toString()));

	assertEquals(ValidationResult.VALIDATION_FAILED, message.getResult());
    }
    
    @Test
    public void test9() throws Exception {

	String request = "http://localhost:9090/gs-service/services/csw?";

	request += "request=GetRecords&";

	request += "service=CSW&";

	request += "version=2.0.2&";

	request += "startPosition=3&";

	request += "maxRecords=11&";

	request += "outputFormat=text/xml&";

	request += "resultType=results&";

	request += "typeNames=gmd:MD_Metadata&";

	request += "elementSetName=brief&";

	request += "outputSchema=http://www.isotc211.org/2005/gmx";

	//
	//
	//

	WebRequest webRequest = WebRequest.createGET(request);

	CSWRequestConverter converter = new CSWRequestConverter();

	GetRecords getRecords = converter.convert(webRequest);

	ResultType resultType = getRecords.getResultType();
	assertEquals(ResultType.RESULTS, resultType);

	int startPosition = getRecords.getStartPosition().intValue();
	assertEquals(3, startPosition);

	int maxRecords = getRecords.getMaxRecords().intValue();
	assertEquals(11, maxRecords);

	String outputFormat = getRecords.getOutputFormat();
	assertEquals("text/xml", outputFormat);

	String outputSchema = getRecords.getOutputSchema();
	assertEquals("http://www.isotc211.org/2005/gmx", outputSchema);

	String requestId = getRecords.getRequestId();
	assertNull(requestId);

	DistributedSearchType distributedSearch = getRecords.getDistributedSearch();
	assertNull(distributedSearch);

	QueryType queryType = ((QueryType) getRecords.getAbstractQuery().getValue());

	//
	// Type Names
	//

	List<QName> typeNames = queryType.getTypeNames();

	assertEquals(1, typeNames.size());

	assertEquals("gmd", typeNames.get(0).getPrefix());
	assertEquals("MD_Metadata", typeNames.get(0).getLocalPart());

	//
	// ElementSetName
	//

	ElementSetName elementSetName = queryType.getElementSetName();

	ElementSetType value = elementSetName.getValue();
	assertEquals(ElementSetType.BRIEF, value);

	List<QName> elementSetNameTypeNames = elementSetName.getTypeNames();
	assertEquals(1, elementSetNameTypeNames.size());

	assertEquals("gmd", elementSetNameTypeNames.get(0).getPrefix());
	assertEquals("MD_Metadata", elementSetNameTypeNames.get(0).getLocalPart());

	//
	// ElementNames
	//

	List<QName> elementNames = queryType.getElementNames();
	assertEquals(0, elementNames.size());

	//
	// SortBy
	//

	SortByType sortBy = queryType.getSortBy();
	assertNull(sortBy);

	//
	//
	//

	CSWRequestValidator validator = new CSWRequestValidator();
	ValidationMessage message = validator.validate(webRequest);

	List<ValidationException> exceptions = message.getExceptions();
	exceptions.forEach(e -> System.out.println(e.toString()));

	assertEquals(ValidationResult.VALIDATION_FAILED, message.getResult());
    }
    
    @Test
    public void test10() throws Exception {

	String request = "http://localhost:9090/gs-service/services/csw?";

	request += "request=GetRecords&";

	request += "service=CSW&";

	request += "version=2.0.2&";

	request += "startPosition=3&";

	request += "maxRecords=11&";

	request += "outputFormat=text/xml&";

	request += "resultType=results&";

	request += "typeNames=gmi:MI_Metadata&";

	request += "elementSetName=summary&";

	request += "outputSchema=http://www.isotc211.org/2005/gmi";

	//
	//
	//

	WebRequest webRequest = WebRequest.createGET(request);

	CSWRequestConverter converter = new CSWRequestConverter();

	GetRecords getRecords = converter.convert(webRequest);

	ResultType resultType = getRecords.getResultType();
	assertEquals(ResultType.RESULTS, resultType);

	int startPosition = getRecords.getStartPosition().intValue();
	assertEquals(3, startPosition);

	int maxRecords = getRecords.getMaxRecords().intValue();
	assertEquals(11, maxRecords);

	String outputFormat = getRecords.getOutputFormat();
	assertEquals("text/xml", outputFormat);

	String outputSchema = getRecords.getOutputSchema();
	assertEquals("http://www.isotc211.org/2005/gmi", outputSchema);

	String requestId = getRecords.getRequestId();
	assertNull(requestId);

	DistributedSearchType distributedSearch = getRecords.getDistributedSearch();
	assertNull(distributedSearch);

	QueryType queryType = ((QueryType) getRecords.getAbstractQuery().getValue());

	//
	// Type Names
	//

	List<QName> typeNames = queryType.getTypeNames();

	assertEquals(1, typeNames.size());

	assertEquals("gmi", typeNames.get(0).getPrefix());
	assertEquals("MI_Metadata", typeNames.get(0).getLocalPart());

	//
	// ElementSetName
	//

	ElementSetName elementSetName = queryType.getElementSetName();

	ElementSetType value = elementSetName.getValue();
	assertEquals(ElementSetType.SUMMARY, value);

	List<QName> elementSetNameTypeNames = elementSetName.getTypeNames();
	assertEquals(1, elementSetNameTypeNames.size());

	assertEquals("gmi", elementSetNameTypeNames.get(0).getPrefix());
	assertEquals("MI_Metadata", elementSetNameTypeNames.get(0).getLocalPart());

	//
	// ElementNames
	//

	List<QName> elementNames = queryType.getElementNames();
	assertEquals(0, elementNames.size());

	//
	// SortBy
	//

	SortByType sortBy = queryType.getSortBy();
	assertNull(sortBy);

	//
	//
	//

	CSWRequestValidator validator = new CSWRequestValidator();
	ValidationMessage message = validator.validate(webRequest);

	List<ValidationException> exceptions = message.getExceptions();
	exceptions.forEach(e -> System.out.println(e.toString()));

	assertEquals(ValidationResult.VALIDATION_SUCCESSFUL, message.getResult());
    }
}
