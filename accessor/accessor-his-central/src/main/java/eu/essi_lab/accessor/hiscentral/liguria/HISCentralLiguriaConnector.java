package eu.essi_lab.accessor.hiscentral.liguria;

import eu.essi_lab.cdk.harvest.*;
import eu.essi_lab.cfga.gs.*;
import eu.essi_lab.jaxb.common.*;
import eu.essi_lab.lib.net.downloader.*;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils.*;
import eu.essi_lab.lib.utils.*;
import eu.essi_lab.messages.listrecords.*;
import eu.essi_lab.model.*;
import eu.essi_lab.model.exceptions.*;
import eu.essi_lab.model.resource.*;
import org.json.*;
import org.slf4j.*;

import java.io.*;
import java.net.http.*;
import java.text.*;
import java.util.*;
import java.util.concurrent.*;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Technologies and Environmental Intelligence (ITIAm)/ESSI-Lab
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

/**
 * @author Fabrizio
 */
public class HISCentralLiguriaConnector extends HarvestedQueryConnector<HISCentralLiguriaConnectorSetting> {

    /**
     *
     */
    static final String TYPE = "HISCentralLiguriaConnector";

    private Downloader downloader;

    /**
     *
     */
    public HISCentralLiguriaConnector() {
	downloader = new Downloader();
    }

    /**
     *
     */
    static final String SENSORS_URL = "HIS_Anagrafica";

    static final String DATI_URL = "HIS_Dati";

    static final String VAR_DESCRIPTION = "HIS_Descrizione";

    public static final String BASE_URL = "https://aws.arpal.liguria.it/siapi/Service/Query/";

    public static final String TOKEN_URL = "https://aws.arpal.liguria.it/siapi/Authentication/Login";

    public static String BEARER_TOKEN = null;

    public static String REFRESH_BEARER_TOKEN = null;

    private static final String HIS_CENTRAL_LIGURIA_CONNECTOR_DOWNLOAD_ERROR = "HIS_CENTRAL_LIGURIA_CONNECTOR_DOWNLOAD_ERROR";

    private int maxRecords;

    private JSONArray allStation;

    private JSONArray stationsParameter;

    private static final int STEP = 10;

    private Logger logger = GSLoggerFactory.getLogger(this.getClass());

    private int partialNumbers;

    private Map<String, Set<String>> map = new HashMap<String, Set<String>>();

    private int index = 0;
    String startTime = null;

    /**
     * Anagrafica delle stazioni: https://aws.arpal.liguria.it/siapi/Service/Query/HIS_Anagrafica Descrizione variabili:
     * https://aws.arpal.liguria.it/siapi/Service/Query/HIS_Descrizione Dati da stazione:
     * https://aws.arpal.liguria.it/siapi/Service/Query/HIS_Dati?dtrf_beg=202301010000&dtrf_end=202301010100&code=CFUNZ
     **/

