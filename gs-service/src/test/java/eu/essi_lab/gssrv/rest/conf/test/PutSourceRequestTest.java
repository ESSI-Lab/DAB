/**
 * 
 */
package eu.essi_lab.gssrv.rest.conf.test;

import java.util.List;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.cfga.option.InputPattern;
import eu.essi_lab.gssrv.rest.conf.Parameter;
import eu.essi_lab.gssrv.rest.conf.PutSourceRequest;
import eu.essi_lab.gssrv.rest.conf.PutSourceRequest.SourceType;
import eu.essi_lab.model.Queryable.ContentType;

/**
 * @author Fabrizio
 */
public class PutSourceRequestTest {

    @Test
    public void getSupportedPametersTest() {

	PutSourceRequest request = new PutSourceRequest();

	List<Parameter> parameters = request.getSupportedParameters();

	Assert.assertEquals(4, parameters.size());

	Assert.assertEquals(Parameter.of(PutSourceRequest.SOURCE_ID, ContentType.TEXTUAL, InputPattern.ALPHANUMERIC_AND_UNDERSCORE, false),
		parameters.get(0));
	Assert.assertEquals(Parameter.of(PutSourceRequest.SOURCE_LABEL, ContentType.TEXTUAL, true), parameters.get(1));
	Assert.assertEquals(Parameter.of(PutSourceRequest.SOURCE_ENDPOINT, ContentType.TEXTUAL, true), parameters.get(2));
	Assert.assertEquals(Parameter.of(PutSourceRequest.SOURCE_TYPE, ContentType.TEXTUAL, SourceType.class, true), parameters.get(3));
    }

    @Test
    public void validationTest1() {

	PutSourceRequest request = new PutSourceRequest();

	request.put(PutSourceRequest.SOURCE_ID, "sourceId");
	request.put(PutSourceRequest.SOURCE_LABEL, "sourceLabel");
	request.put(PutSourceRequest.SOURCE_ENDPOINT, "http://localhost");
	request.put(PutSourceRequest.SOURCE_TYPE, SourceType.WCS_111.getLabel());

	request.validate();
	
	System.out.println(request);
    }

    @Test
    public void basicTest() {

	PutSourceRequest request = new PutSourceRequest();

	basicTest(request);

	JSONObject object = new JSONObject(request.toString());

	PutSourceRequest request2 = new PutSourceRequest(object);

	basicTest(request2);

	Assert.assertEquals(request, request2);
    }

    @Test
    public void basicTest2() {

	JSONObject object = new JSONObject();

	PutSourceRequest request = new PutSourceRequest(object);

	Assert.assertThrows(IllegalArgumentException.class, () -> request.validate());
    }

    /**
     * @param request
     */
    private void basicTest(PutSourceRequest request) {

	Assert.assertEquals("PutSourceRequest", request.getName());

	request.put(PutSourceRequest.SOURCE_ID, "sourceId");
	request.put(PutSourceRequest.SOURCE_LABEL, "sourceLabel");
	request.put(PutSourceRequest.SOURCE_ENDPOINT, "http://localhost");
	request.put(PutSourceRequest.SOURCE_TYPE, SourceType.WCS_111.getLabel());

	Assert.assertEquals("sourceId", request.read(PutSourceRequest.SOURCE_ID).get());
	Assert.assertEquals("sourceLabel", request.read(PutSourceRequest.SOURCE_LABEL).get());
	Assert.assertEquals("http://localhost", request.read(PutSourceRequest.SOURCE_ENDPOINT).get());
	Assert.assertEquals(SourceType.WCS_111.getLabel(), request.read(PutSourceRequest.SOURCE_TYPE).get());

	request.validate();
    }

    @Test
    public void unknownParameterTest() {

	PutSourceRequest request = new PutSourceRequest();

	request.put(PutSourceRequest.SOURCE_ID, "sourceId");
	request.put(PutSourceRequest.SOURCE_LABEL, "sourceLabel");
	request.put(PutSourceRequest.SOURCE_ENDPOINT, "http://localhost");
	request.put(PutSourceRequest.SOURCE_TYPE, SourceType.WCS_111.getLabel());

	request.put("xxx", "xxx");

	Assert.assertThrows(IllegalArgumentException.class, () -> request.validate());
    }

    @Test
    public void validationTest2() {

	PutSourceRequest request = new PutSourceRequest();

	request.put(PutSourceRequest.SOURCE_ID, "sourceId!");
	request.put(PutSourceRequest.SOURCE_LABEL, "sourceLabel");
	request.put(PutSourceRequest.SOURCE_ENDPOINT, "http://localhost");
	request.put(PutSourceRequest.SOURCE_TYPE, SourceType.WCS_111.getLabel());

	Assert.assertThrows(IllegalArgumentException.class, () -> request.validate());
    }

    @Test
    public void validationTest2_2() {

	PutSourceRequest request = new PutSourceRequest();

	request.put(PutSourceRequest.SOURCE_ID, "sourceId#");
	request.put(PutSourceRequest.SOURCE_LABEL, "sourceLabel");
	request.put(PutSourceRequest.SOURCE_ENDPOINT, "http://localhost");
	request.put(PutSourceRequest.SOURCE_TYPE, SourceType.WCS_111.getLabel());

	Assert.assertThrows(IllegalArgumentException.class, () -> request.validate());
    }

    @Test
    public void validationTest3() {

	PutSourceRequest request = new PutSourceRequest();

	request.put(PutSourceRequest.SOURCE_ID, "sourceId");
	request.put(PutSourceRequest.SOURCE_LABEL, "sourceLabel");
	request.put(PutSourceRequest.SOURCE_ENDPOINT, "http://localhost");
	request.put(PutSourceRequest.SOURCE_TYPE, "xxx");

	Assert.assertThrows(IllegalArgumentException.class, () -> request.validate());
    }

    @Test
    public void validationTest4() {

	PutSourceRequest request = new PutSourceRequest();

	request.put(PutSourceRequest.SOURCE_LABEL, "sourceLabel");
	request.put(PutSourceRequest.SOURCE_ENDPOINT, "http://localhost");
	request.put(PutSourceRequest.SOURCE_TYPE, SourceType.WCS_111.getLabel());

	request.validate();
    }

    @Test
    public void validationTest5() {

	PutSourceRequest request = new PutSourceRequest();

	request.put(PutSourceRequest.SOURCE_ID, "sourceId");
	request.put(PutSourceRequest.SOURCE_ENDPOINT, "http://localhost");
	request.put(PutSourceRequest.SOURCE_TYPE, SourceType.WCS_111.getLabel());

	Assert.assertThrows(IllegalArgumentException.class, () -> request.validate());
    }

    @Test
    public void validationTest6() {

	PutSourceRequest request = new PutSourceRequest();

	request.put(PutSourceRequest.SOURCE_ID, "sourceId");
	request.put(PutSourceRequest.SOURCE_LABEL, "sourceLabel");
	request.put(PutSourceRequest.SOURCE_TYPE, SourceType.WCS_111.getLabel());

	Assert.assertThrows(IllegalArgumentException.class, () -> request.validate());
    }

    @Test
    public void validationTest7() {

	PutSourceRequest request = new PutSourceRequest();

	request.put(PutSourceRequest.SOURCE_ID, "sourceId");
	request.put(PutSourceRequest.SOURCE_LABEL, "sourceLabel");
	request.put(PutSourceRequest.SOURCE_ENDPOINT, "http://localhost");

	Assert.assertThrows(IllegalArgumentException.class, () -> request.validate());
    }
}
