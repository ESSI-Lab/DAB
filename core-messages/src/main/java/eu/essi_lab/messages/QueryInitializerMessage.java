/**
 * 
 */
package eu.essi_lab.messages;

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

import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.model.GSProperty;

/**
 * @author Fabrizio
 */
public abstract class QueryInitializerMessage extends UserBondMessage {

    /**
     * 
     */
    private static final long serialVersionUID = -3346736605997668879L;
    public static final String PERMITTED_BOND = "permittedBond";
    public static final String NORMALIZED_BOND = "normalizedBond";

    /**
     * Gets the normalized bond. This is previously computed by the Query Initializer component by adding the source
     * bonds for each
     * permitted source to the original query bond and then normalizing it with respect to the sources.
     *
     * @return
     * @see IQueryInitializer
     */
    public Bond getNormalizedBond() {

	return getPayload().get(NORMALIZED_BOND, Bond.class);
    }

    /**
     * The Query Initializer component uses this method to set the normalized bond.
     *
     * @param bond
     * @see IQueryInitializer
     */
    public void setNormalizedBond(Bond bond) {

	getPayload().add(new GSProperty<Bond>(NORMALIZED_BOND, bond));
    }

    /**
     * Used by the query initializer to get the permitted bond. This is previously computed by the Request Authorization
     * Converter component
     * by adding the required source bonds.
     *
     * @return
     * @see IQueryInitializer
     */
    public Bond getPermittedBond() {

	return getPayload().get(PERMITTED_BOND, Bond.class);
    }

    /**
     * The Request Authorization Converter component uses this method to set the permitted bond according to the query
     * user.
     *
     * @param bond
     * @see IQueryInitializer
     */
    public void setPermittedBond(Bond bond) {

	getPayload().add(new GSProperty<Bond>(PERMITTED_BOND, bond));
    }

}
