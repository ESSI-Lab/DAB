package eu.essi_lab.accessor.ththaiwater;

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
 * Harvests one record per ThaiWater time series (station or reservoir and observed property). The end of each series is the
 * latest observation listed by the service of the station type, while the begin is searched in the archive (see
 * {@link ThaiWaterClient#retrieveFirstObservation(ThaiWaterEntity)}).
 *
 * @author boldrini
 */
public class ThaiWaterConnector extends HarvestedQueryConnector<ThaiWaterConnectorSetting> {

    /**
     *
     */
    public static final String TYPE = "ThaiWaterConnector";

    /**
     * Maximum number of series harvested in a single page
     */
    static final int SERIES_PER_PAGE = 20;

    /** Metadata records emitted in the current harvest (across listRecords calls). */
    private int partialNumbers;

    private List<ThaiWaterEntity> series;

    private ThaiWaterClient client;

    @Override
    public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest request) throws GSException {

	GSLoggerFactory.getLogger(getClass()).debug("List records STARTED");

	ListRecordsResponse<OriginalMetadata> response = new ListRecordsResponse<>();

	Optional<Integer> mr = getSetting().getMaxRecords();
	boolean unlimited = getSetting().isMaxRecordsUnlimited();

	if (!unlimited && mr.isPresent() && partialNumbers >= mr.get()) {
	    response.setResumptionToken(null);
	    reset();
	    return response;
	}

	if (client == null) {
	    // the client caches the yearly responses, shared by the properties of the same station
	    client = new ThaiWaterClient(getSourceURL());
	}

	int index = 0;
	String resumptionToken = request.getResumptionToken();
	if (resumptionToken != null) {
	    index = Integer.parseInt(resumptionToken);
	}

	ensureSeriesLoaded();

	if (index >= series.size()) {
	    response.setResumptionToken(null);
	    reset();
	    return response;
	}

	int nextIndex = Math.min(index + SERIES_PER_PAGE, series.size());

	GSLoggerFactory.getLogger(getClass()).debug("Retrieving first observations of series [{}-{}/{}]", index, nextIndex,
		series.size());

	boolean stoppedByMax = false;

	for (ThaiWaterEntity entity : series.subList(index, nextIndex)) {

	    if (!unlimited && mr.isPresent() && partialNumbers >= mr.get()) {
		stoppedByMax = true;
		break;
	    }

	    Optional<Date> begin = Optional.empty();
	    try {
		begin = client.retrieveFirstObservation(entity);
	    } catch (GSException e) {
		GSLoggerFactory.getLogger(getClass()).warn("Unable to retrieve the first observation of {}", entity.getSeriesId());
	    }

	    // the latest observation is the only one known
	    String beginText = begin.map(ISO8601DateTimeUtils::getISO8601DateTime).orElse(entity.getString(ThaiWaterClient.END));
	    entity.getObject().put(ThaiWaterClient.BEGIN, beginText);

	    OriginalMetadata originalMetadata = new OriginalMetadata();
	    originalMetadata.setMetadata(entity.toString());
	    originalMetadata.setSchemeURI(ThaiWaterMapper.THAIWATER_SCHEMA);
	    response.addRecord(originalMetadata);
	    partialNumbers++;
	}

	if (stoppedByMax || nextIndex >= series.size()) {
	    response.setResumptionToken(null);
	    reset();
	} else {
	    response.setResumptionToken(String.valueOf(nextIndex));
	}

	GSLoggerFactory.getLogger(getClass()).debug("List records ENDED");

	return response;
    }

    /**
     * Prepares the connector for the next harvest
     */
    private void reset() {

	partialNumbers = 0;
	series = null;
	client = null;
    }

    /**
     * @throws GSException
     */
    private void ensureSeriesLoaded() throws GSException {

	if (series != null) {
	    return;
	}

	List<ThaiWaterEntity> all = client.retrieveSeries(getSetting().getStationTypes());

	series = new ArrayList<>();
	for (ThaiWaterEntity entity : all) {
	    if (getSetting().accept(entity)) {
		series.add(entity);
	    }
	}

	GSLoggerFactory.getLogger(getClass()).info("Retrieved {} ThaiWater series", series.size());
    }

    @Override
    public List<String> listMetadataFormats() throws GSException {

	return Arrays.asList(ThaiWaterMapper.THAIWATER_SCHEMA);
    }

    @Override
    public boolean supports(GSSource source) {

	return source.getEndpoint() != null && source.getEndpoint().contains("thaiwater.net");
    }

    @Override
    protected ThaiWaterConnectorSetting initSetting() {

	return new ThaiWaterConnectorSetting();
    }

    @Override
    public String getType() {

	return TYPE;
    }
}
