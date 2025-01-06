package eu.essi_lab.access.datacache;

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
import java.util.Optional;

import eu.essi_lab.access.datacache.DataCacheConnectorFactory.DataConnectorType;
import eu.essi_lab.cfga.gs.setting.TabIndex;
import eu.essi_lab.cfga.gs.setting.database.DatabaseSetting;
import eu.essi_lab.cfga.gs.setting.dc_connector.DataCacheConnectorSetting;
import eu.essi_lab.cfga.gui.extension.ComponentInfo;
import eu.essi_lab.cfga.gui.extension.TabInfo;
import eu.essi_lab.cfga.gui.extension.TabInfoBuilder;
import eu.essi_lab.cfga.option.Option;
import eu.essi_lab.cfga.option.OptionBuilder;
import eu.essi_lab.cfga.option.StringOptionBuilder;
import eu.essi_lab.lib.utils.LabeledEnum;

/**
 * @author Fabrizio
 */
public class DataCacheConnectorSettingImpl extends DataCacheConnectorSetting {

    /**
     * 
     */
    private static final String DATA_CONNECTOR_TYPE_OPTION_KEY = "dataCacheConnectorTypeOption";
    /**
     * 
     */
    private static final String CONNECTOR_OPTION_KEY = "dataCacheConnectorOption";
    /**
     * 
     */
    private static final String CACHE_STORAGE_SETTING_KEY = "dataCacheConnectorStorageOption";

    /**
     * 
     */
    public DataCacheConnectorSettingImpl() {

	setCanBeRemoved(false);
	setCanBeDisabled(false);
	enableCompactMode(false);

	setName("Data cache connector setting");

	//
	// Data connector type
	//

	Option<DataConnectorType> dcTypeOption = OptionBuilder.get(DataConnectorType.class).// .//
		withKey(DATA_CONNECTOR_TYPE_OPTION_KEY).//
		withLabel("Data connector type").//
		withSingleSelection().//
		withValues(LabeledEnum.values(DataConnectorType.class)).//
		withSelectedValue(LabeledEnum.values(DataConnectorType.class).get(0)).//
		required().//
		cannotBeDisabled().//
		build();

	addOption(dcTypeOption);

	//
	// Multiple connector options, one per row
	//
	// FLUSH_INTERVAL_MS:9999
	// MAX_BULK_SIZE:234
	// CACHED_DAYS:43
	//
	//

	String connectorOptionsValue = DataCacheConnector.FLUSH_INTERVAL_MS + ":" + DataCacheConnector.DEFAULT_FLUSH_INTERVAL_MS + "\n";
	connectorOptionsValue += DataCacheConnector.MAX_BULK_SIZE + ":" + DataCacheConnector.DEFAULT_MAX_BULK_SIZE + "\n";
	connectorOptionsValue += DataCacheConnector.CACHED_DAYS + ":" + DataCacheConnector.DEFAULT_CACHED_DAYS;

	Option<String> connectorOptions = StringOptionBuilder.get().//
		withKey(CONNECTOR_OPTION_KEY).//
		withLabel("Connector options"). //
		cannotBeDisabled().//
		required().//
		withTextArea().//
		withValue(connectorOptionsValue).//
		build();

	addOption(connectorOptions);

	//
	// Cache storage settings
	//

	DatabaseSetting cacheStorageSetting = new DatabaseSetting();

	cacheStorageSetting.setCanBeDisabled(false);
	cacheStorageSetting.setEditable(false);
	cacheStorageSetting.setEnabled(true);

	cacheStorageSetting.enableCompactMode(false);
	cacheStorageSetting.setName("Data cache storage settings");
	cacheStorageSetting.clearDescription();
	cacheStorageSetting.setIdentifier(CACHE_STORAGE_SETTING_KEY);
	cacheStorageSetting.removeVolatileSettings();
	cacheStorageSetting.setSelectionMode(SelectionMode.UNSET);
	cacheStorageSetting.hideDatabaseConfigurationName();
	cacheStorageSetting.hideDatabaseConfigurationFolderOption();

	addSetting(cacheStorageSetting);

	//
	// set the component extension
	//
	setExtension(new DataCacheConnectorSettingComponentInfo());
    }

