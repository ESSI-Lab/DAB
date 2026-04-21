package eu.essi_lab.cfga.similarity.test;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.augmenter.AugmentersSetting;
import eu.essi_lab.augmenter.worker.AugmenterWorkerSettingImpl;
import eu.essi_lab.cfga.SelectionUtils;
import eu.essi_lab.cfga.gs.setting.CredentialsSetting;
import eu.essi_lab.cfga.gs.setting.SystemSetting;
import eu.essi_lab.cfga.gs.setting.accessor.AccessorSetting;
import eu.essi_lab.cfga.gs.setting.augmenter.worker.AugmenterWorkerSetting;
import eu.essi_lab.cfga.gs.setting.harvesting.HarvestingSetting;
import eu.essi_lab.cfga.option.Option;
import eu.essi_lab.cfga.setting.Setting;
import eu.essi_lab.cfga.setting.scheduling.Scheduling;
import eu.essi_lab.harvester.worker.HarvestedAccessorsSetting;
import eu.essi_lab.harvester.worker.HarvestingSettingImpl;

/**
 * 
 */
public class GSSettingsSimilarTest {

    @Test
    public void SystemSettingTest1() {

	SystemSetting systemSetting1 = new SystemSetting();
	// property values are ignored
	systemSetting1.setIdentifier("id");

	SystemSetting systemSetting2 = new SystemSetting();
	// property values are ignored
	systemSetting2.setIdentifier("id2");

	Assert.assertTrue(systemSetting1.similar(systemSetting2));
    }

    @Test
    public void SystemSettingTest2() {

	SystemSetting systemSetting1 = new SystemSetting();
	systemSetting1.setIdentifier("id");
	systemSetting1.putKeyValue("key", "value");

	SystemSetting systemSetting2 = new SystemSetting();
	systemSetting2.setIdentifier("id");

	//
	// they both have the "keyValue" option; the "keyValue" option of systemSetting1 has
	// also a value set, but the values are ignored so the settings are similar
	//
	Assert.assertTrue(systemSetting1.similar(systemSetting2));
    }

    @Test
    public void SystemSettingTest3() {

	SystemSetting systemSetting1 = new SystemSetting();
	systemSetting1.setIdentifier("id1");
	systemSetting1.putKeyValue("key2", "value6");

	SystemSetting systemSetting2 = new SystemSetting();
	systemSetting2.setIdentifier("id2");
	systemSetting2.putKeyValue("key3", "value8");

	Assert.assertTrue(systemSetting1.similar(systemSetting2));
    }

    @Test
    public void SystemSettingTest4() {

	SystemSetting systemSetting1 = new SystemSetting();
	systemSetting1.setIdentifier("id1");
	systemSetting1.enableCompactMode(true);

	SystemSetting systemSetting2 = new SystemSetting();
	systemSetting2.setIdentifier("id2");
	systemSetting2.enableCompactMode(false);

	//
	// the COMPACT_MODE property has the optional key with a default value true
	// so when set to true, its key is removed, the settings have different keys (systemSetting1 misses the
	// COMPACT_MODE key, systemSetting2 don't) and they are not similar
	//

	Assert.assertFalse(systemSetting1.similar(systemSetting2));

	//
	//
	//

	Assert.assertTrue(systemSetting1.similar(//
		systemSetting2, //
		Arrays.asList(Setting.COMPACT_MODE.getKey())));

    }

    @Test
    public void SystemSettingTest5() {

	SystemSetting systemSetting1 = new SystemSetting();
	systemSetting1.setIdentifier("id");

	Option<String> option = new Option<String>(String.class);
	option.setKey("key");
	systemSetting1.addOption(option);

	SystemSetting systemSetting2 = new SystemSetting();
	systemSetting2.setIdentifier("id");

	Assert.assertFalse(systemSetting1.similar(systemSetting2));

	//
	//
	//

	systemSetting2.addOption(new Option<String>(option.toString()));

	//
	//
	//

	Assert.assertTrue(systemSetting1.similar(systemSetting2));

    }

