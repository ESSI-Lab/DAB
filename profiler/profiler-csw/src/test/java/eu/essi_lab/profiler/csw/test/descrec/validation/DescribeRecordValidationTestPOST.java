package eu.essi_lab.profiler.csw.test.descrec.validation;

import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;

import javax.xml.namespace.QName;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.jaxb.common.CommonContext;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.jaxb.csw._2_0_2.DescribeRecord;
import eu.essi_lab.jaxb.csw._2_0_2.ExceptionCode;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.profiler.csw.handler.discover.CSWRequestValidator;

public class DescribeRecordValidationTestPOST {

    @Test
    public void wrongRequest1Test() {

	CSWRequestValidator validator = new CSWRequestValidator();
	DescribeRecord describeRecord = new DescribeRecord();
	describeRecord.setSchemaLanguage("X");

	try {
	    WebRequest request = createWEBRequest(true, describeRecord);

	    ValidationMessage message = validator.validate(request);
	    ValidationResult result = message.getResult();
	    String errorCode = message.getErrorCode();
	    String locator = message.getLocator();

	    // System.out.println(message);

	    Assert.assertEquals(ValidationResult.VALIDATION_FAILED, result);
	    Assert.assertEquals(ExceptionCode.INVALID_PARAMETER.getCode(), errorCode);
	    Assert.assertEquals("schemaLanguage", locator);

	} catch (Exception e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void wrongRequest3Test() {

	CSWRequestValidator validator = new CSWRequestValidator();
	DescribeRecord describeRecord = new DescribeRecord();
	describeRecord.getTypeNames().add(new QName("X"));

	try {
	    WebRequest request = createWEBRequest(true, describeRecord);

	    ValidationMessage message = validator.validate(request);
	    ValidationResult result = message.getResult();
	    String errorCode = message.getErrorCode();
	    String locator = message.getLocator();

	    // System.out.println(message);

	    Assert.assertEquals(ValidationResult.VALIDATION_FAILED, result);
	    Assert.assertEquals(ExceptionCode.INVALID_PARAMETER.getCode(), errorCode);
	    Assert.assertEquals("TypeName", locator);

	} catch (Exception e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void wrongRequest4Test() {

	CSWRequestValidator validator = new CSWRequestValidator();
	DescribeRecord describeRecord = new DescribeRecord();
	// describeRecord.getTypeNames().add(new QName("MD_Metadata"));

	describeRecord.getTypeNames().add(new QName(CommonNameSpaceContext.GMD_NS_URI, "gmd:MD_Metadata", "ss"));

	try {
	    WebRequest request = createWEBRequest(false, describeRecord);

	    ValidationMessage message = validator.validate(request);
	    ValidationResult result = message.getResult();
	    String errorCode = message.getErrorCode();
	    String locator = message.getLocator();

	    // System.out.println(message);

	    Assert.assertEquals(ValidationResult.VALIDATION_FAILED, result);
	    Assert.assertEquals(ExceptionCode.INVALID_PARAMETER.getCode(), errorCode);
	    Assert.assertEquals("TypeName", locator);

	} catch (Exception e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void wrongRequest5Test() {

	CSWRequestValidator validator = new CSWRequestValidator();
	DescribeRecord describeRecord = new DescribeRecord();
	describeRecord.getTypeNames().add(new QName("MD_Metadata"));

	try {
	    WebRequest request = createWEBRequest(false, describeRecord);

	    ValidationMessage message = validator.validate(request);
	    ValidationResult result = message.getResult();
	    String errorCode = message.getErrorCode();
	    String locator = message.getLocator();

	    // System.out.println(message);

	    Assert.assertEquals(ValidationResult.VALIDATION_FAILED, result);
	    Assert.assertEquals(ExceptionCode.INVALID_PARAMETER.getCode(), errorCode);
	    Assert.assertEquals("TypeName", locator);

	} catch (Exception e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void wrongRequest6Test() {

	CSWRequestValidator validator = new CSWRequestValidator();
	DescribeRecord describeRecord = new DescribeRecord();
	describeRecord.getTypeNames().add(new QName(CommonNameSpaceContext.GMD_NS_URI, "gmd:MD_Metadata", "ss"));

	try {
	    WebRequest request = createWEBRequest(true, describeRecord);

	    ValidationMessage message = validator.validate(request);
	    ValidationResult result = message.getResult();
	    String errorCode = message.getErrorCode();
	    String locator = message.getLocator();

	    // System.out.println(message);

	    Assert.assertEquals(ValidationResult.VALIDATION_FAILED, result);
	    Assert.assertEquals(ExceptionCode.INVALID_PARAMETER.getCode(), errorCode);
	    Assert.assertEquals("TypeName", locator);

	} catch (Exception e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void wrongRequest7Test() {

	CSWRequestValidator validator = new CSWRequestValidator();
	DescribeRecord describeRecord = new DescribeRecord();
	describeRecord.getTypeNames().add(new QName("MD_Metadata"));

	try {
	    WebRequest request = createWEBRequest(true, describeRecord);

	    ValidationMessage message = validator.validate(request);
	    ValidationResult result = message.getResult();
	    String errorCode = message.getErrorCode();
	    String locator = message.getLocator();

	    // System.out.println(message);

	    Assert.assertEquals(ValidationResult.VALIDATION_FAILED, result);
	    Assert.assertEquals(ExceptionCode.INVALID_PARAMETER.getCode(), errorCode);
	    Assert.assertEquals("TypeName", locator);

	} catch (Exception e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void wrongRequest8Test() {

	CSWRequestValidator validator = new CSWRequestValidator();
	DescribeRecord describeRecord = new DescribeRecord();
	describeRecord.setOutputFormat("X");

	try {
	    WebRequest request = createWEBRequest(true, describeRecord);

	    ValidationMessage message = validator.validate(request);
	    ValidationResult result = message.getResult();
	    String errorCode = message.getErrorCode();
	    String locator = message.getLocator();

	    // System.out.println(message);

	    Assert.assertEquals(ValidationResult.VALIDATION_FAILED, result);
	    Assert.assertEquals(ExceptionCode.INVALID_PARAMETER.getCode(), errorCode);
	    Assert.assertEquals("outputFormat", locator);

	} catch (Exception e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void wrongRequest9Test() {

	CSWRequestValidator validator = new CSWRequestValidator();
	DescribeRecord describeRecord = new DescribeRecord();
	describeRecord.setOutputFormat("X");

	try {
	    WebRequest request = createWEBRequest(false, describeRecord);

	    ValidationMessage message = validator.validate(request);
	    ValidationResult result = message.getResult();
	    String errorCode = message.getErrorCode();
	    String locator = message.getLocator();

	    // System.out.println(message);

	    Assert.assertEquals(ValidationResult.VALIDATION_FAILED, result);
	    Assert.assertEquals(ExceptionCode.INVALID_PARAMETER.getCode(), errorCode);
	    Assert.assertEquals("outputFormat", locator);

	} catch (Exception e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void correctRequest1Test() {

	CSWRequestValidator validator = new CSWRequestValidator();
	DescribeRecord describeRecord = new DescribeRecord();
	describeRecord.setOutputFormat("text/xml");

	try {
	    WebRequest request = createWEBRequest(false, describeRecord);

	    ValidationMessage message = validator.validate(request);
	    ValidationResult result = message.getResult();
	    String errorCode = message.getErrorCode();
	    String locator = message.getLocator();

	    // System.out.println(message);

	    Assert.assertEquals(ValidationResult.VALIDATION_SUCCESSFUL, result);
	    Assert.assertEquals(null, errorCode);
	    Assert.assertEquals(null, locator);

	} catch (Exception e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void correctRequest2Test() {

	CSWRequestValidator validator = new CSWRequestValidator();
	DescribeRecord describeRecord = new DescribeRecord();
	describeRecord.setOutputFormat("text/xml");

	try {
	    WebRequest request = createWEBRequest(true, describeRecord);

	    ValidationMessage message = validator.validate(request);
	    ValidationResult result = message.getResult();
	    String errorCode = message.getErrorCode();
	    String locator = message.getLocator();

	    // System.out.println(message);

	    Assert.assertEquals(ValidationResult.VALIDATION_SUCCESSFUL, result);
	    Assert.assertEquals(null, errorCode);
	    Assert.assertEquals(null, locator);

	} catch (Exception e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void correctRequest3Test() {

	CSWRequestValidator validator = new CSWRequestValidator();
	DescribeRecord describeRecord = new DescribeRecord();
	describeRecord.setOutputFormat("application/xml");

	try {
	    WebRequest request = createWEBRequest(true, describeRecord);

	    ValidationMessage message = validator.validate(request);
	    ValidationResult result = message.getResult();
	    String errorCode = message.getErrorCode();
	    String locator = message.getLocator();

	    // System.out.println(message);

	    Assert.assertEquals(ValidationResult.VALIDATION_SUCCESSFUL, result);
	    Assert.assertEquals(null, errorCode);
	    Assert.assertEquals(null, locator);

	} catch (Exception e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void correctRequest4Test() {

	CSWRequestValidator validator = new CSWRequestValidator();
	DescribeRecord describeRecord = new DescribeRecord();
	describeRecord.setSchemaLanguage("XMLSCHEMA");

	try {
	    WebRequest request = createWEBRequest(true, describeRecord);

	    ValidationMessage message = validator.validate(request);
	    ValidationResult result = message.getResult();
	    String errorCode = message.getErrorCode();
	    String locator = message.getLocator();

	    // System.out.println(message);

	    Assert.assertEquals(ValidationResult.VALIDATION_SUCCESSFUL, result);
	    Assert.assertEquals(null, errorCode);
	    Assert.assertEquals(null, locator);

	} catch (Exception e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void correctRequest5Test() {

	CSWRequestValidator validator = new CSWRequestValidator();
	DescribeRecord describeRecord = new DescribeRecord();
	describeRecord.setSchemaLanguage("http://www.w3.org/XML/Schema");

	try {
	    WebRequest request = createWEBRequest(true, describeRecord);

	    ValidationMessage message = validator.validate(request);
	    ValidationResult result = message.getResult();
	    String errorCode = message.getErrorCode();
	    String locator = message.getLocator();

	    // System.out.println(message);

	    Assert.assertEquals(ValidationResult.VALIDATION_SUCCESSFUL, result);
	    Assert.assertEquals(null, errorCode);
	    Assert.assertEquals(null, locator);

	} catch (Exception e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void correctRequest6Test() {

	CSWRequestValidator validator = new CSWRequestValidator();
	DescribeRecord describeRecord = new DescribeRecord();
	describeRecord.setSchemaLanguage("http://www.w3.org/2001/XMLSchema");

	try {
	    WebRequest request = createWEBRequest(true, describeRecord);

	    ValidationMessage message = validator.validate(request);
	    ValidationResult result = message.getResult();
	    String errorCode = message.getErrorCode();
	    String locator = message.getLocator();

	    // System.out.println(message);

	    Assert.assertEquals(ValidationResult.VALIDATION_SUCCESSFUL, result);
	    Assert.assertEquals(null, errorCode);
	    Assert.assertEquals(null, locator);

	} catch (Exception e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void correctRequest7Test() {

	CSWRequestValidator validator = new CSWRequestValidator();
	DescribeRecord describeRecord = new DescribeRecord();

	describeRecord.getTypeNames().add(new QName(CommonNameSpaceContext.GMD_NS_URI, "MD_Metadata", "gmd"));

	try {
	    WebRequest request = createWEBRequest(true, describeRecord);

	    ValidationMessage message = validator.validate(request);
	    ValidationResult result = message.getResult();
	    String errorCode = message.getErrorCode();
	    String locator = message.getLocator();

	    Assert.assertEquals(ValidationResult.VALIDATION_SUCCESSFUL, result);
	    Assert.assertEquals(null, errorCode);
	    Assert.assertEquals(null, locator);

	} catch (Exception e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void correctRequest8Test() {

	CSWRequestValidator validator = new CSWRequestValidator();
	DescribeRecord describeRecord = new DescribeRecord();

	describeRecord.getTypeNames().add(new QName(CommonNameSpaceContext.GMD_NS_URI, "MD_Metadata", "gmd"));
	describeRecord.getTypeNames().add(new QName(CommonNameSpaceContext.GMI_NS_URI, "MI_Metadata", "gmi"));
	describeRecord.getTypeNames().add(new QName(CommonNameSpaceContext.CSW_NS_URI, "Record", "csw"));

	try {
	    WebRequest request = createWEBRequest(true, describeRecord);

	    ValidationMessage message = validator.validate(request);
	    ValidationResult result = message.getResult();
	    String errorCode = message.getErrorCode();
	    String locator = message.getLocator();

	    Assert.assertEquals(ValidationResult.VALIDATION_SUCCESSFUL, result);
	    Assert.assertEquals(null, errorCode);
	    Assert.assertEquals(null, locator);

	} catch (Exception e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void correctRequest9Test() {

	CSWRequestValidator validator = new CSWRequestValidator();
	DescribeRecord describeRecord = new DescribeRecord();

	describeRecord.getTypeNames().add(new QName(CommonNameSpaceContext.CSW_NS_URI, "Record", "csw"));

	try {
	    WebRequest request = createWEBRequest(false, describeRecord);

	    ValidationMessage message = validator.validate(request);
	    ValidationResult result = message.getResult();
	    String errorCode = message.getErrorCode();
	    String locator = message.getLocator();

	    Assert.assertEquals(ValidationResult.VALIDATION_SUCCESSFUL, result);
	    Assert.assertEquals(null, errorCode);
	    Assert.assertEquals(null, locator);

	} catch (Exception e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void correctRequest10Test() {

	CSWRequestValidator validator = new CSWRequestValidator();
	DescribeRecord describeRecord = new DescribeRecord();

	describeRecord.getTypeNames().add(new QName(CommonNameSpaceContext.CSW_NS_URI, "Record", "csw"));
	describeRecord.setOutputFormat("text/xml");
	describeRecord.setSchemaLanguage("http://www.w3.org/2001/XMLSchema");

	try {
	    WebRequest request = createWEBRequest(false, describeRecord);

	    ValidationMessage message = validator.validate(request);
	    ValidationResult result = message.getResult();
	    String errorCode = message.getErrorCode();
	    String locator = message.getLocator();

	    Assert.assertEquals(ValidationResult.VALIDATION_SUCCESSFUL, result);
	    Assert.assertEquals(null, errorCode);
	    Assert.assertEquals(null, locator);

	} catch (Exception e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    private WebRequest createWEBRequest(boolean iso, DescribeRecord descRec) throws Exception {

	// Marshaller marshaller = CommonContext.createMarshaller(true);
	//
	// ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	// marshaller.marshal(descRec, outputStream);

	ByteArrayInputStream inputStream = CommonContext.asInputStream(descRec, true);
	return WebRequest.createPOST("http://localhost/gs-service/services/essi/csw?", inputStream);
    }

    @SuppressWarnings("unused")
    private void printDescribeRecord(DescribeRecord descRec) throws Exception {

	System.out.println(CommonContext.asString(descRec, true));
    }
}