    /**
     * @author Fabrizio
     */
    public static class DataCacheConnectorSettingComponentInfo extends ComponentInfo {

	/**
	 * 
	 */
	public DataCacheConnectorSettingComponentInfo() {

	    setComponentName(DataCacheConnectorSettingImpl.class.getName());

	    TabInfo tabInfo = TabInfoBuilder.get().//
		    withIndex(TabIndex.DATA_CACHE_CONNECTOR_SETTING.getIndex()).//
		    withShowDirective("Data cache").//
		    build();

	    setTabInfo(tabInfo);
	}
    }

    @Override
    public void setDataConnectorType(String type) {

	getOption(DATA_CONNECTOR_TYPE_OPTION_KEY, DataConnectorType.class).get()
		.setValue(LabeledEnum.valueOf(DataConnectorType.class, type).get());
    }

    @Override
    public void setDatabaseUri(String uri) {

	getSetting(CACHE_STORAGE_SETTING_KEY, DatabaseSetting.class).get().setDatabaseUri(uri);
    }

    @Override
    public void setDatabasePassword(String password) {

	getSetting(CACHE_STORAGE_SETTING_KEY, DatabaseSetting.class).get().setDatabasePassword(password);
    }

    @Override
    public void setDatabaseName(String name) {

	getSetting(CACHE_STORAGE_SETTING_KEY, DatabaseSetting.class).get().setDatabaseName(name);
    }

    @Override
    public void setDatabaseUser(String user) {

	getSetting(CACHE_STORAGE_SETTING_KEY, DatabaseSetting.class).get().setDatabaseUser(user);
    }

    //
    // FLUSH_INTERVAL_MS:9999
    // MAX_BULK_SIZE:234
    // CACHED_DAYS:43
    //
    @Override
    public void setOptionValue(String optionName, String newValue) {

	String value = getOption(CONNECTOR_OPTION_KEY, String.class).get().getValue();

	Optional<String> optKeyValue = Arrays.asList(value.split("\\n")).stream().filter(v -> v.startsWith(optionName)).findFirst();

	if (optKeyValue.isPresent()) {

	    String currentValue = optKeyValue.get().split(":")[1];

	    String newKeyValue = optKeyValue.get().replace(currentValue, newValue);

	    value = value.replace(optKeyValue.get(), newKeyValue);

	    getOption(CONNECTOR_OPTION_KEY, String.class).get().setValue(value);
	}
    }

    /**
     * 
     */
    public String getDataConnectorType() {

	return getOption(DATA_CONNECTOR_TYPE_OPTION_KEY, DataConnectorType.class).get().getValue().getLabel();
    }

    public String getDatabaseUri() {

	return getSetting(CACHE_STORAGE_SETTING_KEY, DatabaseSetting.class).get().getDatabaseUri();
    }

    public String getDatabasePassword() {

	return getSetting(CACHE_STORAGE_SETTING_KEY, DatabaseSetting.class).get().getDatabasePassword();
    }

    public String getDatabaseName() {

	return getSetting(CACHE_STORAGE_SETTING_KEY, DatabaseSetting.class).get().getDatabaseName();
    }

    public String getDatabaseUser() {

	return getSetting(CACHE_STORAGE_SETTING_KEY, DatabaseSetting.class).get().getDatabaseUser();
    }

    //
    // FLUSH_INTERVAL_MS:9999
    // MAX_BULK_SIZE:234
    // CACHED_DAYS:43
    //
    public Optional<String> getOptionValue(String optionName) {

	String value = getOption(CONNECTOR_OPTION_KEY, String.class).get().getValue();

	Optional<String> optKeyValue = Arrays.asList(value.split("\\n")).stream().filter(v -> v.startsWith(optionName)).findFirst();

	if (optKeyValue.isPresent()) {

	    return Optional.of(optKeyValue.get().split(":")[1]);
	}

	return Optional.empty();
    }

    public static void main(String[] args) {

	DataCacheConnectorSettingImpl impl = new DataCacheConnectorSettingImpl();

	System.out.println(impl);

    }

    @Override
    public String getType() {

	return "DataCacheConnectorSetting";
    }

}
