/**
 * 
 */
package eu.essi_lab.database.ftp.server;

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

import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.User;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.usermanager.PropertiesUserManagerFactory;
import org.apache.ftpserver.usermanager.UserFactory;

import eu.essi_lab.api.database.Database;

/**
 * @author Fabrizio
 */
public class DatabaseFTPService {

    private Database database;
    private String storTempDir;

    /**
     * @param database
     */
    private DatabaseFTPService(Database database, String storTempDir) {

	this.database = database;
	this.storTempDir = storTempDir;
    }

    /**
     * @param database
     * @return
     */
    public static DatabaseFTPService get(Database database, String storTempDir) {

	return new DatabaseFTPService(database,storTempDir);
    }

    /**
     * @throws FtpException
     */
    public void start() throws FtpException {

	FtpServerFactory serverFactory = new FtpServerFactory();

	//
	//
	//

	PropertiesUserManagerFactory userManagerFactory = new PropertiesUserManagerFactory();
	userManagerFactory.setAdminName("admin");

	UserFactory userFactory = new UserFactory();
	userFactory.setName("admin");
	userFactory.setPassword("admin");
	// userFactory.setHomeDirectory(new File("D://MLCPData").getAbsolutePath());

	User admin = userFactory.createUser();

	UserManager userManager = userManagerFactory.createUserManager();
	userManager.save(admin);

	serverFactory.setUserManager(userManager);

	//
	//
	//

	DatabaseFileSystemFactory factory = DatabaseFileSystemFactory.get(database);
	factory.setSTORTempDir(storTempDir);

	serverFactory.setFileSystem(factory);

	//
	//
	//

	ListenerFactory listenerFactory = new ListenerFactory();
	listenerFactory.setPort(2221);
	serverFactory.addListener("default", listenerFactory.createListener());

	//
	//
	//

	DatabaseFtpCommandFactory commandFactory = DatabaseFtpCommandFactory.get(serverFactory.getCommandFactory());//
	serverFactory.setCommandFactory(commandFactory);

	//
	//
	//

	FtpServer server = serverFactory.createServer();

	server.start();
    }

    /**
     * @param args
     * @throws FtpException
     */
    public static void main(String[] args) throws FtpException {

    }
}
