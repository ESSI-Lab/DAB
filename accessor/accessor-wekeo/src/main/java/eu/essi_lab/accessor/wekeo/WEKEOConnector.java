package eu.essi_lab.accessor.wekeo;

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
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;

import eu.essi_lab.cdk.harvest.HarvestedQueryConnector;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.OriginalMetadata;

public class WEKEOConnector extends HarvestedQueryConnector<WEKEOConnectorSetting> {

    /**
     * 
     */
    public static final String TYPE = "WEKEOConnector";

    private WEKEOClient client = null;

    private String AUTHORIZATION_TOKEN = null;

    private Logger logger = GSLoggerFactory.getLogger(getClass());;

    private List<String> wekeoCollections = null;

    public WEKEOConnector() {
    }

    public WEKEOClient getClient() {
	return client;
    }

    public void setClient(WEKEOClient client) {
	this.client = client;
    }

    @Override
    public boolean supports(GSSource source) {
	String endpoint = source.getEndpoint();
	return endpoint.contains("wekeo-broker.apps.mercator.dpi.wekeo.eu");

    }

    @Override
    public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest request) throws GSException {

	logger.info("Initialization Client");
	initClient();

	// String[] keys = client.getKeys().toArray(new String[] {});
	// Arrays.sort(keys, new Comparator<String>() {
	//
	// @Override
	// public int compare(String k1, String k2) {
	// Integer l1 = k1 == null ? 0 : k1.length();
	// Integer l2 = k2 == null ? 0 : k2.length();
	// if (l1.equals(l2)) {
	// return k1.compareTo(k2);
	// }
	// return l1.compareTo(l2);
	// }
	// });

	int step = 10;
	String token = request.getResumptionToken();
	Integer i = null;
	if (token == null || token.equals("")) {
	    i = 0;
	} else {
	    i = Integer.parseInt(token);
	}

	int end = Math.min(i + step, wekeoCollections.size());
	logger.info("WEKEO Collections SIZE: " + wekeoCollections.size());
	ListRecordsResponse<OriginalMetadata> ret = new ListRecordsResponse<>();

	Optional<Integer> optionalMaxRecords = getSetting().getMaxRecords();
	if (!getSetting().isMaxRecordsUnlimited() && optionalMaxRecords.isPresent()) {
	    Integer maxRecords = optionalMaxRecords.get();
	    if (i > maxRecords) {
		ret.setResumptionToken(null);
		return ret;
	    }
	}

	for (; i < end; i++) {

	    String identifier = wekeoCollections.get(i);

	    String result = client.getMetadataCollection(identifier, AUTHORIZATION_TOKEN);

	    OriginalMetadata metadataRecord = new OriginalMetadata();
	    metadataRecord.setMetadata(result);
	    metadataRecord.setSchemeURI(CommonNameSpaceContext.WEKEO_NS_URI);
	    ret.addRecord(metadataRecord);

	}
	if (i >= wekeoCollections.size() - 1) {
	    ret.setResumptionToken(null);
	    logger.info("TOTAL NUMBER of records added: " + i);
	} else {
	    ret.setResumptionToken("" + i);
	}

	logger.info("PARTIAL NUMBER of records added: " + i);
	return ret;

    }

    private void initClient() throws GSException {
	if (client == null) {
	    client = new WEKEOClient();
	}
	client.setUser(ConfigurationWrapper.getCredentialsSetting().getWekeUser().orElse(null));
	client.setPassword(ConfigurationWrapper.getCredentialsSetting().getWekeoPassword().orElse(null));

	if (AUTHORIZATION_TOKEN == null) {
	    this.AUTHORIZATION_TOKEN = client.getToken();
	}

	if (wekeoCollections == null) {
	    this.wekeoCollections = client.getCollections(AUTHORIZATION_TOKEN);
	}

    }

    @Override
    public List<String> listMetadataFormats() throws GSException {
	List<String> ret = new ArrayList<>();
	ret.add(CommonNameSpaceContext.WEKEO_NS_URI);
	return ret;
    }

    @Override
    public String getType() {

	return TYPE;
    }

    @Override
    protected WEKEOConnectorSetting initSetting() {

	return new WEKEOConnectorSetting();
    }

}
