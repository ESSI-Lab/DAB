/**
 * 
 */
package eu.essi_lab.lib.skoss.expander.functional.test;

import java.util.Arrays;
import java.util.List;

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

/**
 * @author Fabrizio
 */
public class FedXConceptsExpanderLimitExternalTestIT {

    static final int HYDRO_ONT_WATER_LABELS_COUNT = 49;
    static final int HYDRO_ONT_WATER_ALT_LABELS_COUNT = 19;
    static final int HYDRO_ONT_WATER_CONCEPTS_COUNT = 30;
    static final int HYDRO_ONT_WATER_PREF_LABELS_COUNT = HYDRO_ONT_WATER_CONCEPTS_COUNT;

    @Test
    public void noLimitTestSingleThread() throws Exception {

	test(ThreadMode.SINGLE(), ExpansionLimit.of(LimitTarget.CONCEPTS, 1000), HYDRO_ONT_WATER_CONCEPTS_COUNT,
		HYDRO_ONT_WATER_PREF_LABELS_COUNT, HYDRO_ONT_WATER_ALT_LABELS_COUNT);
    }

    @Test
    public void noLimitTestMultiThread() throws Exception {

	test(ThreadMode.MULTI(), ExpansionLimit.of(LimitTarget.CONCEPTS, 1000), HYDRO_ONT_WATER_CONCEPTS_COUNT,
		HYDRO_ONT_WATER_PREF_LABELS_COUNT, HYDRO_ONT_WATER_ALT_LABELS_COUNT);
    }

    //
    //
    //

    @Test
    public void concepts1SingleThreadTest() throws Exception {

	test(ThreadMode.SINGLE(), ExpansionLimit.of(LimitTarget.CONCEPTS, 1), 1, -1, -1);
    }

    @Test
    public void concepts1MultiThreadTest() throws Exception {

	test(ThreadMode.MULTI(), ExpansionLimit.of(LimitTarget.CONCEPTS, 1), 1, -1, -1);
    }

    //
    //
    //

    @Test
    public void concepts10SingleThreadTest() throws Exception {

	test(ThreadMode.SINGLE(), ExpansionLimit.of(LimitTarget.CONCEPTS, 10), 10, -1, -1);
    }

    @Test
    public void concepts10MultiThreadTest() throws Exception {

	test(ThreadMode.MULTI(), ExpansionLimit.of(LimitTarget.CONCEPTS, 10), 10, -1, -1);
    }

    //
    //
    //

    @Test
    public void concepts3SingleThreadTest() throws Exception {

	test(ThreadMode.SINGLE(), ExpansionLimit.of(LimitTarget.CONCEPTS, 3), 3, -1, -1);
    }

    @Test
    public void concepts3MultiThreadTest() throws Exception {

	test(ThreadMode.MULTI(), ExpansionLimit.of(LimitTarget.CONCEPTS, 3), 3, -1, -1);
    }

    //
    //
    //

    @Test
    public void concepts17SingleThreadTest() throws Exception {

	test(ThreadMode.SINGLE(), ExpansionLimit.of(LimitTarget.CONCEPTS, 17), 17, -1, -1);
    }

    @Test
    public void concepts17MultiThreadTest() throws Exception {

	test(ThreadMode.MULTI(), ExpansionLimit.of(LimitTarget.CONCEPTS, 17), 17, -1, -1);
    }

    //
    //
    //

    @Test
    public void concepts29SingleThreadTest() throws Exception {

	test(ThreadMode.SINGLE(), ExpansionLimit.of(LimitTarget.CONCEPTS, 29), 29, -1, -1);
    }

    @Test
    public void concepts29MultiThreadTest() throws Exception {

	test(ThreadMode.MULTI(), ExpansionLimit.of(LimitTarget.CONCEPTS, 29), 29, -1, -1);
    }

    //
    //
    //

    @Test
    public void concepts40SingleThreadTest() throws Exception {

	test(ThreadMode.SINGLE(), ExpansionLimit.of(LimitTarget.CONCEPTS, 40), HYDRO_ONT_WATER_CONCEPTS_COUNT, -1, -1);
    }

    @Test
    public void concepts40MultiThreadTest() throws Exception {

	test(ThreadMode.MULTI(), ExpansionLimit.of(LimitTarget.CONCEPTS, 40), HYDRO_ONT_WATER_CONCEPTS_COUNT, -1, -1);
    }

