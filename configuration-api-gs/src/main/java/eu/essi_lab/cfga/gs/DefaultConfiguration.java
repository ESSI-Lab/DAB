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

import java.util.concurrent.TimeUnit;

import eu.essi_lab.cfga.ConfigurableLoader;
import eu.essi_lab.cfga.Configuration;
import eu.essi_lab.cfga.ConfigurationSource;
import eu.essi_lab.cfga.SelectionUtils;
import eu.essi_lab.cfga.gs.setting.CredentialsSetting;
import eu.essi_lab.cfga.gs.setting.DownloadSetting;
import eu.essi_lab.cfga.gs.setting.GDCSourcesSetting;
import eu.essi_lab.cfga.gs.setting.ProfilerSetting;
import eu.essi_lab.cfga.gs.setting.SchedulerViewSetting;
import eu.essi_lab.cfga.gs.setting.SourcePrioritySetting;
import eu.essi_lab.cfga.gs.setting.SystemSetting;
import eu.essi_lab.cfga.gs.setting.accessor.AccessorSetting;
import eu.essi_lab.cfga.gs.setting.augmenter.worker.AugmenterWorkerSetting;
import eu.essi_lab.cfga.gs.setting.augmenter.worker.AugmenterWorkerSettingLoader;
import eu.essi_lab.cfga.gs.setting.database.DatabaseSetting;
import eu.essi_lab.cfga.gs.setting.database.SourceStorageSetting;
import eu.essi_lab.cfga.gs.setting.dc_connector.DataCacheConnectorSetting;
import eu.essi_lab.cfga.gs.setting.dc_connector.DataCacheConnectorSettingLoader;
import eu.essi_lab.cfga.gs.setting.distribution.DistributionSetting;
import eu.essi_lab.cfga.gs.setting.driver.DriverSetting;
import eu.essi_lab.cfga.gs.setting.harvesting.HarvestingSetting;
import eu.essi_lab.cfga.gs.setting.harvesting.HarvestingSettingLoader;
import eu.essi_lab.cfga.gs.setting.oauth.OAuthSetting;
import eu.essi_lab.cfga.gs.setting.ratelimiter.RateLimiterSetting;
import eu.essi_lab.cfga.gs.task.CustomTaskSetting;
import eu.essi_lab.cfga.source.FileSource;
import eu.essi_lab.lib.utils.LabeledEnum;
import eu.essi_lab.model.shared.SharedContent.SharedContentCategory;

/**
 * @author Fabrizio
 */
public class DefaultConfiguration extends Configuration {

    /**
     * @author Fabrizio
     */
    public enum SingletonSettingsId implements LabeledEnum {

	/**
	 * 
	 */
	DATABASE_SETTING("database"),

	/**
	 * 
	 */
	SHARED_CACHE_REPO_SETTING("sharedCacheRepo"),

	/**
	 * 
	 */
	SHARED_PERSISTENT_REPO_SETTING("sharedPersistentRepo"),

	/**
	 * 
	 */
	SCHEDULER_SETTING("scheduler"),

	/**
	 * 
	 */
	OAUTH_SETTING("oauthSettings"),

	/**
	 * 
	 */
	SYSTEM_SETTING("systemSettings"),

	/**
	 * 
	 */
	CUSTOM_TASK_SETTING("customTaskSettings"),

	/**
	 * 
	 */
	SOURCE_STORAGE_SETTING("sourceStorageSettings"),

	/**
	 * 
	 */
	CREDENTIALS_SETTING("credentialsSettings"),

	/**
	 * 
	 */
	SOURCE_PRIORITY_SETTING("sourcePrioritySettings"),
	/**
	 * 
	 */
	GDC_SOURCES_SETTING("gdcSourceSettings"), //

	/**
	 * 
	 */
	DATA_CACHE_CONNECTOR_SETTING("dataCacheConnectorSettings"), //

	/**
	 * 
	 */
	DOWNLOAD_SETTING("downloadSettings"),

	/**
	 * 
	 */
	RATE_LIMITER_SETTING("rateLimiterSettings");

	private final String label;

	/**
	 * @param value
	 */
	SingletonSettingsId(String value) {

	    this.label = value;
	}

	@Override
	public String getLabel() {

	    return label;
	}

	@Override
	public String toString() {

	    return getLabel();
	}

    }

    /**
     * Creates an in-memory only configuration with no related source.<br>
     * This configuration <i>cannot be flushed</i>, use it only for test purpose
     */
    public DefaultConfiguration() {

	init();
    }

    /**
     * Uses {@link FileSource#ConfigurationFilesSource(String)}
     * 
     * @param configName
     * @throws Exception
     */
    public DefaultConfiguration(String configName) throws Exception {

	super(new FileSource(configName));
	init();
    }

    /**
     * Uses {@link FileSource#ConfigurationFilesSource(String)}
     * 
     * @param configName
     * @param unit
     * @param interval
     * @throws Exception
     */
    public DefaultConfiguration(String configName, TimeUnit unit, int interval) throws Exception {

	super(new FileSource(configName), unit, interval);
	init();
    }

