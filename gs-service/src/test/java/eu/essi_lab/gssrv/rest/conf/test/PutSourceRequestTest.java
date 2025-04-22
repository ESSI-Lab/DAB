/**
 * 
 */
package eu.essi_lab.gssrv.rest.conf.test;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.gssrv.rest.conf.PutSourceRequest;
import eu.essi_lab.gssrv.rest.conf.PutSourceRequest.SourceType;

/**
 * @author Fabrizio
 */
public class PutSourceRequestTest {

    @Test
    public void validationTest1() {

	PutSourceRequest request = new PutSourceRequest();

	request.put(PutSourceRequest.SOURCE_ID, "sourceId");
	request.put(PutSourceRequest.SOURCE_LABEL, "sourceLabel");
	request.put(PutSourceRequest.SOURCE_ENDPOINT, "http://localhost");
	request.put(PutSourceRequest.SOURCE_TYPE, SourceType.WCS.getLabel());

	request.validate();
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

    /**
     * @param request
     */
    private void basicTest(PutSourceRequest request) {

	Assert.assertEquals("PutSourceRequest", request.getName());

	request.put(PutSourceRequest.SOURCE_ID, "sourceId");
	request.put(PutSourceRequest.SOURCE_LABEL, "sourceLabel");
	request.put(PutSourceRequest.SOURCE_ENDPOINT, "http://localhost");
	request.put(PutSourceRequest.SOURCE_TYPE, SourceType.WCS.getLabel());

	Assert.assertEquals("sourceId", request.read(PutSourceRequest.SOURCE_ID).get());
	Assert.assertEquals("sourceLabel", request.read(PutSourceRequest.SOURCE_LABEL).get());
	Assert.assertEquals("http://localhost", request.read(PutSourceRequest.SOURCE_ENDPOINT).get());
	Assert.assertEquals(SourceType.WCS.getLabel(), request.read(PutSourceRequest.SOURCE_TYPE).get());

	request.validate();
    }

    @Test
    public void validationTest2() {

	PutSourceRequest request = new PutSourceRequest();

	request.put(PutSourceRequest.SOURCE_ID, "sourceId!");
	request.put(PutSourceRequest.SOURCE_LABEL, "sourceLabel");
	request.put(PutSourceRequest.SOURCE_ENDPOINT, "http://localhost");
	request.put(PutSourceRequest.SOURCE_TYPE, SourceType.WCS.getLabel());

	Assert.assertThrows(IllegalArgumentException.class, () -> request.validate());
    }

    @Test
    public void validationTest2_2() {

	PutSourceRequest request = new PutSourceRequest();

	request.put(PutSourceRequest.SOURCE_ID, "sourceId#");
	request.put(PutSourceRequest.SOURCE_LABEL, "sourceLabel");
	request.put(PutSourceRequest.SOURCE_ENDPOINT, "http://localhost");
	request.put(PutSourceRequest.SOURCE_TYPE, SourceType.WCS.getLabel());

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
	request.put(PutSourceRequest.SOURCE_TYPE, SourceType.WCS.getLabel());

	request.validate();
    }

    @Test
    public void validationTest5() {

	PutSourceRequest request = new PutSourceRequest();

	request.put(PutSourceRequest.SOURCE_ID, "sourceId");
	request.put(PutSourceRequest.SOURCE_ENDPOINT, "http://localhost");
	request.put(PutSourceRequest.SOURCE_TYPE, SourceType.WCS.getLabel());

	Assert.assertThrows(IllegalArgumentException.class, () -> request.validate());
    }

    @Test
    public void validationTest6() {

	PutSourceRequest request = new PutSourceRequest();

	request.put(PutSourceRequest.SOURCE_ID, "sourceId");
	request.put(PutSourceRequest.SOURCE_LABEL, "sourceLabel");
	request.put(PutSourceRequest.SOURCE_TYPE, SourceType.WCS.getLabel());

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
