package eu.essi_lab.request.executor;

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

import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.count.CountSet;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;

/**
 * At the very high level the main functionality of IDiscoverExecutor is the discovery of the resources matching the
 * user discovery queries (both count and retrieval).
 * 
 * @author boldrini
 */
public interface IDiscoveryExecutor extends IRequestExecutor<DiscoveryMessage, GSResource, CountSet, ResultSet<GSResource>> {

    /**
     * @param message
     * @return
     * @throws GSException
     */
    public CountSet count(DiscoveryMessage message) throws GSException;

    /**
     * @param message
     * @return
     * @throws GSException
     */
    public ResultSet<GSResource> retrieve(DiscoveryMessage message) throws GSException;

}
