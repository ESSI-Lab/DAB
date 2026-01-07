package eu.essi_lab.accessor.dataloggers;

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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.json.JSONObject;
import org.slf4j.Logger;

import eu.essi_lab.cdk.harvest.HarvestedQueryConnector;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.OriginalMetadata;

public class DataloggersConnector extends HarvestedQueryConnector<DataloggersConnectorSetting> {

    /**
     * 
     */
    public static final String TYPE = "DataloggersConnector";

    private static final String DATALOGGERS_READ_ERROR = "Unable to retrieve dataloggers";
    private static final String DATALOGGERS_URL_NOT_FOUND_ERROR = "DATALOGGERS_URL_NOT_FOUND_ERROR";

    private List<String> originalMetadata;
    private int partialNumbers;
    private DataloggersClient client;
    private Logger logger = GSLoggerFactory.getLogger(this.getClass());

    public DataloggersConnector() {
	this.originalMetadata = new ArrayList<>();
    }

    @Override
    public boolean supports(GSSource source) {
	String endpoint = source.getEndpoint();
	return endpoint != null && !endpoint.isEmpty();
    }

    @Override
    public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest request) throws GSException {
	ListRecordsResponse<OriginalMetadata> ret = new ListRecordsResponse<>();
	if (originalMetadata.isEmpty()) {
	    originalMetadata = getOriginalMetadata();
	}

	String token = request.getResumptionToken();
	int start = 0;
	if (token != null) {
	    start = Integer.valueOf(token);
	}

	int pageSize = getSetting().getPageSize();

	Optional<Integer> mr = getSetting().getMaxRecords();
	boolean maxNumberReached = false;
	if (!getSetting().isMaxRecordsUnlimited() && mr.isPresent() && start > mr.get() - 1) {
	    maxNumberReached = true;
	}

	if (start < originalMetadata.size() && !maxNumberReached) {
	    int end = start + pageSize;
	    if (end > originalMetadata.size()) {
		end = originalMetadata.size();
	    }

	    if (!getSetting().isMaxRecordsUnlimited() && mr.isPresent() && end > mr.get()) {
		end = mr.get();
	    }
	    int count = 0;

	    for (int i = start; i < end; i++) {
		String om = originalMetadata.get(i);

		if (om != null && !om.isEmpty()) {
		    OriginalMetadata metadata = new OriginalMetadata();
		    metadata.setSchemeURI(CommonNameSpaceContext.DATALOGGERS_NS_URI);
		    metadata.setMetadata(om);
		    ret.addRecord(metadata);
		    partialNumbers++;
		    count++;
		}
	    }
	    ret.setResumptionToken(String.valueOf(start + count));
	    logger.debug("ADDED {} records. Number of analyzed datastreams: {}", partialNumbers, String.valueOf(start + count));

	} else {
	    ret.setResumptionToken(null);
	    logger.debug("Added Collection records: {} . TOTAL DATASTREAM SIZE: {}", partialNumbers, originalMetadata.size());
	    partialNumbers = 0;
	    return ret;
	}

	return ret;
    }

    private List<String> getOriginalMetadata() throws GSException {
	logger.trace("Dataloggers List Data finding STARTED");

	List<String> ret = new ArrayList<>();

	String endpoint = getSourceURL();
	if (endpoint == null || endpoint.isEmpty()) {
	    throw GSException.createException(//
		    this.getClass(), //
		    DATALOGGERS_READ_ERROR, //
		    null, //
		    ErrorInfo.ERRORTYPE_SERVICE, //
		    ErrorInfo.SEVERITY_ERROR, //
		    DATALOGGERS_URL_NOT_FOUND_ERROR);
	}

	try {
	    this.client = new DataloggersClient(endpoint);
	    List<Datalogger> allDataloggers = client.getAllDataloggers();

	    for (Datalogger datalogger : allDataloggers) {
		if (datalogger.getDatastreams() != null) {
		    for (Datastream datastream : datalogger.getDatastreams()) {
			// Create JSON metadata for each datastream
			JSONObject datastreamJson = new JSONObject();
			datastreamJson.put("datalogger", dataloggerToJson(datalogger));
			datastreamJson.put("datastream", datastreamToJson(datastream));
			ret.add(datastreamJson.toString());
		    }
		}
	    }

	} catch (IOException | InterruptedException e) {
	    logger.error("Error retrieving dataloggers", e);
	    throw GSException.createException(//
		    this.getClass(), //
		    DATALOGGERS_READ_ERROR + ": " + e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_SERVICE, //
		    ErrorInfo.SEVERITY_ERROR, //
		    DATALOGGERS_URL_NOT_FOUND_ERROR);
	}

	logger.trace("Dataloggers List Data finding ENDED");
	return ret;
    }

    @Override
    public List<String> listMetadataFormats() throws GSException {
	List<String> ret = new ArrayList<>();
	ret.add(CommonNameSpaceContext.DATALOGGERS_NS_URI);
	return ret;
    }

    @Override
    public String getType() {
	return TYPE;
    }

    @Override
    protected DataloggersConnectorSetting initSetting() {
	return new DataloggersConnectorSetting();
    }

    public DataloggersClient getClient() {
	return client;
    }

    public void setClient(DataloggersClient client) {
	this.client = client;
    }

    private JSONObject dataloggerToJson(Datalogger datalogger) {
	JSONObject json = new JSONObject();
	if (datalogger.getDataloggerId() != null) {
	    json.put("datalogger_id", datalogger.getDataloggerId());
	}
	if (datalogger.getDataloggerCod() != null) {
	    json.put("datalogger_cod", datalogger.getDataloggerCod());
	}
	if (datalogger.getDataproviderId() != null) {
	    json.put("dataprovider_id", datalogger.getDataproviderId());
	}
	if (datalogger.getDataproviderCod() != null) {
	    json.put("dataprovider_cod", datalogger.getDataproviderCod());
	}
	if (datalogger.getDataloggerLocation() != null) {
	    json.put("datalogger_location", datalogger.getDataloggerLocation());
	}
	if (datalogger.getDataloggerAvailableSince() != null) {
	    json.put("datalogger_available_since", datalogger.getDataloggerAvailableSince().toString());
	}
	if (datalogger.getDataloggerAvailableUntil() != null) {
	    json.put("datalogger_available_until", datalogger.getDataloggerAvailableUntil().toString());
	}
	return json;
    }

    private JSONObject datastreamToJson(Datastream datastream) {
	JSONObject json = new JSONObject();
	if (datastream.getUomId() != null) {
	    json.put("uom_id", datastream.getUomId());
	}
	if (datastream.getVarId() != null) {
	    json.put("var_id", datastream.getVarId());
	}
	if (datastream.getUomCod() != null) {
	    json.put("uom_cod", datastream.getUomCod());
	}
	if (datastream.getVarCod() != null) {
	    json.put("var_cod", datastream.getVarCod());
	}
	if (datastream.getDataloggerId() != null) {
	    json.put("datalogger_id", datastream.getDataloggerId());
	}
	if (datastream.getDatastreamId() != null) {
	    json.put("datastream_id", datastream.getDatastreamId());
	}
	if (datastream.getDataloggerCod() != null) {
	    json.put("datalogger_cod", datastream.getDataloggerCod());
	}
	if (datastream.getTipologiaRete() != null) {
	    json.put("tipologia_rete", datastream.getTipologiaRete());
	}
	if (datastream.getDataproviderId() != null) {
	    json.put("dataprovider_id", datastream.getDataproviderId());
	}
	if (datastream.getDatastreamStep() != null) {
	    json.put("datastream_step", datastream.getDatastreamStep());
	}
	if (datastream.getDataproviderCod() != null) {
	    json.put("dataprovider_cod", datastream.getDataproviderCod());
	}
	if (datastream.getDataloggerLocation() != null) {
	    json.put("datalogger_location", datastream.getDataloggerLocation());
	}
	if (datastream.getDatastreamAvailableSince() != null) {
	    json.put("datastream_available_since", datastream.getDatastreamAvailableSince().toString());
	}
	if (datastream.getDatastreamAvailableUntil() != null) {
	    json.put("datastream_available_until", datastream.getDatastreamAvailableUntil().toString());
	}
	return json;
    }
}

