package eu.essi_lab.accessor.kisters;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2025 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import com.google.common.collect.Lists;

import eu.essi_lab.accessor.kisters.KISTERSEntity.EntityType;
import eu.essi_lab.cdk.harvest.HarvestedQueryConnector;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.StringUtils;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.OriginalMetadata;
import ucar.nc2.ft.point.remote.PointStreamProto.StationList;

/**
 * @author Fabrizio
 */
public class KISTERSConnector extends HarvestedQueryConnector<KISTERSConnectorSetting> {

    /**
     * 
     */
    public static final String TYPE = "KISTERSConnector";
    // private List<List<OriginalMetadata>> partitions;
    private int recordsCount = 0;
    private HashMap<String, KISTERSEntity> stations = new HashMap<String, KISTERSEntity>();
    private List<String> stationIdentifiers = new ArrayList<>();

    @Override
    public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest request) throws GSException {

	GSLoggerFactory.getLogger(getClass()).debug("List records STARTED");

	ListRecordsResponse<OriginalMetadata> response = new ListRecordsResponse<OriginalMetadata>();

	KISTERSClient kistersClient = new KISTERSClient(getSourceURL());
	//
	// Stations
	//
	boolean onlySeries = true;
	boolean onlyOneStation = false;

	if (stations.isEmpty()) {

	    synchronized (stations) {

		if (stations.isEmpty()) {
		    GSLoggerFactory.getLogger(getClass()).debug("Retrieving stations STARTED");

		    String siteName = getSetting().getSiteName();

		    List<KISTERSEntity> stationsList = null;

		    stationsList = kistersClient.retrieveStations();

		    if (onlyOneStation) {
			stationsList = stationsList.subList(0, 1);
		    }

		    for (KISTERSEntity ke : stationsList) {
			String stationId = ke.getObject().getString(KISTERSClient.STATION_ID);
			String mySiteName = ke.getObject().optString(KISTERSClient.SITE_NAME);
			if (siteName != null && !siteName.isEmpty()) {
			    if (!mySiteName.startsWith(siteName)) {
				continue;
			    }
			}
			stationIdentifiers.add(stationId);
			stations.put(stationId, ke);
		    }

		    GSLoggerFactory.getLogger(getClass()).debug("Retrievied {} stations", StringUtils.format(stationsList.size()));

		    GSLoggerFactory.getLogger(getClass()).debug("Retrieving stations ENDED");
		}

	    }

	}

	// finalStationsList.forEach(station -> {
	//
	// station.setType(EntityType.STATION);
	// String stationId = station.getObject().getString(KISTERSClient.STATION_ID);
	// stationIdentifiers.add(stationId);
	//
	// Optional<KISTERSEntity> childTimeSeries = timeSeriesList.//
	// stream().//
	// filter(ts -> ts.getObject().getString(KISTERSClient.STATION_ID)
	// .equals(station.getObject().getString(KISTERSClient.STATION_ID)))
	// .//
	// findFirst();
	//
	// if (childTimeSeries.isPresent()) {
	//
	// copy(childTimeSeries.get(), station);
	// }
	//
	// OriginalMetadata originalMetadata = new OriginalMetadata();
	// originalMetadata.setMetadata(station.toString());
	// originalMetadata.setSchemeURI(KISTERSMapper.KISTERS_SCHEMA);
	// if (!onlySeries) {
	// entityList.add(originalMetadata);
	// }
	// });

	//
	// Time series
	//

	int stationIndex = 0;
	String resumptionToken = request.getResumptionToken();
	if (resumptionToken != null) {
	    stationIndex = Integer.valueOf(resumptionToken);
	}
	if (stationIndex>=stationIdentifiers.size()) {
	    ListRecordsResponse<OriginalMetadata> ret = new ListRecordsResponse<OriginalMetadata>();
	    return ret ;
	}

	GSLoggerFactory.getLogger(getClass()).debug("Retrieving timeSeries STARTED");

	String stationId = stationIdentifiers.get(stationIndex);
	KISTERSEntity station = stations.get(stationId);
	List<KISTERSEntity> timeSeriesList = kistersClient.retrieveTimeSeriesByStation(stationId);

	GSLoggerFactory.getLogger(getClass()).debug("Retrievied {} timeSeries", StringUtils.format(timeSeriesList.size()));

	GSLoggerFactory.getLogger(getClass()).debug("Retrieving timeSeries ENDED");

	List<OriginalMetadata> originalMetadatas = new ArrayList<>();

	timeSeriesList.forEach(timeSeries -> {

	    String from = timeSeries.getObject().getString(KISTERSClient.TS_FROM);
	    String to = timeSeries.getObject().getString(KISTERSClient.TS_TO);

	    if (from != null && !from.isEmpty() && to != null && !to.isEmpty()) {

		timeSeries.setType(EntityType.TIME_SERIES);

		copy(station, timeSeries);

		OriginalMetadata originalMetadata = new OriginalMetadata();
		originalMetadata.setMetadata(timeSeries.toString());
		originalMetadata.setSchemeURI(KISTERSMapper.KISTERS_SCHEMA);

		originalMetadatas.add(originalMetadata);
		response.addRecord(originalMetadata);
	    } else {

		GSLoggerFactory.getLogger(getClass()).warn("Time series {} has incomplete temporal extent",
			timeSeries.getObject().getString(KISTERSClient.TS_ID));
	    }
	});

	GSLoggerFactory.getLogger(getClass()).debug("Total number of metadatas: {}",

		StringUtils.format(originalMetadatas.size()));

	GSLoggerFactory.getLogger(getClass()).debug("List partition size: {}", originalMetadatas.size());

	GSLoggerFactory.getLogger(getClass()).debug("Current station index: [{}/{}]", stationIndex, stations.size());

	recordsCount += response.getRecordsAsList().size();

	int maxRecords = getSetting().getMaxRecords().orElse(Integer.MAX_VALUE);

	if (stationIndex < (stations.size() - 1) && recordsCount < maxRecords) {
	    response.setResumptionToken(String.valueOf(++stationIndex));
	} else {
	    response.setResumptionToken(null);
	}

	GSLoggerFactory.getLogger(getClass()).debug("List records ENDED");

	return response;
    }

    /**
     * @param source
     * @param target
     */
    private void copy(KISTERSEntity source, KISTERSEntity target) {

	source.getObject().//
		keys().//
		forEachRemaining(key -> {

		    if (!target.getObject().has(key)) {

			target.getObject().put(key, source.getObject().getString(key));
		    }
		});
    }

    @Override
    public List<String> listMetadataFormats() throws GSException {

	return Arrays.asList(KISTERSMapper.KISTERS_SCHEMA);
    }

    @Override
    public boolean supports(GSSource source) {

	return source.getEndpoint().startsWith("https://portal.grdc.bafg.de/KiWIS/KiWIS");
    }

    @Override
    protected KISTERSConnectorSetting initSetting() {

	return new KISTERSConnectorSetting();
    }

    @Override
    public String getType() {

	return TYPE;
    }
}
