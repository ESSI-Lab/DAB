package eu.essi_lab.accessor.hmfs;

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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.SocketTimeoutException;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.util.AbstractMap.SimpleEntry;
import java.util.concurrent.TimeUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.utils.ClonableInputStream;
import eu.essi_lab.lib.utils.ExpiringCache;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.lib.utils.JSONArrayStreamParser;
import eu.essi_lab.lib.utils.JSONArrayStreamParserListener;
import eu.essi_lab.lib.utils.JSONUtils;

/**
 * @author boldrini
 */
public class HMFSClient {

    // Swagger page: https://alerta.ina.gob.ar/test/swagger/index.html

    public static final String STANDARD_ENDPOINT = "https://alerta.ina.gob.ar/test";
    private String endpoint = null;

    public HMFSClient() {
	this(STANDARD_ENDPOINT);
    }

    public HMFSClient(String endpoint) {
	if (endpoint.endsWith("/")) {
	    endpoint = endpoint.substring(0, endpoint.length() - 1);
	}
	this.endpoint = endpoint;
    }

    public static Integer maxStations = null;

    public static void setMaxStations(Integer maxRecords) {
	maxStations = maxRecords;

    }

    /*
     * STATIONS
     */

    private static ExpiringCache<HMFSStation> stationCache;

    private static ExpiringCache<List<HMFSSeries>> seriesCache;

    private static ExpiringCache<List<HMFSSeries>> arealSeriesCache;

    private static ExpiringCache<List<HMFSSeriesInformation>> seriesInformationCache;

    static {
	stationCache = new ExpiringCache<>();
	stationCache.setDuration(604800000);
	seriesCache = new ExpiringCache<>();
	seriesCache.setDuration(604800000);
	arealSeriesCache = new ExpiringCache<>();
	arealSeriesCache.setDuration(604800000);
	seriesInformationCache = new ExpiringCache<>();
	seriesInformationCache.setDuration(604800000);
    }

    public List<String> getStationIdentifiers() throws Exception {
	getStations();
	List<String> identifiers = new ArrayList<>(stationCache.keySet());
	identifiers.sort(new Comparator<String>() {
	    @Override
	    public int compare(String o1, String o2) {
		return o1.compareTo(o2);
	    }
	});
	if (maxStations != null) {
	    identifiers = identifiers.subList(0, maxStations);
	}
	return identifiers;
    }

    public void getStations() throws Exception {
	if (stationCache.size() > 0) {
	    return;
	}
	String url = getEndpoint() + "/obs/puntual/estaciones?tabla=hmfs";
	InputStream result = getResponseStream(url);
	ClonableInputStream cis = new ClonableInputStream(result);
	String s = IOUtils.toString(cis.clone(), Charset.defaultCharset());
	JSONArrayStreamParser parser = new JSONArrayStreamParser();
	parser.parse(cis.clone(), new JSONArrayStreamParserListener() {

	    @Override
	    public void notifyJSONObject(JSONObject json) {
		HMFSStation station = new HMFSStation(json);
		stationCache.put("" + station.getId(), station);
	    }

	    @Override
	    public void finished() {
		GSLoggerFactory.getLogger(getClass()).info("Number of stations {}", stationCache.size());
	    }
	});
    }

    public HMFSStation getStation(String stationCode) throws Exception {
	getStations();
	return stationCache.get(stationCode);
    }

    /*
     * SERIES
     */

    public void getSeries() throws Exception {
	if (seriesCache.size() == 0) {
	    String url = getEndpoint() + "/obs/puntual/series?tabla=hmfs";
	    InputStream result = getResponseStream(url);
	    JSONArrayStreamParser parser = new JSONArrayStreamParser();
	    parser.parse(result, new JSONArrayStreamParserListener() {
		@Override
		public void notifyJSONObject(JSONObject json) {
		    HMFSSeries series = new HMFSSeries(json);
		    String stationCode = "" + series.getStation().getId();
		    List<HMFSSeries> cachedSeries = seriesCache.get(stationCode);
		    if (cachedSeries == null) {
			cachedSeries = new ArrayList<HMFSSeries>();
		    }
		    cachedSeries.add(series);
		    seriesCache.put(stationCode, cachedSeries);
		}

		@Override
		public void finished() {

		}
	    });

	}
	if (arealSeriesCache.size() == 0) {
	    String url = getEndpoint() + "/obs/areal/series?tabla=hmfs";
	    InputStream result = getResponseStream(url);
	    JSONArrayStreamParser parser = new JSONArrayStreamParser();
	    parser.parse(result, new JSONArrayStreamParserListener() {
		@Override
		public void notifyJSONObject(JSONObject json) {
		    HMFSSeries series = new HMFSSeries(json);
		    String stationCode = "" + series.getStation().getId();
		    List<HMFSSeries> cachedSeries = arealSeriesCache.get(stationCode);
		    if (cachedSeries == null) {
			cachedSeries = new ArrayList<HMFSSeries>();
		    }
		    cachedSeries.add(series);
		    arealSeriesCache.put(stationCode, cachedSeries);
		}

		@Override
		public void finished() {

		}
	    });

	}

    }

