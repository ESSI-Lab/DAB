/**
 * 
 */
package eu.essi_lab.cfga.gs;

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
		forEach(this::put);

	//
	// --- Database - Volatile as default ---
	//

	DatabaseSetting databaseSetting = new DatabaseSetting();
	databaseSetting.setVolatile(true);
	databaseSetting.setIdentifier(SingletonSettingsId.DATABASE_SETTING.getLabel());

	put(databaseSetting);

	//
	// --- Scheduler - Volatile as default ---
	//

	SchedulerViewSetting schedulerSetting = new SchedulerViewSetting();
	schedulerSetting.setIdentifier(SingletonSettingsId.SCHEDULER_SETTING.getLabel());

	put(schedulerSetting);

	//
	// --- DataCacheConnectorSetting settings
	//

	DataCacheConnectorSetting dataCacheConnectorSetting = DataCacheConnectorSettingLoader.load();

	dataCacheConnectorSetting.setIdentifier(SingletonSettingsId.DATA_CACHE_CONNECTOR_SETTING.getLabel());

	put(dataCacheConnectorSetting);

	//
	// --- OAuth settings ----
	//

	OAuthSetting oauthSetting = new OAuthSetting();

	oauthSetting.setIdentifier(SingletonSettingsId.OAUTH_SETTING.getLabel());

	put(oauthSetting);

	//
	// --- Credential settings ----
	//

	CredentialsSetting credSetting = new CredentialsSetting();

	credSetting.setIdentifier(SingletonSettingsId.CREDENTIALS_SETTING.getLabel());

	put(credSetting);

	//
	// --- Download settings ----
	//

	DownloadSetting downloadSetting = new DownloadSetting();

	downloadSetting.setIdentifier(SingletonSettingsId.DOWNLOAD_SETTING.getLabel());

	put(downloadSetting);

	//
	// --- Source storage settings
	//

	SourceStorageSetting sourceStorageSetting = new SourceStorageSetting();

	sourceStorageSetting.setIdentifier(SingletonSettingsId.SOURCE_STORAGE_SETTING.getLabel());

	put(sourceStorageSetting);

	//
	// --- Priority source setting
	//

	SourcePrioritySetting sourcePrioritySetting = new SourcePrioritySetting();

	sourcePrioritySetting.setIdentifier(SingletonSettingsId.SOURCE_PRIORITY_SETTING.getLabel());

	put(sourcePrioritySetting);

	//
	// --- GDC source setting
	//

	GDCSourcesSetting gdcSourcesSetting = new GDCSourcesSetting();

	gdcSourcesSetting.setIdentifier(SingletonSettingsId.GDC_SOURCES_SETTING.getLabel());

	put(gdcSourcesSetting);

	//
	// --- System settings ----
	//

	SystemSetting systemSetting = new SystemSetting();

	systemSetting.setIdentifier(SingletonSettingsId.SYSTEM_SETTING.getLabel());

	put(systemSetting);

	//
	// --- Custom Task settings ----
	//

	CustomTaskSetting customTaskSetting = new CustomTaskSetting();

	customTaskSetting.setIdentifier(SingletonSettingsId.CUSTOM_TASK_SETTING.getLabel());

	put(customTaskSetting);
    }
}
