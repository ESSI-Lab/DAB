package eu.essi_lab.api.database;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
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

import eu.essi_lab.api.database.Database.IdentifierType;
import eu.essi_lab.lib.utils.GSLoggerFactory;
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
     * Gets the {@link GSUser} with the provided identifier
     *
     * @param identifier the identifier of the user
     * @return the optional user
     * @throws GSException
     */
    public default Optional<GSUser> getUser(String identifier) throws GSException {

	try {

	    return getUsers().stream().filter(u -> u.getIdentifier().equals(identifier)).findFirst();

	} catch (Exception ex) {

	    GSLoggerFactory.getLogger(getClass()).error(ex);

	    throw GSException.createException(getClass(), "DatabaseGetUserError", ex);
	}
    }

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

    /**
     * Tests whether or not a {@link GSResource} identified by a {@link HarmonizedMetadata} with the supplied
     * <code>identifier</code> of type <code>identifierType</code> exists in the whole database
     *
     * @param identifierType
     * @param identifier
     * @return
     * @throws GSException if error occurs during the request processing
     */
    public default boolean resourceExists(IdentifierType identifierType, String identifier) throws GSException{
	
	return !getResources(identifierType, identifier).isEmpty();
    }

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
     * Special method that WAS used during the tag recovering phase. Normally different resources with same original id are not
     * allowed, but at the end of a non-first harvesting, it is common to have 2 copies of the same resource, from the
     * previous harvesting and from the current one.<br>
     * 
     * From GIP-423 this phase is no longer required, since resources with same original id of the same source
     * are no longer replaced (this was the reason why this phase was necessary, because the new resource with same
     * original id lacked the tags possibly present in the previous copy of the resource)
     *
     * @param originalIdentifier
     * @param source
     * @param includeDeleted
     * @return
     * @throws GSException
     */
    @Deprecated
    public List<GSResource> getResources(String originalIdentifier, GSSource source, boolean includeDeleted) throws GSException;

    /**
     * @param originalIdentifier
     * @param source
     * @param includeDeleted
     * @return
     * @throws GSException
     */
    public default GSResource getResource(String originalIdentifier, GSSource source, boolean includeDeleted) throws GSException {

	List<GSResource> resultsList = getResources(originalIdentifier, source, includeDeleted);
	if (!resultsList.isEmpty()) {

	    if (resultsList.size() > 1) {
		//
		// this should no longer happen from GIP-423
		//
		GSLoggerFactory.getLogger(getClass()).warn("Found {} resources with originalId [{}] from the source [{}] !!!",
			resultsList.size(), originalIdentifier, source.getUniqueIdentifier());
	    }

	    return resultsList.get(0);
	}

	return null;
    }

    /**
     * Verify if there is a resource that match the given
     * <code>originalIdentifier</code> and <code>source</code>.
     *
     * @param originalIdentifier
     * @param source
     * @return true if there is a resource who match given identifier and source
     * @throws GSException if error occurs during the request processing
     */
    public default boolean resourceExists(String originalIdentifier, GSSource source) throws GSException {

	return getResource(originalIdentifier, source) != null;
    }

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
    public default GSResource getResource(String originalIdentifier, GSSource source) throws GSException {

	return getResource(originalIdentifier, source, false);
    }
}
