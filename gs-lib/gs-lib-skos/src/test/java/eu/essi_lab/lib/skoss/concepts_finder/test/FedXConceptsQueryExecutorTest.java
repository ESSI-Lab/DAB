/**
 * 
 */
package eu.essi_lab.lib.skoss.concepts_finder.test;

import java.util.Arrays;
import java.util.List;

import org.eclipse.rdf4j.federated.FedXConfig;
import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.lib.skoss.FedXEngine;
import eu.essi_lab.lib.skoss.concepts_finder.impl.DefaultFedXConceptsQueryExecutor;
import eu.essi_lab.lib.skoss.concepts_finder.impl.DefaultFindConceptsQueryBuilder;

/**
 * @author Fabrizio
 */
public class FedXConceptsQueryExecutorTest {

    @Test
    public void defaultFedXConceptsQueryExecutorTest() throws Exception {

	List<String> ontologyUrls = Arrays.asList(//
		// "http://localhost:3031/gemet/query", //
		"http://hydro.geodab.eu/hydro-ontology/sparql", //
		"https://vocabularies.unesco.org/sparql" //
	);

	List<String> sourceLangs = Arrays.asList("it", "en");

	String searchTerm = "water";

	DefaultFedXConceptsQueryExecutor executor = new DefaultFedXConceptsQueryExecutor();
	executor.setTraceQuery(true);

	FedXEngine engine = FedXEngine.of(ontologyUrls, new FedXConfig());

	DefaultFindConceptsQueryBuilder queryBuilder = new DefaultFindConceptsQueryBuilder();

	List<String> concepts = executor.execute(//
		engine, //
		queryBuilder, //
		searchTerm, //
		ontologyUrls, //
		sourceLangs).//
		stream().//
		sorted().//
		toList();

	Assert.assertEquals("http://hydro.geodab.eu/hydro-ontology/concept/97", concepts.get(0));
	Assert.assertEquals("http://vocabularies.unesco.org/thesaurus/concept189", concepts.get(1));
    }
}
