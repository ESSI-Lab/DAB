/**
 * 
 */
package eu.essi_lab.profiler.rest.views;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
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

import java.util.List;
import java.util.Optional;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.api.database.DatabaseReader;
import eu.essi_lab.api.database.factory.DatabaseProviderFactory;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.messages.ValidationMessage;
import eu.essi_lab.messages.bond.View;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.StorageInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.pdk.handler.DefaultRequestHandler;

/**
 * @author Fabrizio
 */
public class RestViewsSourcesHandler extends DefaultRequestHandler {

    @Override
    public ValidationMessage validate(WebRequest request) throws GSException {

	RestViewsValidator validator = new RestViewsValidator();

	return validator.validate(request);
    }

    @Override
    public String getStringResponse(WebRequest webRequest) throws GSException {

	StorageInfo databaseURI = ConfigurationWrapper.getStorageInfo();
	DatabaseReader reader = DatabaseProviderFactory.getReader(databaseURI);

	//
	// users can see only sources which are set as source property bond in its
	// root view
	//
	String userRootViewId = ViewWorker.getUserRootViewIdentifier(webRequest.getCurrentUser());

	Optional<View> optUserRootView = reader.getView(userRootViewId);

	if (!optUserRootView.isPresent()) {

	    builder = builder.status(Status.FORBIDDEN);

	    return new JSONArray().toString();
	}

	JSONArray out = new JSONArray();

	List<GSSource> sources = ConfigurationWrapper.getViewSources(optUserRootView.get());

	sources.forEach(s -> out.put(mapSource(s)));

	return out.toString();
    }

    /**
     * @param source
     * @return
     */
    private JSONObject mapSource(GSSource source) {

	JSONObject object = new JSONObject();
	object.put("id", source.getUniqueIdentifier());
	object.put("title", source.getLabel());

	return object;
    }

    @Override
    public MediaType getMediaType(WebRequest webRequest) {

	return new MediaType("application", "json");
    }
}
