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
import java.util.TreeMap;

import org.json.JSONArray;
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
 * Harvests one metadata record for each time series of the CanSWE NetCDF file. The stations having the same coordinates
 * are grouped in a site; for each site and variable (snow water equivalent, snow depth, snow density) a single time
 * series is published if there is at most one value at each time, otherwise two time series are published: one with
 * the maximum and one with the minimum of the values at each time.
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
    public static final String RECORD_SITE = "site";
    public static final String RECORD_SITE_ID = "id";
    public static final String RECORD_SITE_LATITUDE = "latitude";
    public static final String RECORD_SITE_LONGITUDE = "longitude";
    public static final String RECORD_SITE_STATIONS = "stations";
    public static final String RECORD_VARIABLE = "variable";
    public static final String RECORD_VARIABLE_NAME = "name";
    public static final String RECORD_STATISTIC = "statistic";
    public static final String RECORD_BEGIN = "begin";
    public static final String RECORD_END = "end";
    public static final String RECORD_VALUES = "values";
    public static final String RECORD_MULTIPLE_VALUES = "timesWithMultipleValues";

    /**
     * Number of sites harvested by each listRecords call
     */
    static final int SITES_PER_PAGE = 100;

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
	List<CanSWESite> sites;

	try {
	    CanSWEClient client = new CanSWEClient(getSourceURL());
	    fileURL = client.getFileURL();
	    dataset = client.getDataset();
	    sites = dataset.getSites();

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e);

	    throw GSException.createException(//
		    getClass(), //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    CANSWE_CONNECTOR_ERROR, //
		    e);
	}

	JSONObject global = new JSONObject();
	for (Entry<String, String> entry : dataset.getGlobalAttributes().entrySet()) {
	    global.put(entry.getKey(), entry.getValue());
	}

	boolean maxReached = false;
	int end = Math.min(index + SITES_PER_PAGE, sites.size());

	for (int i = index; i < end && !maxReached; i++) {

	    CanSWESite site = sites.get(i);
	    JSONObject siteObject = toJSON(site);

	    for (String variable : CanSWEDataset.VARIABLES) {

		TreeMap<Integer, double[]> aggregate = site.aggregate(variable);
		long multiple = aggregate.values().stream().filter(v -> v[2] > 1).count();

		for (CanSWEStatistic statistic : CanSWESite.getStatistics(aggregate)) {

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
		    record.put(RECORD_SITE, siteObject);
		    record.put(RECORD_VARIABLE, variableObject);
		    record.put(RECORD_STATISTIC, statistic.name());
		    record.put(RECORD_BEGIN, dataset.getDate(aggregate.firstKey()).getTime());
		    record.put(RECORD_END, dataset.getDate(aggregate.lastKey()).getTime());
		    record.put(RECORD_VALUES, aggregate.size());
		    record.put(RECORD_MULTIPLE_VALUES, multiple);

		    OriginalMetadata originalMetadata = new OriginalMetadata();
		    originalMetadata.setMetadata(record.toString());
		    originalMetadata.setSchemeURI(CanSWEMapper.CANSWE_SCHEMA);
		    response.addRecord(originalMetadata);
		    partialNumbers++;
		}

		if (maxReached) {
		    break;
		}
	    }
	}

	GSLoggerFactory.getLogger(getClass()).debug("Sites [{}-{}]/{}: {} records so far", index + 1, end, sites.size(), partialNumbers);

	if (end < sites.size() && !maxReached) {
	    response.setResumptionToken(String.valueOf(end));
	} else {
	    response.setResumptionToken(null);
	    partialNumbers = 0;
	}

	GSLoggerFactory.getLogger(getClass()).debug("List records ENDED");

	return response;
    }

    /**
     * @param site
     * @return
     */
    static JSONObject toJSON(CanSWESite site) {

	JSONObject ret = new JSONObject();
	ret.put(RECORD_SITE_ID, site.getId());

	CanSWEStation first = site.getStations().get(0);
	if (!Double.isNaN(first.getLatitude()) && !Double.isNaN(first.getLongitude())) {
	    // float precision of the file
	    ret.put(RECORD_SITE_LATITUDE, Float.toString((float) first.getLatitude()));
	    ret.put(RECORD_SITE_LONGITUDE, Float.toString((float) first.getLongitude()));
	}

	JSONArray stations = new JSONArray();
	for (CanSWEStation station : site.getStations()) {
	    stations.put(station.toJSON());
	}
	ret.put(RECORD_SITE_STATIONS, stations);

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
