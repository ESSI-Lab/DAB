package eu.essi_lab.model.auth;

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

import java.util.List;
import java.util.Optional;

import eu.essi_lab.model.exceptions.GSException;

/**
 * @author Fabrizio
 */
public interface UserBaseClient {

    /**
     * Gets the {@link GSUser} with the provided identifier
     *
     * @param identifier the identifier of the user
     * @return the optional user
     * @throws GSException
     */
    Optional<GSUser> getUser(String userName) throws Exception;

    /**
     * Gets all the available {@link GSUser}s
     *
     * @return the users list, possible empty
     * @throws GSException
     */
    List<GSUser> getUsers() throws Exception;

    /**
     * Adds an user to the database
     * @param user
     * @throws Exception
     */
    void store(GSUser user) throws Exception;
}
