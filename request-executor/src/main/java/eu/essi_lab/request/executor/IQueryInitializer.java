package eu.essi_lab.request.executor;

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

import eu.essi_lab.authorization.converter.IRequestAuthorizationConverter;
import eu.essi_lab.messages.GSMessage;
import eu.essi_lab.messages.QueryInitializerMessage;
import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.model.exceptions.GSException;
public interface IQueryInitializer {

    /**
     * Initializes the given discovery message, augmenting it with a new payload named {@link GSMessage#NORMALIZED_BOND}
     * of type {@link Bond} and generates the set of sources interested for the present discovery
     * 
     * @param message the discovery message containing the query bond to be initialized. The message should contain a
     *        payload named {@link GSMessage#BOND} of type {@link Bond}
     * @exception throws {@link GSException} in case a problem occurred during the normalization, such as the normal
     *            form could not be achieved
     */
    public void initializeQuery(QueryInitializerMessage message) throws GSException;

    /**
     * Sets the request authorization converter to use. This component will be initially called to transform the request
     * according to the user permissions.
     * 
     * @param requestAuthorizationConverter
     */
    public void setRequestAuthorizationConverter(IRequestAuthorizationConverter requestAuthorizationConverter);

}
