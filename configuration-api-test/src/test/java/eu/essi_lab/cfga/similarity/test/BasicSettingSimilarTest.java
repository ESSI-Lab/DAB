package eu.essi_lab.cfga.similarity.test;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.cfga.option.Option;
import eu.essi_lab.cfga.setting.Setting;

/**
 * 
 */
public class BasicSettingSimilarTest {

    @Test
    public void basicTest1() {

	Setting setting1 = new Setting();
	Setting setting2 = new Setting();

	Assert.assertFalse(setting1.getIdentifier().equals(setting2.getIdentifier()));
	Assert.assertFalse(setting1.getName().equals(setting2.getName()));

	boolean similar = setting1.similar(setting2);

	Assert.assertTrue(similar);
    }

    @Test
    public void basicTest1_1() {

	Setting setting1 = new Setting();
	Setting setting2 = new Setting(setting1.toString());

	boolean similar = setting1.similar(setting2);

	Assert.assertTrue(similar);
    }

    @Test
    public void basicTest1_2() {

	Setting setting1 = new Setting();
	Setting setting2 = new Setting(setting1.toString());
	setting2.getObject().put("xxx", "yyy");

	boolean similar = setting1.similar(setting2);

	Assert.assertFalse(similar);
    }

    @Test
    public void basicTest1_3() {

	Setting setting1 = new Setting();
	Setting setting2 = new Setting(setting1.toString());

	setting2.getObject().remove(Setting.IDENTIFIER.getKey());

	boolean similar = setting1.similar(setting2);

	Assert.assertFalse(similar);
    }

    /**
     * They have the same {@link Setting#IDENTIFIER} property key, but with different value type
     */
    @Test
    public void basicTest1_4() {

	Setting setting1 = new Setting();
	Setting setting2 = new Setting(setting1.toString());

	setting2.getObject().remove(Setting.IDENTIFIER.getKey());
	setting2.getObject().put(Setting.IDENTIFIER.getKey(), Integer.valueOf(5));

	boolean similar = setting1.similar(setting2);

	Assert.assertFalse(similar);
    }

    @Test
    public void basicTest2() {

	Setting setting1 = new Setting();
	setting1.setIdentifier("id");
	setting1.setName("name");

	setting1.setDescription("desc1");

	Setting setting2 = new Setting();
	setting2.setIdentifier("id");
	setting2.setName("name");

	setting2.setDescription("desc2");

	boolean similar = setting1.similar(setting2);

	Assert.assertTrue(similar);
    }

    @Test
    public void basicTest2_1() {

	Setting setting1 = new Setting();

	Setting setting2 = new Setting();
	setting2.setDescription("desc2");

	boolean similar = setting1.similar(setting2);

	Assert.assertFalse(similar);
    }

    @Test
    public void basicTest2_2() {

	Setting setting1 = new Setting();

	Setting setting2 = new Setting();
	setting2.setDescription("desc2");

	boolean similar = setting1.similar(//
		setting2, //
		Arrays.asList(Setting.DESCRIPTION.getKey()));

	Assert.assertTrue(similar);
    }

    @Test
    public void basicTest3() {

	Setting setting1 = new Setting();
	setting1.getObject().remove("settingId");

	Setting setting2 = new Setting();

	boolean similar = setting1.similar(setting2);

	Assert.assertFalse(similar);
    }

    @Test
    public void basicTest4() {

	Setting setting1 = new Setting();

	Setting setting2 = new Setting();
	setting2.getObject().remove("settingId");

	boolean similar = setting1.similar(setting2);

	Assert.assertFalse(similar);
    }

    @Test
    public void basicTest5() {

	Setting setting1 = new Setting();
	setting1.setIdentifier("id");
	setting1.setName("name");

	setting1.addSetting(new Setting());

	Setting setting2 = new Setting();
	setting2.setIdentifier("id");
	setting2.setName("name");

	setting2.addSetting(new Setting());

	boolean similar = setting1.similar(setting2);

	Assert.assertFalse(similar);
    }

    @Test
    public void basicTest5_1() {

	Setting setting = new Setting();

	Setting setting1 = new Setting();
	setting1.setIdentifier("id");
	setting1.setName("name");

	setting1.addSetting(setting);

	Setting setting2 = new Setting();
	setting2.setIdentifier("id");
	setting2.setName("name");

	setting2.addSetting(setting);

	boolean similar = setting1.similar(setting2);

	Assert.assertTrue(similar);
    }

    @Test
    public void basicTest5_2() {

	Setting sub1 = new Setting();
	Setting sub2 = new Setting(sub1.toString());
	sub2.setName("sub2");

	//
	//
	//

	Setting setting1 = new Setting();
	setting1.addSetting(sub1);

	//
	//
	//

	Setting setting2 = new Setting();
	setting2.addSetting(sub2);

	//
	//
	//

	boolean similar = setting1.similar(setting2);

	Assert.assertTrue(similar);
    }

