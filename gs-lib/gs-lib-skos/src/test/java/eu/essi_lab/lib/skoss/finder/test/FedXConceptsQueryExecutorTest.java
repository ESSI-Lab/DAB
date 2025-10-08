/**
 * 
 */
package eu.essi_lab.lib.skoss.finder.test;

import java.util.Arrays;
import java.util.List;

import org.eclipse.rdf4j.federated.FedXConfig;
import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.lib.skoss.fedx.FedXEngine;
import eu.essi_lab.lib.skoss.finder.impl.DefaultFindConceptsQueryBuilder;
import eu.essi_lab.lib.skoss.finder.impl.FedXConceptsQueryExecutor;

/**
 * @author Fabrizio
 */
public class FedXConceptsQueryExecutorTest {

    @Test
    public void fedXConceptsQueryExecutorTest() throws Exception {

	List<String> ontologyUrls = Arrays.asList(//
		// "http://localhost:3031/gemet/query", //
		"http://hydro.geodab.eu/hydro-ontology/sparql", //
		"https://vocabularies.unesco.org/sparql" //
	);

	List<String> sourceLangs = Arrays.asList("it", "en");

	String searchTerm = "water";

	FedXConceptsQueryExecutor executor = new FedXConceptsQueryExecutor();

	DefaultFindConceptsQueryBuilder queryBuilder = new DefaultFindConceptsQueryBuilder();

	List<String> concepts = executor.execute(//
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

    @Test
    public void fedXConceptsQueryExecutorWithParamsTest() throws Exception {

	List<String> ontologyUrls = Arrays.asList(//
		// "http://localhost:3031/gemet/query", //
		"http://hydro.geodab.eu/hydro-ontology/sparql", //
		"https://vocabularies.unesco.org/sparql" //
	);

	List<String> sourceLangs = Arrays.asList("it", "en");

	String searchTerm = "water";

	FedXConceptsQueryExecutor executor = new FedXConceptsQueryExecutor();
	executor.setTraceQuery(true);

	// with this builder a particular configuration can be applied
	executor.setEngineBuilder(ontUrls -> FedXEngine.of(ontUrls, new FedXConfig()));

	DefaultFindConceptsQueryBuilder queryBuilder = new DefaultFindConceptsQueryBuilder();

	List<String> concepts = executor.execute(//
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
