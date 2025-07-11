/**
 * 
 */
package eu.essi_lab.model;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.model.resource.SA_ElementWrapper;
import eu.essi_lab.model.resource.composed.ComposedElement;

/**
 * @author Fabrizio
 */
public class SA_ElementWrapperTest {

    @Test
    public void test() throws Exception {

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

	JSONObject json = wrapper.getElement().asJSON();

	ComposedElement fromJSON = ComposedElement.of(json, wrapper.getElement());

	Assert.assertEquals("value", fromJSON.getProperty("value").get().getStringValue());
	Assert.assertEquals("uri", fromJSON.getProperty("uri").get().getStringValue());
	Assert.assertEquals("uri_title", fromJSON.getProperty("uri_title").get().getStringValue());
	Assert.assertEquals("SA_match_type", fromJSON.getProperty("SA_match_type").get().getStringValue());
	Assert.assertEquals("SA_uri", fromJSON.getProperty("SA_uri").get().getStringValue());
	Assert.assertEquals("SA_uri_title", fromJSON.getProperty("SA_uri_title").get().getStringValue());
    }

    @Test
    public void test2() throws Exception {

	SA_ElementWrapper wrapper = SA_ElementWrapper.of(MetadataElement.KEYWORD_SA);

	Assert.assertNull(wrapper.getValue());
	Assert.assertNull(wrapper.getUri());
	Assert.assertNull(wrapper.getUriTitle());
	Assert.assertNull(wrapper.getSA_MatchType());
	Assert.assertNull(wrapper.getSA_Uri());
	Assert.assertNull(wrapper.getSA_UriTitle());

	JSONObject json = wrapper.getElement().asJSON();

	ComposedElement fromJSON = ComposedElement.of(json, wrapper.getElement());

	Assert.assertNull(fromJSON.getProperty("value").get().getStringValue());
	Assert.assertNull(fromJSON.getProperty("uri").get().getStringValue());
	Assert.assertNull(fromJSON.getProperty("uri_title").get().getStringValue());
	Assert.assertNull(fromJSON.getProperty("SA_match_type").get().getStringValue());
	Assert.assertNull(fromJSON.getProperty("SA_uri").get().getStringValue());
	Assert.assertNull(fromJSON.getProperty("SA_uri_title").get().getStringValue());
    }
}
