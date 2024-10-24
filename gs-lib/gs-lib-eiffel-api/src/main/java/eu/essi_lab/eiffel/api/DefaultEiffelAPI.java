package eu.essi_lab.eiffel.api;

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

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils.MethodWithBody;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.IOStreamUtils;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;

/**
 * https://search.eiffel4climate.eu/search-api/docs#/
 * 
 * @author Fabrizio
 */
public class DefaultEiffelAPI implements EiffelAPI {

    public DefaultEiffelAPI() {

    }

    /**
     * Sort and filter: sorted by the Eiffel search API according to the given <code>searchTerms</code>, and filtered by
     * DAB according to a merged query
     * 
     * @param searchTerms
     * @param start
     * @param count
     * @return
     * @throws GSException
     */
    public SimpleEntry<List<String>, Integer> searchIdentifiers(//
	    SearchIdentifiersApi mode, //
	    String searchTerms, //
	    String bbox, //
	    int start, //
	    int count) throws GSException {

	try {
	    searchTerms = URLEncoder.encode(searchTerms, "UTF-8");
	    if (bbox != null) {
		bbox = URLEncoder.encode(bbox, "UTF-8");
	    }
	} catch (UnsupportedEncodingException e) {
	}

	switch (mode) {

	case SEARCH:

	    return searchIdentifiersWithSearchAPI(searchTerms, bbox, start, count);

	case FILTER:
	}

	return

	searchIdentifiersWithFilterAPI(searchTerms);
    }

    /**
     * Sort and filter: sorted by the Eiffel search API according to the given <code>searchTerms</code>, and filtered by
     * DAB according to a merged query
     * 
     * @param searchTerms
     * @param start
     * @param count
     * @return
     * @throws GSException
     */
    @Override
    public SimpleEntry<List<String>, Integer> searchIdentifiers(SearchIdentifiersApi mode, String searchTerms, int start, int count)
	    throws GSException {

	return searchIdentifiers(mode, searchTerms, null, start, count);
    }

    /**
     * Filter and sort: filtered by DAB according to the original user query constraints, sorted by the Eiffel sort API
     * according only to the given <code>searchTerms</code>
     * 
     * <pre>
     * Example query:
     * 
     * curl -X 'POST' \
    'https://search.eiffel4climate.eu/search-api/semantic/sort' \
    -H 'accept: application/json' \
    -H 'Content-Type: application/json' \
    -d '{
    "ids": [
    "c2fb854e-8eec-404b-8430-fba75b0c2e07",
    "787b0d36-fc74-4499-9df7-bf007dba3eb9"
    ],
    "query": "water"
    }'
     * 
     * Request body:
     * 
     * {
     * 	"ids": [
     * 		"c2fb854e-8eec-404b-8430-fba75b0c2e07",
     * 		"787b0d36-fc74-4499-9df7-bf007dba3eb9"
     * 		],
     * 	"query": "water"
     * }
     * 
     * Example response:
     * 
     * {
     * 	"query": "water",
     * 	"sorted": [
     * 		{
     * 			"id": "c2fb854e-8eec-404b-8430-fba75b0c2e07",
     * 			"score": 0.5258864
     * 		},
     * 		{
     * 			"id": "787b0d36-fc74-4499-9df7-bf007dba3eb9",
     * 			"score": 0.5231436
     * 		}
     * 	]
     * }
     * </pre>
     */
    @Override
    public List<String> sortIdentifiers(String searchTerms, List<String> identifiers) {

	try {
	    searchTerms = URLEncoder.encode(searchTerms, "UTF-8");
	} catch (UnsupportedEncodingException e) {
	}

	JSONObject queryObject = new JSONObject();

	JSONArray ids = new JSONArray();
	identifiers.forEach(id -> ids.put(id));

	queryObject.put("ids", ids);
	queryObject.put("query", searchTerms);

	//
	//
	//

	Downloader executor = new Downloader();

	HttpRequest postRequest = null;
	try {
	    postRequest = HttpRequestUtils.build(MethodWithBody.POST, //
		    "https://search.eiffel4climate.eu/search-api/semantic/sort", //
		    queryObject.toString(3));

	} catch (Exception e) {
	}

	//
	//
	//

	ArrayList<String> out = new ArrayList<>();

	try {

	    HttpResponse<InputStream> response = executor.downloadResponse(postRequest);

	    InputStream content = response.body();

	    String contentString = IOStreamUtils.asUTF8String(content);

	    JSONObject responseObject = new JSONObject(contentString);

	    JSONArray sorted = responseObject.getJSONArray("sorted");

	    Iterator<Object> iterator = sorted.iterator();

	    while (iterator.hasNext()) {

		JSONObject next = (JSONObject) iterator.next();

		out.add(next.getString("id"));
	    }

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage());
	}

