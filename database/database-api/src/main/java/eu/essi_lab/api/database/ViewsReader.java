/**
 * 
 */
package eu.essi_lab.api.database;

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

import java.util.List;
import java.util.Optional;

import eu.essi_lab.messages.bond.View;
import eu.essi_lab.model.exceptions.GSException;

/**
 * @author Fabrizio
 */
public interface ViewsReader {

    /**
     * /**
     * Gets the view associated with the given view identifier.
     *
     * @param viewId the view identifier
     * @return the optional view
     * @throws GSException
     */
    Optional<View> getView(String viewId) throws GSException;

    /**
     * Get all the available views
     * 
     * @return
     * @throws GSException
     */
    List<View> getViews() throws GSException;

    /**
     * Gets the list of view identifiers
     *
     * @return
     * @throws GSException
     */
    List<String> getViewIdentifiers(GetViewIdentifiersRequest request) throws GSException;
}
