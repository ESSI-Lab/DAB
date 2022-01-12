package eu.essi_lab.shared.model;

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

import eu.essi_lab.shared.driver.ISharedCacheRepositoryDriver;
import eu.essi_lab.shared.driver.ISharedPersistentRepositoryDriver;
import eu.essi_lab.shared.driver.ISharedRepositoryDriver;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;
public class PersistentGSSharedContentCategory implements IGSSharedContentCategory {

    @Override
    public String getName() {

	return "Persistent";

    }

    @Override
    public String getType() {
	return "PERSISTENT";
    }

    Iterator<ISharedPersistentRepositoryDriver> loadDrivers(Class clazz) {
	return ServiceLoader.load(clazz).iterator();
    }

    @Override
    public List<ISharedRepositoryDriver> getAvailableDrivers() {

	Iterator<ISharedPersistentRepositoryDriver> iterator = loadDrivers(ISharedPersistentRepositoryDriver.class);

	List<ISharedRepositoryDriver> returnList = new ArrayList<>();

	while (iterator.hasNext())
	    returnList.add(iterator.next());

	return returnList;

    }
}
