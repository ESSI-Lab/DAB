package eu.essi_lab.accessor.apitempo;

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
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.accessor.apitempo.APITempoParameter.APITempoParameterCode;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.net.downloader.HttpHeaderUtils;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;

public class APITempoClient {

    public static final String STANDARD_ENDPOINT = "https://apitempo.inmet.gov.br/plata/";

    public APITempoClient() {

    }

    private static String giProxyEndpoint = null;

    public static String getGiProxyEndpoint() {
	if (giProxyEndpoint == null) {
	    giProxyEndpoint = ConfigurationWrapper.getSystemSettings().getProxyEndpoint().orElse(null);
	}
	return giProxyEndpoint;
    }

    public static void setGiProxyEndpoint(String endpoint) {
	giProxyEndpoint = endpoint;
    }

    public APITempoClient(String endpoint) {
	this.endpoint = endpoint;
    }

    @XmlTransient
    private String endpoint = STANDARD_ENDPOINT;

    public String getEndpoint() {
	return endpoint;
    }

    public void setEndpoint(String endpoint) {
	if (!endpoint.endsWith("/")) {
	    endpoint = endpoint + "/";
	}
	this.endpoint = endpoint;

    }

    // STATIONS

    @XmlTransient
    private static List<APITempoStation> stations = new ArrayList<>();

    public List<APITempoStation> getStations() {
	if (stations.isEmpty()) {
	    GSLoggerFactory.getLogger(getClass()).info("Getting stations");
	    synchronized (stations) {
		GSLoggerFactory.getLogger(getClass()).info("Actually getting stations");
		if (stations.isEmpty()) {
		    GSLoggerFactory.getLogger(getClass()).info("Really actually getting stations");
		    Optional<InputStream> optionalStream = getStream(endpoint + "estacoes");
		    List<APITempoStation> newStations = parseStations(optionalStream);
		    stations.addAll(newStations);
		}
	    }
	}
	return stations;

    }

    public APITempoStation getStation(String stationCode) {
	List<APITempoStation> stations = getStations();
	for (APITempoStation station : stations) {
	    if (station.getValue(APITempoStation.APITempoStationCode.ID).equals(stationCode)) {
		return station;
	    }
	}
	return null;
    }

    public static synchronized Optional<InputStream> getStream(String url) {
	int tries = 3;
	Exception e = null;
	for (int i = 0; i < tries; i++) {
	    try {
		Downloader downloader = new Downloader();
		downloader.setConnectionTimeout(TimeUnit.SECONDS, 8);
		Map<String, String> headers = new HashMap<>();
		headers.put("Accept", "*/*");

		String proxyEndpoint = getGiProxyEndpoint();

		if (proxyEndpoint != null) {
		    url = proxyEndpoint + "/get?url=" + url;
		}
		Optional<InputStream> ret = downloader.downloadOptionalStream(url, HttpHeaderUtils.build(headers));
		if (ret.isPresent()) {
		    return ret;
		}
	    } catch (Exception tmp) {
		e = tmp;
		GSLoggerFactory.getLogger(APITempoClient.class).warn("Exception contacting INMET: " + e.getMessage());
	    }
	    if (i == (tries - 1)) {
		GSLoggerFactory.getLogger(APITempoClient.class).warn("Last try failed.");
		if (e != null) {
		    throw new RuntimeException(e.getMessage());
		}
	    } else {
		long ms = (long) (10000.0 * Math.random());
		GSLoggerFactory.getLogger(APITempoClient.class).warn("Retrying (#{}) after a little sleep: {}ms", i, ms);
		try {
		    Thread.sleep(ms);
		} catch (InterruptedException e1) {
		}
	    }
	}
	return Optional.empty();

    }

