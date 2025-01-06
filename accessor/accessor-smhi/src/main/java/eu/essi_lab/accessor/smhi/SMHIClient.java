package eu.essi_lab.accessor.smhi;

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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Node;

import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.lib.xml.XMLDocumentReader;

public class SMHIClient {

    private static final String DEFAULT_ENDPOINT = "https://opendata-download-hydroobs.smhi.se/api";
    private String endpoint;

    public String getEndpoint() {
	return endpoint;
    }

    public String getApiLink() {
	return apiLink;
    }

    public void setApiLink(String apiLink) {
	this.apiLink = apiLink;
    }

    public String getAuthorName() {
	return authorName;
    }

    public void setAuthorName(String authorName) {
	this.authorName = authorName;
    }

    public String geteMail() {
	return eMail;
    }

    public void seteMail(String eMail) {
	this.eMail = eMail;
    }

    public String getRights() {
	return rights;
    }

    public void setRights(String rights) {
	this.rights = rights;
    }

    public String getUpdated() {
	return updated;
    }

    public void setUpdated(String updated) {
	this.updated = updated;
    }

    private String apiLink;
    private String authorName;
    private String eMail;
    private String rights;
    private String updated;

    public SMHIClient() {
	this(DEFAULT_ENDPOINT);
    }

    public SMHIClient(String endpoint) {
	this.endpoint = endpoint;
	Downloader downloader = new Downloader();
	Optional<InputStream> stream = downloader.downloadOptionalStream(endpoint);
	if (stream.isEmpty()) {
	    throw new IllegalArgumentException("Error contacting SMHI service");
	}
	try {
	    XMLDocumentReader reader = new XMLDocumentReader(stream.get());
	    this.authorName = reader.evaluateString("*:feed/*:author/*:name");
	    this.eMail = reader.evaluateString("*:feed/*:author/*:email");
	    this.rights = reader.evaluateString("*:feed/*:rights");
	    this.updated = reader.evaluateString("*:feed/*:updated");
	    Node[] linkNodes = reader.evaluateNodes("//*:feed/*:entry/*:link/@*:href");
	    for (Node linkNode : linkNodes) {
		String link = reader.evaluateString(linkNode, ".");
		if (link.contains("1.0.json")) {
		    this.apiLink = link;
		}
	    }
	} catch (Exception e) {
	    throw new IllegalArgumentException("Error parsing SMHI service response");
	}

    }

    public SMHIParameter getParameter(String key) {
	List<SMHIParameter> pars = getParameters();
	for (SMHIParameter par : pars) {
	    if (par.getKey().equals(key)) {
		return par;
	    }
	}
	return null;

    }

    private static List<SMHIParameter> parameters = new ArrayList<SMHIParameter>();

    public List<SMHIParameter> getParameters() {
	if (!parameters.isEmpty()) {
	    return parameters;
	}
	synchronized (parameters) {

	    Downloader downloader = new Downloader();
	    Optional<String> parameterPage = downloader.downloadOptionalString(apiLink);
	    List<SMHIParameter> ret = new ArrayList<SMHIParameter>();
	    if (parameterPage.isPresent()) {
		System.out.println(parameterPage.get());
		JSONObject json = new JSONObject(parameterPage.get());
		JSONArray resources = json.getJSONArray("resource");
		for (int i = 0; i < resources.length(); i++) {
		    JSONObject parameterObject = resources.getJSONObject(i);
		    String title = parameterObject.getString("title");
		    String key = parameterObject.getString("key");
		    String unit = parameterObject.getString("unit");
		    JSONArray links = parameterObject.getJSONArray("link");
		    SMHIParameter par = new SMHIParameter();
		    par.setTitle(title);
		    par.setKey(key);
		    par.setUnits(unit);
		    for (int j = 0; j < links.length(); j++) {
			JSONObject link = links.getJSONObject(j);
			String href = link.getString("href");
			String rel = link.getString("rel");
			String type = link.getString("type");
			switch (rel) {
			case "iso19139":
			    par.setIsoLink(href);
			    break;
			case "parameter":
			    if (type.equals("application/json")) {
				par.setStationLink(href);
			    }
			    break;
			default:
			    break;
			}
		    }
		    ret.add(par);
		}
	    }
	    Collections.sort(ret, new Comparator<SMHIParameter>() {

		@Override
		public int compare(SMHIParameter o1, SMHIParameter o2) {
		    return o1.getKey().compareTo(o2.getKey());
		}
	    });
	    parameters = ret;
	}
	return parameters;
    }

    private SMHIStation getStation(SMHIParameter parameter, String stationKey) {
	List<SMHIStation> stations = getStations(parameter);
	for (SMHIStation station : stations) {
	    if (station.getKey().equals(stationKey)) {
		return station;
	    }
	}
	return null;

    }

    private static HashMap<String, List<SMHIStation>> stations = new HashMap<String, List<SMHIStation>>();

