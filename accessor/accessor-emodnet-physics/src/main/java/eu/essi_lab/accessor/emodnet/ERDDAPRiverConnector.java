package eu.essi_lab.accessor.emodnet;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
import java.util.List;

import org.slf4j.Logger;

import eu.essi_lab.cdk.harvest.HarvestedQueryConnector;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSProperty;
import eu.essi_lab.model.GSPropertyHandler;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * @author boldrini
 */
public class ERDDAPRiverConnector extends HarvestedQueryConnector<ERDDAPRiverConnectorSetting> {

    /**
     * 
     */
    public static final String TYPE = "ERDDAPRiverConnector";

    private Logger logger = GSLoggerFactory.getLogger(getClass());;

    private ERDDAPRiverClient client;

    @Override
    public boolean supports(GSSource source) {
	String endpoint = source.getEndpoint();
	return endpoint.contains("emodnet");
    }

    @Override
    public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest request) throws GSException {
	logger.info("Initialization Client");

	client = new ERDDAPRiverClient(getSourceURL());

	ListRecordsResponse<OriginalMetadata> ret = new ListRecordsResponse<>();

	List<ERDDAPRow> metadatas = client.getMetaData();

	for (ERDDAPRow metadata : metadatas) {

	    OriginalMetadata metadataRecord = new OriginalMetadata();
	    try {
		metadataRecord.setMetadata(metadata.toJSONObject().toString());
		metadataRecord.setSchemeURI(CommonNameSpaceContext.EMODNET_PHYSICS_RIVER_NS_URI);
		GSPropertyHandler info = new GSPropertyHandler();
		info.add(new GSProperty<String>("url", client.getBaseURL()));
		metadataRecord.setAdditionalInfo(info);
		ret.addRecord(metadataRecord);
	    } catch (Exception e) {
		GSLoggerFactory.getLogger(getClass()).error(e);
		e.printStackTrace();
	    }
	}
	return ret;

    }

    @Override
    public List<String> listMetadataFormats() throws GSException {
	List<String> ret = new ArrayList<>();
	ret.add(CommonNameSpaceContext.EMODNET_PHYSICS_RIVER_NS_URI);
	return ret;
    }

    @Override
    public String getType() {

	return TYPE;
    }

    @Override
    protected ERDDAPRiverConnectorSetting initSetting() {
	return new ERDDAPRiverConnectorSetting();
    }

}
