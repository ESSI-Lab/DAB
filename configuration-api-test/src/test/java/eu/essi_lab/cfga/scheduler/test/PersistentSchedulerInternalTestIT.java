package eu.essi_lab.cfga.scheduler.test;

import java.sql.SQLException;

import org.junit.Before;
import org.junit.Test;
import org.quartz.SchedulerException;

import eu.essi_lab.cfga.SelectionUtils;
import eu.essi_lab.cfga.scheduler.Scheduler;
import eu.essi_lab.cfga.scheduler.SchedulerFactory;
import eu.essi_lab.cfga.scheduler.impl.QuartzDB_Initializer;
import eu.essi_lab.cfga.setting.scheduling.SchedulerSetting;
import eu.essi_lab.cfga.setting.scheduling.SchedulerSetting.JobStoreType;
import eu.essi_lab.lib.utils.GSLoggerFactory;

/**
 * @author Fabrizio
 */
public class PersistentSchedulerInternalTestIT extends SchedulerTest {

    /**
     * 
     */
    private static Scheduler scheduler;

    static {

	QuartzDB_Initializer initializer = new QuartzDB_Initializer(createSetting());

	try {

	    boolean dbExists = initializer.dbExists();

	    if (!dbExists) {

		GSLoggerFactory.getLogger(PersistentSchedulerInternalTestIT.class).info("DB creation STARTED");

		initializer.createDb();

		GSLoggerFactory.getLogger(PersistentSchedulerInternalTestIT.class).info("DB creation ENDED");
	    }

	    GSLoggerFactory.getLogger(PersistentSchedulerInternalTestIT.class).info("Tables initialization STARTED");

	    initializer.initializeTables(true);

	    GSLoggerFactory.getLogger(PersistentSchedulerInternalTestIT.class).info("Tables initialization ENDED");

	} catch (SQLException e) {

	    e.printStackTrace();
	    GSLoggerFactory.getLogger(PersistentSchedulerInternalTestIT.class).error(e.getMessage(), e);
	}
    }

    /**
     * @return
     */
    private static SchedulerSetting createSetting() {

	SchedulerSetting setting = new SchedulerSetting();

	setting.setJobStoreType(JobStoreType.PERSISTENT);
	setting.setUserDateTimeZone("UTC");

	setting.setSQLDatabaseName("quartzJobStore");
	setting.setSQLDatabaseUri(System.getProperty("mysql.host"));
//	setting.setSQLDatabaseUri("jdbc:mysql://localhost:3306");
	setting.setSQLDatabaseUser("root");
	setting.setSQLDatabasePassword("pdw");

	setting.debugSQLSettings();

	return setting;
    }

    @Before
    public void before() throws Exception {

	super.before();
    }

    /**
     * @return
     * @throws SchedulerException
     * @throws SQLException
     */
    protected Scheduler getScheduler() throws SchedulerException, SQLException {

	if (scheduler == null) {

	    SchedulerSetting setting = createSetting();

	    SelectionUtils.deepClean(setting);

	    scheduler = SchedulerFactory.getPersistentScheduler(setting);
	    scheduler.start();
	}

	return scheduler;
    }

    @Test
    public void multipleScheduledSettingsTest() throws Exception {
	super.multipleScheduledSettingsTest();
    }

    @Test
    public void runOnceTest() throws Exception {
	super.runOnceTest();
    }

    @Test
    public void repeatCountTest() throws Exception {
	super.repeatCountTest();
    }

    @Test
    public void startTimeTest() throws Exception {
	super.startTimeTest();
    }

    @Test
    public void rescheduleTest() throws Exception {
	super.rescheduleTest();
    }

    @Test
    public void endTimeTest() throws Exception {
	super.endTimeTest();
    }

    @Test
    public void unscheduleTest() throws Exception {
	super.unscheduleTest();
    }
}
