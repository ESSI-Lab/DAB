package eu.essi_lab.cfga.gs;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2022 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import eu.essi_lab.cfga.Configuration;
import eu.essi_lab.cfga.ConfigurationUtils;
import eu.essi_lab.cfga.gs.DefaultConfiguration.MainSettingsIdentifier;
import eu.essi_lab.cfga.gs.setting.CredentialsSetting;
import eu.essi_lab.cfga.gs.setting.DownloadSetting;
import eu.essi_lab.cfga.gs.setting.GDCSourcesSetting;
import eu.essi_lab.cfga.gs.setting.GSSourceSetting;
import eu.essi_lab.cfga.gs.setting.ProfilerSetting;
import eu.essi_lab.cfga.gs.setting.SchedulerViewSetting;
import eu.essi_lab.cfga.gs.setting.SourcePrioritySetting;
import eu.essi_lab.cfga.gs.setting.SystemSetting;
import eu.essi_lab.cfga.gs.setting.accessor.AccessorSetting;
import eu.essi_lab.cfga.gs.setting.augmenter.worker.AugmenterWorkerSetting;
import eu.essi_lab.cfga.gs.setting.database.DatabaseSetting;
import eu.essi_lab.cfga.gs.setting.database.SourceStorageSetting;
import eu.essi_lab.cfga.gs.setting.distribution.DistributionSetting;
import eu.essi_lab.cfga.gs.setting.driver.SharedCacheDriverSetting;
import eu.essi_lab.cfga.gs.setting.driver.SharedPersistentDriverSetting;
import eu.essi_lab.cfga.gs.setting.harvesting.HarvestingSetting;
import eu.essi_lab.cfga.gs.setting.harvesting.HarvestingSettingLoader;
import eu.essi_lab.cfga.gs.setting.oauth.OAuthSetting;
import eu.essi_lab.cfga.gs.task.CustomTaskSetting;
import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.LogicalBond;
import eu.essi_lab.messages.bond.ResourcePropertyBond;
import eu.essi_lab.messages.bond.View;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.BrokeringStrategy;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.StorageUri;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.ResourceProperty;

/**
 * @author Fabrizio
 */
public class ConfigurationWrapper {

    /**
     * 
     */
    private static Configuration configuration;

    private ConfigurationWrapper() {
    }

    /**
     * @param config
     */
    public static void setConfiguration(Configuration config) {

	configuration = config;
    }

    /**
     * @return
     */
    public static Optional<Configuration> getConfiguration() {

	return Optional.ofNullable(configuration);
    }

    /**
     * @return
     */
    public static StorageUri getDatabaseURI() {

	return getDatabaseSetting().asStorageUri();
    }

    /**
     * @return
     */
    public static DatabaseSetting getDatabaseSetting() {

	return configuration.get(//
		MainSettingsIdentifier.DATABASE.getLabel(), //
		DatabaseSetting.class //
	).//
		get();

    }

    /**
     * @return
     */
    public static SourcePrioritySetting getSourcePrioritySetting() {

	return configuration.get(//
		MainSettingsIdentifier.SOURCE_PRIORITY_SETTINGS.getLabel(), //
		SourcePrioritySetting.class //
	).//
		get();

    }

    /**
     * @return
     */
    public static GDCSourcesSetting getGDCSourceSetting() {

	return configuration.get(//
		MainSettingsIdentifier.GDC_SOURCES_SETTINGS.getLabel(), //
		GDCSourcesSetting.class //
	).//
		get();

    }

    /**
     * @return
     */
    public static SharedCacheDriverSetting getSharedCacheDriverSetting() {

	SharedCacheDriverSetting setting = configuration.get(//
		MainSettingsIdentifier.SHARED_CACHE_REPO.getLabel(), //
		SharedCacheDriverSetting.class).get();

	return setting;
    }

    /**
     * @return
     */
    public static SharedPersistentDriverSetting getSharedPersistentDriverSetting() {

	SharedPersistentDriverSetting setting = configuration.get(//
		MainSettingsIdentifier.SHARED_PERSISTENT_REPO.getLabel(), //
		SharedPersistentDriverSetting.class).get();

	return setting;
    }

    /**
     * @return
     */
    public static SchedulerViewSetting getSchedulerSetting() {

	SchedulerViewSetting setting = configuration.get(//
		MainSettingsIdentifier.SCHEDULER.getLabel(), //
		SchedulerViewSetting.class).get();

	return setting;
    }

    /**
     * @return
     */
    public static List<ProfilerSetting> getProfilerSettings() {

	List<ProfilerSetting> list = configuration.list(ProfilerSetting.class);

	return list;
    }

