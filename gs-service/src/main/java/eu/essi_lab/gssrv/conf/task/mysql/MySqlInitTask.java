package eu.essi_lab.gssrv.conf.task.mysql;

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

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.AbstractMap.SimpleEntry;
import java.util.Optional;

import org.quartz.JobExecutionContext;

import com.amazonaws.util.IOUtils;

import eu.essi_lab.cfga.gs.task.AbstractCustomTask;
import eu.essi_lab.cfga.gs.task.CustomTaskSetting;
import eu.essi_lab.cfga.scheduler.SchedulerJobStatus;
import eu.essi_lab.lib.utils.GSLoggerFactory;

/**
 * This task creates a db for the jobs and init the needed tables
 * 
 * @author boldrini
 */
public class MySqlInitTask extends AbstractCustomTask {

    // Usage expected
    // host: jdbc:mysql://hostname:30111
    // user: root
    // password: mypass
    // database: mynewdb
    public enum TaskParameterKey {
	HOST("host:"), //
	USER("user:"), //
	PASSWORD("password:"), //
	DATABASE("database:"), //
	;

	private String id;

	TaskParameterKey(String id) {
	    this.id = id;
	}

	@Override
	public String toString() {
	    return id;
	}

	public static String getExpectedKeys() {
	    String ret = "";
	    for (TaskParameterKey key : values()) {
		ret += key.toString() + " ";
	    }
	    return ret;
	}

	public static SimpleEntry<TaskParameterKey, String> decodeLine(String line) {
	    for (TaskParameterKey key : values()) {
		if (line.toLowerCase().startsWith(key.toString().toLowerCase())) {
		    SimpleEntry<TaskParameterKey, String> ret = new SimpleEntry<>(key, line.substring(key.toString().length()).trim());
		    return ret;
		}
	    }
	    return null;
	}
    }

    @Override
    public void doJob(JobExecutionContext context, SchedulerJobStatus status) throws Exception {
	GSLoggerFactory.getLogger(getClass()).info("MySQL Init task STARTED");
	log(status, "MySQL Init task STARTED");

	// SETTINGS RETRIEVAL
	CustomTaskSetting taskSettings = retrieveSetting(context);

	Optional<String> taskOptions = taskSettings.getTaskOptions();

	String settings = null;
	if (taskOptions.isPresent()) {
	    String options = taskOptions.get();
	    if (options != null) {
		settings = options;
	    }
	}
	if (settings == null) {
	    GSLoggerFactory.getLogger(getClass()).error("missing settings for mysql init task");
	    return;
	}
	String[] lines = settings.split("\n");
	String host = null;
	String user = null;
	String password = null;
	String database = null;
	for (String line : lines) {
	    SimpleEntry<TaskParameterKey, String> decoded = TaskParameterKey.decodeLine(line);
	    if (decoded == null) {
		GSLoggerFactory.getLogger(getClass())
			.error("unexpected settings for mysql init task. Expected: " + TaskParameterKey.getExpectedKeys());
		return;
	    }
	    switch (decoded.getKey()) {
	    case HOST:
		host = decoded.getValue();
		break;
	    case USER:
		user = decoded.getValue();
		break;
	    case PASSWORD:
		password = decoded.getValue();
		break;
	    case DATABASE:
		database = decoded.getValue();
		break;
	    default:
		break;
	    }
	}

	GSLoggerFactory.getLogger(getClass()).info("Connecting to {} with user {}", host, user);

	String script = IOUtils.toString(MySqlInitTask.class.getClassLoader().getResourceAsStream("init_tables_innodb.sql"));

	script = "CREATE DATABASE IF NOT EXISTS " + database //
		+ " CHARACTER SET utf8;\n"//
		+ "USE " + database + ";\n" + script;
	
	try (Connection conn = DriverManager.getConnection(host, user, password); java.sql.Statement stmt = conn.createStatement()) {

	    // Read SQL script file (you can also use a resource or inline string)

	    // Split script into individual statements (simple split on semicolon)
	    for (String sql : script.split(";")) {
		sql = sql.trim();
		if (!sql.isEmpty()) {
		    GSLoggerFactory.getLogger(getClass()).info("Executing: " + sql);
		    stmt.execute(sql);
		}
	    }

	    GSLoggerFactory.getLogger(getClass()).info("Database creation script executed successfully.");
	}

	GSLoggerFactory.getLogger(getClass()).info("MySQL init task ENDED");
	log(status, "MySQL init task ENDED");
    }

    @Override
    public String getName() {

	return "MySQL init task";
    }
}
