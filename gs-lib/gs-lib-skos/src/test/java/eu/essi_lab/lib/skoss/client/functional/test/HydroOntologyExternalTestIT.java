/**
 * 
 */
package eu.essi_lab.lib.skoss.client.functional.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import eu.essi_lab.lib.skoss.SKOSClient;
import eu.essi_lab.lib.skoss.SKOSConcept;
import eu.essi_lab.lib.skoss.SKOSResponse;
import eu.essi_lab.lib.skoss.SKOSSemanticRelation;
import eu.essi_lab.lib.skoss.expander.ConceptsExpander.ExpansionLevel;
import eu.essi_lab.lib.skoss.expander.impl.FedXConceptsExpander;
import eu.essi_lab.lib.skoss.finder.impl.FedXConceptsFinder;

/**
 * @author boldrini
 */
public class HydroOntologyExternalTestIT {

    private SKOSClient client;

    @Before
    public void init() throws Exception {

	this.client = new SKOSClient();
	client.setLimit(SKOSClient.DEFAULT_LIMIT);

	client.setFinder(new FedXConceptsFinder());
	client.setExpander(new FedXConceptsExpander());
	client.setOntologyUrls(Arrays.asList("http://hydro.geodab.eu/hydro-ontology/sparql"));
    }

    @Test
    public void test() throws Exception {

	client.setSearchTerm("velocity");
	client.setExpansionLevel(ExpansionLevel.NONE);
	client.setExpansionsRelations(Arrays.asList());
	client.setSearchLangs(Arrays.asList("it", "en"));
	client.setSourceLangs(Arrays.asList("it", "en"));

	SKOSResponse response = client.search();

	List<SKOSConcept> concepts = response.getAssembledResults();
	printConcepts(concepts);
	assertTrue(concepts.size() == 1);
	assertTrue(concepts.get(0).getConcept().equals("http://hydro.geodab.eu/hydro-ontology/concept/28"));
	assertEquals(concepts.get(0).getPref().get(), "Velocity");
	assertTrue(concepts.get(0).getAlt().size() == 1);
	assertTrue(concepts.get(0).getAlt().contains("Velocità"));

	client.setExpansionLevel(ExpansionLevel.LOW);// #1
	client.setExpansionsRelations(Arrays.asList(SKOSSemanticRelation.NARROWER));
	client.setSearchLangs(Arrays.asList("it", "en"));
	client.setSourceLangs(Arrays.asList("it", "en"));

	response = client.search();

	concepts = response.getAssembledResults();
	printConcepts(concepts);
	
	 
    }

    private void printConcepts(List<SKOSConcept> items) {
	for (SKOSConcept item : items) {
	    Set<String> alt = item.getAlt();
	    System.out.println(item.getConcept() + ": " + item.getPref().get() + alt);
	}
    }

    private void print(List<String> labels) {
	for (String label : labels) {
	    System.out.println(label);
	}
    }
}
