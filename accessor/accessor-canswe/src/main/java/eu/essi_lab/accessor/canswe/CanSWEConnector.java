package eu.essi_lab.accessor.canswe;

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
import java.util.stream.Collectors;

import org.json.JSONObject;

import eu.essi_lab.cdk.harvest.HarvestedQueryConnector;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * Harvests one metadata record for each station and variable (snow water equivalent, snow depth, snow density) of the
 * CanSWE NetCDF file having at least one valid value
 *
 * @author boldrini
 */
public class CanSWEConnector extends HarvestedQueryConnector<CanSWEConnectorSetting> {

    /**
     *
     */
    public static final String TYPE = "CanSWEConnector";

    /**
     * Keys of the harvested metadata records
     */
    public static final String RECORD_FILE_URL = "fileURL";
    public static final String RECORD_GLOBAL = "global";
    public static final String RECORD_STATION = "station";
    public static final String RECORD_STATION_ID = "station_id";
    public static final String RECORD_LATITUDE = "latitude";
    public static final String RECORD_LONGITUDE = "longitude";
    public static final String RECORD_VARIABLE = "variable";
    public static final String RECORD_VARIABLE_NAME = "name";
    public static final String RECORD_BEGIN = "begin";
    public static final String RECORD_END = "end";
    public static final String RECORD_VALUES = "values";

    /**
     * Number of stations harvested by each listRecords call
     */
    static final int STATIONS_PER_PAGE = 100;

    private static final String CANSWE_CONNECTOR_ERROR = "CANSWE_CONNECTOR_ERROR";

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
	if (index == 0) {
	    partialNumbers = 0;
	}

	String fileURL;
	CanSWEDataset dataset;

	try {
	    CanSWEClient client = new CanSWEClient(getSourceURL());
	    fileURL = client.getFileURL();
	    dataset = client.getDataset();

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e);

	    throw GSException.createException(//
		    getClass(), //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    CANSWE_CONNECTOR_ERROR, //
		    e);
	}

	Set<Long> measurementTypes = getSetting().getMeasurementTypes();

	// the stations of the selected measurement types (all if null)
	List<CanSWEStation> stations = dataset.getStations().stream().//
		filter(s -> measurementTypes == null || measurementTypes.contains(s.getMeasurementType())).//
		collect(Collectors.toList());

	if (index == 0) {
	    GSLoggerFactory.getLogger(getClass()).info("Harvesting {}/{} stations of measurement types {}", stations.size(),
		    dataset.getStations().size(), measurementTypes == null ? "*" : measurementTypes);
	}

	JSONObject global = new JSONObject();
	for (Entry<String, String> entry : dataset.getGlobalAttributes().entrySet()) {
	    global.put(entry.getKey(), entry.getValue());
	}

	boolean maxReached = false;
	int end = Math.min(index + STATIONS_PER_PAGE, stations.size());

	for (int i = index; i < end && !maxReached; i++) {

	    CanSWEStation station = stations.get(i);
	    JSONObject stationObject = toJSON(station);

	    for (String variable : CanSWEDataset.VARIABLES) {

		int[] times = station.getTimeIndexes(variable);
		if (times.length == 0) {
		    continue;
		}

		if (!unlimited && mr.isPresent() && partialNumbers >= mr.get()) {
		    maxReached = true;
		    break;
		}

		JSONObject variableObject = new JSONObject();
		variableObject.put(RECORD_VARIABLE_NAME, variable);
		for (Entry<String, String> entry : dataset.getVariableAttributes(variable).entrySet()) {
		    variableObject.put(entry.getKey(), entry.getValue());
		}

		JSONObject record = new JSONObject();
		record.put(RECORD_FILE_URL, fileURL);
		record.put(RECORD_GLOBAL, global);
		record.put(RECORD_STATION, stationObject);
		record.put(RECORD_VARIABLE, variableObject);
		record.put(RECORD_BEGIN, dataset.getDate(times[0]).getTime());
		record.put(RECORD_END, dataset.getDate(times[times.length - 1]).getTime());
		record.put(RECORD_VALUES, times.length);

		OriginalMetadata originalMetadata = new OriginalMetadata();
		originalMetadata.setMetadata(record.toString());
		originalMetadata.setSchemeURI(CanSWEMapper.CANSWE_SCHEMA);
		response.addRecord(originalMetadata);
		partialNumbers++;
	    }
	}

	GSLoggerFactory.getLogger(getClass()).debug("Stations [{}-{}]/{}: {} records so far", index + 1, end, stations.size(), partialNumbers);

	if (end < stations.size() && !maxReached) {
	    response.setResumptionToken(String.valueOf(end));
	} else {
	    response.setResumptionToken(null);
	    partialNumbers = 0;
	}

	GSLoggerFactory.getLogger(getClass()).debug("List records ENDED");

	return response;
    }

    /**
     * @param station
     * @return the station metadata, with the coordinates
     */
    static JSONObject toJSON(CanSWEStation station) {

	JSONObject ret = station.toJSON();

	if (!Double.isNaN(station.getLatitude()) && !Double.isNaN(station.getLongitude())) {
	    // float precision of the file
	    ret.put(RECORD_LATITUDE, Float.toString((float) station.getLatitude()));
	    ret.put(RECORD_LONGITUDE, Float.toString((float) station.getLongitude()));
	}

	return ret;
    }

    @Override
    public List<String> listMetadataFormats() throws GSException {

	return Arrays.asList(CanSWEMapper.CANSWE_SCHEMA);
    }

    @Override
    public boolean supports(GSSource source) {

	return CanSWEClient.isCanSWE(source.getEndpoint());
    }

    @Override
    protected CanSWEConnectorSetting initSetting() {

	return new CanSWEConnectorSetting();
    }

    @Override
    public String getType() {

	return TYPE;
    }
}
