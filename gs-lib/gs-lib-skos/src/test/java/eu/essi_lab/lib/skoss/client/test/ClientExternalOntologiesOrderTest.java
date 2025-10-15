/**
 * 
 */
package eu.essi_lab.lib.skoss.client.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.lib.skoss.SKOSClient;
import eu.essi_lab.lib.skoss.SKOSClient.SearchTarget;
import eu.essi_lab.lib.skoss.SKOSConcept;
import eu.essi_lab.lib.skoss.SKOSResponse;
import eu.essi_lab.lib.skoss.ThreadMode;
import eu.essi_lab.lib.skoss.expander.ConceptsExpander.ExpansionLevel;
import eu.essi_lab.lib.skoss.expander.ExpansionLimit;
import eu.essi_lab.lib.skoss.expander.ExpansionLimit.LimitTarget;
import eu.essi_lab.lib.skoss.expander.impl.DefaultConceptsExpander;
import eu.essi_lab.lib.skoss.finder.impl.DefaultConceptsFinder;

/**
 * @author Fabrizio
 */
public class ClientExternalOntologiesOrderTest {

    @Test
    public void test() throws IllegalArgumentException, Exception {

	List<String> ontologies = Arrays.asList(//

		"https://dbpedia.org/sparql", //
		"http://localhost:3031/gemet/query", //
		"http://hydro.geodab.eu/hydro-ontology/sparql", //
		"https://vocabularies.unesco.org/sparql"//

	);

	List<List<String>> perms = permutations(ontologies);

	List<List<String>> out = new ArrayList<>();

	for (List<String> ont : perms) {

	    List<SKOSConcept> list = test(ont);

	    out.add(list.stream().map(c -> c.toString()).sorted().toList());
	}

	System.out.println(out);

	Assert.assertEquals(1, out.stream().distinct().count());
    }

    /**
     * @param <T>
     * @param list
     * @return
     */
    private static <T> List<List<T>> permutations(List<T> list) {

	if (list.size() <= 1) {
	    return List.of(list);
	}

	List<List<T>> result = new ArrayList<>();

	for (int i = 0; i < list.size(); i++) {

	    T elem = list.get(i);
	    List<T> rest = new ArrayList<>(list);
	    rest.remove(i);

	    for (List<T> perm : permutations(rest)) {
		List<T> newPerm = new ArrayList<>();
		newPerm.add(elem);
		newPerm.addAll(perm);
		result.add(newPerm);
	    }
	}
	return result;
    }

    /**
     * @param ontologies
     * @return
     * @throws IllegalArgumentException
     * @throws Exception
     */
    private List<SKOSConcept> test(List<String> ontologies) throws IllegalArgumentException, Exception {

	SKOSClient client = new SKOSClient();

	client.setOntologyUrls(ontologies);

	client.setExpansionLevel(ExpansionLevel.MEDIUM);
	client.setExpansionLimit(ExpansionLimit.of(LimitTarget.LABELS, 1000));
	client.setSearchValue(SearchTarget.TERMS, "wind");

	client.setExpansionsRelations(SKOSClient.DEFAULT_RELATIONS);
	client.setSearchLangs(SKOSClient.DEFAULT_SEARCH_LANGS);
	client.setSourceLangs(SKOSClient.DEFAULT_SOURCE_LANGS);

	//
	//
	//

	DefaultConceptsFinder finder = new DefaultConceptsFinder();
	// finder.setTraceQuery(true);
	finder.setThreadMode(ThreadMode.MULTI(() -> Executors.newFixedThreadPool(4)));
	// finder.setTaskConsumer((task) -> System.out.println(task));

	client.setFinder(finder);

	DefaultConceptsExpander expander = new DefaultConceptsExpander();
	expander.setTraceQuery(true);
	expander.setThreadMode(ThreadMode.MULTI(() -> Executors.newFixedThreadPool(4))); // 4 threads per level
	// expander.setTaskConsumer((task) -> System.out.println(task));

	client.setExpander(expander);

	SKOSResponse response = client.search();

	System.out.println(response.getLabels());

	// Assert.assertTrue(response.getLabels().contains("wind"));

	return response.getResults();
    }

}
