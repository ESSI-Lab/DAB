/**
 * 
 */
package eu.essi_lab.cfga.resetandselect.test;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.cfga.Configuration;
import eu.essi_lab.cfga.SelectionUtils;
import eu.essi_lab.cfga.checker.CheckResponse;
import eu.essi_lab.cfga.checker.CheckResponse.CheckResult;
import eu.essi_lab.cfga.gs.SimilarityCheckMethod;
import eu.essi_lab.cfga.option.BooleanChoice;
import eu.essi_lab.cfga.option.Option;
import eu.essi_lab.cfga.setting.Setting;
import eu.essi_lab.cfga.setting.SettingUtils;
import eu.essi_lab.cfga.source.FileSource;

/**
 * @author Fabrizio
 */
public class ConfigurationSynchTest4 {

    /**
     * The tested configuration "synch-test-configuration-4.json" has an instance of {@link TestSetting4}
     * WITHOUT sub-settings while {@link TestSetting4}.java HAS a sub-setting, so the serialized setting is not valid
     * according to the similarity test.<br>
     * <br>
     * In this test the serialized setting is synchronized and replaced,
     * finally the similarity test is redone with success.
     * 
     * @throws Exception
     */
    @Test
    public void synchTest4() throws Exception {

	FileSource fileSource = new FileSource(
		ConfigurationSynchTest4.class.getClassLoader().getResource("synch-test-configuration-4.json").toURI());

	Configuration configuration = new Configuration(fileSource);

	TestSetting4 configTestSetting4 = SettingUtils.downCast(//
		configuration.get("testSetting4id").get(), //
		TestSetting4.class);

	//
	// in the configuration the setting do not have its original sub-setting
	//

	List<Setting> subSettings = configTestSetting4.getSettings();
	Assert.assertEquals(0, subSettings.size());

	//
	//
	//

	SimilarityCheckMethod method = new SimilarityCheckMethod();

	CheckResponse response = method.check(configuration);

	Assert.assertTrue(response.getCheckResult() == CheckResult.CHECK_FAILED);

	List<Setting> settings = response.getSettings();

	//
	// synchronizes the configuration TestSetting4 instance with the current TestSetting4 setting class
	//

	TestSetting4 synchTestSetting4 = null;

	for (Setting setting : settings) {

	    Setting synch = SelectionUtils.resetAndSelect(setting, false);

	    SelectionUtils.deepClean(synch);

	    synchTestSetting4 = SettingUtils.downCast(synch, TestSetting4.class);
	}

	//
	// the current version has 1 sub-setting
	//

	subSettings = synchTestSetting4.getSettings();
	Assert.assertEquals(1, subSettings.size());//

	testSubTestSettingOptions(subSettings.get(0));

	//
	// now replaces the invalid configuration setting with the synch one
	// and repeats the test
	//

	boolean replaced = configuration.replace(synchTestSetting4);
	Assert.assertTrue(replaced);

	response = method.check(configuration);

	Assert.assertTrue(response.getCheckResult() == CheckResult.CHECK_SUCCESSFUL);
    }

    /**
     * Selected values of TestSetting4 sub-setting options in the configuration:
     * option 1 -> Yes
     * option 2 -> 3
     * option 3 -> C
     * 
     * @param setting
     * @param expectedOptions
     */
    private void testSubTestSettingOptions(Setting setting) {

	List<Option<?>> options = setting.getOptions();

	Assert.assertEquals(3, options.size());

	boolean option1SelectedValue = BooleanChoice.toBoolean(//
		setting.getOption("option1", BooleanChoice.class).get().getSelectedValue());

	Assert.assertEquals(true, option1SelectedValue);

	int option2SelectedValue = setting.getOption("option2", Integer.class).get().getSelectedValue();
	Assert.assertEquals(3, option2SelectedValue);

	String option3SelectedValue = setting.getOption("option3", String.class).get().getSelectedValue();
	Assert.assertEquals("C", option3SelectedValue);
    }
}
