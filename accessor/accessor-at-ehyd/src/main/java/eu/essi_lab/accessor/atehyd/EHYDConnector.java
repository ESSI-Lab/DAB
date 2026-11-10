package eu.essi_lab.accessor.atehyd;

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
 * Harvests one record per eHYD time series (station and archive product). The series are listed by the station list of each
 * category (one request per category), then the header, the first and the last valid value of each series are read from the
 * first and from the last bytes of its CSV file (two small HTTP range requests per series, made sequentially).
 *
 * @author boldrini
 */
public class EHYDConnector extends HarvestedQueryConnector<EHYDConnectorSetting> {

    /**
     *
     */
    public static final String TYPE = "EHYDConnector";

    /**
     * Maximum number of series harvested in a single page
     */
    static final int SERIES_PER_PAGE = 20;

    /** Metadata records emitted in the current harvest (across listRecords calls). */
    private int partialNumbers;

    private List<EHYDEntity> series;

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

	EHYDClient client = new EHYDClient(getSourceURL());

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

	GSLoggerFactory.getLogger(getClass()).debug("Reading begin and end of series [{}-{}/{}]", index, nextIndex, series.size());

	boolean stoppedByMax = false;

	for (EHYDEntity entity : series.subList(index, nextIndex)) {

	    if (!unlimited && mr.isPresent() && partialNumbers >= mr.get()) {
		stoppedByMax = true;
		break;
	    }

	    try {

		if (!client.completeSeries(entity)) {
		    GSLoggerFactory.getLogger(getClass()).debug("Series {} has no valid values", entity.getSeriesId());
		    continue;
		}

	    } catch (GSException e) {

		GSLoggerFactory.getLogger(getClass()).warn("Unable to read series {}, skipped", entity.getSeriesId());
		continue;
	    }

	    OriginalMetadata originalMetadata = new OriginalMetadata();
	    originalMetadata.setMetadata(entity.toString());
	    originalMetadata.setSchemeURI(EHYDMapper.EHYD_SCHEMA);
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
    private void ensureSeriesLoaded(EHYDClient client) throws GSException {

	if (series != null) {
	    return;
	}

	List<String> stations = getSetting().getStations();
	List<EHYDEntity> all = client.retrieveSeries(getSetting().getProducts());

	series = new ArrayList<>();
	for (EHYDEntity entity : all) {
	    if (stations.isEmpty() || stations.contains(entity.getHZBNumber())) {
		series.add(entity);
	    }
	}

	GSLoggerFactory.getLogger(getClass()).info("Retrieved {} eHYD series", series.size());
    }

    @Override
    public List<String> listMetadataFormats() throws GSException {

	return Arrays.asList(EHYDMapper.EHYD_SCHEMA);
    }

    @Override
    public boolean supports(GSSource source) {

	return source.getEndpoint() != null && source.getEndpoint().contains("ehyd.gv.at");
    }

    @Override
    protected EHYDConnectorSetting initSetting() {

	return new EHYDConnectorSetting();
    }

    @Override
    public String getType() {

	return TYPE;
    }
}
