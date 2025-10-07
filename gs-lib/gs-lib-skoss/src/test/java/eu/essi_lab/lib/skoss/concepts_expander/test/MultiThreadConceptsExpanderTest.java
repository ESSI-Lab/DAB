/**
 * 
 */
package eu.essi_lab.lib.skoss.concepts_expander.test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;

import org.eclipse.rdf4j.federated.FedXConfig;
import org.junit.Test;

import eu.essi_lab.lib.skoss.ConceptsExpander.ExpansionLevel;
import eu.essi_lab.lib.skoss.FedXEngine;
import eu.essi_lab.lib.skoss.SKOSSResponse;
import eu.essi_lab.lib.skoss.SKOSSResponseItem;
import eu.essi_lab.lib.skoss.concepts_expander.impl.DefaultExpandConceptsQueryBuilder;
import eu.essi_lab.lib.skoss.concepts_expander.impl.FedXMultiThreadConceptsExpander;
import eu.essi_lab.lib.skoss.concepts_finder.impl.DefaultFedXConceptsQueryExecutor;
import eu.essi_lab.lib.skoss.concepts_finder.impl.DefaultFindConceptsQueryBuilder;
import eu.essi_lab.lib.skoss.concepts_finder.impl.FedXConceptsFinder;

/**
 * @author Fabrizio  
 */
public class MultiThreadConceptsExpanderTest {

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

	FedXMultiThreadConceptsExpander expander = new FedXMultiThreadConceptsExpander();

	SKOSSResponse response = expander.expand(//
		concepts, //
		ontologyUrls, //
		sourceLangs, //
		searchLangs, //
		expansionRelations, //
		targetLevel, //
		limit);//

	List<SKOSSResponseItem> results = response.getResults().stream().//
		sorted((r1, r2) -> r1.toString().compareTo(r2.toString())). //
		toList();//

	System.out.println("\n\n");

//	results.forEach(res -> System.out.println(res + "\n---"));

	System.out.println("\n\n");

	response.getPrefLabels().forEach(pref -> System.out.println(pref));

	System.out.println("\n\n");

//	response.getAltLabels().forEach(alt -> System.out.println(alt));
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

	FedXMultiThreadConceptsExpander expander = new FedXMultiThreadConceptsExpander();

	expander.setEngine(FedXEngine.of(ontologyUrls, new FedXConfig()));
	expander.setQueryBuilder(new DefaultExpandConceptsQueryBuilder());
	expander.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
	
	//
	//
	//

	SKOSSResponse response = expander.expand(//
		concepts, //
		ontologyUrls, //
		sourceLangs, //
		searchLangs, //
		expansionRelations, //
		targetLevel, //
		limit);//

	List<SKOSSResponseItem> results = response.getResults().stream().//
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
