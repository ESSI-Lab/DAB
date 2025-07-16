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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.quartz.JobExecutionContext;

import eu.essi_lab.cfga.Configuration;
import eu.essi_lab.cfga.ConfigurationChangeListener;
import eu.essi_lab.cfga.ConfigurationUtils;
import eu.essi_lab.cfga.gs.DefaultConfiguration.MainSettingsIdentifier;
import eu.essi_lab.cfga.gs.setting.CredentialsSetting;
import eu.essi_lab.cfga.gs.setting.DownloadSetting;
import eu.essi_lab.cfga.gs.setting.DownloadSetting.DownloadStorage;
import eu.essi_lab.cfga.gs.setting.GDCSourcesSetting;
import eu.essi_lab.cfga.gs.setting.GSSourceSetting;
import eu.essi_lab.cfga.gs.setting.ProfilerSetting;
import eu.essi_lab.cfga.gs.setting.SchedulerViewSetting;
import eu.essi_lab.cfga.gs.setting.SourcePrioritySetting;
import eu.essi_lab.cfga.gs.setting.SystemSetting;
import eu.essi_lab.cfga.gs.setting.SystemSetting.KeyValueOptionKeys;
import eu.essi_lab.cfga.gs.setting.accessor.AccessorSetting;
import eu.essi_lab.cfga.gs.setting.augmenter.worker.AugmenterWorkerSetting;
import eu.essi_lab.cfga.gs.setting.database.DatabaseSetting;
import eu.essi_lab.cfga.gs.setting.database.SourceStorageSetting;
import eu.essi_lab.cfga.gs.setting.dc_connector.DataCacheConnectorSetting;
import eu.essi_lab.cfga.gs.setting.distribution.DistributionSetting;
import eu.essi_lab.cfga.gs.setting.driver.SharedCacheDriverSetting;
import eu.essi_lab.cfga.gs.setting.driver.SharedPersistentDriverSetting;
import eu.essi_lab.cfga.gs.setting.harvesting.HarvestingSetting;
import eu.essi_lab.cfga.gs.setting.harvesting.HarvestingSettingLoader;
import eu.essi_lab.cfga.gs.setting.oauth.OAuthSetting;
import eu.essi_lab.cfga.gs.setting.ratelimiter.RateLimiterSetting;
import eu.essi_lab.cfga.gs.task.CustomTaskSetting;
import eu.essi_lab.cfga.scheduler.SchedulerUtils;
import eu.essi_lab.cfga.setting.SettingUtils;
import eu.essi_lab.cfga.setting.scheduling.SchedulerWorkerSetting;
import eu.essi_lab.cfga.setting.scheduling.Scheduling;
import eu.essi_lab.configuration.ExecutionMode;
import eu.essi_lab.lib.net.s3.S3TransferWrapper;
import eu.essi_lab.lib.utils.Chronometer;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.BondOperator;
import eu.essi_lab.messages.bond.LogicalBond;
import eu.essi_lab.messages.bond.ResourcePropertyBond;
import eu.essi_lab.messages.bond.View;
import eu.essi_lab.messages.web.WebRequest;
import eu.essi_lab.model.BrokeringStrategy;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.StorageInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.ResourceProperty;

/**
 * @author Fabrizio
 */
public class ConfigurationWrapper {

    /**
     * Reload every 5 minutes
     */
    public static final int CONFIG_RELOAD_TIME = 5;

    /**
     * 
     */
    public static final TimeUnit CONFIG_RELOAD_TIME_UNIT = TimeUnit.MINUTES;

    /**
     * 
     */
    private static List<GSSource> allSourcesCache;

    /**
     * 
     */
    private static List<HarvestingSetting> harvestingSettingsCache;

    /**
     * 
     */
    private static SystemSetting systemSetting;

    /**
     * 
     */
    private static String sparqlProxyEndpoint;
    
    

    /**
     * 
     */
    private static DatabaseSetting databaseSetting;

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
	configuration.addChangeEventListener(new ConfigurationChangeListener() {

	    @Override
	    public void configurationChanged(ConfigurationChangeEvent event) {

		allSourcesCache = getSources(null, false);
		harvestingSettingsCache = _getHarvestingSettings();
		systemSetting = getSystemSetting_();
		databaseSetting = getDatabaseSetting_();
	    }
	});

	allSourcesCache = getSources(null, false);
	harvestingSettingsCache = _getHarvestingSettings();
	systemSetting = getSystemSetting_();
	databaseSetting = getDatabaseSetting_();
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
    public static StorageInfo getStorageInfo() {

	return getDatabaseSetting().asStorageInfo();
    }

