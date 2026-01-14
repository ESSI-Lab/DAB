package eu.essi_lab.profiler.om;

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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.http.client.utils.URIBuilder;
import org.h2.util.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.lib.net.s3.S3TransferWrapper;
import eu.essi_lab.lib.utils.FileUtils;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.messages.web.WebRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;

public class DataDownloaderTool {

    public DataDownloaderTool() {

    }

    /**
     * @param s3wrapper
     * @param folder
     * @param requestURL something like
     *        "http://localhost:9090/gs-service/services/essi/token/my-token/view/his-central/om-api/observations?includeData=true&asynchDownload=true&observedProperty=precipitation&ontology=his-central&north=42.492&south=42.252&east=11.094&west=10.693&beginPosition=2024-01-01&endPosition=2025-01-01"
     * @param operationId
     * @param asynchDownloadName
     */
    public File download(S3TransferWrapper s3wrapper, String bucket, String requestURL, String operationId, String downloadName) {

	GSLoggerFactory.getLogger(getClass()).info("Started asynch download of {}", requestURL);

	try {
	    JSONObject msg = new JSONObject();
	    msg.put("id", operationId);
	    msg.put("status", "Started");
	    msg.put("downloadName", downloadName);
	    msg.put("timestamp", ISO8601DateTimeUtils.getISO8601DateTime());
	    OMHandler.status(s3wrapper, bucket, operationId, msg);
	} catch (Exception e) {
	    e.printStackTrace();
	    GSLoggerFactory.getLogger(getClass()).error(e);
	}

	File zipFile = null;
	Path tempPath = null;

	try {
	    zipFile = File.createTempFile(getClass().getSimpleName(), ".zip");

	    File tempDirFile = FileUtils.getTempDir(StringUtils.urlEncode(operationId), false);

	    tempPath = tempDirFile.toPath();

	    if (tempDirFile.exists()) {

		FileUtils.clearFolder(tempPath.toFile(), false);

	    } else {

		tempDirFile.mkdirs();
	    }

	} catch (IOException e) {

	    GSLoggerFactory.getLogger(getClass()).error(e);
	}

	GSLoggerFactory.getLogger(getClass()).info("Temporary directory created at: {}", tempPath.toAbsolutePath());

	File userRequestFile = new File(tempPath.toFile(), "log.txt");
	Date dateStart = new Date();

	// remove download parameters to find out base URL
	requestURL = requestURL.replace("downloads?", "observations?");
	requestURL = removeParameter(requestURL, "asynchDownload", "true");
	requestURL = removeParameter(requestURL, "includeData", "true");

	URI uri;
	String format = null;
	try {
	    uri = new URI(requestURL);
	    URIBuilder uriBuilder = new URIBuilder(uri);
	    format = uriBuilder.getQueryParams().stream().filter(p -> p.getName().equals("format")).map(p -> p.getValue()).findFirst()
		    .orElse(null);
	} catch (URISyntaxException e) {
	    e.printStackTrace();
	    GSLoggerFactory.getLogger(getClass()).error(e);
	}

	if (format != null) {
	    requestURL = removeParameter(requestURL, "format", format);

	}

	boolean firstLoop = true;
	String resumptionToken = null;
	int blocks = 0;

	int resources = 0;
	int errors = 0;
	BigDecimal size = BigDecimal.ZERO;

	while (firstLoop || resumptionToken != null) {

	    firstLoop = false;

	    GSLoggerFactory.getLogger(getClass()).info("Listing block {}", ++blocks);

	    String listURL = requestURL;

	    if (resumptionToken != null) {
		listURL = listURL + "&resumptionToken=" + resumptionToken;
		listURL = listURL.replace("?&", "?");
	    }

	    OMHandler omHandler = new OMHandler();

	    Optional<JSONObject> response = omHandler.getJSONResponse(WebRequest.createGET(listURL));

	    long last = System.currentTimeMillis();

	    if (response.isPresent()) {

		JSONObject json = response.get();

		if (json.has("completed") && json.getBoolean("completed") == false && json.has("resumptionToken")) {
		    resumptionToken = json.getString("resumptionToken");
		} else {
		    resumptionToken = null;
		}

		if (json.has("member")) {

		    JSONArray members = json.getJSONArray("member");
		    GSLoggerFactory.getLogger(getClass()).info("Retrieved block of size {}, resumption token {}", members.length(),
			    resumptionToken);
		    if (members.length() == 0) {
			break;
		    }
		    // for each observation
		    for (int i = 0; i < members.length(); i++) {
			JSONObject member = members.getJSONObject(i);
			String id = member.getString("id");
			String observedPropertyTitle = id;
			JSONObject observedProperty = member.optJSONObject("observedProperty");
			if (observedProperty!=null){
			    observedPropertyTitle = observedProperty.optString("title",observedPropertyTitle);
			}
			String foiTitle = id;
			JSONObject foi = member.optJSONObject("featureOfInterest");
			if (foi!=null){
			    foiTitle = foi.optString("title",foi.optString("href",foiTitle));
			}

			String sourceId = "unknown";

			JSONArray parameters = member.getJSONArray("parameter");
			for (int j = 0; j < parameters.length(); j++) {
			    JSONObject parameter = parameters.getJSONObject(j);
			    String name = parameter.getString("name");
			    String value = parameter.getString("value");
			    if (name.equals("source")) {
				sourceId = value;
			    }
			}
			if (sourceId.equals("unknown")){
			    for (int j = 0; j < parameters.length(); j++) {
				JSONObject parameter = parameters.getJSONObject(j);
				String name = parameter.getString("name");
				String value = parameter.getString("value");
				if (name.equals("sourceId")) {
				    sourceId = value;
				}
			    }
			}

			// download single observation URL
			String downloadURL = requestURL;
			downloadURL = addParameter(downloadURL, "observationIdentifier", id);
			downloadURL = addParameter(downloadURL, "includeData", "true");
			if (format != null) {
			    downloadURL = addParameter(downloadURL, "format", format);
			}
			downloadURL = removeParameter(downloadURL, "asynchDownloadName", format);
			downloadURL = removeParameter(downloadURL, "eMailNotifications", format);

			// write
			File sourceDir = new File(tempPath.toFile(), sourceId);

			if (!sourceDir.exists()) {
			    sourceDir.mkdir();
			}

			File propertyDir = new File(sourceDir, observedPropertyTitle);

			if (!propertyDir.exists()) {
			    propertyDir.mkdir();
			}


			OMFormat oFormat = OMFormat.decode(format);
			if (oFormat == null) {
			    oFormat = OMFormat.JSON;
			}
			String extension = oFormat.getExtension();

			String baseName = foiTitle;

			File logFile = new File(propertyDir, "log_"+baseName+".txt");
			File dataFile = new File(propertyDir, "data_"+baseName+ extension);

			WebRequest get2 = WebRequest.createGET(downloadURL);

			resources++;

			try {

			    omHandler.handle(new FileOutputStream(dataFile), get2);

			    BigDecimal sizeInMB = null;
			    if (dataFile.exists()) {
				long sizeInBytes = dataFile.length();
				sizeInMB = BigDecimal.valueOf(sizeInBytes).divide(BigDecimal.valueOf(1024 * 1024), 2, RoundingMode.HALF_UP);
			    } else {
				sizeInMB = BigDecimal.ZERO;
			    }
			    size = size.add(sizeInMB);

			} catch (Exception e) {
			    errors++;
			    write("status: 500 \nexception " + e.getMessage(), logFile);
			    GSLoggerFactory.getLogger(getClass()).error(e);
			}
			try {
			    long current = System.currentTimeMillis();
			    long elapsed = current - last;

			    //

			    String key = "data-downloads/" + operationId + "-cancel";
			    HeadObjectResponse metadata = s3wrapper.getObjectMetadata(bucket, key);
			    if (metadata != null) {
				s3wrapper.deleteObject(bucket, key);
				return null;
			    }

			    // gives status updates not before 5000 ms
			    if (elapsed > 5000) {

				last = current;

				JSONObject msg = new JSONObject();
				msg.put("id", operationId);
				String errorInfo = "";
				if (errors > 0) {
				    errorInfo = " (" + errors + " of them failed)";
				}
				msg.put("status", resources + " downloads" + errorInfo + ", " + size + "MB");
				msg.put("downloadName", downloadName);
				msg.put("timestamp", ISO8601DateTimeUtils.getISO8601DateTime());
				OMHandler.status(s3wrapper, bucket, operationId, msg);
			    }
			} catch (Exception e) {
			    e.printStackTrace();
			    GSLoggerFactory.getLogger(getClass()).error(e);
			}
		    }
		} else {
		    break;
		}
	    }
	}

	write("request: " + requestURL + "\ndate start: " + ISO8601DateTimeUtils.getISO8601DateTime(dateStart) + "\ndate end: "
		+ ISO8601DateTimeUtils.getISO8601DateTime(), userRequestFile);

	try {
	    GSLoggerFactory.getLogger(getClass()).info("Zipping folder {}", tempPath);
	    zipFolder(tempPath, zipFile.toPath());
	} catch (IOException e) {
	    GSLoggerFactory.getLogger(getClass()).error("Error zipping folder");
	}

	try {

	    GSLoggerFactory.getLogger(getClass()).info("Removing folder");

	    FileUtils.clearFolder(tempPath.toFile(), true);

	} catch (IOException e) {

	    GSLoggerFactory.getLogger(getClass()).error(e);
	}

	GSLoggerFactory.getLogger(getClass()).info("Ended asynch download of {} to file: {}", requestURL, zipFile.getAbsolutePath());

	return zipFile;

    }

