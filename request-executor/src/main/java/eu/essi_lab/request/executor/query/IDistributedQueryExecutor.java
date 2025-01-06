package eu.essi_lab.request.executor.query;

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

import java.util.AbstractMap.SimpleEntry;

import org.w3c.dom.Node;

import eu.essi_lab.identifierdecorator.IdentifierDecorator;
import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.ReducedDiscoveryMessage;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.count.DiscoveryCountResponse;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;

public interface IDistributedQueryExecutor extends IQueryExecutor {

    public IdentifierDecorator getIdentifierDecorator();

    public void setIdentifierDecorator(IdentifierDecorator identifierDecorator);

    public SimpleEntry<String, DiscoveryCountResponse> count(ReducedDiscoveryMessage message) throws GSException;

    public ResultSet<GSResource> retrieve(ReducedDiscoveryMessage message, Page page) throws GSException;
    
    public ResultSet<Node> retrieveNodes(ReducedDiscoveryMessage message, Page page) throws GSException;
    
    public ResultSet<String> retrieveStrings(ReducedDiscoveryMessage message, Page page) throws GSException;
}
