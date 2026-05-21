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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.h2.util.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.cfga.scheduler.SchedulerJobStatus;
import eu.essi_lab.lib.net.s3.S3TransferWrapper;
import eu.essi_lab.lib.utils.FileUtils;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.messages.web.WebRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;

/**
 * Builds a single metadata catalog (CSV or shapefile) for all observations matching a query, packaged in one zip.
 */
public class MetadataDownloaderTool {

    public MetadataDownloaderTool() {
    }

    /**
     * Writes a metadata catalog zip (CSV or shapefile inside) to the given output stream.
     */
    public void writeMetadataZip(OutputStream output, String requestURL, String downloadName) throws IOException {
	File zipFile = buildMetadataZipFile(requestURL, downloadName, null, null, null, null);
	if (zipFile == null) {
	    throw new IOException("Unable to build metadata archive");
	}
	try (InputStream input = Files.newInputStream(zipFile.toPath())) {
	    input.transferTo(output);
	} finally {
	    zipFile.delete();
	}
    }

    public DownloadPartResult download(S3TransferWrapper s3wrapper, String bucket, String requestURL, String operationId,
	    String downloadName, SchedulerJobStatus status) {

	GSLoggerFactory.getLogger(getClass()).info("Started metadata download of {}", requestURL);

	try {
	    if (s3wrapper != null) {
		JSONObject msg = new JSONObject();
		msg.put("id", operationId);
		msg.put("status", "Started");
		msg.put("downloadName", downloadName);
		msg.put("downloadKind", "metadata");
		msg.put("timestamp", ISO8601DateTimeUtils.getISO8601DateTime());
		OMHandler.status(s3wrapper, bucket, operationId, msg);
	    }
	} catch (Exception e) {
	    GSLoggerFactory.getLogger(getClass()).error(e);
	}

	try {
	    File zipFile = buildMetadataZipFile(requestURL, downloadName, s3wrapper, bucket, operationId, status);
	    if (zipFile == null) {
		return new DownloadPartResult(null, null, false, null, null, null, false, Collections.emptyList());
	    }
	    long sizeInBytes = zipFile.length();
	    BigDecimal sizeInMB = BigDecimal.valueOf(sizeInBytes).divide(BigDecimal.valueOf(1024 * 1024), 2, RoundingMode.UP);
	    return new DownloadPartResult(zipFile, null, false, null, sizeInMB, sizeInMB, true, Collections.emptyList());
	} catch (Exception e) {
	    GSLoggerFactory.getLogger(getClass()).error("Metadata download failed", e);
	    return null;
	}
    }

    private File buildMetadataZipFile(String requestURL, String downloadName, S3TransferWrapper s3wrapper, String bucket,
	    String operationId, SchedulerJobStatus status) throws IOException {

	File zipFile = File.createTempFile(getClass().getSimpleName(), ".zip");
	String tempKey = operationId != null ? operationId : ("metadata_" + System.currentTimeMillis());
	File tempDirFile = FileUtils.getTempDir(StringUtils.urlEncode(tempKey) + "_metadata", false);
	Path tempPath = tempDirFile.toPath();
	if (!tempDirFile.exists()) {
	    tempDirFile.mkdirs();
	}

	try {
	    OMFormat format = extractFormat(requestURL);
	    if (format == null) {
		format = OMFormat.CSV;
	    }
	    if (format != OMFormat.CSV && format != OMFormat.SHAPEFILE) {
		format = OMFormat.CSV;
	    }

	    String baseName = resolveBaseName(downloadName, requestURL);
	    String baseRequestURL = prepareListUrl(requestURL);
	    OMHandler omHandler = new OMHandler();
	    int rowCount;

	    if (format == OMFormat.SHAPEFILE) {
		try (MetadataShapefileWriter shapefileWriter = new MetadataShapefileWriter(tempDirFile, baseName)) {
		    rowCount = collectMetadataRows(baseRequestURL, omHandler, shapefileWriter::append, s3wrapper, bucket,
			    operationId, downloadName, tempPath, status);
		}
	    } else {
		File csvFile = new File(tempDirFile, baseName + format.getExtension());
		try (BufferedWriter csvWriter = Files.newBufferedWriter(csvFile.toPath(), StandardCharsets.UTF_8)) {
		    MetadataCsvWriter.writeHeader(csvWriter);
		    rowCount = collectMetadataRows(baseRequestURL, omHandler, observation -> {
			MetadataCsvWriter.writeObservationRow(csvWriter, observation);
		    }, s3wrapper, bucket, operationId, downloadName, tempPath, status);
		}
	    }

	    if (operationId != null && isCanceled(s3wrapper, bucket, operationId, tempPath)) {
		zipFile.delete();
		return null;
	    }

	    zipFolder(tempPath, zipFile.toPath());
	    GSLoggerFactory.getLogger(getClass()).info("Built metadata zip for {} ({} rows, format {})", requestURL, rowCount,
		    format);
	    return zipFile;
	} finally {
	    FileUtils.clearFolder(tempDirFile, true);
	}
    }

