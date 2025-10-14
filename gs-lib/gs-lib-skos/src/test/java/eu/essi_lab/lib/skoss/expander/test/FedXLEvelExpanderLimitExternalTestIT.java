/**
 * 
 */
package eu.essi_lab.lib.skoss.expander.test;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.lib.skoss.SKOSResponse;
import eu.essi_lab.lib.skoss.SKOSSemanticRelation;
import eu.essi_lab.lib.skoss.ThreadMode;
import eu.essi_lab.lib.skoss.expander.ConceptsExpander.ExpansionLevel;
import eu.essi_lab.lib.skoss.expander.ExpansionLimit;
import eu.essi_lab.lib.skoss.expander.ExpansionLimit.LimitTarget;
import eu.essi_lab.lib.skoss.expander.impl.FedXLevelsExpander;

/**
 * @author Fabrizio
 */
public class FedXLEvelExpanderLimitExternalTestIT {

    final int LABELS_COUNT = 49;
    final int ALT_LABELS_COUNT = 19;
    final int PREF_LABELS_COUNT = 30;

    @Test
    public void concepts100TestSingleThread() throws Exception {

	List<String> ontologyUrls = Arrays.asList("http://hydro.geodab.eu/hydro-ontology/sparql");

	List<String> sourceLangs = Arrays.asList("it", "en");
	List<String> searchLangs = Arrays.asList("it", "en");

	List<SKOSSemanticRelation> relations = Arrays.asList(//
		SKOSSemanticRelation.NARROWER, //
		SKOSSemanticRelation.RELATED);//

	ExpansionLevel targetLevel = ExpansionLevel.HIGH;

	FedXLevelsExpander expander = new FedXLevelsExpander();
	expander.setThreadMode(ThreadMode.SINGLE());

	SKOSResponse response1 = expander.expand(//
		Arrays.asList("http://hydro.geodab.eu/hydro-ontology/concept/97"), //
		ontologyUrls, //
		sourceLangs, //
		searchLangs, //
		relations, //
		targetLevel, //
		ExpansionLimit.of(LimitTarget.CONCEPTS, 100));//

	List<String> labels = response1.getLabels();
	Assert.assertEquals(LABELS_COUNT, labels.size());

	List<String> altLabels = response1.getAltLabels();
	Assert.assertEquals(ALT_LABELS_COUNT, altLabels.size());

	List<String> preLabels = response1.getPrefLabels();
	Assert.assertEquals(PREF_LABELS_COUNT, preLabels.size());
    }

    @Test
    public void concepts100TestSingleMultiThread() throws Exception {

	List<String> ontologyUrls = Arrays.asList("http://hydro.geodab.eu/hydro-ontology/sparql");

	List<String> sourceLangs = Arrays.asList("it", "en");
	List<String> searchLangs = Arrays.asList("it", "en");

	List<SKOSSemanticRelation> relations = Arrays.asList(//
		SKOSSemanticRelation.NARROWER, //
		SKOSSemanticRelation.RELATED);//

	ExpansionLevel targetLevel = ExpansionLevel.HIGH;

	FedXLevelsExpander expander = new FedXLevelsExpander();
	expander.setThreadMode(ThreadMode.MULTI());

	SKOSResponse response1 = expander.expand(//
		Arrays.asList("http://hydro.geodab.eu/hydro-ontology/concept/97"), //
		ontologyUrls, //
		sourceLangs, //
		searchLangs, //
		relations, //
		targetLevel, //
		ExpansionLimit.of(LimitTarget.CONCEPTS, 100));//

	List<String> labels = response1.getLabels();
	Assert.assertEquals(LABELS_COUNT, labels.size());

	List<String> altLabels = response1.getAltLabels();
	Assert.assertEquals(ALT_LABELS_COUNT, altLabels.size());

	List<String> preLabels = response1.getPrefLabels();
	Assert.assertEquals(PREF_LABELS_COUNT, preLabels.size());
    }

    @Test
    public void labels10TestSingleThread() throws Exception {

	List<String> ontologyUrls = Arrays.asList("http://hydro.geodab.eu/hydro-ontology/sparql");

	List<String> sourceLangs = Arrays.asList("it", "en");
	List<String> searchLangs = Arrays.asList("it", "en");

	List<SKOSSemanticRelation> relations = Arrays.asList(//
		SKOSSemanticRelation.NARROWER, //
		SKOSSemanticRelation.RELATED);//

	ExpansionLevel targetLevel = ExpansionLevel.HIGH;

	FedXLevelsExpander expander = new FedXLevelsExpander();
	expander.setThreadMode(ThreadMode.SINGLE());

	SKOSResponse response1 = expander.expand(//
		Arrays.asList("http://hydro.geodab.eu/hydro-ontology/concept/97"), //
		ontologyUrls, //
		sourceLangs, //
		searchLangs, //
		relations, //
		targetLevel, //
		ExpansionLimit.of(LimitTarget.LABELS, 10));//

	List<String> labels = response1.getLabels();
	Assert.assertTrue(labels.size() < 15);
    }

