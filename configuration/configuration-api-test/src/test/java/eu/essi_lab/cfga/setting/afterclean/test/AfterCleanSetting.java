package eu.essi_lab.cfga.setting.afterclean.test;

import eu.essi_lab.cfga.setting.Setting;

/**
 * @author Fabrizio
 */
public class AfterCleanSetting extends Setting {

    /**
     * 
     */
    public AfterCleanSetting() {

	setName("Setting test subclass name");
    }

    /**
     * @param newName
     */
    public void changeName(String newName) {

	setName(newName);
    }
}
