/**
 * 
 */
package eu.essi_lab.lib.skoss.expander.functional.test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;

import org.eclipse.rdf4j.federated.FedXConfig;
import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.lib.skoss.SKOSConcept;
import eu.essi_lab.lib.skoss.SKOSResponse;
import eu.essi_lab.lib.skoss.SKOSSemanticRelation;
import eu.essi_lab.lib.skoss.ThreadMode;
import eu.essi_lab.lib.skoss.expander.ConceptsExpander.ExpansionLevel;
import eu.essi_lab.lib.skoss.expander.impl.DefaultExpandConceptsQueryBuilder;
import eu.essi_lab.lib.skoss.expander.impl.FedXConceptsExpander;
import eu.essi_lab.lib.skoss.finder.impl.DefaultConceptsQueryBuilder;
import eu.essi_lab.lib.skoss.finder.impl.FedXConceptsFinder;
import eu.essi_lab.lib.skoss.finder.impl.FedXConceptsQueryExecutor;

/**
 * @author Fabrizio
 */
public class FedXConceptsExpanderTest {

    @Test
    public void resultsTest() throws Exception {

	String searchTerm = "water";

	List<String> ontologyUrls = Arrays.asList(//
		// "http://localhost:3031/gemet/query", //
		"http://hydro.geodab.eu/hydro-ontology/sparql", //
		"https://vocabularies.unesco.org/sparql" //
	);

	List<String> sourceLangs = Arrays.asList("it", "en");
	List<String> searchLangs = Arrays.asList("it", "en");
	List<SKOSSemanticRelation> relations = Arrays.asList(SKOSSemanticRelation.NARROWER, SKOSSemanticRelation.RELATED);
	ExpansionLevel targetLevel = ExpansionLevel.MEDIUM;
	int limit = 50;

	FedXConceptsFinder finder = new FedXConceptsFinder();
	finder.setThreadMode(ThreadMode.MULTI());

	List<String> concepts = finder.find(searchTerm, ontologyUrls, sourceLangs);

	//
	//
	//

	FedXConceptsExpander expander1 = new FedXConceptsExpander();
	expander1.setTraceQuery(true);

	SKOSResponse response1 = expander1.expand(//
		concepts, //
		ontologyUrls, //
		sourceLangs, //
		searchLangs, //
		relations, //
		targetLevel, //
		limit);//

	//
	//
	//

	FedXConceptsExpander expander2 = new FedXConceptsExpander();

	SKOSResponse response2 = expander2.expand(//
		concepts, //
		ontologyUrls, //
		sourceLangs, //
		searchLangs, //
		relations, //
		targetLevel, //
		limit);//

	//
	//
	//

	response1.getResults().stream().sorted((r1, r2) -> r1.toString().compareTo(r2.toString()))
		.forEach(r -> System.out.println(r + "\n---"));
	
	System.out.println("\n\n\n");
	
	response2.getResults().stream().sorted((r1, r2) -> r1.toString().compareTo(r2.toString()))
		.forEach(r -> System.out.println(r + "\n---"));

	Assert.assertEquals(//
		response1.getResults().stream().sorted((r1, r2) -> r1.toString().compareTo(r2.toString())).toList().toString(), //
		response2.getResults().stream().sorted((r1, r2) -> r1.toString().compareTo(r2.toString())).toList().toString());//
    }