    public List<HMFSSeries> getSeries(String stationCode) throws Exception {
	List<HMFSSeries> ret = getPuntualSeries(stationCode);
	ret.addAll(getArealSeries(stationCode));
	return ret;
    }

    public List<HMFSSeries> getPuntualSeries(String stationCode) throws Exception {
	List<HMFSSeries> ret = seriesCache.get(stationCode);
	if (ret != null) {
	    return ret;
	}
	String url = getEndpoint() + "/obs/puntual/series?tabla=hmfs&estacion_id=" + stationCode;
	String result = getResponseString(url);
	JSONArray array = new JSONArray(result);
	ret = new ArrayList<HMFSSeries>();
	for (int i = 0; i < array.length(); i++) {
	    JSONObject json = array.getJSONObject(i);
	    HMFSSeries series = new HMFSSeries(json);
	    ret.add(series);
	}
	seriesCache.put(stationCode, ret);
	return ret;
    }

    public List<HMFSSeries> getArealSeries(String areaCode) throws Exception {
	List<HMFSSeries> ret = arealSeriesCache.get(areaCode);
	if (ret != null) {
	    return ret;
	}
	String url = getEndpoint() + "/obs/areal/series?tabla=hmfs&area_id=" + areaCode;
	String result = getResponseString(url);
	JSONArray array = new JSONArray(result);
	ret = new ArrayList<HMFSSeries>();
	for (int i = 0; i < array.length(); i++) {
	    JSONObject json = array.getJSONObject(i);
	    HMFSSeries series = new HMFSSeries(json);
	    ret.add(series);
	}
	arealSeriesCache.put(areaCode, ret);
	return ret;
    }

    private String getEndpoint() {
	return endpoint;
    }

    /*
     * LAST FORECAST DATE
     */
    public String getLastForecast(String stationCode, String seriesCode, String variableCode) throws Exception {
	String url = getEndpoint() + "/sim/calibrados/5/corridas/last?estacion_id=" + stationCode + "&series_id=" + seriesCode + "&var_id="
		+ variableCode + "&includeProno=false";
	String result = getResponseString(url);
	JSONObject json = new JSONObject(result);
	String ret = json.getString("forecast_date");
	return ret;
    }

    /*
     * SERIES INFO
     */

    // public HMFSSeriesInformation getSeriesInformation(String stationCode, String seriesCode, String variableCode,
    // String forecastDate,
    // String qualifier) throws Exception {
    // String url = getEndpoint() + "/sim/calibrados/5/corridas?estacion_id=" + stationCode + "&forecast_date=" +
    // forecastDate
    // + "&series_id=" + seriesCode + "&includeProno=false&group_by_qualifier=true&var_id=" + variableCode +
    // "&qualifier="
    // + qualifier;
    // String result = getResponseString(url);
    // JSONArray array = new JSONArray(result);
    // if (array.length() == 0) {
    // return null;
    // }
    // JSONObject info = array.getJSONObject(0);
    // if (!info.has("series")) {
    // return null;
    // }
    // JSONArray series = info.getJSONArray("series");
    // if (series.length() == 0) {
    // return null;
    // }
    // for (int i = 0; i < series.length(); i++) {
    // JSONObject json = series.getJSONObject(i);
    // HMFSSeriesInformation seriesInfo = new HMFSSeriesInformation(json);
    // return seriesInfo;
    // }
    // return null;
    // }

