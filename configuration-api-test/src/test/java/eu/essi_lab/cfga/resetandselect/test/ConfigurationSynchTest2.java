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
public class ConfigurationSynchTest2 {

    /**
     * The tested configuration "synch-test-configuration-2.json" has an instance of {@link TestSetting2}
     * with option 2 and option 3 and without option 1.<br>
     * The selected values of {@link TestSetting1} options in the configurations are:
     * <ul>
     * <li>option 2 -> "4"</li>
     * <li>option 3 -> "D"</li>
     * </ul>
     * {@link TestSetting2}.java has also option 1, so the setting in the
     * configuration is not valid according to the similarity test.<br>
     * <br>
     * In this test the configuration instance of {@link TestSetting1} is synchronized with {@link TestSetting2}.java,
     * than it is replaced with the synch instance and the similarity test is redone with success.
     * 
     * @throws Exception
     */
    @Test
    public void synchTest2() throws Exception {

	FileSource fileSource = new FileSource(
		ConfigurationSynchTest2.class.getClassLoader().getResource("synch-test-configuration-2.json").toURI());

	Configuration configuration = new Configuration(fileSource);

	TestSetting2 configTestSetting2 = SettingUtils.downCast(configuration.get("testSetting2id").get(), TestSetting2.class);

	//
	// in the configuration the setting has 2 options
	//
	testTestSetting2(configTestSetting2, 2);

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

	TestSetting2 synchTestSetting2 = null;

	for (Setting setting : settings) {

	    Setting synch = SelectionUtils.resetAndSelect(setting, false);

	    SelectionUtils.deepClean(synch);

	    synchTestSetting2 = SettingUtils.downCast(synch, TestSetting2.class);
	}

	//
	// the current version has 3 options which after the synch, have the same
	// values of the setting in the configuration. the new option (option 1)
	// have its default selected value "Yes" (true)
	//
	testTestSetting2(synchTestSetting2, 3);

	//
	// now replaces the invalid configuration setting with the synch one
	// and repeats the test
	//

	boolean replaced = configuration.replace(synchTestSetting2);
	Assert.assertTrue(replaced);

	response = method.check(configuration);

	Assert.assertTrue(response.getCheckResult() == CheckResult.CHECK_SUCCESSFUL);
    }

    /**
     * Selected values of TestSetting1 options in the configuration
     * option 1 -> No
     * option 2 -> 4
     * option 3 -> D
     * 
     * @param setting
     * @param expectedOptions
     */
    private void testTestSetting2(TestSetting2 setting, int expectedOptions) {

	List<Option<?>> options = setting.getOptions();

	Assert.assertEquals(expectedOptions, options.size());

	if (expectedOptions == 3) {

	    boolean option1SelectedValue = BooleanChoice
		    .toBoolean(setting.getOption("option1", BooleanChoice.class).get().getSelectedValue());

	    Assert.assertEquals(true, option1SelectedValue);
	}

	int option2SelectedValue = setting.getOption("option2", Integer.class).get().getSelectedValue();
	Assert.assertEquals(4, option2SelectedValue);

	String option3SelectedValue = setting.getOption("option3", String.class).get().getSelectedValue();
	Assert.assertEquals("D", option3SelectedValue);
    }
}
