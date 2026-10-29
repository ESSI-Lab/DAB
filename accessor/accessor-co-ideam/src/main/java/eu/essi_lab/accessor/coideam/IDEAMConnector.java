package eu.essi_lab.accessor.coideam;

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

import eu.essi_lab.cdk.harvest.HarvestedQueryConnector;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * Harvests one record per IDEAM time series (dataset, station and sensor). The series of each dataset are listed with their first
 * and last observation by a single SoQL aggregation query; then each series is described by its most recent row (station name,
 * location, sensor and unit) and by the entry of its station in the national station catalog.
 *
 * @author boldrini
 */
public class IDEAMConnector extends HarvestedQueryConnector<IDEAMConnectorSetting> {

    /**
     *
     */
    public static final String TYPE = "IDEAMConnector";

    /**
     * Maximum number of series harvested in a single page
     */
    static final int SERIES_PER_PAGE = 20;

    /** Metadata records emitted in the current harvest (across listRecords calls). */
    private int partialNumbers;

    private List<IDEAMEntity> series;

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

	IDEAMClient client = new IDEAMClient(getSourceURL(), getSetting().getAppToken());

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

	GSLoggerFactory.getLogger(getClass()).debug("Describing series [{}-{}/{}]", index, nextIndex, series.size());

	List<IDEAMEntity> page = new ArrayList<>();

	boolean stoppedByMax = false;

	for (IDEAMEntity entity : series.subList(index, nextIndex)) {

	    if (!unlimited && mr.isPresent() && partialNumbers + page.size() >= mr.get()) {
		stoppedByMax = true;
		break;
	    }

	    if (!client.completeSeries(entity)) {
		GSLoggerFactory.getLogger(getClass()).debug("Series {} has no observations", entity.getSeriesId());
		continue;
	    }

	    page.add(entity);
	}

	if (!page.isEmpty()) {
	    try {
		client.addCatalogEntries(page);
	    } catch (GSException e) {
		GSLoggerFactory.getLogger(getClass()).warn("Unable to read the IDEAM station catalog");
	    }
	}

	for (IDEAMEntity entity : page) {

	    OriginalMetadata originalMetadata = new OriginalMetadata();
	    originalMetadata.setMetadata(entity.toString());
	    originalMetadata.setSchemeURI(IDEAMMapper.IDEAM_SCHEMA);
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
    private void ensureSeriesLoaded(IDEAMClient client) throws GSException {

	if (series != null) {
	    return;
	}

	List<String> stations = getSetting().getStations();

	series = new ArrayList<>();

	for (IDEAMDataset dataset : getSetting().getDatasets()) {

	    GSLoggerFactory.getLogger(getClass()).info("Retrieving series of IDEAM dataset {} ({})", dataset.getName(), dataset.getId());

	    series.addAll(client.retrieveSeries(dataset, stations));
	}

	GSLoggerFactory.getLogger(getClass()).info("Retrieved {} IDEAM series", series.size());
    }

    @Override
    public List<String> listMetadataFormats() throws GSException {

	return Arrays.asList(IDEAMMapper.IDEAM_SCHEMA);
    }

    @Override
    public boolean supports(GSSource source) {

	return source.getEndpoint() != null && source.getEndpoint().contains("datos.gov.co");
    }

    @Override
    protected IDEAMConnectorSetting initSetting() {

	return new IDEAMConnectorSetting();
    }

    @Override
    public String getType() {

	return TYPE;
    }
}
