/**
 * 
 */
package eu.essi_lab.cfga.gs.setting.driver;

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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.json.JSONObject;

import eu.essi_lab.cfga.gs.setting.database.DatabaseSetting;
import eu.essi_lab.model.shared.SharedContent.SharedContentCategory;

/**
 * @author Fabrizio
 */
public class SharedPersistentDriverSetting extends DriverSetting {

    private static final String LOCAL_PERSISTENT_SETTING_ID = "localPersistentSetting";
    private static final String ES_PERSISTENT_SETTING_ID = "esPersistentSetting";

    public SharedPersistentDriverSetting() {

	super();

	setName("Persistent repository settings");
	setSelectionMode(SelectionMode.SINGLE);
	setDescription("This kind of repository is used to permanently store the status of the asynchronous download");

	setCanBeCleaned(false);

	//
	//
	//

	LocalFolderSetting localFolderSetting = new LocalFolderSetting();
	localFolderSetting.setName("Local");
	localFolderSetting.setDescription("Persistent repository implementation based on the local file system");
	localFolderSetting.setIdentifier(LOCAL_PERSISTENT_SETTING_ID);
	localFolderSetting.setSelected(true);

	addSetting(localFolderSetting);

	//
	//
	//

	DatabaseSetting esPersistentSetting = new DatabaseSetting();
	esPersistentSetting.setShowHeader(true);
	esPersistentSetting.setName("Elasticsearch");
	esPersistentSetting.setIdentifier(ES_PERSISTENT_SETTING_ID);
	esPersistentSetting.setDescription("Persistent repository implementation based on Elasticsearch");
	esPersistentSetting.removeVolatileSettings();
	esPersistentSetting.setSelectionMode(SelectionMode.UNSET);
	esPersistentSetting.setCanBeDisabled(false);
	esPersistentSetting.setEditable(false);
	esPersistentSetting.hideDatabaseConfigurationName();
	esPersistentSetting.hideDatabaseConfigurationFolderOption();

	addSetting(esPersistentSetting);
    }

    /**
     * @param object
     */
    public SharedPersistentDriverSetting(JSONObject object) {

	super(object);
    }

    /**
     * @param object
     */
    public SharedPersistentDriverSetting(String object) {

	super(object);
    }

    /**
     * @return
     */
    public SharedContentCategory getCategory() {

	Optional<DatabaseSetting> optional = getElasticSearchSetting();

	if (optional.isPresent() && optional.get().isSelected()) {

	    return SharedContentCategory.ELASTIC_SEARCH_PERSISTENT;
	}

	return SharedContentCategory.LOCAL_PERSISTENT;
    }

    /**
     * @param category
     */
    @SuppressWarnings("incomplete-switch")
    public void setCategory(SharedContentCategory category) {

	switch (category) {
	case LOCAL_PERSISTENT:
	    getLocalPersistentSetting().ifPresent(s -> s.setSelected(true));
	    getElasticSearchSetting().ifPresent(s -> s.setSelected(false));
	    break;
	case ELASTIC_SEARCH_PERSISTENT:
	    getLocalPersistentSetting().ifPresent(s -> s.setSelected(false));
	    getElasticSearchSetting().ifPresent(s -> s.setSelected(true));
	}
    }

    /**
     * @return
     */
    public Optional<DatabaseSetting> getElasticSearchSetting() {

	return getSetting(ES_PERSISTENT_SETTING_ID, DatabaseSetting.class);
    }

    /**
     * @return
     */
    public Optional<LocalFolderSetting> getLocalPersistentSetting() {

	return getSetting(LOCAL_PERSISTENT_SETTING_ID, LocalFolderSetting.class);
    }

    @Override
    protected List<SharedContentCategory> availableCategories() {

	return Arrays.asList(//
		SharedContentCategory.LOCAL_PERSISTENT, //
		SharedContentCategory.ELASTIC_SEARCH_PERSISTENT);
    }
}
