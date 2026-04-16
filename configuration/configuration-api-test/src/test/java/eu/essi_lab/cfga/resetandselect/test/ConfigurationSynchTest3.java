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
import eu.essi_lab.cfga.check.SimilarityMethod;
import eu.essi_lab.cfga.check.CheckResponse.CheckResult;
import eu.essi_lab.cfga.setting.Setting;
import eu.essi_lab.cfga.setting.SettingUtils;
import eu.essi_lab.cfga.source.FileSource;

/**
 * @author Fabrizio
 */
public class ConfigurationSynchTest3 {

    /**
     * The tested configuration "synch-test-configuration-3.json" has an instance of {@link TestSetting3}
     * having also a sub-setting.<br>
     * {@link TestSetting3}.java do NOT have the sub-setting, so the serialized setting is not valid
     * according to the similarity test.<br>
     * <br>
     * In this test the serialized setting instance of {@link TestSetting3} is synchronized
     * and replaced; finally the similarity test is redone with success
     * 
     * @throws Exception
     */
    @Test
    public void synchTest3() throws Exception {

	FileSource fileSource = new FileSource(
		ConfigurationSynchTest3.class.getClassLoader().getResource("synch-test-configuration-3.json").toURI());

	Configuration configuration = new Configuration(fileSource);

	TestSetting3 configTestSetting3 = SettingUtils.downCast(//
		configuration.get("testSetting3id").get(), //
		TestSetting3.class);

	//
	// in the configuration the setting has its original sub-setting
	//

	List<Setting> subSettings = configTestSetting3.getSettings();
	Assert.assertEquals(1, subSettings.size());

	//
	//
	//

	SimilarityMethod method = new SimilarityMethod();

	CheckResponse response = method.check(configuration);

	Assert.assertTrue(response.getCheckResult() == CheckResult.CHECK_FAILED);

	List<Setting> settings = response.getSettings();

	//
	// synchronizes the configuration TestSetting3 instance with the current TestSetting3 setting class
	//

	TestSetting3 synchTestSetting3 = null;

	for (Setting setting : settings) {

	    Setting synch = SelectionUtils.resetAndSelect(setting, false);

	    SelectionUtils.deepClean(synch);

	    synchTestSetting3 = SettingUtils.downCast(synch, TestSetting3.class);
	}

	//
	// the current version has no sub-settings
	//

	subSettings = synchTestSetting3.getSettings();
	Assert.assertEquals(0, subSettings.size());//

	//
	// now replaces the invalid configuration setting with the synch one
	// and repeats the test
	//

	boolean replaced = configuration.replace(synchTestSetting3);
	Assert.assertTrue(replaced);

	response = method.check(configuration);

	Assert.assertTrue(response.getCheckResult() == CheckResult.CHECK_SUCCESSFUL);
    }
}
