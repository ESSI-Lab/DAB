/**
 * 
 */
package eu.essi_lab.cfga.test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.cfga.Configuration;
import eu.essi_lab.cfga.Configuration.State;
import eu.essi_lab.cfga.gs.setting.database.DatabaseSetting;
import eu.essi_lab.cfga.gs.setting.driver.DriverSetting;
import eu.essi_lab.cfga.gs.setting.driver.SharedCacheDriverSetting;
import eu.essi_lab.cfga.option.Option;
import eu.essi_lab.cfga.setting.Setting;
import eu.essi_lab.cfga.setting.scheduling.SchedulerWorkerSetting;
import eu.essi_lab.cfga.setting.scheduling.Scheduling;
import eu.essi_lab.cfga.source.FileSource;

/**
 * @author Fabrizio
 */
public class ConfigurationTest {

    private static final String SCHEDULED_SETTING_ID = "SCHEDULED_SETTING_ID";
    private static final String CONFIGURATION_SETTING_1_ID = "CONFIGURATION_SETTING_1_ID";
    private static final String CONFIGURATION_SETTING_1_2_ID = "CONFIGURATION_SETTING_1_2_ID";
    private SchedulerWorkerSetting schedulerWorkerSetting;
    private Setting setting1;
    private boolean dirtyFlagTestPassed = true;
    private boolean dirtyFlagTestEnded;

