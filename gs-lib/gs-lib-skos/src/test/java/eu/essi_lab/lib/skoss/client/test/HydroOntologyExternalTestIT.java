/**
 * 
 */
package eu.essi_lab.lib.skoss.client.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.eclipse.rdf4j.federated.FedXConfig;
import org.junit.Before;
import org.junit.Test;

import eu.essi_lab.lib.skos.SKOSClient;
import eu.essi_lab.lib.skos.SKOSConcept;
import eu.essi_lab.lib.skos.SKOSResponse;
import eu.essi_lab.lib.skos.SKOSSemanticRelation;
import eu.essi_lab.lib.skos.SKOSClient.SearchTarget;
import eu.essi_lab.lib.skos.expander.ExpansionLimit;
import eu.essi_lab.lib.skos.expander.ConceptsExpander.ExpansionLevel;
import eu.essi_lab.lib.skos.expander.ExpansionLimit.LimitTarget;
import eu.essi_lab.lib.skos.expander.impl.DefaultConceptsExpander;
import eu.essi_lab.lib.skos.expander.impl.FedXConceptsExpander;
import eu.essi_lab.lib.skos.expander.impl.FedXLevelsExpander;
import eu.essi_lab.lib.skos.finder.impl.DefaultConceptsFinder;
import eu.essi_lab.lib.utils.ThreadMode;

/**
 * Tests the SKOSClient on the hydro ontology concepts e.g.:
 * https://hydro.geodab.eu/ontology-browser/hydro-ontology.html?http://hydro.geodab.eu/hydro-ontology/concept/28
 * 
 * @author boldrini
 */
public class HydroOntologyExternalTestIT {

    private SKOSClient client;

    @Before
    public void init() throws Exception {

	FedXConfig fedXConfig = new FedXConfig();
	// fedXConfig.withDebugQueryPlan(true);
	// fedXConfig.withLogQueries(true);
	fedXConfig.withLogQueryPlan(true);
	fedXConfig.withEnableMonitoring(true);
	fedXConfig.withLogQueryPlan(true);

	this.client = new SKOSClient();
	client.setExpansionLimit(ExpansionLimit.of(LimitTarget.CONCEPTS, 1000));

	// client.setFinder(new FedXConceptsFinder());
	client.setFinder(new DefaultConceptsFinder());

	client.setOntologyUrls(Arrays.asList("http://hydro.geodab.eu/hydro-ontology/sparql"
	// , "http://codes.wmo.int/system/query"
	));
    }

    @Test
    public void testConceptsExpanderSingleThread() throws Exception {

	FedXConceptsExpander expander = new FedXConceptsExpander();
	expander.setTraceQuery(true);
	client.setExpander(expander);
	commonRoutine();
    }

    @Test
    public void testConceptsExpanderMultiThread() throws Exception {

	FedXConceptsExpander expander = new FedXConceptsExpander();
	expander.setTraceQuery(true);
	expander.setThreadMode(ThreadMode.MULTI());
	client.setExpander(expander);
	commonRoutine();
    }

    @Test
    public void testLevelsExpanderSingleThread() throws Exception {

	FedXLevelsExpander expander = new FedXLevelsExpander();
	expander.setTraceQuery(true);
	client.setExpander(expander);
	commonRoutine();
    }

    @Test
    public void testLevelsExpanderMultiThread() throws Exception {

	FedXLevelsExpander expander = new FedXLevelsExpander();
	expander.setTraceQuery(true);
	expander.setThreadMode(ThreadMode.MULTI());
	client.setExpander(expander);
	commonRoutine();
    }

    @Test
    public void testLevelsDefaultExpander() throws Exception {

	DefaultConceptsExpander expander = new DefaultConceptsExpander();
	client.setExpander(expander);
	commonRoutine();
    }

