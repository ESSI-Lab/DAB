package eu.essi_lab.cfga.setting.afterclean.test;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.cfga.setting.AfterCleanFunction;
import eu.essi_lab.cfga.setting.Setting;
import eu.essi_lab.cfga.setting.SettingUtils;

/**
 * @author Fabrizio
 */
public class AfterCleanSettingTest {

    @Test
    public void test1() {

	Setting setting = new Setting();

	setting.setName("The setting name");

	setting.setAfterCleanFunction(new AfterCleanTestFunction());

	checkName("The setting name", setting);
	checkName("The setting name", new Setting(setting.getObject()));
	checkName("The setting name", new Setting(setting.toString()));

	//
	//
	//

	setting.afterClean();

	checkName("After clean name", setting);
	checkName("After clean name", new Setting(setting.getObject()));
	checkName("After clean name", new Setting(setting.toString()));
    }

    /**
     * @author Fabrizio
     */
    public static class AfterCleanTestFunction implements AfterCleanFunction {

	@Override
	public void afterClean(Setting setting) {

	    setting.setName("After clean name");
	}
    }

    /**
     * S@param expectedName
     * 
     * @param setting
     */
    private void checkName(String expectedName, Setting setting) {

	Assert.assertEquals(expectedName, setting.getName());
    }

    /**
     * @author Fabrizio
     */
    public static class AfterCleanTestFunction2 implements AfterCleanFunction {

	@Override
	public void afterClean(Setting setting) {

	    AfterCleanSetting afterCleanSetting = SettingUtils.downCast(setting, AfterCleanSetting.class);

	    afterCleanSetting.changeName("After clean 2 name");
	}
    }

    @Test
    public void test2() {

	AfterCleanSetting setting = new AfterCleanSetting();

	setting.setAfterCleanFunction(new AfterCleanTestFunction2());

	checkName("Setting test subclass name", setting);
	checkName("Setting test subclass name", new Setting(setting.getObject()));
	checkName("Setting test subclass name", new Setting(setting.toString()));

	//
	//
	//

	setting.afterClean();

	checkName("After clean 2 name", setting);
	checkName("After clean 2 name", new Setting(setting.getObject()));
	checkName("After clean 2 name", new Setting(setting.toString()));
    }
}
