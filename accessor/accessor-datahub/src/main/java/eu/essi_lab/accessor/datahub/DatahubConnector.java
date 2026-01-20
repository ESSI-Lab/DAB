package eu.essi_lab.accessor.datahub;

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

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.json.JSONObject;
import org.slf4j.Logger;

import eu.essi_lab.cdk.harvest.HarvestedQueryConnector;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.net.downloader.HttpHeaderUtils;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils.MethodNoBody;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils.MethodWithBody;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.IOStreamUtils;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * Connector for retrieving metadata from DataHub
 * 
 * @author Generated
 */
public class   DatahubConnector extends HarvestedQueryConnector<DatahubConnectorSetting> {

    /**
     * 
     */
    public static final String TYPE = "DatahubConnector";

    private static final String DATAHUB_READ_ERROR = "Unable to retrieve DataHub metadata";
    private static final String DATAHUB_URL_NOT_FOUND_ERROR = "DATAHUB_URL_NOT_FOUND_ERROR";
    private static final String DATAHUB_AUTH_ERROR = "DATAHUB_AUTH_ERROR";
    private static final String DATAHUB_IDENTIFIERS_ERROR = "DATAHUB_IDENTIFIERS_ERROR";
    private static final int IDENTIFIER_BATCH_SIZE = 20;

    private List<String> originalMetadata;
    private int partialNumbers;
    private Logger logger = GSLoggerFactory.getLogger(this.getClass());
    private Downloader downloader;

    public DatahubConnector() {
	this.originalMetadata = new ArrayList<>();
	this.downloader = new Downloader();
    }

    @Override
    public boolean supports(GSSource source) {
	String endpoint = source.getEndpoint();
	return endpoint != null && !endpoint.isEmpty();
    }

