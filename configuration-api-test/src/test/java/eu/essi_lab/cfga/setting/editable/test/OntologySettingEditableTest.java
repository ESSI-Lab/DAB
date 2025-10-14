/**
 * 
 */
package eu.essi_lab.cfga.setting.editable.test;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.cfga.EditableSetting;
import eu.essi_lab.cfga.gs.setting.OntologySetting;

/**
 * @author Fabrizio
 */
public class OntologySettingEditableTest {

    @Test
    public void test() {

	OntologySetting setting = new OntologySetting();

	boolean valid = EditableSetting.test(setting);

	Assert.assertTrue(valid);
    }
}
