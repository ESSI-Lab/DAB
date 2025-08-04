package eu.essi_lab.accessor.hiscentral.liguria;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;

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

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;

import eu.essi_lab.cdk.harvest.HarvestedQueryConnector;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.net.downloader.HttpHeaderUtils;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils.MethodWithBody;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.IOStreamUtils;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.lib.utils.JSONArrayStreamParser;
import eu.essi_lab.lib.utils.JSONArrayStreamParserListener;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.OriginalMetadata;

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
     * Anagrafica delle stazioni: https://aws.arpal.liguria.it/siapi/Service/Query/HIS_Anagrafica
     * Descrizione variabili: https://aws.arpal.liguria.it/siapi/Service/Query/HIS_Descrizione
     * Dati da stazione:
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
	boolean maxNumberReached = false;
	if (!getSetting().isMaxRecordsUnlimited() && mr.isPresent() && start > mr.get() - 1) {
	    // max record set
	    maxNumberReached = true;
	}

	if (BEARER_TOKEN == null) {
	    BEARER_TOKEN = getBearerToken();
	}

	String url = getSourceURL().endsWith("/") ? getSourceURL() + SENSORS_URL : getSourceURL() + "/" + SENSORS_URL;

	String descriptionVariableURL = getSourceURL().endsWith("/") ? getSourceURL() + VAR_DESCRIPTION
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
	    } else {
		ret.setResumptionToken(String.valueOf(end));
	    }

	    // JSONArray description_Response = getOriginalMetadata(descriptionVariableURL);//
	    // downloader.downloadOptionalString(descriptionVariableURL);

	    // JSONObject datasetMetadata = object.getJSONObject("dataset-metadata");

	    for (int j = start; j < end; j++) {

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

		    File tempFile = null;
		    try {

			tempFile = File.createTempFile(getClass().getSimpleName(), ".json");
			tempFile.deleteOnExit();
			try (InputStream is = getData(dataUrl, code, initialDate, date);
				OutputStream fileOut = new FileOutputStream(tempFile)) {
			    is.transferTo(fileOut);
			}
		    } catch (IOException e) {
			logger.error("Failed to download or write HTTP response: " + e.getMessage());
			e.printStackTrace();
			continue;

		    }
		    // if (streamResp != null) {
		    try (InputStream cachedStream = new FileInputStream(tempFile)) {

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
				    if (index == 0) {
					startTime = object.optString("DTRF");
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
				    e.printStackTrace();
				    logger.debug("Error at index:" + index);
				}

			    }

			    @Override
			    public void finished() {
				map.put(code, vars2);
			    }
			});

			Set<String> toAdd = map.get(code);
			for (String s : toAdd) {
			    partialNumbers++;
			    ret.addRecord(HISCentralLiguriaMapper.create(s, startTime, dataUrl, sensorInfo, stationsParameter));
			}
			
			// Now it is safe to delete the file
			if (tempFile != null && tempFile.exists()) {
			    boolean deleted = tempFile.delete();
			    if (!deleted) {
			        logger.debug("Could not delete temp file: " + tempFile.getAbsolutePath());
			    }
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

		    } catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		    }
		}

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

	    String postRequest = "{\"parametri\": [{ \"alias\": \"code\", \"value\":\"" + stationCode
		    + "\"},{ \"alias\": \"dtrf_beg\", \"value\":\"" + startTime + "\"},{ \"alias\": \"dtrf_end\", \"value\":\"" + endTime
		    + "\"}]}";
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
