package eu.essi_lab.cfga.gs.setting.systemsetting.test;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.cfga.SelectionUtils;
import eu.essi_lab.cfga.gs.setting.SystemSetting;
import eu.essi_lab.cfga.gs.setting.ontology.DefaultSemanticSearchSetting;
import eu.essi_lab.cfga.setting.Setting;
import eu.essi_lab.cfga.setting.SettingUtils;
import eu.essi_lab.lib.skos.SKOSSemanticRelation;
import eu.essi_lab.lib.skos.expander.ExpansionLimit;
import eu.essi_lab.lib.skos.expander.ConceptsExpander.ExpansionLevel;
import eu.essi_lab.lib.skos.expander.ExpansionLimit.LimitTarget;
import eu.essi_lab.lib.utils.EuropeanLanguage;

/**
 * @author Fabrizio
 */
public class SystemSettingTest {

    @Test
    public void resetAndSelectTest() {

	SystemSetting systemSetting = new SystemSetting();

	{

	    Assert.assertTrue(systemSetting.getProxyEndpoint().isEmpty());

	    DefaultSemanticSearchSetting semSetting = systemSetting.getDefaultSemanticSearchSetting();

	    Assert.assertEquals(ExpansionLevel.LOW, semSetting.getDefaultExpansionLevel());
	    Assert.assertEquals(ExpansionLimit.of(LimitTarget.CONCEPTS, 50).toString(), semSetting.getDefaultExpansionLimit().toString());
	    Assert.assertEquals(Arrays.asList(EuropeanLanguage.ENGLISH, EuropeanLanguage.ITALIAN).toString(),
		    semSetting.getDefaultSearchLanguages().toString());
	    Assert.assertEquals(Arrays.asList(EuropeanLanguage.ENGLISH, EuropeanLanguage.ITALIAN).toString(),
		    semSetting.getDefaultSourceLanguages().toString());
	    Assert.assertEquals(Arrays.asList(SKOSSemanticRelation.RELATED, SKOSSemanticRelation.NARROWER).toString(),
		    semSetting.getDefaultSemanticRelations().toString());
	    Assert.assertTrue(semSetting.isOriginalTermIncluded());
	    Assert.assertEquals(1, semSetting.getDefaultMaxExecutionTime());
	}

	//
	// no changes to the semantic and system setting test
	//

	{

	    Setting resetAndSelect = SelectionUtils.resetAndSelect(systemSetting, false);

	    SystemSetting systemSetting2 = SettingUtils.downCast(resetAndSelect, SystemSetting.class);

	    Assert.assertTrue(systemSetting2.getProxyEndpoint().isEmpty());

	    DefaultSemanticSearchSetting semSetting2 = systemSetting2.getDefaultSemanticSearchSetting();

	    Assert.assertEquals(ExpansionLevel.LOW, semSetting2.getDefaultExpansionLevel());
	    Assert.assertEquals(ExpansionLimit.of(LimitTarget.CONCEPTS, 50).toString(), semSetting2.getDefaultExpansionLimit().toString());
	    Assert.assertEquals(Arrays.asList(EuropeanLanguage.ENGLISH, EuropeanLanguage.ITALIAN).toString(),
		    semSetting2.getDefaultSearchLanguages().toString());
	    Assert.assertEquals(Arrays.asList(EuropeanLanguage.ENGLISH, EuropeanLanguage.ITALIAN).toString(),
		    semSetting2.getDefaultSourceLanguages().toString());
	    Assert.assertEquals(Arrays.asList(SKOSSemanticRelation.RELATED, SKOSSemanticRelation.NARROWER).toString(),
		    semSetting2.getDefaultSemanticRelations().toString());
	    Assert.assertTrue(semSetting2.isOriginalTermIncluded());
	    Assert.assertEquals(1, semSetting2.getDefaultMaxExecutionTime());

	    SelectionUtils.deepClean(systemSetting2);

	    DefaultSemanticSearchSetting semSetting3 = systemSetting2.getDefaultSemanticSearchSetting();

	    Assert.assertEquals(ExpansionLevel.LOW, semSetting3.getDefaultExpansionLevel());
	    Assert.assertEquals(ExpansionLimit.of(LimitTarget.CONCEPTS, 50).toString(), semSetting3.getDefaultExpansionLimit().toString());
	    Assert.assertEquals(Arrays.asList(EuropeanLanguage.ENGLISH, EuropeanLanguage.ITALIAN).toString(),
		    semSetting3.getDefaultSearchLanguages().toString());
	    Assert.assertEquals(Arrays.asList(EuropeanLanguage.ENGLISH, EuropeanLanguage.ITALIAN).toString(),
		    semSetting3.getDefaultSourceLanguages().toString());
	    Assert.assertEquals(Arrays.asList(SKOSSemanticRelation.RELATED, SKOSSemanticRelation.NARROWER).toString(),
		    semSetting3.getDefaultSemanticRelations().toString());
	    Assert.assertTrue(semSetting3.isOriginalTermIncluded());
	    Assert.assertEquals(1, semSetting3.getDefaultMaxExecutionTime());
	}

	//
	// changes to the semantic setting, in particular all the multi-selection options empty and to the system
	// setting
	//

	systemSetting.setProxyEndpoint("endpoint");

	DefaultSemanticSearchSetting semSetting3 = systemSetting.getDefaultSemanticSearchSetting();

	semSetting3.setDefaultExpansionLimit(ExpansionLimit.of(LimitTarget.LABELS, 10));
	semSetting3.setDefaultExpansionLevel(ExpansionLevel.HIGH);
	semSetting3.setOriginalTermIncluded(false);
	semSetting3.setDefaultMaxExecutionTime(25);

	semSetting3.setDefaultSearchLanguages(List.of());
	semSetting3.setDefaultSourceLanguages(List.of());
	semSetting3.setDefaultSemanticRelations(List.of());

	Assert.assertTrue(semSetting3.getDefaultSemanticRelations().isEmpty());

	//
	// changes to the semantic setting, in particular all the multi-selection options empty
	//

	Setting resetAndSelect2 = SelectionUtils.resetAndSelect(systemSetting, false);

	SystemSetting systemSetting3 = SettingUtils.downCast(resetAndSelect2, SystemSetting.class);

	Assert.assertEquals("endpoint", systemSetting3.getProxyEndpoint().get());

	DefaultSemanticSearchSetting semSetting4 = systemSetting3.getDefaultSemanticSearchSetting();

	Assert.assertEquals(ExpansionLevel.HIGH, semSetting4.getDefaultExpansionLevel());
	Assert.assertEquals(ExpansionLimit.of(LimitTarget.LABELS, 10).toString(), semSetting4.getDefaultExpansionLimit().toString());
	Assert.assertFalse(semSetting4.isOriginalTermIncluded());
	Assert.assertEquals(25, semSetting4.getDefaultMaxExecutionTime());

	Assert.assertEquals(0, semSetting4.getDefaultSearchLanguages().size());
	Assert.assertEquals(0, semSetting4.getDefaultSourceLanguages().size());
	Assert.assertEquals(0, semSetting4.getDefaultSemanticRelations().size());

	//
	//
	//

	SelectionUtils.deepClean(systemSetting3);

	Assert.assertEquals("endpoint", systemSetting3.getProxyEndpoint().get());

	Assert.assertEquals(ExpansionLevel.HIGH, semSetting4.getDefaultExpansionLevel());
	Assert.assertEquals(ExpansionLimit.of(LimitTarget.LABELS, 10).toString(), semSetting4.getDefaultExpansionLimit().toString());
	Assert.assertFalse(semSetting4.isOriginalTermIncluded());
	Assert.assertEquals(25, semSetting4.getDefaultMaxExecutionTime());

	Assert.assertEquals(0, semSetting4.getDefaultSearchLanguages().size());
	Assert.assertEquals(0, semSetting4.getDefaultSourceLanguages().size());
	Assert.assertEquals(0, semSetting4.getDefaultSemanticRelations().size());

	SelectionUtils.deepClean(semSetting4);

	Assert.assertEquals(ExpansionLevel.HIGH, semSetting4.getDefaultExpansionLevel());
	Assert.assertEquals(ExpansionLimit.of(LimitTarget.LABELS, 10).toString(), semSetting4.getDefaultExpansionLimit().toString());
	Assert.assertFalse(semSetting4.isOriginalTermIncluded());
	Assert.assertEquals(25, semSetting4.getDefaultMaxExecutionTime());

	Assert.assertEquals(0, semSetting4.getDefaultSearchLanguages().size());
	Assert.assertEquals(0, semSetting4.getDefaultSourceLanguages().size());
	Assert.assertEquals(0, semSetting4.getDefaultSemanticRelations().size());

    }

