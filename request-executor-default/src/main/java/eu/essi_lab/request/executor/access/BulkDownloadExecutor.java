package eu.essi_lab.request.executor.access;

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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.BulkDownloadMessage;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.count.CountSet;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.data.DataObject;
import eu.essi_lab.model.resource.data.DataReference;
import eu.essi_lab.model.resource.data.DataReferences;
import eu.essi_lab.request.executor.AbstractAuthorizedExecutor;
import eu.essi_lab.request.executor.IBulkDownloadExecutor;

/**
 * @author boldrini
 */
public class BulkDownloadExecutor extends AbstractAuthorizedExecutor implements IBulkDownloadExecutor {

    /**
     * 
     */
    private static final String BULK_DOWNLOAD_EXECUTOR_ERROR = "BULK_DOWNLOAD_EXECUTOR_ERROR";

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

	GSLoggerFactory.getLogger(getClass()).info("[BULK] Starting bulk download");

	ResultSet<DataObject> out = new ResultSet<>();
	DataReferences references = message.getDataReferences();

	try {

	    ExecutorService EXEC = Executors.newCachedThreadPool();

	    List<Callable<String>> tasks = new ArrayList<>();
	    List<DataReference> refs = references.getReferences();

	    // addReferences(refs);

	    for (DataReference reference : refs) {

		Callable<String> c = new Callable<String>() {

		    @Override
		    public String call() throws Exception {

			long start = System.currentTimeMillis();
			String linkage = reference.getLinkage();

			DirectDownloader d;
			if (linkage.contains("worldcereal/query")) {
			    d = new WorldCerealDownloader(linkage);
			} else if (linkage.contains("gwps/dataset")) {
			    d = new GWPSDownloader(linkage);
			} else {
			    d = new DirectDownloader(linkage);
			}

			synchronized (BulkDownloadExecutor.class) {

			    String completed = d.download();
			    long end = System.currentTimeMillis();
			    return (end - start) + "@" + completed;
			}
		    }
		};

		tasks.add(c);
	    }

	    List<Future<String>> results = EXEC.invokeAll(tasks);

	    GSLoggerFactory.getLogger(getClass()).info("Multiple downloads finished, aggregating bulk download...");

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
		    synchronized (BulkDownloadExecutor.class) {

			Future<String> result = results.get(i);

			String timeLink = result.get();
			String[] split = timeLink.split("@");

			String successMessage = "<success>\n";
			successMessage += "<title>" + title + "</title>\n";
			successMessage += "<linkage>" + linkage + "</linkage>\n";
			successMessage += "<time>" + split[0] + "</time>\n";
			successMessage += "<output>" + split[1] + "</output>\n";
			successMessage += "</success>\n";

			GSLoggerFactory.getLogger(getClass()).info(successMessage);

			//
			// adds entry to the ZIP
			//
			Optional<InputStream> optionalStream = downloader.downloadOptionalStream(split[1]);

			if (optionalStream.isPresent()) {

			    InputStream stream = optionalStream.get();
			    // worldcereal-use-case (UNZIP Files)
			    if (linkage.contains("worldcereal/query")) {
				try (ZipInputStream zis = new ZipInputStream(stream)) {
				    ZipEntry entry;
				    while ((entry = zis.getNextEntry()) != null) {
					if (!entry.isDirectory()) {
					    // Read the zip entry's content into a ByteArrayOutputStream
					    ByteArrayOutputStream baos = new ByteArrayOutputStream();
					    byte[] buffer = new byte[1024];
					    int len;
					    while ((len = zis.read(buffer)) > 0) {
						baos.write(buffer, 0, len);
					    }
					    baos.close();

					    // Convert the ByteArrayOutputStream to a ByteArrayInputStream
					    ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
					    String fileName = entry.getName();
					    String extension = fileName.contains(".") ? fileName.substring(fileName.lastIndexOf('.'))
						    : ".bin";

					    // Store the ByteArrayInputStream in the ZIP file
					    addEntry(zipStream, bais, title + extension);
					}

					// Close the current entry
					zis.closeEntry();
				    }
				} finally {
				    if (stream != null)
					stream.close();
				}
			    } else {

				String extension = split[1].contains(".") ? split[1].substring(split[1].lastIndexOf('.')) : ".bin";

				addEntry(zipStream, stream, title + extension);
			    }

			} else {
			    String msg = "Unable to download from the temporary result storage: " + split[1];
			    throw new RuntimeException(msg);
			}
		    }
		} catch (Exception e) {

		    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);

		    String errorMessage = "<error><title>" + title + "</title><linkage>" + linkage + "</linkage><msg>" + e.getMessage()
			    + "</msg></error>";

		    ByteArrayInputStream bais = new ByteArrayInputStream(errorMessage.getBytes());
		    addEntry(zipStream, bais, title + ".error.log");
		}
	    }

	    zipStream.closeEntry();
	    zipStream.close();

	    DataObject result = new DataObject();
	    result.setFile(tmpZip);
	    out.setResultsList(Arrays.asList(result));

	    GSLoggerFactory.getLogger(getClass()).info("[BULK] Ending bulk download");

	    return out;

	} catch (Exception e) {

	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    BULK_DOWNLOAD_EXECUTOR_ERROR);
	}
    }

    @Override
    public boolean isAuthorized(BulkDownloadMessage message) throws GSException {

	return true;
    }

    /**
     * @param zipStream
     * @param stream
     * @param fileName
     * @throws Exception
     */
    private void addEntry(ZipOutputStream zipStream, InputStream stream, String fileName) throws Exception {
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
