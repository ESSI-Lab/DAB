package eu.essi_lab.shared.resultstorage;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2022 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.io.File;

import eu.essi_lab.model.StorageUri;

/**
 * @author Fabrizio
 */
public abstract class ResultStorage {

    private StorageUri storageURI;

    /**
     * 
     */
    public ResultStorage() {
    }

    /**
     * @return
     */
    public StorageUri getResultStorageURI() {

	return storageURI;
    }

    /**
     * @param storageUri
     */
    public ResultStorage(StorageUri storageUri) {

	this.storageURI = storageUri;
    }

    /**
     * @param objectName
     * @param file
     * @throws Exception
     */
    public abstract void store(String objectName, File file) throws Exception;

    /**
     * @param objectName
     * @return
     */
    public abstract String getStorageLocation(String objectName);
}
