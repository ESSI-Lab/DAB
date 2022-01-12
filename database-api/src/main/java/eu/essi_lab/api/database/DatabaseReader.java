package eu.essi_lab.api.database;

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

import org.w3c.dom.Node;

import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.bond.View;
import eu.essi_lab.messages.count.DiscoveryCountResponse;
import eu.essi_lab.messages.count.SemanticCountResponse;
import eu.essi_lab.messages.sem.SemanticMessage;
import eu.essi_lab.messages.sem.SemanticResponse;
import eu.essi_lab.messages.stats.StatisticsMessage;
import eu.essi_lab.messages.stats.StatisticsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.auth.GSUser;
import eu.essi_lab.model.auth.UserBaseClient;
import eu.essi_lab.model.configuration.IGSConfigurable;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.ontology.GSKnowledgeResourceDescription;
import eu.essi_lab.model.ontology.GSKnowledgeScheme;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.HarmonizedMetadata;
public interface DatabaseReader extends DatabaseConsumer, IGSConfigurable {

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
	ORIGINAL
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
    List<String> getViewIdentifiers(int start, int count) throws GSException;

    /**
     * Gets the list of view identifiers created by a given creator
     *
     * @return
     * @throws GSException
     */
    List<String> getViewIdentifiers(int start, int count, String creator) throws GSException;

    /**
     * Get the {@link DiscoveryCountResponse} resulting from the supplied
     * <code>message</code>
     *
     * @param message
     * @return
     * @throws GSException if error occurs during the request processing
     * @see #discover(DiscoveryMessage)
     */
    DiscoveryCountResponse count(DiscoveryMessage message) throws GSException;

    /**
     * Performs a discovery query according to the supplied <code>message</code> and returns the resulting
     * <code>ResultSet&lt;GSResource&gt;</code> .<br>
     * The following <code>message</code> properties determines the content of the
     * <code>ResultSet&lt;GSResource&gt;</code>:
     * <ul>
     * <li>{@link DiscoveryMessage#getUserBond()}</li>
     * <li>{@link DiscoveryMessage#getNormalizedBond()}</li>
     * <li>{@link DiscoveryMessage#getPage()}</li>
     * <li>{@link DiscoveryMessage#isDeletedIncluded()}</li>
     * </ul>
     *
     * @param message
     * @return
     * @throws GSException if error occurs during the request processing
     */
    ResultSet<GSResource> discover(DiscoveryMessage message) throws GSException;
    
    /**
     * Performs a discovery query according to the supplied <code>message</code> and returns the resulting
     * <code>ResultSet&lt;Node&gt;</code> .<br>
     * The following <code>message</code> properties determines the content of the
     * <code>ResultSet&lt;Node&gt;</code>:
     * <ul>
     * <li>{@link DiscoveryMessage#getUserBond()}</li>
     * <li>{@link DiscoveryMessage#getNormalizedBond()}</li>
     * <li>{@link DiscoveryMessage#getPage()}</li>
     * <li>{@link DiscoveryMessage#isDeletedIncluded()}</li>
     * </ul>
     *
     * @param message
     * @return
     * @throws GSException if error occurs during the request processing
     */
    public ResultSet<Node> discoverNodes(DiscoveryMessage message) throws GSException;
    
    /**
     * Performs a discovery query according to the supplied <code>message</code> and returns the resulting
     * <code>ResultSet&lt;String&gt;</code> .<br>
     * The following <code>message</code> properties determines the content of the
     * <code>ResultSet&lt;String&gt;</code>:
     * <ul>
     * <li>{@link DiscoveryMessage#getUserBond()}</li>
     * <li>{@link DiscoveryMessage#getNormalizedBond()}</li>
     * <li>{@link DiscoveryMessage#getPage()}</li>
     * <li>{@link DiscoveryMessage#isDeletedIncluded()}</li>
     * </ul>
     *
     * @param message
     * @return
     * @throws GSException if error occurs during the request processing
     */
    public ResultSet<String> discoverStrings(DiscoveryMessage message) throws GSException;

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
     *
     * @param originalIdentifier given resource original identifier
     * @param source given source to check
     * @return the {@link GSResource} identified by the two parameters
     * @throws GSException if error occurs during the request processing
     * @see DatabaseWriter#store(GSResource)
     */
    GSResource getResource(String originalIdentifier, GSSource source) throws GSException;

    /**
     * Counts the {@link GSKnowledgeResourceDescription} which match the supplied
     * <code>message</code>
     *
     * @param message
     * @return
     * @throws GSException
     */
    SemanticCountResponse count(SemanticMessage message) throws GSException;

    /**
     * @param message
     * @return
     * @throws GSException
     */
    StatisticsResponse compute(StatisticsMessage message) throws GSException;

    /**
     * Get the {@link SemanticResponse} resulting from the supplied
     * <code>message</code>.<br>
     * If <code>message</code> has no {@link Page} set, no limitation on the semantic objects is applied
     *
     * @param message
     * @return
     * @throws GSException
     */
    SemanticResponse<GSKnowledgeResourceDescription> execute(SemanticMessage message) throws GSException;

    /**
     * @param scheme
     * @param subjectId
     * @return
     * @throws GSException
     */
    Optional<GSKnowledgeResourceDescription> getKnowlegdeResource(GSKnowledgeScheme scheme, String subjectId) throws GSException;

}
