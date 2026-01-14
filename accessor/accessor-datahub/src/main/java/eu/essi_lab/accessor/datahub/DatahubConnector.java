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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;

import eu.essi_lab.cdk.harvest.HarvestedQueryConnector;
import eu.essi_lab.lib.utils.GSLoggerFactory;
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
public class DatahubConnector extends HarvestedQueryConnector<DatahubConnectorSetting> {

    /**
     * 
     */
    public static final String TYPE = "DatahubConnector";

    private static final String DATAHUB_READ_ERROR = "Unable to retrieve DataHub metadata";
    private static final String DATAHUB_URL_NOT_FOUND_ERROR = "DATAHUB_URL_NOT_FOUND_ERROR";

    private List<String> originalMetadata;
    private int partialNumbers;
    private Logger logger = GSLoggerFactory.getLogger(this.getClass());

    public DatahubConnector() {
	this.originalMetadata = new ArrayList<>();
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

	// For initial implementation, read example metadata from classpath resources
	// TODO: In future implementation, use endpoint to call actual DataHub API
	String endpoint = getSourceURL();
	if (endpoint != null && !endpoint.isEmpty()) {
	    logger.debug("Endpoint provided: {} (not used in current implementation)", endpoint);
	}

	try {
	    // Read example metadata files from classpath resources
	    String[] resourceFiles = {
		    "example-metadata-model.json",
		    "example-metadata-raster-dataset.json",
		    "example-metadata-vector-dataset.json"
	    };

	    for (String resourceFile : resourceFiles) {
		try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourceFile)) {
		    if (is == null) {
			logger.warn("Resource file not found: {}", resourceFile);
			continue;
		    }

		    String content = new BufferedReader(
			    new InputStreamReader(is, StandardCharsets.UTF_8))
				    .lines()
				    .collect(Collectors.joining("\n"));

		    if (content != null && !content.trim().isEmpty()) {
			ret.add(content);
			logger.debug("Loaded metadata from resource: {}", resourceFile);
		    }
		} catch (IOException e) {
		    logger.error("Error reading resource file: {}", resourceFile, e);
		}
	    }

	    if (ret.isEmpty()) {
		logger.warn("No metadata records loaded from resources");
	    } else {
		logger.info("Loaded {} metadata records from resources", ret.size());
	    }

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

