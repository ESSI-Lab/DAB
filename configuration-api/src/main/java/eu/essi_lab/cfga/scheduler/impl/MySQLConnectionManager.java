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

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Enumeration;

import com.mysql.cj.jdbc.exceptions.CommunicationsException;

import eu.essi_lab.lib.utils.GSLoggerFactory;

/**
 * @author Fabrizio
 */
public class MySQLConnectionManager {

    /**
     * 
     */
    private Connection connection;

    private String user;
    private String pwd;
    private String dbName;
    private boolean useSSl;
    private boolean autoReconnect;
    private String dbUri;

    /**
     * 
     */
    public MySQLConnectionManager() {

	setUseSSl(false);
	setAutoReconnect(false);
    }

    /**
     * @param dbUri must starts with 'jdbc:mysql://' and can ends with or without '/'
     * @param useSSl
     * @param dbName
     * @param autoReconnect
     * @see #MAX_OTHER_WAIT_TIMEOUT
     * @see #MAX_WINDOWS_WAIT_TIMEOUT
     * @return
     */
    public static String createConnectionURL(String dbUri, String dbName, boolean useSSl, boolean autoReconnect) {

	String url = null;

	if (dbName != null) {

	    if (!dbUri.endsWith("/")) {

		dbName = "/" + dbName;
	    }

	    url = dbUri + dbName + "?useSSL=" + useSSl;

	} else {

	    url = dbUri + "?useSSL=" + useSSl;
	}

	return url;
    }

    /**
     * @param dbUri
     * @param dbName
     * @return
     */
    public static String createConnectionURL(String dbUri, String dbName) {

	return createConnectionURL(dbUri, dbName, false, false);
    }

    /**
     * @param dbUri
     * @param dbName
     * @return
     */
    public static String createConnectionURL(String dbUri) {

	return createConnectionURL(dbUri, null, false, false);
    }

    /**
     * @param connectionURL
     * @param user
     * @param pwd
     * @return
     * @throws SQLException
     */
    public static Connection connect(String connectionURL, String user, String pwd) throws SQLException {

	Enumeration<Driver> drivers = DriverManager.getDrivers();
	GSLoggerFactory.getLogger(MySQLConnectionManager.class).debug("Available SQL drivers:");
	while (drivers.hasMoreElements()) {
	    GSLoggerFactory.getLogger(MySQLConnectionManager.class).debug(drivers.nextElement().getClass().getName());
	}

	return DriverManager.getConnection(connectionURL, user, pwd);
    }

    /**
     * @return
     */
    public Connection getConnection() {
	try {
	    if (connection == null) {

		connection = createConnection();

	    } else {

		if (connection.isClosed()) {

		    GSLoggerFactory.getLogger(getClass()).debug("Connection closed, new connection REQUIRED");

		    connection = createConnection();
		}
	    }
	} catch (SQLException e) {

	    e.printStackTrace();
	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage());
	}

