package eu.essi_lab.adk;

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

import eu.essi_lab.adk.harvest.IHarvestedAccessor;
import eu.essi_lab.cdk.harvest.IHarvestedQueryConnector;
import eu.essi_lab.messages.listrecords.ListRecordsRequest;
import eu.essi_lab.messages.listrecords.ListRecordsResponse;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * Interface for harvesting metadata which provides a simplified version of the OAI-PMH harvesting interface
 *
 * @author ilsanto
 */
public interface IHarvestedQuerySubmitter<T> {

    /**
     * Retrieves records from remote service and generates a {@link ListRecordsResponse}.<br>
     * The returned {@link ListRecordsResponse} can optionally contains a {@link OAIPMHResuptionToken} to be used in the
     * next {@link #listRecords(ListRecordsRequest)} invocation. If the resumption
     * token is <code>null</code>, no additional records are available.<br>
     * This interface is parameterized so that it can be implemented both by {@link IHarvestedAccessor}s which set
     * <code>T</code>={@link GSResource}), and
     * by {@link IHarvestedQueryConnector}s which set set <code>T</code>={@link OriginalMetadata})
     *
     * @param request
     * @return
     * @throws GSException when the {@link ListRecordsRequest} can not be satisfied, i.e. when the
     *         {@link OAIPMHResuptionToken} can not be resolved, when the {@link ListRecordsRequest} contains
     *         {@link OAIPMHDateStamp}s and the
     *         connector does not support selective harvesting
     */
    public ListRecordsResponse<T> listRecords(ListRecordsRequest request) throws GSException;

    /**
     * @return
     */
    public List<String> listMetadataFormats() throws GSException;

    /**
     * @return
     * @throws GSException
     */
    public default boolean supportsIncrementalHarvesting() throws GSException {

	return false;
    }

    /**
     * @return
     * @throws GSException
     */
    public default boolean supportsResumedHarvesting() throws GSException {

	return true;
    }

    /**
     * @return
     * @throws GSException
     */
    public default boolean supportsRecovery() throws GSException {

	return true;
    }

    /**
     * @return
     */
    public default boolean supportsExpectedRecordsCount() {

	return false;
    }
}
