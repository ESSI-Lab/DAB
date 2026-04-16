package eu.essi_lab.cfga.option.test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.cfga.option.Option;
import eu.essi_lab.cfga.option.OptionBuilder;

/**
 * @author Fabrizio
 */
public class EnumOptionTest {

    /**
     * 
     */
    @Test
    public void enumOptionTest() {

	Option<TimeUnit> option = OptionBuilder.get(TimeUnit.class).//
		withValue(TimeUnit.DAYS).//
		build();

	test1(option);
	test1(new Option<>(option.getObject()));
	test1(new Option<>(option.getObject().toString()));

	option = OptionBuilder.get(TimeUnit.class).//
		withValues(Arrays.asList(TimeUnit.DAYS, TimeUnit.HOURS, TimeUnit.MINUTES)).//
		build();

	test2(option);
	test2(new Option<>(option.getObject()));
	test2(new Option<>(option.getObject().toString()));
    }

    /**
     * @param option
     */
    private void test1(Option<TimeUnit> option) {

	String optionString = option.toString();
	String objectValue = new JSONObject(optionString).getJSONArray("values").getString(0);

	Assert.assertEquals(objectValue, TimeUnit.DAYS.name());

	TimeUnit value = option.getValue();
	Assert.assertEquals(TimeUnit.DAYS, value);
    }

    /**
     * @param option
     */
    private void test2(Option<TimeUnit> option) {

	List<TimeUnit> values = option.getValues();

	values.sort((l1, l2) -> l1.name().compareTo(l2.name()));

	Assert.assertEquals(TimeUnit.DAYS, values.get(0));
	Assert.assertEquals(TimeUnit.HOURS, values.get(1));
	Assert.assertEquals(TimeUnit.MINUTES, values.get(2));
    }

}
