package eu.essi_lab.accessor.hiscentral.friuli;

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

import java.io.InputStream;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.cdk.harvest.HarvestedQueryConnector;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.net.downloader.HttpHeaderUtils;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils.MethodWithBody;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.IOStreamUtils;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * @author Roberto
 */
public class HISCentralFriuliConnector extends HarvestedQueryConnector<HISCentralFriuliConnectorSetting> {

    /**
     *
     */
    static final String TYPE = "HISCentralFriuliConnector";

    /**
     *
     */
    public HISCentralFriuliConnector() {

    }

    /**
     *
     */

    public static final String LOGIN_URL = "login";

    static final String STATIONS_URL = "stations?group[]=fvg";

    static final String IDRO_STATIONS_URL = "stations?group[]=idro_fvg";

    /**
     *
     */
    static final String SENSOR_URL = "measures?station_id=";

    public static final String DEFAULT_BASE_URL = "https://api.meteo.fvg.it/api/ws/";

    private static final String HIS_CENTRAL_FRIULI_CONNECTOR_DOWNLOAD_ERROR = "HIS_CENTRAL_FRIULI_CONNECTOR_DOWNLOAD_ERROR";

    private int countDataset = 0;

    /**
     *
     */

    private int maxRecords;

    public static String BEARER_TOKEN = null;
    public static String USER = null;
    public static String PASSWORD = null;

