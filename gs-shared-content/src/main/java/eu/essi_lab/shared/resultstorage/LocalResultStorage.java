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
import eu.essi_lab.model.shared.SharedContent;
import eu.essi_lab.model.shared.SharedContent.SharedContentCategory;
import eu.essi_lab.model.shared.SharedContent.SharedContentType;
import eu.essi_lab.shared.driver.DriverFactory;
import eu.essi_lab.shared.driver.ISharedRepositoryDriver;

/**
 * @author Fabrizio
 */
public class LocalResultStorage extends ResultStorage {

    /**
     * @param resultStorageURI
     */
    public LocalResultStorage() {
    }

    /**
     * @param resultStorageURI
     */
    public LocalResultStorage(StorageUri storageURI) {

	super(storageURI);
    }

    @Override
    public void store(String objectName, File file) throws Exception {

	@SuppressWarnings("rawtypes")
	ISharedRepositoryDriver driver = DriverFactory.getDriver(SharedContentCategory.LOCAL_PERSISTENT, true);

	SharedContent<File> sharedContent = new SharedContent<>();

	sharedContent.setIdentifier(objectName);

	sharedContent.setType(SharedContentType.FILE_TYPE);

	sharedContent.setContent(file);

	driver.store(sharedContent);
    }

    @Override
    public String getStorageLocation(String objectName) {

	String location = getResultStorageURI().getUri();

	location = location + File.separator + SharedContentType.FILE_TYPE;

	location = location + File.separator + objectName;

	return location;
    }
}
