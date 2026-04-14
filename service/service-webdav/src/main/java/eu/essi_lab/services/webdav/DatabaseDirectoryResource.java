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
import eu.essi_lab.model.exceptions.*;
import eu.essi_lab.model.resource.*;
import io.milton.http.*;
import io.milton.http.Request.*;
import io.milton.http.exceptions.*;
import io.milton.resource.*;

import java.io.*;
import java.util.*;
import java.util.stream.*;

/**
 * @author Fabrizio
 */
public class DatabaseDirectoryResource
	implements GetableResource, DeletableResource, PropFindableResource, CollectionResource, PutableResource {

    private String dir;
    private Database database;
    private final int maxFiles;

    /**
     * @param dir
     * @param database
     */
    public DatabaseDirectoryResource(Database database, String dir, int maxFiles) {

	this.dir = dir;
	this.database = database;
	this.maxFiles = maxFiles;

	GSLoggerFactory.getLogger(getClass()).debug("Directory resource [{}] created", this);
    }

    @Override
    public String getUniqueId() {

	return dir;
    }

    @Override
    public String getName() {

	return dir;
    }

    @Override
    public List<Resource> getChildren() {

	ArrayList<Resource> list = new ArrayList<>();

	try {
	    if ("/".equals(dir) || "".equals(dir)) {

		database.getMetaFolders().//
			parallelStream().//
			map(f -> new DatabaseDirectoryResource(database, f.getName(), maxFiles)).//
			forEach(list::add);

		database.getDataFolders().//
			parallelStream().//
			map(f -> new DatabaseDirectoryResource(database, f.getName(), maxFiles)).//
			forEach(list::add);

		DatabaseFolder usersFolder = database.getUsersFolder();

		if (usersFolder != null) {

		    list.add(new DatabaseDirectoryResource(database, usersFolder.getName(), maxFiles));
		}

		DatabaseFolder viewFolder = database.getViewFolder(false);

		if (viewFolder != null) {

		    list.add(new DatabaseDirectoryResource(database, viewFolder.getName(), maxFiles));
		}

		DatabaseFolder cacheFolder = database.getCacheFolder();

		if (cacheFolder != null) {

		    list.add(new DatabaseDirectoryResource(database, cacheFolder.getName(), maxFiles));
		}

	    } else {

		DatabaseFolder folder = database.getFolder(dir);

		List<String> keys = Arrays.asList(folder.listKeys());

		keys = keys.subList(0, Math.min(maxFiles, keys.size()));

		keys.forEach(key -> list.add(new DatabaseFileResource(//
			database, dir, key)));
	    }

	} catch (Exception ex) {

	    GSLoggerFactory.getLogger(getClass()).error(ex);
	}

	return list.//
		stream().//
		sorted(Comparator.comparing(Resource::getName)).//
		collect(Collectors.toList());
    }

    @Override
    public Resource createNew(String file, InputStream inputStream, Long length, String contentType) throws IOException {

	GSLoggerFactory.getLogger(getClass()).debug("Creating new file [{}] STARTED", file);

	DatabaseFileResource out = null;

	ClonableInputStream clone = new ClonableInputStream(inputStream);

	if (clone.clone().available() == 0) {

	    GSLoggerFactory.getLogger(getClass()).debug("Stream empty");

	} else {

	    try {

		FolderEntry entry = null;
		EntryType type = null;

		if (file.equals(HarvestingProperties.FILE_NAME)) {

		    entry = FolderEntry.of(clone.clone());
		    type = EntryType.HARVESTING_PROPERTIES;
		} else if (file.equals(SourceStorageWorker.ERRORS_REPORT_FILE_NAME)) {

		    entry = FolderEntry.of(clone.clone());
		    type = EntryType.HARVESTING_ERROR_REPORT;
		} else if (file.equals(SourceStorageWorker.WARN_REPORT_FILE_NAME)) {

		    entry = FolderEntry.of(clone.clone());
		    type = EntryType.HARVESTING_WARN_REPORT;

		} else if (file.endsWith(SourceStorageWorker.DATA_FOLDER_POSTFIX)) {

		    entry = FolderEntry.of(new DataFolderIndexDocument(clone.clone()).getDocument());
		    type = EntryType.DATA_FOLDER_INDEX_DOC;

		} else {

		    GSResource gsResource = GSResource.create(clone.clone());

		    entry = FolderEntry.of(gsResource.asDocument(false));
		    type = EntryType.GS_RESOURCE;
		}

		database.getFolder(dir).store(file, entry, type);

	    } catch (Exception ex) {

		GSLoggerFactory.getLogger(getClass()).error(ex);
	    }
	}

	out = new DatabaseFileResource(database, dir, file);

	GSLoggerFactory.getLogger(getClass()).debug("Creating new file [{}] ENDED", file);

	return out;
    }

    @Override
    public void sendContent(OutputStream out, Range range, Map<String, String> params, String contentType) {

	XmlWriter w = new XmlWriter(out);
	w.open("html");
	w.open("head");

	w.close("head");
	w.open("body");
	w.begin("h1").open().writeText(this.getName()).close();
	w.open("table");

	for (Resource r : Optional.ofNullable(getChildren()).orElse(List.of())) {

	    w.open("tr");

	    w.open("td");

	    String path = r.toString();

	    w.begin("a").writeAtt("href", path).open().writeText(r.getName()).close();

	    w.close("td");

	    if (r instanceof DatabaseDirectoryResource) {

		w.begin("td").open().writeText(((DatabaseDirectoryResource) r).size() + "").close();
	    }

	    w.close("tr");
	}

	w.close("table");
	w.close("body");
	w.close("html");

	w.flush();
    }

    @Override
    public void delete() throws NotAuthorizedException, ConflictException, BadRequestException {

	GSLoggerFactory.getLogger(getClass()).debug("Deleting directory [{}] STARTED", this);

	try {
	    database.removeFolder(dir);

	} catch (GSException ex) {

	    GSLoggerFactory.getLogger(getClass()).error(ex);
	}

	GSLoggerFactory.getLogger(getClass()).debug("Deleting directory [{}] ENDED", this);
    }

    @Override
    public String getContentType(String accepts) {

	return "text/html";
    }

    @Override
    public Date getCreateDate() {

	return new Date();
    }

    @Override
    public String toString() {

	return dir;
    }

    /**
     * @return
     */
    private int size() {

	try {
	    return database.getFolder(dir).size();
	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e);
	}

	return -1;
    }

    @Override
    public Long getContentLength() {

	return null;
    }

    @Override
    public Long getMaxAgeSeconds(Auth auth) {

	return null;
    }

    @Override
    public Object authenticate(String user, String password) {

	return null;
    }

    @Override
    public boolean authorise(Request request, Method method, Auth auth) {

	return true;
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
    public Resource child(String childName) throws NotAuthorizedException, BadRequestException {

	return null;
    }

}
