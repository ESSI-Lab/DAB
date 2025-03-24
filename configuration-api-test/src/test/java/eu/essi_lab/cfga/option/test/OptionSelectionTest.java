package eu.essi_lab.cfga.option.test;

import static org.junit.Assert.assertThrows;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.augmenter.reindexer.ResourceReindexerSetting;
import eu.essi_lab.cfga.Selectable.SelectionMode;
import eu.essi_lab.cfga.SelectionUtils;
import eu.essi_lab.cfga.option.BooleanChoice;
import eu.essi_lab.cfga.option.Option;
import eu.essi_lab.cfga.option.OptionBuilder;
import eu.essi_lab.cfga.option.StringOptionBuilder;
import eu.essi_lab.cfga.option.UnsetSelectionModeException;
import eu.essi_lab.lib.utils.LabeledEnum;
import eu.essi_lab.model.BrokeringStrategy;
import eu.essi_lab.model.resource.MetadataElement;

/**
 * @author Fabrizio
 */
public class OptionSelectionTest {

    @Test
    public void selectionTest2() {

	Option<String> option = new Option<>(String.class);

	int valuesCount = 10;

	for (int i = 0; i < valuesCount; i++) {

	    option.addValue(String.valueOf(i));
	}

	long count = option.getSelectedValues().size();
	Assert.assertEquals(0, count);

	//
	// selects all
	//

	option.setSelectionMode(SelectionMode.SINGLE);
	option.select(s -> true);

	count = option.getSelectedValues().size();
	Assert.assertEquals(valuesCount, count);

	//
	// selects 1, the other are unselected
	//

	option.select(v -> v.equals("0"));

	count = option.getSelectedValues().size();
	Assert.assertEquals(1, count);

	count = option.getValues().stream().filter(v -> !option.getSelectedValues().contains(v)).count();
	Assert.assertEquals(valuesCount - 1, count);

	//
	// selects 1, the last wins, the other are unselected
	//

	option.select(s -> s.equals("0"));
	option.select(s -> s.equals("1"));
	option.select(s -> s.equals("2"));
	option.select(s -> s.equals("3"));
	option.select(s -> s.equals("4"));

	count = option.getSelectedValues().size();
	Assert.assertEquals(1, count);

	String selValue = option.getSelectedValues().get(0);
	Assert.assertEquals("4", selValue);

	count = option.getValues().stream().filter(v -> !option.getSelectedValues().contains(v)).count();
	Assert.assertEquals(valuesCount - 1, count);

	//
	// selects 5, the other are unselected
	//

	List<String> asList = Arrays.asList("0", "1", "2", "3", "4");

	option.select(s -> asList.contains(s));

	count = option.getSelectedValues().size();
	Assert.assertEquals(valuesCount - 5, count);

	count = option.getValues().stream().filter(v -> !option.getSelectedValues().contains(v)).count();
	Assert.assertEquals(valuesCount - 5, count);

	boolean anyMatch = option.getSelectedValues().stream().anyMatch(s -> asList.contains(s));
	Assert.assertTrue(anyMatch);

	List<String> collect = option.getSelectedValues().stream().sorted((s1, s2) -> s1.compareTo(s2)).collect(Collectors.toList());

	Assert.assertEquals("0", collect.get(0));
	Assert.assertEquals("1", collect.get(1));
	Assert.assertEquals("2", collect.get(2));
	Assert.assertEquals("3", collect.get(3));
	Assert.assertEquals("4", collect.get(4));

	//
	// deselects all
	//

	option.select(s -> false);

	count = option.getSelectedValues().size();
	Assert.assertEquals(0, count);

	count = option.getValues().stream().filter(v -> !option.getSelectedValues().contains(v)).count();
	Assert.assertEquals(valuesCount, count);
    }

