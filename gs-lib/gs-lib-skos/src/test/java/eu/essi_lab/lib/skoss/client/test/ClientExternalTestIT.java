/**
 * 
 */
package eu.essi_lab.lib.skoss.client.test;

import java.util.Arrays;
import java.util.List;

import org.eclipse.rdf4j.federated.FedXConfig;
import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.lib.skoss.SKOSClient;
import eu.essi_lab.lib.skoss.SKOSConcept;
import eu.essi_lab.lib.skoss.SKOSResponse;
import eu.essi_lab.lib.skoss.SKOSSemanticRelation;
import eu.essi_lab.lib.skoss.ThreadMode;
import eu.essi_lab.lib.skoss.expander.ConceptsExpander;
import eu.essi_lab.lib.skoss.expander.ConceptsExpander.ExpansionLevel;
import eu.essi_lab.lib.skoss.expander.ExpansionLimit;
import eu.essi_lab.lib.skoss.expander.ExpansionLimit.LimitTarget;
import eu.essi_lab.lib.skoss.expander.impl.DefaultConceptsExpander;
import eu.essi_lab.lib.skoss.expander.impl.FedXConceptsExpander;
import eu.essi_lab.lib.skoss.finder.ConceptsFinder;
import eu.essi_lab.lib.skoss.finder.impl.DefaultConceptsQueryBuilder;
import eu.essi_lab.lib.skoss.finder.impl.FedXConceptsFinder;
import eu.essi_lab.lib.skoss.finder.impl.FedXConceptsQueryExecutor;

/**
 * @author Fabrizio
 */
public class ClientExternalTestIT {

    @Test
    public void paramsTest() throws Exception {

	SKOSClient client = new SKOSClient();

	ConceptsExpander expander = client.getExpander();
	Assert.assertEquals(FedXConceptsExpander.class, expander.getClass());

	ConceptsFinder finder = client.getFinder();
	Assert.assertEquals(FedXConceptsFinder.class, finder.getClass());

	ExpansionLevel expansionLevel = client.getExpansionLevel();
	Assert.assertEquals(ExpansionLevel.LOW, expansionLevel);

	List<SKOSSemanticRelation> expansionsRelations = client.getExpansionsRelations();
	Assert.assertEquals(2, expansionsRelations.size());
	Assert.assertTrue(expansionsRelations.contains(SKOSSemanticRelation.RELATED));
	Assert.assertTrue(expansionsRelations.contains(SKOSSemanticRelation.NARROWER));

	ExpansionLimit limit = client.getExpansionLimit();
	Assert.assertEquals(10, limit.getLimit());

	List<String> searchLangs = client.getSearchLangs();
	Assert.assertEquals(2, searchLangs.size());
	Assert.assertTrue(searchLangs.contains("it"));
	Assert.assertTrue(searchLangs.contains("en"));

	List<String> sourceLangs = client.getSourceLangs();
	Assert.assertEquals(2, sourceLangs.size());
	Assert.assertTrue(sourceLangs.contains("it"));
	Assert.assertTrue(sourceLangs.contains("en"));

	List<String> ontologyUrls = client.getOntologyUrls();
	Assert.assertNull(ontologyUrls);

	String searchTerm = client.getSearchTerm();
	Assert.assertNull(searchTerm);

	//
	//
	//

	client.setExpander(null);
	client.setFinder(null);
	client.setExpansionLevel(ExpansionLevel.NONE);
	client.setExpansionsRelations(Arrays.asList());
	client.setExpansionLimit(ExpansionLimit.of(LimitTarget.ALT_LABELS, 0));
	client.setOntologyUrls(Arrays.asList());
	client.setSearchLangs(Arrays.asList());
	client.setSearchTerm("search");
	client.setSourceLangs(Arrays.asList());

	Assert.assertNull(client.getExpander());
	Assert.assertNull(client.getFinder());
	Assert.assertEquals(ExpansionLevel.NONE, client.getExpansionLevel());
	Assert.assertTrue(client.getExpansionsRelations().isEmpty());
	Assert.assertEquals(0, client.getExpansionLimit().getLimit());
	Assert.assertEquals(LimitTarget.ALT_LABELS, client.getExpansionLimit().getTarget());
	Assert.assertTrue(client.getOntologyUrls().isEmpty());
	Assert.assertTrue(client.getSearchLangs().isEmpty());
	Assert.assertEquals("search", client.getSearchTerm());
	Assert.assertTrue(client.getSourceLangs().isEmpty());
    }

