/**
 * 
 */
package eu.essi_lab.profiler.wms.demo;

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
import java.io.InputStream;

import javax.ws.rs.core.MediaType;

import org.apache.commons.io.IOUtils;

import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.pdk.handler.DefaultRequestHandler;
import eu.essi_lab.profiler.wms.WMSRequest.Parameter;

/**
 * @author boldrini
 */
public class WMSDemoHandler extends DefaultRequestHandler {

    private static final String WMS_DEMO_HANDLER_ERROR = "WMS_DEMO_HANDLER_ERROR";

    @Override
    public ValidationMessage validate(WebRequest request) throws GSException {

	ValidationMessage ret = new ValidationMessage();
	try {
	    new WMSDemoRequest(request);
	    ret.setResult(ValidationResult.VALIDATION_SUCCESSFUL);
	} catch (Exception e) {
	    ret.setResult(ValidationResult.VALIDATION_FAILED);
	}

	return ret;
    }

    @Override
    public String getStringResponse(WebRequest webRequest) throws GSException {
	try {

	    WMSDemoRequest request = new WMSDemoRequest(webRequest);
	    String file = request.getParameterValue(Parameter.FILE);
	    String resource = "wms-demo/";
	    if (file == null || file.isEmpty()) {
		resource += "index.html";
	    } else {
		switch (file) {
		case "lib/leaflet.css":
		case "lib/require.js":
		case "lib/app.js":
		case "ol/style0.css":
		case "ol/style.css":
		case "ol/OpenLayers.js":
		case "ol/ol.css":
		case "ol/ol.css.map":
		case "ol/ol.js":
		case "ol/ol.js.map":
		    resource += file;
		    break;
		default:
		    break;
		}
	    }

	    InputStream stream = WMSDemoHandler.class.getClassLoader().getResourceAsStream(resource);
	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    IOUtils.copy(stream, baos);
	    stream.close();
	    String ret = IOUtils.toString(baos.toByteArray(), "UTF-8");
	    baos.close();
	    return ret;
	} catch (Exception e) {
	    e.printStackTrace();
	
	    throw GSException.createException(//
		    getClass(), //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    WMS_DEMO_HANDLER_ERROR, //
		    e);
	}

    }

    @Override
    public MediaType getMediaType(WebRequest webRequest) {

	return MediaType.TEXT_HTML_TYPE;
    }
}
