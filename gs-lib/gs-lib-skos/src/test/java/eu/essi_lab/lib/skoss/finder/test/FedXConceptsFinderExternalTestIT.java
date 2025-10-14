/**
 * 
 */
package eu.essi_lab.lib.skoss.finder.test;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.lib.skoss.ThreadMode;
import eu.essi_lab.lib.skoss.finder.ConceptsFinder;
import eu.essi_lab.lib.skoss.finder.impl.DefaultConceptsQueryBuilder;
import eu.essi_lab.lib.skoss.finder.impl.FedXConceptsFinder;
import eu.essi_lab.lib.skoss.finder.impl.FedXConceptsQueryExecutor;

/**
 * @author Fabrizio
 */
public class FedXConceptsFinderExternalTestIT {

    @Test
    public void defaultWithParamsTest() throws Exception {

	List<String> ontologyUrls = Arrays.asList(//
		"http://hydro.geodab.eu/hydro-ontology/sparql", //
		"https://vocabularies.unesco.org/sparql", //
		"https://dbpedia.org/sparql");

	FedXConceptsFinder finder = new FedXConceptsFinder();
	
	finder.setExecutor(new FedXConceptsQueryExecutor());
	finder.setQueryBuilder(new DefaultConceptsQueryBuilder());
	finder.setThreadMode(ThreadMode.SINGLE());

	List<String> concepts = finder.find("water", ontologyUrls, Arrays.asList("it", "en")).//
		stream().//
		sorted().//
		toList();

	Assert.assertEquals(2, concepts.size());

	Assert.assertEquals("http://hydro.geodab.eu/hydro-ontology/concept/97", concepts.get(0));
	Assert.assertEquals("http://vocabularies.unesco.org/thesaurus/concept189", concepts.get(1));
    }

    @Test
    public void singleThreadTest1() throws Exception {

	List<String> ontologyUrls = Arrays.asList(//
		"http://hydro.geodab.eu/hydro-ontology/sparql", //
		"https://vocabularies.unesco.org/sparql", //
		"https://dbpedia.org/sparql");

	ConceptsFinder finder = new FedXConceptsFinder();

	List<String> concepts = finder.find("water", ontologyUrls, Arrays.asList("it", "en")).//
		stream().//
		sorted().//
		toList();

	Assert.assertEquals(2, concepts.size());

	Assert.assertEquals("http://hydro.geodab.eu/hydro-ontology/concept/97", concepts.get(0));
	Assert.assertEquals("http://vocabularies.unesco.org/thesaurus/concept189", concepts.get(1));
    }

    @Test
    public void multiThreadTest1() throws Exception {

	List<String> ontologyUrls = Arrays.asList(//
		"http://hydro.geodab.eu/hydro-ontology/sparql", //
		"https://vocabularies.unesco.org/sparql", //
		"https://dbpedia.org/sparql");

	FedXConceptsFinder finder = new FedXConceptsFinder();
	finder.setThreadMode(ThreadMode.MULTI());

	List<String> concepts = finder.find("water", ontologyUrls, Arrays.asList("it", "en")).//
		stream().//
		sorted().//
		toList();

	Assert.assertEquals(3, concepts.size());

	Assert.assertEquals("http://dbpedia.org/resource/Category:Water", concepts.get(0));
	Assert.assertEquals("http://hydro.geodab.eu/hydro-ontology/concept/97", concepts.get(1));
	Assert.assertEquals("http://vocabularies.unesco.org/thesaurus/concept189", concepts.get(2));
    }

    @Test
    public void singleThreadTest2() throws Exception {

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
    public void multiThreadTest2() throws Exception {

	List<String> ontologyUrls = Arrays.asList(//
		"http://localhost:3031/gemet/query", //
		"http://hydro.geodab.eu/hydro-ontology/sparql", //
		"https://vocabularies.unesco.org/sparql", //
		"https://dbpedia.org/sparql");

	FedXConceptsFinder finder = new FedXConceptsFinder();
	finder.setThreadMode(ThreadMode.MULTI());

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
