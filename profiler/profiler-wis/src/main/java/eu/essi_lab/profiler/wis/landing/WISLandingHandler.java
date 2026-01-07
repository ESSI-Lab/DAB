/**
 * 
 */
package eu.essi_lab.profiler.wis.landing;

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

import javax.ws.rs.core.MediaType;

import org.json.JSONObject;

import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.pdk.handler.DefaultRequestHandler;
import eu.essi_lab.profiler.wis.WISRequest;
import eu.essi_lab.profiler.wis.WISRequest.TopRequest;
import eu.essi_lab.profiler.wis.WISUtils;

/**
 * @author boldrini
 */
public class WISLandingHandler extends DefaultRequestHandler {

    private static final String WIS_HANDLER_ERROR = "WIS_HANDLER_ERROR";

    @Override
    public ValidationMessage validate(WebRequest request) throws GSException {

	ValidationMessage ret = new ValidationMessage();
	try {
	    WISRequest wr = new WISRequest(request);
	    TopRequest topRequest = wr.getTopRequest();
	    if (topRequest==null) {
		ret.setResult(ValidationResult.VALIDATION_SUCCESSFUL);		
	    } else {
		ret.setResult(ValidationResult.VALIDATION_FAILED);
	    }
	} catch (Exception e) {
	    ret.setResult(ValidationResult.VALIDATION_FAILED);
	}
 	return ret;
    }

    @Override
    public String getStringResponse(WebRequest webRequest) throws GSException {



	try {

	    String template = WISUtils.getResourceAsString("wis2box/landing.json");
	    // JSONObject json = new JSONObject(template);
	    // ret = json.toString();
	    String ret = WISUtils.filter(webRequest, template);
	    JSONObject json = new JSONObject(ret);
	    ret = json.toString();
	    return ret;

	} catch (

	Exception e) {
	    e.printStackTrace();

	    throw GSException.createException(//
		    getClass(), //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    WIS_HANDLER_ERROR, //
		    e);
	}

    }

    @Override
    public MediaType getMediaType(WebRequest webRequest) {

	return MediaType.APPLICATION_JSON_TYPE;
    }
}
