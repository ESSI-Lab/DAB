package eu.essi_lab.cfga.gs.setting.emailsetting.test;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.cfga.gs.setting.EmailSetting;
import eu.essi_lab.cfga.setting.SettingUtils;

/**
 * @author Fabrizio
 */
public class EmailSettingTest {

    @Test
    public void test() {

	EmailSetting setting = new EmailSetting();

	initTest(setting);
	initTest(new EmailSetting(setting.getObject()));
	initTest(new EmailSetting(setting.getObject().toString()));
	initTest(SettingUtils.downCast(setting, EmailSetting.class, true));

	setting.setSMTPHost("host");
	setting.setSMTPPort(1);
	setting.setSMTPUser("user");
	setting.setSMTPPassword("password");
	setting.setRecipients("r1", "r2", "r3");

	test(setting);
	test(new EmailSetting(setting.getObject()));
	test(new EmailSetting(setting.getObject().toString()));
	test(SettingUtils.downCast(setting, EmailSetting.class, true));

	setting.setRecipients("r1");

	Assert.assertEquals(1, setting.getRecipients().size());
	Assert.assertEquals("r1", setting.getRecipients().get(0));
    }

    /**
     * @param setting
     */
    private void test(EmailSetting setting) {

	Assert.assertEquals("host", setting.getSMTPHost().get());
	Assert.assertEquals(new Integer(1), setting.getSMTPPort().get());
	Assert.assertEquals("user", setting.getSMTPUser().get());
	Assert.assertEquals("password", setting.getSMTPPassword().get());
	List<String> recipients = setting.getRecipients();
	Assert.assertEquals(3, recipients.size());
	recipients.sort(String::compareTo);

	Assert.assertEquals("r1", recipients.get(0));
	Assert.assertEquals("r2", recipients.get(1));
	Assert.assertEquals("r3", recipients.get(2));
    }

    /**
     * @param setting
     */
    private void initTest(EmailSetting setting) {

	Assert.assertFalse(setting.getSMTPHost().isPresent());
	Assert.assertFalse(setting.getSMTPPort().isPresent());
	Assert.assertFalse(setting.getSMTPUser().isPresent());
	Assert.assertFalse(setting.getSMTPPassword().isPresent());
	Assert.assertTrue(setting.getRecipients().isEmpty());
    }

}
