/**
 * 
 */
package eu.essi_lab.cfga.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.cfga.SelectionUtils;
import eu.essi_lab.cfga.check.CheckResponse;
import eu.essi_lab.cfga.check.CheckResponse.CheckResult;
import eu.essi_lab.cfga.check.scheme.SchemeItem;
import eu.essi_lab.cfga.check.scheme.SchemeMethod;
import eu.essi_lab.cfga.check.scheme.SchemeItem.Descriptor;
import eu.essi_lab.cfga.check.scheme.SchemeMethod.CheckMode;
import eu.essi_lab.cfga.gs.DefaultConfiguration;
import eu.essi_lab.cfga.gs.DefaultConfiguration.SingletonSettingsId;
import eu.essi_lab.cfga.gs.GSTabIndex;
import eu.essi_lab.cfga.gs.setting.CredentialsSetting;
import eu.essi_lab.cfga.gs.setting.DownloadSetting;
import eu.essi_lab.cfga.gs.setting.GDCSourcesSetting;
import eu.essi_lab.cfga.gs.setting.ProfilerSetting;
import eu.essi_lab.cfga.gs.setting.SystemSetting;
import eu.essi_lab.cfga.gs.setting.database.DatabaseSetting;
import eu.essi_lab.cfga.gs.setting.database.SourceStorageSetting;
import eu.essi_lab.cfga.gs.setting.database.UsersDatabaseSetting;
import eu.essi_lab.cfga.gs.setting.distribution.DistributionSetting;
import eu.essi_lab.cfga.gs.setting.driver.SharedCacheDriverSetting;
import eu.essi_lab.cfga.gs.setting.driver.SharedPersistentDriverSetting;
import eu.essi_lab.cfga.gui.TabIndex;
import eu.essi_lab.cfga.setting.Setting;
import eu.essi_lab.harvester.worker.HarvestingSettingImpl;

/**
 * @author Fabrizio
 */
public class SchemeCheckMethodTest {

    private int count;

    @Test
    public void missingTest0() {

	DefaultConfiguration configuration = new DefaultConfiguration();
	SelectionUtils.deepClean(configuration);

	SchemeMethod checkMethod = new SchemeMethod();
	checkMethod.setCheckMode(CheckMode.MISSING_SETTINGS);

	checkMethod.setScheme(configuration.getScheme().get());

	CheckResponse check = checkMethod.check(configuration);

	CheckResult checkResult = check.getCheckResult();

	Assert.assertEquals(CheckResult.CHECK_SUCCESSFUL, checkResult);

	Assert.assertEquals(0, check.getSettings().size());
    }

    @Test
    public void missingTest1() {

	DefaultConfiguration configuration = new DefaultConfiguration();
	SelectionUtils.deepClean(configuration);

	SchemeMethod checkMethod = new SchemeMethod();
	checkMethod.setCheckMode(CheckMode.MISSING_SETTINGS);

	List<SchemeItem> list = configuration.getScheme().get().getItems();

	list.add(create(true, () -> new DatabaseSetting(), s -> s.getIdentifier().equals("TEST")));

	checkMethod.setItems(list);

	CheckResponse checkReponse = checkMethod.check(configuration);

	Assert.assertEquals(CheckResult.CHECK_FAILED, checkReponse.getCheckResult());

	Assert.assertEquals(1, checkReponse.getSettings().size());

	Assert.assertEquals(DatabaseSetting.class, checkReponse.getSettings().get(0).getSettingClass());

	//
	//
	//

	checkMethod.setCheckMode(CheckMode.REDUNDANT_SETTINGS);

	checkReponse = checkMethod.check(configuration);

	Assert.assertEquals(CheckResult.CHECK_SUCCESSFUL, checkReponse.getCheckResult());
    }

