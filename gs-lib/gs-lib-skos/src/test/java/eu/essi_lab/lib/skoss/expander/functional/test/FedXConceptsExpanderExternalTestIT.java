/**
 * 
 */
package eu.essi_lab.lib.skoss.expander.functional.test;

import java.util.Arrays;
import java.util.List;

import org.eclipse.rdf4j.federated.FedXConfig;
import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.lib.skoss.SKOSConcept;
import eu.essi_lab.lib.skoss.SKOSResponse;
import eu.essi_lab.lib.skoss.SKOSSemanticRelation;
import eu.essi_lab.lib.skoss.ThreadMode;
import eu.essi_lab.lib.skoss.expander.ConceptsExpander.ExpansionLevel;
import eu.essi_lab.lib.skoss.expander.ExpansionLimit;
import eu.essi_lab.lib.skoss.expander.ExpansionLimit.LimitTarget;
import eu.essi_lab.lib.skoss.expander.impl.FedXConceptsExpander;
import eu.essi_lab.lib.skoss.finder.impl.FedXConceptsFinder;

/**
 * @author Fabrizio
 */
public class FedXConceptsExpanderExternalTestIT {

    @Test
    public void noLimitWaterSearchTermHydroOntologyTest() throws Exception {

	List<String> ontologyUrls = Arrays.asList(//
		"http://hydro.geodab.eu/hydro-ontology/sparql");

	List<String> sourceLangs = Arrays.asList("it", "en");
	List<String> searchLangs = Arrays.asList("it", "en");
	List<SKOSSemanticRelation> relations = Arrays.asList(SKOSSemanticRelation.NARROWER, SKOSSemanticRelation.RELATED);
	ExpansionLevel targetLevel = ExpansionLevel.HIGH;

	ExpansionLimit limit = ExpansionLimit.of(LimitTarget.CONCEPTS, 1000);

	FedXConceptsFinder finder = new FedXConceptsFinder();
	finder.setThreadMode(ThreadMode.MULTI());

	List<String> concepts = finder.find("water", ontologyUrls, sourceLangs);

	//
	//
	//

	FedXConceptsExpander expander = new FedXConceptsExpander();
	expander.setThreadMode(ThreadMode.SINGLE()); // default

	SKOSResponse response = expander.expand(//
		concepts, //
		ontologyUrls, //
		sourceLangs, //
		searchLangs, //
		relations, //
		targetLevel, //
		limit);//

	Assert.assertEquals(FedXConceptsExpanderLimitExternalTestIT.HYDRO_ONT_WATER_CONCEPTS_COUNT, response.getResults().size());

	//
	//
	//

	expander = new FedXConceptsExpander();
	expander.setThreadMode(ThreadMode.MULTI());

	response = expander.expand(//
		concepts, //
		ontologyUrls, //
		sourceLangs, //
		searchLangs, //
		relations, //
		targetLevel, //
		limit);//

	Assert.assertEquals(FedXConceptsExpanderLimitExternalTestIT.HYDRO_ONT_WATER_CONCEPTS_COUNT, response.getResults().size());
    }

    @Test
    public void withLimitSearchTermWaterHydroOntologyTest() throws Exception {

	limitWaterHydroOntologyTest(ExpansionLimit.of(LimitTarget.CONCEPTS, 5), 5);
	limitWaterHydroOntologyTest(ExpansionLimit.of(LimitTarget.CONCEPTS, 11), 11);
	limitWaterHydroOntologyTest(ExpansionLimit.of(LimitTarget.CONCEPTS, 15), 15);
	limitWaterHydroOntologyTest(
		ExpansionLimit.of(LimitTarget.CONCEPTS, FedXConceptsExpanderLimitExternalTestIT.HYDRO_ONT_WATER_CONCEPTS_COUNT),
		FedXConceptsExpanderLimitExternalTestIT.HYDRO_ONT_WATER_CONCEPTS_COUNT);
	limitWaterHydroOntologyTest(ExpansionLimit.of(LimitTarget.CONCEPTS, 3), 3);
	limitWaterHydroOntologyTest(ExpansionLimit.of(LimitTarget.CONCEPTS, 7), 7);
	limitWaterHydroOntologyTest(ExpansionLimit.of(LimitTarget.CONCEPTS, 23), 23);
	limitWaterHydroOntologyTest(ExpansionLimit.of(LimitTarget.CONCEPTS, 57),
		FedXConceptsExpanderLimitExternalTestIT.HYDRO_ONT_WATER_CONCEPTS_COUNT);
	limitWaterHydroOntologyTest(ExpansionLimit.of(LimitTarget.CONCEPTS, 100),
		FedXConceptsExpanderLimitExternalTestIT.HYDRO_ONT_WATER_CONCEPTS_COUNT);
    }

