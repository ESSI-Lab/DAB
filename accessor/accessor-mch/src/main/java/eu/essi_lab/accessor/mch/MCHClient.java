package eu.essi_lab.accessor.mch;

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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.accessor.mch.datamodel.MCHAvailability;
import eu.essi_lab.accessor.mch.datamodel.MCHCountry;
import eu.essi_lab.accessor.mch.datamodel.MCHStation;
import eu.essi_lab.accessor.mch.datamodel.MCHValue;
import eu.essi_lab.accessor.mch.datamodel.MCHVariable;
import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.utils.ExpiringCache;
import eu.essi_lab.lib.utils.GSLoggerFactory;

/**
 * @author boldrini
 */
public class MCHClient {

    private String endpoint = null;
    private Downloader downloader = null;

    public MCHClient(String endpoint) {
	if (!endpoint.endsWith("/")) {
	    endpoint = endpoint + "/";
	}
	this.endpoint = endpoint;
	this.downloader = new Downloader();
    }

    public MCHCountry getCountry() {
	String url = endpoint + "country";
	String ret = downloadString(url);
	JSONArray jarr = new JSONArray(ret);
	if (jarr.length() > 0) {
	    JSONObject json = jarr.getJSONObject(0);
	    return new MCHCountry(json);
	} else {
	    return null;
	}
    }

    private static ExpiringCache<String> cache = new ExpiringCache<>();

    static {
	cache.setDuration(TimeUnit.HOURS.toMillis(6));
	cache.setMaxSize(100);
    }

    public List<MCHAvailability> getAvailability() {
	String url = endpoint + "availability";
	String str;
	synchronized (cache) {
	    str = cache.get(url);
	}
	if (str == null) {
	    synchronized (cache) {
		str = downloadString(url);
		cache.put(url, str);
	    }
	}

	JSONArray json = new JSONArray(str);
	List<MCHAvailability> ret = new ArrayList<>();
	for (int i = 0; i < json.length(); i++) {
	    JSONObject child = json.getJSONObject(i);
	    MCHAvailability avail = new MCHAvailability(child);
	    ret.add(avail);
	}
	return ret;
    }

    public List<MCHStation> getStations() {
	String url = endpoint + "stations";
	String str;
	synchronized (cache) {
	    str = cache.get(url);
	}
	if (str == null) {
	    synchronized (cache) {
		str = downloadString(url);
		cache.put(url, str);
	    }
	}
	List<MCHStation> ret = new ArrayList<>();
	JSONArray json = new JSONArray(str);
	for (int i = 0; i < json.length(); i++) {
	    JSONObject child = json.getJSONObject(i);
	    MCHStation avail = new MCHStation(child);
	    ret.add(avail);
	}
	return ret;
    }

    public MCHStation getStationByName(String name) {
	List<MCHStation> list = getStations();
	for (MCHStation station : list) {
	    if (station.getStationName().equals(name)) {
		return station;
	    }
	}
	return null;
    }

    public MCHStation getStationById(String id) {
	// String url = endpoint + "stations/qry_station?stn_id=" + id;
	// String str = downloadString(url);
	// JSONArray json = new JSONArray(str);
	// if (json.length() > 0) {
	// return new MCHStation(json.getJSONObject(0));
	// }
	// return null;
	List<MCHStation> list = getStations();
	for (MCHStation station : list) {
	    if (station.getStationId().equals(id)) {
		return station;
	    }
	}
	return null;
    }

    public List<String> getStationGroups() {
	String url = endpoint + "stngroups";
	String ret = downloadString(url);
	JSONArray array = new JSONArray(ret);
	List<String> list = new ArrayList<>();
	for (int i = 0; i < array.length(); i++) {
	    JSONObject json = array.getJSONObject(i);
	    if (json.has("Stngroup")) {
		String group = json.getString("Stngroup");
		list.add(group);
	    }
	}
	return list;
    }

