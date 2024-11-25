package eu.essi_lab.accessor.agrostac.harvested;

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

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.lib.net.downloader.Downloader;

public class AgrostacCache {
    private static AgrostacCache instance;
    private Map<String, String> cropTypeMap;
    private JSONObject overview;
    private static String ACCESS_TOKEN;
    private Downloader downloader = new Downloader();

    private static final String CROP_TYPE = "crops";
    private static final String OVERVIEW = "overview";
    

    private AgrostacCache(String token) {
	cropTypeMap = new HashMap<>();
	overview = new JSONObject();
	ACCESS_TOKEN = token;
	populate();
    }

    public static synchronized AgrostacCache getInstance(String token) {
	if (instance == null) {
	    instance = new AgrostacCache(token);
	}
	return instance;
    }

    private void populate() {
	
	JSONObject cropTypeCover = getCropCode();
	JSONArray cropTypeCoverArray = cropTypeCover.optJSONArray("Crops");
	for (int index = 0; index < cropTypeCoverArray.length(); index++) {
	    JSONObject obj = cropTypeCoverArray.optJSONObject(index);
	    String code = obj.optString("crop_code");
	    String label = obj.optString("crop_name");
	    cropTypeMap.put(code, label);
	}
	overview = getAgrostacOverview();
    }

    private JSONObject getAgrostacOverview() {
	JSONObject ret = null;
	
	String overviewRequest = AgrostacConnector.BASE_URL + OVERVIEW + "?accesstoken=" + ACCESS_TOKEN;	
	String str = downloader.downloadOptionalString(overviewRequest).get();	
	ret = new JSONObject(str.replaceAll(System.lineSeparator()," "));
	
	return ret;
    }

    private JSONObject getCropCode() {
	String url = AgrostacConnector.BASE_URL + CROP_TYPE + "?accesstoken=" + ACCESS_TOKEN;
	JSONObject jsonObj = new JSONObject(downloader.downloadOptionalString(url).get());
	return jsonObj;
    }

    public void putCrop(String key, String value) {
	cropTypeMap.put(key, value);
    }

    public String getCrop(String key) {
	return cropTypeMap.get(key);
    }

    public boolean containsKeyCrop(String key) {
	return cropTypeMap.containsKey(key);
    }

    public void removeCrop(String key) {
	cropTypeMap.remove(key);
    }
    
    public JSONObject getOverview() {
	return overview;
    }
    
    public void setOverview(JSONObject overview) {
	this.overview = overview;
    }
    

}
