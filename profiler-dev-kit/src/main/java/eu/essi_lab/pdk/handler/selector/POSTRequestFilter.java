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

import java.util.Optional;

import eu.essi_lab.lib.utils.ClonableInputStream;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.exceptions.GSException;
public class POSTRequestFilter extends PathRequestFilter {

    /**
     * @param path
     */
    public POSTRequestFilter(String path) {

	super(path);
    }

    /**
     * @param path
     */
    public POSTRequestFilter(String path, boolean view) {

	super(path, view);
    }

    /**
     * Accepts <code>request</code> according to the {@link #accept(ClonableInputStream)} method and to the request path
     * 
     * @see #acceptPath(WebRequest)
     */
    public boolean accept(WebRequest request) throws GSException {

	Optional<Boolean> acceptPath = acceptPath(request);

	boolean postRequest = request.isPostRequest();

	return postRequest //
		&& accept(request.getBodyStream())//
		&& (!acceptPath.isPresent() || acceptPath.get());
    }

    /**
     * Default implementation which returns always <code>true</code>
     * 
     * @param content
     * @return
     */
    protected boolean accept(ClonableInputStream content) {

	return true;
    }
}
