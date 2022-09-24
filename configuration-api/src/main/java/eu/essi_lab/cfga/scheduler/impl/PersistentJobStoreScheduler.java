/**
 * 
 */
package eu.essi_lab.cfga.scheduler.impl;

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

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONObject;
import org.quartz.JobDataMap;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;

import eu.essi_lab.cfga.scheduler.SchedulerJobStatus;
import eu.essi_lab.cfga.scheduler.SchedulerUtils;
import eu.essi_lab.cfga.setting.scheduling.SchedulerSetting;
import eu.essi_lab.cfga.setting.scheduling.SchedulerWorkerSetting;

/**
 * @author Fabrizio
 */
public class PersistentJobStoreScheduler extends AbstractScheduler {

    /**
     * 
     */
    private SchedulerSetting setting;

    /**
     * 
     */
    private MySQLConnectionManager manager;

    /**
     * 
     */
    public PersistentJobStoreScheduler() {
    }

    /**
     * @param quartzScheduler
     * @param setting
     */
    public PersistentJobStoreScheduler(Scheduler quartzScheduler, SchedulerSetting setting) {

	super(quartzScheduler);

	this.setting = setting;

	this.manager = new MySQLConnectionManager();
	this.manager.setDbUri(setting.getSQLDatabaseUri());
	this.manager.setDbName(setting.getSQLDatabaseName());
	this.manager.setUser(setting.getSQLDatabaseUser());
	this.manager.setPwd(setting.getSQLDatabasePassword());
    }

    @Override
    public synchronized List<SchedulerWorkerSetting> listScheduledSettings() throws Exception {

	String sql = "select JOB_DATA, NEXT_FIRE_TIME from GS_QRTZ_TRIGGERS";

	ResultSet rs = manager.execQuery(sql);

	ArrayList<SchedulerWorkerSetting> out = new ArrayList<>();

	while (rs.next()) {

	    SchedulerWorkerSetting setting = createSetting(rs);

	    long nextFire = rs.getLong("NEXT_FIRE_TIME");

	    if (nextFire != 0) {

		setting.setNextFireTime(new Date(nextFire));
	    }

	    out.add(setting);
	}

	rs.close();

	return out;
    }

    /**
     * This is required since <code>scheduler.getCurrentlyExecutingJobs()</code> is not cluster aware
     * 
     * @return
     * @throws SchedulerException
     */
    @Override
    public synchronized List<SchedulerWorkerSetting> listExecutingSettings() throws Exception {

	ResultSet rs = manager.execQuery("select JOB_NAME,FIRED_TIME from GS_QRTZ_FIRED_TRIGGERS");

	ArrayList<SchedulerWorkerSetting> out = new ArrayList<>();

	while (rs.next()) {

	    String triggerName = rs.getString("JOB_NAME");
	    long firedTime = rs.getLong("FIRED_TIME");

	    ResultSet jobDataRs = manager.execQuery("select JOB_DATA,JOB_NAME from GS_QRTZ_TRIGGERS where JOB_NAME='" + triggerName + "'");

	    while (jobDataRs.next()) {

		SchedulerWorkerSetting setting = createSetting(jobDataRs);

		setting.setFiredTime(new Date(firedTime));

		out.add(setting);
	    }

	    jobDataRs.close();
	}

	rs.close();

	return out;
    }

    @Override
    public void setJobStatus(SchedulerJobStatus status) throws SQLException {

	String updateQuery = "UPDATE GS_QRTZ_JOB_DETAILS SET JOB_STATUS = '" + status.toString() + "' WHERE JOB_NAME = '"
		+ SchedulerUtils.createJobKeyName(status.getSettingId()) + "'";

	manager.execUpdate(updateQuery);
    }

    @Override
    public List<SchedulerJobStatus> getJobStatuslist() throws SQLException {

	ResultSet rs = manager.execQuery("select JOB_STATUS from GS_QRTZ_JOB_DETAILS");

	ArrayList<SchedulerJobStatus> list = new ArrayList<>();

	while (rs.next()) {

	    String status = rs.getString("JOB_STATUS");
	    if (status != null && !status.isEmpty()) {
		list.add(new SchedulerJobStatus(new JSONObject(status)));
	    }
	}

	return list;
    }

    /**
     * @param resultSet
     * @return
     * @throws Exception
     */
    private SchedulerWorkerSetting createSetting(ResultSet resultSet) throws Exception {

	Blob blob = resultSet.getBlob("JOB_DATA");

	InputStream binaryStream = blob.getBinaryStream();
	ObjectInputStream in = new ObjectInputStream(binaryStream);

	JobDataMap map = (JobDataMap) in.readObject();

	return new SchedulerWorkerSetting(map.getString("setting"));
    }

    /**
     * @return the setting
     */
    public SchedulerSetting getSetting() {

	return setting;
    }
}
