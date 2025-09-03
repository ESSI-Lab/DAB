package eu.essi_lab.accessor.dinaguaws.client;

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

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.net.downloader.HttpHeaderUtils;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils.MethodWithBody;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.IOStreamUtils;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.InterpolationType;

/**
 * @author Fabrizio
 * @author boldrini
 */
public class JSONDinaguaClient extends DinaguaClient {

    private static final String DINAGUA_CREDENTIALS_MISSING_ERROR = "DINAGUA_CREDENTIALS_MISSING_ERROR";
    private static final String DINAGUA_CREDENTIALS_INVALID_ERROR = "DINAGUA_CREDENTIALS_INVALID_ERROR";

    /**
     * @param endpoint
     */
    public JSONDinaguaClient(String endpoint) {

	super(endpoint);
    }

    /**
     * @return
     * @throws IOException
     * @throws GSException
     */
    public String getToken() throws Exception {

	Optional<String> user = getUser();
	Optional<String> password = getPassword();

	if (!user.isPresent()) {

	    user = ConfigurationWrapper.getCredentialsSetting().getDinaguaUser();
	}

	if (!password.isPresent()) {

	    password = ConfigurationWrapper.getCredentialsSetting().getDinaguaPassword();
	}

	if (!user.isPresent() || !password.isPresent()) {

	    throw GSException.createException(//
		    getClass(), //
		    "Dinagua user and/or password missing", //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    DINAGUA_CREDENTIALS_MISSING_ERROR);
	}

	String body = "{\"user\":\"" + user.get() + "\",\"password\":\"" + password.get() + "\"}";

	HttpRequest postRequest = HttpRequestUtils.build(MethodWithBody.POST, getEndpoint() + "/gettoken", body);

	Downloader executor = new Downloader();

	HttpResponse<InputStream> response = executor.downloadResponse(postRequest);

	InputStream content = response.body();

	String token = IOStreamUtils.asUTF8String(content);

	if (token != null && token.toLowerCase().contains("forbidden")) {

	    throw GSException.createException(//
		    getClass(), //
		    "Invalid credentials, token retrieval forbidden", //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    DINAGUA_CREDENTIALS_INVALID_ERROR);
	}

	return token;
    }

    @Override
    public DinaguaData getData(String stationId, String seriesCode, Date begin, Date end, InterpolationType interpolation)
	    throws Exception {

	DinaguaData ret = new DinaguaData();
	Date tmp = begin;

	while (tmp.before(end)) {

	    Date tmp2 = new Date(tmp.getTime() + TimeUnit.DAYS.toMillis(14));
	    if (tmp2.after(end)) {

		tmp2 = end;
	    }

	    List<JSONObject> list = null;

	    list = getData(stationId, tmp, tmp2, interpolation, seriesCode);

	    Date lastInsertedDate = null;

	    for (JSONObject jsonData : list) {

		String valor = jsonData.get("valor").toString();
		String fecha = jsonData.get("fecha").toString();
		String timeZone = jsonData.get("zona_horaria").toString();
		String units = jsonData.get("unidades").toString();

		BigDecimal v = null;
		try {
		    v = new BigDecimal(valor);
		} catch (Exception e) {
		}

		if (timeZone != null && !timeZone.equals("null")) {
		    INVERTED_DATE_TIME_SDF.setTimeZone(TimeZone.getTimeZone(timeZone));
		}
		Date date = INVERTED_DATE_TIME_SDF.parse(fecha);

		//
		//
		//

		DinaguaValue dinaguaValue = new DinaguaValue(v, date);
		dinaguaValue.setUnits(units);
		ret.addValue(dinaguaValue);
		if (lastInsertedDate == null || !lastInsertedDate.equals(date)) {
		    lastInsertedDate = date;
		    ret.addValue(dinaguaValue);
		}
	    }

	    tmp = tmp2;
	}

	return ret;
    }

    @Override
    public DinaguaData getStatusData(String stationId, String temporalidad, Date begin, Date end) throws Exception {

	DinaguaData ret = new DinaguaData();

	String dataURL = getEndpoint() + "/estadohidro/datoscat?serietemporal=Estaciones&stationid=" + stationId + "&temporalidad="
		+ temporalidad + "&monthStart=" + getMonth(begin) + "&monthEnd=" + getMonth(end);

	GSLoggerFactory.getLogger(getClass()).info("Accessing URL: {}", dataURL);

	Optional<HttpResponse<InputStream>> response = downloadResponse(dataURL);

	if (response.isPresent()) {
	    String res = IOUtils.toString(response.get().body(), StandardCharsets.UTF_8);
	    if (res.contains("No se encontraron datos para la serie temporal proporcionada")) {
		// nothing returned
		GSLoggerFactory.getLogger(getClass()).info(res);
	    } else {
		try {
		    JSONArray arr = new JSONArray(res);
		    for (int i = 0; i < arr.length(); i++) {
			JSONObject v = arr.getJSONObject(i);
			BigDecimal value = v.getBigDecimal("category");
			JSONObject filtros = v.optJSONObject("filtros");
			String sid = v.optString("stationID");
			if (sid == null || !sid.equals(stationId)) {
			    continue;
			}
			if (filtros != null) {
			    String fecha = filtros.getString("fecha");
			    Date date = YEAR_MONTH_SDF.parse(fecha);
			    DinaguaValue dv = new DinaguaValue(value, date);
			    ret.addValue(dv);
			}
		    }
		} catch (JSONException e) {
		    GSLoggerFactory.getLogger(getClass()).error("This should not happen, parsing error: {}", res);
		}
	    }
	} else {
	    // nothing returned
	    GSLoggerFactory.getLogger(getClass()).error("This should not happen");
	}

	return ret;
    }

    private String getMonth(Date date) {
	return YEAR_MONTH_SDF.format(date);
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
	    throws Exception {

	String begin = URLEncoder.encode(DATE_TIME_SDF.format(beginDate), "UTF-8");
	String end = URLEncoder.encode(DATE_TIME_SDF.format(endDate), "UTF-8");

	String calculo = "";
	String path = "";

	switch (interpolation) {
	case AVERAGE:
	    calculo = "&tipo_calculo=Promedio";
	    path = "diarios";
	    break;
	case MAX:
	    calculo = "&tipo_calculo=Máximo";
	    path = "diarios";
	    break;
	case MIN:
	    calculo = "&tipo_calculo=Mínimo";
	    path = "diarios";
	    break;
	case CONTINUOUS:
	default:
	    path = "horarios";
	    calculo = "";
	    break;
	}

	String response = downloadString(getEndpoint() + "/service/datos/" + path + "?inicio=" + begin + "&fin=" + end + "&variable="
		+ seriesCode + "&id_estacion=" + stationId + calculo).get();

	JSONArray responseArray = new JSONArray(response);

	ArrayList<JSONObject> out = new ArrayList<>();

	for (int i = 0; i < responseArray.length(); i++) {

	    out.add(responseArray.getJSONObject(i));

	}

	return out;
    }

    /**
     * @return
     * @return
     * @throws IOException
     * @throws GSException
     */
    private Optional<String> downloadString(String request) throws Exception {

	String token = getToken();
	Downloader downloader = new Downloader();

	HashMap<String, String> headers = new HashMap<>();
	headers.put("Authorization", "Bearer " + token);
	return downloader.downloadOptionalString(request, HttpHeaderUtils.build(headers));
    }

    private Optional<HttpResponse<InputStream>> downloadResponse(String request) throws Exception {

	String token = getToken();
	Downloader downloader = new Downloader();

	HashMap<String, String> headers = new HashMap<>();
	headers.put("Authorization", "Bearer " + token);
	return downloader.downloadOptionalResponse(request, HttpHeaderUtils.build(headers));
    }

    @Override
    protected void retrieveStations() throws Exception {

	if (stations.size() == 0) {

	    String response = downloadString(getEndpoint() + "/service/estaciones").get();

	    JSONArray stationsArray = new JSONArray(response);

	    for (int i = 0; i < stationsArray.length(); i++) {

		JSONObject jsonStation = (JSONObject) stationsArray.get(i);

		//
		// MISSING
		//

		// String xCoordinate = jsonStation.get("coordenada_x").toString();
		// String yCoordinate = jsonStation.get("coordenada_y").toString();
		// String river = jsonStation.get("curso_observado").toString();
		// String details = jsonStation.get("observaciones").toString();
		// String regime = jsonStation.get("regimen").toString();

		//
		// CHANGED
		//

		// String basinArea = jsonStation.get("area_cuenca").toString();
		// String id = jsonStation.get("id").toString();
		// String departmentId = jsonStation.get("id_departamento").toString();
		// String name = jsonStation.get("nombre").toString();

		//
		// NEW
		//

		// "fecha_inicio": "1989-01-12",
		// "entidad": "Servicio hidrológico",
		// "pais": "Uruguay",
		// "tipo": "Auxiliar",
		// "var_corto": "H, Q",
		// "variables": "Nivel, Caudal",
		// "zona_horaria": "GMT-3"

		DinaguaStation station = new DinaguaStation(jsonStation);

		stations.put(station.getId(), station);
	    }
	}
    }

    @Override
    protected void retrieveStatusStations() throws Exception {

	if (statusStations.size() == 0) {

	    String response = downloadString(getEndpoint() + "/estadohidro/estaciones").get();

	    JSONArray stationsArray = new JSONArray(response);

	    for (int i = 0; i < stationsArray.length(); i++) {

		JSONObject jsonStation = (JSONObject) stationsArray.get(i);

		//
		// MISSING
		//

		// String xCoordinate = jsonStation.get("coordenada_x").toString();
		// String yCoordinate = jsonStation.get("coordenada_y").toString();
		// String river = jsonStation.get("curso_observado").toString();
		// String details = jsonStation.get("observaciones").toString();
		// String regime = jsonStation.get("regimen").toString();

		//
		// CHANGED
		//

		// String basinArea = jsonStation.get("area_cuenca").toString();
		// String id = jsonStation.get("id").toString();
		// String departmentId = jsonStation.get("id_departamento").toString();
		// String name = jsonStation.get("nombre").toString();

		//
		// NEW
		//

		// "fecha_inicio": "1989-01-12",
		// "entidad": "Servicio hidrológico",
		// "pais": "Uruguay",
		// "tipo": "Auxiliar",
		// "var_corto": "H, Q",
		// "variables": "Nivel, Caudal",
		// "zona_horaria": "GMT-3"

		DinaguaStation station = new DinaguaStation(jsonStation);
		station.setVariableString("monthly,quarterly,yearly");
		station.setVariableAbbreviationString("1,3,12");
		statusStations.put(station.getId(), station);
	    }
	}
    }
}
