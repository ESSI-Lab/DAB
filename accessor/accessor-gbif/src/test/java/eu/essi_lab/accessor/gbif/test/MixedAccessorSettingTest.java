package eu.essi_lab.accessor.gbif.test;

import static org.junit.Assert.fail;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import eu.essi_lab.accessor.gbif.GBIFMixedSettingFactory;
import eu.essi_lab.accessor.gbif.distributed.GBIFDistributedConnector;
import eu.essi_lab.accessor.gbif.distributed.GBIFDistributedConnectorSetting;
import eu.essi_lab.accessor.gbif.distributed.GBIFMixedDistributedAccessor;
import eu.essi_lab.accessor.gbif.harvested.GBIFHarvestedConnector;
import eu.essi_lab.accessor.gbif.harvested.GBIFHarvestedConnectorSetting;
import eu.essi_lab.accessor.gbif.harvested.GBIFMixedHarvestedAccessor;
import eu.essi_lab.adk.AccessorFactory;
import eu.essi_lab.adk.distributed.IDistributedAccessor;
import eu.essi_lab.adk.harvest.IHarvestedAccessor;
import eu.essi_lab.cdk.harvest.IHarvestedQueryConnector;
import eu.essi_lab.cdk.query.IDistributedQueryConnector;
import eu.essi_lab.cfga.gs.setting.accessor.AccessorSetting;
import eu.essi_lab.cfga.gs.setting.connector.DistributedConnectorSetting;
import eu.essi_lab.cfga.gs.setting.connector.HarvestedConnectorSetting;
import eu.essi_lab.model.BrokeringStrategy;
import eu.essi_lab.model.GSSource;

/**
 * @author Fabrizio
 */
public class MixedAccessorSettingTest {

    @Test
    public void distConnectorSettingTest() throws Exception {

	GBIFMixedDistributedAccessor accessor = new GBIFMixedDistributedAccessor();

	DistributedConnectorSetting generalTypeSetting = accessor.getSetting().getDistributedConnectorSetting();

	GBIFDistributedConnectorSetting specificTypeSetting = accessor.getConnector().getSetting();

	Assert.assertEquals(generalTypeSetting, specificTypeSetting);
    }

    @Test
    public void harvConnectorSettingTest() throws Exception {

	GBIFMixedHarvestedAccessor accessor = new GBIFMixedHarvestedAccessor();

	HarvestedConnectorSetting generalTypeSetting = accessor.getSetting().getHarvestedConnectorSetting();

	GBIFHarvestedConnectorSetting specificTypeSetting = accessor.getConnector().getSetting();

	Assert.assertEquals(generalTypeSetting, specificTypeSetting);
    }

    /**
     * Mixed accessors must provide exactly the same setting and the same source
     * 
     * @throws Exception
     */
    @Test
    public void equalityTest() throws Exception {

	GBIFMixedDistributedAccessor mixedDistributedAccessor = new GBIFMixedDistributedAccessor();
	GBIFMixedHarvestedAccessor mixedHarvestedAccessor = new GBIFMixedHarvestedAccessor();

	Assert.assertEquals(mixedDistributedAccessor.getSetting(), mixedHarvestedAccessor.getSetting());

	Assert.assertEquals(mixedDistributedAccessor.getSetting().getSource(), mixedHarvestedAccessor.getSetting().getSource());
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void wrapperTest() throws Exception {

	GBIFMixedDistributedAccessor mixedDistributedAccessor = new GBIFMixedDistributedAccessor();
	GBIFMixedHarvestedAccessor mixedHarvestedAccessor = new GBIFMixedHarvestedAccessor();

	IDistributedAccessor createdDistAccessor = AccessorFactory.getConfiguredDistributedAccessor(mixedDistributedAccessor.getSetting());

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

	AccessorSetting setting = GBIFMixedSettingFactory.createMixedSetting();

	thrown.expect(RuntimeException.class);
	setting.getConfigurableType();

	String harvestedAccessorType = setting.getHarvestedAccessorType();
	Assert.assertEquals(GBIFMixedHarvestedAccessor.ACCESSOR_TYPE, harvestedAccessorType);

	String distributedAccessorType = setting.getDistributedAccessorType();
	Assert.assertEquals(GBIFMixedDistributedAccessor.ACCESSOR_TYPE, distributedAccessorType);
    }

    /**
     * 
     */
    @Test
    public void contentTest() {

	GBIFMixedDistributedAccessor accessor = new GBIFMixedDistributedAccessor();

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
	Assert.assertEquals(GBIFMixedHarvestedAccessor.ACCESSOR_TYPE, harvestedAccessorType);

	String distributedAccessorType = setting.getDistributedAccessorType();
	Assert.assertEquals(GBIFMixedDistributedAccessor.ACCESSOR_TYPE, distributedAccessorType);

	HarvestedConnectorSetting harvestedConnectorSetting = setting.getHarvestedConnectorSetting();
	DistributedConnectorSetting distributedConnectorSetting = setting.getDistributedConnectorSetting();

	// Assert.assertEquals(harvestedConnectorSetting.getGSSourceSetting(),
	// distributedConnectorSetting.getGSSourceSetting());

	GSSource asSource = setting.getGSSourceSetting().asSource();
	Assert.assertEquals(BrokeringStrategy.MIXED, asSource.getBrokeringStrategy());

	String endpoint = asSource.getEndpoint();
	Assert.assertNotNull(endpoint);

	String label = asSource.getLabel();
	Assert.assertNotNull(label);

	// source identifier must be set
	String uniqueIdentifier = asSource.getUniqueIdentifier();
	Assert.assertNull(uniqueIdentifier);

	//
	//
	//
	{
	    String configurableType = harvestedConnectorSetting.getConfigurableType();

	    Assert.assertEquals(GBIFHarvestedConnector.CONNECTOR_TYPE, configurableType);

	    try {
		@SuppressWarnings("rawtypes")
		IHarvestedQueryConnector connector = harvestedConnectorSetting.createConfigurable();
		Assert.assertEquals(GBIFHarvestedConnector.class, connector.getClass());

	    } catch (Exception e) {
		e.printStackTrace();
		fail(e.getMessage());
	    }
	}

	{
	    String configurableType = distributedConnectorSetting.getConfigurableType();

	    Assert.assertEquals(GBIFDistributedConnector.CONNECTOR_TYPE, configurableType);

	    try {
		@SuppressWarnings("rawtypes")
		IDistributedQueryConnector connector = distributedConnectorSetting.createConfigurable();
		Assert.assertEquals(GBIFDistributedConnector.class, connector.getClass());

	    } catch (Exception e) {
		e.printStackTrace();
		fail(e.getMessage());
	    }

	}
    }
}
