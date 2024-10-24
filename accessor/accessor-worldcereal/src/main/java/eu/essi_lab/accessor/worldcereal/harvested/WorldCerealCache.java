package eu.essi_lab.accessor.worldcereal.harvested;

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

public class WorldCerealCache {
    private static WorldCerealCache instance;
    private Map<String, String> cropTypeMap;
    private Map<String, String> irrTypeMap;
    private Map<String, String> landCoverMap;

    private static final String endpoint = "https://ewoc-rdm-api.iiasa.ac.at/";
    private static final String CROP_TYPE = "eotypes/CropType";
    private static final String LAND_COVER_TYPE = "eotypes/LandCoverType";
    private static final String IRRIGATION_TYPE = "eotypes/IrrigationType";

    private WorldCerealCache() {
	cropTypeMap = new HashMap<>();
	irrTypeMap = new HashMap<>();
	landCoverMap = new HashMap<>();
	populateMaps();
    }

    public static synchronized WorldCerealCache getInstance() {
	if (instance == null) {
	    instance = new WorldCerealCache();
	}
	return instance;
    }

    private void populateMaps() {
	JSONArray cropTypeCoverArray = getEwocCode(CROP_TYPE);
	for (int index = 0; index < cropTypeCoverArray.length(); index++) {
	    JSONObject obj = cropTypeCoverArray.optJSONObject(index);
	    String code = obj.optString("value");
	    String label = obj.optString("name");
	    cropTypeMap.put(code, label);
	}
	JSONArray landCoverArray = getEwocCode(LAND_COVER_TYPE);
	for (int index = 0; index < landCoverArray.length(); index++) {
	    JSONObject obj = landCoverArray.optJSONObject(index);
	    String code = obj.optString("value");
	    String label = obj.optString("name");
	    landCoverMap.put(code, label);
	}
	JSONArray irrArray = getEwocCode(IRRIGATION_TYPE);
	for (int index = 0; index < irrArray.length(); index++) {
	    JSONObject obj = irrArray.optJSONObject(index);
	    String code = obj.optString("value");
	    String label = obj.optString("name");
	    irrTypeMap.put(code, label);
	}

    }

    private JSONArray getEwocCode(String path) {
	String url = endpoint + path;

	Downloader downloader = new Downloader();
	JSONArray jsonArray = new JSONArray(downloader.downloadOptionalString(url).get());

	return jsonArray;
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

    public void putIrr(String key, String value) {
	irrTypeMap.put(key, value);
    }

    public String getIrr(String key) {
	return irrTypeMap.get(key);
    }

    public boolean containsKeyIrr(String key) {
	return irrTypeMap.containsKey(key);
    }

    public void removeIrr(String key) {
	irrTypeMap.remove(key);
    }

    public void putLc(String key, String value) {
	landCoverMap.put(key, value);
    }

    public String getLc(String key) {
	return landCoverMap.get(key);
    }

    public boolean containsKeyLc(String key) {
	return landCoverMap.containsKey(key);
    }

    public void removeLc(String key) {
	landCoverMap.remove(key);
    }

}
