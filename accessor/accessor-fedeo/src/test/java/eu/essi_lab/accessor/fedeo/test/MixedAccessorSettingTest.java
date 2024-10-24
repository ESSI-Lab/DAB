package eu.essi_lab.accessor.fedeo.test;

import static org.junit.Assert.fail;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import eu.essi_lab.accessor.fedeo.FEDEOMixedSettingFactory;
import eu.essi_lab.accessor.fedeo.distributed.FEDEOGranulesConnector;
import eu.essi_lab.accessor.fedeo.distributed.FEDEOGranulesConnectorSetting;
import eu.essi_lab.accessor.fedeo.distributed.FEDEOMixedDistributedAccessor;
import eu.essi_lab.accessor.fedeo.harvested.FEDEOCollectionConnector;
import eu.essi_lab.accessor.fedeo.harvested.FEDEOCollectionConnectorSetting;
import eu.essi_lab.accessor.fedeo.harvested.FEDEOMixedHarvestedAccessor;
import eu.essi_lab.adk.AccessorFactory;
import eu.essi_lab.adk.distributed.IDistributedAccessor;
import eu.essi_lab.adk.harvest.IHarvestedAccessor;
import eu.essi_lab.cdk.harvest.IHarvestedQueryConnector;
import eu.essi_lab.cdk.query.IDistributedQueryConnector;
import eu.essi_lab.cfga.gs.setting.accessor.AccessorSetting;
import eu.essi_lab.cfga.gs.setting.connector.DistributedConnectorSetting;
import eu.essi_lab.cfga.gs.setting.connector.HarvestedConnectorSetting;
import eu.essi_lab.model.BrokeringStrategy;

/**
 * @author Fabrizio
 */
public class MixedAccessorSettingTest {

    @Test
    public void distConnectorSettingTest() throws Exception {

	FEDEOMixedDistributedAccessor accessor = new FEDEOMixedDistributedAccessor();

	DistributedConnectorSetting generalTypeSetting = accessor.getSetting().getDistributedConnectorSetting();

	FEDEOGranulesConnectorSetting specificTypeSetting = accessor.getConnector().getSetting();

	Assert.assertEquals(generalTypeSetting, specificTypeSetting);
    }

    @Test
    public void harvConnectorSettingTest() throws Exception {

	FEDEOMixedHarvestedAccessor accessor = new FEDEOMixedHarvestedAccessor();

	HarvestedConnectorSetting generalTypeSetting = accessor.getSetting().getHarvestedConnectorSetting();

	FEDEOCollectionConnectorSetting specificTypeSetting = accessor.getConnector().getSetting();

	Assert.assertEquals(generalTypeSetting, specificTypeSetting);
    }

