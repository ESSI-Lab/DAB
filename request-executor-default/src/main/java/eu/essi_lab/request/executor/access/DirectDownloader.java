package eu.essi_lab.request.executor.access;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.AbstractMap.SimpleEntry;
import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.slf4j.Logger;

import eu.essi_lab.configuration.ConfigurationUtils;
import eu.essi_lab.lib.net.utils.Downloader;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.StorageUri;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.request.executor.schedule.AmazonResultStorage;
import eu.essi_lab.request.executor.schedule.ResultStorage;
import eu.essi_lab.request.executor.utils.MimeTypeConstants;

public class DirectDownloader {

    protected String linkage;
    private Logger logger;

    public DirectDownloader(String linkage) {
	this.linkage = linkage;
	this.logger = GSLoggerFactory.getLogger(getClass());
    }

    public String download() {
	Downloader d = new Downloader();
	Optional<SimpleEntry<Header[], InputStream>> ret = d.downloadHeadersAndBody(linkage);

	InputStream stream;

	String extension = null;

	if (ret.isPresent()) {
	    SimpleEntry<Header[], InputStream> headersAndBody = ret.get();
	    stream = headersAndBody.getValue();
	    Header[] header = headersAndBody.getKey();
	    String ct = null;
	    for (Header h : header) {
		if (h.getName().contains("Content-Type")) {
		    ct = h.getValue();
		    break;
		}

	    }
	    if (ct != null) {
		String ext = MimeTypeConstants.getTypeExtension(ct);

		if (ext != null) {
		    extension = "." + ext;
		} else {
		    extension = guessExtension();
		}

	    } else {
		extension = guessExtension();
	    }

	} else {
	    String msg = "Failed to download from " + linkage + ". Remote host offline?";

	    GSLoggerFactory.getLogger(getClass()).info(msg);

	    throw new RuntimeException(msg);
	}

	String objectName = "unnamed-result-" + System.currentTimeMillis() + extension;

	try {
	    StorageUri resultStorageURI = ConfigurationUtils.getUserJobStorageURI();

	    String get = resultStorageURI.getUri() + resultStorageURI.getStorageName() + "/" + objectName;

	    logger.info("Storing to: {}", get);

	    ResultStorage storage = null;

	    // AMAZON specific result storage
	    if (resultStorageURI.getUri().contains("s3.amazonaws.com")) {
		storage = new AmazonResultStorage(resultStorageURI);
	    }

	    if (storage == null) {
		logger.error("Unable to connect to storage");
		throw new GSException();
	    }

	    File tmpFile = File.createTempFile(DirectDownloader.this.getClass().getSimpleName(), ".tmp");
	    tmpFile.deleteOnExit();
	    FileOutputStream fos = new FileOutputStream(tmpFile);
	    IOUtils.copy(stream, fos);
	    fos.close();
	    storage.store(objectName, tmpFile);
	    tmpFile.delete();

	    logger.info("Result stored");

	    return get;
	} catch (Exception e) {
	    String msg = "Failed trying to store direct downloaded file.";

	    GSLoggerFactory.getLogger(getClass()).info(msg);

	    throw new RuntimeException(msg);
	}

    }

    private String guessExtension() {
	String extension = ".dat";
	int lastIndex = linkage.lastIndexOf(".");
	if (lastIndex != -1) {
	    String tmp = linkage.substring(lastIndex);
	    if (tmp.length() < 5) {
		extension = tmp;
	    }
	}
	return extension;
    }

    public static void main(String[] args) {

    }

}
