package eu.essi_lab.accessor.trigger;

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

import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.net.downloader.HttpHeaderUtils;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils.MethodWithBody;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.InterpolationType;

/**
 * @author Roberto
 */
public class TRIGGERClient {

    private static final String TRIGGER_CREDENTIALS_MISSING_ERROR = "TRIGGER_CREDENTIALS_MISSING_ERROR";
    private static final String TRIGGER_CREDENTIALS_INVALID_ERROR = "TRIGGER_CREDENTIALS_INVALID_ERROR";

    private String endpoint;
    private String user;
    private String password;

    public static String TRIGGER_BEARER_TOKEN = null;
    public static String TRIGGER_REFRESH_TOKEN = null;

    /**
     * @param endpoint
     */
    public TRIGGERClient() {

    }

    /**
     * @param endpoint
     */
    public TRIGGERClient(String endpoint) {

	this.endpoint = endpoint;
    }

    /**
     * @return the user
     */
    public String getUser() {

	return user;
    }

    /**
     * @param user the user to set
     */
    public void setUser(String user) {
	this.user = user;
    }

    /**
     * @return the password
     */
    public String getPassword() {

	return password;
    }

    /**
     * @param password the password to set
     */
    public void setPassword(String password) {
	this.password = password;
    }

    public String getEndpoint() {
	return endpoint;
    }

    public void setEndpoint(String endpoint) {
	this.endpoint = endpoint;
    }

    /**
     * @return
     * @throws IOException
     * @throws GSException
     */
    public String getToken() throws GSException {

	// HttpRequestExecutor executor = new HttpRequestExecutor();

	// HttpPost httpPost = new HttpPost(getEndpoint() + "auth");

	String user = getUser();
	String password = getPassword();

	String result = null;

	HashMap<String, String> params = new HashMap<String, String>();
	params.put("email", user);
	params.put("password", password);

	try {

	    HttpRequest request = HttpRequestUtils.build(//
		    MethodWithBody.POST, //
		    getEndpoint() + "auth", //
		    params);

	    HttpResponse<InputStream> response = new Downloader().downloadResponse(request);

	    int statusCode = response.statusCode();
	    if (statusCode > 400) {
		// token expired - refresh token
		// refreshBearerToken();
		GSLoggerFactory.getLogger(TRIGGERClient.class).info("ERROR getting TOKEN: " + statusCode);
	    }

	    result = IOUtils.toString(response.body(), "UTF-8");
	    GSLoggerFactory.getLogger(TRIGGERClient.class).info("RESPONSE FROM TRIGGER " + result);
	    // if (result != null && !result.isEmpty()) {
	    // JSONObject obj = new JSONObject(result);
	    // token = obj.optString("accessToken");
	    // TRIGGER_REFRESH_TOKEN = obj.optString("refreshToken");
	    // GSLoggerFactory.getLogger(TRIGGERClient.class).info("TOKEN obtained: " + token);
	    // }

	} catch (Exception e) {
	    e.printStackTrace();
	    GSLoggerFactory.getLogger(TRIGGERClient.class).info("ERROR getting BEARER TOKEN: " + e.getMessage());
	    return null;
	}
	return result;

    }

    public String refreshBearerToken() {
	// https://app.meteotracker.com/auth/refreshtoken
	GSLoggerFactory.getLogger(TRIGGERClient.class).info("Refreshing BEARER TOKEN from MeteoTracker service");
	String token = null;

	HashMap<String, String> params = new HashMap<String, String>();
	params.put("refreshToken", TRIGGER_REFRESH_TOKEN);

	String result = null;
	try {

	    HttpRequest request = HttpRequestUtils.build(//
		    MethodWithBody.POST, //
		    getEndpoint() + "/auth/refreshtoken", //
		    params);

	    HttpResponse<InputStream> response = new Downloader().downloadResponse(request);

	    int statusCode = response.statusCode();
	    if (statusCode > 400) {
		// token expired - refresh token
		getToken();
	    }
	    result = IOUtils.toString(response.body(), "UTF-8");
	    GSLoggerFactory.getLogger(TRIGGERClient.class).info("RESPONSE FROM MeteoTracker " + result);
	    if (result != null && !result.isEmpty()) {
		JSONObject obj = new JSONObject(result);
		token = obj.optString("accessToken");
		TRIGGER_REFRESH_TOKEN = obj.optString("refreshToken");
		GSLoggerFactory.getLogger(TRIGGERClient.class).info("BEARER TOKEN obtained: " + TRIGGER_BEARER_TOKEN);
	    }

	} catch (Exception e) {
	    e.printStackTrace();
	    GSLoggerFactory.getLogger(TRIGGERClient.class).info("ERROR getting BEARER TOKEN: " + e.getMessage());
	}
	return token;
    }

