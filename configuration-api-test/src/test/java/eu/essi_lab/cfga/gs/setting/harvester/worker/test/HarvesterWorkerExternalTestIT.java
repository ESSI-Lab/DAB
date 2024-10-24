package eu.essi_lab.cfga.gs.setting.harvester.worker.test;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.xml.bind.JAXBElement;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.accessor.oaipmh.OAIPMHConnectorSetting;
import eu.essi_lab.api.database.Database;
import eu.essi_lab.api.database.DatabaseFinder;
import eu.essi_lab.api.database.DatabaseReader;
import eu.essi_lab.api.database.SourceStorage;
import eu.essi_lab.api.database.factory.DatabaseProviderFactory;
import eu.essi_lab.api.database.factory.DatabaseFactory;
import eu.essi_lab.api.database.vol.VolatileDatabase;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.DefaultConfiguration;
import eu.essi_lab.cfga.gs.setting.harvesting.HarvestingSetting;
import eu.essi_lab.cfga.scheduler.Scheduler;
import eu.essi_lab.cfga.scheduler.Scheduler.JobEvent;
import eu.essi_lab.cfga.scheduler.SchedulerFactory;
import eu.essi_lab.cfga.setting.scheduling.SchedulerSetting;
import eu.essi_lab.harvester.worker.HarvesterWorker;
import eu.essi_lab.jaxb.common.CommonContext;
import eu.essi_lab.jaxb.csw._2_0_2.RecordType;
import eu.essi_lab.jaxb.csw._2_0_2.org.purl.dc.elements._1.SimpleLiteral;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.HarvestingProperties;
import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.count.DiscoveryCountResponse;
import eu.essi_lab.messages.termfrequency.TermFrequencyItem;
import eu.essi_lab.messages.termfrequency.TermFrequencyMap;
import eu.essi_lab.messages.termfrequency.TermFrequencyMap.TermFrequencyTarget;
import eu.essi_lab.model.StorageInfo;
import eu.essi_lab.model.resource.GSResource;

/**
 * @author Fabrizio
 */
public class HarvesterWorkerExternalTestIT {

    /**
     * 
     */
    private volatile boolean jobExecuted = false;

