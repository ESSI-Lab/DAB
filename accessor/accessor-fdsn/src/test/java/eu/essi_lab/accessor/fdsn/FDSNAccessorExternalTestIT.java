package eu.essi_lab.accessor.fdsn;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.cfga.gs.setting.GSSourceSetting;
import eu.essi_lab.cfga.gs.setting.accessor.AccessorSetting;
import eu.essi_lab.cfga.gs.setting.connector.DistributedConnectorSetting;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.ReducedDiscoveryMessage;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.count.DiscoveryCountResponse;
import eu.essi_lab.messages.termfrequency.TermFrequencyItem;
import eu.essi_lab.messages.termfrequency.TermFrequencyMap;
import eu.essi_lab.messages.termfrequency.TermFrequencyMap.TermFrequencyTarget;
import eu.essi_lab.model.BrokeringStrategy;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;

/**
 * @author Fabrizio
 */
public class FDSNAccessorExternalTestIT {

    @Test
    public void queryMethodTest() throws GSException {

	FDSNAccessor accessor = new FDSNAccessor();

	//
	// Accessor setting must be DISTRIBUTED
	//

	AccessorSetting accessorSetting = accessor.getSetting();

	Assert.assertEquals(BrokeringStrategy.DISTRIBUTED, accessorSetting.getBrokeringStrategy());

	Assert.assertEquals("FDSN", accessorSetting.getConfigurableType());

	//
	// The connector setting source has already endpoint and label, but id is always required
	//

	DistributedConnectorSetting distributedConnectorSetting = accessorSetting.getDistributedConnectorSetting();

	Assert.assertEquals(FDSNConnectorSetting.class, distributedConnectorSetting.getClass());

	GSSourceSetting gsSourceSetting = accessorSetting.getGSSourceSetting();

	gsSourceSetting.setSourceIdentifier("usgs");

	Assert.assertNotNull(gsSourceSetting.getSourceEndpoint());
	Assert.assertNotNull(gsSourceSetting.getSourceLabel());

	GSSource accessorSource = accessor.getSource();

	Assert.assertEquals(gsSourceSetting.asSource(), accessorSource);

	//
	// The connector must be found
	//
	FDSNConnector connector = accessor.getConnector();

	Assert.assertEquals(FDSNConnector.class, connector.getClass());

	FDSNConnectorSetting oaiConnectorSetting = connector.getSetting();

	// the connector setting is the same
	Assert.assertEquals(distributedConnectorSetting, oaiConnectorSetting);

	boolean ignoreComplexQueries = oaiConnectorSetting.isIgnoreComplexQueries();

	Assert.assertTrue(ignoreComplexQueries);

	//
	// query method test
	//

	DiscoveryMessage discoveryMessage = new DiscoveryMessage();
	discoveryMessage.setPage(new Page(1, 10));

	ReducedDiscoveryMessage message = new ReducedDiscoveryMessage(discoveryMessage, null);

	ResultSet<GSResource> resultSet = accessor.query(message, message.getPage());

	Assert.assertEquals(10, resultSet.getResultsList().size());
    }

    @Test
    public void countMethodTest() throws GSException {

	FDSNAccessor accessor = new FDSNAccessor();

	//
	// Accessor setting must be DISTRIBUTED
	//

	AccessorSetting accessorSetting = accessor.getSetting();

	Assert.assertEquals(BrokeringStrategy.DISTRIBUTED, accessorSetting.getBrokeringStrategy());

	Assert.assertEquals("FDSN", accessorSetting.getConfigurableType());

	//
	// The connector setting source has already endpoint and label, but id is always required
	//

	DistributedConnectorSetting distributedConnectorSetting = accessorSetting.getDistributedConnectorSetting();

	Assert.assertEquals(FDSNConnectorSetting.class, distributedConnectorSetting.getClass());

	GSSourceSetting gsSourceSetting = accessorSetting.getGSSourceSetting();

	gsSourceSetting.setSourceIdentifier("usgs");

	Assert.assertNotNull(gsSourceSetting.getSourceEndpoint());
	Assert.assertNotNull(gsSourceSetting.getSourceLabel());

	GSSource accessorSource = accessor.getSource();

	Assert.assertEquals(gsSourceSetting.asSource(), accessorSource);

	//
	// The connector must be found
	//
	FDSNConnector connector = accessor.getConnector();

	Assert.assertEquals(FDSNConnector.class, connector.getClass());

	FDSNConnectorSetting oaiConnectorSetting = connector.getSetting();

	// the connector setting is the same
	Assert.assertEquals(distributedConnectorSetting, oaiConnectorSetting);

	boolean ignoreComplexQueries = oaiConnectorSetting.isIgnoreComplexQueries();

	Assert.assertTrue(ignoreComplexQueries);

	//
	// query method test
	//

	DiscoveryMessage discoveryMessage = new DiscoveryMessage();
	discoveryMessage.setPage(new Page(1, 10));

	ReducedDiscoveryMessage message = new ReducedDiscoveryMessage(discoveryMessage, null);

	DiscoveryCountResponse countResponse = accessor.count(message);

	int count = countResponse.getCount();

	GSLoggerFactory.getLogger(getClass()).debug("Count result: " + count);

	Assert.assertTrue(count > 1000);

	TermFrequencyMap termFrequencyMap = countResponse.getTermFrequencyMap().get();
	List<TermFrequencyItem> items = termFrequencyMap.getItems(TermFrequencyTarget.SOURCE);

	Assert.assertEquals(1, items.size());

	Assert.assertEquals(count, items.get(0).getFreq());
    }
}
