package eu.essi_lab.accessor.fedeo.distributed;

import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import eu.essi_lab.cfga.gs.setting.GSSourceSetting;
import eu.essi_lab.cfga.gs.setting.accessor.AccessorSetting;
import eu.essi_lab.cfga.gs.setting.connector.DistributedConnectorSetting;
import eu.essi_lab.lib.xml.XMLDocumentReader;
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
public class FEDEODistributedAccessorExternalTestIT {

    boolean passed;
    private ReducedDiscoveryMessage message;
    private FEDEOMixedDistributedAccessor accessor;

    //
    // the following identifier should work
    //
    private static final String FEDEO_COLLECTION_OSDD = "https://fedeo.ceos.org/opensearch/description.xml?parentIdentifier=urn:eop:VITO:CGS_S1_GRD_L1&startDate=2015-07-06T00:00:00.000Z&endDate=2021-12-31T23:59:00.000Z";

    @Before
    public void before() {

	passed = true;

	accessor = new FEDEOMixedDistributedAccessor();

	//
	// Accessor setting must be DISTRIBUTED
	//

	AccessorSetting accessorSetting = accessor.getSetting();

	Assert.assertEquals(BrokeringStrategy.MIXED, accessorSetting.getBrokeringStrategy());

	Assert.assertEquals("FEDEOMixedDistributed", accessorSetting.getDistributedAccessorType());

	//
	// The connector setting needs the source endpoint
	//

	DistributedConnectorSetting distributedConnectorSetting = accessorSetting.getDistributedConnectorSetting();

	Assert.assertEquals(FEDEOGranulesConnectorSetting.class, distributedConnectorSetting.getClass());

	GSSourceSetting gsSourceSetting = accessorSetting.getGSSourceSetting();

	gsSourceSetting.setSourceIdentifier("sourceId");

	GSSource accessorSource = accessor.getSource();

	Assert.assertEquals(gsSourceSetting.asSource(), accessorSource);

	//
	// The connector must be found
	//
	FEDEOGranulesConnector connector = accessor.getConnector();

	Assert.assertEquals(FEDEOGranulesConnector.class, connector.getClass());

	FEDEOGranulesConnectorSetting oaiConnectorSetting = connector.getSetting();

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
	collection.getExtensionHandler().setFEDEOSecondLevelInfo(FEDEO_COLLECTION_OSDD);
	collection.setPublicId("parentCollectionId");

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

	Assert.assertEquals(10, resultSet.getResultsList().size());

	resultSet.getResultsList().forEach(r -> {

	    OriginalMetadata originalMetadata = r.getOriginalMetadata();

	    String metadata = originalMetadata.getMetadata();

	    try {

		XMLDocumentReader reader = new XMLDocumentReader(metadata);

		passed &= reader.asString().contains("entry");

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

	Assert.assertTrue(count > 20000);

	TermFrequencyMap termFrequencyMap = countResponse.getTermFrequencyMap().get();
	List<TermFrequencyItem> items = termFrequencyMap.getItems(TermFrequencyTarget.SOURCE);

	Assert.assertEquals(1, items.size());

	Assert.assertEquals(count, items.get(0).getFreq());

    }
}