    @Test
    public void selectionTest() {

	Option<String> emptyOption = StringOptionBuilder.get().build();

	emptyOptionSelectionTest(emptyOption);
	emptyOptionSelectionTest(new Option<String>(emptyOption.getObject()));
	emptyOptionSelectionTest(new Option<String>(emptyOption.getObject().toString()));

	//
	//
	//

	Option<BrokeringStrategy> option = OptionBuilder.//
		get(BrokeringStrategy.class).//
		withValues(LabeledEnum.values(BrokeringStrategy.class)).//
		build();

	List<BrokeringStrategy> values = option.getValues();

	Assert.assertEquals(LabeledEnum.values(BrokeringStrategy.class), values);

	allValuesNoneSelectedTest(option);
	allValuesNoneSelectedTest(new Option<BrokeringStrategy>(option.getObject()));
	allValuesNoneSelectedTest(new Option<BrokeringStrategy>(option.getObject().toString()));

	//
	// selects HARVESTING without removing
	//
	option.setSelectionMode(SelectionMode.SINGLE);
	option.select(s -> s == BrokeringStrategy.HARVESTED);

	allValuesHarvestedSelectedTest(option);
	allValuesHarvestedSelectedTest(new Option<BrokeringStrategy>(option.getObject()));
	allValuesHarvestedSelectedTest(new Option<BrokeringStrategy>(option.getObject().toString()));

	//
	// tries to remove DISTRIBUTED and MIXED but the multi type is UNSET
	// so nothing happens
	//
	allValuesHarvestedSelectedTest(option);
	allValuesHarvestedSelectedTest(new Option<BrokeringStrategy>(option.getObject()));
	allValuesHarvestedSelectedTest(new Option<BrokeringStrategy>(option.getObject().toString()));

	//
	// set a multi mode and removes DISTRIBUTED and MIXED
	// From now there is only one value in the option, HARVESTED
	//
	option.setSelectionMode(SelectionMode.SINGLE);

	option.clean();

	afterCleanTest(option);
	option.setSelectionMode(SelectionMode.SINGLE);

	unselectedRemovedHarvestedSelectedTest(option);
	unselectedRemovedHarvestedSelectedTest(new Option<BrokeringStrategy>(option.getObject()));
	unselectedRemovedHarvestedSelectedTest(new Option<BrokeringStrategy>(option.getObject().toString()));

	// nothing should change since there are no unselected values
	option.clean();

	afterCleanTest(option);
	option.setSelectionMode(SelectionMode.SINGLE);

	unselectedRemovedHarvestedSelectedTest(option);
	unselectedRemovedHarvestedSelectedTest(new Option<BrokeringStrategy>(option.getObject()));
	unselectedRemovedHarvestedSelectedTest(new Option<BrokeringStrategy>(option.getObject().toString()));

	//
	// Trying to select a missing value, no selection must be done and the indexes array must be empty
	//
	option.setSelectionMode(SelectionMode.SINGLE);
	option.select(s -> s == BrokeringStrategy.DISTRIBUTED);

	missingValueSelectionTest(option);
	missingValueSelectionTest(new Option<BrokeringStrategy>(option.getObject()));
	missingValueSelectionTest(new Option<BrokeringStrategy>(option.getObject().toString()));

	//
	// set the values again. This method reset the selection state of
	// the option, so there is no selected value now
	//
	option.setValues(LabeledEnum.values(BrokeringStrategy.class));

	allValuesNoneSelectedTest(option);
	allValuesNoneSelectedTest(new Option<BrokeringStrategy>(option.getObject()));
	allValuesNoneSelectedTest(new Option<BrokeringStrategy>(option.getObject().toString()));

	//
	// selects HARVESTED and MIXED without remove DISTRIBUTED
	//
	option.select(s -> s == BrokeringStrategy.HARVESTED || s == BrokeringStrategy.MIXED);

	allValuesHarvestedAndMixedSelectedTest(option);
	allValuesHarvestedAndMixedSelectedTest(new Option<BrokeringStrategy>(option.getObject()));
	allValuesHarvestedAndMixedSelectedTest(new Option<BrokeringStrategy>(option.getObject().toString()));

	// removes DISTRIBUTED
	option.clean();

	afterCleanTest(option);
	option.setSelectionMode(SelectionMode.SINGLE);

	unselectedRemovedHarvestedAndMixedSelectedTest(option);
	unselectedRemovedHarvestedAndMixedSelectedTest(new Option<BrokeringStrategy>(option.getObject()));
	unselectedRemovedHarvestedAndMixedSelectedTest(new Option<BrokeringStrategy>(option.getObject().toString()));

	//
	// set the values again, selects all of them and remove the unselected (that is none)
	//
	option.setValues(LabeledEnum.values(BrokeringStrategy.class));
	option.select(s -> true);
	option.clean();

	afterCleanTest(option);
	option.setSelectionMode(SelectionMode.SINGLE);

	unselectedRemovedAllValuesSelectedTest(option);
	unselectedRemovedAllValuesSelectedTest(new Option<BrokeringStrategy>(option.getObject()));
	unselectedRemovedAllValuesSelectedTest(new Option<BrokeringStrategy>(option.getObject().toString()));

	//
	// if no value is selected, all are removed when the option is clean
	//

	option.select(p -> false);

	option.clean();

	List<BrokeringStrategy> optionValues = option.getValues();

	Assert.assertEquals(0, optionValues.size());

	List<BrokeringStrategy> selValues = option.getSelectedValues();

	Assert.assertEquals(0, selValues.size());
    }

