package eu.essi_lab.cfga.gs.setting.harvester.worker.test;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.access.augmenter.RemoveAccessAugmenter;
import eu.essi_lab.accessor.oaipmh.OAIPMHAccessor;
import eu.essi_lab.adk.AccessorFactory;
import eu.essi_lab.adk.AccessorFactory.LookupPolicy;
import eu.essi_lab.adk.harvest.IHarvestedAccessor;
import eu.essi_lab.augmenter.Augmenter;
import eu.essi_lab.augmenter.metadata.MetadataAugmenter;
import eu.essi_lab.cfga.Configurable;
import eu.essi_lab.cfga.Configuration;
import eu.essi_lab.cfga.Selectable.SelectionMode;
import eu.essi_lab.cfga.SelectionUtils;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.DefaultConfiguration.MainSettingsIdentifier;
import eu.essi_lab.cfga.gs.setting.SystemSetting;
import eu.essi_lab.cfga.gs.setting.accessor.AccessorSetting;
import eu.essi_lab.cfga.gs.setting.augmenter.AugmenterSetting;
import eu.essi_lab.cfga.gs.setting.database.DatabaseSetting;
import eu.essi_lab.cfga.gs.setting.harvesting.HarvestingSetting;
import eu.essi_lab.cfga.gs.setting.harvesting.HarvestingSettingLoader;
import eu.essi_lab.cfga.gs.task.CustomTaskSetting;
import eu.essi_lab.cfga.setting.Setting;
import eu.essi_lab.cfga.source.FileSource;
import eu.essi_lab.harvester.worker.HarvestingSettingHelper;
import eu.essi_lab.lib.utils.StreamUtils;

/**
 * @author Fabrizio
 */
public class HarvestingSettingTest {

    /**
     * @throws Exception
     */
    @Test
    public void selectAccessorTest() throws Exception {

	HarvestingSetting setting = HarvestingSettingLoader.load();

	AccessorSetting selectedAccessorSetting = setting.getSelectedAccessorSetting();
	Assert.assertNotNull(selectedAccessorSetting);

	List<Setting> accessorsSettings = setting.getAccessorsSetting().//
		getSettings().//
		stream().//
		sorted((s1, s2) -> s1.getName().compareTo(s2.getName())).//
		collect(Collectors.toList());

	//
	// per default, the selected accessor is the first according to the
	// name sorting
	//
	Assert.assertEquals(selectedAccessorSetting, accessorsSettings.get(0));

	//
	// selects one setting
	//
	int count = accessorsSettings.size();

	setting.getAccessorsSetting().//
		select(s -> s.getName().equals("OAIPMH Accessor"));

	setting.getAccessorsSetting().clean();

	long newCount = setting.getAccessorsSetting().//
		getSettings().stream().count();

	Assert.assertTrue(newCount < count);
	Assert.assertEquals(1, newCount);

	HarvestingSettingHelper wrapper = new HarvestingSettingHelper(setting);

	@SuppressWarnings("rawtypes")
	IHarvestedAccessor accessor = wrapper.getSelectedAccessor();

	Assert.assertEquals(OAIPMHAccessor.class, accessor.getClass());
    }

    @Test
    public void selectAccessorTest2() throws Exception {

	HarvestingSetting oaiSetting = HarvestingSettingLoader.load();

	oaiSetting.selectAccessorSetting(s -> s.getAccessorType().equals("SOS"));

	SelectionUtils.deepClean(oaiSetting);

	AccessorSetting accessorSetting = oaiSetting.getSelectedAccessorSetting();

	Assert.assertEquals("SOS", accessorSetting.getAccessorType());
    }
    
    @Test
    public void selectAccessorTest3() throws Exception {

	HarvestingSetting oaiSetting = HarvestingSettingLoader.load();

	oaiSetting.selectAccessorSetting(s -> s.getAccessorType().equals("OAIPMH"));

	SelectionUtils.deepClean(oaiSetting);

	AccessorSetting accessorSetting = oaiSetting.getSelectedAccessorSetting();

	Assert.assertEquals("OAIPMH", accessorSetting.getAccessorType());
    }
    