    @Test
    public void missingTest2() {

	DefaultConfiguration configuration = new DefaultConfiguration();
	SelectionUtils.deepClean(configuration);

	SchemeMethod checkMethod = new SchemeMethod();
	checkMethod.setCheckMode(CheckMode.MISSING_SETTINGS);

	List<SchemeItem> list = configuration.getScheme().get().getItems();

	list.add(create(true, () -> new DatabaseSetting(), s -> s.getIdentifier().equals("TEST")));
	list.add(create(true, () -> new SystemSetting(), s -> s.getIdentifier().equals("TEST1")));
	list.add(create(true, () -> new DownloadSetting(), s -> s.getIdentifier().equals("TEST2")));

	checkMethod.setItems(list);

	CheckResponse checkReponse = checkMethod.check(configuration);

	Assert.assertEquals(CheckResult.CHECK_FAILED, checkReponse.getCheckResult());

	Assert.assertEquals(3, checkReponse.getSettings().size());

	List<Setting> sorted = checkReponse.getSettings().//
		stream().//
		sorted((s1, s2) -> s1.getSettingClass().getSimpleName().compareTo(s2.getSettingClass().getSimpleName())).//
		collect(Collectors.toList());

	Assert.assertEquals(DatabaseSetting.class, sorted.get(0).getSettingClass());
	Assert.assertEquals(DownloadSetting.class, sorted.get(1).getSettingClass());
	Assert.assertEquals(SystemSetting.class, sorted.get(2).getSettingClass());

	//
	//
	//

	checkMethod.setCheckMode(CheckMode.REDUNDANT_SETTINGS);

	checkReponse = checkMethod.check(configuration);

	Assert.assertEquals(CheckResult.CHECK_SUCCESSFUL, checkReponse.getCheckResult());
    }

    @Test
    public void missingTest3() {

	DefaultConfiguration configuration = new DefaultConfiguration();
	SelectionUtils.deepClean(configuration);

	SchemeMethod checkMethod = new SchemeMethod();
	checkMethod.setCheckMode(CheckMode.MISSING_SETTINGS);

	List<SchemeItem> list = configuration.getScheme().get().getItems();

	list.add(create(true, () -> new DatabaseSetting(), s -> s.getIdentifier().equals("TEST")));
	list.add(create(false, () -> new DownloadSetting(), s -> s.getIdentifier().equals("TEST1")));
	list.add(create(false, () -> new SystemSetting(), s -> s.getIdentifier().equals("TEST2")));
	list.add(create(true, () -> new GDCSourcesSetting(), s -> s.getIdentifier().equals("TEST3")));

	checkMethod.setItems(list);

	CheckResponse checkReponse = checkMethod.check(configuration);

	Assert.assertEquals(CheckResult.CHECK_FAILED, checkReponse.getCheckResult());

	Assert.assertEquals(2, checkReponse.getSettings().size());

	List<Setting> sorted = checkReponse.getSettings().//
		stream().//
		sorted((s1, s2) -> s1.getSettingClass().getSimpleName().compareTo(s2.getSettingClass().getSimpleName())).//
		collect(Collectors.toList());

	Assert.assertEquals(DatabaseSetting.class, sorted.get(0).getSettingClass());
	Assert.assertEquals(GDCSourcesSetting.class, sorted.get(1).getSettingClass());

	//
	//
	//

	checkMethod.setCheckMode(CheckMode.REDUNDANT_SETTINGS);

	checkReponse = checkMethod.check(configuration);

	Assert.assertEquals(CheckResult.CHECK_SUCCESSFUL, checkReponse.getCheckResult());
    }

