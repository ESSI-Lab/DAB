package eu.essi_lab.model;

import eu.essi_lab.model.resource.OrganizationElementWrapper;
import eu.essi_lab.model.resource.composed.ComposedElement;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Fabrizio
 */
public class OrganizationElementWrapperTest {

    @Test
    public void test() throws Exception {

	OrganizationElementWrapper wrapper = OrganizationElementWrapper.get();

	Assert.assertNull(wrapper.getEmail());
	Assert.assertNull(wrapper.getIndividualURI());
	Assert.assertNull(wrapper.getRole());
	Assert.assertNull(wrapper.getOrgName());
	Assert.assertNull(wrapper.getOrgUri());
	Assert.assertNull(wrapper.getHomePageURL());
	Assert.assertNull(wrapper.getHash());


	JSONObject json = wrapper.getElement().asJSON();

	ComposedElement fromJSON = ComposedElement.of(json, wrapper.getElement());

	Assert.assertNull(fromJSON.getProperty("homePageURL").get().getStringValue());
	Assert.assertNull(fromJSON.getProperty("email").get().getStringValue());
	Assert.assertNull(fromJSON.getProperty("individualName").get().getStringValue());
	Assert.assertNull(fromJSON.getProperty("role").get().getStringValue());
	Assert.assertNull(fromJSON.getProperty("orgName").get().getStringValue());
	Assert.assertNull(fromJSON.getProperty("individualURI").get().getStringValue());
	Assert.assertNull(fromJSON.getProperty("orgURI").get().getStringValue());
	Assert.assertNull(fromJSON.getProperty("hash").get().getStringValue());

	wrapper.setHomePageURL("homePageURL");
	wrapper.setEmail("email");
	wrapper.setIndividualName("indName");
	wrapper.setRole("role");
	wrapper.setOrgName("orgName");
	wrapper.setIndividualURI("indURI");
	wrapper.setOrgURI("orgURI");
	wrapper.setHash("hashValue");

	Assert.assertEquals("homePageURL", wrapper.getHomePageURL());
	Assert.assertEquals("email", wrapper.getEmail());
	Assert.assertEquals("indName", wrapper.getIndividualName());
	Assert.assertEquals("role", wrapper.getRole());
	Assert.assertEquals("orgName", wrapper.getOrgName());
	Assert.assertEquals("indURI", wrapper.getIndividualURI());
	Assert.assertEquals("orgURI", wrapper.getOrgUri());
	Assert.assertEquals("hashValue", wrapper.getHash());

	json = wrapper.getElement().asJSON();

	fromJSON = ComposedElement.of(json, wrapper.getElement());

	Assert.assertEquals("homePageURL", fromJSON.getProperty("homePageURL").get().getStringValue());
	Assert.assertEquals("email", fromJSON.getProperty("email").get().getStringValue());
	Assert.assertEquals("indName", fromJSON.getProperty("individualName").get().getStringValue());
	Assert.assertEquals("role", fromJSON.getProperty("role").get().getStringValue());
	Assert.assertEquals("orgName", fromJSON.getProperty("orgName").get().getStringValue());
	Assert.assertEquals("indURI", fromJSON.getProperty("individualURI").get().getStringValue());
	Assert.assertEquals("orgURI", fromJSON.getProperty("orgURI").get().getStringValue());
	Assert.assertEquals("hashValue", fromJSON.getProperty("hash").get().getStringValue());

    }
}