    /**
     * Mixed accessors must provide exactly the same setting and the same source
     * 
     * @throws Exception
     */
    @Test
    public void equalityTest() throws Exception {

	FEDEOMixedDistributedAccessor mixedDistributedAccessor = new FEDEOMixedDistributedAccessor();
	FEDEOMixedHarvestedAccessor mixedHarvestedAccessor = new FEDEOMixedHarvestedAccessor();

	Assert.assertEquals(mixedDistributedAccessor.getSetting(), mixedHarvestedAccessor.getSetting());

	Assert.assertEquals(mixedDistributedAccessor.getSetting().getSource(), mixedHarvestedAccessor.getSetting().getSource());
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void wrapperTest() throws Exception {

	FEDEOMixedDistributedAccessor mixedDistributedAccessor = new FEDEOMixedDistributedAccessor();
	FEDEOMixedHarvestedAccessor mixedHarvestedAccessor = new FEDEOMixedHarvestedAccessor();

	IDistributedAccessor createdDistAccessor = AccessorFactory
		.getConfiguredDistributedAccessor(mixedDistributedAccessor.getSetting());

	Assert.assertEquals(mixedDistributedAccessor.getType(), createdDistAccessor.getType());

	IHarvestedAccessor createdHarvAccessor = AccessorFactory.getConfiguredHarvestedAccessor(mixedDistributedAccessor.getSetting());

	Assert.assertEquals(mixedHarvestedAccessor.getType(), createdHarvAccessor.getType());
    }

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    /**
     * Mixed accessor settings do not have its own single accessor type
     * they have two of them, a distributed and a harvested one
     */
    @Test
    public void configurableTypeTest() {

	AccessorSetting setting = FEDEOMixedSettingFactory.createMixedSetting();

	thrown.expect(RuntimeException.class);
	setting.getConfigurableType();

	String harvestedAccessorType = setting.getHarvestedAccessorType();
	Assert.assertEquals(FEDEOMixedHarvestedAccessor.ACCESSOR_TYPE, harvestedAccessorType);

	String distributedAccessorType = setting.getDistributedAccessorType();
	Assert.assertEquals(FEDEOMixedDistributedAccessor.ACCESSOR_TYPE, distributedAccessorType);
    }

    /**
     * 
     */
    @Test
    public void contentTest() {

	FEDEOMixedDistributedAccessor accessor = new FEDEOMixedDistributedAccessor();

	AccessorSetting setting = accessor.getSetting();

	Assert.assertEquals(BrokeringStrategy.MIXED, setting.getBrokeringStrategy());

	try {
	    setting.getConfigurableType();
	    fail("Ex not thrown");
	} catch (RuntimeException ex) {
	    // no configurable type is set
	}

	//
	//
	//

	String harvestedAccessorType = setting.getHarvestedAccessorType();
	Assert.assertEquals(FEDEOMixedHarvestedAccessor.ACCESSOR_TYPE, harvestedAccessorType);

	String distributedAccessorType = setting.getDistributedAccessorType();
	Assert.assertEquals(FEDEOMixedDistributedAccessor.ACCESSOR_TYPE, distributedAccessorType);

	HarvestedConnectorSetting harvestedConnectorSetting = setting.getHarvestedConnectorSetting();
	DistributedConnectorSetting distributedConnectorSetting = setting.getDistributedConnectorSetting();

	// Assert.assertEquals(harvestedConnectorSetting.getGSSourceSetting(),
	// distributedConnectorSetting.getGSSourceSetting());

	//
	//
	//
	{
	    String configurableType = harvestedConnectorSetting.getConfigurableType();

	    Assert.assertEquals(FEDEOCollectionConnector.CONNECTOR_TYPE, configurableType);

	    try {
		@SuppressWarnings("rawtypes")
		IHarvestedQueryConnector connector = harvestedConnectorSetting.createConfigurable();
		Assert.assertEquals(FEDEOCollectionConnector.class, connector.getClass());

	    } catch (Exception e) {
		e.printStackTrace();
		fail(e.getMessage());
	    }

	    // GSSource asSource = harvestedConnectorSetting.getGSSourceSetting().asSource();
	    // Assert.assertEquals(BrokeringStrategy.MIXED, asSource.getBrokeringStrategy());
	    //
	    // String endpoint = asSource.getEndpoint();
	    // Assert.assertNotNull(endpoint);
	    //
	    // String label = asSource.getLabel();
	    // Assert.assertNotNull(label);
	    //
	    // // source identifier must be set
	    // String uniqueIdentifier = asSource.getUniqueIdentifier();
	    // Assert.assertNull(uniqueIdentifier);
	}

	{
	    String configurableType = distributedConnectorSetting.getConfigurableType();

	    Assert.assertEquals(FEDEOGranulesConnector.CONNECTOR_TYPE, configurableType);

	    try {
		@SuppressWarnings("rawtypes")
		IDistributedQueryConnector connector = distributedConnectorSetting.createConfigurable();
		Assert.assertEquals(FEDEOGranulesConnector.class, connector.getClass());

	    } catch (Exception e) {
		e.printStackTrace();
		fail(e.getMessage());
	    }

	    // GSSource asSource = distributedConnectorSetting.getGSSourceSetting().asSource();
	    // Assert.assertEquals(BrokeringStrategy.MIXED, asSource.getBrokeringStrategy());
	    //
	    // String endpoint = asSource.getEndpoint();
	    // Assert.assertNotNull(endpoint);
	    //
	    // String label = asSource.getLabel();
	    // Assert.assertNotNull(label);
	    //
	    // // source identifier must be set
	    // String uniqueIdentifier = asSource.getUniqueIdentifier();
	    // Assert.assertNull(uniqueIdentifier);

	}

    }
}
