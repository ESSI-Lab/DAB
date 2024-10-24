package eu.essi_lab.cfga.gs.setting.systemsetting.test;

import java.util.Properties;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.cfga.gs.setting.SystemSetting;
import eu.essi_lab.cfga.setting.SettingUtils;

/**
 * @author Fabrizio
 */
public class SystemSettingTest {

    @Test
    public void test() {

	SystemSetting setting = new SystemSetting();

	initTest(setting);
	initTest(new SystemSetting(setting.getObject()));
	initTest(new SystemSetting(setting.getObject().toString()));
	initTest(SettingUtils.downCast(setting, SystemSetting.class, true));

	setting.setProxyEndpoint("endpoint");

	test(setting);
	test(new SystemSetting(setting.getObject()));
	test(new SystemSetting(setting.getObject().toString()));
	test(SettingUtils.downCast(setting, SystemSetting.class, true));

	//
	//
	//

	boolean replaced1 = setting.putKeyValue("key1", "value1");
	Assert.assertFalse(replaced1);

	Properties properties = setting.getKeyValueOptions().get();

	Assert.assertEquals(1, properties.size());

	Assert.assertEquals("value1", properties.get("key1"));

	//
	//
	//

	boolean replaced2 = setting.putKeyValue("key2", "value2");
	Assert.assertFalse(replaced2);

	properties = setting.getKeyValueOptions().get();

	Assert.assertEquals(2, properties.size());

	Assert.assertEquals("value1", properties.get("key1"));
	Assert.assertEquals("value2", properties.get("key2"));
	
	//
	//

	boolean replaced3 = setting.putKeyValue("key2", "value5");
	Assert.assertTrue(replaced3);

	properties = setting.getKeyValueOptions().get();

	Assert.assertEquals(2, properties.size());

	Assert.assertEquals("value1", properties.get("key1"));
	Assert.assertEquals("value5", properties.get("key2"));
	
	//
	//

	boolean replaced4 = setting.putKeyValue("key1", "value9");
	Assert.assertTrue(replaced4);

	properties = setting.getKeyValueOptions().get();

	Assert.assertEquals(2, properties.size());

	Assert.assertEquals("value9", properties.get("key1"));
	Assert.assertEquals("value5", properties.get("key2"));
	
	//
	//
	//
	
	boolean replaced5 = setting.putKeyValue("key3", "abcd");
	boolean replaced6 = setting.putKeyValue("key4", "xyz");

	Assert.assertFalse(replaced5);
	Assert.assertFalse(replaced6);

	properties = setting.getKeyValueOptions().get();

	Assert.assertEquals(4, properties.size());

	Assert.assertEquals("value9", properties.get("key1"));
	Assert.assertEquals("value5", properties.get("key2"));
	Assert.assertEquals("abcd", properties.get("key3"));
	Assert.assertEquals("xyz", properties.get("key4"));

	//
	//
	//
	
	boolean replaced7 = setting.putKeyValue("key3", "1234");
 
	Assert.assertTrue(replaced7);
 
	properties = setting.getKeyValueOptions().get();

	Assert.assertEquals(4, properties.size());

	Assert.assertEquals("value9", properties.get("key1"));
	Assert.assertEquals("value5", properties.get("key2"));
	Assert.assertEquals("1234", properties.get("key3"));
	Assert.assertEquals("xyz", properties.get("key4"));
	
	//
	//
	//
	
	boolean replaced8 = setting.putKeyValue("key4", "xxx");
 
	Assert.assertTrue(replaced8);
 
	properties = setting.getKeyValueOptions().get();

	Assert.assertEquals(4, properties.size());

	Assert.assertEquals("value9", properties.get("key1"));
	Assert.assertEquals("value5", properties.get("key2"));
	Assert.assertEquals("1234", properties.get("key3"));
	Assert.assertEquals("xxx", properties.get("key4"));

	//
	//
	//
	
	setting.enableAugmentationReportMail(true);
	setting.enableErrorLogsReportEmail(true);
	setting.enableHarvestingReportEmail(true);
	
	Assert.assertTrue(setting.isAugmentationReportMailEnabled());
	Assert.assertTrue(setting.isErrorLogsReportEnabled());
	Assert.assertTrue(setting.isHarvestingReportMailEnabled());
	
	setting.enableAugmentationReportMail(false);
	setting.enableErrorLogsReportEmail(false);
	setting.enableHarvestingReportEmail(false);
	
	Assert.assertFalse(setting.isAugmentationReportMailEnabled());
	Assert.assertFalse(setting.isErrorLogsReportEnabled());
	Assert.assertFalse(setting.isHarvestingReportMailEnabled());	
    }

    /**
     * @param setting
     */
    private void test(SystemSetting setting) {

	Assert.assertEquals("endpoint", setting.getProxyEndpoint().get());
    }

    /**
     * @param setting
     */
    private void initTest(SystemSetting setting) {

	Assert.assertFalse(setting.getProxyEndpoint().isPresent());

	Assert.assertFalse(setting.isHarvestingReportMailEnabled());
	Assert.assertFalse(setting.isAugmentationReportMailEnabled());
	Assert.assertFalse(setting.isErrorLogsReportEnabled());

	Assert.assertFalse(setting.getEmailSetting().isPresent());

	Assert.assertFalse(setting.areStatisticsEnabled());
	Assert.assertFalse(setting.getStatisticsSetting().isPresent());

	Assert.assertFalse(setting.getKeyValueOptions().isPresent());
    }
}
