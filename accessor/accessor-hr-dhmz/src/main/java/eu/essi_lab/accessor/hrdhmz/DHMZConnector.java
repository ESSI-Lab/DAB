package eu.essi_lab.accessor.hrdhmz;

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

import org.json.JSONObject;

import eu.essi_lab.cdk.harvest.HarvestedQueryConnector;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * Harvests the DHMZ series. The whole station list (with the years of measurement of each data type and the latest real time
 * observation) is retrieved with a single request, then the series are emitted in pages
 *
 * @author boldrini
 */
public class DHMZConnector extends HarvestedQueryConnector<DHMZConnectorSetting> {

    /**
     *
     */
    public static final String TYPE = "DHMZConnector";

    /**
     * Maximum number of series harvested in a single page
     */
    static final int SERIES_PER_PAGE = 100;

    /** Metadata records emitted in the current harvest (across listRecords calls). */
    private int partialNumbers;

    private List<DHMZEntity> series;

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

	int index = 0;
	String resumptionToken = request.getResumptionToken();
	if (resumptionToken != null) {
	    index = Integer.parseInt(resumptionToken);
	}

	ensureSeriesLoaded(new DHMZClient(getSourceURL()));

	if (index >= series.size()) {
	    response.setResumptionToken(null);
	    partialNumbers = 0;
	    return response;
	}

	int nextIndex = Math.min(index + SERIES_PER_PAGE, series.size());

	boolean stoppedByMax = false;

	for (DHMZEntity entity : series.subList(index, nextIndex)) {

	    if (!unlimited && mr.isPresent() && partialNumbers >= mr.get()) {
		stoppedByMax = true;
		break;
	    }

	    OriginalMetadata originalMetadata = new OriginalMetadata();
	    originalMetadata.setMetadata(entity.toString());
	    originalMetadata.setSchemeURI(DHMZMapper.DHMZ_SCHEMA);
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
    private void ensureSeriesLoaded(DHMZClient client) throws GSException {

	if (series != null) {
	    return;
	}

	List<String> stations = getSetting().getStations();
	List<DHMZProperty> properties = getSetting().getDataTypes();
	boolean realTime = getSetting().isRealTime();
	Date now = new Date();

	series = new ArrayList<>();

	for (JSONObject station : client.retrieveStations()) {

	    if (!stations.isEmpty() && !stations.contains(station.optString(DHMZClient.SIFRA, "").trim())) {
		continue;
	    }

	    series.addAll(DHMZClient.createSeries(station, properties, realTime, now));
	}

	GSLoggerFactory.getLogger(getClass()).info("Retrieved {} DHMZ series", series.size());
    }

    @Override
    public List<String> listMetadataFormats() throws GSException {

	return Arrays.asList(DHMZMapper.DHMZ_SCHEMA);
    }

    @Override
    public boolean supports(GSSource source) {

	return source.getEndpoint() != null && source.getEndpoint().contains("hidro.dhz.hr");
    }

    @Override
    protected DHMZConnectorSetting initSetting() {

	return new DHMZConnectorSetting();
    }

    @Override
    public String getType() {

	return TYPE;
    }
}