    @Test
    public void SystemSettingTest7() {

	SystemSetting systemSetting1 = new SystemSetting();
	systemSetting1.setIdentifier("id");

	Option<Boolean> clone = new Option<Boolean>(systemSetting1.getOption("enableMailAugmentationReport").get().toString());

	boolean removed = systemSetting1.removeOption("enableMailAugmentationReport");
	Assert.assertTrue(removed);

	SystemSetting systemSetting2 = new SystemSetting();
	systemSetting2.setIdentifier("id");

	Assert.assertFalse(systemSetting1.similar(systemSetting2));

	//
	//
	//

	systemSetting1.addOption(clone);

	Assert.assertTrue(systemSetting1.similar(systemSetting2));
    }

    @Test
    public void HarvestingSettingTest1() {

	HarvestingSetting setting1 = new HarvestingSettingImpl();
	setting1.setIdentifier("id");

	HarvestingSetting setting2 = new HarvestingSettingImpl();
	setting2.setIdentifier("id");

	Assert.assertTrue(setting1.similar(setting2));
    }

    @Test
    public void HarvestingSettingTest2() {

	HarvestingSetting setting1 = new HarvestingSettingImpl();
	setting1.setIdentifier("id");

	Scheduling scheduling1 = setting1.getScheduling();

	HarvestingSetting setting2 = new HarvestingSettingImpl();
	setting2.setIdentifier("id");

	Scheduling scheduling2 = setting2.getScheduling();

	Assert.assertTrue(scheduling1.similar(scheduling2));

	Assert.assertTrue(setting1.similar(setting2));
    }

    @Test
    public void HarvestingSettingTest3() {

	HarvestingSetting setting1 = new HarvestingSettingImpl();
	setting1.setIdentifier("id");

	Scheduling scheduling1 = setting1.getScheduling();
	scheduling1.setRepeatCount(1);

	HarvestingSetting setting2 = new HarvestingSettingImpl();
	setting2.setIdentifier("id");

	Scheduling scheduling2 = setting2.getScheduling();
	scheduling2.setRepeatCount(2);

	Assert.assertTrue(scheduling1.similar(scheduling2));

	Assert.assertTrue(setting1.similar(setting2));
    }

    @Test
    public void HarvestingSettingTest4() {

	HarvestingSetting setting1 = new HarvestingSettingImpl();
	setting1.setIdentifier("id");

	Scheduling scheduling1 = setting1.getScheduling();
	scheduling1.setCanBeDisabled(false);

	HarvestingSetting setting2 = new HarvestingSettingImpl();
	setting2.setIdentifier("id");

	Scheduling scheduling2 = setting2.getScheduling();
	scheduling2.setRepeatCount(2);
	scheduling2.setCanBeDisabled(true);

	Assert.assertFalse(scheduling1.similar(scheduling2));

	Assert.assertFalse(setting1.similar(setting2));
    }

    @Test
    public void HarvestingSettingTest5() {

	HarvestingSetting setting1 = new HarvestingSettingImpl();
	setting1.setIdentifier("id");

	SelectionUtils.deepClean(setting1);

	HarvestingSetting setting2 = new HarvestingSettingImpl();
	setting2.setIdentifier("id");

	SelectionUtils.deepClean(setting2);

	Assert.assertTrue(setting1.similar(setting2));
    }

    @Test
    public void HarvestingSettingTest6() {

	HarvestingSetting setting1 = new HarvestingSettingImpl();
	setting1.setIdentifier("id");

	SelectionUtils.deepClean(setting1);

	HarvestingSetting setting2 = new HarvestingSettingImpl();
	setting2.setIdentifier("id");

	Assert.assertFalse(setting1.similar(setting2));
    }

