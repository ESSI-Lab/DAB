/**
 * 
 */
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

import org.w3c.dom.Node;

import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.count.DiscoveryCountResponse;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;

/**
 * @author Fabrizio
 */
public interface DatabaseFinder extends DatabaseProvider{

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

}
