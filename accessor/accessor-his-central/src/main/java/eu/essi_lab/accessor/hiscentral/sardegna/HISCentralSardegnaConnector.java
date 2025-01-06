package eu.essi_lab.accessor.hiscentral.sardegna;

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

import java.io.InputStream;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.cdk.harvest.HarvestedQueryConnector;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.net.downloader.HttpHeaderUtils;
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
public class HISCentralSardegnaConnector extends HarvestedQueryConnector<HISCentralSardegnaConnectorSetting> {

    /**
     * 
     */
    static final String TYPE = "HISCentralSardegnaConnector";

    /**
     * 
     */
    public HISCentralSardegnaConnector() {

    }

    /**
     * 
     */

    static final String STATIONS_URL = "getStations";
    // getLITStations- livelli idrometrici station
    // getTCIStations - temperature

    static final String ORGANIZATION_URL = "organization";

    static final String METADATI = "getMetadati";
    /**
     * 
     */
    static final String SENSOR_URL = "elements?";

    public static final String BASE_URL = "https://eu-central-1.aws.data.mongodb-api.com/app/hiscentral-dqluv/endpoint/";

    private static final String HIS_CENTRAL_SARDEGNA_CONNECTOR_DOWNLOAD_ERROR = "HIS_CENTRAL_SARDEGNA_CONNECTOR_DOWNLOAD_ERROR";

    /**
     * 
     */

    private int maxRecords;

    public static String API_KEY = null;

    @Override
    public ListRecordsResponse<OriginalMetadata> listRecords(ListRecordsRequest request) throws GSException {

	ListRecordsResponse<OriginalMetadata> ret = new ListRecordsResponse<>();

	String page = "1";

	if (request.getResumptionToken() != null) {

	    page = request.getResumptionToken();
	}

	// add authorization token
	String baseUrl = getSourceURL();
	if (API_KEY == null) {
	    API_KEY = ConfigurationWrapper.getCredentialsSetting().getSardegnaApiKey().orElse(null);
	}

	JSONArray stations = getStationsList();

	if (stations != null) {

	    JSONArray metadataInfoArr = getMetadataInfo();
	    JSONObject metadataInfo = null;

	    Map<String, HISCentralSardegnaVariable> variableMap = new HashMap<String, HISCentralSardegnaVariable>();

	    if (metadataInfoArr != null && metadataInfoArr.length() > 0) {
		metadataInfo = metadataInfoArr.getJSONObject(0);
		JSONArray dataArray = metadataInfo.optJSONArray("Data");
		if (dataArray != null) {
		    for (int j = 0; j < dataArray.length(); j++) {
			JSONObject varObj = dataArray.optJSONObject(j);
			if (varObj != null) {

			    HISCentralSardegnaVariable var = new HISCentralSardegnaVariable(varObj);
			    variableMap.put(var.getId(), var);
			}
		    }
		}
	    }

	    maxRecords = stations.length();
	    getSetting().getMaxRecords().ifPresent(v -> maxRecords = v);
	    for (int i = 0; i < maxRecords; i++) {

		JSONObject datasetMetadata = stations.getJSONObject(i);
		// "TCI_TERMO": "SI",
		// "P1H_PLUVIO": "SI",
		// "LIT_IDRO": "NO"

		String temperature = datasetMetadata.optString("TCI_TERMO");
		String rain = datasetMetadata.optString("P1H_PLUVIO");
		String level = datasetMetadata.optString("LIT_IDRO");

		List<HISCentralSardegnaVariable> varList = new ArrayList<HISCentralSardegnaVariable>();

		if (temperature != null && !temperature.isEmpty()) {
		    if (temperature.toLowerCase().contains("si") || temperature.toLowerCase().contains("yes")) {
			varList.add(variableMap.get("TCI"));
		    }

		}
		if (rain != null && !rain.isEmpty()) {
		    if (rain.toLowerCase().contains("si") || rain.toLowerCase().contains("yes")) {
			varList.add(variableMap.get("P1H"));
		    }

		}
		if (level != null && !level.isEmpty()) {
		    if (level.toLowerCase().contains("si") || level.toLowerCase().contains("yes")) {
			varList.add(variableMap.get("LIT"));
		    }

		}

		for (HISCentralSardegnaVariable var : varList) {
		    ret.addRecord(HISCentralSardegnaMapper.create(datasetMetadata, var, metadataInfo));
		}

	    }

	} else {
	    GSLoggerFactory.getLogger(getClass()).info("ERROR getting items.");
	    API_KEY = null;
	}

	return ret;
    }

