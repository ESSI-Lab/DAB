package eu.essi_lab.cfga.scheduler.test;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;
import org.quartz.Calendar;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.quartz.impl.JobDetailImpl;

import eu.essi_lab.cfga.scheduler.SchedulerJobStatus;
import eu.essi_lab.cfga.setting.scheduling.SchedulerWorkerSetting.SchedulingGroup;
import eu.essi_lab.lib.utils.LabeledEnum;
import eu.essi_lab.messages.JobStatus.JobPhase;

/**
 * @author Fabrizio
 */
public class SchedulerJobStatusTest {

    @Test
    public void test2() throws InterruptedException {

	SchedulerJobStatus jobStatus = new SchedulerJobStatus(createContext(false), "settingId", "workerClassName");

	for (int i = 0; i < 10; i++) {

	    jobStatus.addInfoMessage("Info A - " + i);
	}

	Thread.sleep(TimeUnit.SECONDS.toMillis(2));

	for (int i = 0; i < 10; i++) {

	    jobStatus.addInfoMessage("Info B - " + i);
	}

	Thread.sleep(TimeUnit.SECONDS.toMillis(2));

	for (int i = 0; i < 10; i++) {

	    jobStatus.addInfoMessage("Info C - " + i);
	}

	//
	//
	//

	Thread.sleep(TimeUnit.SECONDS.toMillis(2));

	for (int i = 0; i < 10; i++) {

	    jobStatus.addWarningMessage("Warn A - " + i);
	}

	Thread.sleep(TimeUnit.SECONDS.toMillis(2));

	for (int i = 0; i < 10; i++) {

	    jobStatus.addWarningMessage("Warn B - " + i);
	}

	//
	//
	//

	for (int i = 0; i < 10; i++) {

	    jobStatus.addErrorMessage("Error A - " + i);
	}

	System.out.println(jobStatus);

	//
	//
	//

	List<String> messagesList = jobStatus.getMessagesList();

	Assert.assertEquals(60, messagesList.size());

	List<String> infoMsgList = jobStatus.getInfoMessages();

	Assert.assertEquals(30, infoMsgList.size());

	List<String> warnMsgList = jobStatus.getWarningMessages();

	Assert.assertEquals(20, warnMsgList.size());

	List<String> errorMsgList = jobStatus.getErrorMessages();

	Assert.assertEquals(10, errorMsgList.size());

	//
	//
	//

	jobStatus.clearMessages();

	Assert.assertTrue(jobStatus.getMessagesList().isEmpty());

    }

    @Test
    public void test() {

	SchedulerJobStatus jobStatus = new SchedulerJobStatus(createContext(false), "settingId", "workerClassName");

	initTest(jobStatus);

	initTest(new SchedulerJobStatus(jobStatus.getObject()));

	//
	//
	//

	jobStatus = new SchedulerJobStatus(createContext(true), "settingId", "workerClassName");

	jobStatus.setDataUri("dataUri");
	jobStatus.setEndTime();
	jobStatus.setErrorPhase();

	jobStatus.addErrorMessage("error");
	jobStatus.addWarningMessage("warning");
	jobStatus.addInfoMessage("info");

	jobStatus.setSize(123456789);

	test(jobStatus);

	test(new SchedulerJobStatus(jobStatus.getObject()));

	//
	//
	//

	jobStatus.clearMessages();

	Assert.assertTrue(jobStatus.getErrorMessages().isEmpty());

	Assert.assertTrue(jobStatus.getInfoMessages().isEmpty());

	Assert.assertTrue(jobStatus.getWarningMessages().isEmpty());
    }

