package eu.essi_lab.gssrv.conf.task;

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

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.sql.Blob;
import java.sql.ResultSet;

import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;

import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.setting.SchedulerViewSetting;
import eu.essi_lab.cfga.gs.task.AbstractCustomTask;
import eu.essi_lab.cfga.scheduler.SchedulerJobStatus;
import eu.essi_lab.cfga.scheduler.impl.MySQLConnectionManager;
import eu.essi_lab.cfga.setting.scheduling.SchedulerSetting.JobStoreType;
import eu.essi_lab.cfga.setting.scheduling.SchedulerWorkerSetting;

/**
 * @author Fabrizio
 */
public class CorruptedBlobTriggersCleanerTask extends AbstractCustomTask {

    @Override
    public String getName() {

	return "Corrupted blob triggers cleaner";
    }

    @Override
    public void doJob(JobExecutionContext context, SchedulerJobStatus status) throws Exception {

	SchedulerViewSetting schedulerSetting = ConfigurationWrapper.getSchedulerSetting();
	if (schedulerSetting.getJobStoreType() == JobStoreType.VOLATILE) {

	    status.addWarningMessage("Scheduler not persistent, exit");
	    return;
	}

	status.addInfoMessage("Corrupted blob triggers cleaner STARTED");

	MySQLConnectionManager manager = new MySQLConnectionManager();

	manager.setUser(schedulerSetting.getSQLDatabaseUser());
	manager.setPwd(schedulerSetting.getSQLDatabasePassword());
	manager.setDbName(schedulerSetting.getSQLDatabaseName());
	manager.setDbUri(schedulerSetting.getSQLDatabaseUri());
	manager.setUseSSl(false);

	String sql = "select JOB_DATA, TRIGGER_NAME, JOB_NAME, JOB_GROUP from GS_QRTZ_TRIGGERS";

	ResultSet rs = manager.execQuery(sql);

	int count = 0;

	while (rs.next()) {

	    String triggerName = rs.getString("TRIGGER_NAME");
	    String jobName = rs.getString("JOB_NAME");
	    String jobGroup = rs.getString("JOB_GROUP");

	    Blob blob = rs.getBlob("JOB_DATA");

	    InputStream binaryStream = blob.getBinaryStream();

	    try {

		ObjectInputStream in = new ObjectInputStream(binaryStream);

		JobDataMap map = (JobDataMap) in.readObject();

		new SchedulerWorkerSetting(map.getString("setting"));

	    } catch (Exception ex) {

		count++;

		status.addInfoMessage("Deleting STARTED");

		status.addInfoMessage("Trigger: " + triggerName);
		status.addInfoMessage("Job    : " + jobName);
		status.addInfoMessage("Group  : " + jobGroup);

		manager.execUpdate("DELETE FROM GS_QRTZ_SIMPLE_TRIGGERS WHERE TRIGGER_NAME='" + triggerName + "';");
		manager.execUpdate("DELETE FROM GS_QRTZ_TRIGGERS WHERE TRIGGER_NAME='" + triggerName + "';");

		status.addInfoMessage("Deleting ENDED");
	    }
	}

	status.addInfoMessage("Removed " + count + " corrupted triggers");

	status.addInfoMessage("Corrupted blob triggers cleaner ENDED");
    }
}
