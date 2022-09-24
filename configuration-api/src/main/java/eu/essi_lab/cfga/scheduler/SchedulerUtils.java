package eu.essi_lab.cfga.scheduler;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2022 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.util.Optional;

import org.json.JSONObject;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;

import eu.essi_lab.cfga.setting.Setting;
import eu.essi_lab.cfga.setting.scheduling.SchedulerWorkerSetting;
import eu.essi_lab.cfga.setting.scheduling.SchedulerWorkerSetting.SchedulingGroup;

public class SchedulerUtils {

    /**
     * @param setting
     * @return
     */
    public static JobDetail createJob(SchedulerWorkerSetting setting) {

	JobKey jobKey = SchedulerUtils.createJobKey(setting);

	return JobBuilder.newJob(SchedulerJob.class).//
		withIdentity(jobKey).//
		requestRecovery(true).//
		usingJobData("setting", setting.toString()).//
		build();

    }

    /**
     * @param setting
     * @return
     */
    public static JobKey createJobKey(SchedulerWorkerSetting setting) {

	return new JobKey(createJobKeyName(setting), createJobKeyGroup(setting.getGroup()));
    }

    /**
     * @param setting
     * @return
     */
    public static String createJobKeyName(Setting setting) {

	return createJobKeyName(setting.getIdentifier());
    }

    /**
     * @param identifier
     * @return
     */
    public static String createJobKeyName(String identifier) {

	return "job_id_" + identifier;
    }

    /**
     * @param jobKeyName
     * @return
     */
    public static String retrieveSettingIdentifier(String jobKeyName) {

	return jobKeyName.replace("job_id_", "");
    }

    /**
     * @param schedulingGroup
     * @return
     */
    public static String createJobKeyGroup(SchedulingGroup schedulingGroup) {

	return "job_group_" + schedulingGroup.getLabel();
    }

    /**
     * @param setting
     * @param jobDetail
     * @return
     */
    public static TriggerBuilder<Trigger> createTriggerBuilder(SchedulerWorkerSetting setting, JobDetail jobDetail) {

	TriggerKey triggerKey = createTriggerKey(setting);

	return TriggerBuilder.newTrigger().//
		forJob(jobDetail).//
		usingJobData("setting", setting.toString()).//
		withIdentity(triggerKey);
    }

    /**
     * @param setting
     * @return
     */
    public static TriggerKey createTriggerKey(SchedulerWorkerSetting setting) {

	String id = setting.getIdentifier();
	SchedulingGroup schedulingGroup = setting.getGroup();

	return new TriggerKey("trigger_id_" + id, "trigger_group_" + schedulingGroup.getLabel());
    }

    /**
     * @param context
     * @return
     */
    public static SchedulerWorkerSetting getSetting(JobExecutionContext context) {

	return getSetting(context.//
		getJobDetail().//
		getJobDataMap());
    }

    /**
     * @param jobDataMap
     * @return
     */
    public static SchedulerWorkerSetting getSetting(JobDataMap jobDataMap) {

	return new SchedulerWorkerSetting(//
		jobDataMap.//
			getString("setting"));
    }

  /**
   * 
   * @param context
   * @param status
   */
    public static void putStatus(JobExecutionContext context, SchedulerJobStatus status){
	
	context.put("jobStatus", status.getObject());
    }

    /**
     * 
     * @param context
     * @return
     */
    public static Optional<SchedulerJobStatus> readStatus(JobExecutionContext context){
		
	Object object = context.get("jobStatus");
	
	if(object != null){
	    
	    return Optional.of(new SchedulerJobStatus(new JSONObject(object.toString())));
	}
	
	return Optional.empty();
    }

}