    @Test
    public void missingTest4() {

	DefaultConfiguration configuration = new DefaultConfiguration();
	SelectionUtils.deepClean(configuration);

	SchemeMethod checkMethod = new SchemeMethod();
	checkMethod.setCheckMode(CheckMode.MISSING_SETTINGS);

	List<SchemeItem> list = configuration.getScheme().get().getItems();

	//
	// a tab with multiple kind of settings
	//
	list.add(create(true, //

		Descriptor.of(s -> s.getIdentifier().equals("TEST"), () -> new SourceStorageSetting()),

		Descriptor.of(s -> s.getIdentifier().equals("TEST1"), () -> new CredentialsSetting()),

		Descriptor.of(s -> s.getIdentifier().equals(SingletonSettingsId.DATABASE_SETTING.getLabel()),
			() -> new DatabaseSetting())));

	list.add(create(false, () -> new DownloadSetting(), s -> s.getIdentifier().equals("TEST1")));
	list.add(create(false, () -> new SystemSetting(), s -> s.getIdentifier().equals("TEST2")));

	list.add(create(true, () -> new GDCSourcesSetting(), s -> s.getIdentifier().equals("TEST3")));

	checkMethod.setItems(list);

	CheckResponse checkReponse = checkMethod.check(configuration);

	Assert.assertEquals(CheckResult.CHECK_FAILED, checkReponse.getCheckResult());

	Assert.assertEquals(3, checkReponse.getSettings().size());

	List<Setting> sorted = checkReponse.getSettings().//
		stream().//
		sorted((s1, s2) -> s1.getSettingClass().getSimpleName().compareTo(s2.getSettingClass().getSimpleName())).//
		collect(Collectors.toList());

	Assert.assertEquals(CredentialsSetting.class, sorted.get(0).getSettingClass());
	Assert.assertEquals(GDCSourcesSetting.class, sorted.get(1).getSettingClass());
	Assert.assertEquals(SourceStorageSetting.class, sorted.get(2).getSettingClass());

	//
	//
	//

	checkMethod.setCheckMode(CheckMode.REDUNDANT_SETTINGS);

	checkReponse = checkMethod.check(configuration);

	Assert.assertEquals(CheckResult.CHECK_SUCCESSFUL, checkReponse.getCheckResult());
    }

    @Test
    public void missingTest5() {

	DefaultConfiguration configuration = new DefaultConfiguration();
	SelectionUtils.deepClean(configuration);

	SchemeMethod checkMethod = new SchemeMethod();
	checkMethod.setCheckMode(CheckMode.MISSING_SETTINGS);

	List<SchemeItem> list = configuration.getScheme().get().getItems();

	//
	// a tab with multiple kind of settings
	//
	list.add(create(true, //

		Descriptor.of(s -> s.getIdentifier().equals("TEST"), () -> new SourceStorageSetting()),

		Descriptor.of(s -> s.getIdentifier().equals("TEST1"), () -> new CredentialsSetting()),

		Descriptor.of(s -> s.getIdentifier().equals(SingletonSettingsId.DATABASE_SETTING.getLabel()),
			() -> new DatabaseSetting())));

	list.add(create(false, () -> new DownloadSetting(), s -> s.getIdentifier().equals("TEST1")));

	list.add(create(false, () -> new SystemSetting(), s -> s.getIdentifier().equals("TEST2")));

	//
	// a tab with multiple kind of settings
	//
	list.add(create(true, //

		Descriptor.of(s -> s.getIdentifier().equals("TEST"), () -> new DownloadSetting()),

		Descriptor.of(s -> s.getIdentifier().equals("TEST1"), () -> new SystemSetting()),

		Descriptor.of(s -> s.getIdentifier().equals(SingletonSettingsId.DATABASE_SETTING.getLabel()),
			() -> new DatabaseSetting())));

	checkMethod.setItems(list);

	CheckResponse checkReponse = checkMethod.check(configuration);

	Assert.assertEquals(CheckResult.CHECK_FAILED, checkReponse.getCheckResult());

	Assert.assertEquals(4, checkReponse.getSettings().size());

	List<Setting> sorted = checkReponse.getSettings().//
		stream().//
		sorted((s1, s2) -> s1.getSettingClass().getSimpleName().compareTo(s2.getSettingClass().getSimpleName())).//
		collect(Collectors.toList());

	Assert.assertEquals(CredentialsSetting.class, sorted.get(0).getSettingClass());
	Assert.assertEquals(DownloadSetting.class, sorted.get(1).getSettingClass());
	Assert.assertEquals(SourceStorageSetting.class, sorted.get(2).getSettingClass());
	Assert.assertEquals(SystemSetting.class, sorted.get(3).getSettingClass());

	//
	//
	//

	checkMethod.setCheckMode(CheckMode.REDUNDANT_SETTINGS);

	checkReponse = checkMethod.check(configuration);

	Assert.assertEquals(CheckResult.CHECK_SUCCESSFUL, checkReponse.getCheckResult());
    }

