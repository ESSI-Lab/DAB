package eu.essi_lab.cfga.setting.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.cfga.setting.Setting;
import eu.essi_lab.cfga.setting.SettingUtils;

/**
 * @author Fabrizio
 */
public class SettingDeepFindTest {

    /**
     * 
     */
    @Test
    public void test() {

	Setting setting = new Setting();

	setting.addSetting(new Setting());
	setting.addSetting(new Setting());
	setting.addSetting(new Setting());

	Setting setting2 = new Setting();
	setting2.setIdentifier("toFind1");

	setting2.addSetting(new Setting());
	setting2.addSetting(new Setting());

	Setting setting2_1 = new Setting();
	setting2_1.setIdentifier("toFind2");

	Setting setting2_1_1 = new Setting();
	setting2_1_1.setIdentifier("toFind3");

	setting2_1.addSetting(setting2_1_1);

	setting2.addSetting(setting2_1);

	setting.addSetting(setting2);

	//
	//
	//

	List<String> list = Arrays.asList("toFind1", "toFind2", "toFind3");

	ArrayList<Setting> matches = new ArrayList<>();

	SettingUtils.deepFind(setting, s -> list.contains(s.getIdentifier()), matches);

	//
	//
	//

	Assert.assertEquals(3, matches.size());

	matches.sort((s1, s2) -> s1.getIdentifier().compareTo(s2.getIdentifier()));

	Assert.assertEquals("toFind1", matches.get(0).getIdentifier());
	Assert.assertEquals("toFind2", matches.get(1).getIdentifier());
	Assert.assertEquals("toFind3", matches.get(2).getIdentifier());

    }
}
