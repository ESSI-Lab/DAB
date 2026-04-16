package eu.essi_lab.cfga.setting.editable.test;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.cfga.EditableSetting;
import eu.essi_lab.cfga.gs.setting.GDCSourcesSetting;

/**
 * 
 * @author Fabrizio
 *
 */
public class GDCSourcesSettingEditableTest {

    @Test
    public void test() {

	GDCSourcesSetting setting = new GDCSourcesSetting();

	boolean valid = EditableSetting.test(setting);

	Assert.assertTrue(valid);
    }
}
