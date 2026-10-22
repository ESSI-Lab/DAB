package eu.essi_lab.cfga.gs.test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Assert;
import org.junit.Test;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;

import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.DefaultConfiguration;
import eu.essi_lab.cfga.gs.task.CustomTaskSetting;
import eu.essi_lab.cfga.scheduler.SchedulerUtils;

/**
 * @author boldrini
 */
public class IsJobCanceledTest {

    @Test
    public void test() throws Exception {

	CustomTaskSetting setting = new CustomTaskSetting();
	setting.getScheduling().setEnabled(true);

	DefaultConfiguration configuration = new DefaultConfiguration();
	configuration.clean();
	configuration.put(setting);

	ConfigurationWrapper.setConfiguration(configuration);

	JobExecutionContext context = createContext(setting);

	Assert.assertEquals(setting.getIdentifier(), SchedulerUtils.getSettingIdentifier(context));

	// enabled
	Assert.assertFalse(ConfigurationWrapper.isJobCanceled(context));

	// disabled
	setting.getScheduling().setEnabled(false);
	configuration.replace(setting);
	Assert.assertTrue(ConfigurationWrapper.isJobCanceled(context));

	// enabled again
	setting.getScheduling().setEnabled(true);
	configuration.replace(setting);
	Assert.assertFalse(ConfigurationWrapper.isJobCanceled(context));

	// removed (in the default MIXED execution mode, the removal is immediately detected)
	configuration.remove(setting.getIdentifier());
	Assert.assertTrue(ConfigurationWrapper.isJobCanceled(context));
    }

    private JobExecutionContext createContext(CustomTaskSetting setting) {

	JobDetail jobDetail = SchedulerUtils.createJob(setting);

	JobExecutionContext context = mock(JobExecutionContext.class);
	when(context.getJobDetail()).thenReturn(jobDetail);

	return context;
    }
}
