/**
 * 
 */
package eu.essi_lab.cfga.gs.setting.systemsetting.test;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.cfga.SelectionUtils;
import eu.essi_lab.cfga.gs.setting.ontology.DefaultSemanticSearchSetting;
import eu.essi_lab.cfga.setting.Setting;
import eu.essi_lab.cfga.setting.SettingUtils;
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
    public void resetAndSelectTest() {

	DefaultSemanticSearchSetting setting = new DefaultSemanticSearchSetting();

	setting.setDefaultExpansionLimit(ExpansionLimit.of(LimitTarget.LABELS, 10));
	setting.setDefaultExpansionLevel(ExpansionLevel.HIGH);
	setting.setOriginalTermIncluded(false);
	setting.setDefaultMaxExecutionTime(25);

	setting.setDefaultSearchLanguages(Arrays.asList());
	setting.setDefaultSourceLanguages(Arrays.asList());
	setting.setDefaultSemanticRelations(Arrays.asList());

	Assert.assertTrue(setting.getDefaultSemanticRelations().isEmpty());

	//
	//
	//

	Setting resetAndSelect = SelectionUtils.resetAndSelect(setting, false);

	DefaultSemanticSearchSetting setting2 = SettingUtils.downCast(resetAndSelect, DefaultSemanticSearchSetting.class);

	Assert.assertEquals(ExpansionLevel.HIGH, setting2.getDefaultExpansionLevel());
	Assert.assertEquals(ExpansionLimit.of(LimitTarget.LABELS, 10).toString(), setting2.getDefaultExpansionLimit().toString());
	Assert.assertFalse(setting2.isOriginalTermIncluded());
	Assert.assertEquals(25, setting2.getDefaultMaxExecutionTime());

	Assert.assertEquals(0, setting2.getDefaultSearchLanguages().size());
	Assert.assertEquals(0, setting2.getDefaultSourceLanguages().size());
	Assert.assertEquals(0, setting2.getDefaultSemanticRelations().size());

	//
	//
	//

	SelectionUtils.deepClean(setting2);

	Assert.assertEquals(ExpansionLevel.HIGH, setting2.getDefaultExpansionLevel());
	Assert.assertEquals(ExpansionLimit.of(LimitTarget.LABELS, 10).toString(), setting2.getDefaultExpansionLimit().toString());
	Assert.assertFalse(setting2.isOriginalTermIncluded());
	Assert.assertEquals(25, setting2.getDefaultMaxExecutionTime());

	Assert.assertEquals(0, setting2.getDefaultSearchLanguages().size());
	Assert.assertEquals(0, setting2.getDefaultSourceLanguages().size());
	Assert.assertEquals(0, setting2.getDefaultSemanticRelations().size());

    }

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
	Assert.assertEquals(1, setting.getDefaultMaxExecutionTime());

	//
	//
	//

	setting.setDefaultExpansionLimit(ExpansionLimit.of(LimitTarget.LABELS, 10));
	setting.setDefaultExpansionLevel(ExpansionLevel.HIGH);
	setting.setDefaultSearchLanguages(Arrays.asList(EuropeanLanguage.GERMAN));
	setting.setDefaultSourceLanguages(Arrays.asList(EuropeanLanguage.BULGARIAN));
	setting.setDefaultSemanticRelations(Arrays.asList(SKOSSemanticRelation.BROAD_MATCH));
	setting.setOriginalTermIncluded(false);
	setting.setDefaultMaxExecutionTime(25);

	Assert.assertEquals(ExpansionLevel.HIGH, setting.getDefaultExpansionLevel());
	Assert.assertEquals(ExpansionLimit.of(LimitTarget.LABELS, 10).toString(), setting.getDefaultExpansionLimit().toString());
	Assert.assertEquals(Arrays.asList(EuropeanLanguage.GERMAN).toString(), setting.getDefaultSearchLanguages().toString());
	Assert.assertEquals(Arrays.asList(EuropeanLanguage.BULGARIAN).toString(), setting.getDefaultSourceLanguages().toString());
	Assert.assertEquals(Arrays.asList(SKOSSemanticRelation.BROAD_MATCH).toString(), setting.getDefaultSemanticRelations().toString());
	Assert.assertFalse(setting.isOriginalTermIncluded());
	Assert.assertEquals(25, setting.getDefaultMaxExecutionTime());

    }
}
