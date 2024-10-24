package eu.essi_lab.profiler.csw.test.getrecbyid.validation;

import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.jaxb.common.CommonContext;
import eu.essi_lab.jaxb.csw._2_0_2.ElementSetName;
import eu.essi_lab.jaxb.csw._2_0_2.ElementSetType;
import eu.essi_lab.jaxb.csw._2_0_2.ExceptionCode;
import eu.essi_lab.jaxb.csw._2_0_2.GetRecordById;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.profiler.csw.handler.discover.CSWRequestValidator;

public class GetRecordByIdValidationTestPOST {

    @Test
    public void wrongRequest1Test() {

	CSWRequestValidator validator = new CSWRequestValidator();
	GetRecordById grById = new GetRecordById();

	try {

	    WebRequest request = createWEBRequest(grById);

	    ValidationMessage message = validator.validate(request);
	    ValidationResult result = message.getResult();
	    String errorCode = message.getErrorCode();
	    String locator = message.getLocator();

	    Assert.assertEquals(ValidationResult.VALIDATION_FAILED, result);
	    Assert.assertEquals(ExceptionCode.MISSING_PARAMETER.getCode(), errorCode);
	    Assert.assertEquals("id", locator);

	} catch (Exception e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void wrongRequest3Test() {

	CSWRequestValidator validator = new CSWRequestValidator();
	GetRecordById grById = new GetRecordById();
	grById.setOutputFormat("X");

	try {

	    WebRequest request = createWEBRequest(grById);

	    ValidationMessage message = validator.validate(request);
	    ValidationResult result = message.getResult();
	    String errorCode = message.getErrorCode();
	    String locator = message.getLocator();

	    Assert.assertEquals(ValidationResult.VALIDATION_FAILED, result);
	    Assert.assertEquals(ExceptionCode.INVALID_PARAMETER.getCode(), errorCode);
	    Assert.assertEquals("outputFormat", locator);

	} catch (Exception e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void wrongRequest4Test() {

	CSWRequestValidator validator = new CSWRequestValidator();
	GetRecordById grById = new GetRecordById();
	grById.setOutputSchema("X");

	try {

	    WebRequest request = createWEBRequest(grById);

	    ValidationMessage message = validator.validate(request);
	    ValidationResult result = message.getResult();
	    String errorCode = message.getErrorCode();
	    String locator = message.getLocator();

	    Assert.assertEquals(ValidationResult.VALIDATION_FAILED, result);
	    Assert.assertEquals(ExceptionCode.INVALID_PARAMETER.getCode(), errorCode);
	    Assert.assertEquals("outputSchema", locator);

	} catch (Exception e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    @Test
    public void correctRequestTest() {

	CSWRequestValidator validator = new CSWRequestValidator();
	GetRecordById grById = new GetRecordById();
	ElementSetName setName = new ElementSetName();
	setName.setValue(ElementSetType.FULL);
	grById.setElementSetName(setName);
	grById.setOutputSchema("http://www.opengis.net/cat/csw/2.0.2");
	grById.setOutputFormat("text/xml");
	grById.getIds().add("X");
	grById.getIds().add("Y");
	grById.getIds().add("Z");

	try {

	    WebRequest request = createWEBRequest(grById);

	    ValidationMessage message = validator.validate(request);
	    ValidationResult result = message.getResult();

	    Assert.assertEquals(ValidationResult.VALIDATION_SUCCESSFUL, result);

	} catch (Exception e) {
	    e.printStackTrace();
	    fail("Exception thrown");
	}
    }

    private WebRequest createWEBRequest(GetRecordById grById) throws Exception {

	ByteArrayInputStream inputStream = CommonContext.asInputStream(grById, true);
	return WebRequest.createPOST("http://localhost/gs-service/services/essi/cswiso?", inputStream);
    }
}