    @Override
    public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest request) throws GSException {

	ListRecordsResponse<OriginalMetadata> ret = new ListRecordsResponse<>();

	int page = 0;

	if (request.getResumptionToken() != null) {

	    page = Integer.valueOf(request.getResumptionToken());
	}

	// add authorization token
	String baseUrl = getSourceURL();
	if (BEARER_TOKEN == null) {
	    BEARER_TOKEN = getBearerToken(baseUrl);
	}

	JSONObject stationsObject = getStationsList(page);

	if (stationsObject != null) {

	    JSONArray array = stationsObject.optJSONArray("data");
	    // JSONArray array = new JSONArray(IOStreamUtils.asUTF8String(stream.clone()));
	    if (array == null) {
		GSLoggerFactory.getLogger(getClass()).info("ERROR getting items.");
		BEARER_TOKEN = null;
	    } else {
		maxRecords = array.length();
		getSetting().getMaxRecords().ifPresent(v -> maxRecords = v);
		for (int i = 0; i < maxRecords; i++) {

		    JSONObject datasetMetadata = array.getJSONObject(i);
		    String id = datasetMetadata.optString("id");
		    if (id != null && !id.isEmpty()) {
			JSONObject measuresObject = getMeasures(id);
			if (measuresObject != null) {
			    JSONArray measuresArray = measuresObject.optJSONArray("data");
			    if (measuresArray != null) {
				for (int j = 0; j < measuresArray.length(); j++) {
				    JSONObject sensorInfo = measuresArray.getJSONObject(j);
				    ret.addRecord(HISCentralFriuliMapper.create(datasetMetadata, sensorInfo));
				    countDataset++;
				}

			    }
			}

		    }
		}
	    }

	}
	// page = page + 1;
	// if (page > 1) {
	// ret.setResumptionToken(null);
	// // GSLoggerFactory.getLogger(getClass()).info("Dataset with time interval: {}", countTimeDataset);
	// GSLoggerFactory.getLogger(getClass()).info("Total number of dataset: {}", countDataset);
	// } else {
	// ret.setResumptionToken(String.valueOf(page));
	// }
	// GSLoggerFactory.getLogger(getClass()).debug("ADDED {} records for station", countDataset);
	return ret;
    }

    private JSONObject getMeasures(String id) throws GSException {

	String url = getSourceURL() + SENSOR_URL + id;
	GSLoggerFactory.getLogger(getClass()).info("Getting " + url);

	// HttpGet get = new HttpGet(url.trim());
	// get.addHeader("Authorization", "Bearer " + BEARER_TOKEN);

	int timeout = 120;
	int responseTimeout = 200;
	InputStream stream = null;
	try {

	    // RequestConfig config = RequestConfig.custom().setConnectTimeout(timeout * 1000)
	    // .setConnectionRequestTimeout(responseTimeout * 1000).setSocketTimeout(timeout * 1000).build();
	    // CloseableHttpClient client = HttpClientBuilder.create().setDefaultRequestConfig(config).build();
	    // CloseableHttpResponse getStationResponse = client.execute(get);
	    // stream = getStationResponse.getEntity().getContent();
	    //
	    GSLoggerFactory.getLogger(getClass()).info("Got " + url);

	    Downloader downloader = new Downloader();
	    downloader.setConnectionTimeout(TimeUnit.MILLISECONDS, timeout * 1000);
	    downloader.setResponseTimeout(TimeUnit.MILLISECONDS, responseTimeout * 1000);

	    HttpResponse<InputStream> getStationResponse = downloader.downloadResponse(//
		    url, //
		    HttpHeaderUtils.build("Authorization", "Bearer " + BEARER_TOKEN));

	    stream = getStationResponse.body();

	    if (stream != null) {
		JSONObject obj = new JSONObject(IOStreamUtils.asUTF8String(stream));
		stream.close();
		return obj;
	    }

	} catch (Exception e) {
	    GSLoggerFactory.getLogger(getClass()).error("Unable to retrieve " + url);
	    BEARER_TOKEN = null;
	    throw GSException.createException(//
		    getClass(), //
		    "Unable to retrieve " + url + " after several tries", //
		    null, //
		    ErrorInfo.ERRORTYPE_SERVICE, //
		    ErrorInfo.SEVERITY_ERROR, //
		    HIS_CENTRAL_FRIULI_CONNECTOR_DOWNLOAD_ERROR);
	}

	return null;
    }

    private JSONObject getStationsList(int page) throws GSException {
	String url = getSourceURL();
	if (page == 0) {
	    url = url + STATIONS_URL;
	} else {
	    url = url + IDRO_STATIONS_URL;
	}
	GSLoggerFactory.getLogger(getClass()).info("Getting " + url);

	// HttpGet get = new HttpGet(url.trim());
	// get.addHeader("Authorization", "Bearer " + BEARER_TOKEN);

	int timeout = 120;
	int responseTimeout = 200;
	InputStream stream = null;
	try {

	    // RequestConfig config = RequestConfig.custom().setConnectTimeout(timeout * 1000)
	    // .setConnectionRequestTimeout(responseTimeout * 1000).setSocketTimeout(timeout * 1000).build();
	    // CloseableHttpClient client = HttpClientBuilder.create().setDefaultRequestConfig(config).build();
	    // CloseableHttpResponse getStationResponse = client.execute(get);
	    // stream = getStationResponse.getEntity().getContent();
	    // GSLoggerFactory.getLogger(getClass()).info("Got " + url);

	    Downloader downloader = new Downloader();
	    downloader.setConnectionTimeout(TimeUnit.MILLISECONDS, timeout * 1000);
	    downloader.setResponseTimeout(TimeUnit.MILLISECONDS, responseTimeout * 1000);

	    HttpResponse<InputStream> getStationResponse = downloader.downloadResponse(//
		    url, //
		    HttpHeaderUtils.build("Authorization", "Bearer " + BEARER_TOKEN));

	    stream = getStationResponse.body();

	    if (stream != null) {
		JSONObject obj = new JSONObject(IOStreamUtils.asUTF8String(stream));
		stream.close();
		return obj;
	    }

	} catch (Exception e) {
	    GSLoggerFactory.getLogger(getClass()).error("Unable to retrieve " + url);
	    BEARER_TOKEN = null;
	    throw GSException.createException(//
		    getClass(), //
		    "Unable to retrieve " + url + " after several tries", //
		    null, //
		    ErrorInfo.ERRORTYPE_SERVICE, //
		    ErrorInfo.SEVERITY_ERROR, //
		    HIS_CENTRAL_FRIULI_CONNECTOR_DOWNLOAD_ERROR);
	}
	return null;
    }

    public static String getBearerToken(String baseUrl) {

	GSLoggerFactory.getLogger(HISCentralFriuliConnector.class).info("Getting BEARER TOKEN from FVG Omnia service");

	// StringEntity input = null;
	String result = null;
	String token = null;

	try {
	    // HttpPost httpPost = new HttpPost(baseUrl + LOGIN_URL);
	    // HttpClient httpClient = HttpClientBuilder.create().build();
	    //
	    // List<NameValuePair> params = new ArrayList<NameValuePair>(2);
	    // params.add(new BasicNameValuePair("email", CLIENT_ID));
	    // params.add(new BasicNameValuePair("password", CLIENT_SECRET));
	    // httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

	    HashMap<String, String> params = new HashMap<String, String>();
	    String user = USER != null ? USER : ConfigurationWrapper.getCredentialsSetting().getFriuliClientId().orElse(null);
	    params.put("email", user);
	    String password = PASSWORD != null
		    ? PASSWORD
		    : ConfigurationWrapper.getCredentialsSetting().getFriuliClientPassword().orElse(null);
	    params.put("password", password);

	    HttpRequest request = HttpRequestUtils.build(//
		    MethodWithBody.POST, //
		    baseUrl + LOGIN_URL, //
		    params);

	    HttpResponse<InputStream> response = new Downloader().downloadResponse(request);

	    response = new Downloader().downloadResponse(request);

	    result = IOUtils.toString(response.body(), "UTF-8");
	    if (result != null && !result.isEmpty()) {

		token = result.startsWith("\"") ? result.substring(1, result.length() - 1) : result;
		GSLoggerFactory.getLogger(HISCentralFriuliConnector.class).info("BEARER TOKEN obtained: " + token);
	    }

	} catch (Exception e) {
	    e.printStackTrace();
	    GSLoggerFactory.getLogger(HISCentralFriuliConnector.class).info("ERROR getting BEARER TOKEN: " + e.getMessage());
	    return null;
	}
	return token;
    }

    @Override
    public List<String> listMetadataFormats() throws GSException {
	List<String> ret = new ArrayList<>();
	ret.add(CommonNameSpaceContext.HISCENTRAL_FRIULI_NS_URI);
	return ret;
    }

    @Override
    public String getType() {

	return TYPE;
    }

    @Override
    public boolean supports(GSSource source) {
	String endpoint = source.getEndpoint();
	return endpoint.contains("fvg.it");
    }

    @Override
    protected HISCentralFriuliConnectorSetting initSetting() {

	return new HISCentralFriuliConnectorSetting();
    }
}
