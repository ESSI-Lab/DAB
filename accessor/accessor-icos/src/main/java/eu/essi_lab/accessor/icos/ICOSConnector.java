package eu.essi_lab.accessor.icos;

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

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URLEncoder;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;

import eu.essi_lab.cdk.harvest.HarvestedQueryConnector;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils.MethodWithBody;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.JSONUtils;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSProperty;
import eu.essi_lab.model.GSPropertyHandler;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.OriginalMetadata;

public class ICOSConnector extends HarvestedQueryConnector<ICOSConnectorSetting> {

    /**
     * 
     */
    public static final String TYPE = "ICOSConnector";

    private static final String ICOS_READ_ERROR = "Unable to find stations URL";

    private static final String ICOS_URL_NOT_FOUND_ERROR = "ICOS_URL_NOT_FOUND_ERROR";

    private static final String ICOS_CLIENT_ERROR = "ICOS_CLIENT_ERROR";
    private static final String ICOS_TIME_OUT_ERROR = "ICOS_TIME_OUT_ERROR";
    /**
     * ENDPOINTS:
     * platformCode: https://fleetmonitoring.euro-argo.eu/platformCodes
     * metadata basic: e.g. https://fleetmonitoring.euro-argo.eu/floats/basic/6903238
     * metadata full: e.g. https://fleetmonitoring.euro-argo.eu/floats/6903238
     **/

    private static final String PLATFORM_CODES = "platformCodes";

    private static final String METADATA_BASIC = "floats/basic/";

    private static final String METADATA_FULL = "floats/";

    private List<String> productsIDs;

    private int partialNumbers;

    private Downloader downloader;

    private Logger logger = GSLoggerFactory.getLogger(this.getClass());

    private static final String ICOS_CONNECTOR_PAGESIZE_OPTION_KEY = "ICOS_CONNECTOR_PAGESIZE_OPTION_KEY";

    public ICOSConnector() {

	this.downloader = new Downloader();

	this.productsIDs = new ArrayList<>();
    }

    @Override
    public boolean supports(GSSource source) {
	String endpoint = source.getEndpoint();
	return endpoint.contains("meta.icos-cp.eu");
    }

    public InputStream sparqlRequest(boolean isForGEOSS) {

	if (isForGEOSS) {
	    return ICOSConnector.class.getClassLoader().getResourceAsStream("icos/GEOSSsparqlPostRequest");
	} else {
	    return ICOSConnector.class.getClassLoader().getResourceAsStream("icos/sparqlPostRequest");
	}
    }

