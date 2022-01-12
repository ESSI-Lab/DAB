package eu.essi_lab.adk.harvest;

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

import eu.essi_lab.adk.IHarvestedQuerySubmitter;
import eu.essi_lab.cdk.harvest.IHarvestedQueryConnector;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.configuration.IGSConfigurableComposed;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.ommdk.IResourceMapper;

public interface IHarvestedAccessor extends IGSConfigurableComposed, IHarvestedQuerySubmitter<GSResource> {
    public ListRecordsResponse<GSResource> listRecords(ListRecordsRequest request) throws GSException;

    /**
     * Returns the {@link GSSource} of this accessor
     * 
     * @return a non <code>null</code> {@link GSSource}
     */
    public GSSource getSource();

    /**
     * @return
     * @throws GSException
     */
    public IHarvestedQueryConnector getConnector() throws GSException;

    /**
     * @return
     * @throws GSException
     */
    public IResourceMapper getMapper(String schemeUri) throws GSException;
}