    @Test
    public void HarvestingSettingTest7() {

	HarvestingSetting setting1 = new HarvestingSettingImpl();
	setting1.setIdentifier("id");

	Setting accessorsSetting1 = setting1.getAccessorsSetting();
	accessorsSetting1.select(s -> s.getIdentifier().equals("ONAMET"));

	AccessorSetting selectedAccessorSetting1 = setting1.getSelectedAccessorSetting();
	Assert.assertEquals("ONAMET", selectedAccessorSetting1.getIdentifier());

	//
	//
	//

	HarvestingSetting setting2 = new HarvestingSettingImpl();
	setting2.setIdentifier("id");

	Setting accessorsSetting2 = setting2.getAccessorsSetting();
	accessorsSetting2.select(s -> s.getIdentifier().equals("EGASKRO"));

	AccessorSetting selectedAccessorSetting2 = setting2.getSelectedAccessorSetting();
	Assert.assertEquals("EGASKRO", selectedAccessorSetting2.getIdentifier());

	//
	// they differ due to the Setting#SELECTED property
	//

	Assert.assertFalse(accessorsSetting1.similar(accessorsSetting2));
	Assert.assertFalse(setting1.similar(setting2));

	//
	// excluding the Setting#SELECTED property makes the settings similar
	//

	Assert.assertTrue(accessorsSetting1.similar(accessorsSetting2, Arrays.asList(Setting.SELECTED.getKey())));
	Assert.assertTrue(setting1.similar(setting2, Arrays.asList(Setting.SELECTED.getKey())));

    }

    @Test
    public void HarvestingSettingTest7_2() {

	HarvestedAccessorsSetting accessorsSetting1 = new HarvestedAccessorsSetting();

	accessorsSetting1.select(s -> s.getIdentifier().equals("ONAMET"));

	Setting selectedAccessorSetting1 = accessorsSetting1.//
		getSettings().//
		stream().//
		filter(s -> s.isSelected()).//
		findFirst().//
		get();

	Assert.assertEquals("ONAMET", selectedAccessorSetting1.getIdentifier());

	//
	//
	//

	HarvestedAccessorsSetting accessorsSetting2 = new HarvestedAccessorsSetting();

	accessorsSetting2.select(s -> s.getIdentifier().equals("EGASKRO"));

	Setting selectedAccessorSetting2 = accessorsSetting2.//
		getSettings().//
		stream().//
		filter(s -> s.isSelected()).//
		findFirst().//
		get();

	Assert.assertEquals("EGASKRO", selectedAccessorSetting2.getIdentifier());

	//
	// they differ due to the Setting#SELECTED property
	//
	Assert.assertFalse(accessorsSetting1.similar(accessorsSetting2));

	SelectionUtils.deepClean(accessorsSetting1);
	SelectionUtils.deepClean(accessorsSetting2);

	//
	// after clean, they differ because they have a different (single) sub-setting, with different ids
	//
	Assert.assertFalse(accessorsSetting1.similar(accessorsSetting2));
    }

    @Test
    public void HarvestingSettingTest7_3() {

	HarvestedAccessorsSetting accessorsSetting1 = new HarvestedAccessorsSetting();

	accessorsSetting1.select(s -> s.getIdentifier().equals("ONAMET"));

	Setting selectedAccessorSetting1 = accessorsSetting1.//
		getSettings().//
		stream().//
		filter(s -> s.isSelected()).//
		findFirst().//
		get();

	Assert.assertEquals("ONAMET", selectedAccessorSetting1.getIdentifier());

	//
	//
	//

	HarvestedAccessorsSetting accessorsSetting2 = new HarvestedAccessorsSetting();

	accessorsSetting2.select(s -> s.getIdentifier().equals("EGASKRO"));

	Setting selectedAccessorSetting2 = accessorsSetting2.//
		getSettings().//
		stream().//
		filter(s -> s.isSelected()).//
		findFirst().//
		get();

	Assert.assertEquals("EGASKRO", selectedAccessorSetting2.getIdentifier());

	//
	// they differ due to the Setting#SELECTED property
	//
	Assert.assertFalse(accessorsSetting1.similar(accessorsSetting2));

	//
	//
	//

	accessorsSetting2.select(s -> s.getIdentifier().equals("ONAMET"));

	selectedAccessorSetting2 = accessorsSetting2.//
		getSettings().//
		stream().//
		filter(s -> s.isSelected()).//
		findFirst().//
		get();

	Assert.assertEquals("ONAMET", selectedAccessorSetting2.getIdentifier());

	//
	// with the same selection, they are similar
	//
	Assert.assertTrue(accessorsSetting1.similar(accessorsSetting2));
    }

