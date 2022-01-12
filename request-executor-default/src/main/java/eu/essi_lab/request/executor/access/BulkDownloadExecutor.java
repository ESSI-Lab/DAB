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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.slf4j.Logger;

import eu.essi_lab.lib.net.utils.Downloader;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.BulkDownloadMessage;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.count.CountSet;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.data.DataObject;
import eu.essi_lab.model.resource.data.DataReference;
import eu.essi_lab.model.resource.data.DataReferences;
import eu.essi_lab.request.executor.AbstractAuthorizedExecutor;
import eu.essi_lab.request.executor.IBulkDownloadExecutor;
public class BulkDownloadExecutor extends AbstractAuthorizedExecutor implements IBulkDownloadExecutor {

    Logger logger = GSLoggerFactory.getLogger(getClass());

    @Override
    public CountSet count(BulkDownloadMessage message) throws GSException {

	CountSet count = new CountSet();

	DataReferences dataReferences = message.getDataReferences();

	if (dataReferences != null) {
	    count.setPageCount(dataReferences.getReferences().size());
	}

	return count;
    }

    /**
     * @param accessMessage
     * @return
     * @throws GSException
     */
    @Override
    public ResultSet<DataObject> retrieve(BulkDownloadMessage message) throws GSException {

	logger.info("[BULK] Starting bulk download");
	ResultSet<DataObject> out = new ResultSet<>();
	DataReferences references = message.getDataReferences();

	try {

	    ExecutorService EXEC = Executors.newCachedThreadPool();
	    List<Callable<String>> tasks = new ArrayList<>();
	    List<DataReference> refs = references.getReferences();
	    for (DataReference reference : refs) {
		Callable<String> c = new Callable<String>() {
		    @Override
		    public String call() throws Exception {
			long start = System.currentTimeMillis();
			String linkage = reference.getLinkage();
			DirectDownloader d;
			if (linkage.contains("gwps/dataset")) {
			    d = new GWPSDownloader(linkage);
			} else {
			    d = new DirectDownloader(linkage);
			}
			String completed = d.download();
			long end = System.currentTimeMillis();
			return (end - start) + "@" + completed;

		    }
		};
		tasks.add(c);
	    }
	    List<Future<String>> results = EXEC.invokeAll(tasks);

	    logger.info("Multiple downloads finished, aggregating bulk download...");

	    File tmpZip = File.createTempFile("bulk-download-temp", ".zip");

	    tmpZip.deleteOnExit();

	    FileOutputStream fos = new FileOutputStream(tmpZip);
	    ZipOutputStream zipStream = new ZipOutputStream(fos);

	    Downloader downloader = new Downloader();

	    for (int i = 0; i < results.size(); i++) {
		DataReference reference = refs.get(i);
		String title = reference.getTitle();
		String linkage = reference.getLinkage();
		try {
		    Future<String> result = results.get(i);
		    String timeLink = result.get();
		    String[] split = timeLink.split("@");
		    String successMessage = "<success>\n";
		    successMessage += "<title>" + title + "</title>\n";
		    successMessage += "<linkage>" + linkage + "</linkage>\n";
		    successMessage += "<time>" + split[0] + "</time>\n";
		    successMessage += "<output>" + split[1] + "</output>\n";
		    successMessage += "</success>\n";
		    logger.info(successMessage);

		    // add entry to the ZIP

		    Optional<InputStream> optionalStream = downloader.downloadStream(split[1]);

		    if (optionalStream.isPresent()) {
			InputStream stream = optionalStream.get();
			String extension = split[1].contains(".") ? split[1].substring(split[1].lastIndexOf('.')) : ".bin";
			addEntry(zipStream, stream, title + extension);
		    } else {
			String msg = "Unable to download from the temporary result storage: " + split[1];
			throw new RuntimeException(msg);
		    }

		} catch (Exception e) {
		    String errorMessage = "<error><title>" + title + "</title><linkage>" + linkage + "</linkage><msg>" + e.getMessage()
			    + "</msg></error>";
		    logger.info(errorMessage);
		    ByteArrayInputStream bais = new ByteArrayInputStream(errorMessage.getBytes());
		    addEntry(zipStream, bais, title + ".error.log");
		}
	    }

	    zipStream.closeEntry();
	    zipStream.close();

	    DataObject result = new DataObject();
	    result.setFile(tmpZip);
	    out.setResultsList(Arrays.asList(result));

	    // uploading the zip to S3

	    // logger.info("Uploading the zip to S3...");
	    //
	    // try {
	    // StorageUri resultStorageURI = message.getUserJobStorageURI();
	    // if (resultStorageURI == null) {
	    // logger.error("This should not happen, as previously checked!");
	    // }
	    // String objectName = "user-request-bulk-result-" + System.currentTimeMillis() + ".zip";
	    //
	    // String get = resultStorageURI.getUri() + resultStorageURI.getStorageName() + "/" + objectName;
	    //
	    // logger.info("Storing bulk download to: " + get);
	    //
	    // ResultStorage storage = null;
	    //
	    // // AMAZON specific result storage
	    // if (resultStorageURI.getUri().contains("s3.amazonaws.com")) {
	    // storage = new AmazonResultStorage(resultStorageURI);
	    // }
	    //
	    // if (storage == null) {
	    // logger.error("Unable to connect to storage");
	    // }
	    //
	    // storage.store(objectName, tmpZip);
	    // tmpZip.delete();
	    //
	    // logger.info("Bulk download stored");
	    //
	    // } catch (Exception e) {
	    // e.printStackTrace();
	    // logger.error("Storing failed: " + e.getMessage());
	    // }

	    logger.info("[BULK] Ending bulk download");

	    return out;

	} catch (Exception e) {
	    throw new GSException();
	}

    }

    @Override
    public boolean isAuthorized(BulkDownloadMessage message) throws GSException {

	return true;
    }

    private static void addEntry(ZipOutputStream zipStream, InputStream stream, String fileName) throws Exception {
	byte[] buffer = new byte[1024];
	ZipEntry zipEntry = new ZipEntry(fileName);
	zipStream.putNextEntry(zipEntry);
	int len;
	while ((len = stream.read(buffer)) > 0) {
	    zipStream.write(buffer, 0, len);
	}
	stream.close();
    }

}
