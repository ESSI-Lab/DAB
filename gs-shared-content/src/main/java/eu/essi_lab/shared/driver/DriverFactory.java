/**
 * 
 */
package eu.essi_lab.shared.driver;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
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

import java.util.HashMap;
import java.util.ServiceLoader;

import eu.essi_lab.cfga.gs.setting.driver.DriverSetting;
import eu.essi_lab.model.shared.SharedContent.SharedContentCategory;

/**
 * @author Fabrizio
 */
public class DriverFactory {

    /**
     * 
     */
    @SuppressWarnings("rawtypes")
    private static final HashMap<SharedContentCategory, ISharedRepositoryDriver> DRIVERS_MAP = new HashMap<>();

    private DriverFactory() {
    }

    /**
     * Get the driver according to the given <code>setting</code> {@link DriverSetting#getCategory()} and configures it
     * before to return it
     * 
     * @param setting
     * @param reuseExistent if <code>true</code> reuses an existent created driver, otherwise creates a new one
     * @return
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static ISharedRepositoryDriver getConfiguredDriver(DriverSetting setting, boolean reuseExistent) {
	
	SharedContentCategory category = setting.getCategory();
	
	ISharedRepositoryDriver driver = getDriver(category, reuseExistent);
	driver.configure(setting);

	return driver;
    }

    /**
     * Get the driver according to the given <code>category</code>
     * 
     * @param setting
     * @param reuseExistent if <code>true</code> reuses an existent created driver, otherwise creates a new one
     * @return
     */
    @SuppressWarnings("rawtypes")
    public static ISharedRepositoryDriver getDriver(SharedContentCategory category, boolean reuseExistent) {

	if (reuseExistent && DRIVERS_MAP.containsKey(category)) {

	    return DRIVERS_MAP.get(category);
	}

	ISharedRepositoryDriver driver = loadDriver(category);

	if (driver != null) {

	    DRIVERS_MAP.put(category, driver);
	}

	return driver;
    }

    @SuppressWarnings("rawtypes")
    private static ISharedRepositoryDriver loadDriver(SharedContentCategory category) {

	ServiceLoader<ISharedRepositoryDriver> drivers = ServiceLoader.load(ISharedRepositoryDriver.class);

	for (ISharedRepositoryDriver driver : drivers) {

	    if (driver.getCategory() == category) {
		return driver;
	    }
	}

	return null;
    }
}
