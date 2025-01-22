/**
 * 
 */
package eu.essi_lab.cfga.augmenter.worker.setting.test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import eu.essi_lab.augmenter.Augmenter;
import eu.essi_lab.augmenter.AugmentersSetting;
import eu.essi_lab.augmenter.worker.AugmenterWorker;
import eu.essi_lab.augmenter.worker.AugmenterWorkerSettingImpl;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.DefaultConfiguration;
import eu.essi_lab.cfga.gs.setting.augmenter.AugmenterSetting;
import eu.essi_lab.cfga.gs.setting.augmenter.worker.AugmenterWorkerSetting;
import eu.essi_lab.cfga.gs.setting.augmenter.worker.AugmenterWorkerSettingLoader;
import eu.essi_lab.cfga.setting.Setting;
import eu.essi_lab.cfga.setting.SettingUtils;
import eu.essi_lab.cfga.setting.scheduling.SchedulerWorkerSetting.SchedulingGroup;
import eu.essi_lab.cfga.setting.scheduling.Scheduling;
import eu.essi_lab.lib.utils.StreamUtils;
import eu.essi_lab.model.GSSource;

/**
 * @author Fabrizio
 */
public class AugmenterWorkerSettingTest {

    @Before
    public void before() throws Exception {

	DefaultConfiguration configuration = new DefaultConfiguration(UUID.randomUUID().toString() + ".json");
	configuration.clean();

	ConfigurationWrapper.setConfiguration(configuration);
    }

    @Test
    public void loadAndSelectionTest() throws InterruptedException {

	AugmenterWorkerSetting setting = AugmenterWorkerSettingLoader.load();

	List<String> values = setting.getOption("sourcesOption", String.class).get().//
		getValues();

	Assert.assertTrue(values.isEmpty());

	// initially the sources option has no values (and no selected values...)
	List<String> selectedValues = setting.getOption("sourcesOption", String.class).get().//
		getSelectedValues();

	Assert.assertTrue(selectedValues.isEmpty());

	// no selection implies to augment all the sources
	List<GSSource> selectedSources = setting.getSelectedSources();
	Assert.assertEquals(3, selectedSources.size());

	List<String> selectedSourcesIds = setting.getSelectedSourcesIds();
	Assert.assertEquals(3, selectedSourcesIds.size());

	//
	//
	//

	//
	// loads the sources values
	//

	SettingUtils.loadValues(setting.getOption("sourcesOption", String.class).get(), Optional.empty());

	//
	//
	//

	values = setting.getOption("sourcesOption", String.class).get().//
		getValues().//
		stream().//
		sorted(String::compareTo).//
		collect(Collectors.toList());

	List<String> configSourcesLabels = ConfigurationWrapper.getHarvestedAndMixedSources().//
		stream().//
		map(s -> s.getLabel()).//
		sorted(String::compareTo).//
		collect(Collectors.toList());

	Assert.assertEquals(values, configSourcesLabels);

	//
	//
	//

	String selectedLabel = configSourcesLabels.get(0);

	setting.setSelectedSources(Arrays.asList(selectedLabel));

	selectedValues = setting.getOption("sourcesOption", String.class).get().//
		getSelectedValues();

	Assert.assertEquals(1, selectedValues.size());
	Assert.assertEquals(selectedLabel, selectedValues.get(0));

	//
	//
	//

	selectedSources = setting.getSelectedSources();
	Assert.assertEquals(1, selectedSources.size());

	Assert.assertEquals(selectedLabel, selectedSources.get(0).getLabel());

	selectedSourcesIds = setting.getSelectedSourcesIds();
	Assert.assertEquals(1, selectedSourcesIds.size());

	Assert.assertEquals(selectedSourcesIds.get(0), selectedSources.get(0).getUniqueIdentifier());
    }

