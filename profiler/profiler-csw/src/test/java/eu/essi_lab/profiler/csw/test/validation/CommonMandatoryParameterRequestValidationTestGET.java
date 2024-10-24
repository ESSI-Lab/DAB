package eu.essi_lab.profiler.csw.test.validation;

import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.jaxb.csw._2_0_2.ExceptionCode;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.profiler.csw.handler.discover.CSWRequestValidator;
 
public class CommonMandatoryParameterRequestValidationTestGET {

    @Test
    public void wrongRequestTest() {

	CSWRequestValidator validator = new CSWRequestValidator();
	WebRequest request = WebRequest
		.createGET("http://localhost/gs-service/services/essi/cswiso?service=CSW&version=2.0.5&request=GetCapabilities");
	try {
	    ValidationMessage message = validator.validate(request);
	    ValidationResult result = message.getResult();
	    String errorCode = message.getErrorCode();
	    String locator = message.getLocator();

	    Assert.assertEquals(ValidationResult.VALIDATION_FAILED, result);
	    Assert.assertEquals(ExceptionCode.VERSION_NEGOTIAION_FAILED.getCode(), errorCode);
	    Assert.assertEquals("version", locator);

	} catch (GSException e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void wrongRequest1Test() {

	CSWRequestValidator validator = new CSWRequestValidator();
	WebRequest request = WebRequest.createGET("http://localhost/gs-service/services/essi/cswiso?");
	try {
	    ValidationMessage message = validator.validate(request);
	    ValidationResult result = message.getResult();
	    String errorCode = message.getErrorCode();
	    String locator = message.getLocator();

	    Assert.assertEquals(ValidationResult.VALIDATION_FAILED, result);
	    Assert.assertEquals(ExceptionCode.MISSING_PARAMETER.getCode(), errorCode);
	    Assert.assertEquals("service", locator);

	} catch (GSException e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void wrongRequest2Test() {

	CSWRequestValidator validator = new CSWRequestValidator();
	WebRequest request = WebRequest.createGET("http://localhost/gs-service/services/essi/cswiso?service=CSW");
	try {
	    ValidationMessage message = validator.validate(request);
	    ValidationResult result = message.getResult();
	    String errorCode = message.getErrorCode();
	    String locator = message.getLocator();

	    Assert.assertEquals(ValidationResult.VALIDATION_FAILED, result);
	    Assert.assertEquals(ExceptionCode.MISSING_PARAMETER.getCode(), errorCode);
	    Assert.assertEquals("request", locator);

	} catch (GSException e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void wrongRequest3Test() {

	CSWRequestValidator validator = new CSWRequestValidator();
	WebRequest request = WebRequest.createGET("http://localhost/gs-service/services/essi/cswiso?service=CSWX");
	try {
	    ValidationMessage message = validator.validate(request);
	    ValidationResult result = message.getResult();
	    String errorCode = message.getErrorCode();
	    String locator = message.getLocator();

	    Assert.assertEquals(ValidationResult.VALIDATION_FAILED, result);
	    Assert.assertEquals(ExceptionCode.INVALID_PARAMETER.getCode(), errorCode);
	    Assert.assertEquals("service", locator);

	} catch (GSException e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void wrongRequest4Test() {

	CSWRequestValidator validator = new CSWRequestValidator();
	WebRequest request = WebRequest.createGET("http://localhost/gs-service/services/essi/cswiso?service=CSW&version=2.0.2");
	try {
	    ValidationMessage message = validator.validate(request);
	    ValidationResult result = message.getResult();
	    String errorCode = message.getErrorCode();
	    String locator = message.getLocator();

	    Assert.assertEquals(ValidationResult.VALIDATION_FAILED, result);
	    Assert.assertEquals(ExceptionCode.MISSING_PARAMETER.getCode(), errorCode);
	    Assert.assertEquals("request", locator);

	} catch (GSException e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void wrongRequest5Test() {

	CSWRequestValidator validator = new CSWRequestValidator();
	WebRequest request = WebRequest.createGET("http://localhost/gs-service/services/essi/cswiso?service=CSW&version=2.0.2&request=X");
	try {
	    ValidationMessage message = validator.validate(request);
	    ValidationResult result = message.getResult();
	    String errorCode = message.getErrorCode();
	    String locator = message.getLocator();

	    Assert.assertEquals(ValidationResult.VALIDATION_FAILED, result);
	    Assert.assertEquals(ExceptionCode.INVALID_PARAMETER.getCode(), errorCode);
	    Assert.assertEquals("request", locator);

	} catch (GSException e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void wrongRequest6Test() {

	CSWRequestValidator validator = new CSWRequestValidator();

	try {

	    String s = "saddsadsadas";

	    ByteArrayInputStream inputStream = new ByteArrayInputStream(s.getBytes(StandardCharsets.UTF_8));
	    WebRequest request = WebRequest.createPOST("http://localhost/gs-service/services/essi/cswiso?", inputStream);

	    ValidationMessage message = validator.validate(request);
	    ValidationResult result = message.getResult();
	    String errorCode = message.getErrorCode();

	    // System.out.println(message);

	    Assert.assertEquals(ValidationResult.VALIDATION_FAILED, result);
	    Assert.assertEquals(ExceptionCode.NO_APPLICABLE_CODE.getCode(), errorCode);

	} catch (Exception e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void wrongRequest7Test() {

	CSWRequestValidator validator = new CSWRequestValidator();

	try {

	    String s = "<test></test>";

	    ByteArrayInputStream inputStream = new ByteArrayInputStream(s.getBytes(StandardCharsets.UTF_8));
	    WebRequest request = WebRequest.createPOST("http://localhost/gs-service/services/essi/cswiso?", inputStream);

	    ValidationMessage message = validator.validate(request);
	    ValidationResult result = message.getResult();
	    String errorCode = message.getErrorCode();

	    // System.out.println(message);

	    Assert.assertEquals(ValidationResult.VALIDATION_FAILED, result);
	    Assert.assertEquals(ExceptionCode.NO_APPLICABLE_CODE.getCode(), errorCode);

	} catch (Exception e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

}
