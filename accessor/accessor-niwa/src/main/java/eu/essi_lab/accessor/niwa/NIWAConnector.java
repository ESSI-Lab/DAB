package eu.essi_lab.accessor.niwa;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2024 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import eu.essi_lab.cdk.harvest.HarvestedQueryConnector;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * @author Fabrizio
 */
public class NIWAConnector extends HarvestedQueryConnector<NIWAConnectorSetting> {

    /**
     * 
     */
    public static final String TYPE = "NIWAConnector";
    /**
     * 
     */
    private static final String NIWA_CONNECTOR_GET_DATA_LIST_ERROR = "NIWA_CONNECTOR_GET_DATA_LIST_ERROR";

    @Override
    public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest request) throws GSException {

	NIWAClient client = new NIWAClient();

	List<JSONObject> dataList;
	try {

	    GSLoggerFactory.getLogger(getClass()).debug("Getting data list STARTED");

	    dataList = client.getDataList();

	    GSLoggerFactory.getLogger(getClass()).debug("Getting data list ENDED");

	} catch (IOException e) {

	    throw GSException.createException(//
		    getClass(), //
		    ErrorInfo.ERRORTYPE_SERVICE, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    NIWA_CONNECTOR_GET_DATA_LIST_ERROR, //
		    e);
	}

	GSLoggerFactory.getLogger(getClass()).debug("Creating response STARTED");

	ListRecordsResponse<OriginalMetadata> response = new ListRecordsResponse<>();

	int maxRecords = getSetting().getMaxRecords().orElse(dataList.size());

	GSLoggerFactory.getLogger(getClass()).debug("Number of records to process: {}", maxRecords);

	for (int i = 0; i < maxRecords; i++) {

	    OriginalMetadata metadataRecord = new OriginalMetadata();
	    metadataRecord.setMetadata(dataList.get(i).toString(3));
	    metadataRecord.setSchemeURI(CommonNameSpaceContext.NIWA_NS_URI);

	    response.addRecord(metadataRecord);
	}

	GSLoggerFactory.getLogger(getClass()).debug("Creating response ENDED");

	return response;
    }

    @Override
    public List<String> listMetadataFormats() throws GSException {
	List<String> ret = new ArrayList<>();
	ret.add(CommonNameSpaceContext.NIWA_NS_URI);
	return ret;
    }

    @Override
    public String getType() {

	return TYPE;
    }

    @Override
    public boolean supports(GSSource source) {

	String endpoint = source.getEndpoint();
	return endpoint.contains("hydrowebportal.niwa.co.nz");
    }

    @Override
    protected NIWAConnectorSetting initSetting() {

	return new NIWAConnectorSetting();
    }
}