    @Test
    public void noLimitWaterSearchTermHydroOntologyTest() throws Exception {

	int expected = 58;

	//
	//
	//

	List<String> ontologyUrls = Arrays.asList(//
		"http://hydro.geodab.eu/hydro-ontology/sparql");

	List<String> sourceLangs = Arrays.asList("it", "en");
	List<String> searchLangs = Arrays.asList("it", "en");
	List<SKOSSemanticRelation> relations = Arrays.asList(SKOSSemanticRelation.NARROWER, SKOSSemanticRelation.RELATED);
	ExpansionLevel targetLevel = ExpansionLevel.HIGH;

	int limit = 1000;

	FedXConceptsFinder finder = new FedXConceptsFinder();
	finder.setThreadMode(ThreadMode.MULTI());

	List<String> concepts = finder.find("water", ontologyUrls, sourceLangs);

	//
	//
	//

	FedXConceptsExpander expander = new FedXConceptsExpander();
	expander.setThreadMode(ThreadMode.SINGLE()); // default

	SKOSResponse response = expander.expand(//
		concepts, //
		ontologyUrls, //
		sourceLangs, //
		searchLangs, //
		relations, //
		targetLevel, //
		limit);//

	Assert.assertEquals(expected, response.getResults().size());

	//
	//
	//

	expander = new FedXConceptsExpander();
	expander.setThreadMode(ThreadMode.MULTI());

	response = expander.expand(//
		concepts, //
		ontologyUrls, //
		sourceLangs, //
		searchLangs, //
		relations, //
		targetLevel, //
		limit);//

	Assert.assertEquals(expected, response.getResults().size());
    }

    @Test
    public void withLimitSearchTermWaterHydroOntologyTest() throws Exception {

	limitWaterHydroOntologyTest(5, 5);
	limitWaterHydroOntologyTest(11, 11);
	limitWaterHydroOntologyTest(15, 15);
	limitWaterHydroOntologyTest(33, 33);
	limitWaterHydroOntologyTest(3, 3);
	limitWaterHydroOntologyTest(7, 7);
	limitWaterHydroOntologyTest(23, 23);
	limitWaterHydroOntologyTest(57, 57);
	limitWaterHydroOntologyTest(100, 58);
    }

    /**
     * @param limit
     * @throws Exception
     */
    private void limitWaterHydroOntologyTest(int limit, int expected) throws Exception {

	//
	// finds the concepts
	//

	List<String> ontologyUrls = Arrays.asList(//
		"http://hydro.geodab.eu/hydro-ontology/sparql");

	List<String> sourceLangs = Arrays.asList("it", "en");
	List<String> searchLangs = Arrays.asList("it", "en");
	List<SKOSSemanticRelation> relations = Arrays.asList(SKOSSemanticRelation.NARROWER, SKOSSemanticRelation.RELATED);
	ExpansionLevel targetLevel = ExpansionLevel.HIGH;

	FedXConceptsFinder finder = new FedXConceptsFinder();
	finder.setThreadMode(ThreadMode.MULTI());

	List<String> concepts = finder.find("water", ontologyUrls, sourceLangs);

	//
	// expanding in SingleThread mode
	//

	FedXConceptsExpander expander = new FedXConceptsExpander();
	expander.setThreadMode(ThreadMode.SINGLE()); // default

	SKOSResponse response1 = expander.expand(//
		concepts, //
		ontologyUrls, //
		sourceLangs, //
		searchLangs, //
		relations, //
		targetLevel, //
		limit);//

	Assert.assertEquals(expected, response1.getResults().size());

	//
	// expanding in MultiThread mode, expecting the same results
	//

	expander = new FedXConceptsExpander();
	expander.setThreadMode(ThreadMode.MULTI());

	SKOSResponse response2 = expander.expand(//
		concepts, //
		ontologyUrls, //
		sourceLangs, //
		searchLangs, //
		relations, //
		targetLevel, //
		limit);//

	//
	// the equals test may fail since with multi thread mode the results may differ from single thread mode
	//

	// Assert.assertEquals(response1.getResults().stream().sorted().toList(),
	// response2.getResults().stream().sorted().toList());

	Assert.assertEquals(expected, response2.getResults().size());
    }

