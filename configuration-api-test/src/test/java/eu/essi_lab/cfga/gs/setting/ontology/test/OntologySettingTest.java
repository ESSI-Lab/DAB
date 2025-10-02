/**
 * 
 */
package eu.essi_lab.cfga.gs.setting.ontology.test;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.cfga.gs.setting.OntologySetting;

/**
 * @author Fabrizio
 */
public class OntologySettingTest {

    @Test
    public void test() {

	OntologySetting setting = new OntologySetting();

	Assert.assertNull(setting.getOntologyName());
	Assert.assertNull(setting.getOntologyEndpoint());
	Assert.assertNull(setting.getOntologyId());
	Assert.assertTrue(setting.getOntologyDescription().isEmpty());
	Assert.assertTrue(setting.isOntologyEnabled());

	setting.setOntologyName("name");
	setting.setOntologyDescription("desc");
	setting.setOntologyEndpoint("endpoint");
	setting.setOntologyId("id");
	setting.setOntolgyEnabled(false);

	Assert.assertEquals("name", setting.getOntologyName());
	Assert.assertEquals("endpoint", setting.getOntologyEndpoint());
	Assert.assertEquals("id", setting.getOntologyId());
	Assert.assertEquals("desc", setting.getOntologyDescription().get());
	Assert.assertFalse(setting.isOntologyEnabled());
    }
}
