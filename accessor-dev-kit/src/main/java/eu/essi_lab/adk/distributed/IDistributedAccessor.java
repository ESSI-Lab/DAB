package eu.essi_lab.adk.distributed;

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

import eu.essi_lab.adk.IDistributedQuerySubmitter;
import eu.essi_lab.cdk.query.IDistributedQueryConnector;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.configuration.IGSConfigurableComposed;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.ommdk.IResourceMapper;

public interface IDistributedAccessor extends IGSConfigurableComposed, IDistributedQuerySubmitter<GSResource> {
    public GSSource getSource();
    
    /**
     * 
     * @return
     * @throws GSException
     */
    public IDistributedQueryConnector getConnector() throws GSException;
    
    /**
     * 
     * @return
     * @throws GSException
     */
    public IResourceMapper getMapper(String schemeUri) throws GSException;

}
