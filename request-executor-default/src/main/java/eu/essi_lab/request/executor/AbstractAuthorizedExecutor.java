/**
 * 
 */
package eu.essi_lab.request.executor;

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

import eu.essi_lab.authorization.xacml.XACMLAuthorizer;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.RequestMessage;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;

/**
 * @author Fabrizio
 */
public abstract class AbstractAuthorizedExecutor {

    /**
     * @param message
     * @param policy
     * @param errorId
     * @return
     * @throws GSException
     */
    public boolean isAuthorized(//
	    RequestMessage message, //
	    String errorId) throws GSException {

	boolean authorized = true;

	try {

	    XACMLAuthorizer authorizer = new XACMLAuthorizer();

	    authorized = authorizer.isAuthorized(message);

	    authorizer.close();

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(getClass()).error(e.getMessage(), e);

	    throw GSException.createException(//
		    getClass(), //
		    e.getMessage(), //
		    null, //
		    ErrorInfo.ERRORTYPE_INTERNAL, //
		    ErrorInfo.SEVERITY_ERROR, //
		    errorId, //
		    e);
	}

	return authorized;
    }
}
