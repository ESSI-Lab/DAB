/**
 *
 */
package eu.essi_lab.cfga.test;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import eu.essi_lab.cfga.gs.*;
import eu.essi_lab.cfga.gs.setting.*;
import eu.essi_lab.cfga.gs.setting.dc_connector.*;
import eu.essi_lab.cfga.gs.setting.oauth.*;
import eu.essi_lab.cfga.gs.setting.ontology.*;
import eu.essi_lab.cfga.gs.setting.ratelimiter.*;
import eu.essi_lab.cfga.gs.setting.sessioncoordinator.*;
import org.junit.*;

import eu.essi_lab.cfga.SelectionUtils;
import eu.essi_lab.cfga.check.CheckResponse;
import eu.essi_lab.cfga.check.CheckResponse.CheckResult;
import eu.essi_lab.cfga.check.scheme.SchemeItem;
import eu.essi_lab.cfga.check.scheme.SchemeMethod;
import eu.essi_lab.cfga.check.scheme.SchemeItem.Descriptor;
import eu.essi_lab.cfga.check.scheme.SchemeMethod.CheckMode;
import eu.essi_lab.cfga.gs.DefaultConfiguration.SingletonSettingsId;
import eu.essi_lab.cfga.gs.setting.database.DatabaseSetting;
import eu.essi_lab.cfga.gs.setting.database.SourceStorageSetting;
import eu.essi_lab.cfga.gs.setting.distribution.DistributionSetting;
import eu.essi_lab.cfga.gs.setting.driver.SharedCacheDriverSetting;
import eu.essi_lab.cfga.gs.setting.driver.SharedPersistentDriverSetting;
import eu.essi_lab.cfga.gui.TabIndex;
import eu.essi_lab.cfga.setting.Setting;
import eu.essi_lab.harvester.worker.HarvestingSettingImpl;

/**
 * @author Fabrizio
 */
@Ignore
public class SchemeCheckMethodTest {

    private int count;

