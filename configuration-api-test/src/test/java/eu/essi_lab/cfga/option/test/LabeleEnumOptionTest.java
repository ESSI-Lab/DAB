package eu.essi_lab.cfga.option.test;

import java.util.List;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.cfga.option.Option;
import eu.essi_lab.cfga.option.OptionBuilder;
import eu.essi_lab.lib.utils.LabeledEnum;
import eu.essi_lab.model.BrokeringStrategy;

/**
 * @author Fabrizio
 */
public class LabeleEnumOptionTest {

    /**
     * 
     */
    @Test
    public void labeledEnumOptionTest() {

	Option<BrokeringStrategy> option = OptionBuilder.get(BrokeringStrategy.class).//
		withValue(BrokeringStrategy.MIXED).//
		build();

	test1(option);
	test1(new Option<>(option.getObject()));
	test1(new Option<>(option.getObject().toString()));

	List<BrokeringStrategy> values = LabeledEnum.values(BrokeringStrategy.class);

	option = OptionBuilder.get(BrokeringStrategy.class).//
		withValues(values).//
		build();

	test2(option);
	test2(new Option<>(option.getObject()));
	test2(new Option<>(option.getObject().toString()));
    }

    /**
     * @param option
     */
    private void test1(Option<BrokeringStrategy> option) {

	String optionString = option.toString();
	String objectValue = new JSONObject(optionString).getJSONArray("values").getString(0);

	Assert.assertEquals(objectValue, BrokeringStrategy.MIXED.getLabel());

	BrokeringStrategy value = option.getValue();
	Assert.assertEquals(BrokeringStrategy.MIXED, value);
    }

    /**
     * @param option
     */
    private void test2(Option<BrokeringStrategy> option) {

	List<BrokeringStrategy> values = option.getValues();

	values.sort((l1, l2) -> l1.getLabel().compareTo(l2.getLabel()));

	Assert.assertEquals(BrokeringStrategy.DISTRIBUTED, values.get(0));
	Assert.assertEquals(BrokeringStrategy.HARVESTED, values.get(1));
	Assert.assertEquals(BrokeringStrategy.MIXED, values.get(2));
    }

}
