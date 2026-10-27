package eu.essi_lab.accessor.ieopw;

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
import java.util.Date;
import java.util.List;
import java.util.Optional;

import eu.essi_lab.cdk.harvest.HarvestedQueryConnector;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * Harvests one record per OPW time series (station and parameter), for the stations suitable for republication. The end of each
 * series is the latest observation listed by the map layer of the parameter, while the begin is the first valid row of the
 * archive of the whole period of record (only its beginning is downloaded).
 *
 * @author boldrini
 */
public class OPWConnector extends HarvestedQueryConnector<OPWConnectorSetting> {

    /**
     *
     */
    public static final String TYPE = "OPWConnector";

    /**
     * Maximum number of series harvested in a single page
     */
    static final int SERIES_PER_PAGE = 20;

    /** Metadata records emitted in the current harvest (across listRecords calls). */
    private int partialNumbers;

    private List<OPWEntity> series;

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

	OPWClient client = new OPWClient(getSourceURL());

	int index = 0;
	String resumptionToken = request.getResumptionToken();
	if (resumptionToken != null) {
	    index = Integer.parseInt(resumptionToken);
	}

	ensureSeriesLoaded(client);

	if (index >= series.size()) {
	    response.setResumptionToken(null);
	    partialNumbers = 0;
	    return response;
	}

	int nextIndex = Math.min(index + SERIES_PER_PAGE, series.size());

	GSLoggerFactory.getLogger(getClass()).debug("Retrieving first observations of series [{}-{}/{}]", index, nextIndex,
		series.size());

	boolean stoppedByMax = false;

	for (OPWEntity entity : series.subList(index, nextIndex)) {

	    if (!unlimited && mr.isPresent() && partialNumbers >= mr.get()) {
		stoppedByMax = true;
		break;
	    }

	    Optional<Date> begin = client.retrieveFirstObservation(entity);
	    if (begin.isEmpty()) {
		GSLoggerFactory.getLogger(getClass()).debug("Series {} has no observations", entity.getSeriesId());
		continue;
	    }

	    entity.getObject().put(OPWClient.BEGIN, ISO8601DateTimeUtils.getISO8601DateTime(begin.get()));

	    OriginalMetadata originalMetadata = new OriginalMetadata();
	    originalMetadata.setMetadata(entity.toString());
	    originalMetadata.setSchemeURI(OPWMapper.OPW_SCHEMA);
	    response.addRecord(originalMetadata);
	    partialNumbers++;
	}

	if (stoppedByMax || nextIndex >= series.size()) {
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
    private void ensureSeriesLoaded(OPWClient client) throws GSException {

	if (series != null) {
	    return;
	}

	List<String> stations = getSetting().getStations();
	List<OPWEntity> all = client.retrieveSeries(getSetting().getParameters());

	series = new ArrayList<>();
	for (OPWEntity entity : all) {
	    if (stations.isEmpty() || stations.contains(entity.getStationNumber())) {
		series.add(entity);
	    }
	}

	GSLoggerFactory.getLogger(getClass()).info("Retrieved {} OPW series", series.size());
    }

    @Override
    public List<String> listMetadataFormats() throws GSException {

	return Arrays.asList(OPWMapper.OPW_SCHEMA);
    }

    @Override
    public boolean supports(GSSource source) {

	return source.getEndpoint().contains("waterlevel.ie");
    }

    @Override
    protected OPWConnectorSetting initSetting() {

	return new OPWConnectorSetting();
    }

    @Override
    public String getType() {

	return TYPE;
    }
}
