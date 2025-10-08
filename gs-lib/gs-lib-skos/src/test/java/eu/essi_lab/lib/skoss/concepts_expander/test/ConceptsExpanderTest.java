/**
 * 
 */
package eu.essi_lab.lib.skoss.concepts_expander.test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.rdf4j.federated.FedXConfig;
import org.junit.Test;

import eu.essi_lab.lib.skoss.ConceptsExpander.ExpansionLevel;
import eu.essi_lab.lib.skoss.FedXEngine;
import eu.essi_lab.lib.skoss.SKOSResponse;
import eu.essi_lab.lib.skoss.SKOSResponseItem;
import eu.essi_lab.lib.skoss.concepts_expander.impl.DefaultExpandConceptsQueryBuilder;
import eu.essi_lab.lib.skoss.concepts_expander.impl.FedXConceptsExpander;
import eu.essi_lab.lib.skoss.concepts_expander.impl.ThreadMode;
import eu.essi_lab.lib.skoss.concepts_finder.impl.DefaultFedXConceptsQueryExecutor;
import eu.essi_lab.lib.skoss.concepts_finder.impl.DefaultFindConceptsQueryBuilder;
import eu.essi_lab.lib.skoss.concepts_finder.impl.FedXConceptsFinder;
import eu.essi_lab.lib.skoss.concepts_finder.impl.FedXMultiThreadConceptsFinder;

/**
 * @author Fabrizio
 */
public class ConceptsExpanderTest {

    @Test
    public void defaultTest() throws Exception {

	List<String> ontologyUrls = Arrays.asList(//
		// "http://localhost:3031/gemet/query", //
		"http://hydro.geodab.eu/hydro-ontology/sparql", //
		"https://vocabularies.unesco.org/sparql" //
	);

	List<String> sourceLangs = Arrays.asList("it", "en");

	List<String> searchLangs = Arrays.asList("it", "en");

	List<String> expansionRelations = Arrays.asList("skos:narrower", "skos:related");

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
		expansionRelations, //
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

	List<String> expansionRelations = Arrays.asList("skos:narrower", "skos:related");

	ExpansionLevel targetLevel = ExpansionLevel.MEDIUM;

	int limit = 200;

	//
	//
	//

	FedXConceptsFinder finder = new FedXConceptsFinder();

	finder.setEngine(FedXEngine.of(ontologyUrls, new FedXConfig()));
	finder.setQueryBuilder(new DefaultFindConceptsQueryBuilder());
	finder.setExecutor(new DefaultFedXConceptsQueryExecutor());

	List<String> concepts = finder.find("water", ontologyUrls, sourceLangs);

	//
	//
	//

	FedXConceptsExpander expander = new FedXConceptsExpander();

	expander.setEngine(FedXEngine.of(ontologyUrls, new FedXConfig()));
	expander.setQueryBuilder(new DefaultExpandConceptsQueryBuilder());
	expander.setThreadMode(ThreadMode.MULTI());

	//
	//
	//

	SKOSResponse response = expander.expand(//
		concepts, //
		ontologyUrls, //
		sourceLangs, //
		searchLangs, //
		expansionRelations, //
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

	List<String> expansionRelations = Arrays.asList("skos:narrower", "skos:related");

	ExpansionLevel targetLevel = ExpansionLevel.MEDIUM;

	int limit = 200;

	//
	//
	//

	FedXConceptsFinder finder = new FedXConceptsFinder();

	finder.setEngine(FedXEngine.of(ontologyUrls, new FedXConfig()));
	finder.setQueryBuilder(new DefaultFindConceptsQueryBuilder());
	finder.setExecutor(new DefaultFedXConceptsQueryExecutor());

	List<String> concepts = finder.find("water", ontologyUrls, sourceLangs);

	//
	//
	//

	FedXConceptsExpander expander = new FedXConceptsExpander();

	expander.setEngine(FedXEngine.of(ontologyUrls, new FedXConfig()));
	expander.setQueryBuilder(new DefaultExpandConceptsQueryBuilder());
	expander.setThreadMode(ThreadMode.SINGLE());

	//
	//
	//

	SKOSResponse response = expander.expand(//
		concepts, //
		ontologyUrls, //
		sourceLangs, //
		searchLangs, //
		expansionRelations, //
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
//		"https://dbpedia.org/sparql"
	);

	List<String> sourceLangs = Arrays.asList("it", "en");

	List<String> searchLangs = Arrays.asList("it", "en");

	List<String> expansionRelations = Arrays.asList("skos:semanticRelation");

	ExpansionLevel targetLevel = ExpansionLevel.MEDIUM;

	int limit = 200;

	//
	//
	//

	FedXMultiThreadConceptsFinder finder = new FedXMultiThreadConceptsFinder();

	finder.setConfiguration(new FedXConfig());
	finder.setQueryBuilder(new DefaultFindConceptsQueryBuilder());
	finder.setExecutor(new DefaultFedXConceptsQueryExecutor());

	List<String> concepts = finder.find("water", ontologyUrls, sourceLangs);

	//
	//
	//

	FedXConceptsExpander expander = new FedXConceptsExpander();

	expander.setEngine(FedXEngine.of(ontologyUrls, new FedXConfig()));
	expander.setQueryBuilder(new DefaultExpandConceptsQueryBuilder());
	expander.setThreadMode(ThreadMode.MULTI());

	//
	//
	//

	SKOSResponse response = expander.expand(//
		concepts, //
		ontologyUrls, //
		sourceLangs, //
		searchLangs, //
		expansionRelations, //
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
//		"https://dbpedia.org/sparql"
	);

	List<String> sourceLangs = Arrays.asList("it", "en");

	List<String> searchLangs = Arrays.asList("it", "en");

	List<String> expansionRelations = Arrays.asList("skos:narrower", "skos:related");

	ExpansionLevel targetLevel = ExpansionLevel.MEDIUM;

	int limit = 200;

	//
	//
	//

	FedXMultiThreadConceptsFinder finder = new FedXMultiThreadConceptsFinder();

	finder.setConfiguration(new FedXConfig());
	finder.setQueryBuilder(new DefaultFindConceptsQueryBuilder());
	finder.setExecutor(new DefaultFedXConceptsQueryExecutor());

	List<String> concepts = finder.find("water", ontologyUrls, sourceLangs);

	//
	//
	//

	FedXConceptsExpander expander = new FedXConceptsExpander();

	expander.setEngine(FedXEngine.of(ontologyUrls, new FedXConfig()));
	expander.setQueryBuilder(new DefaultExpandConceptsQueryBuilder());
	expander.setThreadMode(ThreadMode.MULTI(Executors.newFixedThreadPool(5)));

	//
	//
	//

	SKOSResponse response = expander.expand(//
		concepts, //
		ontologyUrls, //
		sourceLangs, //
		searchLangs, //
		expansionRelations, //
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
