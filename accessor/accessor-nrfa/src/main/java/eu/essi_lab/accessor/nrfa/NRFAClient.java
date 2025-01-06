package eu.essi_lab.accessor.nrfa;

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

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.utils.ExpiringCache;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;

/**
 * @author boldrini
 */
public class NRFAClient {

    private String endpoint = "https://nrfaapps.ceh.ac.uk/nrfa/ws";
    private Downloader downloader;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    public NRFAClient() {
	this.downloader = new Downloader();
	sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

    }

    public NRFAClient(String endpoint) {
	this();
	this.endpoint = endpoint;
    }

    public List<String> getStationIdentifiers() {
	String url = endpoint + "/station-ids?format=json-array";
	String response = downloadString(url);
	JSONArray array = new JSONArray(response);
	List<String> ret = new ArrayList<String>();
	for (int i = 0; i < array.length(); i++) {
	    ret.add(array.get(i).toString());
//	    if (i>10 && new File("/home/boldrini").exists()) {
//		break;
//	    }
	}
	return ret;
    }

    public StationInfo getStationInfo(String stationId) {

	JSONObject json = getStationInfoJSON(stationId);

	JSONArray array = json.getJSONArray("data");
	JSONObject station = array.getJSONObject(0);

	StationInfo ret = new StationInfo(station);

	return ret;
    }

    private JSONObject getStationInfoJSON(String stationId) {
	String url = endpoint + "/station-info?station=" + stationId + "&format=json-object&fields=all";
	String response = downloadString(url);
	JSONObject ret = new JSONObject(response);
	return ret;
    }

    public List<SimpleEntry<Date, BigDecimal>> getValues(String stationId, String parameter, Date begin, Date end) {
	String d1 = sdf.format(begin);
	String d2 = sdf.format(end);
	String url = endpoint + "/time-series?format=json-object&data-type=" + parameter + "&station=" + stationId + "&date-range=" + d1
		+ "/" + d2;
	String response = downloadString(url);
	JSONObject object = new JSONObject(response);
	JSONArray array = object.getJSONArray("data-stream");
	List<SimpleEntry<Date, BigDecimal>> ret = new ArrayList<>();
	for (int i = 0; i < array.length(); i += 2) {
	    String dateString = array.get(i).toString();
	    Date date = ISO8601DateTimeUtils.parseISO8601ToDate(dateString).get();
	    BigDecimal value = new BigDecimal(array.get(i + 1).toString());
	    ret.add(new SimpleEntry<Date, BigDecimal>(date, value));
	}
	return ret;
    }

    private static ExpiringCache<String> CACHE = new ExpiringCache<>();
    static {
	CACHE.setDuration(60000);
	CACHE.setMaxSize(10);
    }

    private String downloadString(String url) {
	String response = CACHE.get(url);
	if (response != null) {
	    return response;
	}
	Optional<String> optionalString = downloader.downloadOptionalString(url);
	if (optionalString.isPresent()) {
	    response = optionalString.get();
	    CACHE.put(url, response);
	    return response;
	}
	GSLoggerFactory.getLogger(getClass()).error("Error downloading");
	return null;
    }

}
