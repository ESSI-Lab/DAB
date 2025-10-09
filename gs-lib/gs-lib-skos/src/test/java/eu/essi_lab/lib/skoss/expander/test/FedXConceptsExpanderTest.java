/**
 * 
 */
package eu.essi_lab.lib.skoss.expander.test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;

import org.eclipse.rdf4j.federated.FedXConfig;
import org.junit.Test;

import eu.essi_lab.lib.skoss.SKOSResponse;
import eu.essi_lab.lib.skoss.SKOSResponseItem;
import eu.essi_lab.lib.skoss.SKOSSemanticRelation;
import eu.essi_lab.lib.skoss.ThreadMode;
import eu.essi_lab.lib.skoss.expander.ConceptsExpander.ExpansionLevel;
import eu.essi_lab.lib.skoss.expander.impl.CloseMatchExpandConceptsQueryBuilder;
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

	int limit = 200;

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

	List<SKOSResponseItem> results = response.getResults().stream().//
		sorted((r1, r2) -> r1.toString().compareTo(r2.toString())). //
		toList();//

	System.out.println("\n\n");

	results.forEach(res -> System.out.println(res + "\n---"));

	System.out.println("\n\n");

	response.getPrefLabels().forEach(pref -> System.out.println(pref));

	System.out.println("\n\n");

	response.getAltLabels().forEach(alt -> System.out.println(alt));
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

	int limit = 200;

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

	List<SKOSResponseItem> results = response.getResults().stream().//
		sorted((r1, r2) -> r1.toString().compareTo(r2.toString())). //
		toList();//

	System.out.println("\n\n");

	results.forEach(res -> System.out.println(res + "\n---"));

	System.out.println("\n\n");

	response.getPrefLabels().forEach(pref -> System.out.println(pref));

	System.out.println("\n\n");

	response.getAltLabels().forEach(alt -> System.out.println(alt));
    }

    @Test
    public void singleThreadTest() throws Exception {

	List<String> ontologyUrls = Arrays.asList(//
		// "http://localhost:3031/gemet/query", //
		"http://hydro.geodab.eu/hydro-ontology/sparql", //
		"https://vocabularies.unesco.org/sparql" //
	);

	List<String> sourceLangs = Arrays.asList("it", "en");

	List<String> searchLangs = Arrays.asList("it", "en");

	List<SKOSSemanticRelation> relations = Arrays.asList(SKOSSemanticRelation.NARROWER, SKOSSemanticRelation.RELATED);

	ExpansionLevel targetLevel = ExpansionLevel.MEDIUM;

	int limit = 200;

	//
	//
	//

	FedXConceptsFinder finder = new FedXConceptsFinder();

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
	expander.setThreadMode(ThreadMode.SINGLE());
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

	List<SKOSResponseItem> results = response.getResults().stream().//
		sorted((r1, r2) -> r1.toString().compareTo(r2.toString())). //
		toList();//

	System.out.println("\n\n");

	results.forEach(res -> System.out.println(res + "\n---"));

	System.out.println("\n\n");

	response.getPrefLabels().forEach(pref -> System.out.println(pref));

	System.out.println("\n\n");

	response.getAltLabels().forEach(alt -> System.out.println(alt));
    }

    @Test
    public void multiThreadConceptsFinderTest() throws Exception {

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

	int limit = 200;

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

	List<SKOSResponseItem> results = response.getResults().stream().//
		sorted((r1, r2) -> r1.toString().compareTo(r2.toString())). //
		toList();//

	System.out.println("\n\n");

	results.forEach(res -> System.out.println(res + "\n---"));

	System.out.println("\n\n");

	response.getPrefLabels().forEach(pref -> System.out.println(pref));

	System.out.println("\n\n");

	response.getAltLabels().forEach(alt -> System.out.println(alt));
    }

    @Test
    public void multiThreadConceptsFinderTestWithCloseMatchExpandQueryBuilderWithQueryTracing() throws Exception {

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

	int limit = 200;

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
	expander.setQueryBuilder(new CloseMatchExpandConceptsQueryBuilder());
	expander.setThreadMode(ThreadMode.MULTI());
	expander.setTraceQuery(true);

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

	List<SKOSResponseItem> results = response.getResults().stream().//
		sorted((r1, r2) -> r1.toString().compareTo(r2.toString())). //
		toList();//

	System.out.println("\n\n");

	results.forEach(res -> System.out.println(res + "\n---"));

	System.out.println("\n\n");

	response.getPrefLabels().forEach(pref -> System.out.println(pref));

	System.out.println("\n\n");

	response.getAltLabels().forEach(alt -> System.out.println(alt));
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

	int limit = 200;

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

	List<SKOSResponseItem> results = response.getResults().stream().//
		sorted((r1, r2) -> r1.toString().compareTo(r2.toString())). //
		toList();//

	System.out.println("\n\n");

	results.forEach(res -> System.out.println(res + "\n---"));

	System.out.println("\n\n");

	response.getPrefLabels().forEach(pref -> System.out.println(pref));

	System.out.println("\n\n");

	response.getAltLabels().forEach(alt -> System.out.println(alt));
    }
}
