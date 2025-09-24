/**
 * 
 */
package eu.essi_lab.cfga.test;

import java.util.List;
import java.util.ServiceLoader;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.accessor.gbif.harvested.GBIFHarvestedConnector;
import eu.essi_lab.accessor.oaipmh.OAIPMHConnector;
import eu.essi_lab.accessor.wcs.WCSConnectorWrapper;
import eu.essi_lab.augmenter.worker.AugmenterWorkerSettingImpl;
import eu.essi_lab.cfga.check.ConfigEditableSettingMethod;
import eu.essi_lab.cfga.check.ConfigurationChecker;
import eu.essi_lab.cfga.check.ReferencedClassesMethod;
import eu.essi_lab.cfga.check.RegisteredEditableSettingMethod;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.DefaultConfiguration;
import eu.essi_lab.cfga.gs.SimilarityCheckMethod;
import eu.essi_lab.cfga.gs.setting.CredentialsSetting;
import eu.essi_lab.cfga.gs.setting.DownloadSetting;
import eu.essi_lab.cfga.gs.setting.DownloadSetting.DownloadStorage;
import eu.essi_lab.cfga.gs.setting.GDCSourcesSetting;
import eu.essi_lab.cfga.gs.setting.ProfilerSetting;
import eu.essi_lab.cfga.gs.setting.SchedulerViewSetting;
import eu.essi_lab.cfga.gs.setting.SourcePrioritySetting;
import eu.essi_lab.cfga.gs.setting.SystemSetting;
import eu.essi_lab.cfga.gs.setting.accessor.AccessorSetting;
import eu.essi_lab.cfga.gs.setting.augmenter.worker.AugmenterWorkerSetting;
import eu.essi_lab.cfga.gs.setting.database.DatabaseSetting;
import eu.essi_lab.cfga.gs.setting.database.SourceStorageSetting;
import eu.essi_lab.cfga.gs.setting.dc_connector.DataCacheConnectorSetting;
import eu.essi_lab.cfga.gs.setting.distribution.DistributionSetting;
import eu.essi_lab.cfga.gs.setting.driver.SharedCacheDriverSetting;
import eu.essi_lab.cfga.gs.setting.driver.SharedPersistentDriverSetting;
import eu.essi_lab.cfga.gs.setting.harvesting.HarvestingSetting;
import eu.essi_lab.cfga.gs.setting.oauth.OAuthSetting;
import eu.essi_lab.cfga.gs.setting.oauth.OAuthSetting.OAuthProvider;
import eu.essi_lab.cfga.gs.setting.ratelimiter.RateLimiterSetting;
import eu.essi_lab.cfga.gs.setting.ratelimiter.RateLimiterSetting.ComputationType;
import eu.essi_lab.cfga.gs.task.CustomTaskSetting;
import eu.essi_lab.cfga.setting.Setting;
import eu.essi_lab.cfga.setting.scheduling.SchedulerSetting.JobStoreType;
import eu.essi_lab.cfga.setting.scheduling.SchedulerWorkerSetting;
import eu.essi_lab.lib.utils.StreamUtils;
import eu.essi_lab.model.BrokeringStrategy;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.StorageInfo;
import eu.essi_lab.model.shared.SharedContent.SharedContentCategory;
import eu.essi_lab.pdk.Profiler;

/**
 * @author Fabrizio
 */
public class DefaultConfigurationTest {

    @Test
    public void configurationCheckerTest() throws Exception {

	DefaultConfiguration configuration = new DefaultConfiguration();
	configuration.clean();

	ConfigurationChecker checker = new ConfigurationChecker();

	checker.addCheckMethod(new RegisteredEditableSettingMethod());
	checker.addCheckMethod(new ReferencedClassesMethod());
	checker.addCheckMethod(new ConfigEditableSettingMethod());

	checker.addCheckMethod(new SimilarityCheckMethod());

	List<String> messages = checker.//
		check(configuration).//
		stream().//
		flatMap(r -> r.getMessages().stream()).//
		collect(Collectors.toList());

	Assert.assertEquals(0, messages.size());
    }

