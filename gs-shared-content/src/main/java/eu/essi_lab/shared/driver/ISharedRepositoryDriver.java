package eu.essi_lab.shared.driver;

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

import eu.essi_lab.model.configuration.IGSConfigurable;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.shared.messages.SharedContentQuery;
import eu.essi_lab.shared.model.SharedContent;
import eu.essi_lab.shared.model.SharedContentType;
import java.util.List;
public interface ISharedRepositoryDriver extends IGSConfigurable {

    /**
     * Reads {@link SharedContent} with id identifier and of the provided type. If no element is found with the given identifier, null is
     * returned.
     *
     * @param identifier
     * @return
     * @throws GSException
     */
    SharedContent readSharedContent(String identifier, SharedContentType type) throws GSException;

    List<SharedContent> readSharedContent(SharedContentType type, SharedContentQuery query) throws GSException;

    void store(SharedContent sharedContent) throws GSException;

    Long count(SharedContentType type) throws GSException;

}