    public List<String> getStationGroup(String id) {
	String url = endpoint + "stngroups/qry_stngroup?stngp_id=" + id;
	String str = downloadString(url);
	List<String> ret = new ArrayList<>();
	JSONArray array = new JSONArray(str);
	for (int i = 0; i < array.length(); i++) {
	    JSONObject json = array.getJSONObject(i);
	    String station = json.getString("Station");
	    ret.add(station);
	}
	return ret;
    }

    public List<String> getVariables() {
	String url = endpoint + "variables";
	String str = downloadString(url);
	JSONArray array = new JSONArray(str);
	List<String> ret = new ArrayList<>();
	for (int i = 0; i < array.length(); i++) {
	    JSONObject json = array.getJSONObject(i);
	    String variable = json.getString("Variable");
	    ret.add(variable);
	}
	return ret;
    }

    public MCHVariable getVariable(String id) {
	String url = null;
	try {
	    url = endpoint + "variables/qry_variable?var_id=" + URLEncoder.encode(id, "UTF-8");
	} catch (UnsupportedEncodingException e) {
	    e.printStackTrace();
	}
	String str;
	synchronized (cache) {
	    str = cache.get(url);
	}
	if (str == null) {
	    synchronized (cache) {
		str = downloadString(url);
		cache.put(url, str);
	    }
	}
	JSONArray json = new JSONArray(str);
	if (json.length() > 0) {
	    return new MCHVariable(json.getJSONObject(0));
	}
	return null;
    }

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    public List<MCHValue> getDailyData(String stationId, String variableId, Date start, Date end) {
	String dateString = start.equals(end) ? "&datee=" + sdf.format(start)
		: "&date_ini=" + sdf.format(start) + "&date_end=" + sdf.format(end);
	String url = null;
	try {
	    url = endpoint + "data/dailydata?stn_id=" + URLEncoder.encode(stationId, "UTF-8") + "&var_id="
		    + URLEncoder.encode(variableId, "UTF-8") + dateString;
	} catch (UnsupportedEncodingException e) {
	    e.printStackTrace();
	}
	return getData(url);
    }

    public List<MCHValue> getDetailData(String stationId, String variableId, Date start, Date end) {
	String dateString = start.equals(end) ? "&datee=" + sdf.format(start)
		: "&date_ini=" + sdf.format(start) + "&date_end=" + sdf.format(end);
	String url = null;
	try {
	    url = endpoint + "data/detaildata?stn_id=" + URLEncoder.encode(stationId, "UTF-8") + "&var_id="
		    + URLEncoder.encode(variableId, "UTF-8") + dateString;
	} catch (UnsupportedEncodingException e) {
	    e.printStackTrace();
	}
	return getData(url);
    }

    private List<MCHValue> getData(String url) {
	List<MCHValue> ret = new ArrayList<>();
	String str = downloadString(url);
	try {
	    JSONArray json = new JSONArray(str);
	    for (int i = 0; i < json.length(); i++) {
		JSONObject child = json.getJSONObject(i);
		MCHValue value = new MCHValue(child);
		ret.add(value);
	    }
	} catch (Exception e) {
	    GSLoggerFactory.getLogger(getClass()).error("Error downloading from URL: {}", url);
	}
	return ret;
    }

    private String downloadString(String url) {
	Optional<String> optionalString = downloader.downloadOptionalString(url);
	if (optionalString.isPresent()) {
	    return optionalString.get();
	}
	GSLoggerFactory.getLogger(getClass()).error("Error downloading");
	return null;
    }

    public List<MCHAvailability> getAvailability(String stationName) {
	List<MCHAvailability> ret = new ArrayList<>();
	List<MCHAvailability> all = getAvailability();
	for (MCHAvailability avail : all) {
	    if (avail.getStation().equals(stationName)) {
		ret.add(avail);
	    }
	}
	return ret;
    }

}
