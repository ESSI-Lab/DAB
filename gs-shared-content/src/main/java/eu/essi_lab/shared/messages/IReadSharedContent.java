package eu.essi_lab.shared.messages;

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

import eu.essi_lab.model.exceptions.GSException;
public interface IReadSharedContent<T> {

    /**
     * Reads the {@link eu.essi_lab.shared.model.SharedContent} identified by identifier. If no element with the provided identifier is
     * available, this method returns an instance of {@link eu.essi_lab.shared.messages.SharedContentReadResponse} that has an empty list
     * of {@link eu.essi_lab.shared.model.SharedContent}. Throws a {@link GSException} when some error oocours during content retrieval.
     *
     * @return
     * @throws GSException
     */
    public SharedContentReadResponse<T> read(String identifier) throws GSException;

    /**
     * Counts how many {@link eu.essi_lab.shared.model.SharedContent} elements are currently present in this shared repository. Throws a
     * {@link GSException} when it is not possible to count the elements.
     *
     * @return
     * @throws GSException
     */
    public Long count() throws GSException;

    /**
     * Reads the {@link eu.essi_lab.shared.model.SharedContent} elements which match the provided query object. If no element has
     * timestamp within the provided interval, this method returns an instance of {@link eu.essi_lab.shared.messages.SharedContentReadResponse}
     * that has an empty list of {@link eu.essi_lab.shared.model.SharedContent}. Throws a {@link GSException} when some error occurs
     * during content retrieval.
     *
     * @return
     * @throws GSException
     */
    public SharedContentReadResponse<T> read(SharedContentQuery query) throws GSException;

}
