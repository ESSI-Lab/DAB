/**
 * 
 */
package eu.essi_lab.profiler.os.handler.srvinfo;

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

import java.util.List;
import java.util.Optional;

import javax.ws.rs.core.MediaType;

import org.json.JSONObject;

import eu.essi_lab.iso.datamodel.classes.GeographicBoundingBox;
import eu.essi_lab.lib.net.nominatim.NominatimClient;
import eu.essi_lab.lib.net.nominatim.NominatimResponse;
import eu.essi_lab.lib.net.nominatim.query.FreeFormQuery;
import eu.essi_lab.lib.utils.StringUtils;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.ValidationMessage.ValidationResult;
import eu.essi_lab.messages.web.KeyValueParser;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.pdk.handler.DefaultRequestHandler;

/**
 * @author Fabrizio
 */
public class NominatimQueryHandler extends DefaultRequestHandler {

    @Override
    public ValidationMessage validate(WebRequest request) throws GSException {

	ValidationMessage message = new ValidationMessage();
	message.setResult(ValidationResult.VALIDATION_FAILED);

	String queryString = request.getQueryString();

	if (StringUtils.isReadable(queryString)) {

	    KeyValueParser parser = new KeyValueParser(queryString);

	    Optional<String> optQuery = parser.getOptionalValue("query");
	    if (optQuery.isPresent()) {
		message.setResult(ValidationResult.VALIDATION_SUCCESSFUL);
	    }
	}

	return message;
    }

    @Override
    public String getStringResponse(WebRequest request) throws GSException {

	KeyValueParser parser = new KeyValueParser(request.getQueryString());

	String queryString = parser.getValue("query");

	FreeFormQuery query = new FreeFormQuery();
	query.setQuery(queryString);

	NominatimClient client = new NominatimClient();

	JSONObject out = new JSONObject();

	try {

	    List<NominatimResponse> response = client.search(query);

	    if (!response.isEmpty()) {

		GeographicBoundingBox bbox = response.get(0).getBbox();

		JSONObject resp = new JSONObject();
		JSONObject jsonBbox = new JSONObject();
		jsonBbox.put("north", bbox.getNorth());
		jsonBbox.put("east", bbox.getEast());
		jsonBbox.put("south", bbox.getSouth());
		jsonBbox.put("west", bbox.getWest());

		resp.put("bbox", jsonBbox);
		out.put("response", resp);
	    }

	} catch (Exception ex) {

	    throw GSException.createException(getClass(), "OS_NOMINATIM_ERROR", ex);
	}

	Optional<String> optCallback = parser.getOptionalValue("callback");

	if (optCallback.isPresent()) {

	    return optCallback.get() + "(" + out.toString(3) + ")";
	}

	return out.toString(3);
    }

    @Override
    public MediaType getMediaType(WebRequest request) {

	KeyValueParser parser = new KeyValueParser(request.getQueryString());

	Optional<String> optCallback = parser.getOptionalValue("callback");

	if (optCallback.isPresent()) {

	    return MediaType.valueOf("application/javascript; charset=utf-8");
	}

	return MediaType.valueOf("text/json;charset=UTF-8");
    }
}
