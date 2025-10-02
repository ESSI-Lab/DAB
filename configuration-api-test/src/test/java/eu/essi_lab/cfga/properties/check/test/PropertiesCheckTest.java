/**
 * 
 */
package eu.essi_lab.cfga.properties.check.test;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.cfga.check.CheckResponse;
import eu.essi_lab.cfga.check.CheckResponse.CheckResult;
import eu.essi_lab.cfga.check.PropertiesMethod;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.DefaultConfiguration;
import eu.essi_lab.cfga.gs.setting.EmailSetting;
import eu.essi_lab.cfga.gs.setting.GSSourceSetting;
import eu.essi_lab.cfga.gs.setting.SystemSetting;
import eu.essi_lab.cfga.gs.setting.harvesting.HarvestingSetting;
import eu.essi_lab.cfga.gs.setting.oauth.OAuthSetting;
import eu.essi_lab.cfga.setting.ObjectExtension;
import eu.essi_lab.cfga.setting.Setting;
import eu.essi_lab.cfga.setting.SettingUtils;

/**
 * @author Fabrizio
 */
public class PropertiesCheckTest {

    @Test
    public void test0() {

	DefaultConfiguration configuration = new DefaultConfiguration();

	configuration.clean();

	//
	//
	//

	PropertiesMethod method = new PropertiesMethod();

	method.setProperties(//

		Setting.IDENTIFIER, //
		Setting.SELECTED, //
		Setting.SELECTION_MODE, //
		Setting.AFTER_CLEAN_FUNCTION, //
		Setting.CONFIGURABLE_TYPE, //
		Setting.NAME, //
		Setting.SETTING_CLASS, //
		Setting.CAN_BE_CLEANED, //
		Setting.CAN_BE_DISABLED, //
		Setting.CAN_BE_REMOVED, //
		Setting.COMPACT_MODE, //
		Setting.DESCRIPTION, //
		Setting.EDITABLE, //
		Setting.ENABLED, //
		Setting.EXTENSION, //
		Setting.FOLDED_MODE, //
		Setting.OBJECT_TYPE, //
		Setting.SHOW_HEADER, //
		Setting.VALIDATOR, //
		Setting.VISIBLE);//

	//
	//
	//

	CheckResponse check = method.check(configuration);

	Assert.assertEquals(CheckResult.CHECK_SUCCESSFUL, check.getCheckResult());
    }

    @Test
    public void test1() {

	DefaultConfiguration configuration = new DefaultConfiguration();

	configuration.clean();

	ConfigurationWrapper.setConfiguration(configuration);

	//
	//
	//

	SystemSetting systemSettings = ConfigurationWrapper.getSystemSettings();

	systemSettings.setName("Changed name");

	configuration.replace(systemSettings);

	//
	//
	//

	PropertiesMethod method = new PropertiesMethod();

	method.setProperties(//

		Setting.AFTER_CLEAN_FUNCTION, //
		Setting.CONFIGURABLE_TYPE, //
		Setting.NAME, //
		Setting.SETTING_CLASS, //
		Setting.CAN_BE_CLEANED, //
		Setting.CAN_BE_DISABLED, //
		Setting.CAN_BE_REMOVED, //
		Setting.COMPACT_MODE, //
		Setting.DESCRIPTION, //
		Setting.EDITABLE, //
		Setting.ENABLED, //
		Setting.EXTENSION, //
		Setting.FOLDED_MODE, //
		Setting.OBJECT_TYPE, //
		Setting.SHOW_HEADER, //
		Setting.VALIDATOR, //
		Setting.VISIBLE);//
	//
	//
	//

	CheckResponse check = method.check(configuration);

	Assert.assertEquals(CheckResult.CHECK_FAILED, check.getCheckResult());

	Assert.assertEquals(1, check.getSettings().size());

	Assert.assertEquals(SystemSetting.class, check.getSettings().get(0).getSettingClass());

	List<String> props = PropertiesMethod.getFailedProperties(check);

	Assert.assertEquals(1, props.size());

	Assert.assertEquals(Setting.NAME.getName(), props.get(0));
    }

    @Test
    public void test1_1() {

	DefaultConfiguration configuration = new DefaultConfiguration();

	configuration.clean();

	ConfigurationWrapper.setConfiguration(configuration);

	//
	//
	//

	SystemSetting systemSettings = ConfigurationWrapper.getSystemSettings();

	systemSettings.setName("Changed name");

	configuration.replace(systemSettings);

	//
	//
	//

	PropertiesMethod method = new PropertiesMethod();

	//
	//
	//

	Assert.assertThrows(IllegalArgumentException.class, () -> method.check(configuration));
    }

