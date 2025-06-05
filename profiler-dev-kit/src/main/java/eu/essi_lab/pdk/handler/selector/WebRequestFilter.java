package eu.essi_lab.pdk.handler.selector;

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

import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.pdk.Profiler;
import eu.essi_lab.pdk.handler.WebRequestHandler;

/**
 * A filter for {@link WebRequest}s. The filter is used by the {@link HandlerSelector} to register a supplied
 * {@link WebRequestHandler}
 * 
 * @see WebRequestHandler
 * @see HandlerSelector
 * @see Profiler#getSelector()
 * @author Fabrizio
 */
public interface WebRequestFilter {

    /**
     * Tests whether or not the supplied <code>request</code> is suitable for a registered {@link WebRequestHandler}
     * 
     * @param request a non <code>null</code> {@link WebRequest}
     * @return <code>true</code> if the registered {@link WebRequestHandler} can handle the supplied
     *         <code>request</code>, <code>false</code> otherwise
     * @see WebRequestHandler
     * @see HandlerSelector
     * @see Profiler#getSelector()
     */
    public boolean accept(WebRequest request) throws GSException;
}
