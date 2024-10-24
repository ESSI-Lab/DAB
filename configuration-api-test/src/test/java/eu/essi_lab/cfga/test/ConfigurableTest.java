/**
 * 
 */
package eu.essi_lab.cfga.test;

import eu.essi_lab.cfga.Configurable;
import eu.essi_lab.cfga.setting.Setting;

/**
 * @author Fabrizio
 */
public class ConfigurableTest implements Configurable<Setting> {

    private Setting setting;

    public ConfigurableTest() {

    }

    /**
     * @param setting
     */
    public ConfigurableTest(Setting setting) {

	this.setting = setting;
    }

    @Override
    public Setting getSetting() {

	return setting;
    }

    @Override
    public void configure(Setting setting) {

	this.setting = setting;
    }

    @Override
    public String getType() {

	return "ConfigurableTest";
    }
}
