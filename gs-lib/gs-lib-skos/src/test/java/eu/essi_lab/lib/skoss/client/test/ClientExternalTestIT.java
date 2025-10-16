/**
 * 
 */
package eu.essi_lab.lib.skoss.client.test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.lib.skos.SKOSClient;
import eu.essi_lab.lib.skos.SKOSConcept;
import eu.essi_lab.lib.skos.SKOSResponse;
import eu.essi_lab.lib.skos.SKOSSemanticRelation;
import eu.essi_lab.lib.skos.SKOSClient.SearchTarget;
import eu.essi_lab.lib.skos.expander.ConceptsExpander;
import eu.essi_lab.lib.skos.expander.ExpansionLimit;
import eu.essi_lab.lib.skos.expander.ConceptsExpander.ExpansionLevel;
import eu.essi_lab.lib.skos.expander.ExpansionLimit.LimitTarget;
import eu.essi_lab.lib.skos.expander.impl.DefaultConceptsExpander;
import eu.essi_lab.lib.skos.finder.ConceptsFinder;
import eu.essi_lab.lib.skos.finder.impl.DefaultConceptsFinder;
import eu.essi_lab.lib.utils.ThreadMode;

/**
 * @author Fabrizio
 */
public class ClientExternalTestIT {

    @Test
    public void paramsTest() throws Exception {

	SKOSClient client = new SKOSClient();

	ConceptsExpander<?> expander = client.getExpander();
	Assert.assertEquals(DefaultConceptsExpander.class, expander.getClass());

	ConceptsFinder<?> finder = client.getFinder();
	Assert.assertEquals(DefaultConceptsFinder.class, finder.getClass());

	ExpansionLevel expansionLevel = client.getExpansionLevel();
	Assert.assertEquals(ExpansionLevel.LOW, expansionLevel);

	List<SKOSSemanticRelation> expansionsRelations = client.getExpansionsRelations();
	Assert.assertEquals(2, expansionsRelations.size());
	Assert.assertTrue(expansionsRelations.contains(SKOSSemanticRelation.RELATED));
	Assert.assertTrue(expansionsRelations.contains(SKOSSemanticRelation.NARROWER));

	ExpansionLimit limit = client.getExpansionLimit();
	Assert.assertEquals(10, limit.getLimit());

	List<String> searchLangs = client.getSearchLangs();
	Assert.assertEquals(2, searchLangs.size());
	Assert.assertTrue(searchLangs.contains("it"));
	Assert.assertTrue(searchLangs.contains("en"));

	List<String> sourceLangs = client.getSourceLangs();
	Assert.assertEquals(2, sourceLangs.size());
	Assert.assertTrue(sourceLangs.contains("it"));
	Assert.assertTrue(sourceLangs.contains("en"));

	List<String> ontologyUrls = client.getOntologyUrls();
	Assert.assertNull(ontologyUrls);

	Optional<String> searchTerm = client.getSearchValue();
	Assert.assertTrue(searchTerm.isEmpty());

	Optional<SearchTarget> searchTarget = client.getSearchTarget();
	Assert.assertTrue(searchTarget.isEmpty());

	//
	//
	//

	client.setExpander(null);
	client.setFinder(null);
	client.setExpansionLevel(ExpansionLevel.NONE);
	client.setExpansionsRelations(Arrays.asList());
	client.setExpansionLimit(ExpansionLimit.of(LimitTarget.ALT_LABELS, 0));
	client.setOntologyUrls(Arrays.asList());
	client.setSearchLangs(Arrays.asList());
	client.setSearchValue(SearchTarget.TERMS, "search");
	client.setSourceLangs(Arrays.asList());

	Assert.assertNull(client.getExpander());
	Assert.assertNull(client.getFinder());
	Assert.assertEquals(ExpansionLevel.NONE, client.getExpansionLevel());
	Assert.assertTrue(client.getExpansionsRelations().isEmpty());
	Assert.assertEquals(0, client.getExpansionLimit().getLimit());
	Assert.assertEquals(LimitTarget.ALT_LABELS, client.getExpansionLimit().getTarget());
	Assert.assertTrue(client.getOntologyUrls().isEmpty());
	Assert.assertTrue(client.getSearchLangs().isEmpty());
	Assert.assertEquals("search", client.getSearchValue().get());
	Assert.assertEquals(SearchTarget.TERMS, client.getSearchTarget().get());
	Assert.assertTrue(client.getSourceLangs().isEmpty());

	client.setSearchValue("value");
	Assert.assertEquals("value", client.getSearchValue().get());
	Assert.assertEquals(SearchTarget.TERMS, client.getSearchTarget().get());

	client.setSearchValue("http://concept");
	Assert.assertEquals("http://concept", client.getSearchValue().get());
	Assert.assertEquals(SearchTarget.CONCEPTS, client.getSearchTarget().get());

    }

    @Test
    public void mediumExpansionLimit10Test_ConceptsExpander() throws Exception {

	mediumExpansionLimit10Test(new DefaultConceptsFinder(), new DefaultConceptsExpander(), ThreadMode.MULTI());
    }

    /**
     * @param expander
     * @param mode
     * @throws Exception
     */
    private void mediumExpansionLimit10Test(ConceptsFinder<?> finder, ConceptsExpander<?> expander, ThreadMode mode) throws Exception {

	SKOSClient client = new SKOSClient();

	client.setOntologyUrls(Arrays.asList(//
		"http://localhost:3031/gemet/query", //
		"http://hydro.geodab.eu/hydro-ontology/sparql", //
		"https://vocabularies.unesco.org/sparql", //
		"https://dbpedia.org/sparql"));

	client.setSourceLangs(List.of("en"));
	client.setSearchLangs(List.of("it"));

	client.setExpansionLevel(ExpansionLevel.MEDIUM);
	client.setExpansionLimit(ExpansionLimit.of(LimitTarget.CONCEPTS, 1000));
	
	client.setExpansionsRelations(List.of(SKOSSemanticRelation.NARROWER, SKOSSemanticRelation.RELATED));
//	client.setExpansionsRelations(List.of(SKOSSemanticRelation.NARROWER ));

	
	client.setSearchValue(SearchTarget.TERMS, "water");

	//
	//
	//

	client.setFinder(finder);

	//
	//
	//

	client.setExpander(expander);

	//
	//
	//

	SKOSResponse response = client.search();

	List<SKOSConcept> results = response.getAggregatedResults().//
		stream().//
		sorted((r1, r2) -> r1.toString().compareTo(r2.toString())). //
		toList();//

	System.out.println("\n\n");

	results.forEach(res -> System.out.println(res + "\n---"));

	//
	System.out.println("\n\n");
	//
	response.getLabels().forEach(pref -> System.out.println(pref));
	//
	// System.out.println("\n\n");
	//
	// response.getLabels().forEach(alt -> System.out.println(alt));

	// Assert.assertEquals(10, results.size());

	System.out.println("\n-------");
	System.out.println(results.size());
	System.out.println("\n-------");

    }
}