    /**
     * @throws Exception
     */
    @Test
    public void test() throws Exception {

	DefaultConfiguration configuration = new DefaultConfiguration(UUID.randomUUID().toString() + ".json");
	configuration.flush();

	ConfigurationWrapper.setConfiguration(configuration);

	// clears the volatile db
	StorageInfo databaseURI = ConfigurationWrapper.getDatabaseURI();
	Database provider = DatabaseFactory.create(databaseURI);
	VolatileDatabase database = (VolatileDatabase) provider;
	database.clear();

	//
	// Retrieves the setting from the configuration and edits it
	//

	HarvestingSetting workerSetting = ConfigurationWrapper.getHarvestingSettings().//
		stream().//
		filter(s -> s.getSelectedAccessorSetting().getName().equals("OAIPMH Accessor")).//
		findFirst().//
		get();

	workerSetting.getScheduling().setEnabled(true); // enables the scheduling

	// max 50 records
	workerSetting.getSelectedAccessorSetting().getHarvestedConnectorSetting().setMaxRecords(50);

	OAIPMHConnectorSetting oaiSetting = (OAIPMHConnectorSetting) workerSetting.getSelectedAccessorSetting()
		.getHarvestedConnectorSetting();

	// set oai_dc preferred prefix
	oaiSetting.setPreferredPrefix("oai_dc");

	//
	// Creates a worker and configures it
	//

	HarvesterWorker harvesterWorker = new HarvesterWorker();
	harvesterWorker.configure(workerSetting);

	//
	// Get the scheduler
	//

	SchedulerSetting schedulerSetting = ConfigurationWrapper.getSchedulerSetting();
	Scheduler scheduler = SchedulerFactory.getScheduler(schedulerSetting);

	//
	// Adds a listener which notifies harvesting ended
	//

	scheduler.addJobEventListener((e, c, ex) -> jobExecuted = true, //
		JobEvent.JOB_EXECUTED, //
		UUID.randomUUID().toString(), //
		true); //

	//
	// Makes some test on the empty DB
	//

	DatabaseReader reader = DatabaseProviderFactory.getDatabaseReader(ConfigurationWrapper.getDatabaseURI());
	DatabaseFinder finder = DatabaseProviderFactory.getDatabaseFinder(ConfigurationWrapper.getDatabaseURI());

	DiscoveryCountResponse count = finder.count(new DiscoveryMessage());

	Assert.assertEquals(0, count.getCount());

	DiscoveryMessage message = new DiscoveryMessage();

	Page page = new Page(10);
	message.setPage(page);

	message.setSources(Arrays.asList(workerSetting.//
		getSelectedAccessorSetting().//

		getGSSourceSetting().//
		asSource()));

	ResultSet<GSResource> resultSet = finder.discover(message);
	List<GSResource> resultsList = resultSet.getResultsList();

	Assert.assertEquals(0, resultsList.size());

	SourceStorage sourceStorage = DatabaseProviderFactory.getSourceStorage(ConfigurationWrapper.getDatabaseURI());

	HarvestingProperties harvestingProperties = sourceStorage
		.retrieveHarvestingProperties(workerSetting.getSelectedAccessorSetting().getSource());

	int harvestingCount = harvestingProperties.getHarvestingCount();
	Assert.assertEquals(0, harvestingCount);

	//
	// Schedules the harvesting and waits
	//

	scheduler.schedule(workerSetting);

	while (!jobExecuted) {
	}

	//
	// Makes final tests
	//

	count = finder.count(message);

	Assert.assertEquals(50, count.getCount());

	TermFrequencyMap termFrequencyMap = count.getTermFrequencyMap().get();

	List<TermFrequencyItem> items = termFrequencyMap.getItems(TermFrequencyTarget.SOURCE);
	Assert.assertEquals(1, items.size());

	TermFrequencyItem termFrequencyItem = items.get(0);
	Assert.assertEquals(50, termFrequencyItem.getFreq());

	resultSet = finder.discover(message);
	resultsList = resultSet.getResultsList();

	Assert.assertEquals(10, resultsList.size());

	for (GSResource gsResource : resultsList) {

	    String metadata = gsResource.getOriginalMetadata().getMetadata();

	    @SuppressWarnings("rawtypes")
	    JAXBElement record = CommonContext.unmarshal(metadata, JAXBElement.class);

	    eu.essi_lab.jaxb.csw._2_0_2.RecordType type = (RecordType) record.getValue();

	    List<JAXBElement<SimpleLiteral>> dcElements = type.getDCElements();

	    Assert.assertFalse(dcElements.isEmpty());
	}

	harvestingProperties = sourceStorage.retrieveHarvestingProperties(workerSetting.getSelectedAccessorSetting().getSource());

	harvestingCount = harvestingProperties.getHarvestingCount();
	Assert.assertEquals(1, harvestingCount);

	int resourcesCount = harvestingProperties.getResourcesCount();
	Assert.assertEquals(50, resourcesCount);

	String startHarvestingTimestamp = harvestingProperties.getStartHarvestingTimestamp();
	Assert.assertNotNull(startHarvestingTimestamp);

	String endHarvestingTimestamp = harvestingProperties.getEndHarvestingTimestamp();
	Assert.assertNotNull(endHarvestingTimestamp);

	Assert.assertTrue(endHarvestingTimestamp.compareTo(startHarvestingTimestamp) > 0);

	String recoveryRemovalToken = harvestingProperties.getRecoveryRemovalToken();
	Assert.assertNotNull(recoveryRemovalToken);

	String recoveryResumptionToken = harvestingProperties.getRecoveryResumptionToken();
	Assert.assertNotNull(recoveryResumptionToken);
    }
}
