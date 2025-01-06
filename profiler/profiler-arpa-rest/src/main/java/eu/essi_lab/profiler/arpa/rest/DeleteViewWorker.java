package eu.essi_lab.profiler.arpa.rest;

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

import java.util.Optional;

import eu.essi_lab.api.database.DatabaseWriter;
import eu.essi_lab.messages.bond.View;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.exceptions.GSException;

public class DeleteViewWorker extends ViewWorker {

    public DeleteViewWorker(WebRequest request) throws GSException {
	super(request);
    }

    public String delete(String viewId) {
	try {
	    if (viewId == null || viewId.isEmpty()) {
		return errorMessage("Unable to extract view id from path");
	    }
	    Optional<View> optionalView = reader.getView(viewId);
	    if (optionalView.isPresent()) {
		View view = optionalView.get();
		String creator = view.getCreator();
		if (creator != null && creator.equals(CREATOR)) {
		    DatabaseWriter writer = createWriter();
		    writer.removeView(viewId);
		    return message("Successfully removed view: " + viewId);
		} else {
		    return errorMessage("User " + CREATOR + " is not authorized to remove view: " + viewId);
		}
	    } else {
		return errorMessage("View not found: " + viewId);
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	    return errorMessage("Unexpected exception removing view (" + viewId + "): " + e.getMessage());
	}

    }



}
