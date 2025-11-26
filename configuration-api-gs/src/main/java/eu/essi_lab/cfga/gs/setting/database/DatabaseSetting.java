package eu.essi_lab.cfga.gs.setting.database;

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

import java.util.Optional;

import eu.essi_lab.cfga.gui.extension.*;
import org.json.JSONObject;

import eu.essi_lab.cfga.EditableSetting;
import eu.essi_lab.cfga.gs.GSTabIndex;
import eu.essi_lab.cfga.option.Option;
import eu.essi_lab.cfga.setting.Setting;
import eu.essi_lab.model.StorageInfo;

/**
 * @author Fabrizio
 */
public class DatabaseSetting extends Setting implements EditableSetting {

    /**
     *
     */
    public static final String VOLATILE_DB_STORAGE_NAME = "Volatile";
    public static final String VOLATILE_DB_URI = "http://volatile-db";

    protected static final String DATABASE_NAME_OPTION_KEY = "dbName";
    protected static final String DATABASE_URI_OPTION_KEY = "dbUri";
    protected static final String DATABASE_USER_OPTION_KEY = "dbUser";
    protected static final String DATABASE_PWD_OPTION_KEY = "dbPassword";
    protected static final String DATABASE_TYPE_OPTION_KEY = "dbType";
    protected static final String CONFIG_FOLDER_OPTION_KEY = "configFolder";

    private static final String DATABASE_SETTING_ID = "databaseConfiguration";
    private static final String VOLATILE_DB_SETTING_ID = "volatileDatabaseConfiguration";

    public DatabaseSetting() {

	setCanBeDisabled(false);
	setName("Database settings");
	setDescription("Main database settings");

	setCanBeCleaned(false);

	//
	// volatile or normal
	//
	setSelectionMode(SelectionMode.SINGLE);

	//
	// Database database configuration, read-only
	//
	{
	    Setting volatileDb = new Setting();
	    volatileDb.setName("Volatile database");
	    volatileDb.setIdentifier(getVolatileDbSettingId());
	    String desc = "This configuration is not editable and " + //
		    "the underlyind database implementation is not persistent " + //
		    "and it is supposed to be used only for test purpose";
	    volatileDb.setDescription(desc);
	    volatileDb.setCanBeDisabled(false);
	    volatileDb.setEditable(false);

	    Option<String> dbNameOption = new Option<>(String.class);
	    dbNameOption.setLabel("Database name");
	    dbNameOption.setKey(DATABASE_NAME_OPTION_KEY);
	    dbNameOption.setValue(VOLATILE_DB_STORAGE_NAME);
	    dbNameOption.setVisible(false);

	    volatileDb.addOption(dbNameOption);

	    Option<String> uriOption = new Option<>(String.class);
	    uriOption.setLabel("Database Uri");
	    uriOption.setKey(DATABASE_URI_OPTION_KEY);
	    uriOption.setValue(VOLATILE_DB_URI);
	    uriOption.setVisible(false);

	    volatileDb.addOption(uriOption);

	    addSetting(volatileDb);
	}

	//
	// Database configuration, default
	//
	{
	    Setting dbSettings = new Setting();
	    dbSettings.setName("Database");
	    dbSettings.setIdentifier(getDbSettingId());
	    dbSettings.setCanBeDisabled(false);
	    dbSettings.setSelected(true); // default
	    dbSettings.enableCompactMode(false);
	    dbSettings.setEditable(false);

	    Option<String> uriOption = new Option<>(String.class);
	    uriOption.setLabel("Database URI");
	    uriOption.setKey(DATABASE_URI_OPTION_KEY);
	    uriOption.setRequired(true);
	    uriOption.setCanBeDisabled(false);
	    uriOption.setMultiValue(false);

	    dbSettings.addOption(uriOption);

	    Option<String> userOption = new Option<>(String.class);
	    userOption.setLabel("Database user");
	    userOption.setKey(DATABASE_USER_OPTION_KEY);
	    userOption.setRequired(true);
	    userOption.setCanBeDisabled(false);

	    dbSettings.addOption(userOption);

	    Option<String> pwdOption = new Option<>(String.class);
	    pwdOption.setLabel("Database password");
	    pwdOption.setKey(DATABASE_PWD_OPTION_KEY);
	    pwdOption.setRequired(true);
	    pwdOption.setCanBeDisabled(false);

	    dbSettings.addOption(pwdOption);

	    Option<String> configFolderOption = new Option<>(String.class);
	    configFolderOption.setLabel("Configuration folder");
	    configFolderOption.setKey(CONFIG_FOLDER_OPTION_KEY);
	    configFolderOption.setRequired(true);
	    configFolderOption.setCanBeDisabled(false);

	    dbSettings.addOption(configFolderOption);

	    Option<String> dbNameOption = new Option<>(String.class);
	    dbNameOption.setLabel("Database name");
	    dbNameOption.setKey(DATABASE_NAME_OPTION_KEY);
	    dbNameOption.setRequired(true);
	    dbNameOption.setCanBeDisabled(false);

	    dbSettings.addOption(dbNameOption);

	    Option<String> typeOption = new Option<>(String.class);
	    typeOption.setLabel("Database type");
	    typeOption.setKey(DATABASE_TYPE_OPTION_KEY);
	    typeOption.setRequired(false);
	    typeOption.setCanBeDisabled(false);

	    dbSettings.addOption(typeOption);

	    addSetting(dbSettings);
	}
    }

