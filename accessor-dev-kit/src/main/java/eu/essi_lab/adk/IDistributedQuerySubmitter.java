package eu.essi_lab.adk;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
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

import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.ReducedDiscoveryMessage;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.count.DiscoveryCountResponse;
import eu.essi_lab.messages.termfrequency.TermFrequencyMap;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;

/**
 * @author ilsanto
 */
public interface IDistributedQuerySubmitter<T> {

    /**
     * Executes the count operation to the distributed {@link GSSource} according to the supplied <code>message</code>
     *
     * @param message
     * @return a {@link DiscoveryCountResponse} which holds the number of hits and optionally a {@link TermFrequencyMap}
     * @throws GSException
     */
    public DiscoveryCountResponse count(ReducedDiscoveryMessage message) throws GSException;

    /**
     * Executes the query operation to the distributed {@link GSSource} according to the supplied <code>message</code>
     * and <code>page</code>
     *
     * @param message
     * @param page
     * @return a {@link ResultSet}
     * @throws GSException
     */
    public ResultSet<T> query(ReducedDiscoveryMessage message, Page page) throws GSException;
}
