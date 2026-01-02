package eu.essi_lab.profiler.rest.views;

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

import javax.ws.rs.core.Response.Status;

import org.json.JSONObject;

import eu.essi_lab.messages.RequestMessage;
import eu.essi_lab.messages.bond.View;
import eu.essi_lab.model.exceptions.GSException;

/**
 * @author Fabrizio
 */
public class PostViewWorker extends ViewWorker {

    private JSONObject jsonView;

    /**
     * @param message
     * @throws GSException
     */
    public PostViewWorker(RequestMessage message, JSONObject jsonView) throws GSException {
	super(message);
	this.jsonView = jsonView;
    }

    /**
     * @return
     */
    public String post() throws RuntimeException, GSException, Exception {

	View view = ViewMapper.mapView(jsonView, getMessage(), getDatabaseReader());

	getDatabaseWriter().store(view);

	return createMessage(Status.OK, getResponseMessage(view));
    }

    /**
     * @param view
     * @return
     */
    protected String getResponseMessage(View view) {

	return "View stored: " + view.getId();
    }
}
