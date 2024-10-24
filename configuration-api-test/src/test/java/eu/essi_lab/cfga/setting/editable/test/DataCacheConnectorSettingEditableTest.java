package eu.essi_lab.cfga.setting.editable.test;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.cfga.EditableSetting;
import eu.essi_lab.cfga.gs.setting.dc_connector.DataCacheConnectorSetting;
import eu.essi_lab.cfga.gs.setting.dc_connector.DataCacheConnectorSettingLoader;

/**
 * @author Fabrizio
 */
public class DataCacheConnectorSettingEditableTest {

    @Test
    public void test() {

	DataCacheConnectorSetting setting = DataCacheConnectorSettingLoader.load();

	boolean valid = EditableSetting.test(setting);

	Assert.assertTrue(valid);
    }
}