    @Test
    public void missingTest6() {

	DefaultConfiguration configuration = new DefaultConfiguration();
	SelectionUtils.deepClean(configuration);

	SchemeMethod checkMethod = new SchemeMethod();
	checkMethod.setCheckMode(CheckMode.MISSING_SETTINGS);

	List<SchemeItem> list = configuration.getScheme().get().getItems();

	//
	// a tab with multiple kind of settings
	//
	list.add(create(true, //
		Descriptor.of(s -> s.getIdentifier().equals("TEST"), () -> new Setting()), Descriptor
			.of(s -> s.getIdentifier().equals(SingletonSettingsId.DATABASE_SETTING.getLabel()), () -> new DatabaseSetting())));

	checkMethod.setItems(list);

	CheckResponse checkReponse = checkMethod.check(configuration);

	Assert.assertEquals(CheckResult.CHECK_FAILED, checkReponse.getCheckResult());

	Assert.assertEquals(1, checkReponse.getSettings().size());

	Assert.assertEquals(Setting.class, checkReponse.getSettings().get(0).getSettingClass());

	//
	//
	//

	checkMethod.setCheckMode(CheckMode.REDUNDANT_SETTINGS);

	checkReponse = checkMethod.check(configuration);

	Assert.assertEquals(CheckResult.CHECK_SUCCESSFUL, checkReponse.getCheckResult());
    }

    @Test
    public void missingTest7() {

	DefaultConfiguration configuration = new DefaultConfiguration();
	SelectionUtils.deepClean(configuration);

	SchemeMethod checkMethod = new SchemeMethod();
	checkMethod.setCheckMode(CheckMode.MISSING_SETTINGS);

	List<SchemeItem> list = configuration.getScheme().get().getItems();

	//
	// a tab with multiple kind of settings
	//
	list.add(create(true, //
		Descriptor.of(s -> s.getIdentifier().equals("TEST1"), () -> new SystemSetting()),
		Descriptor.of(s -> s.getIdentifier().equals("TEST2"), () -> new DatabaseSetting())));

	checkMethod.setItems(list);

	CheckResponse checkReponse = checkMethod.check(configuration);

	Assert.assertEquals(CheckResult.CHECK_FAILED, checkReponse.getCheckResult());

	Assert.assertEquals(2, checkReponse.getSettings().size());

	List<Setting> sorted = checkReponse.getSettings().//
		stream().//
		sorted((s1, s2) -> s1.getSettingClass().getSimpleName().compareTo(s2.getSettingClass().getSimpleName())).//
		collect(Collectors.toList());

	Assert.assertEquals(DatabaseSetting.class, sorted.get(0).getSettingClass());
	Assert.assertEquals(SystemSetting.class, sorted.get(1).getSettingClass());

	//
	//
	//

	checkMethod.setCheckMode(CheckMode.REDUNDANT_SETTINGS);

	checkReponse = checkMethod.check(configuration);

	Assert.assertEquals(CheckResult.CHECK_SUCCESSFUL, checkReponse.getCheckResult());
    }

    @Test
    public void redundantTest0() {

	DefaultConfiguration configuration = new DefaultConfiguration();
	SelectionUtils.deepClean(configuration);

	SchemeMethod checkMethod = new SchemeMethod();
	checkMethod.setCheckMode(CheckMode.REDUNDANT_SETTINGS);

	List<SchemeItem> list = configuration.getScheme().get().getItems();

	checkMethod.setItems(list);

	CheckResponse check = checkMethod.check(configuration);

	CheckResult checkResult = check.getCheckResult();

	Assert.assertEquals(CheckResult.CHECK_SUCCESSFUL, checkResult);

	Assert.assertEquals(0, check.getSettings().size());
    }

    @Test
    public void redundantTest0_1() {

	DefaultConfiguration configuration = new DefaultConfiguration();
	SelectionUtils.deepClean(configuration);

	SchemeMethod checkMethod = new SchemeMethod();
	checkMethod.setCheckMode(CheckMode.REDUNDANT_SETTINGS);

	List<SchemeItem> list = new ArrayList<>();

	checkMethod.setItems(list);

	CheckResponse check = checkMethod.check(configuration);

	CheckResult checkResult = check.getCheckResult();

	Assert.assertEquals(CheckResult.CHECK_FAILED, checkResult);

	Assert.assertEquals(configuration.size(), check.getSettings().size());
    }