    /**
     * @return
     */
    public static List<AugmenterWorkerSetting> getAugmenterWorkerSettings() {

	return configuration
		.list(//
			AugmenterWorkerSetting.class, //
			false)
		.//
		stream().//
		collect(Collectors.toList());
    }

    /**
     * @return
     */
    public static List<CustomTaskSetting> getCustomTaskSettings() {

	return configuration
		.list(//
			CustomTaskSetting.class, //
			false)
		.//
		stream().//
		collect(Collectors.toList());
    }

    /**
     * Return the list of all the configured harvesting settings
     * 
     * @return
     */
    public static List<HarvestingSetting> getHarvestingSettings() {

	@SuppressWarnings("unchecked")
	Class<HarvestingSetting> clazz = (Class<HarvestingSetting>) HarvestingSettingLoader.load().getClass();

	List<HarvestingSetting> list = configuration.list(clazz, //

		s -> {

		    if (s.getObject().getString("settingClass").equals(clazz.getName())) {

			HarvestingSetting harvestingSetting = HarvestingSettingLoader.load(s.getObject());

			return harvestingSetting;
		    }

		    return null;
		});

	return list;
    }

    /**
     * Return the list of all the configured distribution settings
     * 
     * @return
     */
    public static List<DistributionSetting> getDistributonSettings() {

	List<DistributionSetting> list = configuration.list(DistributionSetting.class, //

		s -> {

		    if (s.getObject().getString("settingClass").equals(DistributionSetting.class.getName())) {

			return new DistributionSetting(s.getObject().toString());
		    }

		    return null;
		});

	return list;
    }

    /**
     * @return
     */
    public static List<AccessorSetting> getAccessorSettings() {

	return getAccessorSettings(null);
    }

    /**
     * @return
     */
    private static List<AccessorSetting> getAccessorSettings(BrokeringStrategy strategy) {

	List<AccessorSetting> mapped = new ArrayList<>();

	ConfigurationUtils.deepMap(configuration, s -> {

	    if (s.getObject().getString("settingClass").equals(AccessorSetting.class.getName())) { //

		if (strategy == null || s.getObject().getString("brokeringStrategy").equals(strategy.getLabel())) {

		    return new AccessorSetting(s.getObject().toString());
		}
	    }

	    return null;

	}, mapped);

	return mapped;
    }

    /**
     * @return
     */
    public static List<AccessorSetting> getDistributedAccessorSettings() {

	return getAccessorSettings(BrokeringStrategy.DISTRIBUTED);
    }

    /**
     * @return
     */
    public static List<AccessorSetting> getHarvestedAccessorSettings() {

	return getAccessorSettings(BrokeringStrategy.HARVESTED);
    }

    /**
     * @return
     */
    public static List<AccessorSetting> getMixedAccessorSettings() {

	return getAccessorSettings(BrokeringStrategy.MIXED);
    }

    /**
     * @param source
     * @return
     */
    public static Optional<AccessorSetting> getAccessorSetting(GSSource source) {

	return getAccessorSettings().//
		stream().//
		filter(s -> s.getSource().getUniqueIdentifier() != null
			&& s.getSource().getUniqueIdentifier().equals(source.getUniqueIdentifier()))
		.//
		findFirst();
    }

    //
    //
    //
    //
    //
    //

    /**
     * This utility method can be used to implement the {@link #validate(WebRequest)} method. It checks whether a
     * {@link GSSource} with the
     * supplied <code>sourceIdentifier</code> exists
     *
     * @param sourceIdentifier the identifier of the {@link GSSource} to retrieve
     */
    public static boolean checkSource(String sourceIdentifier) {

	return getSource(sourceIdentifier) != null;
    }

    /**
     * This utility method can be used to implement the {@link #validate(WebRequest)} method
     *
     * @param sourceIdentifiers
     */
    public static List<String> checkSources(List<String> sourceIdentifiers) {

	List<String> allSources = getAllSources().stream().//
		map(s -> s.getUniqueIdentifier()).//
		collect(Collectors.toList());

	List<String> collect = sourceIdentifiers.//
		stream().//
		filter(s -> !allSources.contains(s)).//
		collect(Collectors.toList());

	return collect;
    }

    /**
     * Retrieves all the {@link GSSource}s defined by the current configuration: brokered, harvested, mixed
     *
     * @return
     * @throws GSException
     */
    public static List<GSSource> getAllSources() {

	return getSources(null, false);
    }

    /**
     * Retrieves all the {@link GSSource}s defined by the current configuration (brokered, harvested, mixed)
     * according to the given <code>view</code>
     *
     * @return
     * @throws GSException
     */
    public static List<GSSource> getViewSources(View view) {

	final List<String> sourceIds = new ArrayList<>();

	Bond bond = view.getBond();

	findSourceIdentifiers(bond, sourceIds);

	return getSources(null, false).//
		stream().//
		filter(s -> sourceIds.isEmpty() || sourceIds.contains(s.getUniqueIdentifier())).//
		collect(Collectors.toList());
    }

