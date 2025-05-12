/**
 * 
 */
package eu.essi_lab.gssrv.rest.conf.test;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.cfga.option.InputPattern;
import eu.essi_lab.gssrv.rest.conf.Parameter;
import eu.essi_lab.gssrv.rest.conf.requests.ListSourcesRequest;
import eu.essi_lab.gssrv.rest.conf.requests.RemoveSourceRequest;
import eu.essi_lab.model.Queryable.ContentType;

/**
 * @author Fabrizio
 */
public class ListSourcesRequestTest {

    @Test
    public void getSupportedPametersTest() {

	ListSourcesRequest request = new ListSourcesRequest();

	List<Parameter> parameters = request.getSupportedParameters();

	Assert.assertEquals(1, parameters.size());

	Parameter parameter = Parameter.of(RemoveSourceRequest.SOURCE_ID, ContentType.TEXTUAL,
		InputPattern.ALPHANUMERIC_AND_UNDERSCORE_AND_MINUS, false);
	parameter.setMultiValue();

	Assert.assertEquals(parameter, parameters.get(0));
    }

    @Test
    public void validationTest() {

	ListSourcesRequest request = new ListSourcesRequest();

	request.put(RemoveSourceRequest.SOURCE_ID, "sourceId1");

	request.validate();
    }

    @Test
    public void validationTest2() {

	ListSourcesRequest request = new ListSourcesRequest();

	request.put(RemoveSourceRequest.SOURCE_ID, "sourceId1,sourceId1");

	request.validate();
    }

    @Test
    public void validationTest3() {

	ListSourcesRequest request = new ListSourcesRequest();

	request.put(RemoveSourceRequest.SOURCE_ID, "   sourceId1    ,    sourceId1    ");

	request.validate();
    }

    @Test
    public void validationTest4() {

	ListSourcesRequest request = new ListSourcesRequest();

	request.put(RemoveSourceRequest.SOURCE_ID, "   sourceId1    =    sourceId1    ");

	Assert.assertThrows(IllegalArgumentException.class, () -> request.validate());
    }
    
    @Test
    public void validationTest5() {

	ListSourcesRequest request = new ListSourcesRequest();

	request.validate();
    }
}
