/**
 * 
 */
package eu.essi_lab.database.ftp.server;

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

	// DatabaseFtpCommandFactory commandFactory =
	// DatabaseFtpCommandFactory.get(serverFactory.getCommandFactory());//
	// serverFactory.setCommandFactory(commandFactory);

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
