/**
 * 
 */
package eu.essi_lab.profiler.os.handler.srvinfo;

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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MediaType;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Node;

import eu.essi_lab.lib.net.downloader.Downloader;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.StringUtils;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.messages.web.KeyValueParser;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.pdk.handler.DefaultRequestHandler;

/**
 * @author Fabrizio
 */
public class WMSLayersHandler extends DefaultRequestHandler {

    @Override
    public ValidationMessage validate(WebRequest request) throws GSException {

	ValidationMessage validationMessage = new ValidationMessage();
	validationMessage.setResult(ValidationResult.VALIDATION_SUCCESSFUL);

	return validationMessage;
    }

    @Override
    public String getStringResponse(WebRequest webRequest) throws GSException {

	KeyValueParser parser = new KeyValueParser(webRequest.getQueryString());

	String request = parser.getValue("request");

	Downloader downloader = new Downloader();
	JSONObject out = new JSONObject();

	JSONArray layersArray = new JSONArray();
	out.put("layers", layersArray);

	switch (request) {
	case "capabilities":

	    String endpoint = parser.getValue("endpoint");
	    String version = parser.getValue("version");
	    
	    if (!endpoint.startsWith("http")) {
		// relative endpoint
		HttpServletRequest req = webRequest.getServletRequest();
		String scheme = req.getScheme();
	        String serverName = req.getServerName();
	        int serverPort = req.getServerPort();
	        StringBuilder url = new StringBuilder();
	        url.append(scheme).append("://").append(serverName);
	        if ((scheme.equals("http") && serverPort != 80) || (scheme.equals("https") && serverPort != 443)) {
	            url.append(":").append(serverPort);
	        }
	        url.append(endpoint);
	        endpoint = url.toString();
	    }

	    String capRequest = endpoint + "?service=WMS&request=GetCapabilities&version=" + version;

	    Optional<String> response = downloader.downloadOptionalString(capRequest);
	    if (response.isPresent()) {

		String capabilitiesDoc = response.get();
		try {
		    XMLDocumentReader reader = new XMLDocumentReader(capabilitiesDoc);

		    List<Node> layers = Arrays.asList(reader.evaluateNodes("//*:Layer"));

		    for (Node layer : layers) {

			//
			// layer name
			//

			String layerTitle = reader.evaluateString(layer, "./*:Title/text()");
			
			// used as identifier
			String layerName = reader.evaluateString(layer, "./*:Name/text()"); 
			
			if(StringUtils.isEmpty(layerTitle)) {
			    
			    layerTitle = layerName;
			}

			if (!layerName.isEmpty()) {

			    JSONObject layerObject = new JSONObject();
			    layersArray.put(layerObject);

			    layerObject.put("title", layerTitle);
			    layerObject.put("name", layerName);

			    //
			    // layer bbox
			    //

			    String bbox = "";
			    String west = "";
			    String south = "";
			    String east = "";
			    String north = "";

			    if (version.equals("1.1.1")) {

				west = reader.evaluateString(layer, "/*:LatLonBoundingBox/@minx");
				south = reader.evaluateString(layer, "/*:LatLonBoundingBox/@miny");
				east = reader.evaluateString(layer, "/*:LatLonBoundingBox/@maxx");
				north = reader.evaluateString(layer, "/*:LatLonBoundingBox/@maxy");

			    } else { // 1.3.0

				west = reader.evaluateString(layer, "./*:EX_GeographicBoundingBox/*:westBoundLongitude");
				south = reader.evaluateString(layer, "./*:EX_GeographicBoundingBox/*:southBoundLatitude");
				east = reader.evaluateString(layer, "./*:EX_GeographicBoundingBox/*:eastBoundLongitude");
				north = reader.evaluateString(layer, "./*:EX_GeographicBoundingBox/*:northBoundLatitude");
			    }

			    bbox = west + "," + south + "," + east + "," + north;

			    layerObject.put("bbox", bbox);
			}
		    }

		} catch (Exception e) {

		    GSLoggerFactory.getLogger(getClass()).error(e);
		}
	    }

	    break;
	}

	Optional<String> optCallback = parser.getOptionalValue("callback");

	if (optCallback.isPresent()) {

	    return optCallback.get() + "(" + out.toString(3) + ")";
	}

	return out.toString(3);
    }

    @Override
    public MediaType getMediaType(WebRequest webRequest) {

	return MediaType.APPLICATION_JSON_TYPE;
    }
}
