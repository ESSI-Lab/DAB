/**
 * 
 */
package eu.essi_lab.lib.skoss.finder.functional.test;

import java.util.Arrays;
import java.util.List;

import org.eclipse.rdf4j.federated.FedXConfig;
import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.lib.skoss.finder.impl.DefaultConceptsQueryBuilder;
import eu.essi_lab.lib.skoss.finder.impl.FedXConceptsQueryExecutor;

/**
 * @author Fabrizio
 */
public class FedXConceptsQueryExecutorTest {

    @Test
    public void defaultTest() throws Exception {

	List<String> ontologyUrls = Arrays.asList(//
		// "http://localhost:3031/gemet/query", //
		"http://hydro.geodab.eu/hydro-ontology/sparql", //
		"https://vocabularies.unesco.org/sparql" //
	);

	List<String> sourceLangs = Arrays.asList("it", "en");

	String searchTerm = "water";

	FedXConceptsQueryExecutor executor = new FedXConceptsQueryExecutor();

	DefaultConceptsQueryBuilder queryBuilder = new DefaultConceptsQueryBuilder();

	List<String> concepts = executor.execute(//
		queryBuilder, //
		searchTerm, //
		ontologyUrls, //
		sourceLangs).//
		stream().//
		sorted().//
		toList();

	Assert.assertEquals(2, concepts.size());

	Assert.assertEquals("http://hydro.geodab.eu/hydro-ontology/concept/97", concepts.get(0));
	Assert.assertEquals("http://vocabularies.unesco.org/thesaurus/concept189", concepts.get(1));
    }

    @Test
    public void defaultTest2() throws Exception {

	List<String> ontologyUrls = Arrays.asList(//
		"http://localhost:3031/gemet/query", //
		"http://hydro.geodab.eu/hydro-ontology/sparql", //
		"https://vocabularies.unesco.org/sparql" //
	);

	List<String> sourceLangs = Arrays.asList("it", "en");

	String searchTerm = "water";

	FedXConceptsQueryExecutor executor = new FedXConceptsQueryExecutor();

	DefaultConceptsQueryBuilder queryBuilder = new DefaultConceptsQueryBuilder();

	List<String> concepts = executor.execute(//
		queryBuilder, //
		searchTerm, //
		ontologyUrls, //
		sourceLangs).//
		stream().//
		sorted().//
		toList();

	Assert.assertEquals(3, concepts.size());

	Assert.assertEquals("http://hydro.geodab.eu/hydro-ontology/concept/97", concepts.get(0));
	Assert.assertEquals("http://vocabularies.unesco.org/thesaurus/concept189", concepts.get(1));
	Assert.assertEquals("http://www.eionet.europa.eu/gemet/concept/9242", concepts.get(2));
    }

    @Test
    public void withParamsTest() throws Exception {

	List<String> ontologyUrls = Arrays.asList(//
		// "http://localhost:3031/gemet/query", //
		"http://hydro.geodab.eu/hydro-ontology/sparql", //
		"https://vocabularies.unesco.org/sparql" //
	);

	List<String> sourceLangs = Arrays.asList("it", "en");

	String searchTerm = "water";

	FedXConceptsQueryExecutor executor = new FedXConceptsQueryExecutor();
	executor.setTraceQuery(true);
	executor.setEngineConfig(new FedXConfig());

	DefaultConceptsQueryBuilder queryBuilder = new DefaultConceptsQueryBuilder();

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
    public void withParamsTest2() throws Exception {

	List<String> ontologyUrls = Arrays.asList(//
		"http://localhost:3031/gemet/query", //
		"http://hydro.geodab.eu/hydro-ontology/sparql", //
		"https://vocabularies.unesco.org/sparql" //
	);

	List<String> sourceLangs = Arrays.asList("it", "en");

	String searchTerm = "water";

	FedXConceptsQueryExecutor executor = new FedXConceptsQueryExecutor();
	executor.setTraceQuery(true);
	executor.setEngineConfig(new FedXConfig());

	DefaultConceptsQueryBuilder queryBuilder = new DefaultConceptsQueryBuilder();

	List<String> concepts = executor.execute(//
		queryBuilder, //
		searchTerm, //
		ontologyUrls, //
		sourceLangs).//
		stream().//
		sorted().//
		toList();

	Assert.assertEquals(3, concepts.size());

	Assert.assertEquals("http://hydro.geodab.eu/hydro-ontology/concept/97", concepts.get(0));
	Assert.assertEquals("http://vocabularies.unesco.org/thesaurus/concept189", concepts.get(1));
	Assert.assertEquals("http://www.eionet.europa.eu/gemet/concept/9242", concepts.get(2));
    }
}