    /**
     * @param limit
     * @throws Exception
     */
    private void limitWaterHydroOntologyTest(ExpansionLimit limit, int expected) throws Exception {

	//
	// finds the concepts
	//

	List<String> ontologyUrls = Arrays.asList(//
		"http://hydro.geodab.eu/hydro-ontology/sparql");

	List<String> sourceLangs = Arrays.asList("it", "en");
	List<String> searchLangs = Arrays.asList("it", "en");
	List<SKOSSemanticRelation> relations = Arrays.asList(SKOSSemanticRelation.NARROWER, SKOSSemanticRelation.RELATED);
	ExpansionLevel targetLevel = ExpansionLevel.HIGH;

	FedXConceptsFinder finder = new FedXConceptsFinder();

	List<String> concepts = finder.find("water", ontologyUrls, sourceLangs);

	//
	// expanding in SingleThread mode
	//

	FedXConceptsExpander expander = new FedXConceptsExpander();
	expander.setThreadMode(ThreadMode.SINGLE()); // default

	SKOSResponse response1 = expander.expand(//
		concepts, //
		ontologyUrls, //
		sourceLangs, //
		searchLangs, //
		relations, //
		targetLevel, //
		limit);//

	Assert.assertEquals(expected, response1.getResults().size());

	//
	// expanding in MultiThread mode, expecting the same results
	//

	expander = new FedXConceptsExpander();
	expander.setThreadMode(ThreadMode.MULTI());

	SKOSResponse response2 = expander.expand(//
		concepts, //
		ontologyUrls, //
		sourceLangs, //
		searchLangs, //
		relations, //
		targetLevel, //
		limit);//

	//
	// the equals test may fail since with multi thread mode the results may differ from single thread mode
	//

	// Assert.assertEquals(response1.getResults().stream().sorted().toList(),
	// response2.getResults().stream().sorted().toList());

	Assert.assertEquals(expected, response2.getResults().size());
    }

    @Test
    public void twoOntologiesWaterLimit10ConcpectsSingleThreadTest() throws Exception {

	twoOntologiesWaterTest(ThreadMode.SINGLE());
    }

    @Test
    public void twoOntologiesWaterLimit10ConcpectsMultiThreadTest() throws Exception {

	twoOntologiesWaterTest(ThreadMode.MULTI());
    }

    /**
     * @param mode
     * @throws Exception
     */
    private void twoOntologiesWaterTest(ThreadMode mode) throws Exception {

	List<String> ontologyUrls = Arrays.asList(//
		// "http://localhost:3031/gemet/query", //
		"http://hydro.geodab.eu/hydro-ontology/sparql", //
		"https://vocabularies.unesco.org/sparql" //
	);

	List<String> sourceLangs = Arrays.asList("it", "en");
	List<String> searchLangs = Arrays.asList("it", "en");
	List<SKOSSemanticRelation> relations = Arrays.asList(SKOSSemanticRelation.NARROWER, SKOSSemanticRelation.RELATED);
	ExpansionLevel targetLevel = ExpansionLevel.MEDIUM;

	ExpansionLimit limit = ExpansionLimit.of(LimitTarget.CONCEPTS, 10);

	//
	//
	//

	FedXConceptsFinder finder = new FedXConceptsFinder();

	List<String> concepts = finder.find("water", ontologyUrls, sourceLangs);

	FedXConceptsExpander expander = new FedXConceptsExpander();
	expander.setThreadMode(mode);
	FedXConfig fedXConfig = new FedXConfig();
	fedXConfig.withEnableMonitoring(true);
	expander.setEngineConfig(fedXConfig);

	SKOSResponse response = expander.expand(//
		concepts, //
		ontologyUrls, //
		sourceLangs, //
		searchLangs, //
		relations, //
		targetLevel, //
		limit);//

	List<SKOSConcept> results = response.getResults().stream().//
		sorted((r1, r2) -> r1.toString().compareTo(r2.toString())). //
		toList();//

	System.out.println("\n --- Results ---\n");

	results.forEach(res -> System.out.println(res + "\n---"));

	System.out.println("\n --- Pref labels ---\n");

	response.getPrefLabels().forEach(pref -> System.out.println(pref));

	System.out.println("\n\n --- Alt labels ---\n");

	response.getAltLabels().forEach(alt -> System.out.println(alt));

	Assert.assertEquals(10, results.size());
    }

    /**
     * @param limit
     * @throws Exception
     */
    @Test
    public void waterHydroOntologyConcept97Test() throws Exception {

	List<String> ontologyUrls = Arrays.asList(//
		"http://hydro.geodab.eu/hydro-ontology/sparql");

	List<String> sourceLangs = Arrays.asList("it", "en");
	List<String> searchLangs = Arrays.asList("it", "en");

	List<SKOSSemanticRelation> relations = Arrays.asList(SKOSSemanticRelation.NARROWER, //
		SKOSSemanticRelation.RELATED);//

	ExpansionLevel targetLevel = ExpansionLevel.HIGH;

	FedXConceptsFinder finder = new FedXConceptsFinder();
	finder.setThreadMode(ThreadMode.SINGLE());

	List<String> concepts = finder.find("water", ontologyUrls, sourceLangs);

	Assert.assertEquals(1, concepts.size());

	Assert.assertEquals("http://hydro.geodab.eu/hydro-ontology/concept/97", concepts.get(0));

	FedXConceptsExpander expander = new FedXConceptsExpander();
	expander.setThreadMode(ThreadMode.SINGLE());
	expander.setTraceQuery(true);

	SKOSResponse response = expander.expand(//
		concepts, //
		ontologyUrls, //
		sourceLangs, //
		searchLangs, //
		relations, //
		targetLevel, //
		ExpansionLimit.of(LimitTarget.CONCEPTS, 100));//

	List<String> labels = response.getLabels();
	Assert.assertEquals(FedXConceptsExpanderLimitExternalTestIT.HYDRO_ONT_WATER_LABELS_COUNT, labels.size());
    }
}
