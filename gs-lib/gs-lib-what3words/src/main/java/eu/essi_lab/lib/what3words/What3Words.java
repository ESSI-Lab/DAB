package eu.essi_lab.lib.what3words;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.lib.net.downloader.Downloader;

public class What3Words {

    public static final String API_KEY = "CHQR93EQ";
    private String word1 = null;
    private String word2 = null;
    private String word3 = null;

    private String latitude = null;
    private String longitude = null;

    private String endpoint = "https://api.what3words.com/v2";

    public String getEndpoint() {
	return endpoint;
    }

    public String getWord1() {
	return word1;
    }

    public String getWord2() {
	return word2;
    }

    public String getWord3() {
	return word3;
    }

    public String getLatitude() {
	return latitude;
    }

    public String getLongitude() {
	return longitude;
    }

    /**
     * Initializes the object from the 3 words. The APIKey is used to contact the W3W server to retrieve latitude and
     * longitude. A default time out will be applied
     * 
     * @param apiKey
     * @param word1
     * @param word2
     * @param word3
     * @throws Exception
     */
    public What3Words(String firstWord, String secondWord, String thirdWord) throws Exception {
	this(firstWord, secondWord, thirdWord, null);
    }

    /**
     * Initializes the object from the 3 words. The APIKey is used to contact the W3W server to retrieve latitude and
     * longitude. A milliseconds timeout can also be specified
     * 
     * @param apiKey
     * @param word1
     * @param word2
     * @param word3
     * @param millisecondsTimeout
     * @throws Exception
     */
    public What3Words(String firstWord, String secondWord, String thirdWord, Integer millisecondsTimeout) throws Exception {
	this.word1 = firstWord;
	this.word2 = secondWord;
	this.word3 = thirdWord;
	final String url = endpoint + "/forward?key=" + API_KEY + "&addr=" + firstWord + "." + secondWord + "." + thirdWord
		+ "&lang=en&format=json&display=full";

	Downloader downloader = createDownloader();
	if (millisecondsTimeout != null) {
	    downloader.setConnectionTimeout(TimeUnit.MILLISECONDS, millisecondsTimeout);
	}
	String string = downloader.downloadOptionalString(url).orElse(null);
	JSONObject result = new JSONObject(string);

	if (result.has("geometry")) {
	    Object position = result.get("geometry");
	    if (position instanceof JSONObject) {
		JSONObject jsonObject = (JSONObject) position;
		this.latitude = jsonObject.get("lat").toString();
		this.longitude = jsonObject.get("lng").toString();
	    } else if (position instanceof JSONArray) {
		JSONArray jsonArray = (JSONArray) position;
		this.latitude = jsonArray.get(0).toString();
		this.longitude = jsonArray.get(1).toString();
	    }
	}

    }

    /**
     * Initializes the object from latitude and longitude. The APIKey is used to contact the W3W server to retrieve the
     * 3 words. A default timeout is specified.
     * 
     * @param apiKey
     * @param latitude
     * @param longitude
     * @param millisecondsTimeout
     * @throws Exception
     */
    public What3Words(String latitude, String longitude) throws Exception {
	this(latitude, longitude, (Integer) null);
    }

    /**
     * Initializes the object from latitude and longitude. The APIKey is used to contact the W3W server to retrieve the
     * 3 words. A request timeout can also be specified.
     * 
     * @param apiKey
     * @param latitude
     * @param longitude
     * @param millisecondsTimeout
     * @throws Exception
     */
    public What3Words(String latitude, String longitude, Integer millisecondsTimeout) throws Exception {
	this.latitude = latitude;
	this.longitude = longitude;
	final String url = endpoint + "/reverse?key=" + API_KEY + "&coords=" + latitude + "%2C" + longitude
		+ "&lang=en&format=json&display=full";

	Downloader downloader = createDownloader();
	if (millisecondsTimeout != null) {
	    downloader.setConnectionTimeout(TimeUnit.MILLISECONDS, millisecondsTimeout);
	}
	String string = downloader.downloadOptionalString(url).orElse(null);
	JSONObject result = new JSONObject(string);

	if (result.has("words")) {
	    Object position = result.get("words");
	    if (position instanceof String) {
		String str = (String) position;
		String[] split = str.split("\\.");
		this.word1 = split[0];
		this.word2 = split[1];
		this.word3 = split[2];
	    } else if (position instanceof JSONArray) {
		JSONArray jsonArray = (JSONArray) position;
		this.word1 = jsonArray.get(0).toString();
		this.word2 = jsonArray.get(1).toString();
		this.word3 = jsonArray.get(2).toString();
	    }
	}

    }

    protected Downloader createDownloader() {
	return new Downloader();
    }
}
