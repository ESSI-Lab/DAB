/**
 * 
 */
package eu.essi_lab.cfga.gs.setting.ontology.test;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.cfga.gs.setting.ontology.OntologySetting;
import eu.essi_lab.cfga.gs.setting.ontology.OntologySetting.Availability;
import eu.essi_lab.cfga.gs.setting.ontology.OntologySetting.DataModel;
import eu.essi_lab.cfga.gs.setting.ontology.OntologySetting.QueryLanguage;

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

	Assert.assertEquals(Availability.ENABLED, setting.getOntologyAvailability());

	Assert.assertEquals(DataModel.SKOS, setting.getDataModel());
	Assert.assertEquals(QueryLanguage.SPARQL, setting.getQueryLanguage());

	setting.setOntologyName("name");
	setting.setOntologyDescription("desc");
	setting.setOntologyEndpoint("endpoint");
	setting.setOntologyId("id");

	setting.setOntologyAvailability(Availability.DISABLED);
	setting.setDataModel(DataModel.SKOS);
	setting.setQueryLanguage(QueryLanguage.SPARQL);

	Assert.assertEquals("name", setting.getOntologyName());
	Assert.assertEquals("endpoint", setting.getOntologyEndpoint());
	Assert.assertEquals("id", setting.getOntologyId());
	Assert.assertEquals("desc", setting.getOntologyDescription().get());

	Assert.assertEquals(Availability.DISABLED, setting.getOntologyAvailability());

	Assert.assertEquals(DataModel.SKOS, setting.getDataModel());
	Assert.assertEquals(QueryLanguage.SPARQL, setting.getQueryLanguage());
    }
}
