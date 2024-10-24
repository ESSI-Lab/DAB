package eu.essi_lab.cfga.setting.editable.test;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.cfga.EditableSetting;
import eu.essi_lab.cfga.gs.setting.harvesting.HarvestingSetting;
import eu.essi_lab.cfga.gs.setting.harvesting.HarvestingSettingLoader;

/**
 * @author Fabrizio
 */
public class HarvestingSettingEditableTest {

    @Test
    public void test() {

	HarvestingSetting setting = HarvestingSettingLoader.load();

	boolean valid = EditableSetting.test(setting);

	Assert.assertTrue(valid);

    }
}
