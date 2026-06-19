package eu.essi_lab.accessor.depegelonline;

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.accessor.depegelonline.PegelonlineEntity.EntityType;
import eu.essi_lab.cdk.harvest.HarvestedQueryConnector;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.StringUtils;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * @author boldrini
 */
public class PegelonlineConnector extends HarvestedQueryConnector<PegelonlineConnectorSetting> {

    /**
     * 
     */
    public static final String TYPE = "PegelonlineConnector";

    /** Metadata records emitted in the current harvest (across listRecords calls). */
    private int partialNumbers;

    private final HashMap<String, PegelonlineEntity> stations = new HashMap<>();
    private final List<String> stationIdentifiers = new ArrayList<>();
    private boolean stationsLoaded = false;

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

	PegelonlineClient client = new PegelonlineClient(getSourceURL());

	int stationIndex = 0;
	String resumptionToken = request.getResumptionToken();
	if (resumptionToken != null) {
	    stationIndex = Integer.parseInt(resumptionToken);
	}

	ensureStationsLoaded(client, stationIndex);

	if (stationIndex >= stationIdentifiers.size()) {
	    response.setResumptionToken(null);
	    partialNumbers = 0;
	    return response;
	}

	GSLoggerFactory.getLogger(getClass()).debug("Retrieving timeseries STARTED");

	String stationUuid = stationIdentifiers.get(stationIndex);
	PegelonlineEntity station = stations.get(stationUuid);
	JSONArray timeseriesList = station.getObject().optJSONArray("timeseries");

	GSLoggerFactory.getLogger(getClass()).debug("Retrieved {} timeseries",
		StringUtils.format(timeseriesList == null ? 0 : timeseriesList.length()));
	GSLoggerFactory.getLogger(getClass()).debug("Retrieving timeseries ENDED");

	boolean stoppedByMax = false;

	if (timeseriesList != null) {
	    for (int i = 0; i < timeseriesList.length(); i++) {
		if (!unlimited && mr.isPresent() && partialNumbers >= mr.get()) {
		    stoppedByMax = true;
		    break;
		}

		JSONObject timeseries = timeseriesList.getJSONObject(i);
		PegelonlineEntity entity = new PegelonlineEntity(new JSONObject(), EntityType.TIMESERIES);
		PegelonlineClient.mergeStationIntoTimeseries(station, timeseries, entity);

		String from = entity.getObject().optString(PegelonlineClient.FROM, null);
		String to = entity.getObject().optString(PegelonlineClient.TO, null);

		if (from != null && !from.isEmpty() && to != null && !to.isEmpty()) {
		    OriginalMetadata originalMetadata = new OriginalMetadata();
		    originalMetadata.setMetadata(entity.toString());
		    originalMetadata.setSchemeURI(PegelonlineMapper.PEGELONLINE_SCHEMA);
		    response.addRecord(originalMetadata);
		    partialNumbers++;
		} else {
		    GSLoggerFactory.getLogger(getClass()).warn("Timeseries {} has incomplete temporal extent",
			    PegelonlineClient.getTimeseriesId(station, timeseries));
		}
	    }
	}

	if (stoppedByMax) {
	    response.setResumptionToken(null);
	    partialNumbers = 0;
	    GSLoggerFactory.getLogger(getClass()).debug("List records ENDED (max records reached)");
	    return response;
	}

	if (stationIndex < (stationIdentifiers.size() - 1)) {
	    response.setResumptionToken(String.valueOf(stationIndex + 1));
	} else {
	    response.setResumptionToken(null);
	    partialNumbers = 0;
	}

	GSLoggerFactory.getLogger(getClass()).debug("List records ENDED");

	return response;
    }

    /**
     * @param client
     * @param stationIndex
     */
    private void ensureStationsLoaded(PegelonlineClient client, int stationIndex) {

	if (stationsLoaded && stationIdentifiers.size() > stationIndex) {
	    return;
	}

	if (!stationsLoaded) {
	    String stationNamePrefix = getSetting().getStationNamePrefix();
	    List<PegelonlineEntity> stationList = client.retrieveStations();

	    for (PegelonlineEntity station : stationList) {
		String label = station.getObject().optString("longname", "");
		if (stationNamePrefix != null && !stationNamePrefix.isEmpty() && !label.startsWith(stationNamePrefix)) {
		    continue;
		}

		String stationUuid = PegelonlineClient.getStationUuid(station);
		if (stationUuid == null || stationUuid.isEmpty()) {
		    continue;
		}

		stationIdentifiers.add(stationUuid);
		stations.put(stationUuid, station);
	    }

	    stationsLoaded = true;
	}
    }

    @Override
    public List<String> listMetadataFormats() throws GSException {

	return Arrays.asList(PegelonlineMapper.PEGELONLINE_SCHEMA);
    }

    @Override
    public boolean supports(GSSource source) {

	return source.getEndpoint().contains("pegelonline.wsv.de");
    }

    @Override
    protected PegelonlineConnectorSetting initSetting() {

	return new PegelonlineConnectorSetting();
    }

    @Override
    public String getType() {

	return TYPE;
    }
}
