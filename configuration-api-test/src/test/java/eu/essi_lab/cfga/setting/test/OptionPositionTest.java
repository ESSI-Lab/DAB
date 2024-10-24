package eu.essi_lab.cfga.setting.test;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.cfga.option.Option;
import eu.essi_lab.cfga.option.StringOptionBuilder;
import eu.essi_lab.cfga.setting.Setting;

public class OptionPositionTest {

    @Test
    public void test() {
	
	Option<String> op = new Option<>(String.class);
	Assert.assertEquals(new Integer(0), op.getPosition());

	Setting setting = new Setting();

	int length = 10;

	for (int i = 0; i < length; i++) {

	    Option<String> option = StringOptionBuilder.get().withKey("opt" + i).withLabel(String.valueOf(i)).build();
	    setting.addOption(option);
	}

	List<Option<?>> options = setting.getOptions();
	boolean passed = true;
	for (int i = 0; i < length; i++) {

	    passed &= options.get(i).getLabel().equals(String.valueOf(i));
	}

	Assert.assertTrue(passed);
    }
}