    @Test
    public void missingTest0() {

	DefaultConfiguration configuration = new DefaultConfiguration();
	SelectionUtils.deepClean(configuration);

	SchemeMethod checkMethod = new SchemeMethod();
	checkMethod.setCheckMode(CheckMode.MISSING_SETTINGS);

	checkMethod.setScheme(new DefaultConfigurationScheme());

	//
	//
	//

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

	List<SchemeItem> list = new DefaultConfigurationScheme().getItems();

	list.add(createItem(s -> s.getIdentifier().equals("TEST"), DatabaseSetting::new));

	checkMethod.setItems(list);

	//
	//
	//

	CheckResponse checkReponse = checkMethod.check(configuration);

	Assert.assertEquals(CheckResult.CHECK_FAILED, checkReponse.getCheckResult());

	Assert.assertEquals(1, checkReponse.getSettings().size());

	Assert.assertEquals(DatabaseSetting.class, checkReponse.getSettings().getFirst().getSettingClass());

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

	List<SchemeItem> list = new DefaultConfigurationScheme().getItems();

	list.add(createItem(s -> s.getIdentifier().equals("TEST"), DatabaseSetting::new));
	list.add(createItem(s -> s.getIdentifier().equals("TEST1"), SystemSetting::new));
	list.add(createItem(s -> s.getIdentifier().equals("TEST2"), DownloadSetting::new));

	checkMethod.setItems(list);

	CheckResponse checkReponse = checkMethod.check(configuration);

	Assert.assertEquals(CheckResult.CHECK_FAILED, checkReponse.getCheckResult());

	Assert.assertEquals(3, checkReponse.getSettings().size());

	List<Setting> sorted = checkReponse.getSettings().//
		stream().//
		sorted(Comparator.comparing(s -> s.getSettingClass().getSimpleName())).//
		toList();

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

	List<SchemeItem> list = new DefaultConfigurationScheme().getItems();

	list.add(createItem(s -> s.getIdentifier().equals("TEST1"), createSetting("TEST1")));
	list.add(createItem(s -> s.getIdentifier().equals("TEST2"), createSetting("TEST2")));
	list.add(createItem(s -> s.getIdentifier().equals("TEST3"), createSetting("TEST3")));
	list.add(createItem( s -> s.getIdentifier().equals("TEST4"),  createSetting("TEST4")));

	checkMethod.setItems(list);

	CheckResponse checkReponse = checkMethod.check(configuration);

	Assert.assertEquals(CheckResult.CHECK_FAILED, checkReponse.getCheckResult());

	Assert.assertEquals(4, checkReponse.getSettings().size());

	List<Setting> sorted = checkReponse.getSettings().//
		stream().//
		sorted(Comparator.comparing(Setting::getIdentifier)).//
		toList();

	Assert.assertEquals("TEST1", sorted.get(0).getIdentifier());
	Assert.assertEquals("TEST2", sorted.get(1).getIdentifier());
	Assert.assertEquals("TEST3", sorted.get(2).getIdentifier());
	Assert.assertEquals("TEST4", sorted.get(3).getIdentifier());

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

	List<SchemeItem> list = new DefaultConfigurationScheme().getItems();

	list.add(createItem( //
		Descriptor.of(s -> s.getIdentifier().equals("TEST1"), createSetting("TEST1")),
		Descriptor.of(s -> s.getIdentifier().equals("TEST2"), createSetting("TEST2")),
		Descriptor.of(s -> s.getIdentifier().equals("TEST3"), createSetting("TEST3"))));

	checkMethod.setItems(list);

	//
	//
	//

	CheckResponse checkReponse = checkMethod.check(configuration);

	Assert.assertEquals(CheckResult.CHECK_FAILED, checkReponse.getCheckResult());

	Assert.assertEquals(3, checkReponse.getSettings().size());

	List<Setting> sorted = checkReponse.getSettings().//
		stream().//
		sorted(Comparator.comparing(Setting::getIdentifier)).//
		toList();

	Assert.assertEquals("TEST1", sorted.get(0).getIdentifier());
	Assert.assertEquals("TEST2", sorted.get(1).getIdentifier());
	Assert.assertEquals("TEST3", sorted.get(2).getIdentifier());

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

	List<SchemeItem> list = new DefaultConfigurationScheme().getItems();

	list.add(createItem( //

		Descriptor.of(s -> s.getIdentifier().equals("TEST0"), createSetting("TEST0")),

		Descriptor.of(s -> s.getIdentifier().equals("TEST1"), createSetting("TEST1")),

		Descriptor.of(s -> s.getIdentifier().equals(SingletonSettingsId.DATABASE_SETTING.getLabel()), DatabaseSetting::new)));

	list.add(createItem(s -> s.getIdentifier().equals("TEST2"), createSetting("TEST2")));

	list.add(createItem(s -> s.getIdentifier().equals("TEST3"), createSetting("TEST3")));

	list.add(createItem( //

		Descriptor.of(s -> s.getIdentifier().equals("TEST4"), createSetting("TEST4")),

		Descriptor.of(s -> s.getIdentifier().equals("TEST5"), createSetting("TEST5")),

		Descriptor.of(s -> s.getIdentifier().equals(SingletonSettingsId.DATABASE_SETTING.getLabel()), DatabaseSetting::new)));

	checkMethod.setItems(list);

	//
	//
	//

	CheckResponse checkReponse = checkMethod.check(configuration);

	Assert.assertEquals(CheckResult.CHECK_FAILED, checkReponse.getCheckResult());

	Assert.assertEquals(6, checkReponse.getSettings().size());

	List<Setting> sorted = checkReponse.getSettings().//
		stream().//
		sorted(Comparator.comparing(Setting::getIdentifier)).//
		toList();

	Assert.assertEquals("TEST0", sorted.get(0).getIdentifier());
	Assert.assertEquals("TEST1", sorted.get(1).getIdentifier());
	Assert.assertEquals("TEST2", sorted.get(2).getIdentifier());
	Assert.assertEquals("TEST3", sorted.get(3).getIdentifier());
	Assert.assertEquals("TEST4", sorted.get(4).getIdentifier());
	Assert.assertEquals("TEST5", sorted.get(5).getIdentifier());

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

	List<SchemeItem> list = new DefaultConfigurationScheme().getItems();

	list.add(createItem( //
		Descriptor.of(s -> s.getIdentifier().equals("TEST"), createSetting("TEST")),
		Descriptor.of(s -> s.getIdentifier().equals(SingletonSettingsId.DATABASE_SETTING.getLabel()), DatabaseSetting::new)));

	checkMethod.setItems(list);

	//
	//
	//

	CheckResponse checkReponse = checkMethod.check(configuration);

	Assert.assertEquals(CheckResult.CHECK_FAILED, checkReponse.getCheckResult());

	Assert.assertEquals(1, checkReponse.getSettings().size());

	Assert.assertEquals("TEST", checkReponse.getSettings().getFirst().getIdentifier());

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

	List<SchemeItem> list = new DefaultConfigurationScheme().getItems();

	list.add(createItem( //
		Descriptor.of(s -> s.getIdentifier().equals("TEST1"), () -> {
		    Setting s = new SystemSetting();
		    s.setIdentifier("TEST1");
		    return s;
		}),

		Descriptor.of(s -> s.getIdentifier().equals("TEST2"), () -> {
		    DatabaseSetting s = new DatabaseSetting();
		    s.setIdentifier("TEST2");
		    return s;
		})));

	checkMethod.setItems(list);

	//
	//
	//

	CheckResponse checkReponse = checkMethod.check(configuration);

	Assert.assertEquals(CheckResult.CHECK_FAILED, checkReponse.getCheckResult());

	Assert.assertEquals(2, checkReponse.getSettings().size());

	//
	//
	//

	List<Setting> sorted = checkReponse.getSettings().//
		stream().//
		sorted(Comparator.comparing(s -> s.getSettingClass().getSimpleName())).//
		toList();

	Assert.assertEquals(DatabaseSetting.class, sorted.get(0).getSettingClass());
	Assert.assertEquals("TEST2", sorted.get(0).getIdentifier());

	Assert.assertEquals(SystemSetting.class, sorted.get(1).getSettingClass());
	Assert.assertEquals("TEST1", sorted.get(1).getIdentifier());

	//
	//
	//

	checkMethod.setCheckMode(CheckMode.REDUNDANT_SETTINGS);

	checkReponse = checkMethod.check(configuration);

	Assert.assertEquals(CheckResult.CHECK_SUCCESSFUL, checkReponse.getCheckResult());
    }

