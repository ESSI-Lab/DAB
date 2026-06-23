package eu.essi_lab.accessor.ukhydrology;

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

import eu.essi_lab.accessor.ukhydrology.UKHydrologyEntity.EntityType;
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
public class UKHydrologyConnector extends HarvestedQueryConnector<UKHydrologyConnectorSetting> {

    /**
     * 
     */
    public static final String TYPE = "UKHydrologyConnector";

    /** Metadata records emitted in the current harvest (across listRecords calls). */
    private int partialNumbers;

    private final HashMap<String, UKHydrologyEntity> stations = new HashMap<>();
    private final List<String> stationIdentifiers = new ArrayList<>();
    private int stationApiOffset = 0;
    private boolean stationsExhausted = false;

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

	UKHydrologyClient client = new UKHydrologyClient(getSourceURL());

	int stationIndex = 0;
	String resumptionToken = request.getResumptionToken();
	if (resumptionToken != null) {
	    stationIndex = Integer.parseInt(resumptionToken);
	}

	ensureStationLoaded(client, stationIndex);

	if (stationIndex >= stationIdentifiers.size()) {
	    response.setResumptionToken(null);
	    partialNumbers = 0;
	    return response;
	}

	GSLoggerFactory.getLogger(getClass()).debug("Retrieving measures STARTED");

	String stationGuid = stationIdentifiers.get(stationIndex);
	UKHydrologyEntity station = stations.get(stationGuid);
	List<UKHydrologyEntity> measuresList = client.retrieveMeasures(stationGuid);

	GSLoggerFactory.getLogger(getClass()).debug("Retrieved {} measures", StringUtils.format(measuresList.size()));
	GSLoggerFactory.getLogger(getClass()).debug("Retrieving measures ENDED");

	boolean stoppedByMax = false;

	for (UKHydrologyEntity measure : measuresList) {
	    if (!unlimited && mr.isPresent() && partialNumbers >= mr.get()) {
		stoppedByMax = true;
		break;
	    }

	    measure.setType(EntityType.MEASURE);
	    UKHydrologyClient.mergeStationIntoMeasure(station, measure);

	    String from = measure.getObject().optString(UKHydrologyClient.FROM, null);
	    String to = measure.getObject().optString(UKHydrologyClient.TO, null);

	    if (from != null && !from.isEmpty() && to != null && !to.isEmpty()) {
		OriginalMetadata originalMetadata = new OriginalMetadata();
		originalMetadata.setMetadata(measure.toString());
		originalMetadata.setSchemeURI(UKHydrologyMapper.UK_HYDROLOGY_SCHEMA);
		response.addRecord(originalMetadata);
		partialNumbers++;
	    } else {
		GSLoggerFactory.getLogger(getClass()).warn("Measure {} has incomplete temporal extent",
			UKHydrologyClient.getMeasureNotation(measure));
	    }
	}

	if (stoppedByMax) {
	    response.setResumptionToken(null);
	    partialNumbers = 0;
	    GSLoggerFactory.getLogger(getClass()).debug("List records ENDED (max records reached)");
	    return response;
	}

	if (stationIndex < (stationIdentifiers.size() - 1) || hasMoreStations()) {
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
    private void ensureStationLoaded(UKHydrologyClient client, int stationIndex) {

	String stationNamePrefix = getSetting().getStationNamePrefix();

	while (stationIdentifiers.size() <= stationIndex && !stationsExhausted) {
	    List<UKHydrologyEntity> page = client.retrieveStationsPage(UKHydrologyClient.getStationPageSize(),
		    stationApiOffset);

	    if (page.isEmpty()) {
		stationsExhausted = true;
		break;
	    }

	    for (UKHydrologyEntity station : page) {
		String label = station.getObject().optString("label", "");
		if (stationNamePrefix != null && !stationNamePrefix.isEmpty() && !label.startsWith(stationNamePrefix)) {
		    continue;
		}
		String stationGuid = UKHydrologyClient.getStationGuid(station);
		stationIdentifiers.add(stationGuid);
		stations.put(stationGuid, station);
	    }

	    stationApiOffset += page.size();
	    if (page.size() < UKHydrologyClient.getStationPageSize()) {
		stationsExhausted = true;
	    }
	}
    }

    /**
     * @return
     */
    private boolean hasMoreStations() {

	return !stationsExhausted;
    }

    @Override
    public List<String> listMetadataFormats() throws GSException {

	return Arrays.asList(UKHydrologyMapper.UK_HYDROLOGY_SCHEMA);
    }

    @Override
    public boolean supports(GSSource source) {

	return source.getEndpoint().contains("environment.data.gov.uk/hydrology");
    }

    @Override
    protected UKHydrologyConnectorSetting initSetting() {

	return new UKHydrologyConnectorSetting();
    }

    @Override
    public String getType() {

	return TYPE;
    }
}