    @Test
    public void exactClassMatchGetAndListMethodsTest() throws Exception {

	FileSource filesSource = new FileSource();

	Configuration configuration = new Configuration(filesSource);

	configuration.clear();

	// ----

	DatabaseSetting volatileDatabaseSetting = new DatabaseSetting();
	volatileDatabaseSetting.setIdentifier("0volSettingId");

	configuration.put(volatileDatabaseSetting);

	Setting databaseSetting = new Setting();
	databaseSetting.setIdentifier("1dbSettingId");

	configuration.put(databaseSetting);

	// ----

	List<Setting> list = configuration.list();
	Assert.assertEquals(2, list.size());

	// -----------------------------
	//
	//
	// Target: Setting.class
	//
	// 1) Testing list method
	//
	//
	// Testing list method with the flag enabled (default). This way only exact class match produce
	// results, so only settings of class Setting.class are returned, ignoring subclasses
	//

	{
	    List<Setting> dbSettingsList = configuration.list(Setting.class);

	    Assert.assertEquals(1, dbSettingsList.size());
	    Assert.assertEquals(databaseSetting, dbSettingsList.get(0));

	    Assert.assertEquals(Setting.class, dbSettingsList.get(0).getClass());
	    Assert.assertEquals(Setting.class, dbSettingsList.get(0).getSettingClass());

	    dbSettingsList = configuration.list(Setting.class, true);
	    Assert.assertEquals(1, dbSettingsList.size());
	    Assert.assertEquals(databaseSetting, dbSettingsList.get(0));

	    Assert.assertEquals(Setting.class, dbSettingsList.get(0).getClass());
	    Assert.assertEquals(Setting.class, dbSettingsList.get(0).getSettingClass());

	    //
	    // Testing list method with the flag disabled. This way also sublasses produce
	    // results, so also DatabaseSetting.class instance is expected
	    //

	    dbSettingsList = configuration.list(Setting.class, false);
	    dbSettingsList.sort((s1, s2) -> s1.getIdentifier().compareTo(s2.getIdentifier()));

	    Assert.assertEquals(2, dbSettingsList.size());

	    Assert.assertEquals(volatileDatabaseSetting, dbSettingsList.get(0));
	    Assert.assertEquals(databaseSetting, dbSettingsList.get(1));

	    Assert.assertEquals(DatabaseSetting.class, dbSettingsList.get(0).getClass());
	    Assert.assertEquals(DatabaseSetting.class, dbSettingsList.get(0).getSettingClass());

	    Assert.assertEquals(Setting.class, dbSettingsList.get(1).getClass());
	    Assert.assertEquals(Setting.class, dbSettingsList.get(1).getSettingClass());

	    //
	    //
	    // 2) Testing get method
	    //
	    //

	    Setting setting = configuration.get("1dbSettingId").get();
	    Assert.assertEquals(databaseSetting, setting);

	    // the instance is Setting but its setting class is Setting.class
	    Assert.assertEquals(Setting.class, setting.getClass());
	    Assert.assertEquals(Setting.class, setting.getSettingClass());

	    Setting databaseSetting2 = configuration.get("1dbSettingId", Setting.class).get();

	    Assert.assertEquals(databaseSetting, databaseSetting2);
	    Assert.assertEquals(Setting.class, databaseSetting2.getClass());

	    databaseSetting2 = configuration.get("1dbSettingId", Setting.class, false).get();
	    Assert.assertEquals(databaseSetting, databaseSetting2);

	    // looking for an instance of DatabaseSetting.class (0volSettingId)
	    // with the default exact class match produces no results
	    Optional<Setting> optional = configuration.get("0volSettingId", Setting.class);
	    Assert.assertFalse(optional.isPresent());

	    // disabling the flag, since DatabaseSetting is subclass of Setting,
	    // we get the result
	    Optional<Setting> optional2 = configuration.get("0volSettingId", Setting.class, false);
	    Assert.assertTrue(optional2.isPresent());
	    Assert.assertEquals(volatileDatabaseSetting, optional2.get());

	    Assert.assertEquals(DatabaseSetting.class, optional2.get().getClass());
	    Assert.assertEquals(DatabaseSetting.class, optional2.get().getSettingClass());
	}
	{
	    // -----------------------------
	    //
	    //
	    // Target: DatabaseSetting.class
	    //
	    // 1) Testing list method
	    //
	    //
	    // Testing list method with the flag enabled (default)
	    //

	    List<DatabaseSetting> dbSettingsList = configuration.list(DatabaseSetting.class);

	    Assert.assertEquals(1, dbSettingsList.size());
	    Assert.assertEquals(volatileDatabaseSetting, dbSettingsList.get(0));

	    dbSettingsList = configuration.list(DatabaseSetting.class, true);

	    Assert.assertEquals(1, dbSettingsList.size());

	    Assert.assertEquals(volatileDatabaseSetting, dbSettingsList.get(0));
	    Assert.assertEquals(DatabaseSetting.class, dbSettingsList.get(0).getClass());
	    Assert.assertEquals(DatabaseSetting.class, dbSettingsList.get(0).getSettingClass());

	    //
	    // Testing list method with the flag disabled. Since there are no subclasses of
	    // DatabaseSetting.class in the configuration, even with the flag disabled one match
	    // is expected
	    //

	    dbSettingsList = configuration.list(DatabaseSetting.class, false);

	    Assert.assertEquals(1, dbSettingsList.size());

	    Assert.assertEquals(volatileDatabaseSetting, dbSettingsList.get(0));
	    Assert.assertEquals(DatabaseSetting.class, dbSettingsList.get(0).getClass());
	    Assert.assertEquals(DatabaseSetting.class, dbSettingsList.get(0).getSettingClass());

	    //
	    //
	    // 2) Testing get method
	    //
	    //

	    Setting setting = configuration.get("0volSettingId").get();
	    Assert.assertEquals(volatileDatabaseSetting, setting);

	    // the instance is a Setting but its settingClass is a DatabaseSetting
	    Assert.assertEquals(Setting.class, setting.getClass());
	    Assert.assertEquals(DatabaseSetting.class, setting.getSettingClass());

	    DatabaseSetting databaseSetting2 = configuration.get("0volSettingId", DatabaseSetting.class).get();
	    Assert.assertEquals(volatileDatabaseSetting, databaseSetting2);

	    Assert.assertEquals(DatabaseSetting.class, databaseSetting2.getClass());
	    Assert.assertEquals(DatabaseSetting.class, databaseSetting2.getSettingClass());

	    // disabling the flag makes no difference
	    databaseSetting2 = configuration.get("0volSettingId", DatabaseSetting.class, false).get();
	    Assert.assertEquals(volatileDatabaseSetting, databaseSetting2);

	    Assert.assertEquals(DatabaseSetting.class, databaseSetting2.getClass());
	    Assert.assertEquals(DatabaseSetting.class, databaseSetting2.getSettingClass());

	    Optional<Setting> optional = configuration.get("0volSettingId", Setting.class);
	    Assert.assertFalse(optional.isPresent());

	    //
	    //
	    // Testing an abstract class as target
	    //
	    //
	    {
		configuration.clear();

		//
		//
		//

		SharedCacheDriverSetting sharedCacheDriverSetting = new SharedCacheDriverSetting();
		sharedCacheDriverSetting.setIdentifier("id3");

		configuration.put(sharedCacheDriverSetting);

		list = configuration.list();

		Assert.assertEquals(1, list.size());

		Assert.assertEquals(Setting.class, list.get(0).getClass());
		Assert.assertEquals(SharedCacheDriverSetting.class, list.get(0).getSettingClass());

		//
		//
		//

		SharedCacheDriverSetting sharedCacheDriverSetting2 = configuration.list(SharedCacheDriverSetting.class).get(0);

		Assert.assertEquals(sharedCacheDriverSetting, sharedCacheDriverSetting2);

		Assert.assertEquals(SharedCacheDriverSetting.class, sharedCacheDriverSetting2.getClass());
		Assert.assertEquals(SharedCacheDriverSetting.class, sharedCacheDriverSetting2.getSettingClass());

		//
		// DriverSetting is an abstract superclass of SharedCacheDriverSetting and with the flag enabled,
		// since there is no direct instance of DriverSetting.class, 0 results are expected
		//

		List<DriverSetting> driverSettingList = configuration.list(DriverSetting.class);
		Assert.assertEquals(0, driverSettingList.size());

		//
		// with the flag disabled, we expect now one match since SharedCacheDriverSetting.class is
		// subclass of the abstract class DriverSetting.class
		//
		driverSettingList = configuration.list(DriverSetting.class, false);

		Assert.assertEquals(1, driverSettingList.size());

		Assert.assertEquals(SharedCacheDriverSetting.class, driverSettingList.get(0).getClass());
		Assert.assertEquals(SharedCacheDriverSetting.class, driverSettingList.get(0).getSettingClass());
	    }
	}
    }

