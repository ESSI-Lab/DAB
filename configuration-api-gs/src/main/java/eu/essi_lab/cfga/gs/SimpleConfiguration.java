/**
 * 
 */
package eu.essi_lab.cfga.gs;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2024 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import eu.essi_lab.cfga.ConfigurableLoader;
import eu.essi_lab.cfga.gs.setting.CredentialsSetting;
import eu.essi_lab.cfga.gs.setting.DownloadSetting;
import eu.essi_lab.cfga.gs.setting.GDCSourcesSetting;
import eu.essi_lab.cfga.gs.setting.ProfilerSetting;
import eu.essi_lab.cfga.gs.setting.SchedulerViewSetting;
import eu.essi_lab.cfga.gs.setting.SourcePrioritySetting;
import eu.essi_lab.cfga.gs.setting.SystemSetting;
import eu.essi_lab.cfga.gs.setting.augmenter.AugmenterSetting;
import eu.essi_lab.cfga.gs.setting.database.DatabaseSetting;
import eu.essi_lab.cfga.gs.setting.database.SourceStorageSetting;
import eu.essi_lab.cfga.gs.setting.dc_connector.DataCacheConnectorSetting;
import eu.essi_lab.cfga.gs.setting.dc_connector.DataCacheConnectorSettingLoader;
import eu.essi_lab.cfga.gs.setting.harvesting.HarvestingSetting;
import eu.essi_lab.cfga.gs.setting.oauth.OAuthSetting;
import eu.essi_lab.cfga.gs.task.CustomTaskSetting;

/**
 * Subclass of {@link DefaultConfiguration} without all the settings which require several GIP dependencies to be loaded
 * (such as {@link HarvestingSetting}, {@link AugmenterSetting}, etc.). To be used in tests of projects that do not
 * have such dependencies
 * 
 * @author Fabrizio
 */
public class SimpleConfiguration extends DefaultConfiguration {

    /**
     * 
     */
    protected void init() {

	//
	// --- Profilers ---
	//

	ConfigurableLoader.load().//
		filter(c -> c.getSetting() instanceof ProfilerSetting).//
		map(c -> (ProfilerSetting) c.getSetting()).//
		forEach(s -> this.put(s));

	//
	// --- Database - Volatile as default ---
	//

	DatabaseSetting databaseSetting = new DatabaseSetting();
	databaseSetting.setVolatile(true);
	databaseSetting.setIdentifier(MainSettingsIdentifier.DATABASE.getLabel());

	put(databaseSetting);

	//
	// --- Scheduler - Volatile as default ---
	//

	SchedulerViewSetting schedulerSetting = new SchedulerViewSetting();
	schedulerSetting.setIdentifier(MainSettingsIdentifier.SCHEDULER.getLabel());

	put(schedulerSetting);

	//
	// --- DataCacheConnectorSetting settings
	//

	DataCacheConnectorSetting dataCacheConnectorSetting = DataCacheConnectorSettingLoader.load();

	dataCacheConnectorSetting.setIdentifier(MainSettingsIdentifier.DATA_CACHE_CONNECTOR_SETTINGS.getLabel());

	put(dataCacheConnectorSetting);

	//
	// --- OAuth settings ----
	//

	OAuthSetting oauthSetting = new OAuthSetting();

	oauthSetting.setIdentifier(MainSettingsIdentifier.OAUTH_SETTINGS.getLabel());

	put(oauthSetting);

	//
	// --- Credential settings ----
	//

	CredentialsSetting credSetting = new CredentialsSetting();

	credSetting.setIdentifier(MainSettingsIdentifier.CREDENTIALS_SETTINGS.getLabel());

	put(credSetting);

	//
	// --- Download settings ----
	//

	DownloadSetting downloadSetting = new DownloadSetting();

	downloadSetting.setIdentifier(MainSettingsIdentifier.DOWNLOAD_SETTINGS.getLabel());

	put(downloadSetting);

	//
	// --- Source storage settings
	//

	SourceStorageSetting sourceStorageSetting = new SourceStorageSetting();

	sourceStorageSetting.setIdentifier(MainSettingsIdentifier.SOURCE_STORAGE_SETTINGS.getLabel());

	put(sourceStorageSetting);

	//
	// --- Priority source setting
	//

	SourcePrioritySetting sourcePrioritySetting = new SourcePrioritySetting();

	sourcePrioritySetting.setIdentifier(MainSettingsIdentifier.SOURCE_PRIORITY_SETTINGS.getLabel());

	put(sourcePrioritySetting);

	//
	// --- GDC source setting
	//

	GDCSourcesSetting gdcSourcesSetting = new GDCSourcesSetting();

	gdcSourcesSetting.setIdentifier(MainSettingsIdentifier.GDC_SOURCES_SETTINGS.getLabel());

	put(gdcSourcesSetting);

	//
	// --- System settings ----
	//

	SystemSetting systemSetting = new SystemSetting();

	systemSetting.setIdentifier(MainSettingsIdentifier.SYSTEM_SETTINGS.getLabel());

	put(systemSetting);

	//
	// --- Custom Task settings ----
	//

	CustomTaskSetting customTaskSetting = new CustomTaskSetting();

	customTaskSetting.setIdentifier(MainSettingsIdentifier.CUSTOM_TASK_SETTINGS.getLabel());

	put(customTaskSetting);
    }
}