    /**
     * @author Fabrizio
     */
    public static class DatabaseComponentInfo extends TabPlaceholder {

	private final TabDescriptor descriptor;

	/**
	 *
	 */
	public DatabaseComponentInfo() {

	    setLabel("Database");

	    descriptor = TabDescriptorBuilder.get(DatabaseSetting.class).//
		    build();
	}

	/**
	 * @return
	 */
	public TabDescriptor getDescriptor() {

	    return descriptor;
	}
    }

    /**
     * @param vol
     */
    public DatabaseSetting(boolean vol) {

	this();
	setVolatile(vol);
    }

    /**
     * @param uri
     */
    public DatabaseSetting(StorageInfo uri) {

	this();

	setStorageUri(uri);
    }

    /**
     * @param object
     */
    public DatabaseSetting(JSONObject object) {

	super(object);
    }

    /**
     * @param object
     */
    public DatabaseSetting(String object) {

	super(object);
    }

    /**
     *
     */
    public void setVolatile(boolean set) {

	if (getVolatileDbSetting().isPresent()) {

	    getVolatileDbSetting().get().setSelected(set);
	    getDbSetting().setSelected(false);

	    return;
	}

	throw new UnsupportedOperationException("Attempting to set volatile after setting removal");
    }

    /**
     * @return
     */
    public boolean isVolatile() {

	if (getVolatileDbSetting().isPresent()) {

	    return getVolatileDbSetting().get().isSelected();
	}

	return false;
    }

    /**
     *
     */
    public void removeVolatileSettings() {

	if (getVolatileDbSetting().isPresent()) {

	    removeSetting(getVolatileDbSetting().get());
	}
    }

    /**
     * @param name
     */
    public void setDatabaseName(String name) throws UnsupportedOperationException {

	if (isVolatile() && !name.equals(VOLATILE_DB_STORAGE_NAME)) {

	    throw new UnsupportedOperationException("Attempting to edit a volatile database setting");
	}

	getDbSetting().getOption(DATABASE_NAME_OPTION_KEY, String.class).get().setValue(name);
    }

    /**
     * @return
     */
    public String getDatabaseName() {

	return getSelectedSetting().getOption(DATABASE_NAME_OPTION_KEY, String.class).get().getValue();
    }

    /**
     * @param uri
     */
    public void setDatabaseUri(String uri) {

	if (isVolatile() && !uri.equals(VOLATILE_DB_URI)) {

	    throw new UnsupportedOperationException("Attempting to edit a volatile database setting");
	}

	getDbSetting().getOption(DATABASE_URI_OPTION_KEY, String.class).get().setValue(uri);
    }

    /**
     * @return
     */
    public String getDatabaseUri() {

	return getSelectedSetting().getOption(DATABASE_URI_OPTION_KEY, String.class).get().getValue();
    }

