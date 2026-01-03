package eu.essi_lab.accessor.rasaqm;

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

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

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
public class RasaqmConnector extends HarvestedQueryConnector<RasaqmConnectorSetting> {

    /**
     * 
     */
    public static final String TYPE = "RasaqmConnector";
    private static final String RASAQM_CONNECTOR_ERROR = "RASAQM_CONNECTOR_ERROR";

    @Override
    public boolean supports(GSSource source) {
	return source.getEndpoint().contains("rasaqm");
    }

    private RasaqmClient client = new RasaqmClient();

    @Override
    public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest request) throws GSException {
	try {
	    ListRecordsResponse<OriginalMetadata> ret = new ListRecordsResponse<OriginalMetadata>();
	    List<SimpleEntry<String, String>> parameters = client.getParameters();
	    // SimpleEntry<String, String> p = parameters.get(0);
	    // parameters.clear();
	    // parameters.add(p);
	    for (SimpleEntry<String, String> parameter : parameters) {
		String parameterId = parameter.getKey();
		Date now = new Date();
		Date yesterday = new Date(now.getTime() - 1000 * 60 * 60 * 24l);
		RasaqmDataset dataset = client.parseData(parameterId, yesterday, yesterday, null); // gets stations that
												   // were
		// active yesterday
		Set<String> stationNames = dataset.getStationNames();
		for (String stationName : stationNames) {
		    RasaqmSeries serie = dataset.getSeries(stationName);
		    ByteArrayOutputStream baos = new ByteArrayOutputStream();
		    serie.marshal(baos);
		    String metadata = new String(baos.toByteArray(), StandardCharsets.UTF_8);
		    OriginalMetadata metadataRecord = new OriginalMetadata();
		    metadataRecord.setMetadata(metadata);
		    metadataRecord.setSchemeURI(CommonNameSpaceContext.RASAQM_URI);
		    ret.addRecord(metadataRecord);
		}
	    }
	    return ret;
	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e);

	    throw GSException.createException(//
		    getClass(), //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    RASAQM_CONNECTOR_ERROR, //
		    e);
	}

    }

    @Override
    public List<String> listMetadataFormats() throws GSException {
	List<String> ret = new ArrayList<String>();
	ret.add(CommonNameSpaceContext.RASAQM_URI);
	return ret;
    }

    @Override
    public String getType() {

	return TYPE;
    }

    @Override
    protected RasaqmConnectorSetting initSetting() {

	return new RasaqmConnectorSetting();
    }
}
