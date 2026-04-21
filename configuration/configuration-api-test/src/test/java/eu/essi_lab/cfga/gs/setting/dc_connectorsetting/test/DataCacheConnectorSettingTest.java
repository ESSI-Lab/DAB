package eu.essi_lab.cfga.gs.setting.dc_connectorsetting.test;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.access.datacache.DataCacheConnector;
import eu.essi_lab.access.datacache.DataCacheConnectorFactory.DataConnectorType;
import eu.essi_lab.cfga.gs.setting.dc_connector.DataCacheConnectorSetting;
import eu.essi_lab.cfga.gs.setting.dc_connector.DataCacheConnectorSettingLoader;

/**
 * @author Fabrizio
 */
public class DataCacheConnectorSettingTest {

    @Test
    public void initTest() {

	DataCacheConnectorSetting setting = DataCacheConnectorSettingLoader.load();

	Assert.assertNull(setting.getDatabaseName());
	Assert.assertNull(setting.getDatabaseUser());
	Assert.assertNull(setting.getDatabasePassword());
	Assert.assertNull(setting.getDatabaseUri());

	Assert.assertEquals(DataConnectorType.OPEN_SEARCH_DOCKERHUB_1_3.getLabel(), setting.getDataConnectorType());

	Assert.assertFalse(setting.getOptionValue("xxx").isPresent());

	Assert.assertEquals(DataCacheConnector.DEFAULT_CACHED_DAYS, Integer.valueOf(setting.getOptionValue(DataCacheConnector.CACHED_DAYS).get()));
	Assert.assertEquals(DataCacheConnector.DEFAULT_FLUSH_INTERVAL_MS,
		Long.valueOf(setting.getOptionValue(DataCacheConnector.FLUSH_INTERVAL_MS).get()));
	Assert.assertEquals(DataCacheConnector.DEFAULT_MAX_BULK_SIZE,
		Integer.valueOf(setting.getOptionValue(DataCacheConnector.MAX_BULK_SIZE).get()));
    }

    @Test
    public void test() {

	DataCacheConnectorSetting setting = DataCacheConnectorSettingLoader.load();

	setting.setDataConnectorType(DataConnectorType.OPEN_SEARCH_AWS_1_3.getLabel());
	Assert.assertEquals(DataConnectorType.OPEN_SEARCH_AWS_1_3.getLabel(), setting.getDataConnectorType());

	setting.setDatabaseName("name");
	Assert.assertEquals("name", setting.getDatabaseName());

	setting.setDatabaseUser("user");
	Assert.assertEquals("user", setting.getDatabaseUser());

	setting.setDatabasePassword("pwd");
	Assert.assertEquals("pwd", setting.getDatabasePassword());

	setting.setDatabaseUri("uri");
	Assert.assertEquals("uri", setting.getDatabaseUri());

	//
	//
	//

	setting.setOptionValue("xxx", "xxx");
	Assert.assertFalse(setting.getOptionValue("xxx").isPresent());

	setting.setOptionValue(DataCacheConnector.CACHED_DAYS, "100");

	Assert.assertEquals("100", setting.getOptionValue(DataCacheConnector.CACHED_DAYS).get());

	setting.setOptionValue(DataCacheConnector.FLUSH_INTERVAL_MS, "500");

	Assert.assertEquals("500", setting.getOptionValue(DataCacheConnector.FLUSH_INTERVAL_MS).get());

	setting.setOptionValue(DataCacheConnector.MAX_BULK_SIZE, "800");

	Assert.assertEquals("800", setting.getOptionValue(DataCacheConnector.MAX_BULK_SIZE).get());
    }

}
