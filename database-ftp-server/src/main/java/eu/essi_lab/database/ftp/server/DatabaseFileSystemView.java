/**
 * 
 */
package eu.essi_lab.database.ftp.server;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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

import org.apache.ftpserver.ftplet.FileSystemView;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.FtpFile;
import org.apache.ftpserver.ftplet.User;

import eu.essi_lab.api.database.Database;

/**
 * @author Fabrizio
 */
public class DatabaseFileSystemView implements FileSystemView {

    static final String ROOT = "/";
    static final String SELF = "./";
    static final String PARENT = "..";

    private Database database;
    private User user;
    private String currDir;
    private String tempSTORdir;

    static final List<File> TEMP_FILES = new ArrayList<File>();

    /**
     * @param database
     * @param user
     * @param tempSTORdir 
     */
    DatabaseFileSystemView(Database database, User user, String tempSTORdir) {

	this.database = database;
	this.user = user;
	this.tempSTORdir = tempSTORdir;
	this.currDir = ROOT;
    }

    @Override
    public FtpFile getHomeDirectory() throws FtpException {

	return new DatabaseFtpFile(database, ROOT, user);
    }

    @Override
    public FtpFile getWorkingDirectory() throws FtpException {

	return new DatabaseFtpFile(database, currDir, user);
    }

    @Override
    public boolean changeWorkingDirectory(String dir) throws FtpException {

	DatabaseFtpFile file = new DatabaseFtpFile(database, dir, user);

	if (file.isFile()) {

	    return false;
	}

	this.currDir = dir.equals(ROOT) ? dir : dir.replace(ROOT, "");

	return true;
    }

    @Override
    public FtpFile getFile(String file) throws FtpException {

	return new DatabaseFtpFile(database, currDir, file, user, tempSTORdir);
    }

    @Override
    public boolean isRandomAccessible() throws FtpException {

	return true;
    }

    @Override
    public void dispose() {

	TEMP_FILES.forEach(f -> f.delete());
    }
}
