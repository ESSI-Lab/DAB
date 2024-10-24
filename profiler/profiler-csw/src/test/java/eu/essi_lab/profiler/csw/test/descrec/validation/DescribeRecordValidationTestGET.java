package eu.essi_lab.profiler.csw.test.descrec.validation;

import static org.junit.Assert.fail;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.jaxb.csw._2_0_2.ExceptionCode;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.profiler.csw.handler.discover.CSWRequestValidator;

public class DescribeRecordValidationTestGET {

    @Test
    public void wrongRequest1Test() {

	CSWRequestValidator validator = new CSWRequestValidator();
	WebRequest request = WebRequest.createGET(
		"http://localhost/gs-service/services/essi/cswiso?request=DescribeRecord&service=CSW&version=2.0.2&outputFormat=X");
	try {
	    ValidationMessage message = validator.validate(request);
	    ValidationResult result = message.getResult();
	    String errorCode = message.getErrorCode();
	    String locator = message.getLocator();

	    Assert.assertEquals(ValidationResult.VALIDATION_FAILED, result);
	    Assert.assertEquals(ExceptionCode.INVALID_PARAMETER.getCode(), errorCode);
	    Assert.assertEquals("outputFormat", locator);

	} catch (GSException e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void wrongRequest2Test() {

	CSWRequestValidator validator = new CSWRequestValidator();
	WebRequest request = WebRequest.createGET(
		"http://localhost/gs-service/services/essi/cswiso?request=DescribeRecord&service=CSW&version=2.0.2&schemaLanguage=X");
	try {
	    ValidationMessage message = validator.validate(request);
	    ValidationResult result = message.getResult();
	    String errorCode = message.getErrorCode();
	    String locator = message.getLocator();

	    Assert.assertEquals(ValidationResult.VALIDATION_FAILED, result);
	    Assert.assertEquals(ExceptionCode.INVALID_PARAMETER.getCode(), errorCode);
	    Assert.assertEquals("schemaLanguage", locator);

	} catch (GSException e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void wrongRequest3Test() {

	CSWRequestValidator validator = new CSWRequestValidator();
	WebRequest request = WebRequest
		.createGET("http://localhost/gs-service/services/essi/cswiso?request=DescribeRecord&service=CSW&version=2.0.2&TypeName=X");
	try {
	    ValidationMessage message = validator.validate(request);
	    ValidationResult result = message.getResult();
	    String errorCode = message.getErrorCode();
	    String locator = message.getLocator();

	    Assert.assertEquals(ValidationResult.VALIDATION_FAILED, result);
	    Assert.assertEquals(ExceptionCode.INVALID_PARAMETER.getCode(), errorCode);
	    Assert.assertEquals("TypeName", locator);

	} catch (GSException e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void correctRequest1Test() {

	CSWRequestValidator validator = new CSWRequestValidator();
	WebRequest request = WebRequest
		.createGET("http://localhost/gs-service/services/essi/cswiso?request=DescribeRecord&service=CSW&version=2.0.2");
	try {
	    ValidationMessage message = validator.validate(request);
	    ValidationResult result = message.getResult();
	    String errorCode = message.getErrorCode();
	    String locator = message.getLocator();

	    Assert.assertEquals(ValidationResult.VALIDATION_SUCCESSFUL, result);
	    Assert.assertEquals(null, errorCode);
	    Assert.assertEquals(null, locator);

	} catch (GSException e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void correctRequest2Test() {

	CSWRequestValidator validator = new CSWRequestValidator();
	WebRequest request = WebRequest.createGET(
		"http://localhost/gs-service/services/essi/cswiso?request=DescribeRecord&service=CSW&version=2.0.2&outputFormat=text/xml");
	try {
	    ValidationMessage message = validator.validate(request);
	    ValidationResult result = message.getResult();
	    String errorCode = message.getErrorCode();
	    String locator = message.getLocator();

	    Assert.assertEquals(ValidationResult.VALIDATION_SUCCESSFUL, result);
	    Assert.assertEquals(null, errorCode);
	    Assert.assertEquals(null, locator);

	} catch (GSException e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void correctRequest3Test() {

	CSWRequestValidator validator = new CSWRequestValidator();
	WebRequest request = WebRequest.createGET(
		"http://localhost/gs-service/services/essi/cswiso?request=DescribeRecord&service=CSW&version=2.0.2&outputFormat=text/xml");
	try {
	    ValidationMessage message = validator.validate(request);
	    ValidationResult result = message.getResult();
	    String errorCode = message.getErrorCode();
	    String locator = message.getLocator();

	    Assert.assertEquals(ValidationResult.VALIDATION_SUCCESSFUL, result);
	    Assert.assertEquals(null, errorCode);
	    Assert.assertEquals(null, locator);

	} catch (GSException e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void correctRequest4Test() {

	CSWRequestValidator validator = new CSWRequestValidator();
	WebRequest request = WebRequest.createGET(
		"http://localhost/gs-service/services/essi/cswiso?request=DescribeRecord&service=CSW&version=2.0.2&outputFormat=application/xml");
	try {
	    ValidationMessage message = validator.validate(request);
	    ValidationResult result = message.getResult();
	    String errorCode = message.getErrorCode();
	    String locator = message.getLocator();

	    Assert.assertEquals(ValidationResult.VALIDATION_SUCCESSFUL, result);
	    Assert.assertEquals(null, errorCode);
	    Assert.assertEquals(null, locator);

	} catch (GSException e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void correctRequest5Test() {

	CSWRequestValidator validator = new CSWRequestValidator();
	WebRequest request = WebRequest.createGET(
		"http://localhost/gs-service/services/essi/cswiso?request=DescribeRecord&service=CSW&version=2.0.2&schemaLanguage=XMLSCHEMA");
	try {
	    ValidationMessage message = validator.validate(request);
	    ValidationResult result = message.getResult();
	    String errorCode = message.getErrorCode();
	    String locator = message.getLocator();

	    Assert.assertEquals(ValidationResult.VALIDATION_SUCCESSFUL, result);
	    Assert.assertEquals(null, errorCode);
	    Assert.assertEquals(null, locator);

	} catch (GSException e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void correctRequest6Test() {

	CSWRequestValidator validator = new CSWRequestValidator();
	WebRequest request = WebRequest.createGET(
		"http://localhost/gs-service/services/essi/cswiso?request=DescribeRecord&service=CSW&version=2.0.2&schemaLanguage=http://www.w3.org/XML/Schema");
	try {
	    ValidationMessage message = validator.validate(request);
	    ValidationResult result = message.getResult();
	    String errorCode = message.getErrorCode();
	    String locator = message.getLocator();

	    Assert.assertEquals(ValidationResult.VALIDATION_SUCCESSFUL, result);
	    Assert.assertEquals(null, errorCode);
	    Assert.assertEquals(null, locator);

	} catch (GSException e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void correctRequest7Test() {

	CSWRequestValidator validator = new CSWRequestValidator();
	WebRequest request = WebRequest.createGET(
		"http://localhost/gs-service/services/essi/cswiso?request=DescribeRecord&service=CSW&version=2.0.2&schemaLanguage=http://www.w3.org/2001/XMLSchema");
	try {
	    ValidationMessage message = validator.validate(request);
	    ValidationResult result = message.getResult();
	    String errorCode = message.getErrorCode();
	    String locator = message.getLocator();

	    Assert.assertEquals(ValidationResult.VALIDATION_SUCCESSFUL, result);
	    Assert.assertEquals(null, errorCode);
	    Assert.assertEquals(null, locator);

	} catch (GSException e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void correctRequest8Test() {

	CSWRequestValidator validator = new CSWRequestValidator();
	WebRequest request = WebRequest.createGET(
		"http://localhost/gs-service/services/essi/cswiso?request=DescribeRecord&service=CSW&version=2.0.2&TypeName=gmi:MI_Metadata,gmd:MD_Metadata");
	try {
	    ValidationMessage message = validator.validate(request);
	    ValidationResult result = message.getResult();
	    String errorCode = message.getErrorCode();
	    String locator = message.getLocator();

	    Assert.assertEquals(ValidationResult.VALIDATION_SUCCESSFUL, result);
	    Assert.assertEquals(null, errorCode);
	    Assert.assertEquals(null, locator);

	} catch (GSException e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void correctRequest9Test() {

	CSWRequestValidator validator = new CSWRequestValidator();
	WebRequest request = WebRequest.createGET(
		"http://localhost/gs-service/services/essi/cswiso?request=DescribeRecord&service=CSW&version=2.0.2&TypeName=gmd:MD_Metadata");
	try {
	    ValidationMessage message = validator.validate(request);
	    ValidationResult result = message.getResult();
	    String errorCode = message.getErrorCode();
	    String locator = message.getLocator();

	    Assert.assertEquals(ValidationResult.VALIDATION_SUCCESSFUL, result);
	    Assert.assertEquals(null, errorCode);
	    Assert.assertEquals(null, locator);

	} catch (GSException e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void correctRequest10Test() {

	CSWRequestValidator validator = new CSWRequestValidator();
	WebRequest request = WebRequest.createGET(
		"http://localhost/gs-service/services/essi/cswiso?request=DescribeRecord&service=CSW&version=2.0.2&TypeName=gmi:MI_Metadata");
	try {
	    ValidationMessage message = validator.validate(request);
	    ValidationResult result = message.getResult();
	    String errorCode = message.getErrorCode();
	    String locator = message.getLocator();

	    Assert.assertEquals(ValidationResult.VALIDATION_SUCCESSFUL, result);
	    Assert.assertEquals(null, errorCode);
	    Assert.assertEquals(null, locator);

	} catch (GSException e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void correctRequest11Test() {

	CSWRequestValidator validator = new CSWRequestValidator();
	WebRequest request = WebRequest.createGET(
		"http://localhost/gs-service/services/essi/csw?request=DescribeRecord&service=CSW&version=2.0.2&TypeName=csw:Record");
	try {
	    ValidationMessage message = validator.validate(request);
	    ValidationResult result = message.getResult();
	    String errorCode = message.getErrorCode();
	    String locator = message.getLocator();

	    Assert.assertEquals(ValidationResult.VALIDATION_SUCCESSFUL, result);
	    Assert.assertEquals(null, errorCode);
	    Assert.assertEquals(null, locator);

	} catch (GSException e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void correctRequest12Test() {

	CSWRequestValidator validator = new CSWRequestValidator();
	WebRequest request = WebRequest.createGET(
		"http://localhost/gs-service/services/essi/csw?request=DescribeRecord&service=CSW&version=2.0.2&TypeName=gmi:MI_Metadata&schemaLanguage=http://www.w3.org/2001/XMLSchema&outputFormat=application/xml");
	try {
	    ValidationMessage message = validator.validate(request);
	    ValidationResult result = message.getResult();
	    String errorCode = message.getErrorCode();
	    String locator = message.getLocator();

	    Assert.assertEquals(ValidationResult.VALIDATION_SUCCESSFUL, result);
	    Assert.assertEquals(null, errorCode);
	    Assert.assertEquals(null, locator);

	} catch (GSException e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }
}
