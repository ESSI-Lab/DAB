package eu.essi_lab.profiler.semantic;

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
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils;
import eu.essi_lab.lib.net.downloader.HttpRequestUtils.MethodNoBody;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.pdk.handler.WebRequestHandler;
import eu.essi_lab.pdk.validation.WebRequestValidator;

/**
 * This test profiler is used to distribute queries amongst different SPARQL services. It was used for a short time
 * during the WMO Ontology creation
 * 
 * @author boldrini
 */
public class SPARQLHandler implements WebRequestHandler, WebRequestValidator {

    @Override
    public Response handle(WebRequest webRequest) throws GSException {

	// return handleMarklogic(webRequest);
	String query = "";
	String callback = "";
	try {
	    query = URLEncoder.encode(webRequest.getServletRequest().getParameter("query"), "UTF-8");
	    callback = URLEncoder.encode(webRequest.getServletRequest().getParameter("callback"), "UTF-8");
	} catch (UnsupportedEncodingException e1) {
	    e1.printStackTrace();
	}

	String endpoint = "http://localhost:3030/test/sparql";

	JSONObject obj = getResults(endpoint, query);

	// if (query.contains("wmo")) {

	String endpoint2 = "https://codes.wmo.int/system/query";

	JSONObject obj2 = getResults(endpoint2, query);

	merge(obj, obj2);

	// }

	String ret = callback + "(" + obj.toString() + ")";
	return Response.status(Status.OK).type(MediaType.APPLICATION_JSON).entity(new ByteArrayInputStream(ret.getBytes())).build();

    }

    private void merge(JSONObject obj, JSONObject obj2) {
	JSONArray bindings2 = obj2.getJSONObject("results").getJSONArray("bindings");
	JSONArray bindings = obj.getJSONObject("results").getJSONArray("bindings");
	for (int i = 0; i < bindings2.length(); i++) {
	    JSONObject result = bindings2.getJSONObject(i);
	    bindings.put(result);
	}

    }

    private JSONObject getResults(String endpoint, String query) {

	String url = endpoint + "?format=json&query=" + query;

	System.out.println("Sending to url: " + url);

	Downloader hre = new Downloader();

	try {
	    InputStream stream = hre.downloadResponse(HttpRequestUtils.build(MethodNoBody.GET, url)).body();

	    ByteArrayOutputStream baos = new ByteArrayOutputStream();

	    IOUtils.copy(stream, baos);
	    stream.close();
	    baos.close();

	    String str = new String(baos.toByteArray());

	    JSONObject obj = new JSONObject(str);

	    System.out.println(str);

	    return obj;

	} catch (Exception e) {
	    // TODO: handle exception
	}
	return null;
    }

    private Response handleMarklogic(WebRequest webRequest) {
	String query = "";
	String callback = "";
	try {
	    query = URLEncoder.encode(webRequest.getServletRequest().getParameter("query"), "UTF-8");
	    callback = URLEncoder.encode(webRequest.getServletRequest().getParameter("callback"), "UTF-8");
	} catch (UnsupportedEncodingException e1) {
	    e1.printStackTrace();
	}

	String url = "http://localhost:8000/v1/graphs/sparql?format=json&query=" + query;

	System.out.println("Sending to url: " + url);

	Downloader hre = new Downloader();

	try {
	    InputStream stream = hre.downloadResponse(//
		    HttpRequestUtils.build(MethodNoBody.GET, url), //
		    System.getProperty("dbUser"), System.getProperty("dbPassword")).//
		    body();

	    ByteArrayOutputStream baos = new ByteArrayOutputStream();

	    IOUtils.copy(stream, baos);
	    stream.close();
	    baos.close();

	    String ret = callback + "(" + new String(baos.toByteArray()) + ")";

	    return Response.status(Status.OK).type(MediaType.APPLICATION_JSON).entity(new ByteArrayInputStream(ret.getBytes())).build();
	} catch (Exception e) {
	    e.printStackTrace();
	    return Response.status(Status.INTERNAL_SERVER_ERROR).type(MediaType.TEXT_PLAIN)
		    .entity(new ByteArrayInputStream(e.getMessage().getBytes())).build();
	}
    }

    @Override
    public ValidationMessage validate(WebRequest request) throws GSException {
	ValidationMessage ret = new ValidationMessage();
	ret.setResult(ValidationResult.VALIDATION_SUCCESSFUL);
	return ret;
    }

}