    //
    //
    //

    @Test
    public void altLabels1SingleThreadTest() throws Exception {

	test(ThreadMode.SINGLE(), ExpansionLimit.of(LimitTarget.ALT_LABELS, 1), -1, -1, 1);
    }

    @Test
    public void altLabels1MultiThreadTest() throws Exception {

	test(ThreadMode.MULTI(), ExpansionLimit.of(LimitTarget.ALT_LABELS, 1), -1, -1, 1);
    }

    //
    //
    //

    @Test
    public void altLabels11SingleThreadTest() throws Exception {

	test(ThreadMode.SINGLE(), ExpansionLimit.of(LimitTarget.ALT_LABELS, 11), -1, -1, 11);
    }

    @Test
    public void altLabels11MultiThreadTest() throws Exception {

	test(ThreadMode.MULTI(), ExpansionLimit.of(LimitTarget.ALT_LABELS, 11), -1, -1, 11);
    }

    //
    //
    //

    @Test
    public void altLabels13SingleThreadTest() throws Exception {

	test(ThreadMode.SINGLE(), ExpansionLimit.of(LimitTarget.ALT_LABELS, 13), -1, -1, 13);
    }

    @Test
    public void altLabels13MultiThreadTest() throws Exception {

	test(ThreadMode.MULTI(), ExpansionLimit.of(LimitTarget.ALT_LABELS, 13), -1, -1, 13);
    }

    //
    //
    //

    @Test
    public void altLabels17SingleThreadTest() throws Exception {

	test(ThreadMode.SINGLE(), ExpansionLimit.of(LimitTarget.ALT_LABELS, 17), -1, -1, 17);
    }

    @Test
    public void altLabels17MultiThreadTest() throws Exception {

	test(ThreadMode.MULTI(), ExpansionLimit.of(LimitTarget.ALT_LABELS, 17), -1, -1, 17);
    }

    //
    //
    //

    @Test
    public void altLabels18SingleThreadTest() throws Exception {

	test(ThreadMode.SINGLE(), ExpansionLimit.of(LimitTarget.ALT_LABELS, 18), -1, -1, 18);
    }

    @Test
    public void altLabels18MultiThreadTest() throws Exception {

	test(ThreadMode.MULTI(), ExpansionLimit.of(LimitTarget.ALT_LABELS, 18), -1, -1, 18);
    }

    //
    //
    //

    @Test
    public void altLabels19SingleThreadTest() throws Exception {

	test(ThreadMode.SINGLE(), ExpansionLimit.of(LimitTarget.ALT_LABELS, 19), -1, -1, 19);
    }

    @Test
    public void altLabels19MultiThreadTest() throws Exception {

	test(ThreadMode.MULTI(), ExpansionLimit.of(LimitTarget.ALT_LABELS, 19), -1, -1, 19);
    }

    //
    //
    //

    @Test
    public void altLabels21SingleThreadTest() throws Exception {

	test(ThreadMode.SINGLE(), ExpansionLimit.of(LimitTarget.ALT_LABELS, 21), -1, -1, HYDRO_ONT_WATER_ALT_LABELS_COUNT);
    }

    @Test
    public void altLabels21MultiThreadTest() throws Exception {

	test(ThreadMode.MULTI(), ExpansionLimit.of(LimitTarget.ALT_LABELS, 21), -1, -1, HYDRO_ONT_WATER_ALT_LABELS_COUNT);
    }

    //
    //
    //

    @Test
    public void labels1SingleThreadTest() throws Exception {

	test(ThreadMode.SINGLE(), ExpansionLimit.of(LimitTarget.LABELS, 1), 1, 1, -1);
    }

    @Test
    public void labels1MultiThreadTest() throws Exception {

	test(ThreadMode.MULTI(), ExpansionLimit.of(LimitTarget.LABELS, 1), 1, 1, -1);
    }

    //
    //
    //

    @Test
    public void labels2SingleThreadTest() throws Exception {

	test(ThreadMode.SINGLE(), ExpansionLimit.of(LimitTarget.LABELS, 2), -1, 2, -1);
    }

    @Test
    public void labels2MultiThreadTest() throws Exception {

	test(ThreadMode.MULTI(), ExpansionLimit.of(LimitTarget.LABELS, 2), -1, 2, -1);
    }

    //
    //
    //