    @Test
    public void HarvestingSettingTest8() {

	HarvestingSetting setting1 = new HarvestingSettingImpl();
	setting1.setIdentifier("id");

	Setting accessorsSetting1 = setting1.getAccessorsSetting();
	accessorsSetting1.select(s -> s.getIdentifier().equals("ONAMET"));

	AccessorSetting selectedAccessorSetting1 = setting1.getSelectedAccessorSetting();
	Assert.assertEquals("ONAMET", selectedAccessorSetting1.getIdentifier());

	HarvestingSetting setting2 = new HarvestingSettingImpl();
	setting2.setIdentifier("id");

	Setting accessorsSetting2 = setting2.getAccessorsSetting();
	accessorsSetting2.select(s -> s.getIdentifier().equals("EGASKRO"));

	AccessorSetting selectedAccessorSetting2 = setting2.getSelectedAccessorSetting();
	Assert.assertEquals("EGASKRO", selectedAccessorSetting2.getIdentifier());

	SelectionUtils.deepClean(setting1);
	SelectionUtils.deepClean(setting2);

	Assert.assertFalse(accessorsSetting1.similar(accessorsSetting2));

	Assert.assertFalse(setting1.similar(setting2));
    }

    @Test
    public void AugmenterWorkerSettingTest() {

	AugmenterWorkerSetting setting1 = new AugmenterWorkerSettingImpl();
	AugmenterWorkerSetting setting2 = new AugmenterWorkerSettingImpl();

	Assert.assertTrue(setting1.similar(setting2));
    }

    @Test
    public void AugmenterWorkerSettingTest_0() {

	AugmenterWorkerSetting setting1 = new AugmenterWorkerSettingImpl() {
	};
	AugmenterWorkerSetting setting2 = new AugmenterWorkerSettingImpl() {
	};

	Assert.assertFalse(setting1.similar(setting2));
    }

    @Test
    public void AugmenterWorkerSettingTest1() {

	AugmenterWorkerSetting setting1 = new AugmenterWorkerSettingImpl();
	setting1.setIdentifier("id");

	AugmenterWorkerSetting setting2 = new AugmenterWorkerSettingImpl();
	setting2.setIdentifier("id");

	Assert.assertTrue(setting1.similar(setting2));
    }

    @Test
    public void AugmenterWorkerSettingTest2() {

	AugmenterWorkerSetting setting1 = new AugmenterWorkerSettingImpl();
	setting1.setIdentifier("id");

	Setting augmentersSetting1 = setting1.getAugmentersSetting();
	augmentersSetting1.select(s -> s.getIdentifier().equals("DownloadReportToSSCScoreAugmenter"));

	Assert.assertEquals("DownloadReportToSSCScoreAugmenter", setting1.getSelectedAugmenterSettings().get(0).getIdentifier());

	//
	//
	//

	AugmenterWorkerSetting setting2 = new AugmenterWorkerSettingImpl();
	setting2.setIdentifier("id");

	Setting augmentersSetting2 = setting2.getAugmentersSetting();
	augmentersSetting2.select(s -> s.getIdentifier().equals("WHOSVariableAugmenter"));

	Assert.assertEquals("WHOSVariableAugmenter", setting2.getSelectedAugmenterSettings().get(0).getIdentifier());

	//
	//
	//

	Assert.assertFalse(augmentersSetting1.similar(augmentersSetting2));
	Assert.assertFalse(setting1.similar(setting2));

	//
	//
	//

	SelectionUtils.deepClean(setting1);
	SelectionUtils.deepClean(setting2);

	//
	//
	//

	Assert.assertFalse(augmentersSetting1.similar(augmentersSetting2));
	Assert.assertFalse(setting1.similar(setting2));
    }