    @Test
    public void labels10TestMultiThread() throws Exception {

	List<String> ontologyUrls = Arrays.asList("http://hydro.geodab.eu/hydro-ontology/sparql");

	List<String> sourceLangs = Arrays.asList("it", "en");
	List<String> searchLangs = Arrays.asList("it", "en");

	List<SKOSSemanticRelation> relations = Arrays.asList(//
		SKOSSemanticRelation.NARROWER, //
		SKOSSemanticRelation.RELATED);//

	ExpansionLevel targetLevel = ExpansionLevel.HIGH;

	FedXLevelsExpander expander = new FedXLevelsExpander();
	expander.setThreadMode(ThreadMode.MULTI());

	SKOSResponse response1 = expander.expand(//
		Arrays.asList("http://hydro.geodab.eu/hydro-ontology/concept/97"), //
		ontologyUrls, //
		sourceLangs, //
		searchLangs, //
		relations, //
		targetLevel, //
		ExpansionLimit.of(LimitTarget.LABELS, 10));//

	List<String> labels = response1.getLabels();
	Assert.assertTrue(labels.size() < 15);

	labels.forEach(l -> System.out.println(l));
    }

    @Test
    public void prefLabels10TestSingleThread() throws Exception {

	List<String> ontologyUrls = Arrays.asList("http://hydro.geodab.eu/hydro-ontology/sparql");

	List<String> sourceLangs = Arrays.asList("it", "en");
	List<String> searchLangs = Arrays.asList("it", "en");

	List<SKOSSemanticRelation> relations = Arrays.asList(//
		SKOSSemanticRelation.NARROWER, //
		SKOSSemanticRelation.RELATED);//

	ExpansionLevel targetLevel = ExpansionLevel.HIGH;

	FedXLevelsExpander expander = new FedXLevelsExpander();
	expander.setThreadMode(ThreadMode.SINGLE());

	SKOSResponse response1 = expander.expand(//
		Arrays.asList("http://hydro.geodab.eu/hydro-ontology/concept/97"), //
		ontologyUrls, //
		sourceLangs, //
		searchLangs, //
		relations, //
		targetLevel, //
		ExpansionLimit.of(LimitTarget.CONCEPTS, 10));//

	List<String> labels = response1.getPrefLabels();
	Assert.assertTrue(labels.size() < 15);
    }

    @Test
    public void prefLabels10TestMultiThread() throws Exception {

	List<String> ontologyUrls = Arrays.asList("http://hydro.geodab.eu/hydro-ontology/sparql");

	List<String> sourceLangs = Arrays.asList("it", "en");
	List<String> searchLangs = Arrays.asList("it", "en");

	List<SKOSSemanticRelation> relations = Arrays.asList(//
		SKOSSemanticRelation.NARROWER, //
		SKOSSemanticRelation.RELATED);//

	ExpansionLevel targetLevel = ExpansionLevel.HIGH;

	FedXLevelsExpander expander = new FedXLevelsExpander();
	expander.setThreadMode(ThreadMode.MULTI());

	SKOSResponse response1 = expander.expand(//
		Arrays.asList("http://hydro.geodab.eu/hydro-ontology/concept/97"), //
		ontologyUrls, //
		sourceLangs, //
		searchLangs, //
		relations, //
		targetLevel, //
		ExpansionLimit.of(LimitTarget.CONCEPTS, 10));//

	List<String> labels = response1.getPrefLabels();
	Assert.assertTrue(labels.size() < 15);
    }

    @Test
    public void altLabels10TestSingleThread() throws Exception {

	List<String> ontologyUrls = Arrays.asList("http://hydro.geodab.eu/hydro-ontology/sparql");

	List<String> sourceLangs = Arrays.asList("it", "en");
	List<String> searchLangs = Arrays.asList("it", "en");

	List<SKOSSemanticRelation> relations = Arrays.asList(//
		SKOSSemanticRelation.NARROWER, //
		SKOSSemanticRelation.RELATED);//

	ExpansionLevel targetLevel = ExpansionLevel.HIGH;

	FedXLevelsExpander expander = new FedXLevelsExpander();
	expander.setThreadMode(ThreadMode.SINGLE());

	SKOSResponse response1 = expander.expand(//
		Arrays.asList("http://hydro.geodab.eu/hydro-ontology/concept/97"), //
		ontologyUrls, //
		sourceLangs, //
		searchLangs, //
		relations, //
		targetLevel, //
		ExpansionLimit.of(LimitTarget.ALT_LABELS, 10));//

	List<String> labels = response1.getAltLabels();
	Assert.assertTrue(labels.size() < 15);
    }

    @Test
    public void altLabels10TestMultiThread() throws Exception {

	List<String> ontologyUrls = Arrays.asList("http://hydro.geodab.eu/hydro-ontology/sparql");

	List<String> sourceLangs = Arrays.asList("it", "en");
	List<String> searchLangs = Arrays.asList("it", "en");

	List<SKOSSemanticRelation> relations = Arrays.asList(//
		SKOSSemanticRelation.NARROWER, //
		SKOSSemanticRelation.RELATED);//

	ExpansionLevel targetLevel = ExpansionLevel.HIGH;

	FedXLevelsExpander expander = new FedXLevelsExpander();
	expander.setThreadMode(ThreadMode.MULTI());

	SKOSResponse response1 = expander.expand(//
		Arrays.asList("http://hydro.geodab.eu/hydro-ontology/concept/97"), //
		ontologyUrls, //
		sourceLangs, //
		searchLangs, //
		relations, //
		targetLevel, //
		ExpansionLimit.of(LimitTarget.ALT_LABELS, 10));//

	List<String> labels = response1.getAltLabels();
	Assert.assertTrue(labels.size() < 15);
    }
}