    @Test
    public void labels3SingleThreadTest() throws Exception {

	test(ThreadMode.SINGLE(), ExpansionLimit.of(LimitTarget.LABELS, 3), -1, 3, -1);
    }

    @Test
    public void labels3MultiThreadTest() throws Exception {

	test(ThreadMode.MULTI(), ExpansionLimit.of(LimitTarget.LABELS, 3), -1, 3, -1);
    }

    //
    //
    //

    @Test
    public void labels5SingleThreadTest() throws Exception {

	test(ThreadMode.SINGLE(), ExpansionLimit.of(LimitTarget.LABELS, 5), -1, 5, -1);
    }

    @Test
    public void labels5MultiThreadTest() throws Exception {

	test(ThreadMode.MULTI(), ExpansionLimit.of(LimitTarget.LABELS, 5), -1, 5, -1);
    }

    //
    //
    //

    @Test
    public void labels17SingleThreadTest() throws Exception {

	test(ThreadMode.SINGLE(), ExpansionLimit.of(LimitTarget.LABELS, 17), -1, 17, -1);
    }

    @Test
    public void labels17MultiThreadTest() throws Exception {

	test(ThreadMode.MULTI(), ExpansionLimit.of(LimitTarget.LABELS, 17), -1, 17, -1);
    }

    //
    //
    //

    @Test
    public void labels33SingleThreadTest() throws Exception {

	test(ThreadMode.SINGLE(), ExpansionLimit.of(LimitTarget.LABELS, 33), -1, 33, -1);
    }

    @Test
    public void labels33MultiThreadTest() throws Exception {

	test(ThreadMode.MULTI(), ExpansionLimit.of(LimitTarget.LABELS, 33), -1, 33, -1);
    }

    //
    //
    //

    @Test
    public void labels48SingleThreadTest() throws Exception {

	test(ThreadMode.SINGLE(), ExpansionLimit.of(LimitTarget.LABELS, 48), -1, 48, -1);
    }

    @Test
    public void labels48MultiThreadTest() throws Exception {

	test(ThreadMode.MULTI(), ExpansionLimit.of(LimitTarget.LABELS, 48), -1, 48, -1);
    }

    //
    //
    //

    @Test
    public void labels50SingleThreadTest() throws Exception {

	test(ThreadMode.SINGLE(), ExpansionLimit.of(LimitTarget.LABELS, 50), -1, HYDRO_ONT_WATER_PREF_LABELS_COUNT, -1);
    }

    @Test
    public void labels50MultiThreadTest() throws Exception {

	test(ThreadMode.MULTI(), ExpansionLimit.of(LimitTarget.LABELS, 50), -1, HYDRO_ONT_WATER_PREF_LABELS_COUNT, -1);
    }

    /**
     * 
     */
    private void test(ThreadMode mode, ExpansionLimit limit, int excConcepts, int excLabels, int excAlt) throws Exception {

	List<String> ontologyUrls = Arrays.asList("http://hydro.geodab.eu/hydro-ontology/sparql");

	List<String> sourceLangs = Arrays.asList("it", "en");
	List<String> searchLangs = Arrays.asList("it", "en");

	List<SKOSSemanticRelation> relations = Arrays.asList(//
		SKOSSemanticRelation.NARROWER, //
		SKOSSemanticRelation.RELATED);//

	ExpansionLevel targetLevel = ExpansionLevel.HIGH;

	FedXConceptsExpander expander = new FedXConceptsExpander();
	expander.setThreadMode(mode);

	SKOSResponse response = expander.expand(//
		Arrays.asList("http://hydro.geodab.eu/hydro-ontology/concept/97"), //
		ontologyUrls, //
		sourceLangs, //
		searchLangs, //
		relations, //
		targetLevel, //
		limit);//

	List<SKOSConcept> results = response.getResults();

	if (excConcepts > -1) {
	    List<String> preLabels = response.getPrefLabels();
	    Assert.assertEquals(excConcepts, preLabels.size());
	    Assert.assertEquals(excConcepts, results.size());
	}

	if (excLabels > -1) {
	    List<String> labels = response.getLabels();
	    Assert.assertEquals(excLabels, labels.size());
	}

	if (excAlt > -1) {
	    List<String> altLabels = response.getAltLabels();
	    Assert.assertEquals(excAlt, altLabels.size());
	}
    }

}
