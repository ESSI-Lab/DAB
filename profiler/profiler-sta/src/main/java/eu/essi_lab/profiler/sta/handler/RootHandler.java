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

package eu.essi_lab.profiler.sta.handler;

import java.util.ArrayList;
import java.util.List;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.pdk.handler.DefaultRequestHandler;

/**
 * Handler for OGC STA service root (GET / or GET /v1.0).
 * Returns a JSON array of available entity set endpoints.
 */
public class RootHandler extends DefaultRequestHandler {

    @Override
    public String getStringResponse(WebRequest webRequest) throws GSException {
	String baseUrl = buildBaseUrl(webRequest);
	List<JSONObject> endpoints = new ArrayList<>();
	endpoints.add(createEndpoint("Things", baseUrl + "Things"));
	endpoints.add(createEndpoint("Locations", baseUrl + "Locations"));
	endpoints.add(createEndpoint("Observations", baseUrl + "Observations"));
	endpoints.add(createEndpoint("FeaturesOfInterest", baseUrl + "FeaturesOfInterest"));

	JSONArray arr = new JSONArray();
	for (JSONObject e : endpoints) {
	    arr.put(e);
	}
	return arr.toString();
    }

    private JSONObject createEndpoint(String name, String url) {
	JSONObject o = new JSONObject();
	o.put("name", name);
	o.put("url", url);
	return o;
    }

    private String buildBaseUrl(WebRequest webRequest) {
	String base = webRequest.getServletRequest().getRequestURL().toString();
	int idx = base.indexOf("/sta");
	if (idx >= 0) {
	    base = base.substring(0, idx + 4);
	}
	if (!base.endsWith("/")) {
	    base += "/";
	}
	if (!base.endsWith("v1.0/")) {
	    base += "v1.0/";
	}
	return base;
    }

    @Override
    public MediaType getMediaType(WebRequest webRequest) {
	return MediaType.APPLICATION_JSON_TYPE;
    }

    @Override
    public ValidationMessage validate(WebRequest request) throws GSException {
	ValidationMessage vm = new ValidationMessage();
	vm.setResult(ValidationResult.VALIDATION_SUCCESSFUL);
	return vm;
    }
}