    @Test
    public void redundantTest1() {

	DefaultConfiguration configuration = new DefaultConfiguration();
	SelectionUtils.deepClean(configuration);

	SchemeMethod checkMethod = new SchemeMethod();
	checkMethod.setCheckMode(CheckMode.REDUNDANT_SETTINGS);

	List<TabIndex> list = GSTabIndex.getValues();

	// removed system setting from the tab
	list = list.stream().filter(t -> t.getIndex() != GSTabIndex.SYSTEM.getIndex()).collect(Collectors.toList());

	checkMethod.setItems(list);

	CheckResponse checkReponse = checkMethod.check(configuration);

	Assert.assertEquals(CheckResult.CHECK_FAILED, checkReponse.getCheckResult());

	Assert.assertEquals(1, checkReponse.getSettings().size());

	Assert.assertEquals(SystemSetting.class, checkReponse.getSettings().get(0).getSettingClass());

	//
	//
	//

	checkMethod.setCheckMode(CheckMode.MISSING_SETTINGS);

	checkReponse = checkMethod.check(configuration);

	Assert.assertEquals(CheckResult.CHECK_SUCCESSFUL, checkReponse.getCheckResult());
    }

    @Test
    public void redundantTest2() {

	DefaultConfiguration configuration = new DefaultConfiguration();
	SelectionUtils.deepClean(configuration);

	SchemeMethod checkMethod = new SchemeMethod();
	checkMethod.setCheckMode(CheckMode.REDUNDANT_SETTINGS);

	List<TabIndex> list = GSTabIndex.getValues();

	// removed system, database setting and user database setting (which are in the same tab) from the tab
	list = list.stream().filter(t -> t.getIndex() != GSTabIndex.SYSTEM.getIndex()).collect(Collectors.toList());
	list = list.stream().filter(t -> t.getIndex() != GSTabIndex.DATABASE.getIndex()).collect(Collectors.toList());

	checkMethod.setItems(list);

	CheckResponse checkReponse = checkMethod.check(configuration);

	Assert.assertEquals(CheckResult.CHECK_FAILED, checkReponse.getCheckResult());

//	Assert.assertEquals(3, checkReponse.getSettings().size());
	Assert.assertEquals(2, checkReponse.getSettings().size());

	List<Setting> settings = checkReponse.getSettings().stream()
		.sorted((s1, s2) -> s1.getSettingClass().getSimpleName().compareTo(s2.getSettingClass().getSimpleName()))
		.collect(Collectors.toList());

	Assert.assertEquals(DatabaseSetting.class, settings.get(0).getSettingClass());
	Assert.assertEquals(SystemSetting.class, settings.get(1).getSettingClass());
//	Assert.assertEquals(UsersDatabaseSetting.class, settings.get(2).getSettingClass());

	//
	//
	//

	checkMethod.setCheckMode(CheckMode.MISSING_SETTINGS);

	checkReponse = checkMethod.check(configuration);

	Assert.assertEquals(CheckResult.CHECK_SUCCESSFUL, checkReponse.getCheckResult());

    }

    @Test
    public void redundantTest3() {

	DefaultConfiguration configuration = new DefaultConfiguration();
	SelectionUtils.deepClean(configuration);

	SchemeMethod checkMethod = new SchemeMethod();
	checkMethod.setCheckMode(CheckMode.REDUNDANT_SETTINGS);

	List<TabIndex> list = GSTabIndex.getValues();

	list = list.stream().filter(t -> t.getIndex() != GSTabIndex.ABOUT.getIndex()).collect(Collectors.toList());
//	list = list.stream().filter(t -> t.getIndex() != GSTabIndex.SOURCES_INSPECTION.getIndex()).collect(Collectors.toList());

	checkMethod.setItems(list);

	CheckResponse checkReponse = checkMethod.check(configuration);

	Assert.assertEquals(CheckResult.CHECK_SUCCESSFUL, checkReponse.getCheckResult());

	Assert.assertEquals(0, checkReponse.getSettings().size());

	//
	//
	//

	checkMethod.setCheckMode(CheckMode.MISSING_SETTINGS);

	checkReponse = checkMethod.check(configuration);

	Assert.assertEquals(CheckResult.CHECK_SUCCESSFUL, checkReponse.getCheckResult());
    }

