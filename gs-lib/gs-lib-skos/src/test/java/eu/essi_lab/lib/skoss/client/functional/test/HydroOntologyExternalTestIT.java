/**
 * 
 */
package eu.essi_lab.lib.skoss.client.functional.test;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import eu.essi_lab.lib.skoss.SKOSClient;
import eu.essi_lab.lib.skoss.SKOSConcept;
import eu.essi_lab.lib.skoss.SKOSResponse;
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

	client.setSearchTerm("livello");
	client.setExpansionLevel(ExpansionLevel.NONE);
	client.setExpansionsRelations(Arrays.asList());
	client.setSearchLangs(Arrays.asList("it", "en"));
	client.setSourceLangs(Arrays.asList("it", "en"));

	SKOSResponse response = client.search();

	List<String> labels = response.getLabels();
	assertTrue(labels.size()==3);
	assertTrue(labels.contains("Level"));
	assertTrue(labels.contains("Livello"));
	assertTrue(labels.contains("Water level"));
	
	List<SKOSConcept> results = response.getAssembledResults();
	printItem(results);
	assertTrue(results.size() == 1);

    }

    private void printItem(List<SKOSConcept> items) {
	for (SKOSConcept item : items) {
	    List<String> alt = item.getAlt();
	    System.out.println(item.getConcept() + ": " + item.getPref().get() + alt);
	}
    }

    private void print(List<String> labels) {
	for (String label : labels) {
	    System.out.println(label);
	}
    }
}