    @Test
    public void selectAccessorTest4() throws Exception {

	HarvestingSetting oaiSetting = HarvestingSettingLoader.load();

	oaiSetting.selectAccessorSetting(s -> s.getAccessorType().equals("WCS"));

	SelectionUtils.deepClean(oaiSetting);

	AccessorSetting accessorSetting = oaiSetting.getSelectedAccessorSetting();

	Assert.assertEquals("WCS", accessorSetting.getAccessorType());
    }
    
    
    @Test
    public void selectAccessorTest5() throws Exception {

	HarvestingSetting oaiSetting = HarvestingSettingLoader.load();

	oaiSetting.selectAccessorSetting(s -> s.getAccessorType().equals("NOTFOUND"));

	SelectionUtils.deepClean(oaiSetting);

	AccessorSetting accessorSetting = oaiSetting.getSelectedAccessorSetting();

	Assert.assertNull(accessorSetting);
    }

    @Test
    public void selectNoneAugmentersTest() {

	HarvestingSetting setting = HarvestingSettingLoader.load();

	Assert.assertEquals(0, setting.getSelectedAugmenterSettings().size());

	setting.getAugmentersSetting().clean();

	@SuppressWarnings("rawtypes")
	List<Augmenter> augmenters = new HarvestingSettingHelper(setting).getSelectedAugmenters();
	Assert.assertEquals(0, augmenters.size());
    }

    @Test
    public void selectTwoAugmentersTest() {

	HarvestingSetting setting = HarvestingSettingLoader.load();

	String remType = new RemoveAccessAugmenter().getType();
	String mdType = new MetadataAugmenter().getType();

	int count = setting.getAugmentersSetting().getSettings().size();

	setting.getAugmentersSetting().select(s -> //
	s.getConfigurableType().equals(remType) || s.getConfigurableType().equals(mdType));

	setting.getAugmentersSetting().clean();

	int newCount = setting.getAugmentersSetting().getSettings().size();

	Assert.assertTrue(newCount < count);
	Assert.assertEquals(2, newCount);

	@SuppressWarnings("rawtypes")
	List<Augmenter> augmenters = new HarvestingSettingHelper(setting).getSelectedAugmenters();
	augmenters.sort((a1, a2) -> a1.getType().compareTo(a2.getType()));

	Assert.assertEquals(2, augmenters.size());

	Assert.assertEquals(mdType, augmenters.get(0).getType());
	Assert.assertEquals(remType, augmenters.get(1).getType());
    }

    @Test
    public void selectAllAugmentersTest() {

	long count = StreamUtils.iteratorToStream(ServiceLoader.load(Augmenter.class).iterator()).count();

	HarvestingSetting setting = HarvestingSettingLoader.load();

	setting.getAugmentersSetting().getSettings().forEach(s -> s.setSelected(true));

	@SuppressWarnings("rawtypes")
	List<Augmenter> augmenters = new HarvestingSettingHelper(setting).getSelectedAugmenters();

	Assert.assertEquals(count, augmenters.size());

	//
	//
	//

	setting.getAugmentersSetting().getSettings().forEach(s -> s.setSelected(false));

	setting.getAugmentersSetting().clean();

	augmenters = new HarvestingSettingHelper(setting).getSelectedAugmenters();

	Assert.assertEquals(0, augmenters.size());

    }

    /**
     * @throws Exception
     */
    @Test
    public void ConfigurationUtilsRemoveAllUnselectedTest() throws Exception {

	FileSource filesSource = new FileSource(new File(//
		System.getProperty("java.io.tmpdir") + File.separator + UUID.randomUUID().toString() + ".json"));

	Configuration configuration = new Configuration(filesSource);

	HarvestingSetting setting = HarvestingSettingLoader.load();

	setting.getAccessorsSetting().//
		select(s -> s.getName().equals("OAIPMH Accessor"));

	setting.setIdentifier(UUID.randomUUID().toString());

	configuration.put(setting);
	
	SystemSetting systemSetting = new SystemSetting();
	systemSetting.setIdentifier(MainSettingsIdentifier.SYSTEM_SETTINGS.getLabel());
	configuration.put(systemSetting);
	
	DatabaseSetting databaseSetting = new DatabaseSetting();
	databaseSetting.setIdentifier(MainSettingsIdentifier.DATABASE.getLabel());
	configuration.put(databaseSetting);

	SelectionUtils.deepClean(configuration);

	//
	//
	//

	ConfigurationWrapper.setConfiguration(configuration);

	//
	//
	//

	HarvestingSetting harvesterWorkerSetting = ConfigurationWrapper.getHarvestingSettings().get(0);

	//
	//
	//

	AccessorSetting selectedAccessorSetting = harvesterWorkerSetting.getSelectedAccessorSetting();

	selectedAccessorSetting.getConfigurableType().equals(OAIPMHAccessor.TYPE);

	//
	//
	//

	List<AugmenterSetting> selectedAugmenterSettings = harvesterWorkerSetting.getSelectedAugmenterSettings();

	Assert.assertEquals(0, selectedAugmenterSettings.size());
    }

