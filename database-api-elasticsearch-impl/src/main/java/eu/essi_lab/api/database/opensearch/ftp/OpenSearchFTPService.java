/**
 * 
 */
package eu.essi_lab.api.database.opensearch.ftp;

import org.apache.ftpserver.ftplet.FtpException;

import eu.essi_lab.api.database.Database.OpenSearchServiceType;
import eu.essi_lab.api.database.opensearch.OpenSearchDatabase;
import eu.essi_lab.database.ftp.server.DatabaseFTPService;
import eu.essi_lab.model.StorageInfo;

/**
 * @author Fabrizio
 */
public class OpenSearchFTPService {

    /**
     * @param args
     * @throws FtpException
     */
    public static void main(String[] args) throws Exception {

	StorageInfo osStorageInfo = new StorageInfo(System.getProperty("dbUrl"));
	osStorageInfo.setName(System.getProperty("dbName"));
	osStorageInfo.setUser(System.getProperty("dbUser"));
	osStorageInfo.setPassword(System.getProperty("dbPassword"));
	osStorageInfo.setIdentifier(System.getProperty("dbIdentifier"));
	osStorageInfo.setType(OpenSearchServiceType.OPEN_SEARCH_MANAGED.getProtocol());

	// OpenSearchDatabase database = OpenSearchDatabase.createLocalService();

	OpenSearchDatabase database = new OpenSearchDatabase();
	database.initialize(osStorageInfo);

	DatabaseFTPService service = DatabaseFTPService.get(database, "C:\\Users\\Fabrizio\\AppData\\Local\\Temp\\fz3temp-2");

	service.start();
    }
}