    /**
     * @param bond
     * @param out
     */
    private static void findSourceIdentifiers(Bond bond, List<String> out) {

	if (bond instanceof LogicalBond) {

	    LogicalBond logicalBond = (LogicalBond) bond;

	    for (Bond operand : logicalBond.getOperands()) {

		findSourceIdentifiers(operand, out);
	    }

	} else if (bond instanceof ResourcePropertyBond) {

	    ResourcePropertyBond resBond = (ResourcePropertyBond) bond;
	    if (resBond.getProperty() == ResourceProperty.SOURCE_ID) {
		out.add(resBond.getPropertyValue());
	    }
	}
    }

    /**
     * Retrieves all the harvested (mixed excluded) {@link GSSource}s defined by the current configuration
     *
     * @return
     * @throws GSException
     */
    public static List<GSSource> getHarvestedSources() {

	return getSources(BrokeringStrategy.HARVESTED, false);
    }

    /**
     * Retrieves all the distributed (mixed excluded) {@link GSSource}s defined by the current configuration
     *
     * @return
     * @throws GSException
     */
    public static List<GSSource> getDistributedSources() {

	return getSources(BrokeringStrategy.DISTRIBUTED, false);
    }

    /**
     * Retrieves all the mixed {@link GSSource}s defined by the current configuration
     * 
     * @return
     */
    public static List<GSSource> getMixedSources() {

	return getSources(BrokeringStrategy.MIXED, false);
    }

    /**
     * Since the mixed sources are also harvested, this is a quick way to get all the
     * sources that can be harvested
     * 
     * @return
     */
    public static List<GSSource> getHarvestedAndMixedSources() {

	return getSources(null, true);

    }

    /**
     * Retrieves all the harvested (mixed excluded) {@link GSSource}s defined by the current configuration
     *
     * @return
     * @throws GSException
     */
    private static List<GSSource> getSources(BrokeringStrategy strategy, boolean harvestedAndMixed) {

	List<GSSource> mapped = new ArrayList<>();

	ConfigurationUtils.deepMap(configuration, s -> {

	    if (s.getObject().getString("settingClass").equals(GSSourceSetting.class.getName()) && //

	    (strategy == null && !harvestedAndMixed ||

		    strategy == null && harvestedAndMixed
			    && (s.getObject().getString("brokeringStrategy").equals(BrokeringStrategy.HARVESTED.getLabel())
				    || s.getObject().getString("brokeringStrategy").equals(BrokeringStrategy.MIXED.getLabel()))
		    ||

		    strategy != null && s.getObject().getString("brokeringStrategy").equals(strategy.getLabel())

	    ) //

	    ) {

		return new GSSourceSetting(s.getObject().toString()).asSource();
	    }

	    return null;

	}, mapped);

	return mapped;
    }

    /**
     * @param sourceIdentifier
     * @return
     * @throws GSException
     */
    public static GSSource getSource(String sourceIdentifier) {

	return getAllSources().//
		stream().//
		filter(s -> s.getUniqueIdentifier().equals(sourceIdentifier)).//
		findFirst().//
		orElse(null);

    }

    /**
     * @param settingClass
     * @return
     */
    public static SystemSetting getSystemSettings() {

	return configuration.get(//
		MainSettingsIdentifier.SYSTEM_SETTINGS.getLabel(), //
		SystemSetting.class //
	).get();
    }

    /**
     * @return
     */
    public static SourceStorageSetting getSourceStorageSettings() {

	return configuration.get(//
		MainSettingsIdentifier.SOURCE_STORAGE_SETTINGS.getLabel(), //
		SourceStorageSetting.class //
	).get();
    }

    /**
     * @param settingClass
     * @return
     */
    public static DownloadSetting getDownloadSetting() {

	return configuration.get(//
		MainSettingsIdentifier.DOWNLOAD_SETTINGS.getLabel(), //
		DownloadSetting.class //
	).get();
    }

    /**
     * @param oAuthProvider
     * @return
     */
    public static OAuthSetting getOAuthSetting() {

	return configuration.get(//
		MainSettingsIdentifier.OAUTH_SETTINGS.getLabel(), //
		OAuthSetting.class //
	).get();
    }

    /**
     * @return
     */
    public static Optional<String> readAdminIdentifier() {

	return getOAuthSetting().getAdminId();
    }

    // ----
    //
    // Credentials
    //
    // ----

    public static CredentialsSetting getCredentialsSetting() {

	CredentialsSetting setting = configuration.get(//
		MainSettingsIdentifier.CREDENTIALS_SETTINGS.getLabel(), //
		CredentialsSetting.class).get();

	return setting;
    }
}
