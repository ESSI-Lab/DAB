package eu.essi_lab.messages;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2022 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.model.GSProperty;

/**
 * Specialized type of {@link RequestMessage} which carries info about query parameters (provided as {@link Bond})
 * and an optional list of distinct queryables selected by the user
 * 
 * @author Fabrizio
 */
public abstract class UserBondMessage extends RequestMessage {

    /**
     * 
     */
    private static final long serialVersionUID = -2748397786444699512L;
    private static final String BOND = "bond";

    /**
     * Retrieves the bond as it has been formulated by the user (original query bond)
     *
     * @return
     */
    public Optional<Bond> getUserBond() {

	return Optional.ofNullable(getPayload().get(BOND, Bond.class));
    }

    /**
     * Used to set the user bond (original query bond).
     *
     * @param bond
     */
    public void setUserBond(Bond bond) {

	getPayload().add(new GSProperty<Bond>(BOND, bond));
    }

}