    @Override
    public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest request) throws GSException {
	ListRecordsResponse<OriginalMetadata> ret = new ListRecordsResponse<>();
	if (originalMetadata.isEmpty()) {
	    originalMetadata = getOriginalMetadata();
	}

	String token = request.getResumptionToken();
	int start = 0;
	if (token != null) {
	    start = Integer.valueOf(token);
	}

	int pageSize = getSetting().getPageSize();

	Optional<Integer> mr = getSetting().getMaxRecords();
	boolean maxNumberReached = false;
	if (!getSetting().isMaxRecordsUnlimited() && mr.isPresent() && start > mr.get() - 1) {
	    maxNumberReached = true;
	}

	if (start < originalMetadata.size() && !maxNumberReached) {
	    int end = start + pageSize;
	    if (end > originalMetadata.size()) {
		end = originalMetadata.size();
	    }

	    if (!getSetting().isMaxRecordsUnlimited() && mr.isPresent() && end > mr.get()) {
		end = mr.get();
	    }
	    int count = 0;

	    for (int i = start; i < end; i++) {
		String om = originalMetadata.get(i);

		if (om != null && !om.isEmpty()) {
		    OriginalMetadata metadata = new OriginalMetadata();
		    metadata.setSchemeURI(DatahubMapper.DATAHUB_NS_URI);
		    metadata.setMetadata(om);
		    ret.addRecord(metadata);
		    partialNumbers++;
		    count++;
		}
	    }
	    ret.setResumptionToken(String.valueOf(start + count));
	    logger.debug("ADDED {} records. Number of analyzed records: {}", partialNumbers, String.valueOf(start + count));

	} else {
	    ret.setResumptionToken(null);
	    logger.debug("Added records: {} . TOTAL SIZE: {}", partialNumbers, originalMetadata.size());
	    partialNumbers = 0;
	    return ret;
	}

	return ret;
    }

    private List<String> getOriginalMetadata() throws GSException {
	logger.trace("DataHub List Data finding STARTED");

	List<String> ret = new ArrayList<>();

	String endpoint = getSourceURL();
	if (endpoint == null || endpoint.isEmpty()) {
	    throw GSException.createException(//
		    this.getClass(), //
		    "Source endpoint is required", //
		    null, //
		    ErrorInfo.ERRORTYPE_SERVICE, //
		    ErrorInfo.SEVERITY_ERROR, //
		    DATAHUB_URL_NOT_FOUND_ERROR);
	}

	// Ensure endpoint doesn't end with /
	if (endpoint.endsWith("/")) {
	    endpoint = endpoint.substring(0, endpoint.length() - 1);
	}

	try {
	    // Step 1: Get access token
	    String accessToken = getAccessToken(endpoint);
	    if (accessToken == null || accessToken.isEmpty()) {
		throw GSException.createException(//
			this.getClass(), //
			"Failed to obtain access token", //
			null, //
			ErrorInfo.ERRORTYPE_SERVICE, //
			ErrorInfo.SEVERITY_ERROR, //
			DATAHUB_AUTH_ERROR);
	    }
	    logger.debug("Access token obtained successfully");

	    // Step 2: Read identifiers from URL or file
	    List<String> identifiers = readIdentifiers();
	    if (identifiers.isEmpty()) {
		logger.warn("No identifiers found");
		return ret;
	    }
	    logger.info("Found {} identifiers to process", identifiers.size());

	    // Step 3: Process identifiers in blocks of 20
	    for (int i = 0; i < identifiers.size(); i += IDENTIFIER_BATCH_SIZE) {
		int end = Math.min(i + IDENTIFIER_BATCH_SIZE, identifiers.size());
		List<String> batch = identifiers.subList(i, end);
		logger.debug("Processing batch {}-{} of {} identifiers", i + 1, end, identifiers.size());

		for (String identifier : batch) {
		    try {
			String metadata = fetchMetadataForIdentifier(endpoint, accessToken, identifier);
			if (metadata != null && !metadata.trim().isEmpty()) {
			    ret.add(metadata);
			    logger.debug("Successfully fetched metadata for identifier: {}", identifier);
			} else {
			    logger.warn("Empty metadata returned for identifier: {}", identifier);
			}
		    } catch (Exception e) {
			logger.error("Error fetching metadata for identifier: {}", identifier, e);
			// Continue with next identifier
		    }
		}
	    }

	    if (ret.isEmpty()) {
		logger.warn("No metadata records loaded");
	    } else {
		logger.info("Loaded {} metadata records", ret.size());
	    }

	} catch (GSException e) {
	    throw e;
	} catch (Exception e) {
	    logger.error("Error retrieving DataHub metadata", e);
	    throw GSException.createException(//
		    this.getClass(), //
		    DATAHUB_READ_ERROR + ": " + e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_SERVICE, //
		    ErrorInfo.SEVERITY_ERROR, //
		    DATAHUB_URL_NOT_FOUND_ERROR);
	}

	logger.trace("DataHub List Data finding ENDED");
	return ret;
    }

    /**
     * Authenticates with the DataHub API and returns an access token
     * 
     * @param endpoint
     *            the base endpoint URL
     * @return the access token
     * @throws GSException
     */
    private String getAccessToken(String endpoint) throws GSException {
	Optional<String> username = getSetting().getUsername();
	Optional<String> password = getSetting().getPassword();

	if (!username.isPresent() || !password.isPresent()) {
	    throw GSException.createException(//
		    this.getClass(), //
		    "Username and password are required", //
		    null, //
		    ErrorInfo.ERRORTYPE_SERVICE, //
		    ErrorInfo.SEVERITY_ERROR, //
		    DATAHUB_AUTH_ERROR);
	}

	try {
	    String loginUrl = endpoint + "/ext-login";
	    logger.debug("Authenticating at: {}", loginUrl);

	    // Build JSON request body
	    JSONObject requestBody = new JSONObject();
	    requestBody.put("user", username.get());
	    requestBody.put("psw", password.get());
	    requestBody.put("app_to_use", "TUTTE");

	    // Build headers
	    HashMap<String, String> headers = new HashMap<>();
	    headers.put("Content-Type", "application/json");

	    // Build POST request
	    HttpRequest request = HttpRequestUtils.build(//
		    MethodWithBody.POST, //
		    loginUrl, //
		    requestBody.toString(), //
		    HttpHeaderUtils.build(headers));

	    // Execute request
	    HttpResponse<InputStream> response = downloader.downloadResponse(request);
	    int statusCode = response.statusCode();

	    if (statusCode >= 400) {
		logger.error("Authentication failed with status code: {}", statusCode);
		throw GSException.createException(//
			this.getClass(), //
			"Authentication failed with status code: " + statusCode, //
			null, //
			ErrorInfo.ERRORTYPE_SERVICE, //
			ErrorInfo.SEVERITY_ERROR, //
			DATAHUB_AUTH_ERROR);
	    }

	    // Parse response
	    String responseBody = IOStreamUtils.asUTF8String(response.body());
	    JSONObject jsonResponse = new JSONObject(responseBody);
	    String accessToken = jsonResponse.optString("access_token");

	    if (accessToken == null || accessToken.isEmpty()) {
		logger.error("Access token not found in response: {}", responseBody);
		throw GSException.createException(//
			this.getClass(), //
			"Access token not found in authentication response", //
			null, //
			ErrorInfo.ERRORTYPE_SERVICE, //
			ErrorInfo.SEVERITY_ERROR, //
			DATAHUB_AUTH_ERROR);
	    }

	    return accessToken;

	} catch (GSException e) {
	    throw e;
	} catch (Exception e) {
	    logger.error("Error during authentication", e);
	    throw GSException.createException(//
		    this.getClass(), //
		    "Error during authentication: " + e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_SERVICE, //
		    ErrorInfo.SEVERITY_ERROR, //
		    DATAHUB_AUTH_ERROR);
	}
    }

    /**
     * Reads identifiers from a URL (http://) or local file (file://)
     * 
     * @return list of identifiers
     * @throws GSException
     */
    private List<String> readIdentifiers() throws GSException {
	List<String> identifiers = new ArrayList<>();
	Optional<String> identifiersUrl = getSetting().getIdentifiersUrl();

	if (!identifiersUrl.isPresent()) {
	    throw GSException.createException(//
		    this.getClass(), //
		    "Identifiers URL is required", //
		    null, //
		    ErrorInfo.ERRORTYPE_SERVICE, //
		    ErrorInfo.SEVERITY_ERROR, //
		    DATAHUB_IDENTIFIERS_ERROR);
	}

	String url = identifiersUrl.get();
	logger.debug("Reading identifiers from: {}", url);

	try {
	    InputStream inputStream = null;

	    if (url.startsWith("file://")) {
		// Handle local file
		String filePath = url.substring(7); // Remove "file://" prefix
		// Handle Windows paths like file:///C:/path/to/file
		if (filePath.startsWith("/") && filePath.length() > 2 && filePath.charAt(2) == ':') {
		    filePath = filePath.substring(1);
		}
		File file = new File(filePath);
		if (!file.exists()) {
		    throw GSException.createException(//
			    this.getClass(), //
			    "File not found: " + filePath, //
			    null, //
			    ErrorInfo.ERRORTYPE_SERVICE, //
			    ErrorInfo.SEVERITY_ERROR, //
			    DATAHUB_IDENTIFIERS_ERROR);
		}
		inputStream = Files.newInputStream(Paths.get(filePath));
		logger.debug("Reading from local file: {}", filePath);
	    } else {
		// Handle HTTP URL
		Optional<InputStream> stream = downloader.downloadOptionalStream(url);
		if (!stream.isPresent()) {
		    throw GSException.createException(//
			    this.getClass(), //
			    "Failed to download identifiers from URL: " + url, //
			    null, //
			    ErrorInfo.ERRORTYPE_SERVICE, //
			    ErrorInfo.SEVERITY_ERROR, //
			    DATAHUB_IDENTIFIERS_ERROR);
		}
		inputStream = stream.get();
		logger.debug("Reading from HTTP URL: {}", url);
	    }

	    // Read identifiers line by line
	    try (BufferedReader reader = new BufferedReader(//
		    new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
		String line;
		while ((line = reader.readLine()) != null) {
		    line = line.trim();
		    if (!line.isEmpty()) {
			identifiers.add(line);
		    }
		}
	    }

	    logger.info("Read {} identifiers", identifiers.size());
	    return identifiers;

	} catch (GSException e) {
	    throw e;
	} catch (Exception e) {
	    logger.error("Error reading identifiers", e);
	    throw GSException.createException(//
		    this.getClass(), //
		    "Error reading identifiers: " + e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_SERVICE, //
		    ErrorInfo.SEVERITY_ERROR, //
		    DATAHUB_IDENTIFIERS_ERROR);
	}
    }

    /**
     * Fetches metadata for a single identifier
     * 
     * @param endpoint
     *            the base endpoint URL
     * @param accessToken
     *            the access token
     * @param identifier
     *            the dataset identifier
     * @return the metadata JSON string
     * @throws GSException
     */
    private String fetchMetadataForIdentifier(String endpoint, String accessToken, String identifier)
	    throws GSException {
	try {
	    String apiUrl = endpoint + "/datahub-rndt/get-unflatten-properties";
	    logger.debug("Fetching metadata for identifier: {} from: {}", identifier, apiUrl);

	    // Build headers
	    HashMap<String, String> headers = new HashMap<>();
	    headers.put("X-DataHub-URN", identifier);
	    headers.put("Accept", "application/json");
	    headers.put("Authorization", "Bearer " + accessToken);

	    // Build GET request
	    HttpRequest request = HttpRequestUtils.build(//
		    MethodNoBody.GET, //
		    apiUrl, //
		    HttpHeaderUtils.build(headers));

	    // Execute request
	    HttpResponse<InputStream> response = downloader.downloadResponse(request);
	    int statusCode = response.statusCode();

	    if (statusCode >= 400) {
		logger.warn("Failed to fetch metadata for identifier {} with status code: {}", identifier, statusCode);
		return null;
	    }

	    // Parse response
	    String responseBody = IOStreamUtils.asUTF8String(response.body());
	    JSONObject jsonResponse = new JSONObject(responseBody);

	    // Extract metadata from path: #/aspects/datasetProperties/value/customProperties
	    JSONObject aspects = jsonResponse.optJSONObject("aspects");
	    if (aspects == null) {
		logger.warn("No 'aspects' found in response for identifier: {}", identifier);
		return null;
	    }

	    JSONObject datasetProperties = aspects.optJSONObject("datasetProperties");
	    if (datasetProperties == null) {
		logger.warn("No 'datasetProperties' found in response for identifier: {}", identifier);
		return null;
	    }

	    JSONObject value = datasetProperties.optJSONObject("value");
	    if (value == null) {
		logger.warn("No 'value' found in datasetProperties for identifier: {}", identifier);
		return null;
	    }

	    JSONObject customProperties = value.optJSONObject("customProperties");
	    if (customProperties == null) {
		logger.warn("No 'customProperties' found in value for identifier: {}", identifier);
		return null;
	    }

	    return customProperties.toString();

	} catch (Exception e) {
	    logger.error("Error fetching metadata for identifier: {}", identifier, e);
	    throw GSException.createException(//
		    this.getClass(), //
		    "Error fetching metadata for identifier " + identifier + ": " + e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_SERVICE, //
		    ErrorInfo.SEVERITY_ERROR, //
		    DATAHUB_READ_ERROR);
	}
    }

    @Override
    public List<String> listMetadataFormats() throws GSException {
	List<String> ret = new ArrayList<>();
	ret.add(DatahubMapper.DATAHUB_NS_URI);
	return ret;
    }

    @Override
    public String getType() {
	return TYPE;
    }

    @Override
    protected DatahubConnectorSetting initSetting() {
	return new DatahubConnectorSetting();
    }
}

