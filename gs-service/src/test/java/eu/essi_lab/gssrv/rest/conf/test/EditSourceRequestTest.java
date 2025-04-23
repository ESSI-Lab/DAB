/**
 * 
 */
package eu.essi_lab.gssrv.rest.conf.test;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.cfga.option.InputPattern;
import eu.essi_lab.gssrv.rest.conf.EditSourceRequest;
import eu.essi_lab.gssrv.rest.conf.Parameter;
import eu.essi_lab.gssrv.rest.conf.PutSourceRequest;
import eu.essi_lab.gssrv.rest.conf.PutSourceRequest.SourceType;
import eu.essi_lab.model.Queryable.ContentType;

/**
 * @author Fabrizio
 */
public class EditSourceRequestTest {

    @Test
    public void getSupportedPametersTest() {

	EditSourceRequest request = new EditSourceRequest();

	List<Parameter> parameters = request.getSupportedParameters();

	Assert.assertEquals(4, parameters.size());

	Assert.assertEquals(Parameter.of(PutSourceRequest.SOURCE_ID, ContentType.TEXTUAL, InputPattern.ALPHANUMERIC_AND_UNDERSCORE, true),
		parameters.get(0));
	Assert.assertEquals(Parameter.of(PutSourceRequest.SOURCE_LABEL, ContentType.TEXTUAL, true), parameters.get(1));
	Assert.assertEquals(Parameter.of(PutSourceRequest.SOURCE_ENDPOINT, ContentType.TEXTUAL, true), parameters.get(2));
	Assert.assertEquals(Parameter.of(PutSourceRequest.SOURCE_TYPE, ContentType.TEXTUAL, SourceType.class, true), parameters.get(3));
    }

    @Test
    public void validationTest() {

	EditSourceRequest request = new EditSourceRequest();

	request.put(PutSourceRequest.SOURCE_LABEL, "sourceLabel");
	request.put(PutSourceRequest.SOURCE_ENDPOINT, "http://localhost");
	request.put(PutSourceRequest.SOURCE_TYPE, SourceType.WCS_111.getLabel());

	Assert.assertThrows(IllegalArgumentException.class, () -> request.validate());
    }

    @Test
    public void validationTest_2() {

	EditSourceRequest request = new EditSourceRequest();

	request.put(PutSourceRequest.SOURCE_ID, "sourceId");
	request.put(PutSourceRequest.SOURCE_LABEL, "sourceLabel");
	request.put(PutSourceRequest.SOURCE_ENDPOINT, "http://localhost");
	request.put(PutSourceRequest.SOURCE_TYPE, SourceType.WCS_111.getLabel());

	request.validate();
    }
}