    @Test
    public void cleanWithNoSelectedValuesTest() {

	ResourceReindexerSetting setting = new ResourceReindexerSetting();

	//
	//
	//

	List<MetadataElement> values = setting.getOptions(MetadataElement.class).get(0).getValues();

	List<MetadataElement> elements = Arrays.asList(MetadataElement.values());

	Assert.assertEquals(values, elements);

	//
	//
	//

	List<MetadataElement> selectedElements = setting.getSelectedElements();

	Assert.assertEquals(0, selectedElements.size());

	//
	//
	//

	SelectionUtils.deepClean(setting);

	values = setting.getOptions(MetadataElement.class).get(0).getValues();

	Assert.assertEquals(0, values.size());
    }

    private void missingValueSelectionTest(Option<BrokeringStrategy> option) {

	//
	// no value selected
	//
	List<BrokeringStrategy> selectedValues = option.getSelectedValues();

	Assert.assertEquals(0, selectedValues.size());

	Assert.assertNull(option.getSelectedValue());

	Assert.assertFalse(option.getOptionalSelectedValue().isPresent());

	//
	// Brokering strategy is still the only value
	//
	List<BrokeringStrategy> values = option.getValues();

	Assert.assertEquals(1, values.size());

	Assert.assertEquals(BrokeringStrategy.HARVESTED, values.get(0));
    }

    /**
     * @param option
     */
    private void afterCleanTest(Option<?> option) {

	Assert.assertEquals(SelectionMode.UNSET, option.getSelectionMode());
    }

    private void unselectedRemovedAllValuesSelectedTest(Option<BrokeringStrategy> option) {

	//
	// all values are selected
	//
	List<BrokeringStrategy> selectedValues = option.getSelectedValues();

	Assert.assertEquals(LabeledEnum.values(BrokeringStrategy.class), selectedValues);

	//
	// all the values are there
	//
	List<BrokeringStrategy> values = option.getValues();

	Assert.assertEquals(LabeledEnum.values(BrokeringStrategy.class), values);
    }

    private void unselectedRemovedHarvestedAndMixedSelectedTest(Option<BrokeringStrategy> option) {

	//
	// the values are still selected ...
	//
	List<BrokeringStrategy> selectedValues = option.getSelectedValues();
	selectedValues.sort((v1, v2) -> v1.name().compareTo(v2.name()));

	Assert.assertEquals(2, selectedValues.size());

	Assert.assertEquals(BrokeringStrategy.HARVESTED, selectedValues.get(0));
	Assert.assertEquals(BrokeringStrategy.MIXED, selectedValues.get(1));

	//
	// two values are still there
	//
	List<BrokeringStrategy> values = option.getValues();
	values.sort((v1, v2) -> v1.name().compareTo(v2.name()));

	Assert.assertEquals(2, values.size());

	Assert.assertEquals(BrokeringStrategy.HARVESTED, values.get(0));
	Assert.assertEquals(BrokeringStrategy.MIXED, values.get(1));
    }

