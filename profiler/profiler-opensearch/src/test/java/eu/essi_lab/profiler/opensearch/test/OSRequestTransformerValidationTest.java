package eu.essi_lab.profiler.opensearch.test;

import static org.junit.Assert.fail;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.profiler.os.handler.discover.OSRequestTransformer;

public class OSRequestTransformerValidationTest {

    @Before
    public void init() {
    }

    @Test
    public void testValidDescriptionDocumentRequest() {

	WebRequest webRequest = WebRequest.createGET("http://opensearch/description");

	OSRequestTransformer osRequestTransformer = new OSRequestTransformer();
	try {
	    ValidationMessage validate = osRequestTransformer.validate(webRequest);

	    String error = validate.getError();
	    Assert.assertNull(error);

	    ValidationResult result = validate.getResult();
	    Assert.assertEquals(result, ValidationResult.VALIDATION_SUCCESSFUL);

	} catch (GSException e) {

	    fail("Exception thrown");
	}
    }

    @Test
    public void testNullRequest() {

	WebRequest webRequest = null;
	try {
	    webRequest = WebRequest.createGET(null);
	} catch (Exception ex) {
	}

	OSRequestTransformer osRequestTransformer = new OSRequestTransformer();
	try {
	    ValidationMessage validate = osRequestTransformer.validate(webRequest);

	    String error = validate.getError();
	    // System.out.println(error);
	    Assert.assertNotNull(error);

	    ValidationResult result = validate.getResult();
	    Assert.assertEquals(result, ValidationResult.VALIDATION_FAILED);

	} catch (GSException e) {

	    fail("Exception thrown");
	}
    }

    @Test
    public void testEmptyStringRequest() {

	WebRequest webRequest = WebRequest.createGET("http://opensearch?");

	OSRequestTransformer osRequestTransformer = new OSRequestTransformer();
	try {
	    ValidationMessage validate = osRequestTransformer.validate(webRequest);

	    String error = validate.getError();
	    // System.out.println(error);
	    Assert.assertNotNull(error);

	    ValidationResult result = validate.getResult();
	    Assert.assertEquals(result, ValidationResult.VALIDATION_FAILED);

	} catch (GSException e) {

	    fail("Exception thrown");
	}
    }

    @Test
    public void testRequestWithWrongStartTime() {

	WebRequest webRequest = WebRequest.createGET("http://opensearch?ct=10&si=0&st=pippo&ts=XXX");

	OSRequestTransformer osRequestTransformer = new OSRequestTransformer();
	try {
	    ValidationMessage validate = osRequestTransformer.validate(webRequest);

	    String error = validate.getError();
	    // System.out.println(error);
	    Assert.assertNotNull(error);

	    ValidationResult result = validate.getResult();
	    Assert.assertEquals(result, ValidationResult.VALIDATION_FAILED);

	} catch (GSException e) {

	    fail("Exception thrown");
	}
    }

    @Test
    public void testRequestWithWrongEndTime() {

	WebRequest webRequest = WebRequest.createGET("http://opensearch?ct=10&si=0&st=pippo&te=XXX");

	OSRequestTransformer osRequestTransformer = new OSRequestTransformer();
	try {
	    ValidationMessage validate = osRequestTransformer.validate(webRequest);

	    String error = validate.getError();
	    // System.out.println(error);
	    Assert.assertNotNull(error);

	    ValidationResult result = validate.getResult();
	    Assert.assertEquals(result, ValidationResult.VALIDATION_FAILED);

	} catch (GSException e) {

	    fail("Exception thrown");
	}
    }

    @Test
    public void testRequestWithWrongBBOX() {

	WebRequest webRequest = WebRequest.createGET("http://opensearch?ct=10&si=0&st=pippo&bbox=XXX");

	OSRequestTransformer osRequestTransformer = new OSRequestTransformer();
	try {
	    ValidationMessage validate = osRequestTransformer.validate(webRequest);

	    String error = validate.getError();
	    // System.out.println(error);
	    Assert.assertNotNull(error);

	    ValidationResult result = validate.getResult();
	    Assert.assertEquals(result, ValidationResult.VALIDATION_FAILED);

	} catch (GSException e) {

	    fail("Exception thrown");
	}
    }

    @Test
    public void testRequestWithMissingOutputFormat() {

	WebRequest webRequest = WebRequest.createGET("http://opensearch?ct=10&si=0&st=pippo");

	OSRequestTransformer osRequestTransformer = new OSRequestTransformer();
	try {
	    ValidationMessage validate = osRequestTransformer.validate(webRequest);

	    ValidationResult result = validate.getResult();
	    Assert.assertEquals(result, ValidationResult.VALIDATION_SUCCESSFUL);

	} catch (GSException e) {

	    fail("Exception thrown");
	}
    }

    @Test
    public void testRequestWithUnsupportedOutputFormat() {

	WebRequest webRequest = WebRequest.createGET("http://opensearch?ct=10&si=0&st=pippo&outputFormat=application/pippo");

	OSRequestTransformer osRequestTransformer = new OSRequestTransformer();
	try {
	    ValidationMessage validate = osRequestTransformer.validate(webRequest);

	    String error = validate.getError();
	    // System.out.println(error);
	    Assert.assertNotNull(error);

	    ValidationResult result = validate.getResult();
	    Assert.assertEquals(result, ValidationResult.VALIDATION_FAILED);

	} catch (GSException e) {

	    fail("Exception thrown");
	}
    }

    @Test
    public void testValidRequestWithAllSupportedParameters() {

	WebRequest webRequest = WebRequest
		.createGET("http://opensearch?ct=10&si=0&st=pippo&ts=2000&te=2010&bbox=-180,-90,180,90&outputFormat=application/json");

	OSRequestTransformer osRequestTransformer = new OSRequestTransformer();
	try {
	    ValidationMessage validate = osRequestTransformer.validate(webRequest);

	    String error = validate.getError();
	    Assert.assertNull(error);

	    ValidationResult result = validate.getResult();
	    Assert.assertEquals(result, ValidationResult.VALIDATION_SUCCESSFUL);

	} catch (GSException e) {

	    fail("Exception thrown");
	}
    }
}
