package eu.essi_lab.accessor.smhi;

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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.apache.cxf.common.jaxb.JAXBUtils;

import eu.essi_lab.adk.timeseries.StationConnector;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.OriginalMetadata;

public class SMHIConnector extends StationConnector<SMHIConnectorSetting> {

    /**
     * 
     */
    public static final String TYPE = "SMHIConnector";

    public SMHIConnector() {
    }

    @Override
    public boolean supports(GSSource source) {
	String url = source.getEndpoint();
	return url.contains("smhi");
    }

    private SMHIClient client = null;

    @Override
    public ListRecordsResponse<OriginalMetadata> listTimeseries(String stationId) throws GSException {
	ListRecordsRequest request = new ListRecordsRequest();
	request.setResumptionToken(stationId);
	ListRecordsResponse<OriginalMetadata> ret = listRecords(request);
	return ret;

    }

    @Override
    public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest request) throws GSException {

	if (client == null) {
	    String sourceUrl = getSourceURL();
	    if (sourceUrl != null) {
		client = new SMHIClient(sourceUrl);
	    } else {
		client = new SMHIClient();
	    }
	}

	String parameterIndexString = request.getResumptionToken();
	if (parameterIndexString == null || parameterIndexString.isEmpty()) {
	    parameterIndexString = "0";
	}
	int parameterIndex = Integer.parseInt(parameterIndexString);

	ListRecordsResponse<OriginalMetadata> ret = new ListRecordsResponse<OriginalMetadata>();

	List<SMHIParameter> parameters = client.getParameters();

	if (parameterIndex < (parameters.size() - 1)) {
	    ret.setResumptionToken("" + (parameterIndex + 1));
	}

	SMHIParameter parameter = parameters.get(parameterIndex);

	List<SMHIStation> stations = client.getStations(parameter);

	for (SMHIStation station : stations) {

	    SMHIMetadata metadata = new SMHIMetadata();
	    metadata.setParameter(parameter);
	    metadata.setStation(station);
	    
	    OriginalMetadata metadataRecord = new OriginalMetadata();

	    metadataRecord.setSchemeURI(CommonNameSpaceContext.SMHI_URI);

	    try {
		metadataRecord.setMetadata(metadata.marshal());
	    } catch (Exception e) {
		e.printStackTrace();
		GSLoggerFactory.getLogger(getClass()).error("Error during SMHI marshalling");
	    }

	    ret.addRecord(metadataRecord);
	}

	return ret;

    }

    @Override
    public List<String> listMetadataFormats() throws GSException {
	List<String> ret = new ArrayList<String>();
	ret.add(CommonNameSpaceContext.SMHI_URI);
	return ret;
    }

    @Override
    public String getType() {

	return TYPE;
    }

    @Override
    protected SMHIConnectorSetting initSetting() {

	return new SMHIConnectorSetting();
    }

}