    /**
     * @param user
     */
    public void setDatabaseUser(String user) {

	if (isVolatile()) {

	    throw new UnsupportedOperationException("Attempting to edit a volatile database setting");
	}

	getDbSetting().getOption(DATABASE_USER_OPTION_KEY, String.class).get().setValue(user);
    }

    /**
     * @return
     */
    public String getDatabaseUser() {

	if (isVolatile()) {

	    return null;
	}

	return getDbSetting().getOption(DATABASE_USER_OPTION_KEY, String.class).get().getValue();
    }

    /**
     * @param password
     */
    public void setDatabasePassword(String password) {

	if (isVolatile()) {

	    throw new UnsupportedOperationException("Attempting to edit a volatile database setting");
	}

	getDbSetting().getOption(DATABASE_PWD_OPTION_KEY, String.class).get().setValue(password);
    }

    /**
     * @return
     */
    public String getDatabasePassword() {

	if (isVolatile()) {

	    return null;
	}

	return getDbSetting().getOption(DATABASE_PWD_OPTION_KEY, String.class).get().getValue();
    }

    /**
     * @param password
     */
    public void setDatabaseType(String password) {

	if (isVolatile()) {

	    throw new UnsupportedOperationException("Attempting to edit a volatile database setting");
	}

	getDbSetting().getOption(DATABASE_TYPE_OPTION_KEY, String.class).get().setValue(password);
    }

    /**
     * @return
     */
    public Optional<String> getDatabaseType() {

	if (isVolatile()) {

	    return Optional.empty();
	}

	return Optional.ofNullable(getDbSetting().getOption(DATABASE_TYPE_OPTION_KEY, String.class).get().getValue());
    }

    /**
     * @param folder
     */
    public void setConfigurationFolder(String folder) {

	if (isVolatile()) {

	    throw new UnsupportedOperationException("Attempting to edit a volatile database setting");
	}

	getDbSetting().getOption(CONFIG_FOLDER_OPTION_KEY, String.class).get().setValue(folder);
    }

    /**
     * @return
     */
    public String getConfigurationFolder() {

	if (isVolatile()) {

	    return null;
	}

	return getDbSetting().getOption(CONFIG_FOLDER_OPTION_KEY, String.class).get().getValue();
    }

    /**
     * @param storageUri
     */
    public void setStorageUri(StorageInfo uri) {

	if (uri != null) {

	    setDatabaseName(uri.getName());
	    setDatabaseUri(uri.getUri());

	    if (!isVolatile()) {

		if (uri.getIdentifier() != null) {
		    setConfigurationFolder(uri.getIdentifier());
		}

		if (uri.getPassword() != null) {
		    setDatabasePassword(uri.getPassword());
		}

		if (uri.getUser() != null) {
		    setDatabaseUser(uri.getUser());
		}

		if (uri.getType().isPresent()) {
		    setDatabaseType(uri.getType().get());
		}
	    }
	}
    }

    /***
     * @return
     */
    public StorageInfo asStorageInfo() {

	StorageInfo storageUri = new StorageInfo(getDatabaseUri());

	storageUri.setName(getDatabaseName());

	if (!isVolatile()) {

	    storageUri.setIdentifier(getConfigurationFolder());
	    storageUri.setUser(getDatabaseUser());
	    storageUri.setPassword(getDatabasePassword());
	    storageUri.setType(getDatabaseType().orElse(null));
	}

	return storageUri;
    }

    /**
     *
     */
    public void hideDatabaseConfigurationName() {

	getDbSetting().setShowHeader(false);
    }

    /**
     *
     */
    public void hideDatabaseConfigurationFolderOption() {

	getDbSetting().getOption(CONFIG_FOLDER_OPTION_KEY, String.class).get().setVisible(false);
    }

    /**
     * @return
     */
    private Setting getSelectedSetting() {

	return isVolatile() ? getVolatileDbSetting().get() : getDbSetting();
    }

    private Setting getDbSetting() {

	return getSetting(getDbSettingId()).get();
    }

    private Optional<Setting> getVolatileDbSetting() {

	return getSetting(getVolatileDbSettingId());
    }

    protected String getDbSettingId() {

	return DATABASE_SETTING_ID;
    }

    protected String getVolatileDbSettingId() {

	return VOLATILE_DB_SETTING_ID;
    }
}
