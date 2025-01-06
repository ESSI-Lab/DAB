/**
 * 
 */
package eu.essi_lab.cfga.gs.setting.driver;

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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.json.JSONObject;

import eu.essi_lab.cfga.gs.setting.database.DatabaseSetting;
import eu.essi_lab.cfga.option.IntegerOptionBuilder;
import eu.essi_lab.cfga.option.Option;
import eu.essi_lab.cfga.setting.Setting;
import eu.essi_lab.model.shared.SharedContent.SharedContentCategory;

/**
 * @author Fabrizio
 */
public class SharedCacheDriverSetting extends DriverSetting {

    private static final String LOCAL_CACHE_SETTING_ID = "localCacheSetting";
    private static final String DATABASE_CACHE_SETTING_ID = "databaseCacheSetting";
    private static final String CACHE_RETENTION_TIME_KEY = "cacheRetentionTime";
    /**
     * 
     */
    public static final Integer DEFAULT_RETENTION_TIME = 12;

    /**
     * 
     */
    public SharedCacheDriverSetting() {

	super();

	setName("Cached repository settings");
	setDescription("This kind of repository is used to temporary store the results of the distributed queries. These records will be later retrieved if required for a 'get by id' request");
	setSelectionMode(SelectionMode.SINGLE);
	enableCompactMode(false);

	setCanBeCleaned(false);
	
	Option<Integer> retentionTimeOption = IntegerOptionBuilder.get().//
		withLabel("How long (in hours) resources are kept in cache").//
		withKey(CACHE_RETENTION_TIME_KEY).//
		withSingleSelection().//
		withValues(createValues()).//
		withSelectedValue(DEFAULT_RETENTION_TIME).//
		cannotBeDisabled().//
		build();

	addOption(retentionTimeOption);

	//
	//
	//

	Setting localCacheSetting = new Setting();
	localCacheSetting.setName("Local");
	localCacheSetting.setDescription("Cached repository implementation based on the local file system");
	localCacheSetting.setIdentifier(LOCAL_CACHE_SETTING_ID);
	localCacheSetting.setSelected(true);
	localCacheSetting.setCanBeDisabled(false);
	localCacheSetting.setEditable(false);

	addSetting(localCacheSetting);

	//
	// This db setting cannot be volatile since a cache on a volatile db
	// already exists, it is the local cache
	//
	DatabaseSetting dbCacheSetting = new DatabaseSetting();
	dbCacheSetting.setName("Database");
	dbCacheSetting.setDescription("Cached repository implementation based on a database");
	dbCacheSetting.setIdentifier(DATABASE_CACHE_SETTING_ID);
	dbCacheSetting.removeVolatileSettings();
	dbCacheSetting.setSelectionMode(SelectionMode.UNSET);
	dbCacheSetting.setCanBeDisabled(false);
	dbCacheSetting.setEditable(false);

	dbCacheSetting.hideDatabaseConfigurationName();
	
	addSetting(dbCacheSetting);
    }

    /**
     * @param object
     */
    public SharedCacheDriverSetting(JSONObject object) {

	super(object);
    }

    /**
     * @param object
     */
    public SharedCacheDriverSetting(String object) {

	super(object);
    }

    /**
     * @return
     */
    public SharedContentCategory getCategory() {

	Optional<DatabaseSetting> optional = getDatabaseCacheSetting();

	if (optional.isPresent() && optional.get().isSelected()) {

	    return SharedContentCategory.DATABASE_CACHE;
	}

	return SharedContentCategory.LOCAL_CACHE;
    }

    /**
     * @param category
     */
    @SuppressWarnings("incomplete-switch")
    public void setCategory(SharedContentCategory category) {

	switch (category) {
	case LOCAL_CACHE:
	    getLocalCacheSetting().ifPresent(s -> s.setSelected(true));
	    getDatabaseCacheSetting().ifPresent(s -> s.setSelected(false));
	    break;
	case DATABASE_CACHE:
	    getLocalCacheSetting().ifPresent(s -> s.setSelected(false));
	    getDatabaseCacheSetting().ifPresent(s -> s.setSelected(true));
	}
    }

    /**
     * @return
     */
    public int getSelectedRetentionTime() {

	Option<Integer> option = getOption(CACHE_RETENTION_TIME_KEY, Integer.class).get();

	return option.getSelectedValue();
    }

    /**
     * @return
     */
    public void selectRetentionTime(int time) {

	getOption(CACHE_RETENTION_TIME_KEY, Integer.class).get().setEnabled(true);
	getOption(CACHE_RETENTION_TIME_KEY, Integer.class).get().select(v -> v == time);
    }

    /**
     * @return
     */
    public void disableCacheCleaningTime() {

	getOption(CACHE_RETENTION_TIME_KEY, Integer.class).get().setEnabled(false);
    }

    /**
     * @return
     */
    public Optional<DatabaseSetting> getDatabaseCacheSetting() {

	return getSetting(DATABASE_CACHE_SETTING_ID, DatabaseSetting.class);
    }

    /**
     * @return
     */
    private Optional<Setting> getLocalCacheSetting() {

	return getSetting(LOCAL_CACHE_SETTING_ID);
    }

    /**
     * @return
     */
    private List<Integer> createValues() {

	return Arrays.asList(4, 8, 12, 16, 20, 24);
    }

    @Override
    protected List<SharedContentCategory> availableCategories() {

	return Arrays.asList(//
		SharedContentCategory.LOCAL_CACHE, //
		SharedContentCategory.DATABASE_CACHE);
    }
}
