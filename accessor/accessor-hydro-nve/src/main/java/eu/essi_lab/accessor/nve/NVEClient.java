package eu.essi_lab.accessor.nve;

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
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.net.http.HttpResponse;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;

import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils.MethodNoBody;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;

public class NVEClient {

    private static final String NVE_CLIENT_GET_STATIONS_ERROR = "NVE_CLIENT_GET_STATIONS_ERROR";
    private static final String NVE_CLIENT_GET_OBSERVATIONS_ERROR = "NVE_CLIENT_GET_OBSERVATIONS_ERROR";

    private String endpoint = "https://hydapi.nve.no/api/v1";

    public String getEndpoint() {
	return endpoint;
    }

    public void setEndpoint(String endpoint) {
	this.endpoint = endpoint;
    }

    private Logger logger;
    private String authorizationKey;

    public void setAuthorizationKey(String authorizationKey) {
	this.authorizationKey = authorizationKey;
    }

    public NVEClient() {
	this.logger = GSLoggerFactory.getLogger(getClass());
    }

    public Map<String, NVEStation> getStations() throws GSException {
	return getStations(null);
    }

    public Map<String, NVEStation> getStations(String stationId) throws GSException {

	logger.info("Serving get stations request");

	try {
	    InputStream output = downloadStations(stationId);

	    ByteArrayOutputStream baos = new ByteArrayOutputStream();

	    IOUtils.copy(output, baos);
	    output.close();
	    baos.close();

	    logger.info("Stations downloaded.");

	    String str = new String(baos.toByteArray());

	    JSONObject obj = new JSONObject(str);

	    Map<String, NVEStation> ret = new HashMap<>();

	    JSONObject result = (JSONObject) obj;
	    JSONArray dataArray = result.getJSONArray("data");
	    for (int i = 0; i < dataArray.length(); i++) {
		JSONObject stationObject = dataArray.getJSONObject(i);
		NVEStation station = new NVEStation(stationObject);
		String myId = station.getId();
		// if (myId.equals("105.1.0")) {
		ret.put(myId, station);
		// }
	    }

	    return ret;
	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e);

	    throw GSException.createException(//
		    getClass(), //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    NVE_CLIENT_GET_STATIONS_ERROR, //
		    e//
	    );
	}

    }

    public NVEObservations getObservations(String stationId, String parameter, String resolutionTime, Date begin, Date end)
	    throws GSException {

	logger.info("Serving get observations request");

	String str = "";
	try {
	    InputStream output = downloadObservations(stationId, parameter, resolutionTime, begin, end);

	    ByteArrayOutputStream baos = new ByteArrayOutputStream();

	    IOUtils.copy(output, baos);
	    output.close();
	    baos.close();

	    logger.info("Observations downloaded.");

	    str = new String(baos.toByteArray());

	    JSONObject result = new JSONObject(str);

	    JSONArray dataArray = result.getJSONArray("data");
	    JSONObject observationsObject = dataArray.getJSONObject(0);
	    return new NVEObservations(observationsObject);

	} catch (GSException gse) {
	    throw gse;
	} catch (Exception e) {
	    GSLoggerFactory.getLogger(getClass()).error(e);

	    throw GSException.createException(//
		    getClass(), //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    NVE_CLIENT_GET_OBSERVATIONS_ERROR, //
		    e//
	    );
	}

    }

    public InputStream downloadStations() throws IOException, GSException {
	return downloadStations(null);
    }

    public InputStream downloadStations(String stationId) throws IOException, GSException {

	String parameter = "";
	if (stationId != null) {
	    parameter = "StationId=" + stationId + "&Active=All";
	} else {
	    parameter = "Active=All";
	}

	String url = endpoint + "/Stations?" + parameter;

	return retrieveStream(url);
    }

    private String getAuthorizationKey() {
	return authorizationKey;
    }

    public InputStream downloadObservations(String stationId, String parameter, String resolutionTime, Date begin, Date end)
	    throws IOException, GSException {

	String referenceTime = ISO8601DateTimeUtils.getISO8601DateTime(begin) + "/" + ISO8601DateTimeUtils.getISO8601DateTime(end);

	referenceTime = URLEncoder.encode(referenceTime, "UTF-8");

	String url = endpoint + "/Observations?StationId=" + stationId + "&Parameter=" + parameter + "&ResolutionTime=" + resolutionTime
		+ "&ReferenceTime=" + referenceTime;

	return retrieveStream(url);
    }

    private InputStream retrieveStream(String url) throws IOException, GSException {
	String statusInfo;
	String errorMessage = "";

	int maxRetries = 10;
	int statusCode = 0;
	String serviceMessage = "";
	for (int i = 0; i < maxRetries; i++) {

	    try {

		HashMap<String, String> headers = new HashMap<String, String>();
		headers.put("accept", "application/json");
		headers.put("X-API-Key", getAuthorizationKey());

		logger.info("Downloading...");
		HttpResponse<InputStream> response = new Downloader()
			.downloadResponse(HttpRequestUtils.build(MethodNoBody.GET, url, headers));

		statusCode = response.statusCode();
		statusInfo = "Status code: " + statusCode;
		logger.info(statusInfo);

		if (statusCode == 200) {
		    return response.body();
		} else {
		    InputStream stream = response.body();
		    ByteArrayOutputStream baos = new ByteArrayOutputStream();
		    IOUtils.copy(stream, baos);
		    stream.close();
		    baos.close();
		    serviceMessage = new String(baos.toByteArray());
		    errorMessage = statusInfo + serviceMessage;

		    if (statusCode == 400) {
			break;
		    }
		    try {
			long ms = (long) (1000 + Math.random() * (i + 2) * 1000);
			GSLoggerFactory.getLogger(getClass()).info("Sleeping " + ms + " ms before retrying");
			Thread.sleep(ms);
		    } catch (InterruptedException e) {
			e.printStackTrace();
		    }
		}
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	}
	if (statusCode == 400) {
	    throw new IOException("Data provider service error (NVE): " + serviceMessage);
	}
	throw new IOException("Irreparable error downloading from NVE. " + errorMessage);
    }

}
