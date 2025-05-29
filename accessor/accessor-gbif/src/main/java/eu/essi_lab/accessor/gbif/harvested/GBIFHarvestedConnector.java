/**
 * 
 */
package eu.essi_lab.accessor.gbif.harvested;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2025 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import eu.essi_lab.accessor.gbif.GBIFUtils;
import eu.essi_lab.accessor.oaipmh.OAIPMHAccessor;
import eu.essi_lab.accessor.oaipmh.OAIPMHConnectorSetting;
import eu.essi_lab.cdk.harvest.HarvestedQueryConnector;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.BrokeringStrategy;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * @author Fabrizio
 */
public class GBIFHarvestedConnector extends HarvestedQueryConnector<GBIFHarvestedConnectorSetting> {

    public final static String CONNECTOR_TYPE = "GBIFHarvestedConnector";

    private static final List<String> SETS_LIST = new ArrayList<String>();
    private static final String NEXT_SET_TOKEN = "NEXT_SET_TOKEN";
    static {

	SETS_LIST.add("dataset_type:OCCURRENCE");
	SETS_LIST.add("dataset_type:SAMPLING_EVENT");
    }

    private static final String PREFERRED_PREFIX = "oai_dc";

    private int setIndex;
    private int count;
    private OAIPMHAccessor oaiAccessor;
    private OAIPMHConnectorSetting connectorSetting;

    /**
     * 
     */
    public GBIFHarvestedConnector() {
    }

    @Override
    public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest request) throws GSException {

	if (Objects.isNull(oaiAccessor)) {

	    GSSource source = new GSSource();

	    source.setBrokeringStrategy(BrokeringStrategy.HARVESTED);
	    source.setEndpoint("http://api.gbif.org/v1/oai-pmh/registry");
	    source.setUniqueIdentifier("GBIF");
	    source.setLabel("GBIF");

	    oaiAccessor = new OAIPMHAccessor();

	    oaiAccessor.getSetting().getGSSourceSetting().setSource(source);

	    oaiAccessor.getConnector().setSourceURL("http://api.gbif.org/v1/oai-pmh/registry");

	    connectorSetting = oaiAccessor.getConnector().getSetting();

	    connectorSetting.setPreferredPrefix(PREFERRED_PREFIX);

	    connectorSetting.setMaxRecords(getSetting().getMaxRecords().orElse(0));
	}

	GSLoggerFactory.getLogger(getClass()).debug("Current set: " + SETS_LIST.get(setIndex));
	connectorSetting.setSetName(SETS_LIST.get(setIndex));

	ListRecordsResponse<OriginalMetadata> listRecordsResponse = new ListRecordsResponse<>();

	if (Objects.nonNull(request.getResumptionToken()) && request.getResumptionToken().equals(NEXT_SET_TOKEN)) {
	    request.setResumptionToken(null);
	}

	ListRecordsResponse<GSResource> response = oaiAccessor.listRecords(request);
	Iterator<GSResource> records = response.getRecords();

	String resumptionToken = response.getResumptionToken();
	if (resumptionToken == null) {

	    switch (setIndex) {
	    case 0:
		setIndex = 1;
		resumptionToken = NEXT_SET_TOKEN;
		break;
	    case 1:
		break;
	    }
	}

	Optional<Integer> mr = getSetting().getMaxRecords();

	if (mr.isPresent() && count == mr.get()) {
	    GSLoggerFactory.getLogger(getClass()).info("Reached max. records: {}", mr.get());
	    GSLoggerFactory.getLogger(getClass()).info("Setting null resumption token to stop the harvesting");

	    listRecordsResponse.setResumptionToken(null);
	} else {

	    listRecordsResponse.setResumptionToken(resumptionToken);
	}

	records.forEachRemaining(r -> {

	    count++;
	    r.getOriginalMetadata().setSchemeURI(GBIFCollectionMapper.GBIF_COLLECTION_MAPPER_SCHEME_URI);
	    listRecordsResponse.addRecord(r.getOriginalMetadata());
	});

	return listRecordsResponse;
    }

    @Override
    public List<String> listMetadataFormats() throws GSException {

	return Arrays.asList(GBIFCollectionMapper.GBIF_COLLECTION_MAPPER_SCHEME_URI);
    }

    @Override
    public boolean supports(GSSource source) {

	return GBIFUtils.supportsSource(source);
    }

    @Override
    public String getType() {

	return CONNECTOR_TYPE;
    }

    @Override
    protected GBIFHarvestedConnectorSetting initSetting() {

	return new GBIFHarvestedConnectorSetting();
    }
}
