package eu.essi_lab.cfga.augmenter.worker.test;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.accessor.oaipmh.OAIPMHAccessor;
import eu.essi_lab.accessor.oaipmh.OAIPMHConnectorSetting;
import eu.essi_lab.api.database.Database;
import eu.essi_lab.api.database.DatabaseFinder;
import eu.essi_lab.api.database.factory.DatabaseProviderFactory;
import eu.essi_lab.api.database.factory.DatabaseFactory;
import eu.essi_lab.api.database.vol.VolatileDatabase;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.DefaultConfiguration;
import eu.essi_lab.cfga.gs.setting.augmenter.worker.AugmenterWorkerSetting;
import eu.essi_lab.cfga.gs.setting.harvesting.HarvestingSetting;
import eu.essi_lab.cfga.scheduler.Scheduler;
import eu.essi_lab.cfga.scheduler.Scheduler.JobEvent;
import eu.essi_lab.cfga.scheduler.SchedulerFactory;
import eu.essi_lab.harvester.worker.HarvesterWorker;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.StorageInfo;
import eu.essi_lab.model.resource.AugmentedMetadataElement;
import eu.essi_lab.model.resource.GSResource;

/**
 * @author Fabrizio
 */
public class AugmenterWorkerExternalTestIT {

    private volatile boolean test1Ended;

    @Test
    public void test1() throws Exception {

	DefaultConfiguration configuration = new DefaultConfiguration(UUID.randomUUID().toString() + ".json");

	ConfigurationWrapper.setConfiguration(configuration);

	// harvest 50 records from DAB OAIPMH to the Volatile DB
	harvestOAIPMH();

	//
	// Adjust the AugmenterWorkerSetting
	//
	AugmenterWorkerSetting augmenterWorkerSetting = ConfigurationWrapper.getAugmenterWorkerSettings().get(0);

	// only 50 records to augment
	augmenterWorkerSetting.setMaxRecords(50);

	// selects only OAIPMH DAB source
	List<String> list = ConfigurationWrapper.getHarvestedSources().//
		stream().//
		filter(s -> s.getEndpoint() != null && s.getEndpoint().contains("geodab")).//
		map(GSSource::getLabel).//
		collect(Collectors.toList());

	augmenterWorkerSetting.setSelectedSources(list);

	// selects only the metadata augmenter
	augmenterWorkerSetting.getAugmentersSetting().select(s -> s.getConfigurableType().equals("MetadataAugmenter"));

	//
	//
	//
	configuration.clean();
	//
	//
	//

	//
	// schedules the worker
	//
	Scheduler scheduler = SchedulerFactory.getVolatileScheduler();

	scheduler.addJobEventListener((e, c, ex) -> test1Ended = true, //
		JobEvent.JOB_EXECUTED, //
		UUID.randomUUID().toString(), //
		true); //

	scheduler.schedule(augmenterWorkerSetting);

	while (!test1Ended) {
	}

	StorageInfo databaseURI = ConfigurationWrapper.getDatabaseURI();
	DatabaseFinder finder = DatabaseProviderFactory.getFinder(databaseURI);

	DiscoveryMessage message = new DiscoveryMessage();

	GSSource gsSource = ConfigurationWrapper.getHarvestedSources().//
		stream().//
		filter(s -> s.getEndpoint().contains("dab")).//
		findFirst().//
		get();

	message.setSources(Arrays.asList(gsSource));

	Page page = new Page(1, 50);
	message.setPage(page);

	ResultSet<GSResource> resultSet = finder.discover(message);

	List<GSResource> resultsList = resultSet.getResultsList();

	boolean augmented = true;

	//
	// checks that all the resources have been augmented
	//
	for (GSResource gsResource : resultsList) {

	    AugmentedMetadataElement element = gsResource.getHarmonizedMetadata().getAugmentedMetadataElements().get(0);
	    String newValue = element.getNewValue();
	    String title = gsResource.getHarmonizedMetadata().getCoreMetadata().getTitle();

	    augmented &= newValue.equals(title);
	}

	Assert.assertTrue(augmented);
    }

    private void harvestOAIPMH() throws Exception {

	// clears the volatile db
	StorageInfo databaseURI = ConfigurationWrapper.getDatabaseURI();
	Database dataBase = DatabaseFactory.get(databaseURI);
	VolatileDatabase database = (VolatileDatabase) dataBase;
	database.clear();

	//
	// Harvest OAIPMH records in to the VolatileDB
	//

	HarvestingSetting workerSetting = ConfigurationWrapper.getHarvestingSettings().//
		stream().//
		filter(s -> s.getSelectedAccessorSetting().getAccessorType().equals(OAIPMHAccessor.TYPE)).//
		findFirst().//
		get();

	OAIPMHConnectorSetting oaiConnectorSetting = (OAIPMHConnectorSetting) workerSetting.//
		getSelectedAccessorSetting().//
		getHarvestedConnectorSetting();

	oaiConnectorSetting.setMaxRecords(50);
	oaiConnectorSetting.setPreferredPrefix("oai_dc");

	HarvesterWorker harvesterWorker = new HarvesterWorker();
	harvesterWorker.configure(workerSetting);

	harvesterWorker.startHarvesting(false);
    }
}