    @FunctionalInterface
    private interface ObservationConsumer {
	void accept(JSONObject observation) throws IOException;
    }

    private int collectMetadataRows(String baseRequestURL, OMHandler omHandler, ObservationConsumer consumer,
	    S3TransferWrapper s3wrapper, String bucket, String operationId, String downloadName, Path tempPath,
	    SchedulerJobStatus status) throws IOException {

	int rowCount = 0;
	boolean firstLoop = true;
	String resumptionToken = null;
	int blocks = 0;

	while (firstLoop || resumptionToken != null) {
	    firstLoop = false;
	    blocks++;
	    GSLoggerFactory.getLogger(getClass()).info("Metadata list block {}", blocks);

	    String listURL = buildListUrl(baseRequestURL, resumptionToken);

	    Optional<JSONObject> response = omHandler.getJSONResponse(WebRequest.createGET(listURL));
	    if (!response.isPresent()) {
		break;
	    }

	    JSONObject json = response.get();
	    if (json.has("completed") && !json.getBoolean("completed") && json.has("resumptionToken")) {
		resumptionToken = json.getString("resumptionToken");
	    } else {
		resumptionToken = null;
	    }

	    JSONArray members = json.optJSONArray("member");
	    if (members == null || members.length() == 0) {
		break;
	    }

	    for (int i = 0; i < members.length(); i++) {
		consumer.accept(members.getJSONObject(i));
		rowCount++;
	    }

	    if (isCanceled(s3wrapper, bucket, operationId, tempPath)) {
		return rowCount;
	    }

	    updateProgress(s3wrapper, bucket, operationId, downloadName, rowCount, status);
	}
	return rowCount;
    }

    private void updateProgress(S3TransferWrapper s3wrapper, String bucket, String operationId, String downloadName, int rowCount,
	    SchedulerJobStatus status) {
	if (s3wrapper == null) {
	    return;
	}
	try {
	    JSONObject msg = new JSONObject();
	    msg.put("id", operationId);
	    msg.put("status", "InProgress");
	    msg.put("downloadName", downloadName);
	    msg.put("downloadKind", "metadata");
	    msg.put("statusMessage", "Collecting metadata: " + rowCount + " time series");
	    msg.put("timestamp", ISO8601DateTimeUtils.getISO8601DateTime());
	    OMHandler.status(s3wrapper, bucket, operationId, msg);
	    if (status != null) {
		status.clearMessages();
		status.addInfoMessage(rowCount + " metadata rows");
	    }
	} catch (Exception e) {
	    GSLoggerFactory.getLogger(getClass()).error(e);
	}
    }

    private boolean isCanceled(S3TransferWrapper s3wrapper, String bucket, String operationId, Path tempPath) {
	if (s3wrapper == null) {
	    return false;
	}
	try {
	    String key = "data-downloads/" + operationId + "-cancel";
	    HeadObjectResponse metadata = s3wrapper.getObjectMetadata(bucket, key);
	    if (metadata != null) {
		s3wrapper.deleteObject(bucket, key);
		FileUtils.clearFolder(tempPath.toFile(), true);
		return true;
	    }
	} catch (Exception e) {
	    GSLoggerFactory.getLogger(getClass()).error(e);
	}
	return false;
    }

    /**
     * Builds the internal observations list URL for one catalog page. Keeps {@code limit} (and aliases) from the
     * original request when present; only applies {@link OMRequestUtils#METADATA_PAGE_SIZE} as a default page size for
     * unpaginated bulk downloads.
     */
    private String buildListUrl(String baseRequestURL, String resumptionToken) {
	try {
	    URIBuilder builder = new URIBuilder(baseRequestURL);
	    if (!hasLimitParameter(builder)) {
		builder.setParameter("limit", String.valueOf(OMRequestUtils.METADATA_PAGE_SIZE));
	    }
	    if (!hasQueryParameter(builder, OMRequest.APIParameters.EXPAND_FEATURES.getKeys())) {
		builder.setParameter("expandFeatures", "true");
	    }
	    if (resumptionToken != null) {
		builder.setParameter("resumptionToken", resumptionToken);
	    }
	    return builder.build().toString();
	} catch (URISyntaxException e) {
	    GSLoggerFactory.getLogger(getClass()).error(e);
	    String listURL = baseRequestURL;
	    if (!baseRequestURL.toLowerCase().contains("limit=") && !baseRequestURL.toLowerCase().contains("maxrecords=")
		    && !baseRequestURL.toLowerCase().contains("count=")) {
		listURL = addParameter(listURL, "limit", String.valueOf(OMRequestUtils.METADATA_PAGE_SIZE));
	    }
	    if (!baseRequestURL.toLowerCase().contains("expandfeatures=")) {
		listURL = addParameter(listURL, "expandFeatures", "true");
	    }
	    if (resumptionToken != null) {
		listURL = addParameter(listURL, "resumptionToken", resumptionToken);
	    }
	    return listURL;
	}
    }

