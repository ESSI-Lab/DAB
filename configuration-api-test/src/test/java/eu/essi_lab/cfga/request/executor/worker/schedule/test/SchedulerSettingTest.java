/**
 * 
 */
package eu.essi_lab.cfga.request.executor.worker.schedule.test;

import java.util.Date;
import java.util.Set;
import java.util.TimeZone;

import org.joda.time.DateTimeZone;
import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.cfga.Selectable.SelectionMode;
import eu.essi_lab.cfga.SelectionUtils;
import eu.essi_lab.cfga.setting.SettingUtils;
import eu.essi_lab.cfga.setting.scheduling.SchedulerSetting;
import eu.essi_lab.cfga.setting.scheduling.SchedulerSetting.JobStoreType;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;

/**
 * @author Fabrizio
 */
public class SchedulerSettingTest {

    @Test
    public void test() {

	SchedulerSetting setting = new SchedulerSetting();

	initTest(setting);
	initTest(new SchedulerSetting(setting.getObject()));
	initTest(new SchedulerSetting(setting.getObject().toString()));
	initTest(SettingUtils.downCast(setting, SchedulerSetting.class, true));

	setting.setUserDateTimeZone("US/Pacific");

	test1(setting);
	test1(new SchedulerSetting(setting.getObject()));
	test1(new SchedulerSetting(setting.getObject().toString()));
	test1(SettingUtils.downCast(setting, SchedulerSetting.class, true));

	try {
	    setting.setSQLDatabaseUri("url");
	    Assert.fail("Exception not thrown");
	} catch (RuntimeException ex) {
	}

	try {
	    setting.setSQLDatabaseName("name");
	    Assert.fail("Exception not thrown");
	} catch (RuntimeException ex) {
	}

	try {
	    setting.setSQLDatabasePassword("password");
	    Assert.fail("Exception not thrown");
	} catch (RuntimeException ex) {
	}

	try {
	    setting.setSQLDatabaseUser("user");
	    Assert.fail("Exception not thrown");
	} catch (RuntimeException ex) {
	}

	//
	// persistent test
	//

	setting.setJobStoreType(JobStoreType.PERSISTENT);
	setting.setSQLDatabaseUri("uri");
	setting.setSQLDatabaseName("name");
	setting.setSQLDatabasePassword("password");
	setting.setSQLDatabaseUser("user");

	test2(setting);
	test2(new SchedulerSetting(setting.getObject()));
	test2(new SchedulerSetting(setting.getObject().toString()));
	test2(SettingUtils.downCast(setting, SchedulerSetting.class, true));

	//
	// persistent test after clean
	//
	SelectionUtils.deepClean(setting);

	test2(setting);
	test2(new SchedulerSetting(setting.getObject()));
	test2(new SchedulerSetting(setting.getObject().toString()));
	test2(SettingUtils.downCast(setting, SchedulerSetting.class, true));

    }

    /**
     * @param setting
     */
    private void initTest(SchedulerSetting setting) {

	Assert.assertFalse(setting.canBeCleaned());

	Assert.assertFalse(setting.isCompactModeEnabled());

	Assert.assertEquals(SelectionMode.SINGLE, setting.getSelectionMode());

	try {
	    setting.getConfigurableType();
	    Assert.fail("Exception not thrown");
	} catch (RuntimeException ex) {

	}

	JobStoreType jobStoreType = setting.getJobStoreType();
	Assert.assertEquals(JobStoreType.VOLATILE, jobStoreType);

	Assert.assertNull(setting.getSQLDatabaseUri());
	Assert.assertNull(setting.getSQLDatabaseName());
	Assert.assertNull(setting.getSQLDatabasePassword());
	Assert.assertNull(setting.getSQLDatabaseUser());

	DateTimeZone userDateTimeZone = setting.getUserDateTimeZone();
	Assert.assertEquals(DateTimeZone.forID("Europe/Berlin"), userDateTimeZone);
    }

    /**
     * @param setting
     */
    private void test2(SchedulerSetting setting) {

	Assert.assertEquals(JobStoreType.PERSISTENT, setting.getJobStoreType());

	Assert.assertEquals("uri", setting.getSQLDatabaseUri());
	Assert.assertEquals("name", setting.getSQLDatabaseName());
	Assert.assertEquals("password", setting.getSQLDatabasePassword());
	Assert.assertEquals("user", setting.getSQLDatabaseUser());

    }

    /**
     * @param setting
     */
    private void test1(SchedulerSetting setting) {

	DateTimeZone userDateTimeZone = setting.getUserDateTimeZone();
	Assert.assertEquals(DateTimeZone.forID("US/Pacific"), userDateTimeZone);

    }

    @Test
    public void toGMTDateTest() {

	DateTimeZone defaultDateTimeZone = DateTimeZone.forID("Europe/Berlin");

	Set<String> availableIDs = DateTimeZone.getAvailableIDs();
	for (String id : availableIDs) {
	    System.out.println(id);
	}

	TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

	String dateTime = "2021-09-14T10:00:00";

	Date date = ISO8601DateTimeUtils.toGMTDateTime(dateTime, defaultDateTimeZone);

	String string = date.toString();

	Assert.assertEquals("Tue Sep 14 08:00:00 UTC 2021", string);
    }
}