    @Test
    public void test() {

	SystemSetting setting = new SystemSetting();

	System.out.println(setting);

	initTest(setting);
	initTest(new SystemSetting(setting.getObject()));
	initTest(new SystemSetting(setting.getObject().toString()));
	initTest(SettingUtils.downCast(setting, SystemSetting.class, true));

	setting.setProxyEndpoint("endpoint");

	test(setting);
	test(new SystemSetting(setting.getObject()));
	test(new SystemSetting(setting.getObject().toString()));
	test(SettingUtils.downCast(setting, SystemSetting.class, true));

	//
	//
	//

	boolean replaced1 = setting.putKeyValue("key1", "value1");
	Assert.assertFalse(replaced1);

	Properties properties = setting.getKeyValueOptions().get();

	Assert.assertEquals(1, properties.size());

	Assert.assertEquals("value1", properties.get("key1"));

	Assert.assertEquals("value1", setting.readProperty("key1").get());

	//
	//
	//

	boolean replaced2 = setting.putKeyValue("key2", "value2");
	Assert.assertFalse(replaced2);

	properties = setting.getKeyValueOptions().get();

	Assert.assertEquals(2, properties.size());

	Assert.assertEquals("value1", properties.get("key1"));
	Assert.assertEquals("value2", properties.get("key2"));

	Assert.assertEquals("value1", setting.readProperty("key1").get());
	Assert.assertEquals("value2", setting.readProperty("key2").get());

	//
	//

	boolean replaced3 = setting.putKeyValue("key2", "value5");
	Assert.assertTrue(replaced3);

	properties = setting.getKeyValueOptions().get();

	Assert.assertEquals(2, properties.size());

	Assert.assertEquals("value1", properties.get("key1"));
	Assert.assertEquals("value5", properties.get("key2"));

	Assert.assertEquals("value1", setting.readProperty("key1").get());
	Assert.assertEquals("value5", setting.readProperty("key2").get());

	//
	//

	boolean replaced4 = setting.putKeyValue("key1", "value9");
	Assert.assertTrue(replaced4);

	properties = setting.getKeyValueOptions().get();

	Assert.assertEquals(2, properties.size());

	Assert.assertEquals("value9", properties.get("key1"));
	Assert.assertEquals("value5", properties.get("key2"));

	Assert.assertEquals("value9", setting.readProperty("key1").get());
	Assert.assertEquals("value5", setting.readProperty("key2").get());

	//
	//
	//

	boolean replaced5 = setting.putKeyValue("key3", "abcd");
	boolean replaced6 = setting.putKeyValue("key4", "xyz");

	Assert.assertFalse(replaced5);
	Assert.assertFalse(replaced6);

	properties = setting.getKeyValueOptions().get();

	Assert.assertEquals(4, properties.size());

	Assert.assertEquals("value9", properties.get("key1"));
	Assert.assertEquals("value5", properties.get("key2"));
	Assert.assertEquals("abcd", properties.get("key3"));
	Assert.assertEquals("xyz", properties.get("key4"));

	Assert.assertEquals("value9", setting.readProperty("key1").get());
	Assert.assertEquals("value5", setting.readProperty("key2").get());
	Assert.assertEquals("abcd", setting.readProperty("key3").get());
	Assert.assertEquals("xyz", setting.readProperty("key4").get());

	//
	//
	//

	boolean replaced7 = setting.putKeyValue("key3", "1234");

	Assert.assertTrue(replaced7);

	properties = setting.getKeyValueOptions().get();

	Assert.assertEquals(4, properties.size());

	Assert.assertEquals("value9", properties.get("key1"));
	Assert.assertEquals("value5", properties.get("key2"));
	Assert.assertEquals("1234", properties.get("key3"));
	Assert.assertEquals("xyz", properties.get("key4"));

	Assert.assertEquals("value9", setting.readProperty("key1").get());
	Assert.assertEquals("value5", setting.readProperty("key2").get());
	Assert.assertEquals("1234", setting.readProperty("key3").get());
	Assert.assertEquals("xyz", setting.readProperty("key4").get());

	//
	//
	//

	boolean replaced8 = setting.putKeyValue("key4", "xxx");

	Assert.assertTrue(replaced8);

	properties = setting.getKeyValueOptions().get();

	Assert.assertEquals(4, properties.size());

	Assert.assertEquals("value9", properties.get("key1"));
	Assert.assertEquals("value5", properties.get("key2"));
	Assert.assertEquals("1234", properties.get("key3"));
	Assert.assertEquals("xxx", properties.get("key4"));

	Assert.assertEquals("value9", setting.readProperty("key1").get());
	Assert.assertEquals("value5", setting.readProperty("key2").get());
	Assert.assertEquals("1234", setting.readProperty("key3").get());
	Assert.assertEquals("xxx", setting.readProperty("key4").get());

	//
	//
	//

	setting.enableAugmentationReportMail(true);
	setting.enableErrorLogsReportEmail(true);
	setting.enableHarvestingReportEmail(true);
	setting.enableDownloadReportMail(true);

	Assert.assertTrue(setting.isAugmentationReportMailEnabled());
	Assert.assertTrue(setting.isErrorLogsReportEnabled());
	Assert.assertTrue(setting.isHarvestingReportMailEnabled());
	Assert.assertTrue(setting.isDownloadReportMailEnabled());

	setting.enableDownloadReportMail(false);
	setting.enableAugmentationReportMail(false);
	setting.enableErrorLogsReportEmail(false);
	setting.enableHarvestingReportEmail(false);

	Assert.assertFalse(setting.isAugmentationReportMailEnabled());
	Assert.assertFalse(setting.isErrorLogsReportEnabled());
	Assert.assertFalse(setting.isHarvestingReportMailEnabled());
    }

    /**
     * @param setting
     */
    private void test(SystemSetting setting) {

	Assert.assertEquals("endpoint", setting.getProxyEndpoint().get());
    }

    /**
     * @param setting
     */
    private void initTest(SystemSetting setting) {

	Assert.assertFalse(setting.getProxyEndpoint().isPresent());

	Assert.assertFalse(setting.isHarvestingReportMailEnabled());
	Assert.assertFalse(setting.isAugmentationReportMailEnabled());
	Assert.assertFalse(setting.isDownloadReportMailEnabled());
	Assert.assertFalse(setting.isErrorLogsReportEnabled());

	Assert.assertFalse(setting.getEmailSetting().isPresent());

	Assert.assertFalse(setting.areStatisticsEnabled());
	Assert.assertFalse(setting.getStatisticsSetting().isPresent());

	Assert.assertFalse(setting.getKeyValueOptions().isPresent());
    }
}