	return connection;
    }

    /**
     * @param query
     * @return
     * @throws SQLException
     */
    public ResultSet execQuery(String query) throws SQLException {

	GSLoggerFactory.getLogger(getClass()).debug("Query execution STARTED");

	ResultSet resultSet = null;

	try {

	    Statement stmt = getConnection().createStatement();

	    resultSet = stmt.executeQuery(query);

	} catch (CommunicationsException comEx) {

	    //
	    // maybe the connection is expired since inactive for more than wait_timeout time
	    // creating new connection and trying again
	    // see: https://dev.mysql.com/doc/refman/8.0/en/server-system-variables.html#sysvar_wait_timeout
	    // when the error occurs, a message like this is provided:
	    // com.mysql.cj.jdbc.exceptions.CommunicationsException: The last packet successfully received from the
	    // server was 49,036,491
	    // milliseconds ago. The last packet sent successfully to the server was 49,036,507 milliseconds ago.
	    // is longer than the server configured value of 'wait_timeout'.
	    // You should consider either expiring and/or testing connection validity before use in your application,
	    // increasing the server configured values for client timeouts, or using the Connector/J connection property
	    // 'autoReconnect=true' to avoid this problem
	    //
	    GSLoggerFactory.getLogger(getClass()).warn("Communication exception occurred: {}", comEx.getMessage());
	    GSLoggerFactory.getLogger(getClass()).warn("Creating new connection and try again");

	    connection = createConnection();
	    Statement stmt = connection.createStatement();

	    resultSet = stmt.executeQuery(query);

	} catch (SQLException sqlEx) {

	    throw sqlEx;
	}

	GSLoggerFactory.getLogger(getClass()).debug("Query execution ENDED");

	return resultSet;
    }

    /**
     * @param query
     * @return
     * @throws SQLException
     */
    public void execUpdate(String query) throws SQLException {

	GSLoggerFactory.getLogger(getClass()).debug("Query execution STARTED");

	try {

	    Statement stmt = getConnection().createStatement();

	    stmt.executeUpdate(query);

	} catch (CommunicationsException comEx) {

	    //
	    // maybe the connection is expired since inactive for more than wait_timeout time
	    // creating new connection and trying again
	    // see: https://dev.mysql.com/doc/refman/8.0/en/server-system-variables.html#sysvar_wait_timeout
	    // when the error occurs, a message like this is provided:
	    // com.mysql.cj.jdbc.exceptions.CommunicationsException: The last packet successfully received from the
	    // server was 49,036,491
	    // milliseconds ago. The last packet sent successfully to the server was 49,036,507 milliseconds ago.
	    // is longer than the server configured value of 'wait_timeout'.
	    // You should consider either expiring and/or testing connection validity before use in your application,
	    // increasing the server configured values for client timeouts, or using the Connector/J connection property
	    // 'autoReconnect=true' to avoid this problem
	    //
	    GSLoggerFactory.getLogger(getClass()).warn("Communication exception occurred: {}", comEx.getMessage());
	    GSLoggerFactory.getLogger(getClass()).warn("Creating new connection and try again");

	    connection = createConnection();
	    Statement stmt = connection.createStatement();

	    stmt.executeUpdate(query);

	} catch (SQLException sqlEx) {

	    throw sqlEx;
	}

	GSLoggerFactory.getLogger(getClass()).debug("Query execution ENDED");
    }

    /**
     * @return the user
     */
    public String getUser() {

	return user;
    }

    /**
     * @param user the user to set
     */
    public void setUser(String user) {

	this.user = user;
    }

    /**
     * @return the pwd
     */
    public String getPwd() {

	return pwd;
    }

    /**
     * @param pwd the pwd to set
     */
    public void setPwd(String pwd) {

	this.pwd = pwd;
    }

    /**
     * @return the dbName
     */
    public String getDbName() {

	return dbName;
    }

    /**
     * @param dbName the dbName to set
     */
    public void setDbName(String dbName) {

	this.dbName = dbName;
    }

    /**
     * @return the useSSl
     */
    public boolean isUseSSl() {

	return useSSl;
    }

    /**
     * @param useSSl the useSSl to set
     */
    public void setUseSSl(boolean useSSl) {

	this.useSSl = useSSl;
    }

    /**
     * @return the autoReconnect
     */
    public boolean isAutoReconnect() {

	return autoReconnect;
    }

    /**
     * @param autoReconnect the autoReconnect to set
     */
    public void setAutoReconnect(boolean autoReconnect) {

	this.autoReconnect = autoReconnect;
    }

    /**
     * @return the dbUri
     */
    public String getDbUri() {

	return dbUri;
    }

    /**
     * Must starts with 'jdbc:mysql://' and must ends without '/'
     * 
     * @param dbUri the dbUri to set
     */
    public void setDbUri(String dbUri) {

	this.dbUri = dbUri;
    }

    /**
     * @return
     * @throws SQLException
     */
    private Connection createConnection() throws SQLException {

	GSLoggerFactory.getLogger(getClass()).debug("Creating new MySQL connection STARTED");

	GSLoggerFactory.getLogger(getClass()).debug("DB URI: {}", this.dbUri);
	GSLoggerFactory.getLogger(getClass()).debug("DB Name: {}", this.dbName);
	GSLoggerFactory.getLogger(getClass()).debug("DB User: {}", this.user);
	GSLoggerFactory.getLogger(getClass()).debug("DB Pwd: {}", this.pwd);

	GSLoggerFactory.getLogger(getClass()).debug("Autoreconnect: {}", this.autoReconnect);
	GSLoggerFactory.getLogger(getClass()).debug("Use SSL: {}", this.useSSl);

	String connectionURL = createConnectionURL(this.dbUri, this.dbName);

	Connection connection = connect(connectionURL, this.user, this.pwd);

	GSLoggerFactory.getLogger(getClass()).debug("Creating new MySQL connection ENDED");

	return connection;
    }
}
