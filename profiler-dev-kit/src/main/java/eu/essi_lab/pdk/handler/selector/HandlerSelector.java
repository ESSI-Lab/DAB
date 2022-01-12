package eu.essi_lab.pdk.handler.selector;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.pdk.Profiler;
import eu.essi_lab.pdk.handler.WebRequestHandler;
public class HandlerSelector {

    private List<SimpleEntry<WebRequestFilter, WebRequestHandler>> list;

    public HandlerSelector() {

	list = new ArrayList<>();
    }

    /**
     * Register the supplied {@link WebRequestHandler} with a {@link WebRequestFilter}
     *
     * @param filter the filter to register with the handler
     * @param handler the handler to select when <code>filter</code> accepts the request
     * @see #select(WebRequest)
     */
    public void register(WebRequestFilter filter, WebRequestHandler handler) {

	SimpleEntry<WebRequestFilter, WebRequestHandler> entry = new SimpleEntry<WebRequestFilter, WebRequestHandler>(//
		filter, //
		handler); //

	list.add(entry);
    }

    /**
     * Selects a registered {@link WebRequestHandler} according to <code>request</code>
     * 
     * @param request the request to filter
     * @return an {@link Optional} of {@link WebRequestHandler} possible empty in case none of the registered
     *         {@link WebRequestHandler}s is suitable to handle the <code>request</code>
     * @throws GSException
     */
    public Optional<WebRequestHandler> select(WebRequest request) throws GSException {

	return list.stream().//
		filter(entry -> filterAccept(entry.getKey(), request)).//
		map(SimpleEntry::getValue).//
		findFirst();
    }

    private boolean filterAccept(WebRequestFilter filter, WebRequest request) {
	try {

	    boolean accept = filter.accept(request);

	    if (accept) {
		return true;
	    }
	} catch (GSException e) {
	    e.printStackTrace();
	    GSLoggerFactory.getLogger(getClass()).error("Error occurred during request acceptance");
	    GSLoggerFactory.getLogger(getClass()).error(e.getErrorInfoList().toString(), e);
	}catch (Exception e) {
	    e.printStackTrace();
	    GSLoggerFactory.getLogger(getClass()).error("Unexpected error occurred during request acceptance");
	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);
	}

	return false;
    }
}
