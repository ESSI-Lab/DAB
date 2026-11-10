package eu.essi_lab.accessor.luage;

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
 * Harvests one record per time series (station and parameter) of the stations of the inondations.lu flood portal of the Water
 * Management Agency of Luxembourg (AGE). Only the stations operated by AGE are harvested, since the licence (CC0) of the stations
 * of the other agencies published by the portal is not known. The temporal extent of each series is the one of its published values
 * (the last 7 days).
 *
 * @author boldrini
 */
public class AGEConnector extends HarvestedQueryConnector<AGEConnectorSetting> {

    /**
     *
     */
    public static final String TYPE = "AGEConnector";

    /**
     * Maximum number of stations harvested in a single page (2 requests per station)
     */
    static final int STATIONS_PER_PAGE = 10;

    /** Metadata records emitted in the current harvest (across listRecords calls). */
    private int partialNumbers;

    private List<String> stationPages;

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

	AGEClient client = new AGEClient(getSourceURL());

	int index = 0;
	String resumptionToken = request.getResumptionToken();
	if (resumptionToken != null) {
	    index = Integer.parseInt(resumptionToken);
	}

	ensureStationPagesLoaded(client);

	if (index >= stationPages.size()) {
	    response.setResumptionToken(null);
	    partialNumbers = 0;
	    return response;
	}

	int nextIndex = Math.min(index + STATIONS_PER_PAGE, stationPages.size());

	GSLoggerFactory.getLogger(getClass()).debug("Retrieving series of stations [{}-{}/{}]", index, nextIndex, stationPages.size());

	List<String> codes = getSetting().getStations();

	boolean stoppedByMax = false;

	stations: for (String page : stationPages.subList(index, nextIndex)) {

	    Optional<JSONObject> station = client.retrieveStation(page);
	    if (station.isEmpty() || (!codes.isEmpty() && !codes.contains(station.get().getString(AGEClient.CODE)))) {
		continue;
	    }

	    if (!AGEClient.isOperatedByAGE(station.get())) {
		GSLoggerFactory.getLogger(getClass()).debug("Skipping station {} operated by {}", station.get().getString(AGEClient.CODE),
			station.get().optString(AGEClient.OPERATOR, "unknown operator"));
		continue;
	    }

	    for (AGEEntity entity : client.retrieveSeries(station.get())) {

		if (!unlimited && mr.isPresent() && partialNumbers >= mr.get()) {
		    stoppedByMax = true;
		    break stations;
		}

		OriginalMetadata originalMetadata = new OriginalMetadata();
		originalMetadata.setMetadata(entity.toString());
		originalMetadata.setSchemeURI(AGEMapper.AGE_SCHEMA);
		response.addRecord(originalMetadata);
		partialNumbers++;
	    }
	}

	if (stoppedByMax || nextIndex >= stationPages.size()) {
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
    private void ensureStationPagesLoaded(AGEClient client) throws GSException {

	if (stationPages != null) {
	    return;
	}

	stationPages = client.retrieveStationPages();

	GSLoggerFactory.getLogger(getClass()).info("Retrieved {} AGE station pages", stationPages.size());
    }

    @Override
    public List<String> listMetadataFormats() throws GSException {

	return Arrays.asList(AGEMapper.AGE_SCHEMA);
    }

    @Override
    public boolean supports(GSSource source) {

	String endpoint = source.getEndpoint();

	return endpoint != null && (endpoint.contains("inondations.public.lu") || endpoint.contains("inondations.lu"));
    }

    @Override
    protected AGEConnectorSetting initSetting() {

	return new AGEConnectorSetting();
    }

    @Override
    public String getType() {

	return TYPE;
    }
}
