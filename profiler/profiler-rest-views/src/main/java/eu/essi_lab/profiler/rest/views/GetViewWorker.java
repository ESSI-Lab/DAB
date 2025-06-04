package eu.essi_lab.profiler.rest.views;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.json.JSONArray;
import org.json.JSONObject;

import eu.essi_lab.api.database.GetViewIdentifiersRequest;
import eu.essi_lab.messages.RequestMessage;
import eu.essi_lab.messages.bond.View;
import eu.essi_lab.messages.bond.View.ViewVisibility;
import eu.essi_lab.model.exceptions.GSException;

/**
 * @author Fabrizio
 */
public class GetViewWorker extends ViewWorker {

    private Optional<View> optView;

    /**
     * @param message
     * @param optView
     * @throws GSException
     */
    public GetViewWorker(RequestMessage message, Optional<View> optView) throws GSException {

	super(message);
	this.optView = optView;
    }

    /**
     * @return
     * @throws GSException
     */
    public String get() throws IllegalArgumentException, GSException {

	//
	// List of views
	//
	if (!optView.isPresent()) {

	    List<String> privateUserViewIds = new ArrayList<String>();
	    List<String> publicViewIds = new ArrayList<String>();

	    privateUserViewIds = getDatabaseReader().getViewIdentifiers(

		    GetViewIdentifiersRequest.create(//

			    ViewWorker.getUserRootViewIdentifier(getMessage().getCurrentUser().get()), //
			    getRequest().getCurrentUser().getIdentifier(), //
			    ViewVisibility.PRIVATE)

	    );

	    publicViewIds = getDatabaseReader().getViewIdentifiers(

		    GetViewIdentifiersRequest.create(//

			    ViewWorker.getUserRootViewIdentifier(getMessage().getCurrentUser().get()), //
			    ViewVisibility.PUBLIC));

	    List<String> allowedViewIds = new ArrayList<>(privateUserViewIds);
	    allowedViewIds.addAll(publicViewIds);

	    int fromIndex = Math.min(allowedViewIds.size(), getMessage().getPage().getStart() - 1);
	    int toIndex = Math.min(allowedViewIds.size(), getMessage().getPage().getStart() - 1 + getMessage().getPage().getSize());

	    allowedViewIds = allowedViewIds.subList(fromIndex, toIndex);

	    JSONObject out = new JSONObject();

	    JSONArray viewArray = new JSONArray();

	    out.put("size", allowedViewIds.size());
	    out.put("start", getMessage().getPage().getStart());
	    out.put("count", getMessage().getPage().getSize());
	    out.put("views", viewArray);

	    for (String curViewId : allowedViewIds) {

		Optional<View> optionalView = getDatabaseReader().getView(curViewId);

		View view = optionalView.get();

		JSONObject viewObject = ViewMapper.mapView(view, getMessage());

		viewArray.put(viewObject);
	    }

	    return out.toString();
	}

	//
	// View identifier
	//

	JSONObject viewObject = ViewMapper.mapView(optView.get(), getMessage());

	return viewObject.toString();
    }
}
