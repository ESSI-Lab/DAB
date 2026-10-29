package eu.essi_lab.accessor.siarso;

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
 * Harvests one record per ARSO time series (station and quantity). The stations are listed by the XML of the latest data, while
 * the quantities of each station, the begin and the end of the series are read from the table of the last 30 days of the
 * station (one request per station); the end is updated with the latest values reported by the XML.
 *
 * @author boldrini
 */
public class ARSOConnector extends HarvestedQueryConnector<ARSOConnectorSetting> {

    /**
     *
     */
    public static final String TYPE = "ARSOConnector";

    /**
     * Maximum number of stations harvested in a single page
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

	ARSOClient client = new ARSOClient(getSourceURL());

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

	stationLoop: for (JSONObject station : stations.subList(index, nextIndex)) {

	    List<ARSOEntity> series = client.retrieveSeries(station, getSetting().getParameters());

	    for (ARSOEntity entity : series) {

		if (!unlimited && mr.isPresent() && partialNumbers >= mr.get()) {
		    stoppedByMax = true;
		    break stationLoop;
		}

		OriginalMetadata originalMetadata = new OriginalMetadata();
		originalMetadata.setMetadata(entity.toString());
		originalMetadata.setSchemeURI(ARSOMapper.ARSO_SCHEMA);
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
    private void ensureStationsLoaded(ARSOClient client) throws GSException {

	if (stations != null) {
	    return;
	}

	List<String> codes = getSetting().getStations();

	stations = new ArrayList<>();
	for (JSONObject station : client.retrieveStations()) {
	    if (codes.isEmpty() || codes.contains(station.getString(ARSOClient.STATION_ID))) {
		stations.add(station);
	    }
	}

	GSLoggerFactory.getLogger(getClass()).info("Retrieved {} ARSO stations", stations.size());
    }

    @Override
    public List<String> listMetadataFormats() throws GSException {

	return Arrays.asList(ARSOMapper.ARSO_SCHEMA);
    }

    @Override
    public boolean supports(GSSource source) {

	return source.getEndpoint() != null && source.getEndpoint().contains("arso.gov.si");
    }

    @Override
    protected ARSOConnectorSetting initSetting() {

	return new ARSOConnectorSetting();
    }

    @Override
    public String getType() {

	return TYPE;
    }
}
