package eu.essi_lab.cfga.gs;

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
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

import org.quartz.JobDataMap;

import eu.essi_lab.cfga.Configuration;
import eu.essi_lab.cfga.gs.setting.GSSourceSetting;
import eu.essi_lab.cfga.gs.setting.harvesting.HarvestingSetting;
import eu.essi_lab.cfga.scheduler.Scheduler;
import eu.essi_lab.cfga.scheduler.SchedulerFactory;
import eu.essi_lab.cfga.setting.scheduling.SchedulerSetting;
import eu.essi_lab.cfga.setting.scheduling.SchedulerSetting.JobStoreType;
import eu.essi_lab.cfga.setting.scheduling.SchedulerWorkerSetting;
import eu.essi_lab.cfga.setting.scheduling.Scheduling;

/**
 * @author Fabrizio
 */
public class QuartzDB_Manager {

    private SchedulerSetting setting;

    /**
     * @param setting
     */
    public QuartzDB_Manager(SchedulerSetting setting) {

	if (setting == null || setting.getJobStoreType() == JobStoreType.VOLATILE) {

	    throw new RuntimeException("Not persistent setting");
	}

	this.setting = setting;

	setting.debugSQLSettings();
    }

    //
    // --- Scheduling ---------------------------------------------------------------------------------------------
    //

    public void schedule(Configuration configuration) {

	Scheduler scheduler = SchedulerFactory.getScheduler(setting);

	ConfigurationWrapper.setConfiguration(configuration);

	// List<HarvestingSetting> harvestingSettings = ConfigurationWrapper.getHarvestingSettings();

	List<HarvestingSetting> harvestingSettings = ConfigurationWrapper.//
		getHarvestingSettings().//
		stream().//
		filter(s -> s.getScheduling().isEnabled()).//
		sorted((s1, s2) -> s1.getScheduling().getStartTime().get().getValue()
			.compareTo(s2.getScheduling().getStartTime().get().getValue()))
		.//
		collect(Collectors.toList());

	for (HarvestingSetting harvestingSetting : harvestingSettings) {

	    if (harvestingSetting.getScheduling().isEnabled()) {

		try {

		    Scheduling scheduling = harvestingSetting.getScheduling();

		    System.out.println(harvestingSetting.getSelectedAccessorSetting().getSource().getLabel());

		    System.out.println(scheduling.getStartTime().get().getValue());

		    System.out.println("**********************");

		    scheduler.schedule(harvestingSetting);

		} catch (Exception ex) {

		    ex.printStackTrace();
		}
	    }
	}
    }

    //
    // --- Listing ---------------------------------------------------------------------------------------------
    //

    /**
     * @param configuration
     * @throws Exception
     */
    public void listScheduled(Configuration configuration) throws Exception {

	Connection conn = DriverManager.getConnection(//
		setting.getSQLDatabaseUri() + "/" + setting.getSQLDatabaseName(), //
		setting.getSQLDatabaseUser(), //
		setting.getSQLDatabasePassword());

	Statement stmt = conn.createStatement();
	String sql = "select JOB_NAME,NEXT_FIRE_TIME from GS_QRTZ_TRIGGERS order by NEXT_FIRE_TIME ";

	ResultSet rs = stmt.executeQuery(sql);

	ConfigurationWrapper.setConfiguration(configuration);

	List<HarvestingSetting> harvestingSettings = ConfigurationWrapper.getHarvestingSettings();

	System.out.println("\nListing STARTED ---\n\n");

	while (rs.next()) {

	    String triggerName = rs.getString("JOB_NAME");
	    String nextFireTime = rs.getString("NEXT_FIRE_TIME");

	    Optional<HarvestingSetting> harvestingSetting = harvestingSettings.stream()
		    .filter(s -> s.getIdentifier().equals(triggerName.replace("job_id_", ""))).findFirst();

	    if (harvestingSetting.isPresent() && !nextFireTime.equals("-1")) {

		GSSourceSetting sourceSetting = harvestingSetting.get().getSelectedAccessorSetting().getGSSourceSetting();

		System.out.println("Trigger name     " + triggerName);

		System.out.println("Source id    " + sourceSetting.getSourceIdentifier());
		System.out.println("Source label " + sourceSetting.getSourceLabel());

		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);

		System.out.println("Next start   " + dateFormat.format(new Date(Long.valueOf(nextFireTime))));

		System.out.println("");
	    }
	}

	System.out.println("\nListing ENDED ---");