    private void allValuesHarvestedAndMixedSelectedTest(Option<BrokeringStrategy> option) {

	//
	// the values are selected ...
	//
	List<BrokeringStrategy> selectedValues = option.getSelectedValues();
	selectedValues.sort((v1, v2) -> v1.name().compareTo(v2.name()));

	Assert.assertEquals(2, selectedValues.size());

	Assert.assertEquals(BrokeringStrategy.HARVESTED, selectedValues.get(0));
	Assert.assertEquals(BrokeringStrategy.MIXED, selectedValues.get(1));

	//
	// ... but the others are not removed, all the values are still there
	//
	List<BrokeringStrategy> values = option.getValues();

	Assert.assertEquals(LabeledEnum.values(BrokeringStrategy.class), values);
    }

    private void allValuesNoneSelectedTest(Option<BrokeringStrategy> option) {

	//
	// no value selected
	//
	List<BrokeringStrategy> selectedValues = option.getSelectedValues();

	Assert.assertEquals(0, selectedValues.size());

	Assert.assertNull(option.getSelectedValue());

	Assert.assertFalse(option.getOptionalSelectedValue().isPresent());

	//
	// all the values are there
	//
	List<BrokeringStrategy> values = option.getValues();

	Assert.assertEquals(LabeledEnum.values(BrokeringStrategy.class), values);
    }

    /**
     * @param option
     */
    private void unselectedRemovedHarvestedSelectedTest(Option<BrokeringStrategy> option) {

	//
	// The selected value is still the same...
	//

	List<BrokeringStrategy> selectedValues = option.getSelectedValues();

	Assert.assertEquals(1, selectedValues.size());

	Assert.assertEquals(BrokeringStrategy.HARVESTED, selectedValues.get(0));

	Assert.assertEquals(option.getSelectedValue(), selectedValues.get(0));

	Assert.assertEquals(option.getOptionalSelectedValue().get(), selectedValues.get(0));

	//
	// ... but only that value now remains
	//

	List<BrokeringStrategy> values = option.getValues();

	Assert.assertEquals(1, values.size());

	Assert.assertEquals(BrokeringStrategy.HARVESTED, values.get(0));
    }

    /**
     * @param option
     */
    private void allValuesHarvestedSelectedTest(Option<BrokeringStrategy> option) {

	SelectionMode selMode = option.getSelectionMode();
	Assert.assertEquals(SelectionMode.SINGLE, selMode);

	//
	// the value is selected ...
	//
	List<BrokeringStrategy> selectedValues = option.getSelectedValues();

	Assert.assertEquals(1, selectedValues.size());

	Assert.assertEquals(BrokeringStrategy.HARVESTED, selectedValues.get(0));

	Assert.assertEquals(option.getSelectedValue(), selectedValues.get(0));

	Assert.assertEquals(option.getOptionalSelectedValue().get(), selectedValues.get(0));

	//
	// ... but the others are not removed, all the values are still there
	//
	List<BrokeringStrategy> values = option.getValues();

	Assert.assertEquals(LabeledEnum.values(BrokeringStrategy.class), values);
    }

    /**
     * @param emptyOption
     */
    private void emptyOptionSelectionTest(Option<String> emptyOption) {

	SelectionMode multiSelectionMode = emptyOption.getSelectionMode();
	Assert.assertEquals(SelectionMode.UNSET, multiSelectionMode);

	List<String> selectedValues = emptyOption.getSelectedValues();

	Assert.assertEquals(0, selectedValues.size());

	Assert.assertNull(emptyOption.getSelectedValue());

	Assert.assertFalse(emptyOption.getOptionalSelectedValue().isPresent());

	List<String> values = emptyOption.getValues();
	Assert.assertTrue(values.isEmpty());

	assertThrows(UnsetSelectionModeException.class, () -> {
	    emptyOption.select(v -> true);
	});

	values = emptyOption.getValues();
	Assert.assertTrue(values.isEmpty());

	emptyOption.clean();

	emptyOption.getValues();
	Assert.assertTrue(values.isEmpty());
    }

}
