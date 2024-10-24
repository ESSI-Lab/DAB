package eu.essi_lab.accessor.argo;

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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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

public class ARGOConnector extends HarvestedQueryConnector<ARGOConnectorSetting> {

    /**
     * 
     */
    public static final String TYPE = "ARGOConnector";

    private static final String ARGO_READ_ERROR = "Unable to find stations URL";

    private static final String ARGO_URL_NOT_FOUND_ERROR = "ARGO_URL_NOT_FOUND_ERROR";
    /**
     * ENDPOINTS:
     * platformCode: https://fleetmonitoring.euro-argo.eu/platformCodes
     * metadata basic: e.g. https://fleetmonitoring.euro-argo.eu/floats/basic/6903238
     * metadata full: e.g. https://fleetmonitoring.euro-argo.eu/floats/6903238
     **/

    private static final String PLATFORM_CODES = "platformCodes";

    private static final String METADATA_BASIC = "floats/basic/";

    private static final String METADATA_FULL = "floats/";

    private List<String> platformIDs;

    private int partialNumbers;

    private Downloader downloader;

    private Logger logger = GSLoggerFactory.getLogger(this.getClass());

    public ARGOConnector() {

	this.downloader = new Downloader();

	this.platformIDs = new ArrayList<>();
    }

    @Override
    public boolean supports(GSSource source) {
	String endpoint = source.getEndpoint();
	return endpoint.contains("fleetmonitoring.euro-argo.eu");
    }

    @Override
    public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest request) throws GSException {
	ListRecordsResponse<OriginalMetadata> ret = new ListRecordsResponse<>();
	if (platformIDs.isEmpty()) {
	    platformIDs = getPlatformCodes();
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

	if (start < platformIDs.size() && !maxNumberReached) {
	    int end = start + pageSize;
	    if (end > platformIDs.size()) {
		end = platformIDs.size();
	    }

	    if (!getSetting().isMaxRecordsUnlimited() && mr.isPresent() && end > mr.get()) {
		end = mr.get();
	    }
	    int count = 0;

	    for (int i = start; i < end; i++) {
		String platformID = platformIDs.get(i);
		String om = getFloatOriginalMetadata(platformID);
		if (om != null && !om.isEmpty()) {

		    OriginalMetadata metadata = new OriginalMetadata();
		    metadata.setSchemeURI(CommonNameSpaceContext.ARGO_NS_URI);
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

	    logger.debug("Added Collection records: {} . TOTAL STATION SIZE: {}", partialNumbers, platformIDs.size());
	    partialNumbers = 0;
	    return ret;
	}

	return ret;
    }

    private String getFloatOriginalMetadata(String platformID) throws GSException {
	logger.trace("ARGO Get Float Metadata STARTED");

	String ret;

	String argoUrl = getSourceURL();

	argoUrl = argoUrl.endsWith("/") ? argoUrl + METADATA_FULL + platformID : argoUrl + "/" + METADATA_FULL + platformID;

	logger.trace("ARGO FLOAT METADATA URL: {}", argoUrl);

	Optional<String> response = downloader.downloadOptionalString(argoUrl);

	if (response.isPresent()) {

	    ret = response.get();
	    // String[] splittedString = listResponse.get().replaceAll("\"", "").replaceAll("\\[", "").replaceAll("\\]",
	    // "").split(",");
	    // ret = Arrays.asList(splittedString);

	} else {

	    throw GSException.createException(//
		    this.getClass(), //
		    ARGO_READ_ERROR, //
		    null, //
		    ErrorInfo.ERRORTYPE_SERVICE, //
		    ErrorInfo.SEVERITY_ERROR, //
		    ARGO_URL_NOT_FOUND_ERROR);

	}

	logger.trace("ARGO Get Float Metadata ENDED");

	return ret;
    }

    private List<String> getPlatformCodes() throws GSException {
	logger.trace("ARGO List Data finding STARTED");

	List<String> ret = new ArrayList<>();

	String argoUrl = getSourceURL();

	argoUrl = argoUrl.endsWith("/") ? argoUrl + PLATFORM_CODES : argoUrl + "/" + PLATFORM_CODES;

	logger.trace("ARGO LIST COLLECTION IDENTIFIER URL: {}", argoUrl);

	Optional<String> listResponse = downloader.downloadOptionalString(argoUrl);

	if (listResponse.isPresent()) {

	    String[] splittedString = listResponse.get().replaceAll("\"", "").replaceAll("\\[", "").replaceAll("\\]", "").split(",");
	    ret = Arrays.asList(splittedString);

	} else {

	    throw GSException.createException(//
		    this.getClass(), //
		    ARGO_READ_ERROR, //
		    null, //
		    ErrorInfo.ERRORTYPE_SERVICE, //
		    ErrorInfo.SEVERITY_ERROR, //
		    ARGO_URL_NOT_FOUND_ERROR);

	}

	logger.trace("ARGO List Data finding ENDED");

	return ret;
    }

    @Override
    public List<String> listMetadataFormats() throws GSException {
	List<String> ret = new ArrayList<>();
	ret.add(CommonNameSpaceContext.ARGO_NS_URI);
	return ret;
    }

    @Override
    public String getType() {

	return TYPE;
    }

    @Override
    protected ARGOConnectorSetting initSetting() {

	return new ARGOConnectorSetting();
    }

    public static void main(String[] args) throws Exception {
        String urlPlatform = "https://fleetmonitoring.euro-argo.eu/platformCodes";
        String urlMetadata = "https://fleetmonitoring.euro-argo.eu/floats/";
        Downloader d = new Downloader();
        Optional<String> s = d.downloadOptionalString(urlPlatform);
        System.out.println(s.get());
        String[] splittedString = s.get().replaceAll("\"", "").replaceAll("\\[", "").replaceAll("\\]", "").split(",");
        Optional<InputStream> inputStream = d.downloadOptionalStream(urlPlatform);
        List<String> lines = Arrays.asList(splittedString);
    
        System.out.println(lines.size());
    
        urlMetadata += lines.get(0);
    
        Optional<String> res = d.downloadOptionalString(urlMetadata);
    
        JSONObject object = new JSONObject(res.get());
    
        System.out.println(object.keys().hasNext());
    }
}