    @Test
    public void redundantTest4() {

	DefaultConfiguration configuration = new DefaultConfiguration();
	SelectionUtils.deepClean(configuration);

	SchemeMethod checkMethod = new SchemeMethod();
	checkMethod.setCheckMode(CheckMode.REDUNDANT_SETTINGS);

	List<TabIndex> list = GSTabIndex.getValues();

	// removing harvesting and distribution settings from the tab
//	list = list.stream().filter(t -> t.getIndex() != GSTabIndex.HARVESTING.getIndex()).collect(Collectors.toList());
//	list = list.stream().filter(t -> t.getIndex() != GSTabIndex.DISTRIBUTION.getIndex()).collect(Collectors.toList());

	checkMethod.setItems(list);

	CheckResponse checkReponse = checkMethod.check(configuration);

	Assert.assertEquals(CheckResult.CHECK_FAILED, checkReponse.getCheckResult());

	Assert.assertEquals(4, checkReponse.getSettings().size());

	List<Setting> settings = checkReponse.getSettings().stream()
		.sorted((s1, s2) -> s1.getSettingClass().getSimpleName().compareTo(s2.getSettingClass().getSimpleName()))
		.collect(Collectors.toList());

	Assert.assertEquals(DistributionSetting.class, settings.get(0).getSettingClass());
	Assert.assertEquals(HarvestingSettingImpl.class, settings.get(1).getSettingClass());
	Assert.assertEquals(HarvestingSettingImpl.class, settings.get(2).getSettingClass());
	Assert.assertEquals(HarvestingSettingImpl.class, settings.get(3).getSettingClass());

	//
	//
	//

	checkMethod.setCheckMode(CheckMode.MISSING_SETTINGS);

	checkReponse = checkMethod.check(configuration);

	Assert.assertEquals(CheckResult.CHECK_SUCCESSFUL, checkReponse.getCheckResult());
    }

    @Test
    public void redundantTest5() {

	DefaultConfiguration configuration = new DefaultConfiguration();
	SelectionUtils.deepClean(configuration);

	SchemeMethod checkMethod = new SchemeMethod();
	checkMethod.setCheckMode(CheckMode.REDUNDANT_SETTINGS);

	List<TabIndex> list = GSTabIndex.getValues();

	// removing profilers settings from the tab
//	list = list.stream().filter(t -> t.getIndex() != GSTabIndex.PROFILERS.getIndex()).collect(Collectors.toList());

	checkMethod.setItems(list);

	CheckResponse checkReponse = checkMethod.check(configuration);

	Assert.assertEquals(CheckResult.CHECK_FAILED, checkReponse.getCheckResult());

	Assert.assertEquals(34, checkReponse.getSettings().size());

	count = 0;

	checkReponse.getSettings().forEach(s -> {

	    Class<?> superclass = s.getSettingClass().getSuperclass();
	    if (superclass.getName().equals(ProfilerSetting.class.getName())) {

		count++;

	    } else {

		Class<?> superclass2 = s.getSettingClass().getSuperclass().getSuperclass();
		if (superclass2.getName().equals(ProfilerSetting.class.getName())) {

		    count++;
		}
	    }
	});

	Assert.assertEquals(34, count);

	//
	//
	//

	checkMethod.setCheckMode(CheckMode.MISSING_SETTINGS);

	checkReponse = checkMethod.check(configuration);

	Assert.assertEquals(CheckResult.CHECK_SUCCESSFUL, checkReponse.getCheckResult());
    }