	rs.close();
	stmt.close();
	conn.close();
    }

    /**
     * @throws Exception
     */
    public ArrayList<SchedulerWorkerSetting> getScheduledWorkerSettings() throws Exception {

	System.out.println(setting.getSQLDatabaseUri() + "/" + setting.getSQLDatabaseName());
	System.out.println(setting.getSQLDatabaseUser());
	System.out.println(setting.getSQLDatabasePassword());
	
	Connection conn = DriverManager.getConnection(//
		setting.getSQLDatabaseUri() + "/" + setting.getSQLDatabaseName(), //
		setting.getSQLDatabaseUser(), //
		setting.getSQLDatabasePassword());

	Statement stmt = conn.createStatement();

	String sql = "select JOB_DATA from GS_QRTZ_TRIGGERS";

	ResultSet rs = stmt.executeQuery(sql);

	ArrayList<SchedulerWorkerSetting> out = new ArrayList<>();

	while (rs.next()) {

	    Blob blob = rs.getBlob("JOB_DATA");
	    InputStream binaryStream = blob.getBinaryStream();
	    ObjectInputStream in = new ObjectInputStream(binaryStream);
	    JobDataMap map = (JobDataMap) in.readObject();
	    out.add(new SchedulerWorkerSetting(map.getString("setting")));
	}

	return out;
    }

    /**
     * @return
     * @throws Exception
     */
    public ArrayList<SchedulerWorkerSetting> getRunningWorkerSettings() throws Exception {

	Connection conn = DriverManager.getConnection(//
		setting.getSQLDatabaseUri() + "/" + setting.getSQLDatabaseName(), //
		setting.getSQLDatabaseUser(), //
		setting.getSQLDatabasePassword());

	Statement stmt = conn.createStatement();
	String sql = "select JOB_NAME from GS_QRTZ_FIRED_TRIGGERS";

	ResultSet rs = stmt.executeQuery(sql);

	ArrayList<SchedulerWorkerSetting> out = new ArrayList<>();

	while (rs.next()) {

	    String triggerName = rs.getString("JOB_NAME");

	    sql = "select JOB_DATA,JOB_NAME from GS_QRTZ_TRIGGERS where JOB_NAME='" + triggerName + "'";

	    Statement jobDataStat = conn.createStatement();
	    ResultSet jobData = jobDataStat.executeQuery(sql);

	    while (jobData.next()) {

		Blob blob = jobData.getBlob("JOB_DATA");
		InputStream binaryStream = blob.getBinaryStream();
		ObjectInputStream in = new ObjectInputStream(binaryStream);
		JobDataMap map = (JobDataMap) in.readObject();
		SchedulerWorkerSetting setting = new SchedulerWorkerSetting(map.getString("setting"));
		if (!out.contains(setting)) {
		    out.add(setting);
		}
	    }

	    jobDataStat.close();
	    jobData.close();
	}

	rs.close();
	stmt.close();

	return out;

    }

    /**
     * @param configuration
     * @throws Exception
     */
    public void listRunning(Configuration configuration) throws Exception {

	Connection conn = DriverManager.getConnection(//
		setting.getSQLDatabaseUri() + "/" + setting.getSQLDatabaseName(), //
		setting.getSQLDatabaseUser(), //
		setting.getSQLDatabasePassword());

	Statement stmt = conn.createStatement();
	String sql = "select JOB_NAME,FIRED_TIME,SCHED_TIME from GS_QRTZ_FIRED_TRIGGERS order by FIRED_TIME ";

	ResultSet rs = stmt.executeQuery(sql);

	ConfigurationWrapper.setConfiguration(configuration);

	List<HarvestingSetting> harvestingSettings = ConfigurationWrapper.getHarvestingSettings();

	System.out.println("\nListing STARTED ---\n\n");

	while (rs.next()) {

	    String triggerName = rs.getString("JOB_NAME");
	    String firedTime = rs.getString("FIRED_TIME");
	    String schedTime = rs.getString("SCHED_TIME");

	    Optional<HarvestingSetting> harvestingSetting = harvestingSettings.stream()
		    .filter(s -> s.getIdentifier().equals(triggerName.replace("job_id_", ""))).findFirst();

	    if (harvestingSetting.isPresent()) {

		GSSourceSetting sourceSetting = harvestingSetting.get().getSelectedAccessorSetting().getGSSourceSetting();

		System.out.println("Source id    " + sourceSetting.getSourceIdentifier());
		System.out.println("Source label " + sourceSetting.getSourceLabel());

		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);

		System.out.println("Fired time   " + dateFormat.format(new Date(Long.valueOf(firedTime))));
		System.out.println("Sched time   " + dateFormat.format(new Date(Long.valueOf(schedTime))));

		System.out.println("");
	    }
	}

	System.out.println("\nListing ENDED ---");

	rs.close();
	stmt.close();
	conn.close();
    }

    /**
     * @param args
     * @throws Exception
     * @throws IOException
     */
    public static void main(String[] args) throws Exception {

	SchedulerSetting setting = new SchedulerSetting();
	setting.setJobStoreType(JobStoreType.PERSISTENT);

	setting.setUserDateTimeZone("UTC");

	setting.setSQLDatabaseName("gs_quartzJobStore_2");
	setting.setSQLDatabaseUri("jdbc:mysql://localhost:3306");
	setting.setSQLDatabaseUser("root");
	setting.setSQLDatabasePassword("pdw");

	QuartzDB_Manager quartzDB_Manager = new QuartzDB_Manager(setting);

	Configuration configuration = new DefaultConfiguration();

	// if (!quartzManager.isInitialized()) {

	// quartzManager.initialize(true);
	// }

	quartzDB_Manager.schedule(configuration);

	quartzDB_Manager.listScheduled(configuration);
    }
}