    @Override
    public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest request) throws GSException {

	ListRecordsResponse<OriginalMetadata> ret = new ListRecordsResponse<>();

	int start = 0;

	if (request.getResumptionToken() != null) {

	    start = Integer.valueOf(request.getResumptionToken());
	}

	Optional<Integer> mr = getSetting().getMaxRecords();
	boolean unlimited = getSetting().isMaxRecordsUnlimited();
	// maxRecords limits harvested metadata records (partialNumbers), not station index
	boolean maxNumberReached = !unlimited && mr.isPresent() && partialNumbers >= mr.get();

	if (BEARER_TOKEN == null) {
	    BEARER_TOKEN = getBearerToken();
	}

	String url = getSourceURL().endsWith("/") ? getSourceURL() + SENSORS_URL : getSourceURL() + "/" + SENSORS_URL;

	String descriptionVariableURL = getSourceURL().endsWith("/")
		? getSourceURL() + VAR_DESCRIPTION
		: getSourceURL() + "/" + VAR_DESCRIPTION;

	if (allStation == null) {
	    allStation = getOriginalMetadata(url);
	}

	if (stationsParameter == null) {
	    stationsParameter = getOriginalMetadata(descriptionVariableURL);
	}

	JSONArray response = allStation;

	if (start < response.length() && !maxNumberReached) {

	    int end = start + STEP;
	    if (end > response.length()) {
		end = response.length();
	    }

	    boolean stoppedByMax = false;

	    // JSONArray description_Response = getOriginalMetadata(descriptionVariableURL);//
	    // downloader.downloadOptionalString(descriptionVariableURL);

	    // JSONObject datasetMetadata = object.getJSONObject("dataset-metadata");

	    for (int j = start; j < end; j++) {

		if (!unlimited && mr.isPresent() && partialNumbers >= mr.get()) {
		    stoppedByMax = true;
		    break;
		}

		JSONObject sensorInfo = response.getJSONObject(j);

		String code = sensorInfo.optString("CODE");

		if (code != null && !code.isEmpty()) {
		    Date d = new Date();
		    String date = HISCentralLiguriaMapper.getDate(d);
		    String initialDate = "197001010000";

		    String dataUrl = getSourceURL().endsWith("/") ? getSourceURL() + DATI_URL // + "?dtrf_beg=" +
			    // initialDate +
			    // "&dtrf_end=" + date +
			    // "&code=" + code
			    : getSourceURL() + "/" + DATI_URL;// + "?dtrf_beg=" + initialDate + "&dtrf_end=" + date +
		    // "&code=" + code;

		    // InputStream streamResp = getData(dataUrl, code, initialDate, date);//
		    // downloader.downloadOptionalString(dataUrl);
		    List<String> vars = new ArrayList<String>();

		    try {

			Optional<File> json = Downloader.getOrRefreshCachedFile(dataUrl, () -> {

			    try {
				return getData(dataUrl, code, initialDate, date);
			    } catch (GSException e) {
				GSLoggerFactory.getLogger(HISCentralLiguriaConnector.class).error(e);
				return null;
			    }
			}, TimeUnit.DAYS.toMillis(7), "json");

			if (json.isPresent()) {

			    try (InputStream cachedStream = new FileInputStream(json.get())) {

				JSONArrayStreamParser parser = new JSONArrayStreamParser();
				startTime = null;

				// JSONObject varObject = parser.parseFirstObject(streamResp);

				// JSONObject varObject = new JSONObject(tmpJSON);
				// JSONObject varObject = dataResp.optJSONObject(0);
				// startTime = varObject.optString("DTRF");

				Set<String> vars2 = new HashSet<String>();
				index = 0;

				parser.parse(cachedStream, new JSONArrayStreamParserListener() {
				    @Override
				    public void notifyJSONObject(JSONObject object) {

					try {
					    String dtrf = object.optString("DTRF");
					    if (dtrf != null && !dtrf.isEmpty()) {
						// Response rows are not ordered; use earliest DTRF (lexical order matches time for YYYYMMDDHHmm).
						if (startTime == null || dtrf.compareTo(startTime) < 0) {
						    startTime = dtrf;
						}
					    }
					    index++;
					    Iterator<String> iterator = object.keys();
					    while (iterator.hasNext()) {
						String s = iterator.next();
						if (s.contains("CODE") || s.contains("DTRF")) {
						    continue;
						}
						String valueString = object.optString(s);
						if (valueString != null && !valueString.isEmpty()) {
						    vars2.add(s);
						}
					    }
					} catch (Exception e) {
					    GSLoggerFactory.getLogger(HISCentralLiguriaConnector.class).error(e);
					    logger.debug("Error at index:" + index);
					}

				    }

				    @Override
				    public void finished() {
					map.put(code, vars2);
				    }

				    @Override
				    public void notifyJSONArray(JSONArray object) {
					// TODO Auto-generated method stub

				    }
				});

				Set<String> toAdd = map.get(code);
				if (toAdd != null) {
				    for (String s : toAdd) {
					if (!unlimited && mr.isPresent() && partialNumbers >= mr.get()) {
					    stoppedByMax = true;
					    break;
					}
					partialNumbers++;
					ret.addRecord(HISCentralLiguriaMapper.create(s, startTime, dataUrl, sensorInfo, stationsParameter));
				    }
				}

				if (stoppedByMax) {
				    break;
				}
				// while (iterator.hasNext()) {
				// String s = iterator.next();
				// if (s.contains("CODE") || s.contains("DTRF")) {
				// continue;
				// }
				// String valueString = varObject.optString(s);
				// if (valueString != null && !valueString.isEmpty()) {
				// vars.add(s);
				// }
				// partialNumbers++;
				// ret.addRecord(HISCentralLiguriaMapper.create(s, startTime, dataUrl, sensorInfo,
				// stationsParameter));
				// }

			    } catch (Exception ex) {

				GSLoggerFactory.getLogger(HISCentralLiguriaConnector.class).error(ex);
			    }
			}

		    } catch (Exception e) {

			throw GSException.createException(getClass(), "HISCentralLiguriaConnectorDownloadError", e);
		    }
		}
	    }

	    if (stoppedByMax) {
		ret.setResumptionToken(null);
	    } else if (end < response.length()) {
		ret.setResumptionToken(String.valueOf(end));
	    }

	    logger.debug("ADDED {} records for ARPAL Liguria {}", partialNumbers);
	    if (ret.getResumptionToken() == null) {
		BEARER_TOKEN = null;
		REFRESH_BEARER_TOKEN = null;
		logger.debug("Added Collection records: {} . TOTAL STATION SIZE: {}", partialNumbers, response.length());
		partialNumbers = 0;
		index = 0;
	    }

	} else {
	    ret.setResumptionToken(null);
	    BEARER_TOKEN = null;
	    REFRESH_BEARER_TOKEN = null;
	    logger.debug("Added Collection records: {} . TOTAL STATION SIZE: {}", partialNumbers, response.length());
	    partialNumbers = 0;
	    index = 0;
	    return ret;
	}

	return ret;

    }

    public static InputStream getData(String dataUrl, String stationCode, String startTime, String endTime) throws GSException {
	InputStream stream = null;

	if (BEARER_TOKEN == null) {
	    BEARER_TOKEN = getBearerToken();
	}

	// {
	// "parametri": [
	// {
	// "alias": "code",
	// "value": "CALAM"
	// },
	// {
	// "alias": "dtrf_beg",
	// "value": "201504150900"
	// },
	// {
	// "alias": "dtrf_end",
	// "value": "201505150900"
	// }
	// ]
	// }

	try {

	    String postRequest =
		    "{\"parametri\": [{ \"alias\": \"code\", \"value\":\"" + stationCode + "\"},{ \"alias\": \"dtrf_beg\", \"value\":\""
			    + startTime + "\"},{ \"alias\": \"dtrf_end\", \"value\":\"" + endTime + "\"}]}";
	    HashMap<String, String> map = new HashMap<String, String>();
	    map.put("accept", "text/plain");
	    map.put("Content-Type", "application/json");
	    map.put("Authorization", "Bearer " + BEARER_TOKEN);

	    HttpRequest request = HttpRequestUtils.build(MethodWithBody.POST, dataUrl, postRequest, HttpHeaderUtils.build(map));

	    GSLoggerFactory.getLogger(HISCentralLiguriaConnector.class).debug("POST REQUEST: " + postRequest);

	    Downloader d = new Downloader();

	    HttpResponse<InputStream> response = d.downloadResponse(request);
	    int statusCode = response.statusCode();
	    if (statusCode > 400) {
		// refresh token and try again
		BEARER_TOKEN = getBearerToken();
		map = new HashMap<String, String>();
		map.put("accept", "text/plain");
		map.put("Content-Type", "application/json");
		map.put("Authorization", "Bearer " + BEARER_TOKEN);
		request = HttpRequestUtils.build(MethodWithBody.POST, dataUrl, postRequest, HttpHeaderUtils.build(map));
		response = d.downloadResponse(request);
	    }

	    stream = response.body();

	    GSLoggerFactory.getLogger(HISCentralLiguriaConnector.class).info("Got data from station:" + stationCode);

	    if (stream != null) {
		return stream;
	    }

	} catch (Exception e) {
	    GSLoggerFactory.getLogger(HISCentralLiguriaConnector.class).error("Unable to retrieve data from station " + stationCode);
	    BEARER_TOKEN = null;
	    throw GSException.createException(//
		    HISCentralLiguriaConnector.class, //
		    "Unable to get data from station with code " + stationCode + " after several tries", //
		    null, //
		    ErrorInfo.ERRORTYPE_SERVICE, //
		    ErrorInfo.SEVERITY_ERROR, //
		    HIS_CENTRAL_LIGURIA_CONNECTOR_DOWNLOAD_ERROR);
	}
	return null;
    }

    private JSONArray getOriginalMetadata(String url) throws GSException {
	logger.info("Getting " + url);
	int timeout = 120;
	int responseTimeout = 200;
	InputStream stream = null;
	downloader.setConnectionTimeout(TimeUnit.MILLISECONDS, timeout * 1000);
	downloader.setResponseTimeout(TimeUnit.MILLISECONDS, responseTimeout * 1000);
	try {

	    String postRequest = "{\"alias\": \"string\", \"value\": \"string\"}";

	    HashMap<String, String> map = new HashMap<String, String>();
	    map.put("accept", "text/plain");
	    map.put("Content-Type", "application/json");
	    map.put("Authorization", "Bearer " + BEARER_TOKEN);

	    HttpRequest request = HttpRequestUtils.build(MethodWithBody.POST, url, postRequest, HttpHeaderUtils.build(map));

	    logger.debug("POST REQUEST: " + postRequest);

	    HttpResponse<InputStream> response = downloader.downloadResponse(request);
	    int statusCode = response.statusCode();
	    if (statusCode > 400) {
		// refresh token and try again
		BEARER_TOKEN = getBearerToken();
		map = new HashMap<String, String>();
		map.put("accept", "text/plain");
		map.put("Content-Type", "application/json");
		map.put("Authorization", "Bearer " + BEARER_TOKEN);
		request = HttpRequestUtils.build(MethodWithBody.POST, url, postRequest, HttpHeaderUtils.build(map));
		response = downloader.downloadResponse(request);
	    }

	    // HashMap<String, String> params = new HashMap<String, String>();
	    // params.put("alias", "string");
	    // params.put("value", "string");
	    // params.put("Content-Type", "text/xml;charset=UTF-8");
	    // HttpResponse<InputStream> response =
	    // downloader.downloadResponse(HttpRequestUtils.build(MethodWithBody.POST, url, params,
	    // HttpHeaderUtils.build("Authorization", "Bearer " + BEARER_TOKEN)));

	    stream = response.body();

	    logger.info("Got " + url);

	    if (stream != null) {
		JSONArray result = new JSONArray(IOStreamUtils.asUTF8String(stream));
		// JSONObject jsonResult = new JSONObject(IOStreamUtils.asUTF8String(stream));
		stream.close();
		return result;
	    }

	} catch (Exception e) {
	    logger.error("Unable to retrieve " + url);
	    BEARER_TOKEN = null;
	    throw GSException.createException(//
		    getClass(), //
		    "Unable to retrieve " + url + " after several tries", //
		    null, //
		    ErrorInfo.ERRORTYPE_SERVICE, //
		    ErrorInfo.SEVERITY_ERROR, //
		    HIS_CENTRAL_LIGURIA_CONNECTOR_DOWNLOAD_ERROR);
	}
	return null;
    }

    public static String getBearerToken() {
	GSLoggerFactory.getLogger(HISCentralLiguriaConnector.class).info("Getting BEARER TOKEN from ARPAL Liguria service");
	String token = null;
	try {

	    HashMap<String, String> params = new HashMap<String, String>();
	    params.put("apiKey", ConfigurationWrapper.getCredentialsSetting().getLiguriaApiKey().orElse(null));
	    params.put("password", ConfigurationWrapper.getCredentialsSetting().getLiguriaClientPassword().orElse(null));

	    String postRequest = "{\"apiKey\": \"" + ConfigurationWrapper.getCredentialsSetting().getLiguriaApiKey().orElse(null)
		    + "\", \"password\": \"" + ConfigurationWrapper.getCredentialsSetting().getLiguriaClientPassword().orElse(null) + "\"}";

	    HashMap<String, String> map = new HashMap<String, String>();
	    map.put("accept", "text/plain");
	    map.put("Content-Type", "application/json");

	    HttpRequest request = HttpRequestUtils.build(MethodWithBody.POST, TOKEN_URL, postRequest, HttpHeaderUtils.build(map));

	    GSLoggerFactory.getLogger(HISCentralLiguriaConnector.class).debug("POST REQUEST: " + postRequest);

	    Downloader down = new Downloader();
	    HttpResponse<InputStream> response = down.downloadResponse(request);

	    JSONObject result = new JSONObject(IOStreamUtils.asUTF8String(response.body()));

	    if (result != null) {
		token = result.optString("accessToken");
		REFRESH_BEARER_TOKEN = result.optString("refresh_token");
		GSLoggerFactory.getLogger(HISCentralLiguriaConnector.class).info("BEARER TOKEN obtained: " + BEARER_TOKEN);
		GSLoggerFactory.getLogger(HISCentralLiguriaConnector.class).info("BEARER TOKEN obtained: " + REFRESH_BEARER_TOKEN);
	    }

	} catch (Exception e) {
	    e.printStackTrace();
	    GSLoggerFactory.getLogger(HISCentralLiguriaConnector.class).info("ERROR getting BEARER TOKEN: " + e.getMessage());
	    return null;
	}

	return token;
    }

    @Override
    public List<String> listMetadataFormats() throws GSException {
	List<String> ret = new ArrayList<>();
	ret.add(CommonNameSpaceContext.HISCENTRAL_LIGURIA_NS_URI);
	return ret;
    }

    @Override
    public String getType() {

	return TYPE;
    }

    @Override
    public boolean supports(GSSource source) {
	String endpoint = source.getEndpoint();
	return endpoint.contains("arpal.liguria.it");
    }

    @Override
    protected HISCentralLiguriaConnectorSetting initSetting() {

	return new HISCentralLiguriaConnectorSetting();
    }

    public static void main(String[] args) throws ParseException {

	float a = 4388137;
	float c = 784759;
	float b = 100000;
	double d = 4388137;
	double d1 = 784759;
	double div = 100000;
	double res = d / div;
	double res1 = d1 / div;
	float result = a / b;
	float result2 = c / b;

	System.out.println(result);
	System.out.println(result2);
	System.out.println(res);
	System.out.println(res1);

	Date date = new Date();
	Date dateBefore = new Date(date.getTime() - 30 * 24 * 3600 * 1000l); // Subtract n days
	String isotime = ISO8601DateTimeUtils.getISO8601Date(dateBefore);

	System.out.println(isotime.replace("-", "") + "0000");

	Optional<Date> notStandard = ISO8601DateTimeUtils.parseNotStandard2ToDate("190001010000");

	if (notStandard.isPresent()) {
	    Date dat = notStandard.get();
	}

    }

}
