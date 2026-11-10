package eu.essi_lab.cfga.gs.test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Date;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;

import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.DefaultConfiguration;
import eu.essi_lab.cfga.gs.task.CustomTaskSetting;
import eu.essi_lab.cfga.scheduler.SchedulerUtils;
import eu.essi_lab.cfga.setting.SettingUtils;
import eu.essi_lab.configuration.ExecutionMode;

/**
 * In a production environment, a job fired after the last synchronization of the configuration is not canceled because
 * of a disabled scheduling or of different task options, since the setting of the job can be more recent
 *
 * @author boldrini
 */
public class IsJobCanceledSyncTest {

    @After
    public void after() {

	System.clearProperty(ExecutionMode.EXECUTION_MODE_KEY);
    }

    @Test
    public void test() throws Exception {

	System.setProperty(ExecutionMode.EXECUTION_MODE_KEY, ExecutionMode.AUGMENTER.getLabel());
	Assert.assertEquals(ExecutionMode.AUGMENTER, ExecutionMode.get());

	CustomTaskSetting setting = new CustomTaskSetting();
	setting.setTaskOptions("sourceId=source1");
	setting.getScheduling().setEnabled(true);

	DefaultConfiguration configuration = new DefaultConfiguration();
	configuration.clean();
	configuration.put(setting);

	ConfigurationWrapper.setConfiguration(configuration);

	long syncTime = configuration.getSyncTime();

	// the job is enabled with new options, but this configuration still has the previous version of the setting
	CustomTaskSetting jobSetting = SettingUtils.downCast(setting, CustomTaskSetting.class, true);
	jobSetting.setTaskOptions("sourceId=source2");

	setting.getScheduling().setEnabled(false);
	configuration.replace(setting);

	// job fired after the last synchronization: the configuration can be outdated, the job is not canceled
	JobExecutionContext newerJob = createContext(jobSetting, new Date(syncTime + 1000));
	Assert.assertFalse(ConfigurationWrapper.isJobCanceled(newerJob));

	// job fired before the last synchronization: the configuration is up to date, the job is canceled
	JobExecutionContext olderJob = createContext(jobSetting, new Date(syncTime - 1000));
	Assert.assertTrue(ConfigurationWrapper.isJobCanceled(olderJob));

	// enabled, but with different task options
	setting.getScheduling().setEnabled(true);
	configuration.replace(setting);

	Assert.assertFalse(ConfigurationWrapper.isJobCanceled(newerJob));
	Assert.assertTrue(ConfigurationWrapper.isJobCanceled(olderJob));

	// same task options
	setting.setTaskOptions("sourceId=source2");
	configuration.replace(setting);

	Assert.assertFalse(ConfigurationWrapper.isJobCanceled(newerJob));
	Assert.assertFalse(ConfigurationWrapper.isJobCanceled(olderJob));
    }

    private JobExecutionContext createContext(CustomTaskSetting setting, Date fireTime) {

	JobDetail jobDetail = SchedulerUtils.createJob(setting);

	JobExecutionContext context = mock(JobExecutionContext.class);
	when(context.getJobDetail()).thenReturn(jobDetail);
	when(context.getFireTime()).thenReturn(fireTime);

	return context;
    }
}
