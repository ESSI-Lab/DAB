package eu.essi_lab.accessor.fdsn;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.cfga.gs.setting.accessor.AccessorSetting;
import eu.essi_lab.cfga.gs.setting.connector.DistributedConnectorSetting;
import eu.essi_lab.cfga.gs.setting.connector.HarvestedConnectorSetting;
import eu.essi_lab.model.BrokeringStrategy;
import eu.essi_lab.model.GSSource;

/**
 * @author Fabrizio
 */
public class FDSNAccessorSettingTest {

    @Test
    public void connectorSettingTest() throws Exception {

	FDSNAccessor accessor = new FDSNAccessor();

	DistributedConnectorSetting generalTypeSetting = accessor.getSetting().getDistributedConnectorSetting();

	FDSNConnectorSetting specificTypeSetting = accessor.getConnector().getSetting();

	Assert.assertEquals(generalTypeSetting, specificTypeSetting);
    }

    @Test
    public void test() throws Exception {

	FDSNAccessor accessor = new FDSNAccessor();
	AccessorSetting setting = accessor.getSetting();

	//
	//
	//

	BrokeringStrategy brokeringStrategy = setting.getBrokeringStrategy();
	Assert.assertEquals(BrokeringStrategy.DISTRIBUTED, brokeringStrategy);

	//
	//
	//

	String configurableType = setting.getConfigurableType();
	Assert.assertEquals(FDSNAccessor.TYPE, configurableType);

	//
	//
	//

	DistributedConnectorSetting distConnectorSetting = setting.getDistributedConnectorSetting();
	Assert.assertNotNull(distConnectorSetting);

	FDSNConnector connector = distConnectorSetting.createConfigurable();
	Assert.assertEquals(FDSNConnector.class, connector.getClass());

	//
	//
	//

	GSSource source = setting.getSource();
	GSSource asSource = setting.getGSSourceSetting().asSource();

	Assert.assertEquals(source, asSource);

	Assert.assertEquals(BrokeringStrategy.DISTRIBUTED, source.getBrokeringStrategy());
	Assert.assertEquals(BrokeringStrategy.DISTRIBUTED, asSource.getBrokeringStrategy());

	Assert.assertNull(source.getUniqueIdentifier());
	Assert.assertNull(source.getUniqueIdentifier());

	Assert.assertNotNull(source.getLabel());
	Assert.assertNotNull(asSource.getLabel());

	Assert.assertNotNull(source.getEndpoint());
	Assert.assertNotNull(asSource.getEndpoint());

	//
	//
	//

	String harvAccessorType = setting.getHarvestedAccessorType();
	Assert.assertNull(harvAccessorType);

	HarvestedConnectorSetting harvConnectorSetting = setting.getHarvestedConnectorSetting();
	Assert.assertNull(harvConnectorSetting);
    }
}
