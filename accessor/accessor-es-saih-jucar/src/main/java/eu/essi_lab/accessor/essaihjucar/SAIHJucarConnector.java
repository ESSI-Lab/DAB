package eu.essi_lab.accessor.essaihjucar;

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
 * Harvests one record per SAIH Júcar variable (idVariable) of the configured categories. The begin and the end of each series
 * are the first and the last valid observations, found with a few small requests (see
 * {@link SAIHJucarClient#retrieveExtent(String)}).
 *
 * @author boldrini
 */
public class SAIHJucarConnector extends HarvestedQueryConnector<SAIHJucarConnectorSetting> {

    /**
     *
     */
    public static final String TYPE = "SAIHJucarConnector";

    /**
     * Maximum number of series harvested in a single page
     */
    static final int SERIES_PER_PAGE = 10;

    /** Metadata records emitted in the current harvest (across listRecords calls). */
    private int partialNumbers;

    private List<SAIHJucarEntity> series;

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

	SAIHJucarClient client = new SAIHJucarClient(getSourceURL());

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

	GSLoggerFactory.getLogger(getClass()).debug("Retrieving temporal extents of series [{}-{}/{}]", index, nextIndex,
		series.size());

	boolean stoppedByMax = false;

	for (SAIHJucarEntity entity : series.subList(index, nextIndex)) {

	    if (!unlimited && mr.isPresent() && partialNumbers >= mr.get()) {
		stoppedByMax = true;
		break;
	    }

	    Optional<SimpleEntry<Date, Date>> extent = client.retrieveExtent(entity.getVariableId());
	    if (extent.isEmpty()) {
		GSLoggerFactory.getLogger(getClass()).debug("Series {} has no observations", entity.getVariableId());
		continue;
	    }

	    entity.getObject().put(SAIHJucarClient.BEGIN, ISO8601DateTimeUtils.getISO8601DateTime(extent.get().getKey()));
	    entity.getObject().put(SAIHJucarClient.END, ISO8601DateTimeUtils.getISO8601DateTime(extent.get().getValue()));

	    OriginalMetadata originalMetadata = new OriginalMetadata();
	    originalMetadata.setMetadata(entity.toString());
	    originalMetadata.setSchemeURI(SAIHJucarMapper.SAIH_JUCAR_SCHEMA);
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
    private void ensureSeriesLoaded(SAIHJucarClient client) throws GSException {

	if (series != null) {
	    return;
	}

	List<String> variables = getSetting().getVariables();
	List<SAIHJucarEntity> all = client.retrieveVariables(getSetting().getCategories());

	series = new ArrayList<>();
	for (SAIHJucarEntity entity : all) {
	    if (variables.isEmpty() || variables.contains(entity.getVariableId())) {
		series.add(entity);
	    }
	}

	GSLoggerFactory.getLogger(getClass()).info("Retrieved {} SAIH Júcar series", series.size());
    }

    @Override
    public List<String> listMetadataFormats() throws GSException {

	return Arrays.asList(SAIHJucarMapper.SAIH_JUCAR_SCHEMA);
    }

    @Override
    public boolean supports(GSSource source) {

	return source.getEndpoint() != null && source.getEndpoint().contains("saih.chj.es");
    }

    @Override
    protected SAIHJucarConnectorSetting initSetting() {

	return new SAIHJucarConnectorSetting();
    }

    @Override
    public String getType() {

	return TYPE;
    }
}
