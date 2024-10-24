package eu.essi_lab.cfga.setting.editable.test;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.cfga.EditableSetting;
import eu.essi_lab.cfga.gs.setting.distribution.DistributionSetting;

/**
 * @author Fabrizio
 */
public class DistributionSettingEditableTest {

    @Test
    public void test() {

	DistributionSetting setting = new DistributionSetting();

	boolean valid = EditableSetting.test(setting);

	Assert.assertTrue(valid);
    }
}
