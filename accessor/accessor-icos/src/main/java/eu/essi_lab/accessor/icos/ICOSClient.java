package eu.essi_lab.accessor.icos;

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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils.MethodNoBody;
import eu.essi_lab.lib.utils.ExpiringCache;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.GSLoggerFactory.GSLogger;

public class ICOSClient {

	private static ExpiringCache<List<String>> cache = new ExpiringCache<List<String>>();
	static {
		cache.setMaxSize(50);
		cache.setDuration(TimeUnit.MINUTES.toMillis(30));
	}

	private String url;

	public ICOSClient() {
		this("https://meta.icos-cp.eu/sparql");
	}

	public ICOSClient(String url) {
		this.url = url;
	}

	public List<String> getEquivalentConcepts(String conceptURI) throws IOException {
		List<String> ret = cache.get(conceptURI);
		if (ret != null) {
			return ret;
		}

		InputStream postRequest = ICOSClient.class.getClassLoader().getResourceAsStream("icos/parameterQuery");
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		IOUtils.copy(postRequest, baos);
		String queryString = new String(baos.toByteArray());
		queryString = queryString.replace("${PARAMETER_URI}", conceptURI);
		// System.out.println(queryString);
		ret = new ArrayList<>();

		try {
			GSLogger logger = GSLoggerFactory.getLogger(getClass());
			logger.info("Sending SPARQL Request to: " + url);
			Downloader executor = new Downloader();
			executor.setConnectionTimeout(TimeUnit.SECONDS, 10);
			HashMap<String, String> headers = new HashMap<String, String>();
			headers.put("Content-Type", "application/sparql-results+json; charset=UTF-8");
			String target = url + "?query=" + URLEncoder.encode(queryString, StandardCharsets.UTF_8);
			HttpResponse<InputStream> httpResponse = executor
					.downloadResponse(HttpRequestUtils.build(MethodNoBody.GET, target, headers));

			InputStream output = httpResponse.body();

			ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
			IOUtils.copy(output, baos2);
			String stringResponse = new String(baos2.toByteArray());
			// System.out.println(stringResponse);
			JSONObject jsonObject = new JSONObject(stringResponse);

			JSONObject res = jsonObject.optJSONObject("results");

			JSONArray jsonArray = getJSONArray(res, "bindings");

			ret = extractResultsAsList(jsonArray, "uri");

			logger.trace("ICOS List Data finding ENDED");

		} catch (Exception e) {
			e.printStackTrace();
		}
		cache.put(conceptURI, ret);
		return ret;

	}

	private List<String> extractResultsAsList(JSONArray jsonArray, String type) throws Exception {
		List<String> ret = new ArrayList<>();
		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject obj = jsonArray.optJSONObject(i);
			if (obj != null && obj.has("obj")) {
				{
					obj = obj.getJSONObject("obj");
					if (obj.has("type") && obj.getString("type").equals(type)) {
						if (obj.has("value")) {
							ret.add(obj.getString("value"));
						}
					}
				}
			}
		}
		return ret;
	}

	public JSONArray getJSONArray(JSONObject result, String key) {
		try {
			boolean hasKey = result.has(key);
			if (!hasKey) {
				return new JSONArray();
			}
			JSONArray ret = result.getJSONArray(key);
			if (ret == null || ret.length() == 0) {
				ret = new JSONArray();
			}
			return ret;
		} catch (Exception e) {
			// logger.warn("Error getting json array", e);
			return new JSONArray();
		}

	}

}