    public List<SMHIStation> getStations(SMHIParameter parameter) {
	List<SMHIStation> ret = stations.get(parameter.getKey());
	if (ret != null) {
	    return ret;
	}
	synchronized (stations) {

	    Downloader downloader = new Downloader();
	    Optional<String> stationString = downloader.downloadOptionalString(parameter.getStationLink());
	    ret = new ArrayList<SMHIStation>();
	    if (stationString.isPresent()) {
		JSONObject page = new JSONObject(stationString.get());
		JSONArray stations = page.getJSONArray("station");
		for (int i = 0; i < stations.length(); i++) {
		    JSONObject station = stations.getJSONObject(i);
//		    System.out.println(station);
		    SMHIStation s = new SMHIStation();
		    s.setId(station.optInt("id"));
		    s.setKey(station.optString("key"));
		    s.setName(station.optString("name"));
		    s.setOwner(station.optString("owner"));
		    s.setMeasuringStations(station.optString("measuringStations"));
		    s.setActive(station.optBoolean("active"));
		    s.setLatitude(station.optBigDecimal("latitude", null));
		    s.setLongitude(station.optBigDecimal("longitude", null));
		    s.setFrom(station.optLong("from"));
		    s.setTo(station.optLong("to"));
		    s.setTitle(station.optString("title"));
		    s.setRegion(station.optInt("region"));
		    s.setCatchmentName(station.optString("catchmentName"));
		    s.setCatchmentNumber(station.optInt("catchmentNumber"));
		    s.setCatchmentsize(station.optInt("catchmentSize"));
		    s.setSummary(station.optString("summary"));
		    JSONArray links = station.getJSONArray("link");
		    for (int j = 0; j < links.length(); j++) {
			JSONObject link = links.getJSONObject(j);
			String href = link.optString("href");
			String rel = link.optString("rel");
			String type = link.optString("type");
			if (rel.equals("station") && type.equals("application/json")) {
			    s.setStationLink(href);
			}
		    }
		    ret.add(s);
		}
	    }
	    stations.put(parameter.getKey(), ret);
	}
	return ret;

    }

    private String getDataLink(SMHIStation station) {
	String link = station.getStationLink();
	Downloader downloader = new Downloader();
	Optional<String> down = downloader.downloadOptionalString(link);
	if (down.isPresent()) {
	    JSONObject s = new JSONObject(down.get());
	    JSONArray periods = s.getJSONArray("period");
	    for (int k = 0; k < periods.length(); k++) {
		JSONObject period = periods.getJSONObject(k);
		String key = period.getString("key");
		if (!key.equals("corrected-archive")) {
		    continue;
		}
		JSONArray links = period.getJSONArray("link");
		for (int i = 0; i < links.length(); i++) {
		    JSONObject l = links.getJSONObject(i);
		    if (l.getString("type").contains("json")) {
			String href = l.getString("href");
			Optional<String> str = downloader.downloadOptionalString(href);
			if (str.isPresent()) {
			    JSONObject data = new JSONObject(str.get());
			    JSONArray datas = data.getJSONArray("data");
			    for (int j = 0; j < datas.length(); j++) {
				JSONObject d = datas.getJSONObject(j);
				String kk = d.getString("key");
				if (!kk.equals(key)) {
				    continue;
				}
				JSONArray innerLinks = d.getJSONArray("link");
				for (int m = 0; m < innerLinks.length(); m++) {
				    JSONObject innerLink = innerLinks.getJSONObject(m);
				    String ret = innerLink.getString("href");
				    return ret;
				}
			    }
			}
		    }
		}
	    }
	}
	GSLoggerFactory.getLogger(getClass()).error("no data for station {}", station.getKey());
	return null;
    }

    public SMHIData getData(String parameterKey, String stationKey) {
	SMHIParameter parameter = getParameter(parameterKey);
	SMHIStation station = getStation(parameter, stationKey);
	String link = getDataLink(station);
	Downloader downloader = new Downloader();
	Optional<InputStream> dataResponse = downloader.downloadOptionalStream(link);
	SMHIData ret = new SMHIData();
	if (dataResponse.isPresent()) {
	    InputStream inputStream = dataResponse.get();
	    try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
		String line;
		boolean dataBegins = false;
		while ((line = reader.readLine()) != null) {
		    if (line.contains(";")) {
			String[] split = line.split(";");
			if (dataBegins) {
			    SMHIValue value = new SMHIValue();
			    Date date = parseDate(split[0]);
			    value.setDate(date);
			    if (split[1].contains("-")) {
				// value is a date
				Date vd = ISO8601DateTimeUtils.parseISO8601(split[1]);
				value.setValue(new BigDecimal(vd.getTime()));
			    } else {
				value.setValue(new BigDecimal(split[1]));
			    }
			    value.setQuality(split[2]);
			    ret.getValues().add(value);
			} else {
			    String first = split[0].toLowerCase();
			    if (first.contains("datum") || first.contains("säsong")) {
				dataBegins = true;
			    }
			}
		    }

		}
	    } catch (IOException e) {
		e.printStackTrace();
	    }

	}
	return ret;

    }

    private Date parseDate(String dateString) {
	SimpleDateFormat dateFormat = null;
	if (dateString.length() == 10 && dateString.contains("-")) {
	    // a date: 1956-01-27
	    dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	    dateFormat.setTimeZone(TimeZone.getTimeZone("CEST"));
	} else if (dateString.length() == 9 && dateString.contains("-")) {
	    // a season: 1924-1925
	    dateFormat = new SimpleDateFormat("yyyy");
	    dateString = dateString.substring(0, dateString.indexOf("-"));
	} else if (dateString.length() == 19 && dateString.contains(" ")) {
	    // date time: 1984-08-16 09:15:00
	    dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	}

	try {
	    Date date = dateFormat.parse(dateString);
	    return date;
	} catch (Exception e) {
	    e.printStackTrace();
	}
	return null;
    }

    public SMHIMetadata getMetadata(String parameterId, String stationId) {
	SMHIParameter parameter = getParameter(parameterId);
	SMHIStation station = getStation(parameter, stationId);
	SMHIMetadata ret = new SMHIMetadata();
	ret.setParameter(parameter);
	ret.setStation(station);
	return ret ;
	
    }

}
