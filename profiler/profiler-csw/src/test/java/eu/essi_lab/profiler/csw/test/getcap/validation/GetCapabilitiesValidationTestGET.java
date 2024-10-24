package eu.essi_lab.profiler.csw.test.getcap.validation;

import static org.junit.Assert.fail;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.jaxb.csw._2_0_2.ExceptionCode;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.profiler.csw.handler.discover.CSWRequestValidator;
 
public class GetCapabilitiesValidationTestGET {

    @Test
    public void wrongRequest4Test() {

	CSWRequestValidator validator = new CSWRequestValidator();
	WebRequest request = WebRequest
		.createGET("http://localhost/gs-service/services/essi/cswiso?service=CSW&version=2.0.2&request=GetCapabilities&sections=X");
	try {
	    ValidationMessage message = validator.validate(request);
	    ValidationResult result = message.getResult();
	    String errorCode = message.getErrorCode();
	    String locator = message.getLocator();

	    Assert.assertEquals(ValidationResult.VALIDATION_FAILED, result);
	    Assert.assertEquals(ExceptionCode.INVALID_PARAMETER.getCode(), errorCode);
	    Assert.assertEquals("Sections", locator);

	} catch (GSException e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void wrongRequest5Test() {

	CSWRequestValidator validator = new CSWRequestValidator();
	WebRequest request = WebRequest.createGET(
		"http://localhost/gs-service/services/essi/cswiso?service=CSW&version=2.0.2&request=GetCapabilities&acceptVersions=2.0.3");
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
    public void correctRequestTest0() {

	CSWRequestValidator validator = new CSWRequestValidator();
	WebRequest request = WebRequest.createGET(
		"http://localhost/gs-service/services/essi/cswiso?service=CSW&version=2.0.2&request=GetCapabilities&AcceptFormats=X");
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
    public void correctRequestTest1() {

	CSWRequestValidator validator = new CSWRequestValidator();
	WebRequest request = WebRequest
		.createGET("http://localhost/gs-service/services/essi/cswiso?service=CSW&version=2.0.2&request=GetCapabilities");
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
    public void correctRequestTest2() {

	CSWRequestValidator validator = new CSWRequestValidator();
	WebRequest request = WebRequest
		.createGET("http://localhost/gs-service/services/essi/cswiso?service=CSW&version=2.0.2&request=getCapaBiliTies");
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
    public void correctRequestTest3() {

	CSWRequestValidator validator = new CSWRequestValidator();
	WebRequest request = WebRequest
		.createGET("http://localhost/gs-service/services/essi/cswiso?service=CSW&vErsIon=2.0.2&request=GetCapabilities");
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
    public void correctRequestTest4() {

	CSWRequestValidator validator = new CSWRequestValidator();
	WebRequest request = WebRequest
		.createGET("http://localhost/gs-service/services/essi/cswiso?serViCe=CSW&version=2.0.2&request=GetCapabilities");
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
    public void correctRequestTest5() {

	CSWRequestValidator validator = new CSWRequestValidator();
	WebRequest request = WebRequest.createGET(
		"http://localhost/gs-service/services/essi/cswiso?service=CSW&version=2.0.2&request=GetCapabilities&sections=ServiceIdentification,ServiceProvider");
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
    public void correctRequestTest6() {

	CSWRequestValidator validator = new CSWRequestValidator();
	WebRequest request = WebRequest.createGET(
		"http://localhost/gs-service/services/essi/cswiso?service=CSW&version=2.0.2&request=GetCapabilities&acceptVersions=2.0.2,2.0.3");
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
    public void correctRequestTest7() {

	CSWRequestValidator validator = new CSWRequestValidator();
	WebRequest request = WebRequest.createGET(
		"http://localhost/gs-service/services/essi/cswiso?service=CSW&version=2.0.2&request=GetCapabilities&acceptVersions=2.0.2");
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
    public void correctRequestTest8() {

	CSWRequestValidator validator = new CSWRequestValidator();
	WebRequest request = WebRequest.createGET(
		"http://localhost/gs-service/services/essi/cswiso?service=CSW&version=2.0.2&request=GetCapabilities&AcceptFormats=application/xml");
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
    public void correctRequestTest9() {

	CSWRequestValidator validator = new CSWRequestValidator();
	WebRequest request = WebRequest.createGET(
		"http://localhost/gs-service/services/essi/cswiso?service=CSW&version=2.0.2&request=GetCapabilities&AcceptFormats=text/xml");
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
    public void correctRequestTest10() {

	CSWRequestValidator validator = new CSWRequestValidator();
	WebRequest request = WebRequest.createGET(
		"http://localhost/gs-service/services/essi/cswiso?service=CSW&version=2.0.2&request=GetCapabilities&AcceptFormats=text/xml,application/xml");
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
    public void correctRequestTest11() {

	CSWRequestValidator validator = new CSWRequestValidator();
	WebRequest request = WebRequest.createGET(
		"http://localhost/gs-service/services/essi/cswiso?service=CSW&version=2.0.2&request=GetCapabilities&AcceptFormats=text/xml,application/xml,text/csv");
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
    public void correctRequestTest12() {

	CSWRequestValidator validator = new CSWRequestValidator();
	WebRequest request = WebRequest.createGET(
		"http://localhost/gs-service/services/essi/cswiso?service=CSW&version=2.0.2&request=GetCapabilities&AcceptFormats=text/xml,application/xml,text/csv&acceptVersions=2.0.2&sections=ServiceIdentification,ServiceProvider");
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
