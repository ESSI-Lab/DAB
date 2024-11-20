package eu.essi_lab.accessor.kisters;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2024 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

/**
 * @author Fabrizio
 */
public class KISTERSConnector extends HarvestedQueryConnector<KISTERSConnectorSetting> {

    /**
     * 
     */
    public static final String TYPE = "KISTERSConnector";
    private static final int DEFAULT_PARTITION_SIZE = 50;
    private List<List<OriginalMetadata>> partitions;
    private int recordsCount;

    @Override
    public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest request) throws GSException {

	GSLoggerFactory.getLogger(getClass()).debug("List records STARTED");

	ListRecordsResponse<OriginalMetadata> response = new ListRecordsResponse<OriginalMetadata>();

	if (partitions == null) {

	    List<OriginalMetadata> entityList = new ArrayList<>();

	    KISTERSClient kistersClient = new KISTERSClient(getSourceURL());

	    GSLoggerFactory.getLogger(getClass()).debug("Retrieving stations STARTED");

	    List<KISTERSEntity> stationsList = kistersClient.retrieveStations();

	    GSLoggerFactory.getLogger(getClass()).debug("Retrievied {} stations", StringUtils.format(stationsList.size()));

	    GSLoggerFactory.getLogger(getClass()).debug("Retrieving stations ENDED");

	    GSLoggerFactory.getLogger(getClass()).debug("Retrieving timeSeries STARTED");

	    List<KISTERSEntity> timeSeriesList = kistersClient.retrieveTimeSeries();

	    GSLoggerFactory.getLogger(getClass()).debug("Retrievied {} timeSeries", StringUtils.format(timeSeriesList.size()));

	    GSLoggerFactory.getLogger(getClass()).debug("Retrieving timeSeries ENDED");

	    //
	    // Stations
	    //

	    boolean onlyOneStation = false;

	    if (onlyOneStation) {
		stationsList=stationsList.subList(0, 1);
	    }

	    List<KISTERSEntity> finalStationsList = stationsList;

	    HashSet<String> stationIdentifiers = new HashSet<>();

	    finalStationsList.forEach(station -> {

		station.setType(EntityType.STATION);
		String stationId = station.getObject().getString(KISTERSClient.STATION_ID);
		stationIdentifiers.add(stationId);

		Optional<KISTERSEntity> childTimeSeries = timeSeriesList.//
		stream().//
		filter(ts -> ts.getObject().getString(KISTERSClient.STATION_ID)
			.equals(station.getObject().getString(KISTERSClient.STATION_ID))).//
		findFirst();

		if (childTimeSeries.isPresent()) {

		    copy(childTimeSeries.get(), station);
		}

		OriginalMetadata originalMetadata = new OriginalMetadata();
		originalMetadata.setMetadata(station.toString());
		originalMetadata.setSchemeURI(KISTERSMapper.KISTERS_SCHEMA);

		entityList.add(originalMetadata);
	    });

	    //
	    // Time series
	    //

	    timeSeriesList.forEach(timeSeries -> {
		String stationId = timeSeries.getObject().getString(KISTERSClient.STATION_ID);
		if (!stationIdentifiers.contains(stationId)) {
		    return;
		}
		String from = timeSeries.getObject().getString(KISTERSClient.TS_FROM);
		String to = timeSeries.getObject().getString(KISTERSClient.TS_TO);

		if (from != null && !from.isEmpty() && to != null && !to.isEmpty()) {

		    timeSeries.setType(EntityType.TIME_SERIES);

		    Optional<KISTERSEntity> parentStation = finalStationsList.//
		    stream().//
		    filter(s -> s.getObject().getString(KISTERSClient.STATION_ID).//
		    equals(timeSeries.getObject().getString(KISTERSClient.STATION_ID))).//
		    findFirst();

		    if (parentStation.isPresent()) {

			copy(parentStation.get(), timeSeries);
		    }

		    OriginalMetadata originalMetadata = new OriginalMetadata();
		    originalMetadata.setMetadata(timeSeries.toString());
		    originalMetadata.setSchemeURI(KISTERSMapper.KISTERS_SCHEMA);

		    entityList.add(originalMetadata);

		} else {

		    GSLoggerFactory.getLogger(getClass()).warn("Time series {} has incomplete temporal extent",
			    timeSeries.getObject().getString(KISTERSClient.TS_ID));
		}
	    });

	    GSLoggerFactory.getLogger(getClass()).debug("Total number of entities: {}",

		    StringUtils.format(entityList.size()));

	    partitions = Lists.partition(entityList, DEFAULT_PARTITION_SIZE);

	    GSLoggerFactory.getLogger(getClass()).debug("List partition size: {}", partitions.size());
	}

	int partitionIndex = 0;
	String resumptionToken = request.getResumptionToken();
	if (resumptionToken != null) {
	    partitionIndex = Integer.valueOf(resumptionToken);
	}

	GSLoggerFactory.getLogger(getClass()).debug("Current partition index: [{}/{}]", partitionIndex, partitions.size());
	partitions.get(partitionIndex).forEach(o -> response.addRecord(o));
	recordsCount += response.getRecordsAsList().size();

	int maxRecords = getSetting().getMaxRecords().orElse(Integer.MAX_VALUE);

	if (recordsCount >= maxRecords) {

	    GSLoggerFactory.getLogger(getClass()).debug("Max records {} reached", maxRecords);

	} else if (partitionIndex == partitions.size() - 1) {

	    GSLoggerFactory.getLogger(getClass()).debug("Last partition processed");

	} else {

	    partitionIndex++;

	    response.setResumptionToken(String.valueOf(partitionIndex));

	    GSLoggerFactory.getLogger(getClass()).debug("Set resumption token to: {}", partitionIndex);
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
