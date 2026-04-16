/**
 *
 */
package eu.essi_lab.services.ftp;

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

import eu.essi_lab.api.database.*;
import eu.essi_lab.api.database.opensearch.*;
import eu.essi_lab.cfga.gs.*;
import eu.essi_lab.lib.utils.*;
import eu.essi_lab.model.*;
import eu.essi_lab.model.exceptions.*;
import eu.essi_lab.services.impl.*;
import eu.essi_lab.services.message.*;
import org.apache.ftpserver.*;
import org.apache.ftpserver.ftplet.*;
import org.apache.ftpserver.listener.*;
import org.apache.ftpserver.usermanager.*;

/**
 * @author Fabrizio
 */
public class DatabaseFTPService extends AbstractManagedService {

    private static final String TEMP_STORE_DIR_KEY = "tempStoreDir";
    private FtpServer server;

    /**
     *
     */
    public DatabaseFTPService() {
    }

    @Override
    public void start() {

	try {

	    publish(MessageChannel.MessageLevel.INFO, "FTP Service " + getId() + " starting");

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

	    OpenSearchDatabase database = new OpenSearchDatabase();

	    database.initialize(ConfigurationWrapper.getStorageInfo());

	    DatabaseFileSystemFactory factory = DatabaseFileSystemFactory.get(database);

	    String tempStoreDir = getSetting(). //
		    readKeyValue(TEMP_STORE_DIR_KEY).//
		    orElse(FileUtils.getTempDir().getAbsolutePath());//

	    publish(MessageChannel.MessageLevel.INFO, "Temporary store directory: " + tempStoreDir);

	    factory.setSTORTempDir(tempStoreDir);

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

	    server = serverFactory.createServer();

	    server.start();

	    publish(MessageChannel.MessageLevel.INFO, "FTP Service " + getId() + " started");

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e);

	    publish(MessageChannel.MessageLevel.ERROR, e.getMessage());
	}
    }

    @Override
    public void stop() {

	server.stop();

	publish(MessageChannel.MessageLevel.INFO, "FTP Service " + getId() + " stopped");
    }

    /**
     * @param args
     * @throws FtpException
     */
    public static void main(String[] args) throws Exception {

	//	StorageInfo osStorageInfo = new StorageInfo(System.getProperty("dbUrl"));
	//	osStorageInfo.setName(System.getProperty("dbName"));
	//	osStorageInfo.setUser(System.getProperty("dbUser"));
	//	osStorageInfo.setPassword(System.getProperty("dbPassword"));
	//	osStorageInfo.setIdentifier(System.getProperty("dbIdentifier"));
	//	osStorageInfo.setType(OpenSearchServiceType.OPEN_SEARCH_MANAGED.getProtocol());
	//
	//	OpenSearchDatabase database = OpenSearchDatabase.createLocalService();
	//
	//	//	OpenSearchDatabase database = new OpenSearchDatabase();
	//	//	database.initialize(osStorageInfo);
	//
	//	DatabaseFTPService service = DatabaseFTPService.get(database, "D:\\Desktop\\FTP\\");
	//
	//	service.start();
    }
}