    //
    // Concepts expander
    //

    @Test
    public void mediumExpansionLimit10Test_ConceptsExpander() throws Exception {

	mediumExpansionLimit10Test(new FedXConceptsExpander(), ThreadMode.SINGLE());
    }

    @Test
    public void mediumExpansionLimit10Test_DefaultConceptsExpander() throws Exception {

	mediumExpansionLimit10Test(new DefaultConceptsExpander(), ThreadMode.SINGLE());
    }

    //
    // Levels expander
    //

    // @Test
    // public void mediumExpansionLimit10Test_LevelsExpander_SingleThread() throws Exception {
    //
    // mediumExpansionLimit10Test(new DefaultConceptsExpander(), ThreadMode.SINGLE());
    // }
    //
    // @Test
    // public void mediumExpansionLimit10Test_LevelsExpander_MultiThread() throws Exception {
    //
    // mediumExpansionLimit10Test(new DefaultConceptsExpander(), ThreadMode.MULTI());
    // }

    /**
     * @param expander
     * @param mode
     * @throws Exception
     */
    private void mediumExpansionLimit10Test(ConceptsExpander expander, ThreadMode mode) throws Exception {

	SKOSClient client = new SKOSClient();

	client.setOntologyUrls(Arrays.asList(//
		"http://localhost:3031/gemet/query", //
		"http://hydro.geodab.eu/hydro-ontology/sparql", //
		"https://vocabularies.unesco.org/sparql" //
	));

	client.setExpansionLevel(ExpansionLevel.LOW);
	client.setExpansionLimit(ExpansionLimit.of(LimitTarget.CONCEPTS, 100));
	client.setSearchTerm("water");

	FedXConfig fedXConfig = new FedXConfig();
	// fedXConfig.withDebugQueryPlan(true);
	// fedXConfig.withLogQueries(true);
	// fedXConfig.withLogQueryPlan(true);
	fedXConfig.withEnableMonitoring(true);

	//
	//
	//

	FedXConceptsFinder finder = new FedXConceptsFinder();

	FedXConceptsQueryExecutor conceptsQueryExecutor = new FedXConceptsQueryExecutor();
	conceptsQueryExecutor.setEngineConfig(fedXConfig);
	conceptsQueryExecutor.setTraceQuery(true);

	finder.setExecutor(conceptsQueryExecutor);
	finder.setQueryBuilder(new DefaultConceptsQueryBuilder());
	finder.setThreadMode(ThreadMode.SINGLE());

	client.setFinder(finder);

	//
	//
	//

	// expander.setEngineConfig(fedXConfig);
	// expander.setQueryBuilder(new DefaultExpandConceptsQueryBuilder());

	if (expander instanceof FedXConceptsExpander) {
	    ((FedXConceptsExpander) expander).setThreadMode(ThreadMode.MULTI());
	}
	// expander.setTraceQuery(true);

	client.setExpander(expander);

	//
	//
	//

	SKOSResponse response = client.search();

	List<SKOSConcept> results = response.getResults().stream().//
		sorted((r1, r2) -> r1.toString().compareTo(r2.toString())). //
		toList();//

	System.out.println("\n\n");

	results.forEach(res -> System.out.println(res + "\n---"));

	//
	// System.out.println("\n\n");
	//
	// response.getPrefLabels().forEach(pref -> System.out.println(pref));
	//
	// System.out.println("\n\n");
	//
	// response.getLabels().forEach(alt -> System.out.println(alt));

	// Assert.assertEquals(10, results.size());

	System.out.println("\n-------");
	System.out.println(results.size());
	System.out.println("\n-------");

    }
}
