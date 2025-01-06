package eu.essi_lab.accessor.whos;

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
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.utils.ExpiringCache;
import eu.essi_lab.lib.utils.GSLoggerFactory;

/**
 * @author boldrini
 */
public class MWRIClient {

    private String endpoint = null;

    public MWRIClient(String endpoint) {
	this.endpoint = endpoint;
    }

    private static ExpiringCache<List<MWRIStation>> stationCache;

    static {
	stationCache = new ExpiringCache<>();
	stationCache.setDuration(12000000);
    }

    public List<MWRIStation> getStations() throws Exception {
	List<MWRIStation> ret = stationCache.get("stations");

	if (ret != null) {
	    return ret;
	} else {
	    ret = new ArrayList<>();
	}
	String url = endpoint;
	List<JSONObject> responses = getResponses(url);
	for (JSONObject response : responses) {
	    
		MWRIStation station = new MWRIStation(response);
		ret.add(station);
	   
	}
	stationCache.put("stations", ret);
	return ret;
    }

    private List<JSONObject> getResponses(String url) throws Exception {
	List<JSONObject> r = new ArrayList<>();

	Downloader downloader = new Downloader();
	HttpResponse<InputStream> ret;
	int maxTries = 5;
	do {
	    ret = downloader.downloadResponse(url);
	    if (ret.statusCode() == 200) {
		break;
	    } else {
		GSLoggerFactory.getLogger(getClass()).info("Sleeping and retrying {}", maxTries);
		Thread.sleep(5000);
	    }
	} while (maxTries-- > 0);

	if (maxTries < 0) {
	    throw new RuntimeException("HTTP error code getting from MWRI");
	}

	InputStream stream = ret.body();
	String source = IOUtils.toString(stream, StandardCharsets.UTF_8);
	stream.close();
	JSONArray json = new JSONArray(source);
	JSONArray innerArray = json.getJSONArray(1);
	for (int i = 0; i < innerArray.length(); i++) {
	    JSONObject j = innerArray.getJSONObject(i);
	    r.add(j);
	}
	return r;

    }

    public MWRIStation getStation(String stationCode) throws Exception {
	List<MWRIStation> stations = getStations();
	for (MWRIStation station : stations) {
	    if (stationCode.equals(station.getName())) {
		return station;
	    }
	}
	return null;
    }

}
