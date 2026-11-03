package eu.essi_lab.accessor.gcw;

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

import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import org.json.JSONObject;

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
 * Harvests the time series of the GCW catalogue that can be downloaded by {@link GCWDownloader}: the records
 * distributed through OPeNDAP are listed from the catalogue and each listRecords call opens one of the OPeNDAP
 * datasets, emitting one metadata record for each variable (and vertical level, for profile datasets) having valid
 * values on the time dimension. Datasets that are not time series (e.g. gridded products) or that can not be opened
 * are skipped.
 *
 * @author boldrini
 */
public class GCWConnector extends HarvestedQueryConnector<GCWConnectorSetting> {

    /**
     *
     */
    public static final String TYPE = "GCWConnector";

    /**
     * Keys of the harvested metadata records
     */
    public static final String RECORD_CATALOGUE = "catalogue";
    public static final String RECORD_DATASET = "dataset";
    public static final String RECORD_STATION = "station";
    public static final String RECORD_SERIES = "series";
    public static final String RECORD_BEGIN = "begin";
    public static final String RECORD_END = "end";

    /**
     * The global attributes of the OPeNDAP datasets copied in the harvested metadata records
     */
    static final Set<String> GLOBAL_ATTRIBUTES = Set.of("title", "summary", "institution", "featureType", "time_coverage_resolution",
	    "license", "source", "platform", "platform_name", "station_name", "station_id", "wigosId", "wigos_id", "site_id", "references");

    private static final String GCW_CONNECTOR_ERROR = "GCW_CONNECTOR_ERROR";

    private List<JSONObject> records;

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
	    if (records == null || index == 0) {
		records = new GCWClient(getSourceURL()).listOPeNDAPRecords();
		partialNumbers = 0;
		GSLoggerFactory.getLogger(getClass()).debug("Found {} OPeNDAP datasets", StringUtils.format(records.size()));
	    }
	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e);

	    throw GSException.createException(//
		    getClass(), //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    GCW_CONNECTOR_ERROR, //
		    e);
	}

	boolean maxReached = false;

	if (index < records.size()) {

	    JSONObject record = records.get(index);
	    String url = GCWClient.getOPeNDAPURL(record).get();

	    try {
		GCWTimeSeriesDataset dataset = GCWOPeNDAP.describe(url, getSetting().getExcludedVariables());

		JSONObject datasetObject = new JSONObject();
		for (Entry<String, String> entry : dataset.getGlobalAttributes().entrySet()) {
		    if (GLOBAL_ATTRIBUTES.contains(entry.getKey())) {
			datasetObject.put(entry.getKey(), entry.getValue());
		    }
		}

		for (JSONObject series : dataset.getSeries()) {

		    if (!unlimited && mr.isPresent() && partialNumbers >= mr.get()) {
			maxReached = true;
			break;
		    }

		    JSONObject metadata = new JSONObject();
		    metadata.put(RECORD_CATALOGUE, record);
		    metadata.put(RECORD_DATASET, datasetObject);
		    metadata.put(RECORD_STATION, dataset.getStation());
		    metadata.put(RECORD_SERIES, series);
		    if (dataset.getBegin() != null && dataset.getEnd() != null) {
			metadata.put(RECORD_BEGIN, dataset.getBegin());
			metadata.put(RECORD_END, dataset.getEnd());
		    }

		    OriginalMetadata originalMetadata = new OriginalMetadata();
		    originalMetadata.setMetadata(metadata.toString());
		    originalMetadata.setSchemeURI(GCWMapper.GCW_SCHEMA);
		    response.addRecord(originalMetadata);
		    partialNumbers++;
		}

		GSLoggerFactory.getLogger(getClass()).debug("Dataset {} [{}/{}]: {} time series", url, index + 1, records.size(),
			dataset.getSeries().size());

	    } catch (Exception e) {
		// datasets that are not time series or are not reachable must not stop the harvesting of the others
		GSLoggerFactory.getLogger(getClass()).warn("Skipping dataset {} [{}/{}]: {}", url, index + 1, records.size(), e.getMessage());
	    }
	}

	if (index + 1 < records.size() && !maxReached) {
	    response.setResumptionToken(String.valueOf(index + 1));
	} else {
	    response.setResumptionToken(null);
	    records = null;
	    partialNumbers = 0;
	}

	GSLoggerFactory.getLogger(getClass()).debug("List records ENDED");

	return response;
    }

    @Override
    public List<String> listMetadataFormats() throws GSException {

	return Arrays.asList(GCWMapper.GCW_SCHEMA);
    }

    @Override
    public boolean supports(GSSource source) {

	return source.getEndpoint().contains("gcw.csw.met.no");
    }

    @Override
    protected GCWConnectorSetting initSetting() {

	return new GCWConnectorSetting();
    }

    @Override
    public String getType() {

	return TYPE;
    }
}