    @Test
    public void defaultTest() throws Exception {

	List<String> ontologyUrls = Arrays.asList(//
		// "http://localhost:3031/gemet/query", //
		"http://hydro.geodab.eu/hydro-ontology/sparql", //
		"https://vocabularies.unesco.org/sparql" //
	);

	List<String> sourceLangs = Arrays.asList("it", "en");
	List<String> searchLangs = Arrays.asList("it", "en");
	List<SKOSSemanticRelation> relations = Arrays.asList(SKOSSemanticRelation.NARROWER, SKOSSemanticRelation.RELATED);
	ExpansionLevel targetLevel = ExpansionLevel.MEDIUM;

	int limit = 10;

	//
	//
	//

	FedXConceptsFinder finder = new FedXConceptsFinder();

	List<String> concepts = finder.find("water", ontologyUrls, sourceLangs);

	FedXConceptsExpander expander = new FedXConceptsExpander();

	SKOSResponse response = expander.expand(//
		concepts, //
		ontologyUrls, //
		sourceLangs, //
		searchLangs, //
		relations, //
		targetLevel, //
		limit);//

	List<SKOSConcept> results = response.getResults().stream().//
		sorted((r1, r2) -> r1.toString().compareTo(r2.toString())). //
		toList();//

	System.out.println("\n\n");

	results.forEach(res -> System.out.println(res + "\n---"));

	System.out.println("\n\n");

	response.getPrefLabels().forEach(pref -> System.out.println(pref));

	System.out.println("\n\n");

	response.getAltLabels().forEach(alt -> System.out.println(alt));

	Assert.assertEquals(10, results.size());
    }

    @Test
    public void defaultWithParamsTest() throws Exception {

	List<String> ontologyUrls = Arrays.asList(//
		// "http://localhost:3031/gemet/query", //
		"http://hydro.geodab.eu/hydro-ontology/sparql", //
		"https://vocabularies.unesco.org/sparql" //
	);

	List<String> sourceLangs = Arrays.asList("it", "en");
	List<String> searchLangs = Arrays.asList("it", "en");
	List<SKOSSemanticRelation> relations = Arrays.asList(SKOSSemanticRelation.NARROWER, SKOSSemanticRelation.RELATED);
	ExpansionLevel targetLevel = ExpansionLevel.MEDIUM;

	int limit = 10;

	//
	//
	//

	FedXConceptsFinder finder = new FedXConceptsFinder();
	finder.setQueryBuilder(new DefaultConceptsQueryBuilder());

	FedXConceptsQueryExecutor executor = new FedXConceptsQueryExecutor();
	executor.setEngineConfig(new FedXConfig());
	executor.setTraceQuery(false);

	finder.setExecutor(executor);

	List<String> concepts = finder.find("water", ontologyUrls, sourceLangs);

	//
	//
	//

	FedXConceptsExpander expander = new FedXConceptsExpander();

	expander.setEngineConfig(new FedXConfig());
	expander.setQueryBuilder(new DefaultExpandConceptsQueryBuilder());
	expander.setThreadMode(ThreadMode.MULTI());
	expander.setTraceQuery(false);

	//
	//
	//

	SKOSResponse response = expander.expand(//
		concepts, //
		ontologyUrls, //
		sourceLangs, //
		searchLangs, //
		relations, //
		targetLevel, //
		limit);//

	List<SKOSConcept> results = response.getResults().stream().//
		sorted((r1, r2) -> r1.toString().compareTo(r2.toString())). //
		toList();//

	System.out.println("\n\n");

	results.forEach(res -> System.out.println(res + "\n---"));

	System.out.println("\n\n");

	response.getPrefLabels().forEach(pref -> System.out.println(pref));

	System.out.println("\n\n");

	response.getAltLabels().forEach(alt -> System.out.println(alt));

	Assert.assertEquals(10, results.size());
    }

