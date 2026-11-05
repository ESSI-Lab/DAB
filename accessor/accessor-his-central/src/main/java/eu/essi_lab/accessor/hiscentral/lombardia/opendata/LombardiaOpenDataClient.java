package eu.essi_lab.accessor.hiscentral.lombardia.opendata;

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

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.net.http.HttpHeaders;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONObject;

import dev.failsafe.RetryPolicy;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.net.downloader.HttpHeaderUtils;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.IOStreamUtils;

/**
 * Client of the Socrata (SODA) API of the Regione Lombardia open data portal, serving the ARPA Lombardia
 * hydro-nivo-meteorological sensor registry and observations.
 * <p>
 * Anonymous requests are throttled by the portal (HTTP 429), so requests are retried with exponential backoff; an
 * application token, when available, raises the limits.
 *
 * @author boldrini
 */
public class LombardiaOpenDataClient {

    public static final String DEFAULT_ENDPOINT = "https://www.dati.lombardia.it";

    /**
     * Sensor registry ("Stazioni Idro Nivo Meteorologiche")
     */
    public static final String SENSORS_DATASET = "nf78-nj6b";

    /**
     * Real time observations ("Dati sensori meteo"), a rolling window of the latest months for all sensor types
     */
    public static final String REAL_TIME_DATASET = "647i-nhxk";

    /**
     * Observation times are expressed in "ora solare" (CET, UTC+1 all year round)
     */
    public static final ZoneOffset DATA_OFFSET = ZoneOffset.ofHours(1);

    private static final DateTimeFormatter FLOATING_TIMESTAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private static final int PAGE_SIZE = 50000;

    private final String endpoint;
    private final String appToken;

    public LombardiaOpenDataClient() {
	this(DEFAULT_ENDPOINT, null);
    }

    /**
     * @param endpoint the open data portal base URL
     * @param appToken the Socrata application token (optional)
     */
    public LombardiaOpenDataClient(String endpoint, String appToken) {
	String url = endpoint == null || endpoint.isBlank() ? DEFAULT_ENDPOINT : endpoint.trim();
	while (url.endsWith("/")) {
	    url = url.substring(0, url.length() - 1);
	}
	this.endpoint = url;
	this.appToken = appToken == null || appToken.isBlank() ? null : appToken.trim();
    }

    public String getEndpoint() {
	return endpoint;
    }

    /**
     * @return all the sensors of the registry, active and dismissed
     */
    public List<JSONObject> getSensors() throws IOException {
	Map<String, String> params = new LinkedHashMap<>();
	params.put("$order", "idsensore");
	return queryAll(SENSORS_DATASET, params);
    }

    /**
     * Summarizes the real time observations of the latest days, to find which series are currently updated and with
     * which time step
     *
     * @param days number of days before now to consider
     * @return one object per sensor and operator, with fields {@code idsensore}, {@code idoperatore}, {@code count}
     */
    public List<JSONObject> getRecentObservationCounts(int days) throws IOException {
	LocalDateTime end = LocalDateTime.now(DATA_OFFSET).withHour(0).withMinute(0).withSecond(0).withNano(0);
	LocalDateTime begin = end.minusDays(days);
	Map<String, String> params = new LinkedHashMap<>();
	params.put("$select", "idsensore,idoperatore,count(*) as count");
	params.put("$where", "data > '" + format(begin) + "' AND data <= '" + format(end) + "'");
	params.put("$group", "idsensore,idoperatore");
	return queryAll(REAL_TIME_DATASET, params);
    }

    /**
     * @param datasetId the dataset to query (the real time dataset or one of the archives)
     * @param sensorId the sensor identifier
     * @param operator the operator code, used only for the real time dataset (archives have no operator column)
     * @param begin the begin of the interval, in the data time zone (see {@link #DATA_OFFSET})
     * @param end the end of the interval, in the data time zone (see {@link #DATA_OFFSET})
     * @return the observations ordered by time, with fields {@code data}, {@code valore} and {@code stato}
     */
    public List<JSONObject> getObservations(String datasetId, String sensorId, String operator, LocalDateTime begin, LocalDateTime end)
	    throws IOException {

	StringBuilder where = new StringBuilder();
	where.append("idsensore = '").append(sensorId).append("'");
	if (operator != null && datasetId.equals(REAL_TIME_DATASET)) {
	    where.append(" AND idoperatore = '").append(operator).append("'");
	}
	where.append(" AND data >= '").append(format(begin)).append("'");
	where.append(" AND data <= '").append(format(end)).append("'");

	Map<String, String> params = new LinkedHashMap<>();
	params.put("$select", "data,valore,stato");
	params.put("$where", where.toString());
	params.put("$order", "data");
	return queryAll(datasetId, params);
    }