    @Test
    public void multiRemovalTest() {

	Configuration configuration = new Configuration();

	for (int i = 0; i < 5; i++) {

	    Setting setting = new Setting();
	    setting.setIdentifier("setting" + i);

	    configuration.put(setting);
	}

	List<Setting> list = configuration.list();
	Assert.assertEquals(5, list.size());

	//
	//
	//

	boolean removed = configuration.remove(Arrays.asList("settingX"));
	Assert.assertFalse(removed);
	Assert.assertEquals(5, list.size());

	//
	//
	//

	removed = configuration.remove(Arrays.asList("settingX", "setting0"));
	Assert.assertTrue(removed);
	list = configuration.list();
	Assert.assertEquals(4, list.size());

	//
	//
	//

	removed = configuration.remove(Arrays.asList("setting1", "setting2", "setting3", "setting4"));
	Assert.assertTrue(removed);
	list = configuration.list();
	Assert.assertEquals(0, list.size());
    }

    @Test
    public void dirtyFlagTest() throws Exception {

	FileSource filesSource = new FileSource();

	Configuration configuration = new Configuration(filesSource);

	configuration.clear();

	Assert.assertEquals(0, configuration.list().size());

	//
	//
	//

	SchedulerWorkerSetting setting = new SchedulerWorkerSetting();

	setting.setIdentifier("id");
	setting.setDescription("Description");
	setting.setName("Name");

	boolean added = configuration.put(setting);

	Assert.assertEquals(1, configuration.list().size());

	//
	//
	//

	Assert.assertEquals(State.DIRTY, configuration.getState());

	//
	//
	//

	configuration.flush();

	Assert.assertEquals(State.SYNCH, configuration.getState());

	//
	//
	//

	configuration.list().get(0).setName("New name");

	Assert.assertEquals("Name", configuration.list().get(0).getName());

	Assert.assertEquals(State.SYNCH, configuration.getState());

	//
	//
	//

	configuration.get("id").get().setName("New name 1");

	Assert.assertEquals("Name", configuration.list().get(0).getName());

	Assert.assertEquals(State.SYNCH, configuration.getState());

	//
	//
	//

	configuration.list(SchedulerWorkerSetting.class).get(0).setName("New name 2");

	Assert.assertEquals("Name", configuration.list().get(0).getName());

	Assert.assertEquals(State.SYNCH, configuration.getState());

	//
	//
	//

	configuration.get("id", SchedulerWorkerSetting.class).get().setName("New name 3");

	Assert.assertEquals("Name", configuration.list().get(0).getName());

	Assert.assertEquals(State.SYNCH, configuration.getState());

	//
	// this setting is already in, so the put method fails
	//

	added = configuration.put(setting);

	Assert.assertEquals(1, configuration.list().size());

	Assert.assertFalse(added);

	Assert.assertEquals(State.SYNCH, configuration.getState());

	//
	// this setting is not in, so the remove method fails
	//

	boolean removed = configuration.remove("abc");

	Assert.assertEquals(1, configuration.list().size());

	Assert.assertFalse(removed);

	Assert.assertEquals(State.SYNCH, configuration.getState());

	//
	// the remove method works now
	//

	removed = configuration.remove(setting.getIdentifier());

	Assert.assertEquals(0, configuration.list().size());

	Assert.assertTrue(removed);

	Assert.assertEquals(State.DIRTY, configuration.getState());

	configuration.flush();

	Assert.assertEquals(State.SYNCH, configuration.getState());

	//
	// the list is empty, so the put method works
	// note that the put method put a clone of the setting
	//

	setting.setName("New name 6");

	added = configuration.put(setting);

	Assert.assertTrue(added);

	Assert.assertEquals(State.DIRTY, configuration.getState());

	//
	// the setting description is changed, but a setting with
	// the same id is already in so the put method fails
	//

	setting.setDescription("New description");

	added = configuration.put(setting);

	Assert.assertFalse(added);

	Assert.assertEquals(State.DIRTY, configuration.getState());

	setting.setDescription("Description");

	//
	// changing a setting retrieved from readonly methods do not alter the config
	//

	SchedulerWorkerSetting schedulingSetting = configuration.get("id", SchedulerWorkerSetting.class).get();

	schedulingSetting.setName("New name 7");

	Assert.assertEquals("New name 6", configuration.get("id", SchedulerWorkerSetting.class).get().getName());

	schedulingSetting = configuration.list(SchedulerWorkerSetting.class).get(0);

	schedulingSetting.setName("New name 7");

	Assert.assertEquals("New name 6", configuration.get("id", SchedulerWorkerSetting.class).get().getName());

	Setting setting2 = configuration.get("id").get();

	setting2.setName("New name 7");

	Assert.assertEquals("New name 6", configuration.get("id", SchedulerWorkerSetting.class).get().getName());

	//
	// the setting is not in, so the replace method fails
	//

	boolean replaced = configuration.replace(new Setting());

	Assert.assertFalse(replaced);

	Assert.assertEquals(State.DIRTY, configuration.getState());

	//
	// the setting is not changed, so even if a setting with such id
	// is in, the replace method fails
	//

	replaced = configuration.replace(setting);

	Assert.assertFalse(replaced);

	Assert.assertEquals(State.DIRTY, configuration.getState());

	//
	// changing the setting outside the config do not alter the configuration
	// since the put method used before put a copy of the setting
	// the previous setting with "New name 6" is still in the config
	//
	setting.setName("New name 7");

	Assert.assertEquals("New name 6", configuration.get("id").get().getName());

	//
	// now the setting is changed, so the put method works
	//

	replaced = configuration.replace(setting);

	Assert.assertEquals("New name 7", configuration.get("id").get().getName());

	Assert.assertTrue(replaced);

	Assert.assertEquals(State.DIRTY, configuration.getState());

	//
	// the replace method put a clone of the setting, so changing the setting
	// outside the config do not alter the config
	//

	setting.setName("New name 8");

	Assert.assertEquals("New name 7", configuration.get("id").get().getName());

	configuration.list(SchedulerWorkerSetting.class).get(0).setName("New name 8");

	Assert.assertEquals("New name 7", configuration.get("id").get().getName());

	configuration.get("id", SchedulerWorkerSetting.class).get().setName("New name 8");

	Assert.assertEquals("New name 7", configuration.get("id").get().getName());

	//
	// now the config is dirty
	//
	// the config will try to synch with the source
	// and since dirty --->
	//
	configuration.autoreload(TimeUnit.SECONDS, 2, 0);

	TimerTask timerTask = new TimerTask() {
	    @Override
	    public void run() {

		//
		// --> after 3 seconds and at least 2 autoreload attempts,
		// we expect that the autoreload failed until
		// the config is flushed
		//
		try {
		    dirtyFlagTestPassed &= configuration.getState() == State.DIRTY;

		    // the config is now flushed
		    configuration.flush();

		    // and now it's no longer dirty
		    dirtyFlagTestPassed &= configuration.getState() == State.SYNCH;

		} catch (Exception e) {
		    e.printStackTrace();
		    dirtyFlagTestPassed = false;
		}

		dirtyFlagTestEnded = true;
	    }
	};

	new Timer().schedule(//
		timerTask, //
		TimeUnit.SECONDS.toMillis(3));

	//
	//
	//

	while (!dirtyFlagTestEnded) {
	    Thread.sleep(1000);
	}

	Assert.assertTrue(dirtyFlagTestPassed);

	//
	//
	//

	configuration.clear();

	Assert.assertEquals(0, configuration.list().size());

	Assert.assertEquals(State.DIRTY, configuration.getState());
    }

