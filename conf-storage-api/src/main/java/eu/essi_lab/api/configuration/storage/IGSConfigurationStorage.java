package eu.essi_lab.api.configuration.storage;

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

import eu.essi_lab.model.StorageUri;
import eu.essi_lab.model.configuration.composite.GSConfiguration;
import eu.essi_lab.model.exceptions.GSException;
public interface IGSConfigurationStorage {

    /**
     * This method stores the passed configuration to the remote storage. First the remote configuration file is locked, then it is updated
     * and finally it is unlocked.
     *
     * @param conf
     * @throws GSException when: the remote storage system is unavailable or the lock can't be obtained (i.e. another instance has already
     * locked it).
     */
    void transactionUpdate(GSConfiguration conf) throws GSException;

    /**
     * The method reads the {@link GSConfiguration}. No cache is assumed to be implemented here, so the actual remote call is invoked here.
     *
     * @return {@link GSConfiguration}
     * @throws {@link GSException} when the connection can't be established (e.g. for a network error, wrong username/password, etc.).
     */
    GSConfiguration read() throws GSException;

    /**
     * Checks if this implementation supports the storage referenced by the <code>dbUri</code> parameter.
     * <code>dbUri</code> parameter validation is not assumed to be implemented in this method (it is delegated to the
     * validate method)
     *
     * @param dbUri
     * @return <code>true</code> if this implementation supports the storage referenced by
     * the parameter, <code>false</code> otherwise
     */
    boolean supports(StorageUri dbUri);

    /**
     * This method checks connection to the storage referenced by the parameter. Input parameter validation is not assumed to be implemented
     * in this method.
     *
     * @param url
     * @return true if the implementing class can read/write {@link GSConfiguration} to the storage referenced by the parameter, false
     * otherwise.
     */
    boolean validate(StorageUri url);

    /**
     * @param url
     */
    void setStorageUri(StorageUri url);

    /**
     * @return
     */
    StorageUri getStorageUri();

}
