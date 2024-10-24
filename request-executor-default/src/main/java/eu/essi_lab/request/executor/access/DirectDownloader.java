package eu.essi_lab.request.executor.access;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2024 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
import java.net.http.HttpHeaders;
import java.net.http.HttpResponse;
import java.util.AbstractMap.SimpleEntry;
import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.setting.DownloadSetting;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.StorageInfo;
import eu.essi_lab.request.executor.utils.MimeTypeConstants;
import eu.essi_lab.shared.resultstorage.ResultStorage;
import eu.essi_lab.shared.resultstorage.ResultStorageFactory;

public class DirectDownloader {

    protected String linkage;
    private Logger logger;

    public DirectDownloader(String linkage) {
	this.linkage = linkage;
	this.logger = GSLoggerFactory.getLogger(getClass());
    }

    public String download() {
	Downloader d = new Downloader();
	 Optional<HttpResponse<InputStream>> ret = d.downloadOptionalResponse(linkage);

	InputStream stream;

	String extension = null;

	if (ret.isPresent()) {
	    HttpResponse<InputStream> response = ret.get();
	    stream = response.body();
	    HttpHeaders headers = response.headers();
	    String ct = null;

	    Optional<String> cType = headers.firstValue("Content-Type");
	    if (cType.isEmpty()) {
		cType = headers.firstValue("content-type");
	    }

	    if (cType.isPresent()) {

		ct = cType.get();

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

	try {

	    String objectName = "unnamed-result-" + System.currentTimeMillis() + extension;

	    DownloadSetting downloadSetting = ConfigurationWrapper.getDownloadSetting();

	    ResultStorage storage = null;
	    StorageInfo resultStorageURI = downloadSetting.getStorageUri();

	    switch (downloadSetting.getDownloadStorage()) {

	    case LOCAL_DOWNLOAD_STORAGE:

		storage = ResultStorageFactory.createLocalResultStorage(resultStorageURI);

	    case S3_DOWNLOAD_STORAGE:

		storage = ResultStorageFactory.createAmazonS3ResultStorage(resultStorageURI);
	    }

	    String get = storage.getStorageLocation(objectName);

	    logger.info("Storing to {} STARTED", get);

	    //
	    //
	    //

	    File tmpFile = File.createTempFile(DirectDownloader.this.getClass().getSimpleName(), ".tmp");
	    tmpFile.deleteOnExit();
	    FileOutputStream fos = new FileOutputStream(tmpFile);
	    IOUtils.copy(stream, fos);
	    fos.close();
	    storage.store(objectName, tmpFile);
	    tmpFile.delete();

	    logger.info("Storing to {} ENDED", get);

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
}
