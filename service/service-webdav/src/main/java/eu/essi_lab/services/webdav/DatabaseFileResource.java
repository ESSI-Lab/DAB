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

import eu.essi_lab.api.database.*;
import eu.essi_lab.api.database.DatabaseFolder.*;
import eu.essi_lab.api.database.SourceStorageWorker.*;
import eu.essi_lab.lib.utils.*;
import eu.essi_lab.messages.*;
import eu.essi_lab.model.resource.*;
import io.milton.http.*;
import io.milton.http.Request.*;
import io.milton.http.exceptions.*;
import io.milton.resource.*;
import org.apache.commons.io.*;

import java.io.*;
import java.util.*;

/**
 * @author Fabrizio
 */
public class DatabaseFileResource implements GetableResource, PropFindableResource, ReplaceableResource, DeletableResource {

    private Database database;
    private String file;
    private String dir;

    /**
     * @param database
     * @param dir
     * @param file
     */
    public DatabaseFileResource(Database database, String dir, String file) {

	this.dir = dir;
	this.file = file;
	this.database = database;

	GSLoggerFactory.getLogger(getClass()).debug("File resource [{}] created", this);
    }

    @Override
    public String getName() {

	return file;
    }

    /**
     * @return
     */
    public boolean exists() {

	try {

	    DatabaseFolder folder = database.getFolder(dir);

	    if (folder != null) {

		return folder.exists(file);
	    }
	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e);
	}

	return false;
    }

    @Override
    public void sendContent(OutputStream out, Range range, Map<String, String> params, String contentType) {

	GSLoggerFactory.getLogger(getClass()).debug("Sending content of [{}] STARTED", this);

	try {

	    InputStream binary = database.getFolder(dir).getBinary(StringUtils.URLDecodeUTF8(file));

	    IOUtils.copy(binary, out);

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e);
	}

	GSLoggerFactory.getLogger(getClass()).debug("Sending content of [{}] ENDED", this);
    }

    @Override
    public void replaceContent(InputStream inputStream, Long length) {

	GSLoggerFactory.getLogger(getClass()).debug("Replacing content of [{}] STARTED", this);

	try {

	    FolderEntry entry = null;
	    EntryType type = null;

	    if (file.equals(HarvestingProperties.FILE_NAME)) {

		entry = FolderEntry.of(inputStream);
		type = EntryType.HARVESTING_PROPERTIES;
	    } else if (file.equals(SourceStorageWorker.ERRORS_REPORT_FILE_NAME)) {

		entry = FolderEntry.of(inputStream);
		type = EntryType.HARVESTING_ERROR_REPORT;
	    } else if (file.equals(SourceStorageWorker.WARN_REPORT_FILE_NAME)) {

		entry = FolderEntry.of(inputStream);
		type = EntryType.HARVESTING_WARN_REPORT;

	    } else if (file.endsWith(SourceStorageWorker.DATA_FOLDER_POSTFIX)) {

		entry = FolderEntry.of(new DataFolderIndexDocument(inputStream).getDocument());
		type = EntryType.DATA_FOLDER_INDEX_DOC;

	    } else {

		GSResource gsResource = GSResource.create(inputStream);

		entry = FolderEntry.of(gsResource.asDocument(false));
		type = EntryType.GS_RESOURCE;
	    }

	    database.getFolder(dir).replace(file, entry, type);

	} catch (Exception ex) {

	    GSLoggerFactory.getLogger(getClass()).error(ex);
	}

	GSLoggerFactory.getLogger(getClass()).debug("Replacing content of [{}] ENDED", this);
    }

    @Override
    public void delete() throws NotAuthorizedException, ConflictException, BadRequestException {

	GSLoggerFactory.getLogger(getClass()).debug("Deleting file [{}] STARTED", this);

	try {
	    database.getFolder(dir).remove(StringUtils.URLDecodeUTF8(file));

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e);
	}

	GSLoggerFactory.getLogger(getClass()).debug("Deleting ended [{}] STARTED", this);
    }

    @Override
    public String getContentType(String accepts) {

	return "application/octet-stream";
    }

    @Override
    public String toString() {

	return dir.equals("/") ? dir + file : dir + "/" + file;
    }

    @Override
    public String getUniqueId() {

	return toString();
    }

    @Override
    public Object authenticate(String user, String password) {

	return "ok";
    }

    @Override
    public boolean authorise(Request request, Method method, Auth auth) {

	return true;
    }

    @Override
    public Date getCreateDate() {

	return null;
    }

    @Override
    public Long getContentLength() {

	return null;
    }

    @Override
    public String getRealm() {

	return null;
    }

    @Override
    public Date getModifiedDate() {

	return null;
    }

    @Override
    public String checkRedirect(Request request) {

	return null;
    }

    @Override
    public Long getMaxAgeSeconds(Auth auth) {

	return null;
    }
}
