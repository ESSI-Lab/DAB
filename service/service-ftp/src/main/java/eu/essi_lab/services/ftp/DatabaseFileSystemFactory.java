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
import org.apache.ftpserver.ftplet.*;

/**
 * @author Fabrizio
 */
public class DatabaseFileSystemFactory implements FileSystemFactory {

    private Database database;
    private String tempSTORdir;

    /**
     * 
     */
    private DatabaseFileSystemFactory() {
    }

    /**
     * @param database
     */
    private DatabaseFileSystemFactory(Database database) {

	this.database = database;
    }

    /**
     * @return
     */
    public static DatabaseFileSystemFactory get(Database database) {

	return new DatabaseFileSystemFactory(database);
    }

    @Override
    public FileSystemView createFileSystemView(User user) throws FtpException {

	return new DatabaseFileSystemView(database,user,tempSTORdir);
    }

    /**
     * @param tempSTORdir
     */
    public void setSTORTempDir(String tempSTORdir) {
	
	this.tempSTORdir = tempSTORdir;	
    }
}
