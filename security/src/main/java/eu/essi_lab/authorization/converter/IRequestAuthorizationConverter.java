package eu.essi_lab.authorization.converter;

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

import eu.essi_lab.messages.QueryInitializerMessage;
import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.model.exceptions.GSException;

/**
 * The Request Authorization Converter is responsible to return the authorized bond depending on the given message
 * (in particular the given user).
 * 
 * @author boldrini
 */
public interface IRequestAuthorizationConverter {

    /**
     * Generates the authorized bond, that is an OR bond containing all the sources that can be accessed by a
     * specific user.
     * The permitted bond is constructed by adding (in AND) the authorization bond to the original bond
     * <br/>
     * <br/>
     * E.g.<br/>
     * User bond: source1 AND temperature<br/>
     * Let's assume that user is permitted to discover from source1 and source2<br/>
     * Authorized bond: source1 OR source2
     * Permitted bond: (source1 AND temperature) AND (source1 OR source2)
     * 
     * @param message
     * @return the authorization bond
     * @throws GSException in case a problem during the request modification occurred that prevented the creation of the
     *         permitted bond.
     */
    public Bond generateAuthorizedBond(QueryInitializerMessage message) throws GSException;

}