    public HMFSSeriesInformation getSeriesInformation(String stationCode, String seriesCode, String variableCode, String forecastDate,
	    String qualifier) throws Exception {
	List<HMFSSeriesInformation> information = getSeriesInformation(stationCode, seriesCode, variableCode);
	if (information == null || information.isEmpty()) {
	    return null;
	}
	HMFSSeriesInformation info = information.get(0);
	return info;
    }

    public List<HMFSSeriesInformation> getSeriesInformation(String stationCode, String seriesCode, String variableCode) throws Exception {
	getSeriesInformation();

	String id = stationCode + ";" + seriesCode + ";" + variableCode;
	List<HMFSSeriesInformation> ret = seriesInformationCache.get(id);

	return ret;
    }

    /*
     * DATA
     */

    private static String forecastDate;

    public String getForecastDate() {
	return forecastDate;
    }

    public String getSeriesInformation() throws Exception {
	if (seriesInformationCache.size() > 0) {
	    return forecastDate;
	}
	String url = getEndpoint() + "/sim/calibrados/5/corridas/last?tabla=hmfs";
	String result = getResponseString(url);
	JSONObject info = new JSONObject(result);
	forecastDate = info.getString("forecast_date");

	JSONArray series = info.getJSONArray("series");
	if (series.length() == 0) {
	    return forecastDate;
	}

	for (int i = 0; i < series.length(); i++) {
	    JSONObject json = series.getJSONObject(i);
	    HMFSSeriesInformation seriesInfo = new HMFSSeriesInformation(json);
	    String id = seriesInfo.getStationId() + ";" + seriesInfo.getId() + ";" + seriesInfo.getVariableId();
	    List<HMFSSeriesInformation> ret = seriesInformationCache.get(id);
	    if (ret == null) {
		ret = new ArrayList<HMFSSeriesInformation>();
	    }
	    ret.add(seriesInfo);
	    seriesInformationCache.put(id, ret);
	}
	return forecastDate;

    }

    public List<SimpleEntry<Date, BigDecimal>> getValues(String stationCode, String seriesCode, String variableCode, String forecastDate,
	    String qualifier, Date begin, Date end, String type) throws Exception {
	String url = getEndpoint() + "/sim/calibrados/5/corridas?estacion_id=" + stationCode + "&forecast_date=" + forecastDate
		+ "&series_id=" + seriesCode + "&qualifier=" + qualifier + "&includeProno=true&group_by_qualifier=true&var_id="
		+ variableCode + "&tipo=" + type;

	if (begin != null) {
	    url += "&timestart=" + ISO8601DateTimeUtils.getISO8601Date(begin);
	}
	if (end != null) {
	    url += "&timeend=" + ISO8601DateTimeUtils.getISO8601Date(end);
	}
	String result = getResponseString(url);
	List<SimpleEntry<Date, BigDecimal>> ret = new ArrayList<>();
	JSONArray array = new JSONArray(result);
	if (array.length() == 0) {
	    return ret;
	}
	JSONObject info = array.getJSONObject(0);
	if (!info.has("series")) {
	    return ret;
	}
	JSONArray series = info.getJSONArray("series");
	if (series.length() == 0) {
	    return ret;
	}
	JSONObject firstSerie = series.getJSONObject(0);
	if (!firstSerie.has("pronosticos")) {
	    return ret;
	}
	JSONArray pronosticos = firstSerie.getJSONArray("pronosticos");
	for (int i = 0; i < pronosticos.length(); i++) {
	    JSONObject json = pronosticos.getJSONObject(i);
	    String time = json.getString("timestart");
	    Date date = ISO8601DateTimeUtils.parseISO8601ToDate(time).get();
	    BigDecimal value = JSONUtils.getBigDecimal(json, "valor");
	    ret.add(new SimpleEntry<Date, BigDecimal>(date, value));
	}
	return ret;
    }

    /*
     * Utils
     */

    private String getResponseString(String url) throws Exception {
	InputStream stream = getResponseStream(url);
	ByteArrayOutputStream bos = new ByteArrayOutputStream();
	IOUtils.copy(stream, bos);
	stream.close();
	bos.close();
	return new String(bos.toByteArray());
    }

    private InputStream getResponseStream(String url) throws Exception {

	Downloader downloader = new Downloader();
	downloader.setConnectionTimeout(TimeUnit.SECONDS, 30);
	downloader.setRetryPolicy(5, TimeUnit.SECONDS, 5);

	HttpResponse<InputStream> ret = downloader.downloadResponse(url);

	return ret.body();
    }

}
