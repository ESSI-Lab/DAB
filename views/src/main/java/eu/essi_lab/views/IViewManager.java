package eu.essi_lab.views;

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

import java.util.List;
import java.util.Optional;

import eu.essi_lab.messages.bond.View;
import eu.essi_lab.model.exceptions.GSException;

/**
 * The {@link IViewManager} is used to implement CRUD of views.
 * A view is composed by:
 * - an id
 * - a label
 * - a bond
 * The {@link IViewManager} can be used by:
 * - The Query Initializer during the discovery. If a view id is present in the discovery message, then the Query
 * Initializer uses the View Manager to resolve it to a bond to be added to the user original query.
 * - The REST service interface, to manage views (add, list, delete, ...)
 * 
 * @author boldrini
 */
public interface IViewManager {

    /**
     * Gets the view associated with the given view identifier, resolved (inner views are converted into concrete
     * bonds).
     * 
     * @param viewId the view identifier
     * @return the optional resolved view
     * @throws GSException
     */
    public Optional<View> getResolvedView(String viewId) throws GSException;

    /**
     * Gets the view associated with the given view identifier, not resolved (inner views are returned as initially
     * defined).
     * 
     * @param viewId the view identifier
     * @return the optional view
     * @throws GSException
     */
    public Optional<View> getView(String viewId) throws GSException;

    /**
     * Puts the given bond associated with the view identifier, overwriting a possible existing view with the same
     * identifier
     * 
     * @param viewId
     * @param view
     * @throws GSException
     */
    public void putView(View view) throws GSException;

    /**
     * Removes the view associated with the given identifier
     * 
     * @param viewId
     * @throws GSException
     */
    public void removeView(String viewId) throws GSException;

    /**
     * Gets the list of view identifiers
     * 
     * @return
     * @throws GSException
     */
    public List<String> getViewIdentifiers(int start, int count) throws GSException;

}
