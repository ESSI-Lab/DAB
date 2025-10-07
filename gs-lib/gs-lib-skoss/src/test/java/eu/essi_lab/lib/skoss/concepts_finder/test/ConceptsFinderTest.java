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
import eu.essi_lab.lib.skoss.FedXEngine;
import eu.essi_lab.lib.skoss.concepts_finder.impl.DefaultFedXConceptsQueryExecutor;
import eu.essi_lab.lib.skoss.concepts_finder.impl.DefaultFindConceptsQueryBuilder;
import eu.essi_lab.lib.skoss.concepts_finder.impl.FedXConceptsFinder;

/**
 * @author Fabrizio
 */
public class ConceptsFinderTest {

    @Test
    public void defaultTest() throws Exception {

	List<String> ontologyUrls = Arrays.asList(//
		"http://localhost:3031/gemet/query", //
		"http://hydro.geodab.eu/hydro-ontology/sparql", //
		"https://vocabularies.unesco.org/sparql", //
		"https://dbpedia.org/sparql");

	ConceptsFinder finder = new FedXConceptsFinder();

	List<String> concepts = finder.find("water", ontologyUrls, Arrays.asList("it", "en")).//
		stream().//
		sorted().//
		toList();

	Assert.assertEquals(3, concepts.size());

	Assert.assertEquals("http://hydro.geodab.eu/hydro-ontology/concept/97", concepts.get(0));
	Assert.assertEquals("http://vocabularies.unesco.org/thesaurus/concept189", concepts.get(1));
	Assert.assertEquals("http://www.eionet.europa.eu/gemet/concept/9242", concepts.get(2));
    }

    @Test
    public void defaultWithParamsTest() throws Exception {

	List<String> ontologyUrls = Arrays.asList(//
		"http://localhost:3031/gemet/query", //
		"http://hydro.geodab.eu/hydro-ontology/sparql", //
		"https://vocabularies.unesco.org/sparql", //
		"https://dbpedia.org/sparql");

	FedXConceptsFinder finder = new FedXConceptsFinder();

	finder.setEngine(FedXEngine.of(ontologyUrls, new FedXConfig()));
	finder.setQueryBuilder(new DefaultFindConceptsQueryBuilder());
	finder.setExecutor(new DefaultFedXConceptsQueryExecutor());

	//
	//
	//

	List<String> concepts = finder.find("water", ontologyUrls, Arrays.asList("it", "en")).//
		stream().//
		sorted().//
		toList();

	Assert.assertEquals(3, concepts.size());

	Assert.assertEquals("http://hydro.geodab.eu/hydro-ontology/concept/97", concepts.get(0));
	Assert.assertEquals("http://vocabularies.unesco.org/thesaurus/concept189", concepts.get(1));
	Assert.assertEquals("http://www.eionet.europa.eu/gemet/concept/9242", concepts.get(2));
    }
}