    @Test
    public void test2() {

	DefaultConfiguration configuration = new DefaultConfiguration();

	configuration.clean();

	ConfigurationWrapper.setConfiguration(configuration);

	//
	//
	//

	SystemSetting systemSettings = ConfigurationWrapper.getSystemSettings();

	systemSettings.setName("Changed name");
	systemSettings.setDescription("Changed description");

	configuration.replace(systemSettings);

	//
	//
	//

	PropertiesMethod method = new PropertiesMethod();

	method.setProperties(//
		Setting.NAME, //
		Setting.DESCRIPTION //
	);

	CheckResponse check = method.check(configuration);

	Assert.assertEquals(CheckResult.CHECK_FAILED, check.getCheckResult());

	Assert.assertEquals(1, check.getSettings().size());

	Assert.assertEquals(SystemSetting.class, check.getSettings().get(0).getSettingClass());

	List<String> props = PropertiesMethod.getFailedProperties(check);

	Assert.assertEquals(2, props.size());

	Assert.assertEquals(Setting.DESCRIPTION.getName(), props.get(0));
	Assert.assertEquals(Setting.NAME.getName(), props.get(1));
    }

    @Test
    public void test3() {

	DefaultConfiguration configuration = new DefaultConfiguration();

	configuration.clean();

	ConfigurationWrapper.setConfiguration(configuration);

	//
	//
	//

	SystemSetting systemSettings = ConfigurationWrapper.getSystemSettings();

	//
	// in setting -> false
	// default -> true
	//
	systemSettings.setCanBeDisabled(true);

	//
	// in setting -> true
	// default -> true
	//
	systemSettings.setEnabled(false);

	//
	// in setting -> false
	// default -> true
	//
	systemSettings.enableCompactMode(true);

	//
	// in setting -> true
	// default -> true
	//
	systemSettings.setCanBeCleaned(false);

	//
	// in setting -> false
	// default -> false
	//
	systemSettings.setCanBeRemoved(true);

	//
	// in setting -> false
	// default -> false
	//
	systemSettings.enableFoldedMode(true);

	//
	// in setting -> true
	// default -> true
	//
	systemSettings.setShowHeader(false);

	//
	// in setting -> true
	// default -> true
	//
	systemSettings.setEditable(false);

	//
	// in setting -> false
	// default -> false
	//
	// this property cannot be tested since the new setting has the same selections
	// of the config setting
	//
	systemSettings.setSelected(true);

	//
	//
	//

	configuration.replace(systemSettings);

	//
	//
	//

	PropertiesMethod method = new PropertiesMethod();

	method.setProperties(//
		Setting.ENABLED, //
		Setting.CAN_BE_DISABLED, //
		Setting.COMPACT_MODE, //
		Setting.CAN_BE_CLEANED, //
		Setting.CAN_BE_REMOVED, //
		Setting.SELECTED, //
		Setting.FOLDED_MODE, //
		Setting.SHOW_HEADER, //
		Setting.VISIBLE, //
		Setting.EDITABLE

	);

	CheckResponse check = method.check(configuration);

	Assert.assertEquals(CheckResult.CHECK_FAILED, check.getCheckResult());

	Assert.assertEquals(1, check.getSettings().size());

	Assert.assertEquals(SystemSetting.class, check.getSettings().get(0).getSettingClass());

	List<String> props = PropertiesMethod.getFailedProperties(check);

	Assert.assertEquals(8, props.size());

	Assert.assertEquals(Setting.CAN_BE_CLEANED.getName(), props.get(0));

	Assert.assertEquals(Setting.CAN_BE_DISABLED.getName(), props.get(1));

	Assert.assertEquals(Setting.CAN_BE_REMOVED.getName(), props.get(2));

	Assert.assertEquals(Setting.COMPACT_MODE.getName(), props.get(3));

	Assert.assertEquals(Setting.EDITABLE.getName(), props.get(4));

	Assert.assertEquals(Setting.ENABLED.getName(), props.get(5));

	Assert.assertEquals(Setting.FOLDED_MODE.getName(), props.get(6));

	Assert.assertEquals(Setting.SHOW_HEADER.getName(), props.get(7));
    }

    @Test
    public void test4() {

	DefaultConfiguration configuration = new DefaultConfiguration();

	configuration.clean();

	ConfigurationWrapper.setConfiguration(configuration);

	//
	//
	//

	SystemSetting systemSettings = ConfigurationWrapper.getSystemSettings();

	//
	// in setting -> true
	// default -> true
	//
	// this property is propagated to the sub-settings
	//
	systemSettings.setVisible(false);

	//
	//
	//

	configuration.replace(systemSettings);

	//
	//
	//

	PropertiesMethod method = new PropertiesMethod();

	method.setProperties(//
		Setting.ENABLED, //
		Setting.CAN_BE_DISABLED, //
		Setting.COMPACT_MODE, //
		Setting.CAN_BE_CLEANED, //
		Setting.CAN_BE_REMOVED, //
		Setting.SELECTED, //
		Setting.FOLDED_MODE, //
		Setting.SHOW_HEADER, //
		Setting.VISIBLE, //
		Setting.EDITABLE);

	CheckResponse check = method.check(configuration);

	Assert.assertEquals(CheckResult.CHECK_FAILED, check.getCheckResult());

	List<Setting> list = new ArrayList<>();

	SettingUtils.deepFind(systemSettings, s -> list.add(s), list);

	List<Setting> distinct = list.stream().distinct().toList();

	//
	// due to the propagation, we expect SystemSetting ad all its sub-settings are in the list
	//
	Assert.assertEquals(distinct.size(), check.getSettings().size());

	Assert.assertEquals(SystemSetting.class, check.getSettings().get(0).getSettingClass());

	List<String> props = PropertiesMethod.getFailedProperties(check);

	Assert.assertEquals(1, props.size());

	Assert.assertEquals(Setting.VISIBLE.getName(), props.get(0));
    }

