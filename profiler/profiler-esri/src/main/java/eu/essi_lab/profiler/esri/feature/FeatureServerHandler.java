package eu.essi_lab.profiler.esri.feature;

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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.pdk.handler.WebRequestHandler;
import eu.essi_lab.pdk.validation.WebRequestValidator;
import eu.essi_lab.profiler.esri.feature.query.ESRIRequest;

/**
 * @author boldrini
 */
public class FeatureServerHandler implements WebRequestHandler, WebRequestValidator {

    @Override
    public Response handle(WebRequest request) throws GSException {

	// String myIp = "79.12.126.178";
	// String fullRequest = request.getServletRequest().getRequestURL().toString();
	// if (!fullRequest.contains(myIp)) {
	// String queryString = request.getQueryString();
	//
	// if (queryString != null) {
	// fullRequest = fullRequest + "?" + queryString;
	// }
	//
	// String rest = fullRequest.substring(fullRequest.indexOf("/gs-service/"));
	//
	// Downloader d = new Downloader();
	// Optional<String> string = d.downloadString("http://" + myIp + ":9090" + rest);
	//
	// if (string.isPresent()) {
	// ByteArrayInputStream stream = new ByteArrayInputStream(string.get().getBytes());
	// return Response.status(Status.OK).type(MediaType.APPLICATION_JSON).entity(stream).build();
	// }
	// }

	// https://sampleserver3.arcgisonline.com/ArcGIS/rest/services/Earthquakes/EarthquakesFromLastSevenDays/FeatureServer?f=json

	// String rest = fullRequest.substring(fullRequest.indexOf("/gs-service/"));
	// rest =
	// rest.replace("/gs-service/services/essi/view/whos-arctic/ArcGIS/rest/services/EarthquakesFromLastSevenDays/FeatureServer",
	// "https://sampleserver3.arcgisonline.com/ArcGIS/rest/services/Earthquakes/EarthquakesFromLastSevenDays/FeatureServer");
	//
	// Downloader dd = new Downloader();
	// String queryString = request.getQueryString();
	//
	// if (queryString != null) {
	// rest = rest + "?" + queryString;
	// }
	//
	// Optional<String> string = dd.downloadString(rest);
	// if (string.isPresent()) {
	// ByteArrayInputStream stream = new ByteArrayInputStream(string.get().getBytes());
	// return Response.status(Status.OK).type(MediaType.APPLICATION_JSON).entity(stream).build();
	// }

	ESRIRequest esriRequest = new ESRIRequest(request);

	String callback = esriRequest.getParameter("callback");

	String path = request.getServletRequest().getPathInfo();
	if (path.contains("?")) {
	    path = path.substring(0, path.indexOf("?"));
	}

	JSONObject ret = new JSONObject();
	ret.put("currentVersion", 11);
	// base server info
	if (path.endsWith("/FeatureServer")) {

	    InputStream stream = FeatureServerHandler.class.getClassLoader()
		    .getResourceAsStream("esri/feature-server-server-template.json");
	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    try {
		IOUtils.copy(stream, baos);
		String str = new String(baos.toByteArray());
		stream.close();
		baos.close();
		ret = new JSONObject(str);
	    } catch (IOException e) {
		e.printStackTrace();
	    }

	    ret.put("serviceDescription", "This service is powered by the Discovery and Access Broker (DAB). Contacts: http://essi-lab.eu");
	    JSONArray layers = new JSONArray();
	    List<FeatureLayer> availableLayers = FeatureLayer.getAvailableLayers();
	    for (FeatureLayer layer : availableLayers) {
		JSONObject l = new JSONObject();
		l.put("id", layer.getId());
		l.put("name", layer.getName());
		l.put("parentLayerId", -1);
		l.put("defaultVisibility", true);
		l.put("minScale", 0);
		l.put("maxScale", 0);
		l.put("type", "Feature Layer");
		l.put("geometryType", "esriGeometryPoint");
		layers.put(l);
	    }
	    ret.put("layers", layers);
	    JSONArray tables = new JSONArray();
	    ret.put("tables", tables);
	} else {
	    // layer info
	    String id = path.substring(path.lastIndexOf("/") + 1);
	    FeatureLayer layer = FeatureLayer.getLayer(id);
	    if (layer == null) {
		JSONObject error = new JSONObject();
		error.put("code", 500);
		error.put("message", "Layer not found");
		JSONArray details = new JSONArray();
		error.put("details", details);
		ret.put("error", error);
	    } else {
		ret = layer.getJSON(request);

	    }
	}

	if (callback == null) {
	    callback = "";
	} else {
	    callback = callback + "(";
	}

	String str = callback + ret.toString();

	if (!callback.isEmpty()) {
	    str += ")";
	}

	ByteArrayInputStream stream = new ByteArrayInputStream(str.getBytes());
	return Response.status(Status.OK).type(MediaType.APPLICATION_JSON).entity(stream).build();

    }

    @Override
    public ValidationMessage validate(WebRequest request) throws GSException {
	ValidationMessage ret = new ValidationMessage();
	ret.setResult(ValidationResult.VALIDATION_SUCCESSFUL);
	return ret;
    }

}
