/**
 * 
 */
package eu.essi_lab.cfga.resetandselect.test;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.cfga.Configuration;
import eu.essi_lab.cfga.SelectionUtils;
import eu.essi_lab.cfga.check.CheckResponse;
import eu.essi_lab.cfga.check.CheckResponse.CheckResult;
import eu.essi_lab.cfga.gs.SimilarityCheckMethod;
import eu.essi_lab.cfga.option.Option;
import eu.essi_lab.cfga.setting.Setting;
import eu.essi_lab.cfga.setting.SettingUtils;
import eu.essi_lab.cfga.source.FileSource;

/**
 * @author Fabrizio
 */
public class ConfigurationSynchTest1 {

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
		ConfigurationSynchTest1.class.getClassLoader().getResource("synch-test-configuration-1.json").toURI());

	Configuration configuration = new Configuration(fileSource);

	TestSetting1 configTestSetting1 = SettingUtils.downCast(configuration.get("testSetting1id").get(), TestSetting1.class);

	//
	// in the configuration the setting has still 3 options
	//
	testTestSetting1(configTestSetting1, 3);

	//
	//
	//

	SimilarityCheckMethod method = new SimilarityCheckMethod();

	CheckResponse response = method.check(configuration);

	Assert.assertTrue(response.getCheckResult() == CheckResult.CHECK_FAILED);

	List<Setting> settings = response.getSettings();

	//
	// synchronizes the configuration TestSetting1 instance with the current TestSetting1 setting class
	//

	TestSetting1 synchTestSetting1 = null;

	for (Setting setting : settings) {

	    Setting synch = SelectionUtils.resetAndSelect(setting, false);

	    SelectionUtils.deepClean(synch);

	    synchTestSetting1 = SettingUtils.downCast(synch, TestSetting1.class);
	}

	//
	// the current version has 2 options which after the synch, have the same
	// values of the setting in the configuration
	//
	testTestSetting1(synchTestSetting1, 2);

	//
	// now replaces the invalid configuration setting with the synch one
	// and repeats the test
	//

	boolean replaced = configuration.replace(synchTestSetting1);
	Assert.assertTrue(replaced);

	response = method.check(configuration);

	Assert.assertTrue(response.getCheckResult() == CheckResult.CHECK_SUCCESSFUL);
    }

    /**
     * Selected values of TestSetting1 options in the configuration
     * option 1 -> No (removed from the current setting class)
     * option 2 -> 2
     * option 3 -> C
     * 
     * @param setting
     * @param expectedOptions
     */
    private void testTestSetting1(TestSetting1 setting, int expectedOptions) {

	List<Option<?>> options = setting.getOptions();

	//
	// in the configuration the setting has still 3 options
	//
	Assert.assertEquals(expectedOptions, options.size());

	int option2SelectedValue = setting.getOption("option2", Integer.class).get().getSelectedValue();
	Assert.assertEquals(2, option2SelectedValue);

	String option3SelectedValue = setting.getOption("option3", String.class).get().getSelectedValue();
	Assert.assertEquals("C", option3SelectedValue);
    }
}