    @Test
    public void test() throws Exception {

	FileSource filesSource = new FileSource();

	Configuration configuration = new Configuration(filesSource);

	configuration.clear();

	schedulerWorkerSetting = new SchedulerWorkerSetting();

	// !!! concrete subclasses must be provided !!!
	schedulerWorkerSetting.setConfigurableType("Configurable");

	Scheduling scheduling = schedulerWorkerSetting.getScheduling();
	Assert.assertNotNull(scheduling);

	schedulerWorkerSetting.getScheduling().setStartTime("startTime");
	schedulerWorkerSetting.getScheduling().setEndTime("endTime");
	schedulerWorkerSetting.getScheduling().setRepeatCount(5);
	schedulerWorkerSetting.getScheduling().setRepeatInterval(10, TimeUnit.DAYS);

	schedulerWorkerSetting.setIdentifier(SCHEDULED_SETTING_ID);
	schedulerWorkerSetting.setDescription("Scheduled setting description");
	schedulerWorkerSetting.setName("Scheduled setting");

	setting1 = new Setting();

	// !!! concrete subclasses must be provided !!!
	setting1.setConfigurableType("Configurable");

	setting1.setIdentifier(CONFIGURATION_SETTING_1_ID);
	setting1.setDescription("Configuration setting description");
	setting1.setName("Configuration setting");

	// A string option for the configuration setting
	Option<String> configurationOption = new Option<>(String.class);
	configurationOption.setValues(Arrays.asList("a", "b", "c"));
	configurationOption.setKey("lettersOption");
	configurationOption.setLabel("Choose the letter");
	configurationOption.setRequired(true);
	configurationOption.setValue("a");

	// adds the option
	setting1.addOption(configurationOption);

	// adds a boolean option
	Option<Boolean> booleanOption = new Option<>(Boolean.class);
	booleanOption.setKey("booleanOption");
	setting1.addOption(booleanOption);

	// An inner setting
	Setting setting1_2 = new Setting();
	setting1_2.setIdentifier(CONFIGURATION_SETTING_1_2_ID);
	setting1_2.setDescription("Description 1_2");
	setting1_2.setName("Setting 1_1");

	setting1.addSetting(setting1_2);

	//
	// put the settings
	//
	configuration.put(schedulerWorkerSetting);
	configuration.put(setting1);

	// flushes the config to the file
	configuration.flush();

	Assert.assertEquals(1, configuration.size(Setting.class));

	test(configuration);

	test(new Configuration(filesSource));
    }

