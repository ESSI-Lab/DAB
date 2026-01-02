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
import java.net.URISyntaxException;
import java.net.http.HttpTimeoutException;
import java.util.AbstractMap.SimpleEntry;
import java.util.concurrent.TimeUnit;

import org.json.JSONObject;
import org.slf4j.Logger;

import eu.essi_lab.cdk.harvest.HarvestedQueryConnector;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.net.utils.HttpConnectionUtils;
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
public class EMODNETPhysicsConnector extends HarvestedQueryConnector<EMODNETPhysicsConnectorSetting> {

    /**
     * 
     */
    public static final String TYPE = "EMODNETPhysicsConnector";

    private Logger logger = GSLoggerFactory.getLogger(getClass());;

    private EMODNETPhysicsClient client;

    @Override
    public boolean supports(GSSource source) {
	String endpoint = source.getEndpoint();
	return endpoint.contains("emodnet");
    }

    /**
     * @return
     */

    public boolean isDownloadLinkSet() {
	return getSetting().isDownloadLinkSet();
    }

    @Override
    public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest request) throws GSException {
	logger.info("Initialization Client");

	client = new EMODNETPhysicsClient(getSourceURL());

	ListRecordsResponse<OriginalMetadata> ret = new ListRecordsResponse<>();

	SimpleEntry<List<String>, List<String>> datasetIdentifiers;
	try {
	    datasetIdentifiers = client.getIdentifiers();
	} catch (Exception e) {
	    e.printStackTrace();
	    throw GSException.createException();
	}
	List<String> identifiers = datasetIdentifiers.getKey();
	List<String> metadataIdentifiers = datasetIdentifiers.getValue();

	for (String identifier : identifiers) {
	    JSONObject metadata = null;
	    JSONObject additionalMetadata = null;
	    try {
		metadata = client.getMetadata(identifier);
		String metadataIdentifier = identifier + "_METADATA";
		if (metadataIdentifiers.contains(metadataIdentifier)) {
		    additionalMetadata = client.getMetadata(metadataIdentifier);
		    metadata.put("additionalMetadata", additionalMetadata);
		}
	    } catch (Exception e) {
		e.printStackTrace();
		throw GSException.createException();
	    }

	    OriginalMetadata metadataRecord = new OriginalMetadata();
	    metadataRecord.setMetadata(metadata.toString());
	    metadataRecord.setSchemeURI(CommonNameSpaceContext.EMODNET_PHYSICS_NS_URI);
	    Boolean isDownloadLink = isDownloadLinkSet();
	    GSPropertyHandler handler = GSPropertyHandler.of(new GSProperty<Boolean>("isDownloadLink", isDownloadLink));
	    if (isDownloadLink) {
		String downloadLink = getSourceURL() + "/tabledap/" + identifier + ".nc";
		boolean isAvailable = false;
		try {
		    isAvailable = HttpConnectionUtils.checkConnectivity(downloadLink, TimeUnit.MINUTES, 2);
		} catch (Exception e) {
		    GSLoggerFactory.getLogger(getClass()).error(e);
		}
		if (!isAvailable) {
		    downloadLink = getSourceURL() + "/griddap/" + identifier + ".nc";
		}
		handler.add(new GSProperty<String>("downloadLink", downloadLink));
	    }
	    metadataRecord.setAdditionalInfo(handler);
	    ret.addRecord(metadataRecord);
	}
	return ret;

    }

    @Override
    public List<String> listMetadataFormats() throws GSException {
	List<String> ret = new ArrayList<>();
	ret.add(CommonNameSpaceContext.EMODNET_PHYSICS_NS_URI);
	return ret;
    }

    @Override
    public String getType() {

	return TYPE;
    }

    @Override
    protected EMODNETPhysicsConnectorSetting initSetting() {
	return new EMODNETPhysicsConnectorSetting();
    }

}
