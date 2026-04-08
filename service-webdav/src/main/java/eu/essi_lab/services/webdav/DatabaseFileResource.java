/**
 * 
 */
package eu.essi_lab.services.webdav;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import eu.essi_lab.api.database.Database;
import eu.essi_lab.api.database.DatabaseFolder;
import eu.essi_lab.api.database.DatabaseFolder.EntryType;
import eu.essi_lab.api.database.DatabaseFolder.FolderEntry;
import eu.essi_lab.api.database.SourceStorageWorker;
import eu.essi_lab.api.database.SourceStorageWorker.DataFolderIndexDocument;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.StringUtils;
import eu.essi_lab.messages.HarvestingProperties;
import eu.essi_lab.model.resource.GSResource;
import io.milton.http.Auth;
import io.milton.http.Range;
import io.milton.http.Request;
import io.milton.http.Request.Method;
import io.milton.http.exceptions.BadRequestException;
import io.milton.http.exceptions.ConflictException;
import io.milton.http.exceptions.NotAuthorizedException;
import io.milton.http.exceptions.NotFoundException;
import io.milton.resource.DeletableResource;
import io.milton.resource.GetableResource;
import io.milton.resource.PropFindableResource;
import io.milton.resource.ReplaceableResource;

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

	GSLoggerFactory.getLogger(getClass()).info("File resource [{}] created", this);
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
    public void sendContent(OutputStream out, Range range, Map<String, String> params, String contentType)
	    throws IOException, NotAuthorizedException, BadRequestException, NotFoundException {

	GSLoggerFactory.getLogger(getClass()).info("Seding content of [{}] STARTED", this);

	try {

	    InputStream binary = database.getFolder(dir).getBinary(StringUtils.URLDecodeUTF8(file));

	    IOUtils.copy(binary, out);

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e);
	}

	GSLoggerFactory.getLogger(getClass()).info("Seding content of [{}] ENDED", this);
    }

    @Override
    public void replaceContent(InputStream inputStream, Long length) throws BadRequestException, ConflictException, NotAuthorizedException {

	GSLoggerFactory.getLogger(getClass()).info("Replacing content of [{}] STARTED", this);

	try {

	    FolderEntry entry = null;
	    EntryType type = null;

	    if (file.equals(HarvestingProperties.FILE_NAME)) {

		entry = FolderEntry.of(inputStream);
		type = EntryType.HARVESTING_PROPERTIES;
	    }

	    else if (file.equals(SourceStorageWorker.ERRORS_REPORT_FILE_NAME)) {

		entry = FolderEntry.of(inputStream);
		type = EntryType.HARVESTING_ERROR_REPORT;
	    }

	    else if (file.equals(SourceStorageWorker.WARN_REPORT_FILE_NAME)) {

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

	GSLoggerFactory.getLogger(getClass()).info("Replacing content of [{}] ENDED", this);
    }

    @Override
    public void delete() throws NotAuthorizedException, ConflictException, BadRequestException {

	GSLoggerFactory.getLogger(getClass()).info("Deleting file [{}] STARTED", this);

	try {
	    database.getFolder(dir).remove(StringUtils.URLDecodeUTF8(file));

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e);
	}

	GSLoggerFactory.getLogger(getClass()).info("Deleting ended [{}] STARTED", this);
    }

    @Override
    public String getContentType(String accepts) {

	return "application/octet-stream";
    }

    @Override
    public Long getContentLength() {

	return null;
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
    public Date getCreateDate() {

	return new Date();
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

    @Override
    public Long getMaxAgeSeconds(Auth auth) {

	return null;
    }
}
