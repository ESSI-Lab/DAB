package eu.essi_lab.accessor.chslf;

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

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.json.JSONObject;

import eu.essi_lab.cdk.harvest.HarvestedQueryConnector;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.lib.utils.StringUtils;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * Harvests one metadata record for each station and parameter published by the SLF Measurement API
 *
 * @author boldrini
 */
public class SLFConnector extends HarvestedQueryConnector<SLFConnectorSetting> {

    /**
     *
     */
    public static final String TYPE = "SLFConnector";

    private static final int PAGE_SIZE = 50;

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

	if (records == null || index == 0) {
	    records = buildRecords(new SLFClient(getSourceURL()));
	    partialNumbers = 0;
	}

	int end = Math.min(index + PAGE_SIZE, records.size());

	for (; index < end; index++) {

	    if (!unlimited && mr.isPresent() && partialNumbers >= mr.get()) {
		break;
	    }

	    OriginalMetadata originalMetadata = new OriginalMetadata();
	    originalMetadata.setMetadata(records.get(index).toString());
	    originalMetadata.setSchemeURI(SLFMapper.SLF_SCHEMA);
	    response.addRecord(originalMetadata);
	    partialNumbers++;
	}

	boolean maxReached = !unlimited && mr.isPresent() && partialNumbers >= mr.get();

	if (index < records.size() && !maxReached) {
	    response.setResumptionToken(String.valueOf(index));
	} else {
	    response.setResumptionToken(null);
	    records = null;
	    partialNumbers = 0;
	}

	GSLoggerFactory.getLogger(getClass()).debug("List records ENDED");

	return response;
    }

    /**
     * @param client
     * @return
     */
    private List<JSONObject> buildRecords(SLFClient client) {

	List<String> stationFilter = getSetting().getStationCodes();

	SimpleEntry<Date, Date> extent = SLFClient.getDefaultExtent();
	String from = ISO8601DateTimeUtils.getISO8601DateTime(extent.getKey());
	String to = ISO8601DateTimeUtils.getISO8601DateTime(extent.getValue());

	List<JSONObject> ret = new ArrayList<>();

	for (SLFNetwork network : SLFNetwork.values()) {

	    List<JSONObject> stations = client.retrieveStations(network);

	    GSLoggerFactory.getLogger(getClass()).debug("Retrieved {} {} stations", StringUtils.format(stations.size()), network.getId());

	    Map<String, Set<String>> available = null;
	    if (!network.isSeasonal()) {
		Optional<Map<String, Set<String>>> optional = client.retrieveAvailableParameters(network);
		if (optional.isPresent()) {
		    available = optional.get();
		} else {
		    GSLoggerFactory.getLogger(getClass()).warn("Unable to retrieve last {} measurements, all parameters will be harvested",
			    network.getId());
		}
	    }

	    stations.sort(Comparator.comparing(s -> s.optString(SLFClient.STATION_CODE, "")));

	    for (JSONObject station : stations) {

		String code = station.optString(SLFClient.STATION_CODE, null);
		if (code == null || code.isEmpty()) {
		    continue;
		}
		if (!stationFilter.isEmpty() && !stationFilter.contains(code)) {
		    continue;
		}

		Set<String> stationParameters = available == null ? null : available.get(code);
		if (available != null && stationParameters == null) {
		    // the station has not reported any value for this network in the last 24 hours
		    continue;
		}

		for (SLFParameter parameter : SLFParameter.getParameters(network)) {

		    if (stationParameters != null && !stationParameters.contains(parameter.getCode())) {
			continue;
		    }

		    JSONObject record = new JSONObject(station.toString());
		    record.put(SLFClient.NETWORK, network.getId());
		    record.put(SLFClient.PARAMETER, parameter.getCode());
		    record.put(SLFClient.TIMESERIES_ID, SLFClient.buildTimeseriesId(network, code, parameter.getCode()));
		    record.put(SLFClient.FROM, from);
		    record.put(SLFClient.TO, to);

		    ret.add(record);
		}
	    }
	}

	GSLoggerFactory.getLogger(getClass()).debug("Found {} SLF timeseries", StringUtils.format(ret.size()));

	return ret;
    }

    @Override
    public List<String> listMetadataFormats() throws GSException {

	return Arrays.asList(SLFMapper.SLF_SCHEMA);
    }

    @Override
    public boolean supports(GSSource source) {

	return source.getEndpoint().contains("measurement-api.slf.ch");
    }

    @Override
    protected SLFConnectorSetting initSetting() {

	return new SLFConnectorSetting();
    }

    @Override
    public String getType() {

	return TYPE;
    }
}