    /**
     * @return
     */
    public static DatabaseSetting getDatabaseSetting() {

	if (databaseSetting == null) {

	    databaseSetting = getDatabaseSetting_();
	}

	return databaseSetting;
    }

    /**
     * @return
     */
    private static DatabaseSetting getDatabaseSetting_() {

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

	return getSourcePrioritySetting(configuration);
    }

    /**
     * @return
     */
    public static SourceStorageSetting getSourceStorageSettings() {

	return getSourceStorageSettings(configuration);
    }

    /**
     * @return
     */
    public static GDCSourcesSetting getGDCSourceSetting() {

	return getGDCSourceSetting(configuration);
    }

    /**
     * @return
     */
    public static SourcePrioritySetting getSourcePrioritySetting(Configuration configuration) {

	return configuration.get(//
		MainSettingsIdentifier.SOURCE_PRIORITY_SETTINGS.getLabel(), //
		SourcePrioritySetting.class //
	).//
		get();

    }

    /**
     * @return
     */
    public static SourceStorageSetting getSourceStorageSettings(Configuration configuration) {

	return configuration.get(//
		MainSettingsIdentifier.SOURCE_STORAGE_SETTINGS.getLabel(), //
		SourceStorageSetting.class //
	).get();
    }

    /**
     * @return
     */
    public static GDCSourcesSetting getGDCSourceSetting(Configuration configuration) {

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

	List<ProfilerSetting> list = configuration.list(ProfilerSetting.class, false);

	return list;
    }

    /**
     * @return
     */
    public static List<AugmenterWorkerSetting> getAugmenterWorkerSettings() {

	return configuration.list(//
		AugmenterWorkerSetting.class, //
		false).//
		stream().//
		collect(Collectors.toList());
    }

    /**
     * @return
     */
    public static List<CustomTaskSetting> getCustomTaskSettings() {

	return configuration.list(//
		CustomTaskSetting.class, //
		false).//
		stream().//
		collect(Collectors.toList());
    }

    /**
     * Return the list of all the configured harvesting settings
     * 
     * @return
     */
    public static List<HarvestingSetting> getHarvestingSettings() {

	return harvestingSettingsCache;
    }

    /**
     * @return
     */
    private static List<HarvestingSetting> _getHarvestingSettings() {

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

	return allSourcesCache;
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

	findSourceIdentifiers(bond, sourceIds, view.getSourceDeployment());

	return getAllSources().//
		stream().//
		filter(s -> sourceIds.isEmpty() || sourceIds.contains(s.getUniqueIdentifier())).//
		collect(Collectors.toList());
    }

