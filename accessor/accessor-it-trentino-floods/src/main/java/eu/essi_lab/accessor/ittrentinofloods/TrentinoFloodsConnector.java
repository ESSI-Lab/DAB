package eu.essi_lab.accessor.ittrentinofloods;

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
 * Harvests one record per hydrometric sensor of the Ufficio Dighe of the Provincia autonoma di Trento (each sensor observes a single
 * property, water level or calculated discharge). The begin and end of each series are the first and last valid values of the file
 * of the sensor (which holds the last 7 days only); sensors without valid values are discarded.
 *
 * @author boldrini
 */
public class TrentinoFloodsConnector extends HarvestedQueryConnector<TrentinoFloodsConnectorSetting> {

    /**
     *
     */
    public static final String TYPE = "TrentinoFloodsConnector";

    /**
     * Maximum number of series harvested in a single page (one request per series)
     */
    static final int SERIES_PER_PAGE = 10;

    /** Metadata records emitted in the current harvest (across listRecords calls). */
    private int partialNumbers;

    private List<TrentinoFloodsEntity> series;

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

	TrentinoFloodsClient client = new TrentinoFloodsClient(getSourceURL());

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

	GSLoggerFactory.getLogger(getClass()).debug("Retrieving observations of series [{}-{}/{}]", index, nextIndex, series.size());

	boolean stoppedByMax = false;

	for (TrentinoFloodsEntity entity : series.subList(index, nextIndex)) {

	    if (!unlimited && mr.isPresent() && partialNumbers >= mr.get()) {
		stoppedByMax = true;
		break;
	    }

	    if (!client.addTemporalExtent(entity)) {
		GSLoggerFactory.getLogger(getClass()).debug("Series {} has no observations", entity.getSeriesId());
		continue;
	    }

	    OriginalMetadata originalMetadata = new OriginalMetadata();
	    originalMetadata.setMetadata(entity.toString());
	    originalMetadata.setSchemeURI(TrentinoFloodsMapper.TRENTINO_FLOODS_SCHEMA);
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
    private void ensureSeriesLoaded(TrentinoFloodsClient client) throws GSException {

	if (series != null) {
	    return;
	}

	List<String> sensors = getSetting().getSensors();
	List<TrentinoFloodsEntity> all = client.retrieveSeries();

	series = new ArrayList<>();
	for (TrentinoFloodsEntity entity : all) {
	    if (sensors.isEmpty() || sensors.contains(entity.getSensorId())) {
		series.add(entity);
	    }
	}

	GSLoggerFactory.getLogger(getClass()).info("Retrieved {} Trentino floods series", series.size());
    }

    @Override
    public List<String> listMetadataFormats() throws GSException {

	return Arrays.asList(TrentinoFloodsMapper.TRENTINO_FLOODS_SCHEMA);
    }

    @Override
    public boolean supports(GSSource source) {

	return source.getEndpoint() != null && source.getEndpoint().contains("floods.it");
    }

    @Override
    protected TrentinoFloodsConnectorSetting initSetting() {

	return new TrentinoFloodsConnectorSetting();
    }

    @Override
    public String getType() {

	return TYPE;
    }
}
