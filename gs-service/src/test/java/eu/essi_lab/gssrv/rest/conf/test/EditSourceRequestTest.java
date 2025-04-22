/**
 * 
 */
package eu.essi_lab.gssrv.rest.conf.test;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.gssrv.rest.conf.EditSourceRequest;
import eu.essi_lab.gssrv.rest.conf.PutSourceRequest;
import eu.essi_lab.gssrv.rest.conf.PutSourceRequest.SourceType;

/**
 * @author Fabrizio
 */
public class EditSourceRequestTest {

    @Test
    public void validationTest() {

	EditSourceRequest request = new EditSourceRequest();

	request.put(PutSourceRequest.SOURCE_LABEL, "sourceLabel");
	request.put(PutSourceRequest.SOURCE_ENDPOINT, "http://localhost");
	request.put(PutSourceRequest.SOURCE_TYPE, SourceType.WCS.getLabel());

	Assert.assertThrows(IllegalArgumentException.class, () -> request.validate());
    }
    
    @Test
    public void validationTest_2() {

	EditSourceRequest request = new EditSourceRequest();

	request.put(PutSourceRequest.SOURCE_ID, "sourceId");
	request.put(PutSourceRequest.SOURCE_LABEL, "sourceLabel");
	request.put(PutSourceRequest.SOURCE_ENDPOINT, "http://localhost");
	request.put(PutSourceRequest.SOURCE_TYPE, SourceType.WCS.getLabel());

	request.validate();
    }
}
