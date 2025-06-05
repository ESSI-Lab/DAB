package eu.essi_lab.cfga.gs.task;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2025 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import org.quartz.JobExecutionContext;

import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.ConfiguredGmailClient;
import eu.essi_lab.cfga.gs.TaskStarter;
import eu.essi_lab.cfga.gs.setting.EmailSetting;
import eu.essi_lab.cfga.gs.setting.SchedulerViewSetting;
import eu.essi_lab.cfga.scheduler.Scheduler;
import eu.essi_lab.cfga.scheduler.SchedulerFactory;
import eu.essi_lab.cfga.scheduler.SchedulerJobStatus;
import eu.essi_lab.cfga.scheduler.SchedulerWorker;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.exceptions.GSException;

/**
 * @author Fabrizio
 */
public class CustomTaskWorker extends SchedulerWorker<CustomTaskSetting> {

    /**
     * 
     */
    static final String CONFIGURABLE_TYPE = "CustomTaskWorker";

    @Override
    public String getType() {

	return CONFIGURABLE_TYPE;
    }

    @Override
    public void doJob(JobExecutionContext context, SchedulerJobStatus status) throws Exception {

	CustomTaskSetting setting = getSetting();

	String taskClassName = setting.getTaskClassName();

	@SuppressWarnings("unchecked")
	Class<CustomTask> taskClass = (Class<CustomTask>) Class.forName(taskClassName);

	CustomTask task = taskClass.newInstance();

	String taskName = task.getName();

	task.doJob(context, status);

	Optional<EmailSetting> optEmailSetting = ConfigurationWrapper.getSystemSettings().getEmailSetting();

	if (optEmailSetting.isPresent()) {

	    List<String> recipients = setting.getEmailRecipients();

	    if (!recipients.isEmpty()) {

		String message = TaskStarter.formatStatus(status);

		String[] recArray = recipients.toArray(new String[] {});

		String subject = ConfiguredGmailClient.MAIL_REPORT_SUBJECT + "[CUSTOM TASK][" + taskName + "]" + "[COMPLETED]";

		ConfiguredGmailClient.sendEmail(subject, message, recArray);
	    }
	} else {

	    GSLoggerFactory.getLogger(getClass()).warn("Unable to send email, system email settings not configured");
	}

	if (task.clearMessagesBeforeStoreStatus()) {

	    status.clearMessages();
	}
    }

    @Override
    protected CustomTaskSetting initSetting() {

	return new CustomTaskSetting();
    }

    @Override
    protected void storeJobStatus(SchedulerJobStatus status) throws GSException {

	SchedulerViewSetting setting = ConfigurationWrapper.getSchedulerSetting();

	Scheduler scheduler = SchedulerFactory.getScheduler(setting);

	try {
	    scheduler.setJobStatus(status);

	} catch (SQLException e) {

	    GSLoggerFactory.getLogger(getClass()).error("Unable to store status");
	    GSLoggerFactory.getLogger(getClass()).error(status.toString());
	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}
    }
}