    /**
     * @param bond
     * @param out
     */
    @SuppressWarnings("incomplete-switch")
    private static void findSourceIdentifiers(Bond bond, List<String> out, String sourceDeployment) {

	if (bond instanceof LogicalBond) {

	    LogicalBond logicalBond = (LogicalBond) bond;

	    for (Bond operand : logicalBond.getOperands()) {

		findSourceIdentifiers(operand, out, sourceDeployment);
	    }

	} else if (bond instanceof ResourcePropertyBond) {

	    ResourcePropertyBond resBond = (ResourcePropertyBond) bond;

	    if (resBond.getProperty() == ResourceProperty.SOURCE_ID) {

		BondOperator operator = resBond.getOperator();
		switch (operator) {
		case EQUAL:
		    out.add(resBond.getPropertyValue());
		    break;
		case TEXT_SEARCH:

		    List<String> ids = getAllSources().//
			    stream().//
			    filter(s -> s.getUniqueIdentifier().startsWith(resBond.getPropertyValue())).//
			    map(s -> s.getUniqueIdentifier()).//
			    collect(Collectors.toList());

		    out.addAll(ids);
		    break;
		}
	    } else if (resBond.getProperty() == ResourceProperty.SOURCE_DEPLOYMENT) {

		BondOperator operator = resBond.getOperator();

		List<String> ids = getAllSources().//
			stream().//

			filter(s -> operator == BondOperator.EQUAL ? s.getDeployment().contains(sourceDeployment) : //
				s.getDeployment().stream().anyMatch(dep -> dep.startsWith(sourceDeployment)))
			.//
			map(s -> s.getUniqueIdentifier()).//
			collect(Collectors.toList());

		out.addAll(ids);
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

	return getAllSources().//
		stream().//
		filter(s -> s.getBrokeringStrategy() == BrokeringStrategy.HARVESTED).//
		collect(Collectors.toList());
    }

    /**
     * Retrieves all the distributed (mixed excluded) {@link GSSource}s defined by the current configuration
     *
     * @return
     * @throws GSException
     */
    public static List<GSSource> getDistributedSources() {

	return getAllSources().//
		stream().//
		filter(s -> s.getBrokeringStrategy() == BrokeringStrategy.DISTRIBUTED).//
		collect(Collectors.toList());
    }

    /**
     * Retrieves all the mixed {@link GSSource}s defined by the current configuration
     * 
     * @return
     */
    public static List<GSSource> getMixedSources() {

	return getAllSources().//
		stream().//
		filter(s -> s.getBrokeringStrategy() == BrokeringStrategy.MIXED).//
		collect(Collectors.toList());
    }

    /**
     * @return
     */
    public static List<GSSource> getIncrementalSources() {

	List<String> incrementalConnectors = Arrays.asList(//
		"ChinaGeossConnector", //
		"INPEConnector", //
		"Landsat8Connector", //
		"MeteoTrackerConnector", //
		"OAIPMHConnector", //
		"ONAMETConnector", //
		"PRISMAConnector", //
		"PolytopeIonBeamConnector", //
		"SentinelConnector");//

	return getHarvestingSettings().//
		stream().//
		map(s -> s.getSelectedAccessorSetting()).//
		filter(as -> incrementalConnectors.contains(as.getHarvestedConnectorSetting().getConfigurableType())).//
		map(as -> as.getSource()).//
		collect(Collectors.toList());
    }

    /**
     * Since the mixed sources are also harvested, this is a quick way to get all the
     * sources that can be harvested
     * 
     * @return
     */
    public static List<GSSource> getHarvestedAndMixedSources() {

	return getAllSources().//
		stream().//
		filter(s -> s.getBrokeringStrategy() == BrokeringStrategy.MIXED || s.getBrokeringStrategy() == BrokeringStrategy.HARVESTED).//
		collect(Collectors.toList());
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

	if (systemSetting == null) {

	    systemSetting = getSystemSetting_();
	}

	return systemSetting;
    }

    /**
     * @return
     */
    public static Optional<StorageInfo> getUsersStorageInfo() {

	return getSystemSettings().getUsersDatabaseSetting().map(s -> s.asStorageInfo());
    }

    /**
     * @param settingClass
     * @return
     */
    private static SystemSetting getSystemSetting_() {

	return configuration.get(//
		MainSettingsIdentifier.SYSTEM_SETTINGS.getLabel(), //
		SystemSetting.class //
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

    // ----
    //
    // Rate limiter
    //
    // ----

    public static RateLimiterSetting getRateLimiterSettingSettings() {

	RateLimiterSetting setting = configuration.get(//
		MainSettingsIdentifier.RATE_LIMITER_SETTINGS.getLabel(), //
		RateLimiterSetting.class).get();

	return setting;
    }

    /**
     * @return
     */
    public static DataCacheConnectorSetting getDataCacheConnectorSetting() {

	DataCacheConnectorSetting setting = configuration.get(//
		MainSettingsIdentifier.DATA_CACHE_CONNECTOR_SETTINGS.getLabel(), //
		DataCacheConnectorSetting.class, //
		false).get();

	return setting;
    }

    /**
     * @return
     */
    public static String getSparqlProxyEndpoint() {

	Optional<Properties> kvo = getSystemSettings().getKeyValueOptions();

	if (kvo.isPresent()) {

	    String prop = kvo.get().getProperty(KeyValueOptionKeys.SPARQL_PROXY_ENDPOINT.getLabel());

	    if (prop != null) {

		sparqlProxyEndpoint = prop;
	    }
	}

	return sparqlProxyEndpoint;
    }
    
    public static List<String> getAdminUsers() {

	Optional<Properties> kvo = getSystemSettings().getKeyValueOptions();
	List<String>adminUsers = new ArrayList<String>();
	if (kvo.isPresent()) {

	    String prop = kvo.get().getProperty(KeyValueOptionKeys.ADMIN_USERS.getLabel());

	    if (prop != null) {

		String[] s = prop.split(";");
		adminUsers = new ArrayList<String>();
		for (String user : s) {
		    adminUsers.add(user);
		}
	    }
	}

	return adminUsers;
    }

    /**
     * @param sparqlProxyEndpoint
     */
    public static void setSparqlProxyEndpoint(String sparqlProxyEndpoint) {

	ConfigurationWrapper.sparqlProxyEndpoint = sparqlProxyEndpoint;
    }

    /**
     * 
     */
    private static HashMap<String, Chronometer> isJobCanceledMap = new HashMap<>();

    /**
     * This method returns <code>true</code> in the following cases:
     * <ol>
     * <li>the scheduling of the given worker setting is disabled</li>
     * <li>the given worker setting is not found in the configuration</li>
     * </ol>
     * The second case can return a false positive, since the given worker setting can result missing even if is
     * present
     * in the
     * configuration. This can happen in a production environment if the given worker setting has been put in the
     * configuration less then
     * {@value #CONFIG_RELOAD_TIME} minutes from the method call and the task which calls this method is not in synch
     * with the
     * configuration.<br>
     * For example. An harvested accessor is added to the configuration and the scheduling is immediately started. The
     * task which runs the harvester, will
     * update its own copy of the configuration in {@value #CONFIG_RELOAD_TIME} minutes so in its own copy <i>the new
     * setting is missing</i>.
     * Thus, calling this method would return <code>true</code> since the task is not in synch with the DB
     * configuration.<br>
     * To avoid this, when this method is called from a production task for a given worker setting,
     * we ensure that at least ({@value #CONFIG_RELOAD_TIME} * 2) minutes are passed from the first call of this method
     * for that worker setting. If
     * ({@value #CONFIG_RELOAD_TIME} * 2) minutes are not passed, the method returns <code>false</code> because we
     * cannot be sure that the setting is
     * actually not present in the configuration
     * 
     * @param context
     * @return
     */
    public synchronized static boolean isJobCanceled(JobExecutionContext context) {

	SchedulerWorkerSetting contextSetting = SchedulerUtils.getSetting(context);

	//
	//
	//

	List<SchedulerWorkerSetting> workerSettingList = getAugmenterWorkerSettings().//
		stream().//
		map(s -> (SchedulerWorkerSetting) SettingUtils.downCast(s, s.getSettingClass())).//
		filter(s -> s.getIdentifier().equals(contextSetting.getIdentifier())).//
		collect(Collectors.toList());

	workerSettingList.addAll(getHarvestingSettings().//
		stream().//
		map(s -> (SchedulerWorkerSetting) SettingUtils.downCast(s, s.getSettingClass())).//
		filter(s -> s.getIdentifier().equals(contextSetting.getIdentifier())).//
		collect(Collectors.toList()));

	workerSettingList.addAll(getCustomTaskSettings().//
		stream().//
		map(s -> (SchedulerWorkerSetting) SettingUtils.downCast(s, s.getSettingClass())).//
		filter(s -> s.getIdentifier().equals(contextSetting.getIdentifier())).//
		collect(Collectors.toList()));

	//
	//
	//

	if (!workerSettingList.isEmpty()) {

	    Scheduling scheduling = workerSettingList.get(0).getScheduling();

	    return !scheduling.isEnabled();
	}

	//
	// if the list is empty, it means that the context setting is no longer in the configuration, it has been
	// removed
	//
	if (ExecutionMode.get() != ExecutionMode.MIXED && ExecutionMode.get() != ExecutionMode.LOCAL_PRODUCTION) {

	    Chronometer chronometer = isJobCanceledMap.get(contextSetting.getIdentifier());

	    if (chronometer == null) {

		chronometer = new Chronometer();
		chronometer.start();

		isJobCanceledMap.put(contextSetting.getIdentifier(), chronometer);

		return false;
	    }

	    long elapsedTimeMillis = chronometer.getElapsedTimeMillis();
	    long reloadTime = CONFIG_RELOAD_TIME_UNIT.toMillis(CONFIG_RELOAD_TIME * 2);

	    if (elapsedTimeMillis < reloadTime) {

		return false;
	    }
	}

	GSLoggerFactory.getLogger(ConfigurationWrapper.class).info("Setting of worker '" + contextSetting.getWorkerName() + "' removed");

	return true;
    }

    /**
     * @return
     */
    public static Optional<S3TransferWrapper> getS3TransferManager() {

	if (getDownloadSetting().getDownloadStorage() == DownloadStorage.LOCAL_DOWNLOAD_STORAGE) {

	    return Optional.empty();
	}

	String accessKey = getDownloadSetting().getS3StorageSetting().getAccessKey().get();
	String secretKey = getDownloadSetting().getS3StorageSetting().getSecretKey().get();
	Optional<String> endpoint = getDownloadSetting().getS3StorageSetting().getEndpoint();

	S3TransferWrapper manager = new S3TransferWrapper();
	manager.setAccessKey(accessKey);
	manager.setSecretKey(secretKey);
	if (endpoint.isPresent()) {
	    manager.setEndpoint(endpoint.get());
	}
	manager.initialize();
	return Optional.of(manager);
    }

}
