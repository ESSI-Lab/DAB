package eu.essi_lab.accessor.ltlhmt;

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.json.JSONObject;

import eu.essi_lab.cdk.harvest.HarvestedQueryConnector;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * Harvests one record per LHMT time series (station and parameter) of the hydrological stations of the Meteo LT API. The
 * temporal extent of each series is the stored data range of its observation type (measured or historical).
 *
 * @author boldrini
 */
public class LHMTConnector extends HarvestedQueryConnector<LHMTConnectorSetting> {

    /**
     *
     */
    public static final String TYPE = "LHMTConnector";

    /**
     * Maximum number of stations harvested in a single page (up to 4 requests per station)
     */
    static final int STATIONS_PER_PAGE = 10;

    /** Metadata records emitted in the current harvest (across listRecords calls). */
    private int partialNumbers;

    private List<JSONObject> stations;

    @Override
    public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest request) throws GSException {

	GSLoggerFactory.getLogger(getClass()).debug("List records STARTED");

	ListRecordsResponse<OriginalMetadata> response = new ListRecordsResponse<>();

	Optional<Integer> mr = getSetting().getMaxRecords();
	boolean unlimited = getSetting().isMaxRecordsUnlimited();

	if (!unlimited && mr.isPresent() && partialNumbers >= mr.get()) {
	    response.setResumptionToken(null);
	    partialNumbers = 0;
	    return response;
	}

	LHMTClient client = new LHMTClient(getSourceURL());

	int index = 0;
	String resumptionToken = request.getResumptionToken();
	if (resumptionToken != null) {
	    index = Integer.parseInt(resumptionToken);
	}

	ensureStationsLoaded(client);

	if (index >= stations.size()) {
	    response.setResumptionToken(null);
	    partialNumbers = 0;
	    return response;
	}

	int nextIndex = Math.min(index + STATIONS_PER_PAGE, stations.size());

	GSLoggerFactory.getLogger(getClass()).debug("Retrieving series of stations [{}-{}/{}]", index, nextIndex, stations.size());

	boolean stoppedByMax = false;

	stations: for (JSONObject station : stations.subList(index, nextIndex)) {

	    for (LHMTEntity entity : client.retrieveSeries(station, getSetting().getParameters(),
		    getSetting().getStationsWithoutTemperature())) {

		if (!unlimited && mr.isPresent() && partialNumbers >= mr.get()) {
		    stoppedByMax = true;
		    break stations;
		}

		OriginalMetadata originalMetadata = new OriginalMetadata();
		originalMetadata.setMetadata(entity.toString());
		originalMetadata.setSchemeURI(LHMTMapper.LHMT_SCHEMA);
		response.addRecord(originalMetadata);
		partialNumbers++;
	    }
	}

	if (stoppedByMax || nextIndex >= stations.size()) {
	    response.setResumptionToken(null);
	    partialNumbers = 0;
	} else {
	    response.setResumptionToken(String.valueOf(nextIndex));
	}

	GSLoggerFactory.getLogger(getClass()).debug("List records ENDED");

	return response;
    }

    /**
     * @param client
     * @throws GSException
     */
    private void ensureStationsLoaded(LHMTClient client) throws GSException {

	if (stations != null) {
	    return;
	}

	List<String> codes = getSetting().getStations();

	stations = new ArrayList<>();
	for (JSONObject station : client.retrieveStations()) {
	    if (codes.isEmpty() || codes.contains(station.optString(LHMTClient.CODE))) {
		stations.add(station);
	    }
	}

	GSLoggerFactory.getLogger(getClass()).info("Retrieved {} LHMT hydrological stations", stations.size());
    }

    @Override
    public List<String> listMetadataFormats() throws GSException {

	return Arrays.asList(LHMTMapper.LHMT_SCHEMA);
    }

    @Override
    public boolean supports(GSSource source) {

	return source.getEndpoint().contains("api.meteo.lt");
    }

    @Override
    protected LHMTConnectorSetting initSetting() {

	return new LHMTConnectorSetting();
    }

    @Override
    public String getType() {

	return TYPE;
    }
}
