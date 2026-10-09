package eu.essi_lab.accessor.geuspromice;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Technologies and Environmental Intelligence (ITIAm)/ESSI-Lab
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
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;

import org.json.JSONObject;

import eu.essi_lab.accessor.thredds.THREDDSDataset;
import eu.essi_lab.cdk.harvest.HarvestedQueryConnector;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.StringUtils;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * Harvests one metadata record for each site and variable: each listRecords call downloads and describes one site
 * NetCDF file of the configured THREDDS catalog
 *
 * @author boldrini
 */
public class PROMICEConnector extends HarvestedQueryConnector<PROMICEConnectorSetting> {

    /**
     *
     */
    public static final String TYPE = "PROMICEConnector";

    /**
     * Keys of the harvested metadata records
     */
    public static final String RECORD_SITE = "site";
    public static final String RECORD_VARIABLE = "variable";
    public static final String RECORD_FILE_URL = "fileURL";
    public static final String RECORD_DATASET_NAME = "datasetName";

    private static final String PROMICE_CONNECTOR_ERROR = "PROMICE_CONNECTOR_ERROR";

    private List<THREDDSDataset> datasets;

    /** Metadata records emitted in the current harvest (across listRecords calls). */
    private int partialNumbers;

    @Override
    public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest request) throws GSException {

	GSLoggerFactory.getLogger(getClass()).debug("List records STARTED");

	ListRecordsResponse<OriginalMetadata> response = new ListRecordsResponse<>();

	Optional<Integer> mr = getSetting().getMaxRecords();
	boolean unlimited = getSetting().isMaxRecordsUnlimited();

	int index = 0;
	String resumptionToken = request.getResumptionToken();
	if (resumptionToken != null && !resumptionToken.isEmpty()) {
	    index = Integer.parseInt(resumptionToken);
	}

	try {
	    if (datasets == null || index == 0) {
		datasets = new PROMICEClient(getSourceURL()).listDatasets();
		partialNumbers = 0;
		GSLoggerFactory.getLogger(getClass()).debug("Found {} site files", StringUtils.format(datasets.size()));
	    }
	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e);

	    throw GSException.createException(//
		    getClass(), //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    PROMICE_CONNECTOR_ERROR, //
		    e);
	}

	boolean maxReached = false;

	if (index < datasets.size()) {

	    THREDDSDataset dataset = datasets.get(index);
	    String fileURL = PROMICEClient.getFileURL(dataset).get();

	    try {
		File file = PROMICEClient.getFile(fileURL);
		PROMICESite site = PROMICENetCDF.describe(file, getSetting().getVariables());

		JSONObject siteObject = new JSONObject();
		for (Entry<String, String> entry : site.getGlobalAttributes().entrySet()) {
		    siteObject.put(entry.getKey(), entry.getValue());
		}

		for (JSONObject variable : site.getVariables()) {

		    if (!unlimited && mr.isPresent() && partialNumbers >= mr.get()) {
			maxReached = true;
			break;
		    }

		    JSONObject record = new JSONObject();
		    record.put(RECORD_SITE, siteObject);
		    record.put(RECORD_VARIABLE, variable);
		    record.put(RECORD_FILE_URL, fileURL);
		    record.put(RECORD_DATASET_NAME, dataset.getName());

		    OriginalMetadata originalMetadata = new OriginalMetadata();
		    originalMetadata.setMetadata(record.toString());
		    originalMetadata.setSchemeURI(PROMICEMapper.PROMICE_SCHEMA);
		    response.addRecord(originalMetadata);
		    partialNumbers++;
		}

		GSLoggerFactory.getLogger(getClass()).debug("Site file {} [{}/{}]: {} variables", dataset.getName(), index + 1,
			datasets.size(), site.getVariables().size());

	    } catch (Exception e) {
		// a broken file must not stop the harvesting of the other sites
		GSLoggerFactory.getLogger(getClass()).error("Unable to read site file {}: {}", fileURL, e.getMessage());
	    }
	}

	if (index + 1 < datasets.size() && !maxReached) {
	    response.setResumptionToken(String.valueOf(index + 1));
	} else {
	    response.setResumptionToken(null);
	    datasets = null;
	    partialNumbers = 0;
	}

	GSLoggerFactory.getLogger(getClass()).debug("List records ENDED");

	return response;
    }

    @Override
    public List<String> listMetadataFormats() throws GSException {

	return Arrays.asList(PROMICEMapper.PROMICE_SCHEMA);
    }

    @Override
    public boolean supports(GSSource source) {

	return source.getEndpoint().contains("thredds.geus.dk") && source.getEndpoint().contains("/aws/");
    }

    @Override
    protected PROMICEConnectorSetting initSetting() {

	return new PROMICEConnectorSetting();
    }

    @Override
    public String getType() {

	return TYPE;
    }
}
