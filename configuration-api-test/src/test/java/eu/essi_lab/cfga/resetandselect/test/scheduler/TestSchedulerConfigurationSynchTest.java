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
     * The tested configuration "synch-test-configuration-1.json" has an instance of {@link TestSetting1}
     * with option 1, option 2 and option 3.<br>
     * The selected values of {@link TestSetting1} options in the configurations are:
     * <ul>
     * <li>option 1 -> "No"</li>
     * <li>option 2 -> "2"</li>
     * <li>option 3 -> "C"</li>
     * </ul>
     * {@link TestSetting1}.java misses option 1, so the setting in the
     * configuration is not valid according to the similarity test.<br>
     * <br>
     * In this test the configuration instance of {@link TestSetting1} is synchronized with {@link TestSetting1}.java,
     * than it is replaced with the synch instance and the similarity test is redone with success.
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
	// the serialized version has 1 option
	//

	List<Option<?>> options = settings.get(0).getOptions();
	Assert.assertEquals(1, options.size());

	//
	// synchronizes the configuration TestSetting1 instance with the current TestSetting1 setting class
	//

	TestSchedulerSetting synchTestSetting = null;

	for (Setting setting : settings) {

	    Setting synch = SelectionUtils.resetAndSelect(setting, false);

	    SelectionUtils.deepClean(synch);

	    synchTestSetting = SettingUtils.downCast(synch, TestSchedulerSetting.class);
	}

	//
	// the current version has 2 options
	//

	List<Option<?>> synchTestSettingOptions = synchTestSetting.getOptions();
	Assert.assertEquals(2, synchTestSettingOptions.size());

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