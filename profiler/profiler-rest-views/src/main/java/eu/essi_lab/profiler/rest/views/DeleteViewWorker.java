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

import java.util.Optional;

import javax.ws.rs.core.Response.Status;

import eu.essi_lab.api.database.DatabaseWriter;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.RequestMessage;
import eu.essi_lab.messages.bond.View;
import eu.essi_lab.model.auth.GSUser;
import eu.essi_lab.model.exceptions.GSException;

/**
 * @author Fabrizio
 */
public class DeleteViewWorker extends ViewWorker {

    private View view;

    /**
     * @param message
     * @param view
     * @param builder
     * @throws GSException
     */
    public DeleteViewWorker(RequestMessage message, View view) throws GSException {
	super(message);
	this.view = view;
    }

    /**
     * @return
     */
    public String delete() throws RuntimeException, Exception {

	Optional<String> optViewId = getRequest().extractViewId();

	try {

	    //
	    // following checks are redundant, since they are previously made by the authorizer
	    //
	    String creator = view.getCreator();
	    String owner = view.getOwner();

	    GSUser user = getMessage().getCurrentUser().get();

	    if (creator != null && creator.equals(ViewWorker.getUserRootViewIdentifier(user))

		    && owner != null && owner.equals(user.getIdentifier())) {

		DatabaseWriter writer = getDatabaseWriter();

		writer.removeView(optViewId.get());

		return createMessage(Status.OK, "Successfully removed view: " + optViewId.get());

	    } else {

		throw new RuntimeException("User " + user.getIdentifier() + " is not authorized to remove view: " + optViewId);
	    }

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage());

	    throw new Exception("Error occurred while removing view " + optViewId.get() + ": " + e.getMessage());
	}
    }
}