    public JSONArray getData(String stationId, String seriesCode, Date begin, Date end, InterpolationType interpolation) throws Exception {

	// JSONArray ret = new JSONArray();
	// Date tmp = begin;
	//
	// while (tmp.before(end)) {
	//
	// Date tmp2 = new Date(tmp.getTime() + TimeUnit.DAYS.toMillis(14));
	// if (tmp2.after(end)) {
	//
	// tmp2 = end;
	// }
	//
	// List<JSONObject> list = null;
	//
	// list = getData(stationId, tmp, tmp2, interpolation, seriesCode);
	//
	// Date lastInsertedDate = null;
	//
	// for (JSONObject jsonData : list) {
	//
	// String valor = jsonData.get("valor").toString();
	// String fecha = jsonData.get("fecha").toString();
	// String timeZone = jsonData.get("zona_horaria").toString();
	// String units = jsonData.get("unidades").toString();
	//
	// BigDecimal v = null;
	// try {
	// v = new BigDecimal(valor);
	// } catch (Exception e) {
	// }
	//
	// if (timeZone != null && !timeZone.equals("null")) {
	// INVERTED_DATE_TIME_SDF.setTimeZone(TimeZone.getTimeZone(timeZone));
	// }
	// Date date = INVERTED_DATE_TIME_SDF.parse(fecha);
	//
	// //
	// //
	// //
	//
	// DinaguaValue dinaguaValue = new DinaguaValue(v, date);
	// dinaguaValue.setUnits(units);
	// ret.addValue(dinaguaValue);
	// if (lastInsertedDate == null || !lastInsertedDate.equals(date)) {
	// lastInsertedDate = date;
	// ret.addValue(dinaguaValue);
	// }
	// }
	//
	// tmp = tmp2;
	// }

	return null;
    }

    /**
     * @param stationId
     * @param beginDate
     * @param endDate
     * @param variable
     * @return
     * @throws IOException
     * @throws GSException
     */
    private List<JSONObject> getData(String stationId, Date beginDate, Date endDate, InterpolationType interpolation, String seriesCode)
	    throws IOException, GSException {

	// Downloader downloader = getDownloader();

	// String begin = URLEncoder.encode(DATE_TIME_SDF.format(beginDate), "UTF-8");
	// String end = URLEncoder.encode(DATE_TIME_SDF.format(endDate), "UTF-8");
	//
	// String calculo = "";
	// String path = "";
	//
	// switch (interpolation) {
	// case AVERAGE:
	// calculo = "&tipo_calculo=Promedio";
	// path = "diarios";
	// break;
	// case MAX:
	// calculo = "&tipo_calculo=Máximo";
	// path = "diarios";
	// break;
	// case MIN:
	// calculo = "&tipo_calculo=Mínimo";
	// path = "diarios";
	// break;
	// case CONTINUOUS:
	// default:
	// path = "horarios";
	// calculo = "";
	// break;
	// }
	//
	// String response = downloader.downloadString(getEndpoint() + "/service/datos/" + path + "?inicio=" + begin +
	// "&fin=" + end
	// + "&variable=" + seriesCode + "&id_estacion=" + stationId + calculo).get();
	//
	// JSONArray responseArray = new JSONArray(response);
	//
	// ArrayList<JSONObject> out = new ArrayList<>();
	//
	// for (int i = 0; i < responseArray.length(); i++) {
	//
	// out.add(responseArray.getJSONObject(i));
	//
	// }

	return null;
    }

    /**
     * @return
     * @throws IOException
     * @throws GSException
     */
    private String getResponse(String url) throws GSException {

	String token = getToken();

	HashMap<String, String> headers = new HashMap<>();
	headers.put("Authorization", "Bearer " + token);

	Downloader downloader = new Downloader();

	String response = downloader.downloadOptionalString(url, HttpHeaderUtils.build(headers)).get();

	return response;
    }

    protected List<JSONObject> getStations() throws GSException {

	List<JSONObject> list = new ArrayList<JSONObject>();

	String response = getResponse(getEndpoint() + "/service/estaciones");

	JSONArray stationsArray = new JSONArray(response);

	for (int i = 0; i < stationsArray.length(); i++) {

	    JSONObject jsonStation = (JSONObject) stationsArray.get(i);
	    list.add(jsonStation);

	}
	return list;

    }
}
