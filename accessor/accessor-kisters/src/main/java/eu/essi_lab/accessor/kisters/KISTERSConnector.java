package eu.essi_lab.accessor.kisters;

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

import eu.essi_lab.accessor.kisters.KISTERSEntity.*;
import eu.essi_lab.cdk.harvest.*;
import eu.essi_lab.lib.utils.*;
import eu.essi_lab.messages.listrecords.*;
import eu.essi_lab.model.*;
import eu.essi_lab.model.exceptions.*;
import eu.essi_lab.model.resource.*;

import java.util.*;
import java.util.regex.Pattern;

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

	List<String> groupIds = getSetting().getTimeSeriesGroupIds();

	// the pages are the stations, or the time series groups if configured
	int pagesCount = groupIds.isEmpty() ? stationIdentifiers.size() : groupIds.size();

	int pageIndex = 0;
	String resumptionToken = request.getResumptionToken();
	if (resumptionToken != null) {
	    pageIndex = Integer.valueOf(resumptionToken);
	}
	if (pageIndex >= pagesCount) {
	    ListRecordsResponse<OriginalMetadata> ret = new ListRecordsResponse<OriginalMetadata>();
	    return ret;
	}

	GSLoggerFactory.getLogger(getClass()).debug("Retrieving timeSeries STARTED");

	List<KISTERSEntity> timeSeriesList = null;

	if (groupIds.isEmpty()) {

	    timeSeriesList = kistersClient.retrieveTimeSeriesByStation(stationIdentifiers.get(pageIndex));

	} else {

	    GSLoggerFactory.getLogger(getClass()).debug("Retrieving timeSeries of group {}", groupIds.get(pageIndex));

	    timeSeriesList = kistersClient.retrieveTimeSeriesByGroup(groupIds.get(pageIndex));
	}

	GSLoggerFactory.getLogger(getClass()).debug("Retrievied {} timeSeries", StringUtils.format(timeSeriesList.size()));

	GSLoggerFactory.getLogger(getClass()).debug("Retrieving timeSeries ENDED");

	List<OriginalMetadata> originalMetadatas = new ArrayList<>();

	Optional<Pattern> namePattern = getSetting().getTimeSeriesNameRegex().map(Pattern::compile);

	boolean siteFilter = getSetting().getSiteName() != null && !getSetting().getSiteName().isEmpty();

	timeSeriesList.forEach(timeSeries -> {

	    String tsId = timeSeries.getObject().getString(KISTERSClient.TS_ID);

	    KISTERSEntity station = stations.get(timeSeries.getObject().optString(KISTERSClient.STATION_ID));

	    if (station == null && (groupIds.isEmpty() || siteFilter)) {

		// group mode: the station has been excluded by the site name filter
		return;
	    }

	    if (namePattern.isPresent() && !namePattern.get().matcher(timeSeries.getObject().optString(KISTERSClient.TS_NAME)).matches()) {

		return;
	    }

	    String from = timeSeries.getObject().getString(KISTERSClient.TS_FROM);
	    String to = timeSeries.getObject().getString(KISTERSClient.TS_TO);

	    if (from != null && !from.isEmpty() && to != null && !to.isEmpty()) {

		timeSeries.setType(EntityType.TIME_SERIES);

		if (station != null) {

		    copy(station, timeSeries);
		}

		resolveParameterLongName(station, timeSeries);

		if (timeSeries.getObject().optString(KISTERSClient.STATION_LAT).isEmpty()
			|| timeSeries.getObject().optString(KISTERSClient.STATION_LON).isEmpty()) {

		    // e.g. the basin pseudo stations of Wallonia
		    GSLoggerFactory.getLogger(getClass()).warn("Time series {} has no station coordinates", tsId);
		    return;
		}

		OriginalMetadata originalMetadata = new OriginalMetadata();
		originalMetadata.setMetadata(timeSeries.toString());
		originalMetadata.setSchemeURI(KISTERSMapper.KISTERS_SCHEMA);

		originalMetadatas.add(originalMetadata);
		response.addRecord(originalMetadata);
	    } else {

		GSLoggerFactory.getLogger(getClass()).warn("Time series {} has incomplete temporal extent", tsId);
	    }
	});

	GSLoggerFactory.getLogger(getClass()).debug("Total number of metadatas: {}",

		StringUtils.format(originalMetadatas.size()));

	GSLoggerFactory.getLogger(getClass()).debug("List partition size: {}", originalMetadatas.size());

	GSLoggerFactory.getLogger(getClass()).debug("Current page index: [{}/{}]", pageIndex, pagesCount);

	recordsCount += response.getRecordsAsList().size();

	int maxRecords = getSetting().getMaxRecords().orElse(Integer.MAX_VALUE);

	if (pageIndex < (pagesCount - 1) && recordsCount < maxRecords) {
	    response.setResumptionToken(String.valueOf(++pageIndex));
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

    /**
     * When a station measures more than one parameter, the station list returns their ids, names and long names joined by ';'
     * (e.g. SEPA). The time series carries its own parameter type id and name, while the long name is only available from the
     * station, so it is picked from the station list at the same position of the time series parameter type id. Time series
     * retrieved by group carry the long name of their station parameter (e.g. "River Discharge"), which is used if the station
     * long name is missing (e.g. HIC Flanders) or ambiguous
     * 
     * @param station
     * @param timeSeries
     */
    static void resolveParameterLongName(KISTERSEntity station, KISTERSEntity timeSeries) {

	String stationParamLongName = timeSeries.getObject().optString(KISTERSClient.STATION_PARAM_LONG_NAME);
	String currentLongName = timeSeries.getObject().optString(KISTERSClient.PARAM_TYPE_LONG_NAME);

	if (!stationParamLongName.isEmpty() && (currentLongName.isEmpty() || currentLongName.contains(";"))) {

	    timeSeries.getObject().put(KISTERSClient.PARAM_TYPE_LONG_NAME, stationParamLongName);
	    return;
	}

	if (station == null) {

	    return;
	}

	String stationParamIds = station.getObject().optString(KISTERSClient.PARAM_TYPE_ID);
	String stationParamLongNames = station.getObject().optString(KISTERSClient.PARAM_TYPE_LONG_NAME);
	String paramId = timeSeries.getObject().optString(KISTERSClient.PARAM_TYPE_ID);

	if (!stationParamIds.contains(";") || paramId.isEmpty() || paramId.contains(";")) {

	    return;
	}

	List<String> ids = Arrays.asList(stationParamIds.split(";", -1));
	List<String> longNames = Arrays.asList(stationParamLongNames.split(";", -1));

	int index = ids.indexOf(paramId);

	if (index >= 0 && ids.size() == longNames.size()) {

	    timeSeries.getObject().put(KISTERSClient.PARAM_TYPE_LONG_NAME, longNames.get(index));

	} else {

	    timeSeries.getObject().put(KISTERSClient.PARAM_TYPE_LONG_NAME, timeSeries.getObject().optString(KISTERSClient.PARAM_TYPE_NAME));
	}
    }

    @Override
    public List<String> listMetadataFormats() throws GSException {

	return Arrays.asList(KISTERSMapper.KISTERS_SCHEMA);
    }

    @Override
    public boolean supports(GSSource source) {

	return source.getEndpoint().contains("/KiWIS/KiWIS");
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
