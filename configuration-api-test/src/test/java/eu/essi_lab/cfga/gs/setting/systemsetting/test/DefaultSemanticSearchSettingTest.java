/**
 * 
 */
package eu.essi_lab.cfga.gs.setting.systemsetting.test;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.cfga.gs.setting.ontology.DefaultSemanticSearchSetting;
import eu.essi_lab.lib.skos.SKOSSemanticRelation;
import eu.essi_lab.lib.skos.expander.ConceptsExpander.ExpansionLevel;
import eu.essi_lab.lib.skos.expander.ExpansionLimit;
import eu.essi_lab.lib.skos.expander.ExpansionLimit.LimitTarget;
import eu.essi_lab.lib.utils.EuropeanLanguage;

/**
 * @author Fabrizio
 */
public class DefaultSemanticSearchSettingTest {

    @Test
    public void test() {

	DefaultSemanticSearchSetting setting = new DefaultSemanticSearchSetting();

	Assert.assertEquals(ExpansionLevel.LOW, setting.getDefaultExpansionLevel());
	Assert.assertEquals(ExpansionLimit.of(LimitTarget.CONCEPTS, 50).toString(), setting.getDefaultExpansionLimit().toString());
	Assert.assertEquals(Arrays.asList(EuropeanLanguage.ENGLISH, EuropeanLanguage.ITALIAN).toString(),
		setting.getDefaultSearchLanguages().toString());
	Assert.assertEquals(Arrays.asList(EuropeanLanguage.ENGLISH, EuropeanLanguage.ITALIAN).toString(),
		setting.getDefaultSourceLanguages().toString());
	Assert.assertEquals(Arrays.asList(SKOSSemanticRelation.RELATED, SKOSSemanticRelation.NARROWER).toString(),
		setting.getDefaultSemanticRelations().toString());
	Assert.assertTrue(setting.isOriginalTermIncluded());

	//
	//
	//

	setting.setDefaultExpansionLimit(ExpansionLimit.of(LimitTarget.LABELS, 10));
	setting.setDefaultExpansionLevel(ExpansionLevel.HIGH);
	setting.setDefaultSearchLanguages(Arrays.asList(EuropeanLanguage.GERMAN));
	setting.setDefaultSourceLanguages(Arrays.asList(EuropeanLanguage.BULGARIAN));
	setting.setDefaultSemanticRelations(Arrays.asList(SKOSSemanticRelation.BROAD_MATCH));
	setting.setOriginalTermIncluded(false);

	Assert.assertEquals(ExpansionLevel.HIGH, setting.getDefaultExpansionLevel());
	Assert.assertEquals(ExpansionLimit.of(LimitTarget.LABELS, 10).toString(), setting.getDefaultExpansionLimit().toString());
	Assert.assertEquals(Arrays.asList(EuropeanLanguage.GERMAN).toString(), setting.getDefaultSearchLanguages().toString());
	Assert.assertEquals(Arrays.asList(EuropeanLanguage.BULGARIAN).toString(), setting.getDefaultSourceLanguages().toString());
	Assert.assertEquals(Arrays.asList(SKOSSemanticRelation.BROAD_MATCH).toString(), setting.getDefaultSemanticRelations().toString());
	Assert.assertFalse(setting.isOriginalTermIncluded());
    }
}