    private static boolean hasLimitParameter(URIBuilder builder) {
	return hasQueryParameter(builder, OMRequest.APIParameters.LIMIT.getKeys());
    }

    private static boolean hasQueryParameter(URIBuilder builder, String... names) {
	for (NameValuePair pair : builder.getQueryParams()) {
	    for (String name : names) {
		if (name.equalsIgnoreCase(pair.getName())) {
		    return true;
		}
	    }
	}
	return false;
    }

    private String prepareListUrl(String requestURL) {
	try {
	    String observationsUrl = requestURL.replace("downloads?", "observations?");
	    URIBuilder builder = new URIBuilder(observationsUrl);
	    List<NameValuePair> filtered = new ArrayList<>();
	    for (NameValuePair pair : builder.getQueryParams()) {
		String name = pair.getName();
		if ("asynchDownloadName".equals(name) || "eMailNotifications".equals(name) || "includeData".equals(name)
			|| "includeValues".equals(name) || "format".equals(name) || "asynchDownload".equals(name)) {
		    continue;
		}
		filtered.add(pair);
	    }
	    builder.setParameters(filtered);
	    return builder.build().toString();
	} catch (URISyntaxException e) {
	    GSLoggerFactory.getLogger(getClass()).error(e);
	    return requestURL.replace("downloads?", "observations?");
	}
    }

    private OMFormat extractFormat(String requestURL) {
	return OMFormat.decode(extractFormatValue(requestURL));
    }

    private String extractFormatValue(String requestURL) {
	try {
	    URI uri = new URI(requestURL);
	    URIBuilder uriBuilder = new URIBuilder(uri);
	    return uriBuilder.getQueryParams().stream().filter(p -> p.getName().equals("format")).map(p -> p.getValue())
		    .findFirst().orElse(null);
	} catch (URISyntaxException e) {
	    return null;
	}
    }

    /**
     * Base file name for catalog files inside the zip (without extension).
     */
    public static String resolveBaseName(String downloadName, String requestURL) {
	if (downloadName != null && !downloadName.isEmpty()) {
	    return sanitizeDownloadName(downloadName);
	}
	String fromUrl = extractQueryParam(requestURL, "asynchDownloadName");
	if (fromUrl != null && !fromUrl.isEmpty()) {
	    return sanitizeDownloadName(fromUrl);
	}
	return "metadata";
    }

    private static String sanitizeDownloadName(String downloadName) {
	if (downloadName == null || downloadName.isEmpty()) {
	    return "metadata";
	}
	return FileUtils.sanitizeForNtfs(downloadName);
    }

    private static String extractQueryParam(String requestURL, String paramName) {
	if (requestURL == null) {
	    return null;
	}
	try {
	    URIBuilder builder = new URIBuilder(requestURL);
	    return builder.getQueryParams().stream().filter(p -> paramName.equals(p.getName())).map(NameValuePair::getValue)
		    .findFirst().orElse(null);
	} catch (URISyntaxException e) {
	    return null;
	}
    }

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

    private String removeParameter(String baseURL, String parameter, String value) {
	if (value != null) {
	    baseURL = baseURL.replace("&" + parameter + "=" + value, "");
	    baseURL = baseURL.replace("?" + parameter + "=" + value + "&", "?");
	    baseURL = baseURL.replace("?" + parameter + "=" + value, "?");
	} else {
	    int idx = baseURL.indexOf(parameter + "=");
	    while (idx >= 0) {
		int start = baseURL.lastIndexOf('&', idx);
		if (start < 0) {
		    start = baseURL.indexOf('?');
		}
		int end = baseURL.indexOf('&', idx);
		if (end < 0) {
		    end = baseURL.length();
		} else {
		    end++;
		}
		if (start >= 0 && baseURL.charAt(start) == '?') {
		    baseURL = baseURL.substring(0, start + 1) + baseURL.substring(end);
		} else if (start >= 0) {
		    baseURL = baseURL.substring(0, start) + baseURL.substring(end);
		}
		idx = baseURL.indexOf(parameter + "=");
	    }
	}
	baseURL = baseURL.replace("?&", "?");
	if (baseURL.endsWith("?")) {
	    baseURL = baseURL.substring(0, baseURL.length() - 1);
	}
	return baseURL;
    }

    private String addParameter(String baseURL, String parameter, String value) {
	if (baseURL.endsWith("?") || baseURL.endsWith("&")) {
	    return baseURL + parameter + "=" + value;
	}
	if (!baseURL.contains("?")) {
	    return baseURL + "?" + parameter + "=" + value;
	}
	return baseURL + "&" + parameter + "=" + value;
    }
}
