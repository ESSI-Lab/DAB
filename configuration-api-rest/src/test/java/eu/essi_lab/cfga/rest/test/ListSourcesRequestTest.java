/**
 * 
 */
package eu.essi_lab.cfga.rest.test;

import eu.essi_lab.cfga.option.*;
import eu.essi_lab.cfga.rest.*;
import eu.essi_lab.cfga.rest.source.*;
import eu.essi_lab.model.Queryable.*;
import org.junit.*;

import java.util.*;
/**
 * @author Fabrizio
 */
public class ListSourcesRequestTest {

    @Test
    public void getSupportedParametersTest() {

	ListSourcesRequest request = new ListSourcesRequest();

	List<Parameter> parameters = request.getSupportedParameters();

	Assert.assertEquals(1, parameters.size());

	Parameter parameter = Parameter.of( //
		RemoveSourceRequest.SOURCE_ID, //
		ContentType.TEXTUAL,//
		InputPattern.ALPHANUMERIC_AND_UNDERSCORE_AND_MINUS, //
		false);//

	parameter.setMultiValue();

	Assert.assertEquals(parameter, parameters.getFirst());
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

    @Test
    public void validationTest5_1() {

	ListSourcesRequest request = new ListSourcesRequest();
	request.getObject().remove("parameters");

	request.validate();
    }
}