    @Test
    public void multiThreadConceptsFinderAndExpanderTest() throws Exception {

	List<String> ontologyUrls = Arrays.asList(//
		"http://localhost:3031/gemet/query", //
		"http://hydro.geodab.eu/hydro-ontology/sparql", //
		"https://vocabularies.unesco.org/sparql" //
	// "https://dbpedia.org/sparql"
	);

	List<String> sourceLangs = Arrays.asList("it", "en");
	List<String> searchLangs = Arrays.asList("it", "en");
	List<SKOSSemanticRelation> relations = Arrays.asList(SKOSSemanticRelation.NARROWER, SKOSSemanticRelation.RELATED);
	ExpansionLevel targetLevel = ExpansionLevel.MEDIUM;

	int limit = 100;

	//
	//
	//

	FedXConceptsFinder finder = new FedXConceptsFinder();
	finder.setThreadMode(ThreadMode.MULTI());
	finder.setQueryBuilder(new DefaultConceptsQueryBuilder());

	FedXConceptsQueryExecutor executor = new FedXConceptsQueryExecutor();
	executor.setEngineConfig(new FedXConfig());
	executor.setTraceQuery(false);

	finder.setExecutor(executor);

	List<String> concepts = finder.find("water", ontologyUrls, sourceLangs);

	//
	//
	//

	FedXConceptsExpander expander = new FedXConceptsExpander();

	expander.setEngineConfig(new FedXConfig());
	expander.setQueryBuilder(new DefaultExpandConceptsQueryBuilder());
	expander.setThreadMode(ThreadMode.MULTI());
	expander.setTraceQuery(false);

	//
	//
	//

	SKOSResponse response = expander.expand(//
		concepts, //
		ontologyUrls, //
		sourceLangs, //
		searchLangs, //
		relations, //
		targetLevel, //
		limit);//

	List<SKOSConcept> results = response.getResults().stream().//
		sorted((r1, r2) -> r1.toString().compareTo(r2.toString())). //
		toList();//

	System.out.println("\n\n");

	results.forEach(res -> System.out.println(res + "\n---"));

	System.out.println("\n\n");

	response.getPrefLabels().forEach(pref -> System.out.println(pref));

	System.out.println("\n\n");

	response.getAltLabels().forEach(alt -> System.out.println(alt));

	Assert.assertEquals(100, results.size());
    }

   

    @Test
    public void multiThreadConceptsFinderWithFixedThreadPoolExecutorTest() throws Exception {

	List<String> ontologyUrls = Arrays.asList(//
		"http://localhost:3031/gemet/query", //
		"http://hydro.geodab.eu/hydro-ontology/sparql", //
		"https://vocabularies.unesco.org/sparql" //
	// "https://dbpedia.org/sparql"
	);

	List<String> sourceLangs = Arrays.asList("it", "en");

	List<String> searchLangs = Arrays.asList("it", "en");

	List<SKOSSemanticRelation> relations = Arrays.asList(SKOSSemanticRelation.NARROWER, SKOSSemanticRelation.RELATED);

	ExpansionLevel targetLevel = ExpansionLevel.MEDIUM;

	int limit = 10;

	//
	//
	//

	FedXConceptsFinder finder = new FedXConceptsFinder();
	finder.setThreadMode(ThreadMode.MULTI());
	finder.setQueryBuilder(new DefaultConceptsQueryBuilder());

	FedXConceptsQueryExecutor executor = new FedXConceptsQueryExecutor();
	executor.setTraceQuery(false);
	executor.setEngineConfig(new FedXConfig());

	finder.setExecutor(executor);

	List<String> concepts = finder.find("water", ontologyUrls, sourceLangs);

	//
	//
	//

	FedXConceptsExpander expander = new FedXConceptsExpander();

	expander.setEngineConfig(new FedXConfig());
	expander.setQueryBuilder(new DefaultExpandConceptsQueryBuilder());
	// limit the thread pool to 5 threads
	expander.setThreadMode(ThreadMode.MULTI(Executors.newFixedThreadPool(5)));
	expander.setTraceQuery(false);

	//
	//
	//

	SKOSResponse response = expander.expand(//
		concepts, //
		ontologyUrls, //
		sourceLangs, //
		searchLangs, //
		relations, //
		targetLevel, //
		limit);//

	List<SKOSConcept> results = response.getResults().stream().//
		sorted((r1, r2) -> r1.toString().compareTo(r2.toString())). //
		toList();//

	System.out.println("\n\n");

	results.forEach(res -> System.out.println(res + "\n---"));

	System.out.println("\n\n");

	response.getPrefLabels().forEach(pref -> System.out.println(pref));

	System.out.println("\n\n");

	response.getAltLabels().forEach(alt -> System.out.println(alt));

	Assert.assertEquals(10, results.size());
    }
}
