package eu.essi_lab.accessor.lvlvgmc;

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
 * Harvests one record per LVGMC time series (hydrological station and parameter) of the dataset "Hidrometeoroloģiskie
 * novērojumi" of the Latvian Open Data Portal. The series and their temporal extent are retrieved at the first page with a
 * single aggregation query over the archive (last 365 days) and the operational data (last 48 hours).
 *
 * @author boldrini
 */
public class LVGMCConnector extends HarvestedQueryConnector<LVGMCConnectorSetting> {

    /**
     *
     */
    public static final String TYPE = "LVGMCConnector";

    /**
     * Maximum number of records of a single page
     */
    static final int RECORDS_PER_PAGE = 50;

    /** Metadata records emitted in the current harvest (across listRecords calls). */
    private int partialNumbers;

    private List<LVGMCEntity> series;

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

	ensureSeriesLoaded();

	if (index >= series.size()) {
	    response.setResumptionToken(null);
	    partialNumbers = 0;
	    return response;
	}

	int nextIndex = Math.min(index + RECORDS_PER_PAGE, series.size());

	GSLoggerFactory.getLogger(getClass()).debug("Listing series [{}-{}/{}]", index, nextIndex, series.size());

	boolean stoppedByMax = false;

	for (LVGMCEntity entity : series.subList(index, nextIndex)) {

	    if (!unlimited && mr.isPresent() && partialNumbers >= mr.get()) {
		stoppedByMax = true;
		break;
	    }

	    OriginalMetadata originalMetadata = new OriginalMetadata();
	    originalMetadata.setMetadata(entity.toString());
	    originalMetadata.setSchemeURI(LVGMCMapper.LVGMC_SCHEMA);
	    response.addRecord(originalMetadata);
	    partialNumbers++;
	}

	if (stoppedByMax || nextIndex >= series.size()) {
	    response.setResumptionToken(null);
	    partialNumbers = 0;
	    series = null;
	} else {
	    response.setResumptionToken(String.valueOf(nextIndex));
	}

	GSLoggerFactory.getLogger(getClass()).debug("List records ENDED");

	return response;
    }

    /**
     * @throws GSException
     */
    private void ensureSeriesLoaded() throws GSException {

	if (series != null) {
	    return;
	}

	series = new LVGMCClient(getSourceURL()).retrieveSeries(getSetting().getStations(), getSetting().getParameters());

	GSLoggerFactory.getLogger(getClass()).info("Retrieved {} LVGMC hydrological series", series.size());
    }

    @Override
    public List<String> listMetadataFormats() throws GSException {

	return Arrays.asList(LVGMCMapper.LVGMC_SCHEMA);
    }

    @Override
    public boolean supports(GSSource source) {

	String endpoint = source.getEndpoint();

	return endpoint != null && endpoint.contains("data.gov.lv")
		&& (endpoint.contains(LVGMCClient.DATASET_ID) || endpoint.contains(LVGMCClient.DATASET_NAME));
    }

    @Override
    protected LVGMCConnectorSetting initSetting() {

	return new LVGMCConnectorSetting();
    }

    @Override
    public String getType() {

	return TYPE;
    }
}