    private List<APITempoStation> parseStations(Optional<InputStream> optionalStream) {
	ArrayList<APITempoStation> ret = new ArrayList<>();
	if (optionalStream.isPresent()) {
	    InputStream stream = optionalStream.get();
	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    try {
		IOUtils.copy(stream, baos);
		stream.close();
		String stationString = new String(baos.toByteArray(), StandardCharsets.UTF_8);
		JSONArray stations = new JSONArray(stationString);
		for (int i = 0; i < stations.length(); i++) {
		    JSONObject stationJSON = stations.getJSONObject(i);
		    APITempoStation station = new APITempoStation(stationJSON);
		    ret.add(station);
		}
	    } catch (IOException e) {
		e.printStackTrace();
	    }

	}
	return ret;
    }

    // PARAMETERS

    public List<APITempoParameter> getStationParameters(String stationCode) {
	Optional<InputStream> optionalStream = getStream(endpoint + "atributos/" + stationCode);
	return parseParameters(optionalStream);
    }

    private List<APITempoParameter> parseParameters(Optional<InputStream> optionalStream) {
	ArrayList<APITempoParameter> ret = new ArrayList<>();
	if (optionalStream.isPresent()) {
	    InputStream stream = optionalStream.get();
	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    try {
		IOUtils.copy(stream, baos);
		stream.close();
		String stationString = new String(baos.toByteArray(), StandardCharsets.UTF_8);
		JSONArray stations = new JSONArray(stationString);
		for (int i = 0; i < stations.length(); i++) {
		    JSONObject parameterJSON = stations.getJSONObject(i);
		    APITempoParameter parameter = new APITempoParameter(parameterJSON);
		    ret.add(parameter);
		}
	    } catch (IOException e) {
		e.printStackTrace();
	    }

	}
	return ret;
    }

    public APITempoParameter getStationParameter(String stationCode, String parameterCode) {
	List<APITempoParameter> parameters = getStationParameters(stationCode);
	for (APITempoParameter parameter : parameters) {
	    if (parameter.getValue(APITempoParameterCode.ID).equals(parameterCode)) {
		return parameter;
	    }
	}
	return null;
    }

    // DATA

    public List<APITempoData> getData(String stationCode, String parameterCode, String dateBegin, String dateEnd) {
	Optional<InputStream> optionalStream = getStream(endpoint + stationCode + "/" + parameterCode + "/" + dateBegin + "/" + dateEnd);
	return parseData(optionalStream);
    }

    private List<APITempoData> parseData(Optional<InputStream> optionalStream) {
	ArrayList<APITempoData> ret = new ArrayList<>();
	if (optionalStream.isPresent()) {
	    InputStream stream = optionalStream.get();
	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    try {
		IOUtils.copy(stream, baos);
		stream.close();
		String str = new String(baos.toByteArray(), StandardCharsets.UTF_8);
		JSONArray stations = new JSONArray(str);
		for (int i = 0; i < stations.length(); i++) {
		    JSONObject dataJSON = stations.getJSONObject(i);
		    APITempoData data = new APITempoData(dataJSON);
		    ret.add(data);
		}
	    } catch (IOException e) {
		e.printStackTrace();
	    }

	}
	return ret;
    }

    public SimpleEntry<Date, Date> getBeginEndDates(String stationCode, String parameterCode) throws ParseException {

	// first, find out end date
	Date endDate = getEndDate(stationCode, parameterCode);

	if (endDate == null) {
	    return null;
	}

	GSLoggerFactory.getLogger(getClass()).info("Got end date:  {}", endDate);

	Date beginDate = getBeginDate(stationCode, parameterCode, new Date(endDate.getTime()));

	if (beginDate == null) {
	    return null;
	}

	GSLoggerFactory.getLogger(getClass()).info("Got begin date:  {}", beginDate);

	return new SimpleEntry<>(beginDate, endDate);
    }

