package eu.essi_lab.shared.driver.es.connector;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
public class QueryDocumentParser extends JSONDocumentParser {

    private static final String SOURCE_KEY = "_source";
    private static final String IOEXCEPTION_PARSING_ES_DOCUMENT = "IOEXCEPTION_PARSING_ES_DOCUMENT";
    private static final String HITS_KEY = "hits";

    public QueryDocumentParser(String jsonString) throws GSException {
	super(jsonString);
    }

    public List<InputStream> getSources() throws GSException {

	List<InputStream> lis = new ArrayList<>();

	try {

	    JSONArray jar = jsonObject.getJSONObject(HITS_KEY).getJSONArray(HITS_KEY);

	    for (int i = 0; i < jar.length(); i++) {

		JSONObject jobj = jar.getJSONObject(i);

		JSONObject source = jobj.getJSONObject(SOURCE_KEY);

		lis.add(new ByteArrayInputStream(source.toString().getBytes()));

	    }

	    return lis;

	} catch (JSONException e) {
	    throw GSException.createException(getClass(), "Can't parse document response", null, ErrorInfo.ERRORTYPE_SERVICE,
		    ErrorInfo.SEVERITY_ERROR, IOEXCEPTION_PARSING_ES_DOCUMENT, e);
	}
    }
}
