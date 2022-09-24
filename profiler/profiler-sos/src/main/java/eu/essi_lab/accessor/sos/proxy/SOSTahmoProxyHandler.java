package eu.essi_lab.accessor.sos.proxy;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2022 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import javax.ws.rs.core.MediaType;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import eu.essi_lab.accessor.sos.observation.GetObservationRequest;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.JSONUtils;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.pdk.handler.DefaultRequestHandler;

public class SOSTahmoProxyHandler extends DefaultRequestHandler {

    private final static String TWIGA_URL = "http://hn4s.hydronet.com/api/service/TWIGA/sos?";
    private static final String SOS_TAHMO_PROXY_HANDLER_ERROR = "SOS_TAHMO_PROXY_HANDLER_ERROR";

    @Override
    public ValidationMessage validate(WebRequest request) throws GSException {

	ValidationMessage ret = new ValidationMessage();
	try {
	    new GetObservationRequest(request);
	    ret.setResult(ValidationResult.VALIDATION_SUCCESSFUL);
	} catch (Exception e) {
	    ret.setResult(ValidationResult.VALIDATION_FAILED);
	}

	return ret;
    }

    @Override
    public String getStringResponse(WebRequest webRequest) throws GSException {

	String query = webRequest.getQueryString();

	// send request to TAHMO service

	String request = TWIGA_URL + query;

	try {

	    GSLoggerFactory.getLogger(getClass()).info("Getting " + request);
	    HttpGet get = new HttpGet(request.trim());

	    // add authorization token
	    get.addHeader("Authorization", "Bearer " + ConfigurationWrapper.getCredentialsSetting().getSOSTahmoToken().orElse(""));

	    int timeout = 10;
	    int responseTimeout = 20;
	    RequestConfig config = RequestConfig.custom().setConnectTimeout(timeout * 1000)
		    .setConnectionRequestTimeout(responseTimeout * 1000).setSocketTimeout(timeout * 1000).build();
	    CloseableHttpClient client = HttpClientBuilder.create().setDefaultRequestConfig(config).build();

	    CloseableHttpResponse response = client.execute(get);

	    InputStream stream = response.getEntity().getContent();

	    if (stream == null) {
		throw new RuntimeException("Error downloading SOS GetObservation from remote service");
	    }

	    String ret = IOUtils.toString(stream, "UTF-8");
	    if (stream != null) {
		stream.close();
	    }

	    if (isJSON(ret)) {
		// error case
		ret = buildErrorResponse(ret);
	    }

	    return ret;

	} catch (Exception e) {

	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    ErrorInfo.ERRORTYPE_CLIENT, //
		    ErrorInfo.SEVERITY_ERROR, //
		    SOS_TAHMO_PROXY_HANDLER_ERROR);

	}

    }

    private String buildErrorResponse(String ret) {

	String error = "";
	String message = "Get Observation from " + TWIGA_URL + " failed";
	String name = "Invalid Operation Exception";
	try {

	    if (ret.contains("Exception")) {
		JSONObject json = new JSONObject(ret);
		JSONObject obj = json.getJSONObject("Exception");
		message = JSONUtils.getValue(obj, "Message");
		name = JSONUtils.getValue(obj, "Name");
		// request id
	    }

	    // String errorBody = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
	    // + "<ows:ExceptionReport xmlns:ows=\"http://www.opengis.net/ows/1.1\"
	    // xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" version=\"2.0.0\"
	    // xsi:schemaLocation=\"http://www.opengis.net/ows/1.1 http://schemas.opengis.net/ows/1.1.0/owsAll.xsd\">\n"
	    // + " <ows:Exception exceptionCode=\"" + error + "\" locator=\"" + error + "\">\n" + " <ows:ExceptionText>"
	    // + error
	    // + "</ows:ExceptionText>\n" + " </ows:Exception>\n" + "</ows:ExceptionReport>";

	    error = "<gs:error xmlns:gs=\"http://flora.eu/gi-suite/1.0/dataModel/schema\">";
	    error += "<gs:status>" + name + "</gs:status>";
	    error += "<gs:message>" + message + "</gs:message>";
	    error += "</gs:error>";

	    return error;

	} catch (Exception e) {
	    error = "<gs:error xmlns:gs=\"http://flora.eu/gi-suite/1.0/dataModel/schema\">";
	    error += "<gs:status>" + name + "</gs:status>";
	    error += "<gs:message>" + message + "</gs:message>";
	    error += "</gs:error>";
	    return error;
	}

    }

    private boolean isJSON(String text) {
	try {
	    new JSONObject(text);
	} catch (JSONException ex) {
	    // e.g. in case JSONArray is valid as well...
	    try {
		new JSONArray(text);
	    } catch (JSONException ex1) {
		return false;
	    }
	}
	return true;
    }

    @Override
    public MediaType getMediaType(WebRequest webRequest) {
	return MediaType.APPLICATION_XML_TYPE;
    }

}
