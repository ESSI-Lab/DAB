package eu.essi_lab.accessor.nlrijkswaterstaat;

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

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
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
 * Harvests one record per Rijkswaterstaat time series (location and observed property). For each page of series of the same location,
 * the latest observations give the end of each series, while the begin is approximated by a binary search of at most
 * {@link RijkswaterstaatConnectorSetting#getBeginSearchMaxRequests()} availability requests per series, or found exactly if
 * {@link RijkswaterstaatConnectorSetting#isExactBegin()}.
 *
 * @author boldrini
 */
public class RijkswaterstaatConnector extends HarvestedQueryConnector<RijkswaterstaatConnectorSetting> {

    /**
     *
     */
    public static final String TYPE = "RijkswaterstaatConnector";

    /**
     * Maximum number of series of the same location harvested in a single page
     */
    static final int SERIES_PER_PAGE = 50;

    /** Metadata records emitted in the current harvest (across listRecords calls). */
    private int partialNumbers;

    private List<RijkswaterstaatEntity> series;

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

	RijkswaterstaatClient client = new RijkswaterstaatClient(getSourceURL());

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

	List<RijkswaterstaatEntity> page = getPage(series, index, SERIES_PER_PAGE);
	int nextIndex = index + page.size();

	GSLoggerFactory.getLogger(getClass()).debug("Retrieving latest observations of {} series of location {} [{}/{}]", page.size(),
		page.get(0).getLocationCode(), nextIndex, series.size());

	Map<String, SimpleEntry<Date, Date>> latest = client.retrieveLatestObservations(page);
	int maxRequests = getSetting().getBeginSearchMaxRequests();
	boolean exactBegin = getSetting().isExactBegin();

	boolean stoppedByMax = false;

	for (RijkswaterstaatEntity entity : page) {

	    if (!unlimited && mr.isPresent() && partialNumbers >= mr.get()) {
		stoppedByMax = true;
		break;
	    }

	    SimpleEntry<Date, Date> extent = latest.get(entity.getSeriesId());
	    if (extent == null) {
		GSLoggerFactory.getLogger(getClass()).debug("Series {} has no observations", entity.getSeriesId());
		continue;
	    }

	    // the earliest of the latest observations of the sub series is a known observation: the first observation is before it
	    Date begin = exactBegin ? //
		    client.findFirstObservation(entity, extent.getKey()) : //
		    client.estimateFirstObservation(entity, extent.getKey(), maxRequests);
	    Date end = extent.getValue();

	    entity.getObject().put(RijkswaterstaatClient.BEGIN, ISO8601DateTimeUtils.getISO8601DateTime(begin));
	    entity.getObject().put(RijkswaterstaatClient.END, ISO8601DateTimeUtils.getISO8601DateTime(end));

	    OriginalMetadata originalMetadata = new OriginalMetadata();
	    originalMetadata.setMetadata(entity.toString());
	    originalMetadata.setSchemeURI(RijkswaterstaatMapper.RIJKSWATERSTAAT_SCHEMA);
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
     * @param series series sorted by location
     * @param index
     * @param maxSize
     * @return the series starting from index, of the same location, at most maxSize
     */
    static List<RijkswaterstaatEntity> getPage(List<RijkswaterstaatEntity> series, int index, int maxSize) {

	List<RijkswaterstaatEntity> ret = new ArrayList<>();
	String location = series.get(index).getLocationCode();

	for (int i = index; i < series.size() && ret.size() < maxSize; i++) {
	    if (!series.get(i).getLocationCode().equals(location)) {
		break;
	    }
	    ret.add(series.get(i));
	}

	return ret;
    }

    /**
     * @param client
     * @throws GSException
     */
    private void ensureSeriesLoaded(RijkswaterstaatClient client) throws GSException {

	if (series != null) {
	    return;
	}

	String prefix = getSetting().getLocationCodePrefix();
	List<RijkswaterstaatEntity> all = client.retrieveSeries(getSetting().getProcessTypes());

	series = new ArrayList<>();
	for (RijkswaterstaatEntity entity : all) {
	    if (prefix == null || prefix.isEmpty() || entity.getLocationCode().startsWith(prefix)) {
		series.add(entity);
	    }
	}

	GSLoggerFactory.getLogger(getClass()).info("Retrieved {} Rijkswaterstaat series", series.size());
    }

    @Override
    public List<String> listMetadataFormats() throws GSException {

	return Arrays.asList(RijkswaterstaatMapper.RIJKSWATERSTAAT_SCHEMA);
    }

    @Override
    public boolean supports(GSSource source) {

	return source.getEndpoint().contains("waterwebservices.rijkswaterstaat.nl");
    }

    @Override
    protected RijkswaterstaatConnectorSetting initSetting() {

	return new RijkswaterstaatConnectorSetting();
    }

    @Override
    public String getType() {

	return TYPE;
    }
}
