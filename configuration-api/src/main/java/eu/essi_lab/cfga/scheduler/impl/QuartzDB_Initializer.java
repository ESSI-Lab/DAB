package eu.essi_lab.cfga.scheduler.impl;

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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.ibatis.jdbc.ScriptRunner;

import eu.essi_lab.cfga.setting.scheduling.SchedulerSetting;
import eu.essi_lab.cfga.setting.scheduling.SchedulerSetting.JobStoreType;
import eu.essi_lab.lib.utils.GSLoggerFactory;

/**
 * @author Fabrizio
 */
public class QuartzDB_Initializer {

    private final SchedulerSetting setting;

    /**
     * @param setting
     */
    public QuartzDB_Initializer(SchedulerSetting setting) {

	if (setting == null || setting.getJobStoreType() == JobStoreType.VOLATILE) {

	    throw new RuntimeException("Not persistent setting");
	}

	this.setting = setting;

	setting.debugSQLSettings();
    }

    //
    // --- Initialization ----------------------------------------------------------------------
    //

    /**
     * @return
     * @throws SQLException
     */
    public boolean dbExists() throws SQLException {

	Connection conn = createConnection(false);

	return dbExists(conn);
    }

    /**
     * @param innoDB
     * @throws SQLException
     */
    public void initializeTables(boolean innoDB) throws SQLException {

	Connection conn = createConnection(true);

	InputStream sql = null;

	if (innoDB) {

	    sql = getClass().getClassLoader().getResourceAsStream("init_tables_innodb.sql");

	} else {

	    sql = getClass().getClassLoader().getResourceAsStream("init_tables.sql");
	}

	ScriptRunner sr = new ScriptRunner(conn);

	Reader reader = new BufferedReader(new InputStreamReader(sql));

	sr.runScript(reader);

	conn.close();
    }

    /**
     * @param dbName
     * @return
     * @throws SQLException
     */
    public void createDb() throws SQLException {

	Connection conn = createConnection(false);

	createDb(conn);
    }

    /**
     * @param tableName
     * @return
     * @throws SQLException
     */
    public boolean tableExists(String tableName) throws SQLException {

	Connection conn = createConnection(true);

	return tableExists(conn, tableName);
    }

    /**
     * @param withDBName
     * @return
     * @throws SQLException
     */
    private Connection createConnection(boolean withDBName) throws SQLException {

	String uri = MySQLConnectionManager.createConnectionURL(//
		setting.getSQLDatabaseUri(), //
		withDBName ? setting.getSQLDatabaseName() : null);

	//

	return DriverManager.getConnection(//
		uri, //
		setting.getSQLDatabaseUser(), //
		setting.getSQLDatabasePassword());
    }

    /**
     * @param conn
     * @return
     * @throws SQLException
     */
    private boolean dbExists(Connection conn) throws SQLException {

	Statement statement = conn.createStatement();

	String query = "SELECT SCHEMA_NAME FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME = '" + setting.getSQLDatabaseName() + "'";

	ResultSet resultSet = statement.executeQuery(query);

	boolean next = resultSet.next();

	statement.close();

	return next;
    }

    /**
     * @param conn
     * @param dbName
     * @return
     * @throws SQLException
     */
    private void createDb(Connection conn) throws SQLException {

	Statement statement = conn.createStatement();

	String query = "CREATE DATABASE " + setting.getSQLDatabaseName() + ";";

	int result = statement.executeUpdate(query);

	if (result == 0) {
	    throw new SQLException("DB not created");
	}

	statement.close();
    }

    /**
     * @param conn
     * @param tableName
     * @return
     * @throws SQLException
     */
    private boolean tableExists(Connection conn, String tableName) throws SQLException {

	Statement statement = conn.createStatement();

	String query = "SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = '" + tableName + "';";

	ResultSet resultSet = statement.executeQuery(query);

	statement.close();

	return resultSet.next();
    }

    /**
     * @param args
     */
    public static void main(String[] args) {

	SchedulerSetting setting = createSetting("quartzJobStore", "localhost", "root", "password", 3306);

	QuartzDB_Initializer initializer = new QuartzDB_Initializer(setting);

	try {

	    boolean dbExists = initializer.dbExists();

	    if (!dbExists) {

		GSLoggerFactory.getLogger(QuartzDB_Initializer.class).info("DB creation STARTED");

		initializer.createDb();

		GSLoggerFactory.getLogger(QuartzDB_Initializer.class).info("DB creation ENDED");
	    }

	    GSLoggerFactory.getLogger(QuartzDB_Initializer.class).info("Tables initialization STARTED");

	    initializer.initializeTables(true);

	    GSLoggerFactory.getLogger(QuartzDB_Initializer.class).info("Tables initialization ENDED");

	} catch (SQLException e) {


	    GSLoggerFactory.getLogger(QuartzDB_Initializer.class).error(e.getMessage(), e);
	}
    }

    /**
     * @return
     */
    private static SchedulerSetting createSetting(String dbName, String host, String user, String pwd, int port) {

	SchedulerSetting setting = new SchedulerSetting();

	setting.setJobStoreType(JobStoreType.PERSISTENT);
	setting.setUserDateTimeZone("UTC");

	setting.setSQLDatabaseName(dbName);
	setting.setSQLDatabaseUri("jdbc:mysql://" + host + ":" + port);
	setting.setSQLDatabaseUser(user);
	setting.setSQLDatabasePassword(pwd);

	setting.debugSQLSettings();

	return setting;
    }
}
