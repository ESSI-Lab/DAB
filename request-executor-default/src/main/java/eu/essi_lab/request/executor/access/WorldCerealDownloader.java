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
import java.util.Optional;

import org.apache.commons.io.IOUtils;

import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.setting.DownloadSetting;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.StorageInfo;
import eu.essi_lab.request.executor.utils.MimeTypeConstants;
import eu.essi_lab.shared.resultstorage.ResultStorage;
import eu.essi_lab.shared.resultstorage.ResultStorageFactory;

public class WorldCerealDownloader extends DirectDownloader {

    public WorldCerealDownloader(String linkage) {
	super(linkage);
    }

    @SuppressWarnings({ "unchecked", "incomplete-switch" })
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
	    String name = null;
	    Optional<String> contentDisposition = headers.firstValue("Content-Disposition");
	    if (contentDisposition.isPresent()) {
		String attachmentFile = contentDisposition.get();
		if (attachmentFile != null && attachmentFile.contains("filename=")) {
		    String[] splittedString = attachmentFile.split("filename=");
		    if (splittedString.length > 1) {
			name = splittedString[1].replaceAll("\"", "").replace(".", "-" + System.currentTimeMillis() + ".");

		    }

		}

	    }

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
		    extension = ".json";
		}
	    }

	    try {

		String objectName = (name != null) ? name : "unnamed-result-" + System.currentTimeMillis() + extension;

		DownloadSetting downloadSetting = ConfigurationWrapper.getDownloadSetting();

		ResultStorage storage = null;
		StorageInfo resultStorageURI = downloadSetting.getStorageUri();

		switch (downloadSetting.getDownloadStorage()) {

		case LOCAL_DOWNLOAD_STORAGE:

		    storage = ResultStorageFactory.createLocalResultStorage(resultStorageURI);
		    break;
		case S3_DOWNLOAD_STORAGE:

		    storage = ResultStorageFactory.createAmazonS3ResultStorage(resultStorageURI);
		    break;
		}

		String get = storage.getStorageLocation(objectName);

		GSLoggerFactory.getLogger(getClass()).info("Storing to {} STARTED", get);

		//
		// store file
		//

		File tmpFile = File.createTempFile(WorldCerealDownloader.this.getClass().getSimpleName(), ".tmp");
		tmpFile.deleteOnExit();
		FileOutputStream fos = new FileOutputStream(tmpFile);
		IOUtils.copy(stream, fos);
		fos.close();
		storage.store(objectName, tmpFile);
		tmpFile.delete();

		GSLoggerFactory.getLogger(getClass()).info("Storing to {} ENDED", get);

		return get;
	    } catch (Exception e) {

		String msg = "Failed trying to store direct downloaded file.";

		GSLoggerFactory.getLogger(getClass()).info(msg);

		throw new RuntimeException(msg);
	    }

	} else {
	    String msg = "Failed to download from " + linkage + ". Remote host offline?";

	    GSLoggerFactory.getLogger(getClass()).info(msg);

	    throw new RuntimeException(msg);
	}

    }

}