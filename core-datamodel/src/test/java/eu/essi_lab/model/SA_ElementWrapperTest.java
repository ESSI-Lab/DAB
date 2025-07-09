/**
 * 
 */
package eu.essi_lab.model;

import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.model.resource.SA_ElementWrapper;

/**
 * @author Fabrizio
 */
public class SA_ElementWrapperTest {

    @Test
    public void test() {

	SA_ElementWrapper wrapper = SA_ElementWrapper.of(MetadataElement.KEYWORD_SA);

	wrapper.setValue("value");
	wrapper.setUri("uri");
	wrapper.setUriTitle("uri_title");
	wrapper.setSA_MatchType("SA_match_type");
	wrapper.setSA_Uri("SA_uri");
	wrapper.setSA_UriTitle("SA_uri_title");

	Assert.assertEquals("value", wrapper.getValue());
	Assert.assertEquals("uri", wrapper.getUri());
	Assert.assertEquals("uri_title", wrapper.getUriTitle());
	Assert.assertEquals("SA_match_type", wrapper.getSA_MatchType());
	Assert.assertEquals("SA_uri", wrapper.getSA_Uri());
	Assert.assertEquals("SA_uri_title", wrapper.getSA_UriTitle());
    }
}