    /**
     * @param timestamp a Socrata floating timestamp, e.g. 2026-11-03T12:20:00.000
     * @return the timestamp as a local date time in the data time zone
     */
    public static LocalDateTime parse(String timestamp) {
	String value = timestamp.trim();
	int dot = value.indexOf('.');
	if (dot > 0) {
	    value = value.substring(0, dot);
	}
	return LocalDateTime.parse(value, FLOATING_TIMESTAMP);
    }

    public static String format(LocalDateTime dateTime) {
	return FLOATING_TIMESTAMP.format(dateTime);
    }

    /**
     * Pages through the results of the given query
     */
    private List<JSONObject> queryAll(String datasetId, Map<String, String> params) throws IOException {

	List<JSONObject> ret = new ArrayList<>();
	int offset = 0;
	while (true) {
	    Map<String, String> pageParams = new LinkedHashMap<>(params);
	    pageParams.put("$limit", String.valueOf(PAGE_SIZE));
	    pageParams.put("$offset", String.valueOf(offset));

	    JSONArray page = query(datasetId, pageParams);
	    for (int i = 0; i < page.length(); i++) {
		ret.add(page.getJSONObject(i));
	    }
	    if (page.length() < PAGE_SIZE) {
		return ret;
	    }
	    offset += PAGE_SIZE;
	}
    }

    private JSONArray query(String datasetId, Map<String, String> params) throws IOException {

	StringBuilder url = new StringBuilder(endpoint).append("/resource/").append(datasetId).append(".json");
	String separator = "?";
	for (Map.Entry<String, String> param : params.entrySet()) {
	    url.append(separator).append(param.getKey()).append("=").append(URLEncoder.encode(param.getValue(), StandardCharsets.UTF_8));
	    separator = "&";
	}

	String body = get(url.toString());
	if (body.trim().startsWith("{")) {
	    // errors are returned as objects, e.g. {"message":"Too many requests","errorCode":"too-many-requests"}
	    throw new IOException("Error querying " + url + ": " + body);
	}
	return new JSONArray(body);
    }

    private String get(String url) throws IOException {

	Downloader downloader = new Downloader();
	downloader.setConnectionTimeout(TimeUnit.SECONDS, 60);
	downloader.setResponseTimeout(TimeUnit.MINUTES, 5);
	downloader.setRetryPolicy(RetryPolicy.<HttpResponse<InputStream>> builder()//
		.handleResultIf(r -> r.statusCode() == 429 || r.statusCode() >= 500)//
		.withBackoff(Duration.ofSeconds(2), Duration.ofMinutes(2))//
		.withJitter(0.25)//
		.withMaxAttempts(8)//
		.onRetry(e -> GSLoggerFactory.getLogger(getClass()).warn("Lombardia open data: HTTP {}, retry #{}",
			e.getLastResult() == null ? "-" : e.getLastResult().statusCode(), e.getAttemptCount()))//
		.build());

	HttpHeaders headers = appToken == null ? HttpHeaderUtils.buildEmpty() : HttpHeaderUtils.build("X-App-Token", appToken);

	GSLoggerFactory.getLogger(getClass()).trace("Getting {}", url);

	try {
	    HttpResponse<InputStream> response = downloader.downloadResponse(url, headers);
	    String body = IOStreamUtils.asUTF8String(response.body());
	    if (response.statusCode() != 200) {
		throw new IOException("HTTP " + response.statusCode() + " from " + url + ": " + body);
	    }
	    return body;
	} catch (IOException e) {
	    throw e;
	} catch (InterruptedException e) {
	    Thread.currentThread().interrupt();
	    throw new IOException("Interrupted while getting " + url, e);
	} catch (Exception e) {
	    throw new IOException("Unable to get " + url + ": " + e.getMessage(), e);
	}
    }
}