    @Test
    public void redundantTest6() {

	DefaultConfiguration configuration = new DefaultConfiguration();
	SelectionUtils.deepClean(configuration);

	SchemeMethod checkMethod = new SchemeMethod();
	checkMethod.setCheckMode(CheckMode.REDUNDANT_SETTINGS);

	List<TabIndex> list = GSTabIndex.getValues();

	//
	// removing repository setting from the tab
	// this tabs holds 2 kind of settings, SharedCacheDriverSetting and SharedPersistentDriverSetting
	//
	list = list.stream().filter(t -> t.getIndex() != GSTabIndex.REPOSITORY.getIndex()).collect(Collectors.toList());

	checkMethod.setItems(list);

	CheckResponse checkReponse = checkMethod.check(configuration);

	Assert.assertEquals(CheckResult.CHECK_FAILED, checkReponse.getCheckResult());

	Assert.assertEquals(2, checkReponse.getSettings().size());

	List<Setting> settings = checkReponse.getSettings().stream()
		.sorted((s1, s2) -> s1.getSettingClass().getSimpleName().compareTo(s2.getSettingClass().getSimpleName()))
		.collect(Collectors.toList());

	Assert.assertEquals(SharedCacheDriverSetting.class, settings.get(0).getSettingClass());
	Assert.assertEquals(SharedPersistentDriverSetting.class, settings.get(1).getSettingClass());

	//
	//
	//

	checkMethod.setCheckMode(CheckMode.MISSING_SETTINGS);

	checkReponse = checkMethod.check(configuration);

	Assert.assertEquals(CheckResult.CHECK_SUCCESSFUL, checkReponse.getCheckResult());
    }

    @Test
    public void redundantTest7() {

	DefaultConfiguration configuration = new DefaultConfiguration();
	SelectionUtils.deepClean(configuration);

	SchemeMethod checkMethod = new SchemeMethod();
	checkMethod.setCheckMode(CheckMode.REDUNDANT_SETTINGS);

	List<TabIndex> list = GSTabIndex.getValues();

	//
	//
	// the GSTabIndex.REPOSITORY holds 2 kind of settings SharedCacheDriverSetting and SharedPersistenDriverSetting
	// according to the function s -> s.getSettingClass().getSuperclass().equals(DriverSetting.class)).
	//
	//
	// here we replace that scheme item with an item having only SharedPersistentDriverSetting.
	//
	//
	// as result, the configuration will have a redundant setting of class SharedPersistentDriverSetting
	//
	//
	//
	list = list.stream().filter(t -> t.getIndex() != GSTabIndex.REPOSITORY.getIndex()).collect(Collectors.toList());

	list.add(createTabIndex(true, GSTabIndex.REPOSITORY.getIndex(),

		Descriptor.of(s -> s.getSettingClass().equals(SharedCacheDriverSetting.class))));
	//
	//
	//

	checkMethod.setItems(list);

	CheckResponse checkReponse = checkMethod.check(configuration);

	Assert.assertEquals(CheckResult.CHECK_FAILED, checkReponse.getCheckResult());

	Assert.assertEquals(1, checkReponse.getSettings().size());

	Assert.assertEquals(SharedPersistentDriverSetting.class, checkReponse.getSettings().get(0).getSettingClass());

	//
	//
	//

	checkMethod.setCheckMode(CheckMode.MISSING_SETTINGS);

	checkReponse = checkMethod.check(configuration);

	Assert.assertEquals(CheckResult.CHECK_SUCCESSFUL, checkReponse.getCheckResult());
    }

    @Test
    public void redundantTest8() {

	DefaultConfiguration configuration = new DefaultConfiguration();
	SelectionUtils.deepClean(configuration);

	SchemeMethod checkMethod = new SchemeMethod();
	checkMethod.setCheckMode(CheckMode.REDUNDANT_SETTINGS);

	List<TabIndex> list = GSTabIndex.getValues();

	//
	//
	// the GSTabIndex.REPOSITORY holds 2 kind of settings SharedCacheDriverSetting and SharedPersistenDriverSetting
	// according to the function s -> s.getSettingClass().getSuperclass().equals(DriverSetting.class)).
	//
	//
	// here we replace that scheme item with an item having two descriptors, specific for both classes
	// we expect no changes
	//
	//
	//
	list = list.stream().filter(t -> t.getIndex() != GSTabIndex.REPOSITORY.getIndex()).collect(Collectors.toList());

	list.add(createTabIndex(true, GSTabIndex.REPOSITORY.getIndex(),

		Descriptor.of(s -> s.getSettingClass().equals(SharedCacheDriverSetting.class)),

		Descriptor.of(s -> s.getSettingClass().equals(SharedPersistentDriverSetting.class))));
	//
	//
	//

	checkMethod.setItems(list);

	CheckResponse checkReponse = checkMethod.check(configuration);

	Assert.assertEquals(CheckResult.CHECK_SUCCESSFUL, checkReponse.getCheckResult());

	//
	//
	//

	checkMethod.setCheckMode(CheckMode.MISSING_SETTINGS);

	checkReponse = checkMethod.check(configuration);

	Assert.assertEquals(CheckResult.CHECK_SUCCESSFUL, checkReponse.getCheckResult());
    }

