package eu.essi_lab.accessor.bluecloud;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
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

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;

import eu.essi_lab.cdk.harvest.HarvestedQueryConnector;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.OriginalMetadata;

public class BLUECLOUDConnector extends HarvestedQueryConnector<BLUECLOUDConnectorSetting> {

    /**
     * 
     */
    public static final String TYPE = "BLUECLOUDConnector";

    private static final String BLUECLOUD_READ_ERROR = "Unable to find stations URL";

    private static final String BLUECLOUD_URL_NOT_FOUND_ERROR = "BLUECLOUD_URL_NOT_FOUND_ERROR";
    /**
     * ENDPOINTS:
     * collections: https://data.blue-cloud.org/api/collections
     **/

    private static final String PLATFORM_CODES = "platformCodes";

    private static final String METADATA_BASIC = "floats/basic/";

    private static final String METADATA_FULL = "floats/";

    private List<String> collectionURLs;

    private int partialNumbers;

    private Downloader downloader;

    private Logger logger = GSLoggerFactory.getLogger(this.getClass());

    public BLUECLOUDConnector() {

	this.downloader = new Downloader();

	this.collectionURLs = new ArrayList<>();
    }

    @Override
    public boolean supports(GSSource source) {
	String endpoint = source.getEndpoint();
	return endpoint.contains("data.blue-cloud.org");
    }

    @Override
    public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest request) throws GSException {
	ListRecordsResponse<OriginalMetadata> ret = new ListRecordsResponse<>();
	if (collectionURLs.isEmpty()) {
	    collectionURLs = getCollectionCodes();
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
	    // max record set
	    maxNumberReached = true;
	}

	if (start < collectionURLs.size() && !maxNumberReached) {
	    int end = start + pageSize;
	    if (end > collectionURLs.size()) {
		end = collectionURLs.size();
	    }

	    if (!getSetting().isMaxRecordsUnlimited() && mr.isPresent() && end > mr.get()) {
		end = mr.get();
	    }
	    int count = 0;

	    for (int i = start; i < end; i++) {
		String collectionURL = collectionURLs.get(i);
		String om = getOriginalMetadata(collectionURL);
		if (om != null && !om.isEmpty()) {

		    OriginalMetadata metadata = new OriginalMetadata();
		    metadata.setSchemeURI(CommonNameSpaceContext.BLUECLOUD_API);
		    metadata.setMetadata(om);
		    ret.addRecord(metadata);
		    partialNumbers++;
		    count++;
		}

	    }
	    ret.setResumptionToken(String.valueOf(start + count));
	    logger.debug("ADDED {} records. Number of analyzed floats: {}", partialNumbers, String.valueOf(start + count));

	} else {
	    ret.setResumptionToken(null);

	    logger.debug("Added Collection records: {} . TOTAL STATION SIZE: {}", partialNumbers, collectionURLs.size());
	    partialNumbers = 0;
	    return ret;
	}

	return ret;
    }

    private String getOriginalMetadata(String collectionUrl) throws GSException {
	logger.trace("BLUECLOUD Collection Metadata STARTED");

	String ret;

	Optional<String> response = downloader.downloadOptionalString(collectionUrl);

	if (response.isPresent()) {

	    ret = response.get();
	    // String[] splittedString = listResponse.get().replaceAll("\"", "").replaceAll("\\[", "").replaceAll("\\]",
	    // "").split(",");
	    // ret = Arrays.asList(splittedString);

	} else {

	    throw GSException.createException(//
		    this.getClass(), //
		    BLUECLOUD_READ_ERROR, //
		    null, //
		    ErrorInfo.ERRORTYPE_SERVICE, //
		    ErrorInfo.SEVERITY_ERROR, //
		    BLUECLOUD_URL_NOT_FOUND_ERROR);

	}

	logger.trace("BLUECLOUD Collection Metadata ENDED");

	return ret;
    }

    private List<String> getCollectionCodes() throws GSException {
	logger.trace("BLUECLOUD List Data finding STARTED");

	List<String> ret = new ArrayList<>();

	String blueCloudUrl = getSourceURL();

	logger.trace("BLUECLOUD LIST COLLECTION IDENTIFIER URL: {}", blueCloudUrl);

	Optional<String> listResponse = downloader.downloadOptionalString(blueCloudUrl);

	if (listResponse.isPresent()) {

	    JSONObject jsonResponse = new JSONObject(listResponse.get());
	    JSONArray array = jsonResponse.optJSONArray("urls");
	    if (array != null) {
		int len = array.length();
		for (int i = 0; i < len; i++) {

		    ret.add(array.get(i).toString());
		}

	    }

	} else {

	    throw GSException.createException(//
		    this.getClass(), //
		    BLUECLOUD_READ_ERROR, //
		    null, //
		    ErrorInfo.ERRORTYPE_SERVICE, //
		    ErrorInfo.SEVERITY_ERROR, //
		    BLUECLOUD_URL_NOT_FOUND_ERROR);

	}

	logger.trace("BLUECLOUD List Data finding ENDED");

	return ret;
    }

    @Override
    public List<String> listMetadataFormats() throws GSException {
	List<String> ret = new ArrayList<>();
	ret.add(CommonNameSpaceContext.BLUECLOUD_API);
	return ret;
    }

    @Override
    public String getType() {

	return TYPE;
    }

    @Override
    protected BLUECLOUDConnectorSetting initSetting() {

	return new BLUECLOUDConnectorSetting();
    }

    public static void main(String[] args) throws Exception {
	String urlPlatform = "https://data.blue-cloud.org/api/collections";
	Downloader d = new Downloader();
	Optional<String> s = d.downloadOptionalString(urlPlatform);
	JSONObject jsonResponse = new JSONObject(s.get());
	JSONArray array = jsonResponse.optJSONArray("urls");
	List<String> list = new ArrayList<String>();
	if (array != null) {
	    int len = array.length();
	    for (int i = 0; i < len; i++) {

		list.add(array.get(i).toString());
	    }

	}
	System.out.println(list.size());
    }
}
