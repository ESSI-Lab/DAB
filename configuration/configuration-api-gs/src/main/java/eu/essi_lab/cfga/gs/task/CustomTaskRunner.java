package eu.essi_lab.cfga.gs.task;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import java.util.Date;

import org.json.JSONObject;
import org.quartz.Calendar;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.Scheduler;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.quartz.impl.JobDetailImpl;

import eu.essi_lab.cfga.scheduler.SchedulerJobStatus;

/**
 * @author Fabrizio
 */
public class CustomTaskRunner {

    private final CustomTask task;
    private final JobExecutionContext context;

    /**
     * @param task
     * @param customTaskOptions
     */
    private CustomTaskRunner(CustomTask task, String customTaskOptions) {

	this.task = task;
	this.context = new JobExecutionContext() {

	    @Override
	    public void setResult(Object result) {
	    }

	    @Override
	    public void put(Object key, Object value) {
	    }

	    @Override
	    public boolean isRecovering() {

		return false;
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

		JobDetail ret = new JobDetailImpl();
		JobDataMap datamap = ret.getJobDataMap();

		CustomTaskSetting sws = new CustomTaskSetting();

		sws.setTaskOptions(customTaskOptions);

		datamap.put("setting", sws.getObject().toString());

		return ret;
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

    /**
     * @param task
     * @return
     */
    public static CustomTaskRunner get(CustomTask task, String customTaskOptions) {

	return new CustomTaskRunner(task, customTaskOptions);
    }

    /**
     * @param task
     * @param context
     * @throws Exception
     */
    public void run() throws Exception {

	task.doJob(context, new SchedulerJobStatus(new JSONObject()));
    }
}