    @Test
    public void mixedTest1() {

	DefaultConfiguration configuration = new DefaultConfiguration();
	SelectionUtils.deepClean(configuration);

	SchemeMethod checkMethod = new SchemeMethod();
	checkMethod.setCheckMode(CheckMode.REDUNDANT_SETTINGS);

	List<TabIndex> list = GSTabIndex.getValues();

	//
	// a tab with multiple kind of settings
	//
	list.add(createTabIndex(true, 90, //

		Descriptor.of(s -> s.getIdentifier().equals("TEST"), () -> new SourceStorageSetting()),

		Descriptor.of(s -> s.getIdentifier().equals("TEST1"), () -> new CredentialsSetting()),

		Descriptor.of(s -> s.getIdentifier().equals(SingletonSettingsId.DATABASE_SETTING.getLabel()),
			() -> new DatabaseSetting())));

	//
	// removing repository setting from the tab
	// this tabs holds 2 kind of settings, SharedCacheDriverSetting and SharedPersistentDriverSetting
	//
	list = list.stream().filter(t -> t.getIndex() != GSTabIndex.REPOSITORY.getIndex()).collect(Collectors.toList());

	checkMethod.setItems(list);

	CheckResponse checkReponse = checkMethod.check(configuration);

	Assert.assertEquals(CheckResult.CHECK_FAILED, checkReponse.getCheckResult());

	Assert.assertEquals(2, checkReponse.getSettings().size());

	List<Setting> settings = checkReponse.getSettings().stream()
		.sorted((s1, s2) -> s1.getSettingClass().getSimpleName().compareTo(s2.getSettingClass().getSimpleName()))
		.collect(Collectors.toList());

	Assert.assertEquals(SharedCacheDriverSetting.class, settings.get(0).getSettingClass());
	Assert.assertEquals(SharedPersistentDriverSetting.class, settings.get(1).getSettingClass());

	//
	//
	//

	checkMethod.setCheckMode(CheckMode.MISSING_SETTINGS);

	checkReponse = checkMethod.check(configuration);

	Assert.assertEquals(CheckResult.CHECK_FAILED, checkReponse.getCheckResult());

	List<Setting> sorted = checkReponse.getSettings().//
		stream().//
		sorted((s1, s2) -> s1.getSettingClass().getSimpleName().compareTo(s2.getSettingClass().getSimpleName())).//
		collect(Collectors.toList());

	Assert.assertEquals(CredentialsSetting.class, sorted.get(0).getSettingClass());
	Assert.assertEquals(SourceStorageSetting.class, sorted.get(1).getSettingClass());
    }

    /**
     * @param required
     * @param index
     * @param desc
     * @return
     */
    private static TabIndex createTabIndex(//
	    boolean required, //
	    int index, //
	    Descriptor... desc) {

	return new TabIndex() {

	    @Override
	    public boolean required() {

		return required;
	    }

	    @Override
	    public List<Descriptor> getDescriptors() {

		return Arrays.asList(desc);
	    }

	    @Override
	    public int getIndex() {

		return index;
	    }
	};
    }

    /**
     * @param required
     * @param desc
     * @return
     */
    private static SchemeItem create(//
	    boolean required, //
	    Descriptor... desc) {

	return new SchemeItem() {

	    @Override
	    public boolean required() {

		return required;
	    }

	    @Override
	    public List<Descriptor> getDescriptors() {

		return Arrays.asList(desc);
	    }
	};
    }

    /**
     * @param singleton
     * @param required
     * @param function
     * @param creator
     * @return
     */
    private static SchemeItem create(//
	    boolean required, //
	    Supplier<Setting> creator, //
	    Function<Setting, Boolean> function) {

	return new SchemeItem() {

	    @Override
	    public boolean required() {

		return required;
	    }

	    @Override
	    public List<Descriptor> getDescriptors() {

		return Arrays.asList(Descriptor.of(function, creator));

	    }
	};
    }
}