    private Date getBeginDate(String stationCode, String parameterCode, Date okDate) throws ParseException {

	okDate = new Date(okDate.getTime() + 1);
	System.out.println("Max date to test: " + ISO8601DateTimeUtils.getISO8601Date(okDate));

	String tentativeDateString = "1900-01-01";
	Date tentativeDate = ISO8601DateTimeUtils.parseISO8601(tentativeDateString);

	List<APITempoData> data;
	String check = null;
	Long previousGap = null;
	while (true) {
	    long gap = okDate.getTime() - tentativeDate.getTime();
	    if (previousGap == null) {
		previousGap = gap;
	    } else {
		if (previousGap.equals(gap)) {
		    return null;
		}
		previousGap = gap;
	    }
	    tentativeDateString = ISO8601DateTimeUtils.getISO8601Date(tentativeDate);

	    if (check != null && check.equals(tentativeDateString)) {
		// the check is skipped
		data = new ArrayList<>();
	    } else {
		data = getData(stationCode, parameterCode, "1900-01-01", tentativeDateString);
	    }
	    check = tentativeDateString;

	    if (data.isEmpty()) {
		tentativeDate = new Date(tentativeDate.getTime() + (gap / 2));
	    } else {
		return data.get(0).getDate();
	    }
	    System.out.println("GAP: " + gap);

	}

    }

    private Date getEndDate(String stationCode, String parameterCode) {
	Date date = new Date(new Date().getTime() - 1000l * 60 * 60 * 24); // 1 day ago
	SimpleEntry<Date, Date> info = getBeginEndDates(stationCode, parameterCode, date, new Date());

	if (info != null) {
	    return info.getValue();
	}
	date = new Date(date.getTime() - (1000l * 60 * 60 * 24)); // 2 days ago
	info = getBeginEndDates(stationCode, parameterCode, date, new Date());

	if (info != null) {
	    return info.getValue();
	}

	date = new Date(date.getTime() - (1000l * 60 * 60 * 24 * 30)); // 1 month ago
	info = getBeginEndDates(stationCode, parameterCode, date, new Date());

	if (info != null) {
	    return info.getValue();
	}

	date = new Date(date.getTime() - (1000l * 60 * 60 * 24 * 30 * 12)); // 1 year ago
	info = getBeginEndDates(stationCode, parameterCode, date, new Date());

	if (info != null) {
	    return info.getValue();
	}
	date = new Date(date.getTime() - (5*1000l * 60 * 60 * 24 * 30 * 12)); // 5 year ago
	info = getBeginEndDates(stationCode, parameterCode, date, new Date());

	if (info != null) {
	    return info.getValue();
	}

	date = new Date(date.getTime() - (10*1000l * 60 * 60 * 24 * 30 * 12)); // 10 year ago
	info = getBeginEndDates(stationCode, parameterCode, date, new Date());

	if (info != null) {
	    return info.getValue();
	}
	
	date = new Date(date.getTime() - (100*1000l * 60 * 60 * 24 * 30 * 12)); // 100 year ago
	info = getBeginEndDates(stationCode, parameterCode, date, new Date());

	if (info != null) {
	    return info.getValue();
	}

	GSLoggerFactory.getLogger(getClass()).error("End date not found");

	return null;
    }

    public SimpleEntry<Date, Date> getBeginEndDates(String stationCode, String parameterCode, Date dateBegin, Date dateEnd) {

	SimpleEntry<Date, Date> ret = null;

	String dateBeginString = ISO8601DateTimeUtils.getISO8601Date(dateBegin);
	String dateEndString = ISO8601DateTimeUtils.getISO8601Date(dateEnd);

	GSLoggerFactory.getLogger(getClass()).info("Checking dates: {}-{}", dateBeginString, dateEndString);

	try {

	    List<APITempoData> datas = getData(stationCode, parameterCode, dateBeginString, dateEndString);

	    Date minDate = null;
	    Date maxDate = null;

	    for (APITempoData data : datas) {

		Date parsed = data.getDate();

		if (minDate == null || parsed.before(minDate)) {
		    minDate = parsed;
		}
		if (maxDate == null || parsed.after(maxDate)) {
		    maxDate = parsed;
		}
	    }

	    if (minDate != null && maxDate != null) {
		ret = new SimpleEntry<>(minDate, maxDate);
	    }

	    return ret;

	} catch (Exception e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}

	return null;
    }

}