    @Override
    public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest request) throws GSException {
	ListRecordsResponse<OriginalMetadata> ret = new ListRecordsResponse<>();

	Boolean isICOSInGEOSS = isUseICOSInGEOSS();

	if (productsIDs.isEmpty()) {
	    productsIDs = getProductsIDs(isICOSInGEOSS);
	}

	int step = 10;
	String token = request.getResumptionToken();
	Integer i = null;
	if (token == null || token.equals("")) {
	    i = 0;
	} else {
	    i = Integer.parseInt(token);
	}

	int end = Math.min(i + step, productsIDs.size());
	logger.info("ICOS Collections SIZE: " + productsIDs.size());

	Optional<Integer> optionalMaxRecords = getSetting().getMaxRecords();
	if (!getSetting().isMaxRecordsUnlimited() && optionalMaxRecords.isPresent()) {
	    Integer maxRecords = optionalMaxRecords.get();
	    if (i > maxRecords) {
		ret.setResumptionToken(null);
		return ret;
	    }
	}

	for (; i < end; i++) {

	    String platformID = productsIDs.get(i);
	    String om = getOriginalMetadata(platformID);
	    if (om != null && !om.isEmpty()) {

		OriginalMetadata metadata = new OriginalMetadata();
		metadata.setSchemeURI(CommonNameSpaceContext.ICOS_NS_URI);
		metadata.setMetadata(om);
		GSPropertyHandler handler = GSPropertyHandler.of(new GSProperty<Boolean>("isForGEOSS", isICOSInGEOSS));
		metadata.setAdditionalInfo(handler);
		ret.addRecord(metadata);
		partialNumbers++;
	    }

	}

	if (i >= productsIDs.size() - 1) {
	    ret.setResumptionToken(null);
	    logger.info("TOTAL NUMBER of records added: " + i);
	} else {
	    ret.setResumptionToken("" + i);
	}

	logger.info("PARTIAL NUMBER of records added: " + i);
	return ret;

    }

    private String getOriginalMetadata(String icosUrl) throws GSException {
	logger.trace("ICOS Get Original Metadata STARTED");

	String ret;

	logger.trace("ICOS METADATA URL: {}", icosUrl);

	Optional<String> response = downloader.downloadOptionalString(icosUrl);

	if (response.isPresent()) {

	    ret = response.get();
	    // String[] splittedString = listResponse.get().replaceAll("\"", "").replaceAll("\\[", "").replaceAll("\\]",
	    // "").split(",");
	    // ret = Arrays.asList(splittedString);

	} else {

	    throw GSException.createException(//
		    this.getClass(), //
		    ICOS_READ_ERROR, //
		    null, //
		    ErrorInfo.ERRORTYPE_SERVICE, //
		    ErrorInfo.SEVERITY_ERROR, //
		    ICOS_URL_NOT_FOUND_ERROR);

	}

	logger.trace("ICOS Get Original Metadata ENDED");

	return ret;
    }

    private List<String> getProductsIDs(boolean isICOSInGEOSS) throws GSException {
	logger.trace("ICOS List Data finding STARTED");

	List<String> ret = new ArrayList<>();

	String icosUrl = getSourceURL();

	// icosUrl = icosUrl.endsWith("/") ? icosUrl + PLATFORM_CODES : icosUrl + "/" + PLATFORM_CODES;

	InputStream postRequest = sparqlRequest(isICOSInGEOSS);

	// HttpPost post = new HttpPost(icosUrl);

	ByteArrayOutputStream baos = new ByteArrayOutputStream();

	try {
	    IOUtils.copy(postRequest, baos);

	    // ByteArrayEntity inputEntity = new ByteArrayEntity(baos.toByteArray());
	    // inputEntity.setChunked(false);
	    // post.setEntity(inputEntity);
	    //
	    logger.info("Sending SPARQL Request to: " + icosUrl);

	    Downloader executor = new Downloader();
	    executor.setConnectionTimeout(TimeUnit.SECONDS, 10);

	    HttpResponse<InputStream> response = executor
		    .downloadResponse(HttpRequestUtils.build(MethodWithBody.POST, icosUrl, baos.toByteArray()));

	    InputStream output = response.body();

	    JSONObject jsonObject = JSONUtils.fromStream(output);

	    JSONObject res = jsonObject.optJSONObject("results");

	    JSONArray jsonArray = getJSONArray(res, "bindings");
	    logger.info("ICOS number of Products: " + jsonArray.length());
	    if (jsonArray.length() < 1) {
		logger.error("NO RESULTS FOUND!!!");
		return ret;
	    }

	    ret = extractProductsList(jsonArray);

	    logger.trace("ICOS List Data finding ENDED");

	    return ret;

	} catch (Exception e) {
	    e.printStackTrace();
	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_CLIENT, //
		    ErrorInfo.SEVERITY_ERROR, //
		    ICOS_CLIENT_ERROR, //
		    e);
	}

    }

    private List<String> extractProductsList(JSONArray jsonArray) throws Exception {
	List<String> ret = new ArrayList<>();
	for (int i = 0; i < jsonArray.length(); i++) {
	    String fileString = null;
	    String objString = null;
	    JSONObject obj = jsonArray.optJSONObject(i);
	    if (obj != null) {
		JSONObject fileName = obj.optJSONObject("fileName");
		JSONObject fileObj = obj.optJSONObject("dobj");
		if (fileObj != null && fileName != null) {
		    fileString = fileName.optString("value");
		    objString = fileObj.optString("value");
		}
	    }
	    if (fileString != null && objString != null) {
		String enc = URLEncoder.encode(fileString, "UTF-8");
		enc = enc.replace("+", "%20");
		String identifier = objString.endsWith("/") ? objString + enc + ".json" : objString + "/" + enc + ".json";
		ret.add(identifier);
	    }
	    // System.out.println(identifier);
	}
	return ret;
    }

    @Override
    public List<String> listMetadataFormats() throws GSException {
	List<String> ret = new ArrayList<>();
	ret.add(CommonNameSpaceContext.ICOS_NS_URI);
	return ret;
    }

    public JSONArray getJSONArray(JSONObject result, String key) {
	try {
	    boolean hasKey = result.has(key);
	    if (!hasKey) {
		return new JSONArray();
	    }
	    JSONArray ret = result.getJSONArray(key);
	    if (ret == null || ret.length() == 0) {
		ret = new JSONArray();
	    }
	    return ret;
	} catch (Exception e) {
	    // logger.warn("Error getting json array", e);
	    return new JSONArray();
	}

    }

    public static void main(String[] args) throws Exception {
	String urlPlatform = "https://meta.icos-cp.eu/sparql";

	InputStream postRequest = ICOSConnector.class.getClassLoader().getResourceAsStream("icos/sparqlPostRequest");

	ByteArrayOutputStream baos = new ByteArrayOutputStream();

	IOUtils.copy(postRequest, baos);

	System.out.println("Sending SPARQL Request to: " + urlPlatform);

	Downloader executor = new Downloader();
	executor.setConnectionTimeout(TimeUnit.SECONDS, 10);
	HttpResponse<InputStream> response = executor
		.downloadResponse(HttpRequestUtils.build(MethodWithBody.POST, urlPlatform, baos.toByteArray()));

	InputStream output = response.body();

	JSONObject jsonObject = JSONUtils.fromStream(output);

	JSONObject res = jsonObject.optJSONObject("results");

	// JSONArray jsonArray = getJSONArray(res, "bindings");

	// System.out.println(jsonArray.length());

	System.out.println("TEST DONE");
    }

    @Override
    public String getType() {

	return TYPE;
    }

    @Override
    protected ICOSConnectorSetting initSetting() {

	return new ICOSConnectorSetting();
    }

    /**
     * @return
     */
    public boolean isUseICOSInGEOSS() {

	return getSetting().isUseICOSInGEOSS();
    }

    /**
     * @return
     */
    public void setUseICOSInGEOSS(Boolean value) {

	getSetting().setUseICOSInGEOSS(value);
    }

}
