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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import eu.essi_lab.cfga.scheduler.SchedulerJobStatus;
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
     * Performs a full download with no size limits. For backward compatibility.
     *
     * @param s3wrapper
     * @param bucket
     * @param requestURL
     * @param operationId
     * @param downloadName
     * @return the zip file, or null if canceled
     */
    public File download(S3TransferWrapper s3wrapper, String bucket, String requestURL, String operationId, String downloadName, SchedulerJobStatus status) {
	DownloadPartResult result = downloadPart(s3wrapper, bucket, requestURL, operationId, downloadName, Integer.MAX_VALUE,
		Integer.MAX_VALUE, null, BigDecimal.ZERO, 1, new ArrayList<>(), status);
	return result != null ? result.getPartFile() : null;
    }

    /**
     * Performs a partial download. When current part size exceeds maxDownloadPartSizeMB, returns a part and resumption token. When total
     * size would exceed maxDownloadSizeMB, returns the last part with maxSizeReached and error message. Caller publishes each part, sends
     * notification, then continues with the resumption token until null.
     *
     * @param s3wrapper
     * @param bucket
     * @param requestURL
     * @param operationId
     * @param downloadName
     * @param maxDownloadSizeMB               overall max size in MB (process ends when reached)
     * @param maxDownloadPartSizeMB           max size per part in MB (part returned and continued with token)
     * @param resumptionTokenFromPreviousPart token from previous part, or null for first part
     * @param totalDownloadedSoFarMB          sum of sizes of previous parts in MB
     * @param currentPartNumber               part number being downloaded (1-based), for progress messages
     * @param completedPartLocators           list of locators of completed parts (for progress UI), in order
     * @param status
     * @return result with part file, optional resumption token, and maxSizeReached/errorMessage
     */
    public DownloadPartResult downloadPart(S3TransferWrapper s3wrapper, String bucket, String requestURL, String operationId,
	    String downloadName, int maxDownloadSizeMB, int maxDownloadPartSizeMB, String resumptionTokenFromPreviousPart,
	    BigDecimal totalDownloadedSoFarMB, int currentPartNumber, List<String> completedPartLocators, SchedulerJobStatus status) {

	GSLoggerFactory.getLogger(getClass()).info("Started asynch download part of {}", requestURL);

	try {
	    if (s3wrapper != null && resumptionTokenFromPreviousPart == null) {
		JSONObject msg = new JSONObject();
		msg.put("id", operationId);
		msg.put("status", "Started");
		msg.put("downloadName", downloadName);
		msg.put("timestamp", ISO8601DateTimeUtils.getISO8601DateTime());
		OMHandler.status(s3wrapper, bucket, operationId, msg);
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	    GSLoggerFactory.getLogger(getClass()).error(e);
	}

	File zipFile = null;
	Path tempPath = null;

	try {
	    zipFile = File.createTempFile(getClass().getSimpleName(), ".zip");

	    File tempDirFile = FileUtils.getTempDir(StringUtils.urlEncode(operationId) + "_" + System.currentTimeMillis(), false);

	    tempPath = tempDirFile.toPath();

	    if (tempDirFile.exists()) {
		FileUtils.clearFolder(tempPath.toFile(), false);
	    } else {
		tempDirFile.mkdirs();
	    }
	} catch (IOException e) {
	    GSLoggerFactory.getLogger(getClass()).error(e);
	    return null;
	}

	GSLoggerFactory.getLogger(getClass()).info("Temporary directory created at: {}", tempPath.toAbsolutePath());

	File userRequestFile = new File(tempPath.toFile(), "log.txt");
	Date dateStart = new Date();

	String baseRequestURL = requestURL.replace("downloads?", "observations?");
	baseRequestURL = removeParameter(baseRequestURL, "asynchDownload", "true");
	baseRequestURL = removeParameter(baseRequestURL, "includeData", "true");

	URI uri;
	String format = null;
	try {
	    uri = new URI(baseRequestURL);
	    URIBuilder uriBuilder = new URIBuilder(uri);
	    format = uriBuilder.getQueryParams().stream().filter(p -> p.getName().equals("format")).map(p -> p.getValue()).findFirst()
		    .orElse(null);
	} catch (URISyntaxException e) {
	    e.printStackTrace();
	    GSLoggerFactory.getLogger(getClass()).error(e);
	}

	if (format != null) {
	    baseRequestURL = removeParameter(baseRequestURL, "format", format);
	}

	boolean firstLoop = (resumptionTokenFromPreviousPart == null);
	String resumptionToken = resumptionTokenFromPreviousPart;
	int blocks = 0;
	int resources = 0;
	int errors = 0;
	BigDecimal currentPartSizeMB = BigDecimal.ZERO;
	boolean partComplete = false;
	boolean maxSizeReached = false;
	String maxSizeErrorMessage = null;
	List<String> downloadedFileNames = new ArrayList<>();
	long last = System.currentTimeMillis();

	while (firstLoop || resumptionToken != null) {

	    firstLoop = false;

	    GSLoggerFactory.getLogger(getClass()).info("Listing block {}", ++blocks);

	    String listURL = addParameter(baseRequestURL, "limit", "1");
	    if (resumptionToken != null) {
		listURL = listURL + "&resumptionToken=" + resumptionToken;
		listURL = listURL.replace("?&", "?");
	    }

	    OMHandler omHandler = new OMHandler();
	    Optional<JSONObject> response = omHandler.getJSONResponse(WebRequest.createGET(listURL));

	    if (response.isPresent()) {

		JSONObject json = response.get();

		if (json.has("completed") && json.getBoolean("completed") == false && json.has("resumptionToken")) {
		    resumptionToken = json.getString("resumptionToken");
		} else {
		    resumptionToken = null;
		}

		// Look ahead: if next response would be empty and completed, treat current as last to avoid empty final part
		if (resumptionToken != null) {
		    String lookAheadURL = addParameter(baseRequestURL, "limit", "1");
		    lookAheadURL = lookAheadURL + "&resumptionToken=" + resumptionToken;
		    lookAheadURL = lookAheadURL.replace("?&", "?");
		    Optional<JSONObject> lookAheadResponse = omHandler.getJSONResponse(WebRequest.createGET(lookAheadURL));
		    if (lookAheadResponse.isPresent()) {
			JSONObject nextJson = lookAheadResponse.get();
			boolean nextCompleted = nextJson.optBoolean("completed", true);
			JSONArray nextMembers = nextJson.optJSONArray("member");
			boolean nextEmpty = (nextMembers == null || nextMembers.length() == 0);
			if (nextCompleted && nextEmpty) {
			    resumptionToken = null;
			    GSLoggerFactory.getLogger(getClass())
				    .info("Look ahead: next response empty and completed, treating current as last");
			}
		    }
		}

		if (json.has("member")) {

		    JSONArray members = json.getJSONArray("member");
		    GSLoggerFactory.getLogger(getClass())
			    .info("Retrieved block of size {}, resumption token {}", members.length(), resumptionToken);
		    if (members.length() == 0) {
			break;
		    }

		    for (int i = 0; i < members.length(); i++) {
			JSONObject member = members.getJSONObject(i);
			String id = member.getString("id");
			String observedPropertyTitle = id;
			JSONObject observedProperty = member.optJSONObject("observedProperty");
			if (observedProperty != null) {
			    observedPropertyTitle = observedProperty.optString("title", observedPropertyTitle);
			}
			String foiTitle = id;
			JSONObject foi = member.optJSONObject("featureOfInterest");
			if (foi != null) {
			    foiTitle = foi.optString("title", foi.optString("href", foiTitle));
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
			if (sourceId.equals("unknown")) {
			    for (int j = 0; j < parameters.length(); j++) {
				JSONObject parameter = parameters.getJSONObject(j);
				String name = parameter.getString("name");
				String value = parameter.getString("value");
				if (name.equals("sourceId")) {
				    sourceId = value;
				}
			    }
			}

			String downloadURL = baseRequestURL;
			downloadURL = addParameter(downloadURL, "observationIdentifier", id);
			downloadURL = addParameter(downloadURL, "includeData", "true");
			if (format != null) {
			    downloadURL = addParameter(downloadURL, "format", format);
			}
			downloadURL = removeParameter(downloadURL, "asynchDownloadName", format);
			downloadURL = removeParameter(downloadURL, "eMailNotifications", format);

			File sourceDir = new File(tempPath.toFile(), FileUtils.sanitizeForNtfs(sourceId));
			if (!sourceDir.exists()) {
			    sourceDir.mkdir();
			}
			File propertyDir = new File(sourceDir, FileUtils.sanitizeForNtfs(observedPropertyTitle));
			if (!propertyDir.exists()) {
			    propertyDir.mkdir();
			}

			OMFormat oFormat = OMFormat.decode(format);
			if (oFormat == null) {
			    oFormat = OMFormat.JSON;
			}
			String extension = oFormat.getExtension();
			String baseName = foiTitle;

			File logFile = new File(propertyDir, FileUtils.sanitizeForNtfs(baseName + "_" + id + "_log.txt"));

			File dataFile = new File(propertyDir, FileUtils.sanitizeForNtfs(baseName + "_" + id + "_data" + extension));

			WebRequest get2 = WebRequest.createGET(downloadURL);
			resources++;

			try {
			    omHandler.handle(new FileOutputStream(dataFile), get2);
			    BigDecimal sizeInMB = null;
			    if (dataFile.exists()) {
				long sizeInBytes = dataFile.length();
				sizeInMB = BigDecimal.valueOf(sizeInBytes).divide(BigDecimal.valueOf(1024 * 1024), 2, RoundingMode.HALF_UP);
				downloadedFileNames.add(tempPath.relativize(dataFile.toPath()).toString().replace("\\", "/"));
			    } else {
				sizeInMB = BigDecimal.ZERO;
			    }
			    currentPartSizeMB = currentPartSizeMB.add(sizeInMB);

			    if (totalDownloadedSoFarMB.add(currentPartSizeMB).compareTo(BigDecimal.valueOf(maxDownloadSizeMB)) >= 0) {
				maxSizeReached = true;
				maxSizeErrorMessage = "Maximum download size (" + maxDownloadSizeMB + " MB) reached. Download truncated.";
				break;
			    }
			    if (currentPartSizeMB.compareTo(BigDecimal.valueOf(maxDownloadPartSizeMB)) >= 0) {
				partComplete = true;
				break;
			    }
			} catch (Exception e) {
			    errors++;
			    write("status: 500 \nexception " + e.getMessage(), logFile);
			    GSLoggerFactory.getLogger(getClass()).error(e);
			}

			try {
			    long current = System.currentTimeMillis();
			    long elapsed = current - last;
			    if (s3wrapper != null) {
				String key = "data-downloads/" + operationId + "-cancel";
				HeadObjectResponse metadata = s3wrapper.getObjectMetadata(bucket, key);
				if (metadata != null) {
				    s3wrapper.deleteObject(bucket, key);
				    try {
					FileUtils.clearFolder(tempPath.toFile(), true);
				    } catch (IOException e) {
					GSLoggerFactory.getLogger(getClass()).error(e);
				    }
				    return new DownloadPartResult(null, null, false, null, null, null, false, new ArrayList<>());
				}
				if (elapsed > 5000) {
				    last = current;
				    JSONObject msg = new JSONObject();
				    msg.put("id", operationId);
				    String errorInfo = errors > 0 ? " (" + errors + " failed)" : "";
				    String progressDetail = resources + " files, " + currentPartSizeMB + " MB" + errorInfo;
				    msg.put("status", currentPartNumber > 0 ? "PartInProgress" : "InProgress");
				    msg.put("statusMessage", (currentPartNumber > 0 ? "Part " + currentPartNumber + ": " : "Downloading: ")
					    + progressDetail);
				    msg.put("statusMessageKey",
					    currentPartNumber > 0 ? "status_message_part_in_progress" : "status_message_downloading");
				    JSONObject statusParams = new JSONObject();
				    statusParams.put("fileCount", resources);
				    statusParams.put("sizeMb", currentPartSizeMB.toString());
				    statusParams.put("errorSuffix", errorInfo);
				    if (currentPartNumber > 0) {
					statusParams.put("part", currentPartNumber);
				    }
				    msg.put("statusMessageParams", statusParams);
				    msg.put("downloadName", downloadName);
				    msg.put("partNumber", completedPartLocators != null ? completedPartLocators.size() : 0);
				    if (completedPartLocators != null && !completedPartLocators.isEmpty()) {
					msg.put("locators", new JSONArray(completedPartLocators));
					msg.put("totalUncompressedSizeInMB", totalDownloadedSoFarMB);
				    }
				    msg.put("timestamp", ISO8601DateTimeUtils.getISO8601DateTime());
				    OMHandler.status(s3wrapper, bucket, operationId, msg);

				    status.clearMessages();
				    status.addInfoMessage(totalDownloadedSoFarMB + " MB downloaded");
				}
			    }
			} catch (Exception e) {
			    e.printStackTrace();
			    GSLoggerFactory.getLogger(getClass()).error(e);
			}
		    }

		    if (partComplete || maxSizeReached) {
			break;
		    }
		} else {
		    break;
		}
	    }
	}

	boolean isFinalPart = (maxSizeReached || resumptionToken == null);
	StringBuilder logContent = new StringBuilder();
	logContent.append("request: ").append(baseRequestURL).append("\n");
	logContent.append("date start: ").append(ISO8601DateTimeUtils.getISO8601DateTime(dateStart)).append("\n");
	logContent.append("date end: ").append(ISO8601DateTimeUtils.getISO8601DateTime()).append("\n\n");
	logContent.append("Part total size: ").append(currentPartSizeMB).append(" MB\n");
	logContent.append("Part is final: ").append(isFinalPart ? "yes" : "no").append("\n");
	logContent.append("Downloaded files:\n");
	for (String name : downloadedFileNames) {
	    logContent.append("  ").append(name).append("\n");
	}
	write(logContent.toString(), userRequestFile);

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

	long sizeInBytes = zipFile.length();
	BigDecimal sizeInMB = BigDecimal.valueOf(sizeInBytes).divide(BigDecimal.valueOf(1024 * 1024), 2, RoundingMode.UP);

	GSLoggerFactory.getLogger(getClass()).info("Ended asynch download part of {} to file: {}", requestURL, zipFile.getAbsolutePath());

	String tokenToReturn = maxSizeReached ? null : resumptionToken;
	return new DownloadPartResult(zipFile, tokenToReturn, maxSizeReached, maxSizeErrorMessage, sizeInMB, currentPartSizeMB,
		(tokenToReturn == null), downloadedFileNames);
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
