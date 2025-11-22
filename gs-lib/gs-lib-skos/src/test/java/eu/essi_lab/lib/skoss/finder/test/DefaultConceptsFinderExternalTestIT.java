/**
 * 
 */
package eu.essi_lab.lib.skoss.finder.test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.lib.skos.finder.impl.DefaultConceptsFinder;
import eu.essi_lab.lib.skos.finder.impl.DefaultConceptsQueryBuilder;
import eu.essi_lab.lib.utils.ThreadMode;

/**
 * @author Fabrizio
 */
public class DefaultConceptsFinderExternalTestIT {

    @Test
    public void defaultTest() throws Exception {

	List<String> ontologyUrls = Arrays.asList(//
		"http://hydro.geodab.eu/hydro-ontology/sparql", //
		"https://vocabularies.unesco.org/sparql" //
	// ,"https://dbpedia.org/sparql"
	);

	DefaultConceptsFinder finder = new DefaultConceptsFinder();

	finder.setTraceQuery(true);
	finder.setQueryBuilder(new DefaultConceptsQueryBuilder());
	finder.setThreadMode(ThreadMode.MULTI(() -> Executors.newFixedThreadPool(4)));
	finder.setTaskConsumer(task -> task.setMaxExecutionTime(1));

	List<String> concepts = finder.find("water", ontologyUrls, Arrays.asList("it", "en")).//
		stream().//
		sorted().//
		toList();

	Assert.assertEquals(2, concepts.size());

	Assert.assertEquals("http://hydro.geodab.eu/hydro-ontology/concept/97", concepts.get(0));
	Assert.assertEquals("http://vocabularies.unesco.org/thesaurus/concept189", concepts.get(1));
    }
    
    @Test
    public void singleThreadTest() throws Exception {

	List<String> ontologyUrls = Arrays.asList(//
		"http://hydro.geodab.eu/hydro-ontology/sparql", //
		"https://vocabularies.unesco.org/sparql" //
	// ,"https://dbpedia.org/sparql"
	);

	DefaultConceptsFinder finder = new DefaultConceptsFinder();

	finder.setTraceQuery(true);
	finder.setQueryBuilder(new DefaultConceptsQueryBuilder());
	finder.setThreadMode(ThreadMode.SINGLE());
	finder.setTaskConsumer(task -> task.setMaxExecutionTime(1));

	List<String> concepts = finder.find("water", ontologyUrls, Arrays.asList("it", "en")).//
		stream().//
		sorted().//
		toList();

	Assert.assertEquals(2, concepts.size());

	Assert.assertEquals("http://hydro.geodab.eu/hydro-ontology/concept/97", concepts.get(0));
	Assert.assertEquals("http://vocabularies.unesco.org/thesaurus/concept189", concepts.get(1));
    }

    @Test
    public void testWithoutLanguages() throws Exception {

	List<String> ontologyUrls = Arrays.asList(//
		"http://hydro.geodab.eu/hydro-ontology/sparql", //
		"https://vocabularies.unesco.org/sparql" //
	// ,"https://dbpedia.org/sparql"
	);

	DefaultConceptsFinder finder = new DefaultConceptsFinder();

	finder.setTraceQuery(true);
	finder.setQueryBuilder(new DefaultConceptsQueryBuilder());
	finder.setThreadMode(ThreadMode.MULTI(() -> Executors.newFixedThreadPool(4)));
	finder.setTaskConsumer(task -> task.setMaxExecutionTime(1));

	List<String> concepts = finder.find("water", ontologyUrls, Arrays.asList()).//
		stream().//
		sorted().//
		toList();

	Assert.assertEquals(2, concepts.size());

	Assert.assertEquals("http://hydro.geodab.eu/hydro-ontology/concept/97", concepts.get(0));
	Assert.assertEquals("http://vocabularies.unesco.org/thesaurus/concept189", concepts.get(1));
    }
}
