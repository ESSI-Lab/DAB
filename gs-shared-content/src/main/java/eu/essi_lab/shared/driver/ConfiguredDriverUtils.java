package eu.essi_lab.shared.driver;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import org.json.JSONObject;

import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.setting.driver.DriverSetting;
import eu.essi_lab.cfga.gs.setting.driver.SharedCacheDriverSetting;
import eu.essi_lab.cfga.gs.setting.driver.SharedPersistentDriverSetting;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.shared.SharedContent;
import eu.essi_lab.model.shared.SharedContent.SharedContentType;

/**
 * @author Fabrizio
 */
public class ConfiguredDriverUtils {

    /**
     * @param status
     * @throws GSException
     */
    public static void storeToPersistentStorage(String contentId, JSONObject object) throws GSException {

	SharedPersistentDriverSetting setting = ConfigurationWrapper.getSharedPersistentDriverSetting();

	store(setting, contentId, object);
    }

    /**
     * @param status
     * @throws GSException
     */
    public static void storeToCachedStorage(String contentId, JSONObject object) throws GSException {

	SharedCacheDriverSetting setting = ConfigurationWrapper.getSharedCacheDriverSetting();

	store(setting, contentId, object);
    }

    /**
     * @throws GSException
     */
    private static void store(DriverSetting setting, String contentId, JSONObject object) throws GSException {

	@SuppressWarnings("rawtypes")
	ISharedRepositoryDriver driver = DriverFactory.getConfiguredDriver(setting, true);

	SharedContent<JSONObject> sharedContent = new SharedContent<>();

	sharedContent.setIdentifier(contentId);

	sharedContent.setType(SharedContentType.JSON_TYPE);

	sharedContent.setContent(object);

	driver.store(sharedContent);
    }
}
