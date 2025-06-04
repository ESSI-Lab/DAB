package eu.essi_lab.profiler.esri.proxy;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2025 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
import java.net.http.HttpHeaders;
import java.net.http.HttpResponse;
import java.util.Optional;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import com.amazonaws.util.IOUtils;

import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.pdk.handler.WebRequestHandler;
import eu.essi_lab.pdk.validation.WebRequestValidator;

/**
 * @author boldrini
 */
public class FeatureServerProxyHandler implements WebRequestHandler, WebRequestValidator {

    @Override
    public Response handle(WebRequest request) throws GSException {

	String myIp = "boldrini.essi-lab.eu";
	String baseRequest = request.getServletRequest().getRequestURL().toString();
	String queryString = request.getQueryString();

	Downloader d = new Downloader();

	// https://services8.arcgis.com/3ArZhpXFARDixnL2/ArcGIS/rest/services/geolytix_retailpoints_v26_202211/FeatureServer/0?f=pjson&token=
	// https://services8.arcgis.com/3ArZhpXFARDixnL2/ArcGIS/rest/services/geolytix_retailpoints_v26_202211/FeatureServer?f=json

	// String url =
	// "https://services.arcgis.com/P3ePLMYs2RVChkJx/ArcGIS/rest/services/USA_ZIP_Code_Points_analysis/FeatureServer/0?"
	// + queryString;

	String url = "https://services.arcgis.com/d3voDfTFbHOCRwVR/ArcGIS/rest/services/ter_2016_stop/";

	// String url =
	// "https://boldrini.essi-lab.eu:8443/gs-service/services/essi/view/whos-arctic/ArcGIS/rest/services/WHOS/";

	String userRequest = baseRequest.substring(baseRequest.indexOf("FeatureServer"));

	ResponseBuilder builder = Response.status(Status.OK);

	// try {
	// if (userRequest.equals("FeatureServer")) {
	//
	// return builder.entity(
	// new FileInputStream(new
	// File("/home/boldrini/git/GI-project/profiler/profiler-esri/src/main/resources/ESRI1.json")))
	// .build();
	// }
	// if (userRequest.equals("FeatureServer/0")||userRequest.equals("FeatureServer/1")) {
	//
	// return builder.entity(
	// new FileInputStream(new
	// File("/home/boldrini/git/GI-project/profiler/profiler-esri/src/main/resources/ESRI2.json")))
	// .build();
	// }
	// } catch (Exception e) {
	// e.printStackTrace();
	// }

	url = url + userRequest;

	url = url + "?" + queryString;
	// "http://" + myIp + ":9090" + rest

	Optional<HttpResponse<InputStream>> hb = d.downloadOptionalResponse(url);

	// if (hb.isPresent()) {
	// String contentType = "";

	HttpHeaders headers = hb.get().headers();
	Optional<String> cType = headers.firstValue("Content-type");
	if (cType.isEmpty()) {

	    cType = headers.firstValue("content-type");
	}

	if (cType.isPresent()) {

	    builder = builder.header("Content-type", cType.get());
	    System.out.println(cType.get());
	}

	// for (Header header : hb.get().getKey()) {
	// if (header.getName().toLowerCase().equals("content-type")) {
	// // contentType = header.getValue();
	// }
	// builder = builder.header(header.getName(), header.getValue());
	// System.out.println(header.getName());
	// }

	return builder.entity(hb.get().body()).build();
	// }
	// }

	// if (rest.equals("/services/essi/view/whos-arctic/ArcGIS/rest/info?f=json")) {
	// // first operation
	// ret = readResponse("ESRI1.json");
	//
	// } else if (rest.equals("/gs-service/services/essi/view/whos-arctic/ArcGIS/rest/info?f=json")) {
	//
	// ret = readResponse("ESRI2.json");
	//
	//// http://boldrini.essi-lab.eu:9090/gs-service/services/essi/view/whos-arctic/ArcGIS/rest/services/WHOS/FeatureServer/1?f=json
	//
	// }else if
	// (rest.equals("/gs-service/services/essi/view/whos-arctic/ArcGIS/rest/services/WHOS/FeatureServer/1?f=json"))
	// {
	// ret = readResponse("ESRI5.json");
	// }else if
	// (rest.contains("/gs-service/services/essi/view/whos-arctic/ArcGIS/rest/services/WHOS/FeatureServer/1/query"))
	// {
	//
	// if (rest.contains("returnIdsOnly=true")) {
	// ret = readResponse("ESRI3.json");
	// }else {
	// ret = readResponse("ESRI4.json");
	// }
	//
	// }else {
	// System.err.println("ERROR");
	// ret = "ERROR";
	// }

	// String json =
	// "https://services8.arcgis.com/3ArZhpXFARDixnL2/ArcGIS/rest/services/geolytix_retailpoints_v26_202211/FeatureServer?f=json";

	// https://sampleserver3.arcgisonline.com/ArcGIS/rest/services/Earthquakes/EarthquakesFromLastSevenDays/FeatureServer?f=json

	// String rest = fullRequest.substring(fullRequest.indexOf("/gs-service/"));
	// String rest2 = rest;
	// if (rest.contains("Earthquakes")) {
	// rest = rest.replace("/gs-service/services/essi/view/whos-arctic/ArcGISProxy",
	// "https://sampleserver3.arcgisonline.com/ArcGIS");
	// } else {
	// rest2 = rest.replace("/gs-service/services/essi/view/whos-arctic/ArcGISProxy",
	// "http://gs-service-production.geodab.eu/gs-service/services/essi/view/whos-arctic/ArcGIS");
	// rest = rest.replace("/gs-service/services/essi/view/whos-arctic/ArcGISProxy",
	// "http://localhost:9090/gs-service/services/essi/view/whos-arctic/ArcGIS");
	//
	// }
	//
	// String queryString = request.getQueryString();
	//
	// if (queryString != null) {
	// rest = rest + "?" + queryString;
	// rest2 = rest2 + "?" + queryString;
	// }
	//
	// String ret = download(rest);
	// final String tt = rest2;
	// Thread t = new Thread() {
	// @Override
	// public void run() {
	//
	// String ret2 = download(tt);
	//
	// if (!ret.equals(ret2)) {
	// try {
	// File tmp1 = File.createTempFile("A", ".json");
	// FileOutputStream fos = new FileOutputStream(tmp1);
	// ByteArrayInputStream bis = new ByteArrayInputStream(ret.getBytes());
	// IOUtils.copy(bis, fos);
	// File tmp2 = File.createTempFile("B", ".json");
	// FileOutputStream fos2 = new FileOutputStream(tmp2);
	// ByteArrayInputStream bis2 = new ByteArrayInputStream(ret2.getBytes());
	// IOUtils.copy(bis2, fos2);
	//
	// System.err.println(
	// "DIFFERENCE IN REQUEST: \n" + tt + "\n\n" + tmp1.getAbsolutePath() + ":" + tmp2.getAbsolutePath() + "\n\n");
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	//
	// } else {
	// System.out.println("O.K.");
	// }
	//
	// }
	// };
	// t.start();
	//
	// ByteArrayInputStream stream = new ByteArrayInputStream(ret.getBytes());
	// return Response.status(Status.OK).type(MediaType.APPLICATION_JSON).entity(stream).build();

    }

    private String readResponse(String name) {
	InputStream stream = FeatureServerProxyHandler.class.getClassLoader().getResourceAsStream(name);
	ByteArrayOutputStream boas = new ByteArrayOutputStream();
	try {
	    IOUtils.copy(stream, boas);
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	return new String(boas.toByteArray());
    }

    private String download(String rest) {
	Downloader dd = new Downloader();
	Optional<String> string = dd.downloadOptionalString(rest);
	String ret = "";
	if (string.isPresent()) {
	    ret = string.get();
	} else {
	    ret = "ERROR";
	}
	return ret;
    }

    @Override
    public ValidationMessage validate(WebRequest request) throws GSException {
	ValidationMessage ret = new ValidationMessage();
	ret.setResult(ValidationResult.VALIDATION_SUCCESSFUL);
	return ret;
    }

}