    @Test
    public void missingTest8() {

	DefaultConfiguration configuration = new DefaultConfiguration();

	// clear the config
	configuration.clear();

	SchemeMethod checkMethod = new SchemeMethod();
	checkMethod.setCheckMode(CheckMode.MISSING_SETTINGS);

	DefaultConfigurationScheme scheme = new DefaultConfigurationScheme();

	checkMethod.setScheme(scheme);

	CheckResponse checkReponse = checkMethod.check(configuration);

	Assert.assertEquals(CheckResult.CHECK_FAILED, checkReponse.getCheckResult());

	// only singleton settings are created
	Assert.assertEquals(scheme.getDescriptors().stream().filter(Descriptor::isSingleton).count(), checkReponse.getSettings().size());

	Assert.assertEquals(SourcePrioritySetting.class, checkReponse.getSettings().get(0).getSettingClass());
	Assert.assertEquals(SourceStorageSetting.class, checkReponse.getSettings().get(1).getSettingClass());

	Assert.assertEquals(GDCSourcesSetting.class, checkReponse.getSettings().get(2).getSettingClass());
	Assert.assertEquals(KeycloakProviderSetting.class, checkReponse.getSettings().get(3).getSettingClass());

	Assert.assertEquals(CredentialsSetting.class, checkReponse.getSettings().get(4).getSettingClass());
	Assert.assertEquals(DataCacheConnectorSettingLoader.load().getClass(), checkReponse.getSettings().get(5).getSettingClass());

	Assert.assertEquals(DefaultSemanticSearchSetting.class, checkReponse.getSettings().get(6).getSettingClass());

	Assert.assertEquals(SystemSetting.class, checkReponse.getSettings().get(7).getSettingClass());
	Assert.assertEquals(DatabaseSetting.class, checkReponse.getSettings().get(8).getSettingClass());

	Assert.assertEquals(SchedulerViewSetting.class, checkReponse.getSettings().get(9).getSettingClass());

	Assert.assertEquals(SharedCacheDriverSetting.class, checkReponse.getSettings().get(10).getSettingClass());
	Assert.assertEquals(SharedPersistentDriverSetting.class, checkReponse.getSettings().get(11).getSettingClass());

	Assert.assertEquals(DownloadSetting.class, checkReponse.getSettings().get(12).getSettingClass());
	Assert.assertEquals(RateLimiterSetting.class, checkReponse.getSettings().get(13).getSettingClass());
	Assert.assertEquals(SessionCoordinatorSetting.class, checkReponse.getSettings().get(14).getSettingClass());

	//
	//
	//

	checkMethod.setCheckMode(CheckMode.REDUNDANT_SETTINGS);

	checkReponse = checkMethod.check(configuration);

	Assert.assertEquals(CheckResult.CHECK_SUCCESSFUL, checkReponse.getCheckResult());
    }

