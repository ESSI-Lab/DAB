package eu.essi_lab.api.database;

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

import eu.essi_lab.api.database.factory.DatabaseProviderFactory;
import eu.essi_lab.model.StorageUri;
import eu.essi_lab.model.exceptions.GSException;
public interface DatabaseProvider extends DatabaseClient {

    /**
     * Initializes a data base instance with the given <code>dbUri</code> and and <code>suiteIdentifier</code>
     *
     * @param dbUri
     * @param suiteIdentifier an available suite identifier, or <code>null</code> if not available
     * @return the suite identifier of this data base instance. Corresponds to <code>suiteIdentifier</code> if not
     *         <code>null</code>,
     *         otherwise a new identifier must be returned
     * @throws GSException if the initialization fails
     */
    public String initialize(StorageUri dbUri, String suiteIdentifier) throws GSException;

    /**
     * A possible implementation can execute some code which release some resources.<br>
     * After this method calling, the {@link Database} provided by this provider is no longer usable
     * 
     * @throws GSException
     */
    public void release() throws GSException;

}
