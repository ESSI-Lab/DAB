package eu.essi_lab.accessor.gbif.distributed.test;

import static org.junit.Assert.fail;

import java.util.List;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import eu.essi_lab.accessor.gbif.distributed.GBIFDistributedConnector;
import eu.essi_lab.accessor.gbif.distributed.GBIFDistributedConnectorSetting;
import eu.essi_lab.accessor.gbif.distributed.GBIFMixedDistributedAccessor;
import eu.essi_lab.cfga.gs.setting.GSSourceSetting;
import eu.essi_lab.cfga.gs.setting.accessor.AccessorSetting;
import eu.essi_lab.cfga.gs.setting.connector.DistributedConnectorSetting;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.ReducedDiscoveryMessage;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.bond.BondFactory;
import eu.essi_lab.messages.bond.BondOperator;
import eu.essi_lab.messages.bond.SimpleValueBond;
import eu.essi_lab.messages.count.DiscoveryCountResponse;
import eu.essi_lab.messages.termfrequency.TermFrequencyItem;
import eu.essi_lab.messages.termfrequency.TermFrequencyMap;
import eu.essi_lab.messages.termfrequency.TermFrequencyMap.TermFrequencyTarget;
import eu.essi_lab.model.BrokeringStrategy;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.DatasetCollection;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.model.resource.OriginalMetadata;

/**
 * @author Fabrizio
 */
public class GBIFDistributedAccessorExternalTestIT {

    boolean passed;
    private ReducedDiscoveryMessage message;
    private GBIFMixedDistributedAccessor accessor;

    //
    // the following identifier should work
    //
    private static final String GBIF_ORIGINAL_ID = "https://www.gbif.org/dataset/5888c533-f265-41c3-9078-bf0630ef4aa7";

    @Before
    public void before() {

	passed = true;

	accessor = new GBIFMixedDistributedAccessor();

	//
	// Accessor setting must be DISTRIBUTED
	//

	AccessorSetting accessorSetting = accessor.getSetting();

	Assert.assertEquals(BrokeringStrategy.MIXED, accessorSetting.getBrokeringStrategy());

	Assert.assertEquals("GBIFMixedDistributed", accessorSetting.getDistributedAccessorType());

	//
	// The connector setting needs the source endpoint
	//

	DistributedConnectorSetting distributedConnectorSetting = accessorSetting.getDistributedConnectorSetting();

	Assert.assertEquals(GBIFDistributedConnectorSetting.class, distributedConnectorSetting.getClass());

	GSSourceSetting gsSourceSetting = accessorSetting.getGSSourceSetting();

	gsSourceSetting.setSourceIdentifier("sourceId");

	GSSource accessorSource = accessor.getSource();

	Assert.assertEquals(gsSourceSetting.asSource(), accessorSource);

	//
	// The connector must be found
	//
	GBIFDistributedConnector connector = accessor.getConnector();

	Assert.assertEquals(GBIFDistributedConnector.class, connector.getClass());

	GBIFDistributedConnectorSetting oaiConnectorSetting = connector.getSetting();

	// the connector setting is the same
	Assert.assertEquals(distributedConnectorSetting, oaiConnectorSetting);

	//
	//
	//

	DiscoveryMessage discoveryMessage = new DiscoveryMessage();
	discoveryMessage.setPage(new Page(1, 10));

	//
	// creates and a parent resource, the following identifier "eop:DLR:EOWEB:IRS-P6.AWiFS.P" should work
	//
	DatasetCollection collection = new DatasetCollection();
	collection.setPublicId("parentCollectionId");
	collection.setOriginalId(GBIF_ORIGINAL_ID);

	discoveryMessage.addParentGSResource(collection);

	//
	//
	//

	SimpleValueBond parentBond = BondFactory.createSimpleValueBond(//
		BondOperator.EQUAL, //
		MetadataElement.PARENT_IDENTIFIER, //
		collection.getPublicId());

	discoveryMessage.setNormalizedBond(parentBond);
	discoveryMessage.setPermittedBond(parentBond);
	discoveryMessage.setUserBond(parentBond);

	message = new ReducedDiscoveryMessage(discoveryMessage, parentBond);
    }

    /**
     * @throws GSException
     */
    @Test
    public void queryMethodTest() throws GSException {

	ResultSet<GSResource> resultSet = accessor.query(message, message.getPage());

	Assert.assertFalse(resultSet.getResultsList().isEmpty());

	resultSet.getResultsList().forEach(r -> {

	    OriginalMetadata originalMetadata = r.getOriginalMetadata();

	    String metadata = originalMetadata.getMetadata();

	    try {

		JSONObject object = new JSONObject(metadata);

		passed &= object.has("gbifID");

	    } catch (Exception e) {
		e.printStackTrace();
		fail(e.getMessage());
	    }
	});

	Assert.assertTrue(passed);
    }

    /**
     * @throws GSException
     */
    @Test
    public void countMethodTest() throws GSException {

	DiscoveryCountResponse countResponse = accessor.count(message);

	int count = countResponse.getCount();

	Assert.assertTrue(count > 0);

	TermFrequencyMap termFrequencyMap = countResponse.getTermFrequencyMap().get();
	List<TermFrequencyItem> items = termFrequencyMap.getItems(TermFrequencyTarget.SOURCE);

	Assert.assertEquals(1, items.size());

	Assert.assertEquals(count, items.get(0).getFreq());
    }
}
