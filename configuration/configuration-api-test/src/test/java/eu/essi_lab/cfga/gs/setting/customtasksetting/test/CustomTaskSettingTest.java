package eu.essi_lab.cfga.gs.setting.customtasksetting.test;

import java.util.List;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.DefaultConfiguration;
import eu.essi_lab.cfga.gs.task.CustomTaskSetting;
import eu.essi_lab.cfga.gs.task.DefaultCustomTask;
import eu.essi_lab.cfga.setting.SettingUtils;
import eu.essi_lab.cfga.setting.validation.ValidationContext;
import eu.essi_lab.cfga.setting.validation.ValidationResponse;

/**
 * @author Fabrizio
 */
public class CustomTaskSettingTest {

    @Test
    public void validationTest() {

	DefaultConfiguration defaultConfiguration = new DefaultConfiguration();

	ConfigurationWrapper.setConfiguration(defaultConfiguration);

	CustomTaskSetting customTaskSetting = ConfigurationWrapper.getCustomTaskSettings().get(0);

	//
	//
	//

	customTaskSetting.setTaskDescription("");

	Optional<ValidationResponse> validationResponse = customTaskSetting.validate(defaultConfiguration, ValidationContext.put());

	List<String> errors = validationResponse.get().getErrors();

	// task description missing
	Assert.assertEquals(1, errors.size());

	//
	//
	//
	//

	customTaskSetting.selectTaskName("abc");

	validationResponse = customTaskSetting.validate(defaultConfiguration, ValidationContext.put());

	errors = validationResponse.get().getErrors();

	// task name not valid
	Assert.assertEquals(2, errors.size());

	//
	//
	//

	customTaskSetting.setTaskDescription("Description");

	customTaskSetting.selectTaskName(DefaultCustomTask.getTaskName());

	validationResponse = customTaskSetting.validate(defaultConfiguration, ValidationContext.put());

	errors = validationResponse.get().getErrors();

	Assert.assertEquals(0, errors.size());

	//
	//
	//

	customTaskSetting.selectTaskName("");

	validationResponse = customTaskSetting.validate(defaultConfiguration, ValidationContext.put());

	errors = validationResponse.get().getErrors();

	// task name empty
	Assert.assertEquals(1, errors.size());
    }

    @Test
    public void test() {

	CustomTaskSetting setting = new CustomTaskSetting();

	initTest(setting);
	initTest(new CustomTaskSetting(setting.getObject()));
	initTest(new CustomTaskSetting(setting.getObject().toString()));
	initTest(SettingUtils.downCast(setting, CustomTaskSetting.class, true));

	setting.selectTaskName(DefaultCustomTask.getTaskName());
	setting.setTaskOptions("Options");
	setting.setEmailRecipients("   r3    ", "  r1 ", " r2   ");
	setting.setTaskDescription("Description");

	test(setting);
	test(new CustomTaskSetting(setting.getObject()));
	test(new CustomTaskSetting(setting.getObject().toString()));
	test(SettingUtils.downCast(setting, CustomTaskSetting.class, true));

	setting.getOption("emailRecipients", String.class).get().setValue("   r1,r2\n   ,   r3\n\n   ");

	test(setting);
	test(new CustomTaskSetting(setting.getObject()));
	test(new CustomTaskSetting(setting.getObject().toString()));
	test(SettingUtils.downCast(setting, CustomTaskSetting.class, true));
    }

    /**
     * @param setting
     */
    private void test(CustomTaskSetting setting) {

	Assert.assertEquals(DefaultCustomTask.getTaskName(), setting.getSelectedTaskName());
	Assert.assertEquals(DefaultCustomTask.class.getCanonicalName(), setting.getTaskClassName());
	Assert.assertEquals("Description", setting.getTaskDescription());

	Assert.assertEquals("Options", setting.getTaskOptions().get());

	List<String> emailRecipients = setting.getEmailRecipients();
	emailRecipients.sort(String::compareTo);
	Assert.assertEquals(3, emailRecipients.size());
	Assert.assertEquals("r1", emailRecipients.get(0));
	Assert.assertEquals("r2", emailRecipients.get(1));
	Assert.assertEquals("r3", emailRecipients.get(2));
    }

    /**
     * @param setting
     */
    private void initTest(CustomTaskSetting setting) {

	Assert.assertEquals(DefaultCustomTask.class.getCanonicalName(), setting.getTaskClassName());
	Assert.assertEquals(DefaultCustomTask.getTaskName(), setting.getSelectedTaskName());
	Assert.assertFalse(setting.getTaskOptions().isPresent());
	Assert.assertTrue(setting.getEmailRecipients().isEmpty());
	Assert.assertEquals("No description provided", setting.getTaskDescription());

    }
}
