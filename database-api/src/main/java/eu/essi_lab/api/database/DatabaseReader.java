package eu.essi_lab.api.database;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2024 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.auth.GSUser;
import eu.essi_lab.model.auth.UserBaseClient;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.HarmonizedMetadata;

/**
 * @author Fabrizio
 */
public interface DatabaseReader extends DatabaseProvider, UserBaseClient {

    /**
     * @author Fabrizio
     */
    public enum IdentifierType {

	/**
	 * 
	 */
	PUBLIC,
	/**
	 * 
	 */
	PRIVATE,
	/**
	 * 
	 */
	ORIGINAL,
	/**
	 * 
	 */
	OAI_HEADER
    }

    /**
     * Gets the {@link GSUser} with the provided identifier
     *
     * @param identifier the identifier of the user
     * @return the optional user
     * @throws GSException
     */
    Optional<GSUser> getUser(String userName) throws GSException;

    /**
     * Gets all the available {@link GSUser}s
     *
     * @return the users list, possible empty
     * @throws GSException
     */
    List<GSUser> getUsers() throws GSException;

    /**
     * Gets the view associated with the given view identifier.
     *
     * @param viewId the view identifier
     * @return the optional view
     * @throws GSException
     */
    Optional<View> getView(String viewId) throws GSException;

    /**
     * Gets the list of view identifiers
     *
     * @return
     * @throws GSException
     */
    List<String> getViewIdentifiers(GetViewIdentifiersRequest request) throws GSException;

    /**
     * Tests whether or not a {@link GSResource} identified by a {@link HarmonizedMetadata} with the supplied
     * <code>identifier</code> of type <code>identifierType</code> exists in the whole database
     *
     * @param identifierType
     * @param identifier
     * @return
     * @throws GSException if error occurs during the request processing
     */
    public boolean resourceExists(IdentifierType identifierType, String identifier) throws GSException;

    /**
     * Get the {@link GSResource} identified by a {@link HarmonizedMetadata} with the given
     * <code>identifier</code> of type <code>identifierType</code>
     *
     * @param identifierType
     * @param identifier
     * @return the {@link GSResource} identified by a {@link HarmonizedMetadata} with the given
     *         <code>identifier</code> according to the supplied <code>identifierType</code> or
     *         <code>null</code> if none is found
     * @throws GSException if error occurs during the request processing
     * @see DatabaseWriter#store(GSResource)
     */
    public List<GSResource> getResources(IdentifierType identifierType, String identifier) throws GSException;

    /**
     * 
     * @param originalIdentifier
     * @param source
     * @param includeDeleted
     * @return
     * @throws GSException
     */
    public List<GSResource> getResources(String originalIdentifier, GSSource source, boolean includeDeleted) throws GSException;

    /**
     * @param originalIdentifier
     * @param source
     * @param includeDeleted
     * @return
     * @throws GSException
     */
    public GSResource getResource(String originalIdentifier, GSSource source, boolean includeDeleted) throws GSException;

    /**
     * Verify if there is a resource that match the given
     * <code>originalIdentifier</code> and <code>source</code>.
     *
     * @param originalIdentifier
     * @param source
     * @return true if there is a resource who match given identifier and source
     * @throws GSException if error occurs during the request processing
     */
    public boolean resourceExists(String originalIdentifier, GSSource source) throws GSException;

    /**
     * Get the {@link GSResource} identified by the given
     * <code>originalIdentifier</code> and <code>source</code>.
     * This method searches for resources with the provided original identifier
     * in the whole DB, included the temporary folders
     *
     * @param originalIdentifier given resource original identifier
     * @param source given source to check
     * @return the {@link GSResource} identified by the two parameters
     * @throws GSException if error occurs during the request processing
     * @see DatabaseWriter#store(GSResource)
     */
    public GSResource getResource(String originalIdentifier, GSSource source) throws GSException;
}