    @Test
    @Ignore
    public void redundantTest0() {

	DefaultConfiguration configuration = new DefaultConfiguration();
	SelectionUtils.deepClean(configuration);

	SchemeMethod checkMethod = new SchemeMethod();
	checkMethod.setCheckMode(CheckMode.REDUNDANT_SETTINGS);

	List<SchemeItem> list = new DefaultConfigurationScheme().getItems();

	checkMethod.setItems(list);

	CheckResponse check = checkMethod.check(configuration);

	CheckResult checkResult = check.getCheckResult();

	Assert.assertEquals(CheckResult.CHECK_SUCCESSFUL, checkResult);

	Assert.assertEquals(0, check.getSettings().size());
    }

    @Test
    @Ignore
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
    @Ignore
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

	Assert.assertEquals(SystemSetting.class, checkReponse.getSettings().getFirst().getSettingClass());

	//
	//
	//

	checkMethod.setCheckMode(CheckMode.MISSING_SETTINGS);

	checkReponse = checkMethod.check(configuration);

	Assert.assertEquals(CheckResult.CHECK_SUCCESSFUL, checkReponse.getCheckResult());
    }

    @Test
    @Ignore
    public void redundantTest2() {

	DefaultConfiguration configuration = new DefaultConfiguration();
	SelectionUtils.deepClean(configuration);

	SchemeMethod checkMethod = new SchemeMethod();
	checkMethod.setCheckMode(CheckMode.REDUNDANT_SETTINGS);

	List<TabIndex> list = GSTabIndex.getValues();

	// removed system, database setting and user database setting (which are in the same tab) from the tab
	list = list.stream().filter(t -> t.getIndex() != GSTabIndex.SYSTEM.getIndex()).collect(Collectors.toList());
	//	list = list.stream().filter(t -> t.getIndex() != GSTabIndex.DATABASE.getIndex()).collect(Collectors.toList());

	checkMethod.setItems(list);

	CheckResponse checkReponse = checkMethod.check(configuration);

	Assert.assertEquals(CheckResult.CHECK_FAILED, checkReponse.getCheckResult());

	//	Assert.assertEquals(3, checkReponse.getSettings().size());
	Assert.assertEquals(2, checkReponse.getSettings().size());

	List<Setting> settings = checkReponse.getSettings().stream().sorted(Comparator.comparing(s -> s.getSettingClass().getSimpleName()))
		.toList();

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
    @Ignore
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
    @Ignore
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

	List<Setting> settings = checkReponse.getSettings().stream().sorted(Comparator.comparing(s -> s.getSettingClass().getSimpleName()))
		.toList();

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
    @Ignore
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
    @Ignore
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
	//	list = list.stream().filter(t -> t.getIndex() != GSTabIndex.REPOSITORY.getIndex()).collect(Collectors.toList());

	checkMethod.setItems(list);

	CheckResponse checkReponse = checkMethod.check(configuration);

	Assert.assertEquals(CheckResult.CHECK_FAILED, checkReponse.getCheckResult());

	Assert.assertEquals(2, checkReponse.getSettings().size());

	List<Setting> settings = checkReponse.getSettings().stream().sorted(Comparator.comparing(s -> s.getSettingClass().getSimpleName()))
		.toList();

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
    @Ignore
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
	//	list = list.stream().filter(t -> t.getIndex() != GSTabIndex.REPOSITORY.getIndex()).collect(Collectors.toList());
	//
	//	list.add(createTabIndex(true, GSTabIndex.REPOSITORY.getIndex(),
	//
	//		Descriptor.of(s -> s.getSettingClass().equals(SharedCacheDriverSetting.class))));
	//
	//
	//

	checkMethod.setItems(list);

	CheckResponse checkReponse = checkMethod.check(configuration);

	Assert.assertEquals(CheckResult.CHECK_FAILED, checkReponse.getCheckResult());

	Assert.assertEquals(1, checkReponse.getSettings().size());

	Assert.assertEquals(SharedPersistentDriverSetting.class, checkReponse.getSettings().getFirst().getSettingClass());

	//
	//
	//

	checkMethod.setCheckMode(CheckMode.MISSING_SETTINGS);

	checkReponse = checkMethod.check(configuration);

	Assert.assertEquals(CheckResult.CHECK_SUCCESSFUL, checkReponse.getCheckResult());
    }

    @Test
    @Ignore
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
	//	list = list.stream().filter(t -> t.getIndex() != GSTabIndex.REPOSITORY.getIndex()).collect(Collectors.toList());
	//
	//	list.add(createTabIndex(true, GSTabIndex.REPOSITORY.getIndex(),
	//
	//		Descriptor.of(s -> s.getSettingClass().equals(SharedCacheDriverSetting.class)),
	//
	//		Descriptor.of(s -> s.getSettingClass().equals(SharedPersistentDriverSetting.class))));
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
    @Ignore
    public void mixedTest1() {

	DefaultConfiguration configuration = new DefaultConfiguration();
	SelectionUtils.deepClean(configuration);

	SchemeMethod checkMethod = new SchemeMethod();
	checkMethod.setCheckMode(CheckMode.REDUNDANT_SETTINGS);

	List<TabIndex> list = GSTabIndex.getValues();

	//
	// a tab with multiple kind of settings
	//
	list.add(createTabIndex(90, //

		Descriptor.of(s -> s.getIdentifier().equals("TEST"), SourceStorageSetting::new),

		Descriptor.of(s -> s.getIdentifier().equals("TEST1"), CredentialsSetting::new),

		Descriptor.of(s -> s.getIdentifier().equals(SingletonSettingsId.DATABASE_SETTING.getLabel()), DatabaseSetting::new)));

	//
	// removing repository setting from the tab
	// this tabs holds 2 kind of settings, SharedCacheDriverSetting and SharedPersistentDriverSetting
	//
	//	list = list.stream().filter(t -> t.getIndex() != GSTabIndex.REPOSITORY.getIndex()).collect(Collectors.toList());

	checkMethod.setItems(list);

	CheckResponse checkReponse = checkMethod.check(configuration);

	Assert.assertEquals(CheckResult.CHECK_FAILED, checkReponse.getCheckResult());

	Assert.assertEquals(2, checkReponse.getSettings().size());

	List<Setting> settings = checkReponse.getSettings().stream().sorted(Comparator.comparing(s -> s.getSettingClass().getSimpleName()))
		.toList();

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
		sorted(Comparator.comparing(s -> s.getSettingClass().getSimpleName())).//
		toList();

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
	    int index, //
	    Descriptor... desc) {

	return new TabIndex() {

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
    private SchemeItem createItem(Descriptor... desc) {

	return () -> List.of(desc);
    }

    /**
     * @param singleton
     * @param required
     * @param function
     * @param creator
     * @return
     */
    private SchemeItem createItem(//
	    Function<Setting, Boolean> function,
	    Supplier<Setting> creator) {

	return () -> List.of(Descriptor.of(function, creator));
    }

    /**
     * @param id
     * @return
     */
    private Supplier<Setting> createSetting(String id) {

	Setting setting = new Setting();
	setting.setIdentifier(id);
	return () -> setting;
    }
}
