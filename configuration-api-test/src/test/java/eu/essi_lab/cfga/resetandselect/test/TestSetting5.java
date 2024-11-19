/**
 * 
 */
package eu.essi_lab.cfga.resetandselect.test;

import eu.essi_lab.cfga.Configuration;
import eu.essi_lab.cfga.SelectionUtils;
import eu.essi_lab.cfga.setting.ObjectExtension;
import eu.essi_lab.cfga.setting.Setting;
import eu.essi_lab.cfga.setting.validation.ValidationContext;
import eu.essi_lab.cfga.setting.validation.ValidationResponse;
import eu.essi_lab.cfga.setting.validation.Validator;

/**
 * @author Fabrizio
 */
public class TestSetting5 extends Setting {

    /**
     * @param args
     */
    public static void main(String[] args) {

	Setting testSetting5 = new Setting();

	//
	// ConfigurationObject properties
	//

	testSetting5.setEnabled(false);

	testSetting5.setCanBeDisabled(false);

	testSetting5.setVisible(false);

	testSetting5.setEditable(false);

	testSetting5.setDescription("description");

	//
	// AbstractSetting properties
	//

	testSetting5.enableCompactMode(false);

	testSetting5.enableFoldedMode(true);

	testSetting5.setCanBeRemoved(true);

	testSetting5.setCanBeCleaned(false);

	testSetting5.setShowHeader(false);

	testSetting5.setExtension(new TestObjectExension());

	testSetting5.setValidator(new TestValidator());

	//
	// Setting properties
	//

	testSetting5.setIdentifier("identifier");

	testSetting5.setName("Test setting 5");

	testSetting5.setConfigurableType("condigurableType");

	SelectionUtils.deepClean(testSetting5);

	System.out.println(testSetting5);
    }

    /**
     * @author Fabrizio
     */
    public static class TestObjectExension implements ObjectExtension {

    }

    /**
     * @author Fabrizio
     */
    public static class TestValidator implements Validator {

	@Override
	public ValidationResponse validate(Configuration configuration, Setting setting, ValidationContext context) {

	    return null;
	}
    }

}
