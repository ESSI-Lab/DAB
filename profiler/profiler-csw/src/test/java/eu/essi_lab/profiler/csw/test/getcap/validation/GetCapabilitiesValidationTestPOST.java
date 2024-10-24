package eu.essi_lab.profiler.csw.test.getcap.validation;

import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.jaxb.common.CommonContext;
import eu.essi_lab.jaxb.csw._2_0_2.ExceptionCode;
import eu.essi_lab.jaxb.csw._2_0_2.GetCapabilities;
import eu.essi_lab.jaxb.ows._1_0_0.AcceptFormatsType;
import eu.essi_lab.jaxb.ows._1_0_0.AcceptVersionsType;
import eu.essi_lab.jaxb.ows._1_0_0.SectionsType;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.profiler.csw.handler.discover.CSWRequestValidator;
 
public class GetCapabilitiesValidationTestPOST {

    @Test
    public void wrongRequest1Test() {

	CSWRequestValidator validator = new CSWRequestValidator();
	GetCapabilities getCapabilities = new GetCapabilities();

	try {
	    WebRequest request = createWEBRequest(getCapabilities);

	    ValidationMessage message = validator.validate(request);
	    ValidationResult result = message.getResult();
	    String errorCode = message.getErrorCode();
	    String locator = message.getLocator();

	    // System.out.println(message);
	    // printGetCapabilities(getCapabilities);

	    Assert.assertEquals(ValidationResult.VALIDATION_FAILED, result);
	    Assert.assertEquals(ExceptionCode.MISSING_PARAMETER.getCode(), errorCode);
	    Assert.assertEquals("service", locator);

	} catch (Exception e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void wrongRequest2Test() {

	CSWRequestValidator validator = new CSWRequestValidator();
	GetCapabilities getCapabilities = new GetCapabilities();
	getCapabilities.setService("CSW");
	getCapabilities.setSections(new SectionsType());
	getCapabilities.getSections().getSection().add("X");

	try {
	    WebRequest request = createWEBRequest(getCapabilities);

	    ValidationMessage message = validator.validate(request);

	    // System.out.println(message);

	    ValidationResult result = message.getResult();
	    String errorCode = message.getErrorCode();
	    String locator = message.getLocator();

	    Assert.assertEquals(ValidationResult.VALIDATION_FAILED, result);
	    Assert.assertEquals(ExceptionCode.INVALID_PARAMETER.getCode(), errorCode);
	    Assert.assertEquals("Sections", locator);

	} catch (Exception e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void wrongRequest3Test() {

	CSWRequestValidator validator = new CSWRequestValidator();
	GetCapabilities getCapabilities = new GetCapabilities();
	getCapabilities.setService("CSW");
	getCapabilities.setAcceptVersions(new AcceptVersionsType());
	getCapabilities.getAcceptVersions().getVersion().add("2.0.3");

	try {

	    WebRequest request = createWEBRequest(getCapabilities);

	    ValidationMessage message = validator.validate(request);
	    ValidationResult result = message.getResult();
	    String errorCode = message.getErrorCode();
	    String locator = message.getLocator();

	    Assert.assertEquals(ValidationResult.VALIDATION_FAILED, result);
	    Assert.assertEquals(ExceptionCode.VERSION_NEGOTIAION_FAILED.getCode(), errorCode);
	    Assert.assertEquals("version", locator);

	} catch (Exception e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void correctRequestTest0() {

	CSWRequestValidator validator = new CSWRequestValidator();
	GetCapabilities getCapabilities = new GetCapabilities();
	getCapabilities.setService("CSW");
	getCapabilities.setAcceptFormats(new AcceptFormatsType());
	getCapabilities.getAcceptFormats().getOutputFormat().add("X");

	try {

	    WebRequest request = createWEBRequest(getCapabilities);

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

    /**
     * version and request are automatically set, no need to test them
     */
    @Test
    public void correctRequest1Test() {

	CSWRequestValidator validator = new CSWRequestValidator();

	GetCapabilities getCapabilities = new GetCapabilities();
	getCapabilities.setService("CSW");

	try {
	    WebRequest request = createWEBRequest(getCapabilities);
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
    public void correctRequestTest2() {

	CSWRequestValidator validator = new CSWRequestValidator();
	GetCapabilities getCapabilities = new GetCapabilities();
	getCapabilities.setService("CSW");
	getCapabilities.setSections(new SectionsType());
	getCapabilities.getSections().getSection().add("ServiceIdentification");
	getCapabilities.getSections().getSection().add("ServiceProvider");

	try {
	    WebRequest request = createWEBRequest(getCapabilities);

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
    public void correctRequestTest3() {

	CSWRequestValidator validator = new CSWRequestValidator();
	GetCapabilities getCapabilities = new GetCapabilities();
	getCapabilities.setService("CSW");
	getCapabilities.setAcceptVersions(new AcceptVersionsType());
	getCapabilities.getAcceptVersions().getVersion().add("2.0.2");

	try {

	    WebRequest request = createWEBRequest(getCapabilities);

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
    public void correctRequestTest4() {

	CSWRequestValidator validator = new CSWRequestValidator();
	GetCapabilities getCapabilities = new GetCapabilities();
	getCapabilities.setService("CSW");
	getCapabilities.setAcceptVersions(new AcceptVersionsType());
	getCapabilities.getAcceptVersions().getVersion().add("2.0.2");
	getCapabilities.getAcceptVersions().getVersion().add("2.0.3");

	try {

	    WebRequest request = createWEBRequest(getCapabilities);

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
    public void correctRequestTest5() {

	CSWRequestValidator validator = new CSWRequestValidator();
	GetCapabilities getCapabilities = new GetCapabilities();
	getCapabilities.setService("CSW");
	getCapabilities.setAcceptFormats(new AcceptFormatsType());
	getCapabilities.getAcceptFormats().getOutputFormat().add("application/xml");
	getCapabilities.getAcceptFormats().getOutputFormat().add("text/xml");

	try {

	    WebRequest request = createWEBRequest(getCapabilities);

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
    public void correctRequestTest6() {

	CSWRequestValidator validator = new CSWRequestValidator();
	GetCapabilities getCapabilities = new GetCapabilities();
	getCapabilities.setService("CSW");
	getCapabilities.setAcceptFormats(new AcceptFormatsType());
	getCapabilities.getAcceptFormats().getOutputFormat().add("application/xml");
	getCapabilities.getAcceptFormats().getOutputFormat().add("text/xml");
	getCapabilities.getAcceptFormats().getOutputFormat().add("text/csv");

	try {

	    WebRequest request = createWEBRequest(getCapabilities);

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

    private WebRequest createWEBRequest(GetCapabilities getCap) throws Exception {

	ByteArrayInputStream inputStream = CommonContext.asInputStream(getCap, true);
	return WebRequest.createPOST("http://localhost/gs-service/services/essi/cswiso?", inputStream);
    }

    @SuppressWarnings("unused")
    private void printGetCapabilities(GetCapabilities getCap) throws Exception {

	System.out.println(CommonContext.asString(getCap, true));
    }
}
