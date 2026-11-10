package eu.essi_lab.accessor.esaca;

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
 * Harvests one record per ACA time series (Sentilo sensor) of the configured component types. The end of each series is the latest
 * observation returned by the data service, while the begin is the start of the rolling window kept by the service (about the last
 * 3 months). The whole harvest needs two requests per Sentilo provider (catalog and latest observations).
 *
 * @author boldrini
 */
public class ACAConnector extends HarvestedQueryConnector<ACAConnectorSetting> {

    /**
     *
     */
    public static final String TYPE = "ACAConnector";

    /**
     * Maximum number of series harvested in a single page
     */
    static final int SERIES_PER_PAGE = 50;

    /** Metadata records emitted in the current harvest (across listRecords calls). */
    private int partialNumbers;

    private List<ACAEntity> series;

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

	ensureSeriesLoaded(new ACAClient(getSourceURL()));

	if (index >= series.size()) {
	    response.setResumptionToken(null);
	    partialNumbers = 0;
	    return response;
	}

	int nextIndex = Math.min(index + SERIES_PER_PAGE, series.size());

	boolean stoppedByMax = false;

	for (ACAEntity entity : series.subList(index, nextIndex)) {

	    if (!unlimited && mr.isPresent() && partialNumbers >= mr.get()) {
		stoppedByMax = true;
		break;
	    }

	    OriginalMetadata originalMetadata = new OriginalMetadata();
	    originalMetadata.setMetadata(entity.toString());
	    originalMetadata.setSchemeURI(ACAMapper.ACA_SCHEMA);
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
    private void ensureSeriesLoaded(ACAClient client) throws GSException {

	if (series != null) {
	    return;
	}

	List<String> components = getSetting().getComponents();
	List<ACAEntity> all = client.retrieveSeries(getSetting().getComponentTypes());

	series = new ArrayList<>();
	for (ACAEntity entity : all) {
	    if (components.isEmpty() || components.contains(entity.getComponent())) {
		series.add(entity);
	    }
	}

	GSLoggerFactory.getLogger(getClass()).info("Retrieved {} ACA series", series.size());
    }

    @Override
    public List<String> listMetadataFormats() throws GSException {

	return Arrays.asList(ACAMapper.ACA_SCHEMA);
    }

    @Override
    public boolean supports(GSSource source) {

	return source.getEndpoint().contains("aplicacions.aca.gencat.cat");
    }

    @Override
    protected ACAConnectorSetting initSetting() {

	return new ACAConnectorSetting();
    }

    @Override
    public String getType() {

	return TYPE;
    }
}