    public void commonRoutine() throws Exception {

	client.setSearchValue(SearchTarget.TERMS, "velocity");
	client.setSearchLangs(Arrays.asList("it", "en"));
	client.setSourceLangs(Arrays.asList("it", "en"));

	{
	    client.setExpansionLevel(ExpansionLevel.NONE);

	    client.setExpansionsRelations(Arrays.asList());

	    SKOSResponse response = client.search();

	    List<SKOSConcept> concepts = response.getAggregatedResults();
	    printConcepts(concepts);
	    assertTrue(concepts.size() == 1);
	    assertTrue(concepts.get(0).getConceptURI().equals("http://hydro.geodab.eu/hydro-ontology/concept/28"));
	    assertEquals(concepts.get(0).getPref().get(), "Velocity");
	    assertTrue(concepts.get(0).getAlt().size() == 1);
	    assertTrue(concepts.get(0).getAlt().contains("Velocità"));
	}

	//////////////////////////////////////////////////////
	{
	    client.setExpansionLevel(ExpansionLevel.LOW);// #1
	    client.setExpansionsRelations(Arrays.asList(SKOSSemanticRelation.NARROWER));

	    SKOSResponse response = client.search();

	    List<SKOSConcept> concepts = response.getAggregatedResults();
	    printConcepts(concepts);
	    assertTrue(concepts.size() == 5);
	    Set<String> uris = response.getConcepts();

	    assertTrue(uris.contains("http://hydro.geodab.eu/hydro-ontology/concept/28"));
	    assertTrue(uris.contains("http://hydro.geodab.eu/hydro-ontology/concept/33"));
	    assertTrue(uris.contains("http://hydro.geodab.eu/hydro-ontology/concept/5391"));
	    assertTrue(uris.contains("http://hydro.geodab.eu/hydro-ontology/concept/32"));
	    assertTrue(uris.contains("http://hydro.geodab.eu/hydro-ontology/concept/30"));
	}

	//////////////////////////////////////////////////////
	{
	    client.setExpansionLevel(ExpansionLevel.MEDIUM);// #1
	    client.setExpansionsRelations(Arrays.asList(SKOSSemanticRelation.NARROWER));

	    SKOSResponse response = client.search();

	    List<SKOSConcept> concepts = response.getAggregatedResults();
	    printConcepts(concepts);
	    assertTrue(concepts.size() == 8);
	    Set<String> uris = response.getConcepts();

	    assertTrue(uris.contains("http://hydro.geodab.eu/hydro-ontology/concept/28"));
	    assertTrue(uris.contains("http://hydro.geodab.eu/hydro-ontology/concept/33"));
	    assertTrue(uris.contains("http://hydro.geodab.eu/hydro-ontology/concept/5391"));
	    assertTrue(uris.contains("http://hydro.geodab.eu/hydro-ontology/concept/32"));
	    assertTrue(uris.contains("http://hydro.geodab.eu/hydro-ontology/concept/30"));
	    assertTrue(uris.contains("http://hydro.geodab.eu/hydro-ontology/concept/5328"));
	    assertTrue(uris.contains("http://hydro.geodab.eu/hydro-ontology/concept/35"));
	    assertTrue(uris.contains("http://hydro.geodab.eu/hydro-ontology/concept/34"));
	}
	//////////////////////////////////////////////////////
	{
	    client.setExpansionLevel(ExpansionLevel.MEDIUM);// #1
	    client.setExpansionsRelations(Arrays.asList(SKOSSemanticRelation.NARROWER, SKOSSemanticRelation.CLOSE_MATCH));

	    SKOSResponse response = client.search();

	    List<SKOSConcept> concepts = response.getAggregatedResults();
	    printConcepts(concepts);
	    assertTrue(concepts.size() == 9);
	    Set<String> uris = response.getConcepts();

	    assertTrue(uris.contains("http://hydro.geodab.eu/hydro-ontology/concept/28"));
	    assertTrue(uris.contains("http://hydro.geodab.eu/hydro-ontology/concept/33"));
	    assertTrue(uris.contains("http://hydro.geodab.eu/hydro-ontology/concept/5391"));
	    assertTrue(uris.contains("http://hydro.geodab.eu/hydro-ontology/concept/32"));
	    assertTrue(uris.contains("http://hydro.geodab.eu/hydro-ontology/concept/30"));
	    assertTrue(uris.contains("http://hydro.geodab.eu/hydro-ontology/concept/5328"));
	    assertTrue(uris.contains("http://hydro.geodab.eu/hydro-ontology/concept/35"));
	    assertTrue(uris.contains("http://hydro.geodab.eu/hydro-ontology/concept/34"));
	    assertTrue(uris.contains("http://codes.wmo.int/wmdr/ObservedVariableTerrestrial/12006"));

	}
    }

    private void printConcepts(List<SKOSConcept> items) {
	for (SKOSConcept item : items) {
	    System.out.println("********");
	    System.out.println(item);
	}
    }

    private void print(List<String> labels) {
	for (String label : labels) {
	    System.out.println(label);
	}
    }
}