    /**
     * @param sourceFolderPath
     * @param zipPath
     * @throws IOException
     */
    private void zipFolder(Path sourceFolderPath, Path zipPath) throws IOException {
	try (ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(zipPath.toFile()))) {
	    Files.walk(sourceFolderPath).filter(path -> !Files.isDirectory(path)).forEach(path -> {
		ZipEntry zipEntry = new ZipEntry(sourceFolderPath.relativize(path).toString().replace("\\", "/"));
		try (InputStream inputStream = Files.newInputStream(path)) {
		    zipOutputStream.putNextEntry(zipEntry);
		    byte[] buffer = new byte[1024];
		    int length;
		    while ((length = inputStream.read(buffer)) > 0) {
			zipOutputStream.write(buffer, 0, length);
		    }
		    zipOutputStream.closeEntry();
		} catch (IOException e) {
		    throw new UncheckedIOException(e);
		}
	    });
	}
    }

    /**
     * @param baseURL
     * @param parameter
     * @param value
     * @return
     */
    private String removeParameter(String baseURL, String parameter, String value) {
	baseURL = baseURL.replace("&" + parameter + "=" + value, "");
	baseURL = baseURL.replace("?" + parameter + "=" + value, "?");
	baseURL = baseURL.replace("?&", "?");
	return baseURL;
    }

    /**
     * @param baseURL
     * @param parameter
     * @param value
     * @return
     */
    private String addParameter(String baseURL, String parameter, String value) {
	if (baseURL.endsWith("?") || baseURL.endsWith("&")) {
	    return baseURL + parameter + "=" + value;
	}
	if (!baseURL.contains("?")) {
	    return baseURL + "?" + parameter + "=" + value;
	} else {
	    return baseURL + "&" + parameter + "=" + value;
	}
    }

    /**
     * @param string
     * @param logFile
     */
    private void write(String string, File logFile) {
	try {
	    FileOutputStream fos = new FileOutputStream(logFile);
	    fos.write(string.getBytes(StandardCharsets.UTF_8));
	    fos.close();

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e);
	}
    }
}
