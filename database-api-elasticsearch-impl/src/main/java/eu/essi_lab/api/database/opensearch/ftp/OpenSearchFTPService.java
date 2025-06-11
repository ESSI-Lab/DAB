/**
 * 
 */
package eu.essi_lab.api.database.opensearch.ftp;

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

	OpenSearchDatabase database = OpenSearchDatabase.createLocalService();

//	OpenSearchDatabase database = new OpenSearchDatabase();
//	database.initialize(osStorageInfo);

	DatabaseFTPService service = DatabaseFTPService.get(database, "D:\\Desktop\\FTP\\");

	service.start();
    }
}