    @Test
    public void AugmenterWorkerSettingTest3_1() {

	AugmentersSetting augmentersSetting1 = new AugmentersSetting();
	augmentersSetting1.select(s -> s.getIdentifier().equals("DownloadReportToSSCScoreAugmenter"));

	Assert.assertEquals("DownloadReportToSSCScoreAugmenter", augmentersSetting1.//
		getSettings().//
		stream().//
		filter(s -> s.getIdentifier().equals("DownloadReportToSSCScoreAugmenter")).//
		findFirst().//
		get().//
		getIdentifier());

	//
	//
	//

	AugmentersSetting augmentersSetting2 = new AugmentersSetting();
	augmentersSetting2.select(s -> s.getIdentifier().equals("WHOSVariableAugmenter"));

	Assert.assertEquals("WHOSVariableAugmenter", augmentersSetting2.//
		getSettings().//
		stream().//
		filter(s -> s.getIdentifier().equals("WHOSVariableAugmenter")).//
		findFirst().//
		get().//
		getIdentifier());

	Assert.assertFalse(augmentersSetting1.similar(augmentersSetting2));

	Assert.assertTrue(augmentersSetting1.similar(augmentersSetting2, Arrays.asList(Setting.SELECTED.getKey())));
    }

    @Test
    public void AugmenterWorkerSettingTest3_2() {

	AugmentersSetting augmentersSetting1 = new AugmentersSetting();
	augmentersSetting1.select(s -> s.getIdentifier().equals("DownloadReportToSSCScoreAugmenter"));

	Setting clone = augmentersSetting1.clone();
	clone.reset();

	Assert.assertFalse(clone.similar(augmentersSetting1));

	Assert.assertTrue(clone.similar(augmentersSetting1, Arrays.asList(Setting.SELECTED.getKey())));
    }

    @Test
    public void AugmenterWorkerSettingTest3_3() {

	AugmentersSetting augmentersSetting1 = new AugmentersSetting();
	augmentersSetting1.select(s -> s.getIdentifier().equals("DownloadReportToSSCScoreAugmenter"));

	SelectionUtils.deepClean(augmentersSetting1);

	Setting clone = augmentersSetting1.clone();
	clone.reset();

	Assert.assertFalse(clone.similar(augmentersSetting1));
    }

    @Test
    public void CredentialsSettingTest() {

	CredentialsSetting setting1 = new CredentialsSetting();
	setting1.setIdentifier("id");
	setting1.setDinaguaPassword("pwd");

	CredentialsSetting setting2 = new CredentialsSetting();
	setting2.setIdentifier("id");
	setting2.setDMHToken("token");

	Assert.assertTrue(setting1.similar(setting2));
    }

    @Test
    public void CredentialsSettingTest2() {

	CredentialsSetting setting1 = new CredentialsSetting();
	setting1.setIdentifier("id");
	setting1.setDinaguaPassword("pwd");

	CredentialsSetting setting2 = new CredentialsSetting();
	setting2.setIdentifier("id");
	setting2.setDinaguaPassword("pwd");

	Assert.assertTrue(setting1.similar(setting2));
    }

    @Test
    public void CredentialsSettingTest3() {

	CredentialsSetting setting1 = new CredentialsSetting();
	setting1.setIdentifier("id");
	setting1.removeOption("dinaguaPassword");

	CredentialsSetting setting2 = new CredentialsSetting();
	setting2.setIdentifier("id");

	Assert.assertFalse(setting1.similar(setting2));
    }

    @Test
    public void CredentialsSettingTest4() {

	CredentialsSetting setting1 = new CredentialsSetting();
	setting1.setIdentifier("id");

	CredentialsSetting setting2 = new CredentialsSetting();
	setting2.setIdentifier("id");
	Option<String> option = new Option<String>(String.class);
	option.setKey("key");
	setting2.addOption(option);

	Assert.assertFalse(setting1.similar(setting2));
    }

}
