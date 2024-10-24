package eu.essi_lab.profiler.csw.test.getrecbyid.validation;

import static org.junit.Assert.fail;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.jaxb.csw._2_0_2.ExceptionCode;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.profiler.csw.handler.discover.CSWRequestValidator;
 
public class GetRecordByIdValidationTestGET {

    @Test
    public void wrongRequest1Test() {

	CSWRequestValidator validator = new CSWRequestValidator();
	WebRequest request = WebRequest
		.createGET("http://localhost/gs-service/services/essi/cswiso?service=CSW&version=2.0.2&request=GetRecordById");
	try {
	    ValidationMessage message = validator.validate(request);
	    ValidationResult result = message.getResult();
	    String errorCode = message.getErrorCode();
	    String locator = message.getLocator();

	    Assert.assertEquals(ValidationResult.VALIDATION_FAILED, result);
	    Assert.assertEquals(ExceptionCode.MISSING_PARAMETER.getCode(), errorCode);
	    Assert.assertEquals("id", locator);

	} catch (GSException e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void wrongRequest2Test() {

	CSWRequestValidator validator = new CSWRequestValidator();
	WebRequest request = WebRequest
		.createGET("http://localhost/gs-service/services/essi/cswiso?service=CSW&version=2.0.2&request=GetRecordById&id=");
	try {
	    ValidationMessage message = validator.validate(request);
	    ValidationResult result = message.getResult();
	    String errorCode = message.getErrorCode();
	    String locator = message.getLocator();

	    Assert.assertEquals(ValidationResult.VALIDATION_FAILED, result);
	    Assert.assertEquals(ExceptionCode.MISSING_PARAMETER.getCode(), errorCode);
	    Assert.assertEquals("id", locator);

	} catch (GSException e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void wrongRequest3Test() {

	CSWRequestValidator validator = new CSWRequestValidator();
	WebRequest request = WebRequest.createGET(
		"http://localhost/gs-service/services/essi/cswiso?service=CSW&version=2.0.2&request=GetRecordById&id=X&outputFormat=X");
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
    public void wrongRequest4Test() {

	CSWRequestValidator validator = new CSWRequestValidator();
	WebRequest request = WebRequest.createGET(
		"http://localhost/gs-service/services/essi/cswiso?service=CSW&version=2.0.2&request=GetRecordById&id=X&outputSchema=X");
	try {
	    ValidationMessage message = validator.validate(request);
	    ValidationResult result = message.getResult();
	    String errorCode = message.getErrorCode();
	    String locator = message.getLocator();

	    Assert.assertEquals(ValidationResult.VALIDATION_FAILED, result);
	    Assert.assertEquals(ExceptionCode.INVALID_PARAMETER.getCode(), errorCode);
	    Assert.assertEquals("outputSchema", locator);

	} catch (GSException e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void wrongRequest5Test() {

	CSWRequestValidator validator = new CSWRequestValidator();
	WebRequest request = WebRequest.createGET(
		"http://localhost/gs-service/services/essi/cswiso?service=CSW&version=2.0.2&request=GetRecordById&id=X&ElementSetName=X");
	try {
	    ValidationMessage message = validator.validate(request);
	    ValidationResult result = message.getResult();
	    String errorCode = message.getErrorCode();
	    String locator = message.getLocator();

	    Assert.assertEquals(ValidationResult.VALIDATION_FAILED, result);
	    Assert.assertEquals(ExceptionCode.INVALID_PARAMETER.getCode(), errorCode);
	    Assert.assertEquals("ElementSetName", locator);

	} catch (GSException e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void correctRequestTest() {

	CSWRequestValidator validator = new CSWRequestValidator();
	WebRequest request = WebRequest.createGET(
		"http://localhost/gs-service/services/essi/cswiso?service=CSW&version=2.0.2&request=GetRecordById&id=X&outputFormat=text/xml&outputSchema=http://www.opengis.net/cat/csw/2.0.2&ElementSetName=full");
	try {
	    ValidationMessage message = validator.validate(request);
	    ValidationResult result = message.getResult();

	    Assert.assertEquals(ValidationResult.VALIDATION_SUCCESSFUL, result);

	} catch (GSException e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }
}