    @Test
    public void test5() {

	DefaultConfiguration configuration = new DefaultConfiguration();

	configuration.clean();

	ConfigurationWrapper.setConfiguration(configuration);

	//
	//
	//

	OAuthSetting setting = ConfigurationWrapper.getOAuthSetting();

	setting.setValidator((config, set, cont) -> null);

	setting.setAfterCleanFunction(s -> System.out.println());

	setting.setExtension(new ObjectExtension() {
	});

	setting.setConfigurableType("type");

	//
	//
	//

	configuration.replace(setting);

	//
	//
	//

	PropertiesMethod method = new PropertiesMethod();

	method.setProperties(//
		Setting.VALIDATOR, //
		Setting.AFTER_CLEAN_FUNCTION, //
		Setting.EXTENSION, //
		Setting.CONFIGURABLE_TYPE);

	CheckResponse check = method.check(configuration);

	Assert.assertEquals(CheckResult.CHECK_FAILED, check.getCheckResult());

	Assert.assertEquals(1, check.getSettings().size());

	Assert.assertEquals(OAuthSetting.class, check.getSettings().get(0).getSettingClass());

	List<String> props = PropertiesMethod.getFailedProperties(check);

	Assert.assertEquals(4, props.size());

	Assert.assertEquals(Setting.AFTER_CLEAN_FUNCTION.getName(), props.get(0));
	Assert.assertEquals(Setting.CONFIGURABLE_TYPE.getName(), props.get(1));
	Assert.assertEquals(Setting.EXTENSION.getName(), props.get(2));
	Assert.assertEquals(Setting.VALIDATOR.getName(), props.get(3));
    }

    @Test
    public void test6() {

	DefaultConfiguration configuration = new DefaultConfiguration();

	configuration.clean();

	ConfigurationWrapper.setConfiguration(configuration);

	//
	//
	//

	SystemSetting systemSettings = ConfigurationWrapper.getSystemSettings();

	//
	// the SelectionUtils.resetAndSelect method copy this value to the reset setting
	//
	systemSettings.getSetting("emailSetting").get().setEnabled(true);

	EmailSetting emailSetting = systemSettings.getEmailSetting().get();

	emailSetting.setName("Changed name");

	//
	//
	//

	configuration.replace(systemSettings);

	//
	//
	//

	PropertiesMethod method = new PropertiesMethod();

	method.setProperties(//
		Setting.ENABLED, //
		Setting.NAME);

	CheckResponse check = method.check(configuration);

	Assert.assertEquals(CheckResult.CHECK_FAILED, check.getCheckResult());

	Assert.assertEquals(1, check.getSettings().size());

	Assert.assertEquals(EmailSetting.class, check.getSettings().get(0).getSettingClass());

	List<String> props = PropertiesMethod.getFailedProperties(check);

	Assert.assertEquals(1, props.size());

	Assert.assertEquals(Setting.NAME.getName(), props.get(0));
    }

    @Test
    public void test7() {

	DefaultConfiguration configuration = new DefaultConfiguration();

	configuration.clean();

	ConfigurationWrapper.setConfiguration(configuration);

	//
	//
	//

	List<HarvestingSetting> harvestingSettings = ConfigurationWrapper.getHarvestingSettings();

	harvestingSettings.forEach(setting -> {

	    setting.getSelectedAccessorSetting().getGSSourceSetting().setDescription("Changed desc");

	    configuration.replace(setting);
	});

	//
	//
	//

	PropertiesMethod method = new PropertiesMethod();

	method.setProperties(Setting.DESCRIPTION);

	CheckResponse check = method.check(configuration);
	Assert.assertEquals(CheckResult.CHECK_FAILED, check.getCheckResult());

	Assert.assertEquals(harvestingSettings.size(), check.getSettings().size());

	for (Setting set : check.getSettings()) {

	    Assert.assertEquals(GSSourceSetting.class, set.getSettingClass());
	}

	List<String> props = PropertiesMethod.getFailedProperties(check);

	Assert.assertEquals(1, props.size());

	Assert.assertEquals(Setting.DESCRIPTION.getName(), props.get(0));
    }
}
