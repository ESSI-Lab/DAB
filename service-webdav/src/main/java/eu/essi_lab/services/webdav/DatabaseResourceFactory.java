/**
 * 
 */
package eu.essi_lab.services.webdav;

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

import eu.essi_lab.api.database.Database;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.exceptions.GSException;
import io.milton.http.ResourceFactory;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.resource.Resource;

/**
 * @author Fabrizio
 */
public class DatabaseResourceFactory implements ResourceFactory {

    private Database database;

    /**
     * @param database
     */
    public DatabaseResourceFactory(Database database) {

	this.database = database;
    }

    @Override
    public Resource getResource(String host, String path) throws NotAuthorizedException, BadRequestException {
	
	if (path.contains("desktop.ini") || path.contains("favicon.ico") || path.contains("Thumbs.db")) {

	    return null;
	}

	Resource out = null;

	if ("/".equals(path) || "".equals(path)) {

	    GSLoggerFactory.getLogger(getClass()).info("Getting directory resource [{}] STARTED", path);

	    out = new DatabaseDirectoryResource(database, path);

	    GSLoggerFactory.getLogger(getClass()).info("Getting directory resource [{}] ENDED", path);

	} else {

	    try {

		String path_ = path.substring(1, path.length());

		if (path_.endsWith("/")) {
		    path_ = path_.substring(0,path_.length()-1);
		}
		if (database.existsFolder(path_)) {

		    GSLoggerFactory.getLogger(getClass()).info("Getting directory resource [{}] STARTED", path);

		    out = new DatabaseDirectoryResource(database, path_);

		    GSLoggerFactory.getLogger(getClass()).info("Getting directory resource [{}] ENDED", path);

		} else {

		    GSLoggerFactory.getLogger(getClass()).info("Getting file resource [{}] STARTED", path);

		    String dir = path.lastIndexOf("/") == 0 ? "/" : path.substring(1, path.lastIndexOf("/"));

		    String file = path.substring(path.lastIndexOf("/") + 1, path.length());

		    out = new DatabaseFileResource(database, dir, file);

		    if (!((DatabaseFileResource) out).exists()) {

			out = null;

			GSLoggerFactory.getLogger(getClass()).info("File resource [{}] not found", path);
		    }

		    GSLoggerFactory.getLogger(getClass()).info("Getting file resource [{}] ENDED", path);

		}
	    } catch (GSException e) {

		GSLoggerFactory.getLogger(getClass()).error(e);
	    }
	}

	return out;
    }
}