    @Test
    public void basicTest5_3() {

	Setting sub1 = new Setting();
	sub1.setIdentifier("sub1");
	sub1.setName("sub1");

	Setting sub_1_1 = new Setting();
	sub_1_1.setIdentifier("sub_1_1");
	sub_1_1.setName("sub_1_1");

	sub1.addSetting(sub_1_1);

	Setting sub_1_1_1 = new Setting();
	sub_1_1_1.setIdentifier("sub_1_1_1");
	sub_1_1_1.setName("sub_1_1_1");

	sub_1_1.addSetting(sub_1_1_1);

	//

	Setting setting1 = new Setting();
	setting1.setIdentifier("id");
	setting1.setName("name");

	setting1.addSetting(sub1);

	//

	Setting setting2 = new Setting();
	setting2.setIdentifier("id");
	setting2.setName("name");

	Setting sub2 = new Setting(sub1.toString());

	Setting sub_1_1_1_2 = sub2.//
		getSetting("sub_1_1").get().//
		getSetting("sub_1_1_1").get();

	sub_1_1_1_2.setIdentifier("sub_1_1_1_2");
	sub_1_1_1_2.setName("sub_1_1_1_2");

	setting2.addSetting(sub2);

	boolean similar = setting1.similar(setting2);

	Assert.assertFalse(similar);
    }

    @Test
    public void basicTest6() {

	Setting setting1 = new Setting();
	setting1.setIdentifier("id");
	setting1.setName("name");

	Option<String> option1 = new Option<String>(String.class);
	option1.setKey("key1");

	setting1.addOption(option1);

	Setting setting2 = new Setting();
	setting2.setIdentifier("id");
	setting2.setName("name");

	Option<String> option2 = new Option<String>(String.class);
	option2.setKey("key1");

	setting2.addOption(option2);

	boolean similar = setting1.similar(setting2);

	Assert.assertTrue(similar);
    }

    @Test
    public void basicTest6_1() {

	Setting setting1 = new Setting();
	setting1.setIdentifier("id");
	setting1.setName("name");

	Option<String> option1 = new Option<String>(String.class);
	option1.setKey("key1");

	setting1.addOption(option1);

	Setting setting2 = new Setting(setting1.toString());

	Option<?> option2 = setting2.getOption("key1").get();
	option2.setKey("key2");

	boolean similar = setting1.similar(setting2);

	Assert.assertFalse(similar);
    }

    @Test
    public void basicTest6_2() {

	Setting setting1 = new Setting();
	setting1.setIdentifier("id");
	setting1.setName("name");

	Option<String> option1 = new Option<String>(String.class);
	option1.setKey("key");

	setting1.addOption(option1);

	Setting setting2 = new Setting();
	setting2.setIdentifier("id");
	setting2.setName("name");

	Option<Integer> option2 = new Option<Integer>(Integer.class);
	option2.setKey("key");

	setting2.addOption(option2);

	boolean similar = setting1.similar(setting2);

	//
	// they have option with same key but different value class
	//
	Assert.assertFalse(similar);
    }

    @Test
    public void basicTest7() {

	Setting setting1 = new Setting();

	Option<String> option1 = new Option<String>(String.class);
	option1.setKey("key1");

	setting1.addOption(option1);

	Setting setting2 = new Setting();

	Option<String> option2 = new Option<String>(String.class);
	option2.setKey("key2");

	setting2.addOption(option2);

	boolean similar = setting1.similar(setting2);

	Assert.assertFalse(similar);
    }

    @Test
    public void basicTest8() {

	Setting setting1 = new Setting();

	Option<String> option1 = new Option<String>(String.class);
	option1.setKey("key1");
	option1.setValue("xxx");

	setting1.addOption(option1);

	Setting setting2 = new Setting(setting1.toString());

	Option<String> option2 = setting2.getOption("key1", String.class).get();
	option2.setValue("yyy");

	boolean similar = setting1.similar(setting2);

	Assert.assertTrue(similar);
    }

    /**
     * The {@link Setting#SELECTED} property has a default value(false), so its key is omitted in case the value is set
     * to false. So the first setting has the property key, because set to true, while the second don't and the key test
     * do not match
     */
    @Test
    public void basicTest9() {

	Setting setting1 = new Setting();
	setting1.setIdentifier("id1");
	setting1.setName("name2");
	setting1.setSelected(true);

	Setting setting2 = new Setting();
	setting2.setIdentifier("id2");
	setting2.setName("name2");
	setting2.setSelected(false);

	boolean similar = setting1.similar(setting2);

	Assert.assertFalse(similar);
    }

    /**
     * Like test 9 but excluding the {@link Setting#SELECTED} property key
     */
    @Test
    public void basicTest9_1() {

	Setting setting1 = new Setting();
	setting1.setIdentifier("id1");
	setting1.setName("name2");
	setting1.setSelected(true);

	Setting setting2 = new Setting();
	setting2.setIdentifier("id2");
	setting2.setName("name2");
	setting2.setSelected(false);

	boolean similar = setting1.similar(//
		setting2, //
		Arrays.asList(Setting.SELECTED.getKey()));

	Assert.assertTrue(similar);
    }

    /**
     * They have different {@link Setting#SETTING_CLASS}
     */
    @Test
    public void basicTest10() {

	Setting setting1 = new Setting() {
	};
	Setting setting2 = new Setting() {
	};

	boolean similar = setting1.similar(setting2);

	Assert.assertFalse(similar);
    }
}