    /**
     * 
     */
    @SuppressWarnings("rawtypes")
    @Test
    public void test() {

	ServiceLoader<Configurable> configurables = ServiceLoader.load(Configurable.class);

	HarvestingSetting setting = HarvestingSettingLoader.load();

	//
	//
	//

	Optional<CustomTaskSetting> customTaskSetting = setting.getCustomTaskSetting();
	Assert.assertTrue(customTaskSetting.isPresent());

	Assert.assertFalse(customTaskSetting.get().isEnabled());

	String name = customTaskSetting.get().getName();
	Assert.assertEquals("Custom task settings", name);

	//
	//
	//

	{
	    List<IHarvestedAccessor> harvestedAccessors = AccessorFactory.getHarvestedAccessors(LookupPolicy.ALL);

	    //
	    // This setting holds all the available harvested accessors
	    //
	    Setting accessorsSetting = setting.getAccessorsSetting();

	    List<Setting> accessorsSettingsList = accessorsSetting.getSettings();
	    Assert.assertEquals(harvestedAccessors.size(), accessorsSettingsList.size());

	    //
	    // only one accessor can be selected
	    //
	    SelectionMode multiMode = accessorsSetting.getSelectionMode();
	    Assert.assertEquals(SelectionMode.SINGLE, multiMode);

	    //
	    // none setting is compacted
	    //
	    long count = accessorsSettingsList.stream().filter(s -> s.isCompactModeEnabled()).count();
	    Assert.assertEquals(0, count);

	    //
	    // selecting OAI and removing all the others
	    //
	    accessorsSetting.//
		    select(s -> s.getName().equals("OAIPMH Accessor"));

	    accessorsSetting.clean();

	    //
	    //
	    //

	    accessorsSettingsList = accessorsSetting.getSettings();
	    Assert.assertEquals(1, accessorsSettingsList.size());

	    accessorsSettingsList.get(0).getConfigurableType().equals(OAIPMHAccessor.TYPE);

	    AccessorSetting selectedAccessorSetting = setting.getSelectedAccessorSetting();
	    Assert.assertEquals(selectedAccessorSetting, accessorsSettingsList.get(0));
	}

	{

	    List<Configurable> augmenters = StreamUtils.iteratorToStream(configurables.iterator()).//
		    filter(c -> c.getSetting() instanceof AugmenterSetting).//
		    collect(Collectors.toList());

	    Setting augmentersSetting = setting.getAugmentersSetting();

	    //
	    // This setting holds all the available augmenters
	    //
	    List<Setting> augmentersSettingsList = augmentersSetting.getSettings();
	    Assert.assertEquals(augmenters.size(), augmentersSettingsList.size());

	    // more than one augmenter can be selected
	    SelectionMode multiMode = augmentersSetting.getSelectionMode();
	    Assert.assertEquals(SelectionMode.MULTI, multiMode);

	    //
	    // selecting 2 augmenters and removing all the others
	    //
	    augmentersSetting.//
		    select(s -> s.getConfigurableType().equals("MetadataAugmenter") || //
			    s.getConfigurableType().equals("ResourceReindexer"));

	    augmentersSetting.clean();

	    //
	    //
	    //

	    augmentersSettingsList = augmentersSetting.getSettings();
	    Assert.assertEquals(2, augmentersSettingsList.size());
	    augmentersSettingsList.sort((s1, s2) -> s1.getConfigurableType().compareTo(s2.getConfigurableType()));

	    Assert.assertEquals("MetadataAugmenter", augmentersSettingsList.get(0).getConfigurableType());
	    Assert.assertEquals("ResourceReindexer", augmentersSettingsList.get(1).getConfigurableType());

	    List<AugmenterSetting> selectedAugmentersSettings = setting.getSelectedAugmenterSettings();
	    Assert.assertEquals(2, selectedAugmentersSettings.size());
	    selectedAugmentersSettings.sort((s1, s2) -> s1.getConfigurableType().compareTo(s2.getConfigurableType()));

	    Assert.assertEquals("MetadataAugmenter", selectedAugmentersSettings.get(0).getConfigurableType());
	    Assert.assertEquals("ResourceReindexer", selectedAugmentersSettings.get(1).getConfigurableType());
	}
    }
}
