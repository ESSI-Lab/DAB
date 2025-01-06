package eu.essi_lab.request.executor;

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

import org.w3c.dom.Node;

import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.count.CountSet;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.request.executor.query.IQueryExecutor;

/**
 * Delegates the incoming query request to a set of query submitters and collect back the results.
 * 
 * @author boldrini
 */
public interface IDistributor {

    /**
     * Counts the results of the discovery request represented by the given message
     * 
     * @param message
     * @return the count set
     * @throws GSException
     */
    CountSet count(DiscoveryMessage message) throws GSException;

    /**
     * Retrieves the results of the discovery request represented by the given message
     * 
     * @param message
     * @return
     * @throws GSException
     */
    ResultSet<GSResource> retrieve(DiscoveryMessage message) throws GSException;
    
    /**
     * Retrieves the results of the discovery request represented by the given message
     * 
     * @param message
     * @return
     * @throws GSException
     */
    ResultSet<Node> retrieveNodes(DiscoveryMessage message) throws GSException;
    
    /**
     * Retrieves the results of the discovery request represented by the given message
     * 
     * @param message
     * @return
     * @throws GSException
     */
    ResultSet<String> retrieveStrings(DiscoveryMessage message) throws GSException;

    /**
     * Sets an ordered list of query submitters to forward the discovery request to.
     * 
     * @param querySubmitters
     */
    void setQuerySubmitters(List<? extends IQueryExecutor> querySubmitters);

}