    /**
     * @param context
     * @param jobStatus
     */
    private void test(SchedulerJobStatus jobStatus) {

	Assert.assertEquals(JobPhase.ERROR, jobStatus.getPhase());

	Assert.assertEquals("settingId", jobStatus.getSettingId());

	Assert.assertEquals("jobKey", jobStatus.getJobIdentifier());

	Assert.assertEquals("workerClassName", jobStatus.getWorkerClassName());

	Assert.assertEquals(SchedulingGroup.HARVESTING, LabeledEnum.valueOf(SchedulingGroup.class, jobStatus.getJobGroup()).get());

	Assert.assertTrue(jobStatus.isRecovering());

	Assert.assertTrue(jobStatus.getStartTime().isPresent());

	Assert.assertTrue(jobStatus.getEndTime().isPresent());

	Assert.assertEquals("dataUri", jobStatus.getDataUri().get());

	Assert.assertFalse(jobStatus.getErrorMessages().isEmpty());

	Assert.assertFalse(jobStatus.getInfoMessages().isEmpty());

	Assert.assertFalse(jobStatus.getWarningMessages().isEmpty());

	Assert.assertFalse(jobStatus.getJoinedMessages().isEmpty());

	Assert.assertEquals("123.456.789", jobStatus.getSize().get());
    }

    /**
     * @param context
     * @param jobStatus
     */
    private void initTest(SchedulerJobStatus jobStatus) {

	Assert.assertEquals(JobPhase.RUNNING, jobStatus.getPhase());

	Assert.assertEquals("settingId", jobStatus.getSettingId());

	Assert.assertEquals("jobKey", jobStatus.getJobIdentifier());

	Assert.assertEquals("workerClassName", jobStatus.getWorkerClassName());

	Assert.assertEquals(SchedulingGroup.HARVESTING, LabeledEnum.valueOf(SchedulingGroup.class, jobStatus.getJobGroup()).get());

	Assert.assertFalse(jobStatus.isRecovering());

	Assert.assertTrue(jobStatus.getStartTime().isPresent());

	Assert.assertFalse(jobStatus.getEndTime().isPresent());

	Assert.assertFalse(jobStatus.getDataUri().isPresent());

	Assert.assertTrue(jobStatus.getErrorMessages().isEmpty());

	Assert.assertTrue(jobStatus.getInfoMessages().isEmpty());

	Assert.assertTrue(jobStatus.getWarningMessages().isEmpty());

	Assert.assertTrue(jobStatus.getJoinedMessages().isEmpty());

	Assert.assertFalse(jobStatus.getSize().isPresent());
    }

    /**
     * @param recovering
     * @return
     */
    private static JobExecutionContext createContext(boolean recovering) {

	return new JobExecutionContext() {

	    @Override
	    public void setResult(Object result) {
	    }

	    @Override
	    public void put(Object key, Object value) {
	    }

	    @Override
	    public boolean isRecovering() {

		return recovering;
	    }

	    @Override
	    public Trigger getTrigger() {

		return null;
	    }

	    @Override
	    public Scheduler getScheduler() {

		return null;
	    }

	    @Override
	    public Date getScheduledFireTime() {

		return null;
	    }

	    @Override
	    public Object getResult() {

		return null;
	    }

	    @Override
	    public int getRefireCount() {

		return 0;
	    }

	    @Override
	    public TriggerKey getRecoveringTriggerKey() throws IllegalStateException {

		return null;
	    }

	    @Override
	    public Date getPreviousFireTime() {

		return null;
	    }

	    @Override
	    public Date getNextFireTime() {

		return null;
	    }

	    @Override
	    public JobDataMap getMergedJobDataMap() {

		return null;
	    }

	    @Override
	    public long getJobRunTime() {

		return 0;
	    }

	    @Override
	    public Job getJobInstance() {

		return null;
	    }

	    @Override
	    public JobDetail getJobDetail() {

		JobDetailImpl jobDetailImpl = new JobDetailImpl();
		jobDetailImpl.setKey(new JobKey("jobKey", SchedulingGroup.HARVESTING.getLabel()));

		return jobDetailImpl;
	    }

	    @Override
	    public Date getFireTime() {

		return null;
	    }

	    @Override
	    public String getFireInstanceId() {

		return null;
	    }

	    @Override
	    public Calendar getCalendar() {

		return null;
	    }

	    @Override
	    public Object get(Object key) {

		return null;
	    }
	};
    }

}