    private void test(Configuration configuration) {

	//
	//
	//

	Assert.assertEquals(2, configuration.list().size());

	//
	//
	//
	Optional<SchedulerWorkerSetting> optionalScheduledSetting = configuration.get(//
		SCHEDULED_SETTING_ID, //
		SchedulerWorkerSetting.class);

	Assert.assertTrue(optionalScheduledSetting.isPresent());

	Assert.assertEquals(//
		optionalScheduledSetting.get().getClass(), //
		this.schedulerWorkerSetting.getClass());

	Assert.assertEquals(optionalScheduledSetting.get(), this.schedulerWorkerSetting);

	List<SchedulerWorkerSetting> list = configuration.list(SchedulerWorkerSetting.class);
	Assert.assertEquals(1, list.size());

	Assert.assertEquals(list.get(0), optionalScheduledSetting.get());

	//
	// Setting is the super class of ScheduledSetting, so
	// no setting should be retrieved
	//
	Optional<Setting> optional = configuration.get(//
		SCHEDULED_SETTING_ID, //
		Setting.class);

	Assert.assertFalse(optional.isPresent());

	//
	//
	//

	Optional<Setting> optionalSetting = configuration.get(CONFIGURATION_SETTING_1_ID);
	Assert.assertEquals(optionalSetting.get(), this.setting1);

	List<Option<?>> options = optionalSetting.get().getOptions();
	Assert.assertEquals(2, options.size());

	List<Option<String>> stringOptions = optionalSetting.get().getOptions(String.class);
	Assert.assertEquals(1, stringOptions.size());

	List<Option<Boolean>> booleanOptions = optionalSetting.get().getOptions(Boolean.class);
	Assert.assertEquals(1, booleanOptions.size());

	List<Option<Integer>> integerOptions = optionalSetting.get().getOptions(Integer.class);
	Assert.assertEquals(0, integerOptions.size());

	//
	//
	//

	Assert.assertTrue(configuration.exists(CONFIGURATION_SETTING_1_ID));

	Assert.assertTrue(configuration.exists(SCHEDULED_SETTING_ID));

	Assert.assertFalse(configuration.exists(CONFIGURATION_SETTING_1_2_ID));

	//
 	//
 	//

	Assert.assertTrue(configuration.contains(optionalSetting.get()));

	Assert.assertTrue(configuration.contains(optionalScheduledSetting.get()));

	//
	//
	//

	boolean removed = configuration.remove(CONFIGURATION_SETTING_1_ID);
	Assert.assertTrue(removed);

	removed = configuration.remove("xxx");
	Assert.assertFalse(removed);

	Assert.assertFalse(configuration.exists(CONFIGURATION_SETTING_1_ID));

	//
	//
	//

	JSONArray jsonArray = new JSONArray(configuration.toString());

	Assert.assertTrue(jsonArray.similar(configuration.toJSONArray()));

	//
	//
	//

	configuration.clear();

	Assert.assertEquals(0, configuration.list().size());

    }
}
