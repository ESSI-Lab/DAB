package eu.essi_lab.accessor.oaipmh.test;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.accessor.oaipmh.OAIPMHAccessor;
import eu.essi_lab.accessor.oaipmh.OAIPMHConnector;
import eu.essi_lab.accessor.oaipmh.OAIPMHConnectorSetting;
import eu.essi_lab.cfga.gs.setting.accessor.AccessorSetting;
import eu.essi_lab.cfga.gs.setting.connector.DistributedConnectorSetting;
import eu.essi_lab.cfga.gs.setting.connector.HarvestedConnectorSetting;
import eu.essi_lab.model.BrokeringStrategy;
import eu.essi_lab.model.GSSource;

/**
 * @author Fabrizio
 */
public class OAIPMHAccessorSettingTest {

    @Test
    public void connectorSettingTest() throws Exception {

	OAIPMHAccessor accessor = new OAIPMHAccessor();

	HarvestedConnectorSetting generalTypeSetting = accessor.getSetting().getHarvestedConnectorSetting();

	OAIPMHConnectorSetting specificTypeSetting = accessor.getConnector().getSetting();

	Assert.assertEquals(generalTypeSetting, specificTypeSetting);
    }

    @Test
    public void test() throws Exception {

	OAIPMHAccessor accessor = new OAIPMHAccessor();
	AccessorSetting setting = accessor.getSetting();

	//
	//
	//

	BrokeringStrategy brokeringStrategy = setting.getBrokeringStrategy();
	Assert.assertEquals(BrokeringStrategy.HARVESTED, brokeringStrategy);

	//
	//
	//

	String configurableType = setting.getConfigurableType();
	Assert.assertEquals(OAIPMHAccessor.TYPE, configurableType);

	//
	//
	//

	HarvestedConnectorSetting harvConnectorSetting = setting.getHarvestedConnectorSetting();
	Assert.assertNotNull(harvConnectorSetting);

	OAIPMHConnector connector = harvConnectorSetting.createConfigurable();
	Assert.assertEquals(OAIPMHConnector.class, connector.getClass());

	//
	//
	//

	GSSource source = setting.getSource();
	GSSource asSource = setting.getGSSourceSetting().asSource();

	Assert.assertEquals(source, asSource);

	Assert.assertEquals(BrokeringStrategy.HARVESTED, source.getBrokeringStrategy());
	Assert.assertEquals(BrokeringStrategy.HARVESTED, asSource.getBrokeringStrategy());

	Assert.assertNull(source.getUniqueIdentifier());
	Assert.assertNull(asSource.getUniqueIdentifier());

	Assert.assertNull(source.getEndpoint());
	Assert.assertNull(asSource.getEndpoint());

	Assert.assertNull(source.getLabel());
	Assert.assertNull(asSource.getLabel());

	//
	//
	//

	String distAccessorType = setting.getDistributedAccessorType();
	Assert.assertNull(distAccessorType);

	DistributedConnectorSetting distConnectorSetting = setting.getDistributedConnectorSetting();
	Assert.assertNull(distConnectorSetting);
    }

}