	return out;
    }

    /**
     * @param searchTerms
     * @return
     * @throws GSException
     */
    private SimpleEntry<List<String>, Integer> searchIdentifiersWithFilterAPI(String searchTerms) throws GSException {

	String query = buildFilterAPIQuery(searchTerms);

	Downloader downloader = new Downloader();
	Optional<String> response = downloader.downloadOptionalString(query);

	if (!response.isPresent()) {

	    throw GSException.createException(//
		    getClass(), //
		    "Unable to execute filter API query", //
		    ErrorInfo.ERRORTYPE_SERVICE, //
		    ErrorInfo.SEVERITY_ERROR, //
		    "EIFFEL_FILTER_API_ERROR");
	}

	String stringResponse = response.get();

	JSONObject jsonResponse = new JSONObject(stringResponse);

	int pageCount = 1;

	JSONArray results = jsonResponse.getJSONArray("results");

	ArrayList<String> list = new ArrayList<String>();

	for (int i = 0; i < results.length(); i++) {

	    JSONObject result = results.getJSONObject(i);

	    list.add(result.getString("id"));
	}

	return new SimpleEntry<>(list, pageCount);
    }

    /**
     * @param searchTerms
     * @return
     * @throws GSException
     */
    private String buildFilterAPIQuery(String searchTerms) {

	String query = "https://search.eiffel4climate.eu/search-api/semantic/filter?query=" + searchTerms + "&threshold="
		+ getFilterTreshold();

	return query;
    }

    /**
     * @param searchTerms
     * @param bbox
     * @param start
     * @param count
     * @return
     * @throws GSException
     */
    private SimpleEntry<List<String>, Integer> searchIdentifiersWithSearchAPI(//
	    String searchTerms, //
	    String bbox, //
	    int start, //
	    int count) throws GSException {

	String query = buildSearchAPIQuery(searchTerms, bbox, start, count);

	Downloader downloader = new Downloader();
	Optional<String> response = downloader.downloadOptionalString(query);

	if (!response.isPresent()) {

	    throw GSException.createException(//
		    getClass(), //
		    "Unable to execute search API query", //
		    ErrorInfo.ERRORTYPE_SERVICE, //
		    ErrorInfo.SEVERITY_ERROR, //
		    "EIFFEL_SERARCH_API_ERROR");
	}

	String stringResponse = response.get();

	JSONObject jsonResponse = new JSONObject(stringResponse);

	int pageCount = jsonResponse.getInt("totalPages");

	JSONArray data = jsonResponse.getJSONArray("data");

	ArrayList<String> list = new ArrayList<String>();

	for (int i = 0; i < data.length(); i++) {

	    JSONObject result = data.getJSONObject(i);

	    JSONArray members = result.getJSONArray("members");

	    members.forEach(id -> list.add(id.toString()));
	}

	return new SimpleEntry<>(list, pageCount);
    }

    /**
     * @param searchTerms
     * @param bbox
     * @param start
     * @param count
     * @return
     */
    private String buildSearchAPIQuery(String searchTerms, String bbox, int start, int count) {

	String query = "query=" + searchTerms + "&";

	String bboxParam = bbox == null ? "" : "&bbox=" + bbox;

	return "https://search.eiffel4climate.eu/search-api/search?" + query + "page=" + start + "&recordsPerPage=" + count
		+ "&queryMethod=" + getSearchQueryMethod() + "&minScore=" + getMinScore() + bboxParam;
    }

    /**
     * @param args
     * @throws URISyntaxException
     */
    public static void main(String[] args) throws URISyntaxException {

	String query = "https://search.eiffel4climate.eu/search-api/semantic/filter?query=water&threshold=0.7";

	Downloader downloader = new Downloader();
	Optional<String> response = downloader.downloadOptionalString(query);

	String stringResponse = response.get();

	JSONObject jsonResponse = new JSONObject(stringResponse);

	JSONArray results = jsonResponse.getJSONArray("results");

	ArrayList<String> list = new ArrayList<String>();

	for (int i = 0; i < results.length(); i++) {

	    JSONObject result = results.getJSONObject(i);

	    list.add(result.getString("id"));
	}

	System.out.println(results);
    }
}