    /**
     * @param source
     * @param unit
     * @param interval
     * @throws Exception
     */
    public DefaultConfiguration(ConfigurationSource source, TimeUnit unit, int interval) throws Exception {

	super(source, unit, interval);
	init();
    }

    /**
     * @param source
     * @throws Exception
     */
    public DefaultConfiguration(ConfigurationSource source) throws Exception {

	super(source);
	init();
    }

    /**
     * 
     */
    protected void init() {

	//
	// setting the scheme
	//

	setScheme(new DefaultConfigurationScheme());

	//
	// --- Profilers ---
	//

	ConfigurableLoader.load().//
		filter(c -> c.getSetting() instanceof ProfilerSetting).//
		map(c -> (ProfilerSetting) c.getSetting()).//
		forEach(this::put);

	//
	// --- Accessors ---
	//

	//
	// FDSN Earth quake events as default distributed
	//

	DistributionSetting distributionSetting = new DistributionSetting();

	distributionSetting.getAccessorsSetting().select(s -> s.getName().equals("USGS Earthquake Events Accessor"));

	distributionSetting.getSelectedAccessorSetting().getGSSourceSetting().setSourceIdentifier("defaultFDSNSource");

	put(distributionSetting);

	//
	// OAIPMH connected to the DAB as default harvester worker setting
	//

	HarvestingSetting oaiSetting = HarvestingSettingLoader.load();

	oaiSetting.getAccessorsSetting().//
		select(s -> s.getName().equals("OAIPMH Accessor"));

	oaiSetting.getSelectedAccessorSetting().getGSSourceSetting().setSourceIdentifier("defaultOAISource");
	oaiSetting.getSelectedAccessorSetting().getGSSourceSetting().setSourceLabel("DAB OAIPMH Service");
	oaiSetting.getSelectedAccessorSetting().getGSSourceSetting()
		.setSourceEndpoint("http://gs-service-production.geodab.eu/gs-service/services/oaipmh");

	put(oaiSetting);

	//
	// WCS Accessor for download tests
	//
	HarvestingSetting wcsSetting = HarvestingSettingLoader.load();

	wcsSetting.getAccessorsSetting().//
		select(s -> s.getName().equals("WCS Accessor"));

	wcsSetting.getSelectedAccessorSetting().getGSSourceSetting().setSourceIdentifier("atlasSouth");
	wcsSetting.getSelectedAccessorSetting().getGSSourceSetting().setSourceLabel("Atlas of the Cryosphere: Southern Hemisphere (WCS)");
	wcsSetting.getSelectedAccessorSetting().getGSSourceSetting().setSourceEndpoint("http://nsidc.org/cgi-bin/atlas_south");

	wcsSetting.getAugmentersSetting().select(s -> s.getName().equals("Access augmenter"));

	put(wcsSetting);

	//
	// GBIF as default mixed harvester worker setting
	//

	HarvestingSetting gbifSetting = HarvestingSettingLoader.load();

	gbifSetting.getAccessorsSetting().//
		select(s -> s.getName().equals("GBIF Accessor"));

	AccessorSetting gbifAccessorSetting = gbifSetting.getSelectedAccessorSetting();

	gbifAccessorSetting.getGSSourceSetting().setSourceIdentifier("defaultGBIFMixedSource");

	put(gbifSetting);

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
	// --- Shared Cache Repo of type Local ---
	//

	DriverSetting cacheSetting = ConfigurableLoader.load().//

		filter(c -> c.getSetting() instanceof DriverSetting).//
		map(c -> ((DriverSetting) c.getSetting())).//
		filter(s -> s.getCategory() == SharedContentCategory.LOCAL_CACHE).//
		findFirst().//
		get();

	cacheSetting.setIdentifier(SingletonSettingsId.SHARED_CACHE_REPO_SETTING.getLabel());

	put(cacheSetting);

	//
	// --- Shared Persistent Repo of type Local ---
	//

	DriverSetting persistentSetting = ConfigurableLoader.load().//

		filter(c -> c.getSetting() instanceof DriverSetting).//
		map(c -> ((DriverSetting) c.getSetting())).//
		filter(s -> s.getCategory() == SharedContentCategory.LOCAL_PERSISTENT).//
		findFirst().//
		get();

	persistentSetting.setIdentifier(SingletonSettingsId.SHARED_PERSISTENT_REPO_SETTING.getLabel());

	put(persistentSetting);

	//
	// --- AugmenterWorker settings - The default one has all the augmenters and the scheduling disabled ---
	//

	AugmenterWorkerSetting augmenterWorkerSetting = AugmenterWorkerSettingLoader.load();

	put(augmenterWorkerSetting);

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

	//
	// --- Rate Limiter settings ----
	//

	RateLimiterSetting rateLimiterSetting = new RateLimiterSetting();

	rateLimiterSetting.setIdentifier(SingletonSettingsId.RATE_LIMITER_SETTING.getLabel());

	put(rateLimiterSetting);
    }

    /**
     * 
     */
    public void clean() {

	SelectionUtils.deepClean(this);

	SelectionUtils.deepAfterClean(this);
    }
}
