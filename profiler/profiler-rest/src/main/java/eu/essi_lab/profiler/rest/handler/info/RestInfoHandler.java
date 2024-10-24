package eu.essi_lab.profiler.rest.handler.info;

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

import java.util.Arrays;

import javax.ws.rs.core.MediaType;

import org.json.JSONObject;
import org.json.XML;

import eu.essi_lab.lib.utils.JSONUtils;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.messages.web.KeyValueParser;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.pdk.handler.DefaultRequestHandler;

/**
 * @author Fabrizio
 */
public abstract class RestInfoHandler extends DefaultRequestHandler {

    /**
     * @param responseFormat
     * @return
     */
    public static ValidationMessage validateResponseFormat(String responseFormat) {

	ValidationMessage message = new ValidationMessage();
	message.setResult(ValidationResult.VALIDATION_SUCCESSFUL);

	boolean anyMatch = Arrays.asList(MessageFormat.values()).//
		stream().//
		anyMatch(p -> p.getFormat().equals(responseFormat));

	if (!anyMatch) {

	    message.setResult(ValidationResult.VALIDATION_FAILED);
	    message.setError(RestParameter.RESPONSE_FORMAT.getName() + " parameter must be one of the following: "
		    + Arrays.asList(MessageFormat.values()));
	    message.setLocator(RestParameter.RESPONSE_FORMAT.getName());
	}

	return message;
    }

    @Override
    public final String getStringResponse(WebRequest webRequest) throws GSException {

	String out = createXMLResponse(webRequest);

	return handleResponse(webRequest, out);
    }

    /**
     * @return
     */
    protected abstract String createXMLResponse(WebRequest webRequest) throws GSException;

    /**
     * @param webRequest
     * @param out
     * @return
     */
    private String handleResponse(WebRequest webRequest, String out) {

	KeyValueParser parser = new KeyValueParser(webRequest.getQueryString());
	String responseFormat = parser.getValue(RestParameter.RESPONSE_FORMAT.getName());

	if (parser.isValid(RestParameter.RESPONSE_FORMAT.getName()) && MessageFormat.fromFormat(responseFormat) == MessageFormat.JSON) {

	    JSONObject jsonObject = XML.toJSONObject(out);

	    JSONUtils.clearNSDeclarations(jsonObject);

	    out = jsonObject.toString(4);
	}

	return out;
    }

    @Override
    public MediaType getMediaType(WebRequest webRequest) {

	KeyValueParser parser = new KeyValueParser(webRequest.getQueryString());
	String responseFormat = parser.getValue(RestParameter.RESPONSE_FORMAT.getName());

	MediaType type = MediaType.APPLICATION_XML_TYPE;

	if (parser.isValid(RestParameter.RESPONSE_FORMAT.getName()) && MessageFormat.fromFormat(responseFormat) == MessageFormat.JSON) {

	    type = MediaType.APPLICATION_JSON_TYPE;
	}

	return type;
    }
}
