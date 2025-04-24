/**
 * 
 */
package eu.essi_lab.gssrv.rest.conf.test;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.cfga.option.InputPattern;
import eu.essi_lab.gssrv.rest.conf.Parameter;
import eu.essi_lab.gssrv.rest.conf.RemoveSourceRequest;
import eu.essi_lab.model.Queryable.ContentType;

/**
 * @author Fabrizio
 */
public class RemoveSourceRequestTest {

    @Test
    public void getSupportedPametersTest() {

	RemoveSourceRequest request = new RemoveSourceRequest();

	List<Parameter> parameters = request.getSupportedParameters();

	Assert.assertEquals(2, parameters.size());

	Assert.assertEquals(
		Parameter.of(RemoveSourceRequest.SOURCE_ID, ContentType.TEXTUAL, InputPattern.ALPHANUMERIC_AND_UNDERSCORE_AND_MINUS, true),
		parameters.get(0));

	Assert.assertEquals(Parameter.of(RemoveSourceRequest.REMOVE_DATA, ContentType.BOOLEAN, false), parameters.get(1));
    }

    @Test
    public void validationTest() {

	RemoveSourceRequest request = new RemoveSourceRequest();

	request.put(RemoveSourceRequest.SOURCE_ID, "sourceId");
	request.put(RemoveSourceRequest.REMOVE_DATA, "false");

	request.validate();
    }

    @Test
    public void validationTest2() {

	RemoveSourceRequest request = new RemoveSourceRequest();

	request.put(RemoveSourceRequest.SOURCE_ID, "sourceId");
	request.put(RemoveSourceRequest.REMOVE_DATA, "true");

	request.validate();
    }

    @Test
    public void validationTest3() {

	RemoveSourceRequest request = new RemoveSourceRequest();

	request.put(RemoveSourceRequest.REMOVE_DATA, "true");

	Assert.assertThrows(IllegalArgumentException.class, () -> request.validate());
    }

    @Test
    public void validationTest4() {

	RemoveSourceRequest request = new RemoveSourceRequest();

	request.put(RemoveSourceRequest.SOURCE_ID, "sourceId");

	request.validate();
    }

    @Test
    public void validationTest5() {

	RemoveSourceRequest request = new RemoveSourceRequest();

	request.put(RemoveSourceRequest.SOURCE_ID, "sourceId");
	request.put(RemoveSourceRequest.REMOVE_DATA, "xxx");

	Assert.assertThrows(IllegalArgumentException.class, () -> request.validate());
    }

}
