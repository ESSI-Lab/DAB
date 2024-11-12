/**
 * 
 */
package eu.essi_lab.cfga.resetandselect.test.scheduler;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.cfga.Configuration;
import eu.essi_lab.cfga.SelectionUtils;
import eu.essi_lab.cfga.checker.CheckResponse;
import eu.essi_lab.cfga.checker.CheckResponse.CheckResult;
import eu.essi_lab.cfga.gs.SimilarityCheckMethod;
import eu.essi_lab.cfga.option.Option;
import eu.essi_lab.cfga.setting.Setting;
import eu.essi_lab.cfga.setting.SettingUtils;
import eu.essi_lab.cfga.source.FileSource;

/**
 * @author Fabrizio
 */
public class TestSchedulerConfigurationSynchTest {

    /**
     * The tested configuration "synch-test-configuration-5.json" has an instance of {@link TestSchedulerSetting}
     * without the test option.<br>
     * 
     * @throws Exception
     */
    @Test
    public void synchTest1() throws Exception {

	FileSource fileSource = new FileSource(
		TestSchedulerConfigurationSynchTest.class.getClassLoader().getResource("synch-test-configuration-5.json").toURI());

	Configuration configuration = new Configuration(fileSource);

	//
	//
	//

	SimilarityCheckMethod method = new SimilarityCheckMethod();

	CheckResponse response = method.check(configuration);

	Assert.assertTrue(response.getCheckResult() == CheckResult.CHECK_FAILED);

	List<Setting> settings = response.getSettings();

	Assert.assertEquals(1, settings.size());

	Assert.assertEquals("TEST Scheduler", settings.get(0).getName());

	//
	// the serialized TestSchedulerSetting version has only 1 option (userDateTime) 
	// while it should have 3 options
	//

	List<Option<?>> options = settings.get(0).getOptions();
	Assert.assertEquals(1, options.size());

	//
	// synchronizes the configuration TestSchedulerSetting instance with the current TestSchedulerSetting setting class
	//

	TestSchedulerSetting synchTestSetting = null;

	for (Setting setting : settings) {

	    Setting synch = SelectionUtils.resetAndSelect(setting, false);

	    SelectionUtils.deepClean(synch);

	    synchTestSetting = SettingUtils.downCast(synch, TestSchedulerSetting.class);
	}

	//
	// the current version has 3 options
	//

	List<Option<?>> synchTestSettingOptions = synchTestSetting.getOptions();
	Assert.assertEquals(3, synchTestSettingOptions.size());

	//
	// now replaces the invalid configuration setting with the synch one
	// and repeats the test
	//

	boolean replaced = configuration.replace(synchTestSetting);
	Assert.assertTrue(replaced);

	response = method.check(configuration);

	Assert.assertTrue(response.getCheckResult() == CheckResult.CHECK_SUCCESSFUL);
    }
}
