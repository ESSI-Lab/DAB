/**
 * 
 */
package eu.essi_lab.lib.skoss.concepts_finder.test;

import java.util.Arrays;
import java.util.List;

import org.eclipse.rdf4j.federated.FedXConfig;
import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.lib.skoss.ConceptsFinder;
import eu.essi_lab.lib.skoss.concepts_finder.impl.DefaultFedXConceptsQueryExecutor;
import eu.essi_lab.lib.skoss.concepts_finder.impl.DefaultFindConceptsQueryBuilder;
import eu.essi_lab.lib.skoss.concepts_finder.impl.FedXMultiThreadConceptsFinder;

/**
 * @author Fabrizio
 */
public class MultiThreadConceptsFinderTest {

    @Test
    public void defaultTest() throws Exception {

	List<String> ontologyUrls = Arrays.asList(//
		// "http://localhost:3031/gemet/query", //
		"http://hydro.geodab.eu/hydro-ontology/sparql", //
		"https://vocabularies.unesco.org/sparql", //
		"https://dbpedia.org/sparql");

	ConceptsFinder finder = new FedXMultiThreadConceptsFinder();

	List<String> concepts = finder.find("water", ontologyUrls, Arrays.asList("it", "en")).//
		stream().//
		sorted().//
		toList();

	Assert.assertEquals(4, concepts.size());

	Assert.assertEquals("http://dbpedia.org/resource/Category:Water", concepts.get(0));
	Assert.assertEquals("http://hydro.geodab.eu/hydro-ontology/concept/97", concepts.get(1));
	Assert.assertEquals("http://vocabularies.unesco.org/thesaurus/concept189", concepts.get(2));
	Assert.assertEquals("http://www.eionet.europa.eu/gemet/concept/9242", concepts.get(3));
    }

    @Test
    public void defaultWithParamsTest() throws Exception {

	List<String> ontologyUrls = Arrays.asList(//
		"http://localhost:3031/gemet/query", //
		"http://hydro.geodab.eu/hydro-ontology/sparql", //
		"https://vocabularies.unesco.org/sparql", //
		"https://dbpedia.org/sparql");

	FedXMultiThreadConceptsFinder finder = new FedXMultiThreadConceptsFinder();

	finder.setConfiguration(new FedXConfig());

	DefaultFedXConceptsQueryExecutor executor = new DefaultFedXConceptsQueryExecutor();
	executor.setTraceQuery(false);

	finder.setExecutor(executor);
	
	finder.setQueryBuilder(new DefaultFindConceptsQueryBuilder());

	List<String> concepts = finder.find("water", ontologyUrls, Arrays.asList("it", "en")).//
		stream().//
		sorted().//
		toList();

	Assert.assertEquals(4, concepts.size());

	Assert.assertEquals("http://dbpedia.org/resource/Category:Water", concepts.get(0));
	Assert.assertEquals("http://hydro.geodab.eu/hydro-ontology/concept/97", concepts.get(1));
	Assert.assertEquals("http://vocabularies.unesco.org/thesaurus/concept189", concepts.get(2));
	Assert.assertEquals("http://www.eionet.europa.eu/gemet/concept/9242", concepts.get(3));
    }
}
