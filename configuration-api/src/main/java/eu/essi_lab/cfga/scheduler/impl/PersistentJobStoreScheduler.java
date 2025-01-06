/**
 * 
 */
package eu.essi_lab.cfga.scheduler.impl;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
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

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.AbstractMap.SimpleEntry;
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
import eu.essi_lab.lib.utils.ClonableInputStream;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.IOStreamUtils;

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

    /**
     * @return
     * @throws Throwable
     */
    public synchronized List<SimpleEntry<String, String>> checkJobData() throws Throwable {

	String sql1 = "select JOB_NAME, JOB_DATA from GS_QRTZ_TRIGGERS";
	String sql2 = "select JOB_NAME, JOB_DATA from GS_QRTZ_JOB_DETAILS";

	ResultSet rs1 = manager.execQuery(sql1);
	ResultSet rs2 = manager.execQuery(sql2);

	List<SimpleEntry<String, String>> report = new ArrayList<>();

	checkJobData(rs1, report, "GS_QRTZ_TRIGGERS");
	checkJobData(rs2, report, "GS_QRTZ_JOB_DETAILS");

	manager.close(rs1);
	manager.close(rs2);

	return report;
    }

    @Override
    public synchronized List<SchedulerWorkerSetting> listScheduledSettings() throws Exception {

	String sql = "select JOB_DATA, NEXT_FIRE_TIME, TRIGGER_NAME from GS_QRTZ_TRIGGERS";

	ResultSet rs = manager.execQuery(sql);

	ArrayList<SchedulerWorkerSetting> out = new ArrayList<>();

	while (rs.next()) {

	    try {

		String triggerName = rs.getString("TRIGGER_NAME");

		long nextFire = rs.getLong("NEXT_FIRE_TIME");

		SchedulerWorkerSetting setting = createSetting(rs, triggerName);

		if (nextFire != 0) {

		    setting.setNextFireTime(new Date(nextFire));
		}

		out.add(setting);

	    } catch (Exception ex) {

		GSLoggerFactory.getLogger(getClass()).error("Error ocurred during list scheduled settings: {}", ex.getMessage());
		GSLoggerFactory.getLogger(getClass()).error(ex);
	    }
	}

	manager.close(rs);

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

		try {

		    SchedulerWorkerSetting setting = createSetting(jobDataRs, triggerName);

		    setting.setFiredTime(new Date(firedTime));

		    out.add(setting);

		} catch (Exception ex) {

		    GSLoggerFactory.getLogger(getClass()).error("Error ocurred during list executing settings: {}", ex.getMessage());
		    GSLoggerFactory.getLogger(getClass()).error(ex);
		}
	    }

	    jobDataRs.close();
	}

	manager.close(rs);

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

	manager.close(rs);

	return list;
    }

    /**
     * @param rs
     * @param report
     * @param table
     * @throws SQLException
     * @throws IOException
     * @throws Throwable
     */
    private void checkJobData(ResultSet rs, List<SimpleEntry<String, String>> report, String table)
	    throws SQLException, IOException, Throwable {

	while (rs.next()) {

	    Blob blob = rs.getBlob("JOB_DATA");
	    String jobName = rs.getString("JOB_NAME");

	    try {

		InputStream binaryStream = blob.getBinaryStream();

		ObjectInputStream in = new ObjectInputStream(binaryStream);

		JobDataMap map = (JobDataMap) in.readObject();

		new SchedulerWorkerSetting(map.getString("setting"));

	    } catch (Throwable t) {

		GSLoggerFactory.getLogger(getClass()).error(t);

		report.add(new SimpleEntry<>(jobName, table));
	    }
	}
    }

    /**
     * @param resultSet
     * @param triggerName
     * @return
     * @throws Exception
     */
    private SchedulerWorkerSetting createSetting(ResultSet resultSet, String triggerName) throws Exception {

	Blob blob = resultSet.getBlob("JOB_DATA");

	ClonableInputStream clonableInputStream = new ClonableInputStream(blob.getBinaryStream());

	try {

	    ObjectInputStream in = new ObjectInputStream(clonableInputStream.clone());

	    JobDataMap map = (JobDataMap) in.readObject();

	    return new SchedulerWorkerSetting(map.getString("setting"));

	} catch (Throwable t) {

	    GSLoggerFactory.getLogger(getClass()).error(t);

	    GSLoggerFactory.getLogger(getClass()).error("Trigger name with error: {}", triggerName);

	    GSLoggerFactory.getLogger(getClass()).error("Blob with error: {}", IOStreamUtils.asUTF8String(clonableInputStream.clone()));

	    throw t;
	}
    }

    /**
     * @return the setting
     */
    public SchedulerSetting getSetting() {

	return setting;
    }
}