    @Test
    public void test() throws Exception {

	DefaultConfiguration configuration = new DefaultConfiguration(UUID.randomUUID().toString());

	configuration.clean();

	// flush is not required for test purpose but it is useful to copy and paste
	// the file URL and open the config with Oxygen
	configuration.flush();

	ConfigurationWrapper.setConfiguration(configuration);

	Assert.assertTrue(ConfigurationWrapper.getConfiguration().isPresent());

	//
	// --- Profilers ---
	//

	@SuppressWarnings("rawtypes")
	ServiceLoader<Profiler> profilers = ServiceLoader.load(Profiler.class);

	Assert.assertEquals(//
		StreamUtils.iteratorToStream(profilers.iterator()).count(), //
		ConfigurationWrapper.getProfilerSettings().size());

	// all profilers have its own type of setting
	long count = StreamUtils.iteratorToStream(profilers.iterator())
		.filter(p -> p.getSetting().getSettingClass().equals(ProfilerSetting.class)).count();

	Assert.assertEquals(0, count);

	//
	//
	//
	// --- Accessors ---
	//
	//
	//

	//
	// HarvestingSetting test
	//

	List<HarvestingSetting> harvestingSettings = ConfigurationWrapper.getHarvestingSettings();

	Assert.assertEquals(3, harvestingSettings.size());

	harvestingSettings.sort((s1, s2) -> s1.getSelectedAccessorSetting().getHarvestedConnectorSetting().getConfigurableType()
		.compareTo(s2.getSelectedAccessorSetting().getHarvestedConnectorSetting().getConfigurableType()));

	AccessorSetting sel0 = harvestingSettings.get(0).getSelectedAccessorSetting();
	String type0 = sel0.getHarvestedConnectorSetting().getConfigurableType();

	AccessorSetting sel1 = harvestingSettings.get(1).getSelectedAccessorSetting();
	String type1 = sel1.getHarvestedConnectorSetting().getConfigurableType();

	AccessorSetting sel2 = harvestingSettings.get(2).getSelectedAccessorSetting();
	String type2 = sel2.getHarvestedConnectorSetting().getConfigurableType();

	Assert.assertEquals(GBIFHarvestedConnector.CONNECTOR_TYPE, type0);
	Assert.assertEquals(OAIPMHConnector.CONNECTOR_TYPE, type1);
	Assert.assertEquals(WCSConnectorWrapper.TYPE, type2);

	Assert.assertEquals(0, harvestingSettings.get(0).getSelectedAugmenterSettings().size());
	Assert.assertEquals(0, harvestingSettings.get(1).getSelectedAugmenterSettings().size());
	// for WCS Access Augmenter is selected
	Assert.assertEquals(1, harvestingSettings.get(2).getSelectedAugmenterSettings().size());

	//
	// All AccessorSetting test
	//

	List<AccessorSetting> allAccessorSettings = ConfigurationWrapper.getAccessorSettings();
	allAccessorSettings.sort(//
		(s1, s2) -> s1.getBrokeringStrategy().getLabel().compareTo(s2.getBrokeringStrategy().getLabel()));

	Assert.assertEquals(4, allAccessorSettings.size());

	Assert.assertEquals(BrokeringStrategy.DISTRIBUTED, allAccessorSettings.get(0).getBrokeringStrategy());
	Assert.assertEquals(BrokeringStrategy.HARVESTED, allAccessorSettings.get(1).getBrokeringStrategy());
	Assert.assertEquals(BrokeringStrategy.HARVESTED, allAccessorSettings.get(2).getBrokeringStrategy());
	Assert.assertEquals(BrokeringStrategy.MIXED, allAccessorSettings.get(3).getBrokeringStrategy());

	//
	// Harvested AccessorSetting test
	//

	List<AccessorSetting> harvestedAccessorSettings = ConfigurationWrapper.getHarvestedAccessorSettings().stream().//
		sorted((a1, a2) -> a1.getGSSourceSetting().getSourceLabel().compareTo(a2.getGSSourceSetting().getSourceLabel())).//
		collect(Collectors.toList());

	Assert.assertEquals(2, harvestedAccessorSettings.size());
	Assert.assertEquals(BrokeringStrategy.HARVESTED, harvestedAccessorSettings.get(0).getBrokeringStrategy());
	Assert.assertEquals(BrokeringStrategy.HARVESTED, harvestedAccessorSettings.get(1).getBrokeringStrategy());

	//
	// Distributed AccessorSetting test
	//

	List<DistributionSetting> distSettings = ConfigurationWrapper.getDistributonSettings();
	Assert.assertEquals(1, distSettings.size());

	List<AccessorSetting> distributedAccessorSettings = ConfigurationWrapper.getDistributedAccessorSettings();

	Assert.assertEquals(1, distributedAccessorSettings.size());
	Assert.assertEquals(BrokeringStrategy.DISTRIBUTED, distributedAccessorSettings.get(0).getBrokeringStrategy());

	Assert.assertEquals(distSettings.get(0).getSelectedAccessorSetting(), distributedAccessorSettings.get(0));

	//
	// Mixed AccessorSetting test
	//

	List<AccessorSetting> mixedAccessorSettings = ConfigurationWrapper.getMixedAccessorSettings();

	Assert.assertEquals(1, mixedAccessorSettings.size());
	Assert.assertEquals(BrokeringStrategy.MIXED, mixedAccessorSettings.get(0).getBrokeringStrategy());

	//
	// All Sources test
	//

	List<GSSource> allSources = ConfigurationWrapper.getAllSources();

	allSources.sort((s1, s2) -> s1.getLabel().compareTo(s2.getLabel()));
	allAccessorSettings.sort((s1, s2) -> s1.getSource().getLabel().compareTo(s2.getSource().getLabel()));

	Assert.assertEquals(4, allSources.size());

	Assert.assertEquals(allSources.get(0), allAccessorSettings.get(0).getSource());
	Assert.assertEquals(allSources.get(1), allAccessorSettings.get(1).getSource());
	Assert.assertEquals(allSources.get(2), allAccessorSettings.get(2).getSource());
	Assert.assertEquals(allSources.get(3), allAccessorSettings.get(3).getSource());

	Assert.assertTrue(ConfigurationWrapper.checkSource(allSources.get(0).getUniqueIdentifier()));
	Assert.assertTrue(ConfigurationWrapper.checkSource(allSources.get(1).getUniqueIdentifier()));
	Assert.assertTrue(ConfigurationWrapper.checkSource(allSources.get(2).getUniqueIdentifier()));
	Assert.assertTrue(ConfigurationWrapper.checkSource(allSources.get(3).getUniqueIdentifier()));
	Assert.assertFalse(ConfigurationWrapper.checkSource("missing"));

	//
	// Harvested Sources test
	//
	List<GSSource> harvestedSources = ConfigurationWrapper.getHarvestedSources().//
		stream().//
		sorted((a1, a2) -> a1.getLabel().compareTo(a2.getLabel())).//
		collect(Collectors.toList());

	Assert.assertEquals(2, harvestedSources.size());

	Assert.assertEquals(harvestedSources.get(0), harvestedAccessorSettings.get(0).getSource());
	Assert.assertEquals(harvestedSources.get(1), harvestedAccessorSettings.get(1).getSource());

	//
	// Distributed Sources test
	//

	List<GSSource> distributedSources = ConfigurationWrapper.getDistributedSources();

	Assert.assertEquals(1, distributedSources.size());

	Assert.assertEquals(distributedSources.get(0), distributedAccessorSettings.get(0).getSource());

	//
	// Mixed Sources test
	//

	List<GSSource> mixedSources = ConfigurationWrapper.getMixedSources();

	Assert.assertEquals(1, mixedSources.size());

	Assert.assertEquals(mixedSources.get(0), mixedAccessorSettings.get(0).getSource());

	//
	// Harvested and Mixed Sources test
	//

	List<GSSource> mixedAndHarvestedSources = ConfigurationWrapper.getHarvestedAndMixedSources();

	Assert.assertEquals(3, mixedAndHarvestedSources.size());

	Assert.assertTrue(mixedAndHarvestedSources.contains(mixedAccessorSettings.get(0).getSource()));
	Assert.assertTrue(mixedAndHarvestedSources.contains(harvestedAccessorSettings.get(0).getSource()));
	Assert.assertTrue(mixedAndHarvestedSources.contains(harvestedAccessorSettings.get(1).getSource()));

	//
	// --- Database ---
	//

	DatabaseSetting databaseSetting = ConfigurationWrapper.getDatabaseSetting();

	Assert.assertNotNull(databaseSetting);

	List<Setting> settings = databaseSetting.getSettings();
	Assert.assertEquals(2, settings.size());

	Assert.assertTrue(databaseSetting.isVolatile());

	StorageInfo databaseURI = databaseSetting.asStorageInfo();

	Assert.assertNotNull(databaseURI.getUri());
	Assert.assertNotNull(databaseURI.getName());

	Assert.assertEquals(DatabaseSetting.VOLATILE_DB_URI, databaseURI.getUri());
	Assert.assertEquals(DatabaseSetting.VOLATILE_DB_STORAGE_NAME, databaseURI.getName());

	Assert.assertEquals(databaseURI.getUri(), databaseSetting.getDatabaseUri());
	Assert.assertEquals(databaseURI.getName(), databaseSetting.getDatabaseName());

	Assert.assertNull(databaseURI.getIdentifier());
	Assert.assertNull(databaseURI.getPassword());
	Assert.assertNull(databaseURI.getUser());

	//
	// Scheduler
	//

	SchedulerViewSetting schedulerSetting = ConfigurationWrapper.getSchedulerSetting();

	Assert.assertNotNull(schedulerSetting);

	settings = schedulerSetting.getSettings();
	Assert.assertEquals(2, settings.size());

	Assert.assertEquals(JobStoreType.VOLATILE, schedulerSetting.getJobStoreType());

	//
	// --- Shared Cache Repo of type Local ---
	//

	SharedCacheDriverSetting sharedCacheDriverSetting = ConfigurationWrapper.getSharedCacheDriverSetting();

	Assert.assertNotNull(sharedCacheDriverSetting);

	settings = sharedCacheDriverSetting.getSettings();
	Assert.assertEquals(2, settings.size());

	Assert.assertEquals(SharedContentCategory.LOCAL_CACHE, sharedCacheDriverSetting.getCategory());

	//
	// --- Shared Persistent Repo of type Local ---
	//

	SharedPersistentDriverSetting sharedPersistentDriverSetting = ConfigurationWrapper.getSharedPersistentDriverSetting();

	Assert.assertNotNull(sharedPersistentDriverSetting);

	settings = sharedPersistentDriverSetting.getSettings();
	Assert.assertEquals(2, settings.size());

	Assert.assertEquals(SharedContentCategory.LOCAL_PERSISTENT, sharedPersistentDriverSetting.getCategory());

	//
	// --- AugmenterWorker settings - The default one has no augmenter selected and the scheduling disabled ---
	//

	List<AugmenterWorkerSetting> augmenterWorkerSettings = ConfigurationWrapper.getAugmenterWorkerSettings();
	Assert.assertEquals(1, augmenterWorkerSettings.size());

	AugmenterWorkerSetting augmenterWorkerSetting = augmenterWorkerSettings.get(0);

	Assert.assertEquals(SchedulerWorkerSetting.SchedulingGroup.AUGMENTING, augmenterWorkerSetting.getGroup());

	Assert.assertFalse(augmenterWorkerSetting.getScheduling().isEnabled());

	// Augmenter settings
	Assert.assertEquals(AugmenterWorkerSettingImpl.class, augmenterWorkerSetting.getSettingClass());

	AugmenterWorkerSetting augmenterSetting = new AugmenterWorkerSettingImpl(augmenterWorkerSetting.getObject());

	Assert.assertEquals(0, augmenterSetting.getSelectedAugmenterSettings().size());

	//
	// --- Custom task settings ----
	//

	List<CustomTaskSetting> customTasksSettings = ConfigurationWrapper.getCustomTaskSettings();
	Assert.assertEquals(1, customTasksSettings.size());

	CustomTaskSetting customTaskSetting = customTasksSettings.get(0);

	Assert.assertEquals(SchedulerWorkerSetting.SchedulingGroup.CUSTOM_TASK, customTaskSetting.getGroup());

	Assert.assertFalse(customTaskSetting.getScheduling().isEnabled());

	//
	// --- OAuth settings ----
	//

	OAuthSetting oauthSetting = ConfigurationWrapper.getOAuthSetting();

	Assert.assertNotNull(oauthSetting);

	OAuthProvider selectedProvider = oauthSetting.getSelectedProvider();
	Assert.assertEquals(OAuthProvider.GOOGLE, selectedProvider);

	Assert.assertFalse(oauthSetting.getClientId().isPresent());
	Assert.assertFalse(oauthSetting.getClientSecret().isPresent());

	//
	// --- Credentials settings ----
	//

	CredentialsSetting credSetting = ConfigurationWrapper.getCredentialsSetting();

	Assert.assertNotNull(credSetting);

	//
	// --- Download settings ----
	//

	DownloadSetting downloadSetting = ConfigurationWrapper.getDownloadSetting();

	Assert.assertNotNull(downloadSetting);

	settings = downloadSetting.getSettings();
	Assert.assertEquals(2, settings.size());

	DownloadStorage downloadStorage = downloadSetting.getDownloadStorage();

	Assert.assertEquals(DownloadStorage.LOCAL_DOWNLOAD_STORAGE, downloadStorage);

	//
	// --- Source storage settings ----
	//

	SourceStorageSetting sourceStorageSetting = ConfigurationWrapper.getSourceStorageSettings();

	Assert.assertNotNull(sourceStorageSetting);

	//
	// --- Priority source settings ----
	//

	SourcePrioritySetting sourcePrioritySetting = ConfigurationWrapper.getSourcePrioritySetting();

	Assert.assertNotNull(sourcePrioritySetting);

	//
	// --- GDC source settings ----
	//

	GDCSourcesSetting gdcPrioritySetting = ConfigurationWrapper.getGDCSourceSetting();

	Assert.assertNotNull(gdcPrioritySetting);

	//
	// --- Data cache connector settings ----
	//

	DataCacheConnectorSetting dataCacheConnectorSetting = ConfigurationWrapper.getDataCacheConnectorSetting();

	Assert.assertNotNull(dataCacheConnectorSetting);

	//
	// --- System settings ----
	//

	SystemSetting systemSetting = ConfigurationWrapper.getSystemSettings();

	Assert.assertNotNull(systemSetting);

	boolean mailEnabled = systemSetting.isHarvestingReportMailEnabled();
	Assert.assertFalse(mailEnabled);

	boolean databaseInfoPublisherEnabled = systemSetting.areStatisticsEnabled();
	Assert.assertFalse(databaseInfoPublisherEnabled);

	//
	// --- Rate Limiter settings ----
	//

	RateLimiterSetting rateLimiterSetting = ConfigurationWrapper.getRateLimiterSettingSettings();

	Assert.assertNotNull(rateLimiterSetting);

	ComputationType computationType = rateLimiterSetting.getComputationType();
	Assert.assertEquals(ComputationType.DISABLED, computationType);
    }
}
