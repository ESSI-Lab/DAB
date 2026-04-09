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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import eu.essi_lab.api.database.Database;
import eu.essi_lab.api.database.DatabaseFolder;
import eu.essi_lab.api.database.DatabaseFolder.EntryType;
import eu.essi_lab.api.database.DatabaseFolder.FolderEntry;
import eu.essi_lab.api.database.SourceStorageWorker;
import eu.essi_lab.api.database.SourceStorageWorker.DataFolderIndexDocument;
import eu.essi_lab.lib.utils.ClonableInputStream;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.HarvestingProperties;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;
import io.milton.http.Auth;
import io.milton.http.Range;
import io.milton.http.Request;
import io.milton.http.Request.Method;
import io.milton.http.XmlWriter;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.ConflictException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.http.exceptions.NotFoundException;
import io.milton.resource.CollectionResource;
import io.milton.resource.DeletableResource;
import io.milton.resource.GetableResource;
import io.milton.resource.PropFindableResource;
import io.milton.resource.PutableResource;
import io.milton.resource.Resource;

/**
 * @author Fabrizio
 */
public class DatabaseDirectoryResource
	implements GetableResource, DeletableResource, PropFindableResource, CollectionResource, PutableResource {

    private String dir;
    private Database database;

    /**
     * @param dir
     * @param database
     */
    public DatabaseDirectoryResource(Database database, String dir) {

	this.dir = dir;
	this.database = database;

	GSLoggerFactory.getLogger(getClass()).info("Directory resource [{}] created", this);
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
    public Resource child(String childName) throws NotAuthorizedException, BadRequestException {

	return null;
    }

    @Override
    public List<Resource> getChildren() throws NotAuthorizedException, BadRequestException {

	ArrayList<Resource> list = new ArrayList<>();

	try {
	    if ("/".equals(dir) || "".equals(dir)) {

		database.getMetaFolders().//
			parallelStream().//
			map(f -> new DatabaseDirectoryResource(database, f.getName())).//
			forEach(f -> list.add(f));

		database.getDataFolders().//
			parallelStream().//
			map(f -> new DatabaseDirectoryResource(database, f.getName())).//
			forEach(f -> list.add(f));

		DatabaseFolder usersFolder = database.getUsersFolder();

		if (usersFolder != null) {

		    list.add(new DatabaseDirectoryResource(database, usersFolder.getName()));
		}

		DatabaseFolder viewFolder = database.getViewFolder(false);

		if (viewFolder != null) {

		    list.add(new DatabaseDirectoryResource(database, viewFolder.getName()));
		}

		DatabaseFolder cacheFolder = database.getCacheFolder();

		if (cacheFolder != null) {

		    list.add(new DatabaseDirectoryResource(database, cacheFolder.getName()));
		}

	    } else {

		DatabaseFolder folder = database.getFolder(dir);

		List<String> keys = Arrays.asList(folder.listKeys());

//		keys = keys.subList(0, Math.min(1000, keys.size()));

		keys.forEach(key -> list.add(new DatabaseFileResource(//
			database, dir, key)));
	    }

	} catch (Exception ex) {

	    GSLoggerFactory.getLogger(getClass()).error(ex);
	}

	return list.//
		stream().//
		sorted((r1, r2) -> r1.getName().compareTo(r2.getName())).//
		collect(Collectors.toList());
    }

    @Override
    public Resource createNew(String file, InputStream inputStream, Long length, String contentType)
	    throws IOException, ConflictException, NotAuthorizedException, BadRequestException {

	GSLoggerFactory.getLogger(getClass()).info("Creating new file [{}] STARTED", file);

	DatabaseFileResource out = null;

	ClonableInputStream clone = new ClonableInputStream(inputStream);

	if (clone.clone().available() == 0) {

	    GSLoggerFactory.getLogger(getClass()).info("Stream empty");

	} else {

	    try {

		FolderEntry entry = null;
		EntryType type = null;

		if (file.equals(HarvestingProperties.FILE_NAME)) {

		    entry = FolderEntry.of(clone.clone());
		    type = EntryType.HARVESTING_PROPERTIES;
		}

		else if (file.equals(SourceStorageWorker.ERRORS_REPORT_FILE_NAME)) {

		    entry = FolderEntry.of(clone.clone());
		    type = EntryType.HARVESTING_ERROR_REPORT;
		}

		else if (file.equals(SourceStorageWorker.WARN_REPORT_FILE_NAME)) {

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

	GSLoggerFactory.getLogger(getClass()).info("Creating new file [{}] ENDED", file);

	return out;
    }

    @Override
    public void sendContent(OutputStream out, Range range, Map<String, String> params, String contentType)
	    throws IOException, NotAuthorizedException, BadRequestException, NotFoundException {

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

	    String path = buildHref(r.toString(), r.getName());

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

	GSLoggerFactory.getLogger(getClass()).info("Deleting directory [{}] STARTED", this);

	try {
	    database.removeFolder(dir);

	} catch (GSException ex) {

	    GSLoggerFactory.getLogger(getClass()).error(ex);
	}

	GSLoggerFactory.getLogger(getClass()).info("Deleting directory [{}] ENDED", this);
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

    /**
     * @param uri
     * @param name
     * @return
     */
    private String buildHref(String uri, String name) {

	String abUrl = uri;

	if (!abUrl.endsWith("/")) {
	    abUrl += "/";
	}

	return abUrl + name;
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
    public String checkRedirect(Request request) throws NotAuthorizedException, BadRequestException {

	return null;
    }

}