    @Test
    public void test() throws Exception {

	AugmenterWorkerSetting setting = AugmenterWorkerSettingLoader.load();

	List<Setting> settings = setting.getSettings();
	//
	// getSettings method looks for object having objectType = "setting"
	// while Scheduling has "scheduling", so it must be excluded
	//
	boolean allMatch = settings.//
		stream().filter(s -> !s.getObjectType().equals(Scheduling.SCHEDULING_OBJECT_TYPE)).//
		allMatch(s -> AugmentersSetting.class.isAssignableFrom(s.getSettingClass()));
	Assert.assertTrue(allMatch);

	test1(setting);
	test1(new AugmenterWorkerSettingImpl(setting.getObject()));
	test1(new AugmenterWorkerSettingImpl(setting.getObject().toString()));

	AugmenterWorker worker = setting.createConfigurable();
	AugmenterWorkerSetting setting2 = worker.getSetting();

	//
	// ---
	//
	Assert.assertEquals(0, setting2.getMaxAge());
	Assert.assertEquals(0, setting2.getMaxRecords());
	Assert.assertFalse(setting2.isLessRecentOrderingSet());

	//
	// ---
	//

	setting.setMaxAge(5);
	setting.setIsLessRecentOrderingSet(true);
	setting.setMaxRecords(11);
	setting.setAugmentationJobName("Name");

	worker = setting.createConfigurable();
	setting2 = worker.getSetting();

	Assert.assertEquals(setting.getMaxAge(), setting2.getMaxAge());
	Assert.assertEquals(setting.isLessRecentOrderingSet(), setting2.isLessRecentOrderingSet());
	Assert.assertEquals(setting.getMaxRecords(), setting2.getMaxRecords());
	Assert.assertEquals("Name", setting2.getAugmentationJobName());

	setting.setViewIdentifier("viewId");
	Assert.assertEquals("viewId", setting.getViewIdentifier().get());
    }

    @Test
    public void downCastTest() {

	AugmenterWorkerSetting dbSetting = AugmenterWorkerSettingLoader.load();

	Setting setting = new Setting(dbSetting.getObject());

	Setting downCasted = SettingUtils.downCast(setting);

	Assert.assertEquals(dbSetting.getClass(), downCasted.getClass());

	// ----

	setting = new Setting(dbSetting.getObject().toString());

	downCasted = SettingUtils.downCast(setting);

	Assert.assertEquals(dbSetting.getClass(), downCasted.getClass());

	// ----

	downCasted = SettingUtils.downCast(dbSetting);

	Assert.assertEquals(dbSetting.getClass(), downCasted.getClass());
    }

    /**
     * @param setting
     * @throws ClassNotFoundException
     */
    private void test1(AugmenterWorkerSetting setting) throws ClassNotFoundException {

	Assert.assertEquals("AugmenterWorker", setting.getConfigurableType());
	Assert.assertFalse(setting.getDescription().isPresent());

	Assert.assertEquals(SchedulingGroup.AUGMENTING, setting.getGroup());

	Assert.assertNotNull(setting.getIdentifier());

	Assert.assertEquals(0, setting.getMaxAge());
	Assert.assertEquals(0, setting.getMaxRecords());
	Assert.assertFalse(setting.isLessRecentOrderingSet());

	Assert.assertFalse(setting.getViewIdentifier().isPresent());

	Assert.assertEquals("Default augmentation job", setting.getAugmentationJobName());

	//
	// with an empty selection, all the available (non distributed) sources are augmented
	//
	Assert.assertEquals(3, setting.getSelectedSourcesIds().size());
	Assert.assertEquals(3, setting.getSelectedSources().size());

	//
	// checking if all the available augmenters are provided by the AugmenterWorkerSetting
	//

	@SuppressWarnings("rawtypes")
	ServiceLoader<Augmenter> loader = ServiceLoader.load(Augmenter.class);

	List<AugmenterSetting> loadedSettings = StreamUtils.iteratorToStream(loader.iterator()).//
		map(a -> a.getSetting()).//
		sorted((a1, a2) -> a1.getName().compareTo(a2.getName())).//
		collect(Collectors.toList());

	List<AugmenterSetting> settings = setting.getAugmentersSetting().//
		getSettings(AugmenterSetting.class, false).//
		stream().//
		sorted((a1, a2) -> a1.getName().compareTo(a2.getName())).//
		collect(Collectors.toList());

	Assert.assertEquals(loadedSettings.size(), settings.size());

	//
	//
	//

	for (int i = 0; i < loadedSettings.size(); i++) {

	    loadedSettings.get(i).setIdentifier("id"); // id is randomly created, must be set equals
	    //
	    //
	    //

	    settings.get(i).setIdentifier("id");

	    Assert.assertEquals(loadedSettings.get(i), settings.get(i));
	}
    }
}