    private JSONArray getMetadataInfo() throws GSException {
	// e.g. https://eu-central-1.aws.data.mongodb-api.com/app/hiscentral-dqluv/endpoint/getMetadati
	String url = getSourceURL() + METADATI;

	GSLoggerFactory.getLogger(getClass()).info("Getting " + url);

	int timeout = 120;
	int responseTimeout = 200;
	InputStream stream = null;
	try {

	    Downloader downloader = new Downloader();
	    downloader.setConnectionTimeout(TimeUnit.SECONDS, timeout);
	    downloader.setResponseTimeout(TimeUnit.SECONDS, responseTimeout);

	    HttpResponse<InputStream> getStationResponse = downloader.downloadResponse(//
		    url.trim(), //
		    HttpHeaderUtils.build("api-key", API_KEY));

	    stream = getStationResponse.body();
	    GSLoggerFactory.getLogger(getClass()).info("Got " + url);

	    if (stream != null) {
		JSONArray jsonResult = new JSONArray(IOStreamUtils.asUTF8String(stream));
		stream.close();
		return jsonResult;
	    }

	} catch (Exception e) {
	    GSLoggerFactory.getLogger(getClass()).error("Unable to retrieve " + url);
	    API_KEY = null;
	    throw GSException.createException(//
		    getClass(), //
		    "Unable to retrieve " + url + " after several tries", //
		    null, //
		    ErrorInfo.ERRORTYPE_SERVICE, //
		    ErrorInfo.SEVERITY_ERROR, //
		    HIS_CENTRAL_SARDEGNA_CONNECTOR_DOWNLOAD_ERROR);
	}
	return null;
    }

    private JSONArray getStationsList() throws GSException {
	// https://eu-central-1.aws.data.mongodb-api.com/app/hiscentral-dqluv/endpoint/getStations
	String url = getSourceURL() + STATIONS_URL;

	GSLoggerFactory.getLogger(getClass()).info("Getting " + url);

	int timeout = 120;
	int responseTimeout = 200;
	InputStream stream = null;
	try {

	    Downloader downloader = new Downloader();
	    downloader.setConnectionTimeout(TimeUnit.SECONDS, timeout);
	    downloader.setResponseTimeout(TimeUnit.SECONDS, responseTimeout);

	    HttpResponse<InputStream> getStationResponse = downloader.downloadResponse(//
		    url.trim(), //
		    HttpHeaderUtils.build("api-key", API_KEY));

	    stream = getStationResponse.body();

	    GSLoggerFactory.getLogger(getClass()).info("Got " + url);

	    if (stream != null) {
		JSONArray jsonResult = new JSONArray(IOStreamUtils.asUTF8String(stream));
		stream.close();
		return jsonResult;
	    }

	} catch (Exception e) {
	    GSLoggerFactory.getLogger(getClass()).error("Unable to retrieve " + url);
	    API_KEY = null;
	    throw GSException.createException(//
		    getClass(), //
		    "Unable to retrieve " + url + " after several tries", //
		    null, //
		    ErrorInfo.ERRORTYPE_SERVICE, //
		    ErrorInfo.SEVERITY_ERROR, //
		    HIS_CENTRAL_SARDEGNA_CONNECTOR_DOWNLOAD_ERROR);
	}
	return null;
    }

    @Override
    public List<String> listMetadataFormats() throws GSException {
	List<String> ret = new ArrayList<>();
	ret.add(CommonNameSpaceContext.HISCENTRAL_SARDEGNA_NS_URI);
	return ret;
    }

    @Override
    public String getType() {

	return TYPE;
    }

    @Override
    public boolean supports(GSSource source) {
	String endpoint = source.getEndpoint();
	return endpoint.contains("eu-central-1.aws.data.mongodb-api.com");
    }

    @Override
    protected HISCentralSardegnaConnectorSetting initSetting() {

	return new HISCentralSardegnaConnectorSetting();
    }

    public static void main(String[] args) throws Exception {

	String url = "https://eu-central-1.aws.data.mongodb-api.com/app/hiscentral-dqluv/endpoint/getStations";

	int timeout = 120;
	int responseTimeout = 200;
	InputStream stream = null;

	Downloader downloader = new Downloader();
	downloader.setConnectionTimeout(TimeUnit.SECONDS, timeout);
	downloader.setResponseTimeout(TimeUnit.SECONDS, responseTimeout);

	HttpResponse<InputStream> getStationResponse = downloader.downloadResponse(//
		url.trim(), //
		HttpHeaderUtils.build("api-key", "7uBFiUsEayn5fCV4creG8CLJVCygTidGejpyH08CcENPyoyD2wH2BFdnluRHZ2og"));

	stream = getStationResponse.body();

	if (stream != null) {
	    JSONArray jsonResult = new JSONArray(IOStreamUtils.asUTF8String(stream));
	    stream.close();
	}

    }

    @Override
    public String getSourceURL() {

	String url = super.getSourceURL();
	if (!url.endsWith("/")) {
	    url += "/";
	}

	return url;

    }

}
