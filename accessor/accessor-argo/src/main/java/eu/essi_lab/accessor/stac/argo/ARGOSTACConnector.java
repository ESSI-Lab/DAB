package eu.essi_lab.accessor.stac.argo;

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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
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

public class ARGOSTACConnector extends HarvestedQueryConnector<ARGOSTACConnectorSetting> {

    /**
     * 
     */
    public static final String TYPE = "ARGOSTACConnector";

    private static final String ARGO_STAC_READ_ERROR = "Unable to find stations URL";

    private static final String ARGO_STAC_URL_NOT_FOUND_ERROR = "ARGO_STAC_URL_NOT_FOUND_ERROR";
    /**
     * ENDPOINTS:
     * platformCode: https://stac-pg-api.ifremer.fr/collections/argo_platforms/items?limit=20
     **/

    private static final String PLATFORM_CODES = "argo_platforms";

    private static final String ARRGO_PROFILES = "argo_profiles";

    private static final String METADATA_FULL = "floats/";

    private List<String> platformIDs;

    private int partialNumbers;

    private Downloader downloader;

    private Logger logger = GSLoggerFactory.getLogger(this.getClass());

    public ARGOSTACConnector() {

	this.downloader = new Downloader();

	this.platformIDs = new ArrayList<>();
    }

    @Override
    public boolean supports(GSSource source) {
	String endpoint = source.getEndpoint();
	return endpoint.contains("stac-pg-api.ifremer.fr");
    }

    @Override
    public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest request) throws GSException {
	ListRecordsResponse<OriginalMetadata> ret = new ListRecordsResponse<>();

	String token = request.getResumptionToken();
	// int start = 0;

	int pageSize = getSetting().getPageSize();

	JSONObject platformObject = getPlatformList(token, pageSize);

	String nextToken = null;
	if (platformObject != null) {

	    JSONArray platformList = platformObject.optJSONArray("features");

	    JSONArray linkList = platformObject.optJSONArray("links");
	    for (int k = 0; k < linkList.length(); k++) {
		JSONObject linkObj = linkList.getJSONObject(k);
		String rel = linkObj.optString("rel");
		if (rel != null && rel.toLowerCase().equals("next")) {
		    nextToken = linkObj.optString("href");
		    break;
		}
	    }
	    Optional<Integer> mr = getSetting().getMaxRecords();
	    boolean maxNumberReached = false;
	    if (!getSetting().isMaxRecordsUnlimited() && mr.isPresent()) {
		// max record set
		maxNumberReached = true;
	    }

	    if (!maxNumberReached) {
		int count = 0;
		for (int i = 0; i < platformList.length(); i++) {
		    JSONObject platformObj = platformList.getJSONObject(i);
		    if (platformObj != null && !platformObj.isEmpty()) {

			OriginalMetadata metadata = new OriginalMetadata();
			metadata.setSchemeURI(CommonNameSpaceContext.ARGO_STAC_NS_URI);
			metadata.setMetadata(platformObj.toString());
			ret.addRecord(metadata);
			partialNumbers++;
			count++;
		    }

		}
		ret.setResumptionToken(nextToken);
		logger.debug("ADDED {} records. Number of analyzed floats: {}", count, partialNumbers);

	    } else {
		ret.setResumptionToken(null);

		logger.debug("TOTAL STATION SIZE: {}", partialNumbers);
		partialNumbers = 0;
		return ret;
	    }
	}

	return ret;
    }

    private JSONObject getPlatformList(String token, int pageSize) {

	JSONObject ret = null;
	if (token == null) {
	    String argoUrl = getSourceURL();
	    token = argoUrl.endsWith("/") ? argoUrl + PLATFORM_CODES + "/items?limit=" + pageSize
		    : argoUrl + "/" + PLATFORM_CODES + "/items?limit=" + pageSize;
	}

	logger.trace("ARGO FLOAT METADATA URL: {}", token);

	Optional<String> response = downloader.downloadOptionalString(token);

	if (response.isPresent()) {

	    ret = new JSONObject(response.get());

	}
	
	logger.trace("ARGO Get Float Metadata ENDED");

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
		    ARGO_STAC_READ_ERROR, //
		    null, //
		    ErrorInfo.ERRORTYPE_SERVICE, //
		    ErrorInfo.SEVERITY_ERROR, //
		    ARGO_STAC_URL_NOT_FOUND_ERROR);

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
		    ARGO_STAC_READ_ERROR, //
		    null, //
		    ErrorInfo.ERRORTYPE_SERVICE, //
		    ErrorInfo.SEVERITY_ERROR, //
		    ARGO_STAC_URL_NOT_FOUND_ERROR);

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
    protected ARGOSTACConnectorSetting initSetting() {

	return new ARGOSTACConnectorSetting();
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
