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

import org.json.JSONObject;

import eu.essi_lab.messages.RequestMessage;
import eu.essi_lab.messages.bond.View;
import eu.essi_lab.model.exceptions.GSException;

/**
 * @author Fabrizio
 */
public class PutViewWorker extends PostViewWorker {

    /**
     * @param message
     * @throws GSException
     */
    public PutViewWorker(RequestMessage message, JSONObject jsonView) throws GSException {

	super(message, jsonView);
    }

    /**
     * @return
     * @throws Exception
     * @throws RuntimeException
     */
    public String put() throws RuntimeException, GSException, Exception {

	String viewIdentifier = getMessage().getWebRequest().extractViewId().get();

	getDatabaseWriter().removeView(viewIdentifier);

	return super.post();
    }

    /**
     * @param view
     * @return
     */
    @Override
    protected String getResponseMessage(View view) {

	return "View updated: " + view.getId();
    }
}
