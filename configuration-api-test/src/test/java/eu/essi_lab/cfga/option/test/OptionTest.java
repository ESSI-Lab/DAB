/**
 * 
 */
package eu.essi_lab.cfga.option.test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import eu.essi_lab.cfga.Configuration;
import eu.essi_lab.cfga.Selectable.SelectionMode;
import eu.essi_lab.cfga.option.InputPattern;
import eu.essi_lab.cfga.option.IntegerOptionBuilder;
import eu.essi_lab.cfga.option.Option;
import eu.essi_lab.cfga.option.StringOptionBuilder;
import eu.essi_lab.cfga.setting.Setting;
import eu.essi_lab.cfga.source.FileSource;
import eu.essi_lab.model.BrokeringStrategy;

/**
 * @author Fabrizio
 */
public class OptionTest {

    @Rule
    public ExpectedException ex = ExpectedException.none();

    @Test
    public void test() {

	Option<String> configurationOption = new Option<>(String.class);

	Class<?> valueClass = configurationOption.getValueClass();
	Assert.assertEquals(String.class, valueClass);

	Assert.assertTrue(configurationOption.isValueOf(String.class));
	Assert.assertFalse(configurationOption.isValueOf(Integer.class));

	StringOptionBuilder builder = StringOptionBuilder.get();

	test1(builder.build());
	test1(configurationOption);
	test1(new Option<>(configurationOption.getObject()));
	test1(new Option<>(configurationOption.getObject().toString()));

	configurationOption.setValues(Arrays.asList("a", "b", "c"));
	configurationOption.setKey("lettersOption");
	configurationOption.setLabel("Choose the letter");
	configurationOption.setSelectionMode(SelectionMode.SINGLE);
	configurationOption.select(v -> v.equals("a"));

	configurationOption.setRequired(true);
	configurationOption.setSelectionMode(SelectionMode.SINGLE);

	configurationOption.setVisible(false);
	configurationOption.setEditable(false);

	configurationOption.setEnabled(false);

	configurationOption.setDescription("desc");

	configurationOption.setAdvanced(true);

	configurationOption.setCanBeDisabled(false);

	configurationOption.setInputPattern(InputPattern.ALPHANUMERIC);

	configurationOption.enableTextArea(true);

	Option<String> buildedOption = builder.//
		withKey("lettersOption").//
		withLabel("Choose the letter").//
		withDescription("desc").//
		withValues(Arrays.asList("a", "b", "c")).//
		withSingleSelection().//
		withSelectedValue("a").//
		required().//
		disabled().//
		advanced().//
		withInputPattern(InputPattern.ALPHANUMERIC).//
		cannotBeDisabled().//
		withSingleSelection().//
		hidden().//
		readOnly().//
		withTextArea().//
		build();

	test2(buildedOption);
	test2(new Option<>(buildedOption.getObject()));
	test2(new Option<>(configurationOption.getObject().toString()));

	test2(configurationOption);
	test2(new Option<>(configurationOption.getObject()));
	test2(new Option<>(configurationOption.getObject().toString()));

	//
	//
	//

	buildedOption = builder.//
		withValue("X").//
		withValue("Y").//
		withMultiSelection().//
		editable().//
		optional().//
		notAdvanced().//
		canBeDisabled().//
		build();

	configurationOption.addValue("X");
	configurationOption.addValue("Y");
	configurationOption.setSelectionMode(SelectionMode.MULTI);
	configurationOption.setEditable(true);
	configurationOption.setRequired(false);
	configurationOption.setAdvanced(false);
	configurationOption.setCanBeDisabled(true);

	test3(buildedOption);
	test3(new Option<>(buildedOption.getObject()));
	test3(new Option<>(configurationOption.getObject().toString()));

	test3(configurationOption);
	test3(new Option<>(configurationOption.getObject()));
	test3(new Option<>(configurationOption.getObject().toString()));

	//
	//
	//

	// nothing is expected to change
	configurationOption.setValues(Arrays.asList("a", "b", "c", "X", "Y"));

	test3(configurationOption);
	test3(new Option<>(configurationOption.getObject()));
	test3(new Option<>(configurationOption.getObject().toString()));

	//
	//
	//

	buildedOption = builder.//
		withValues(Arrays.asList()).//
		build();

	configurationOption.setValues(Arrays.asList());

	test4(buildedOption);
	test4(new Option<>(buildedOption.getObject()));
	test4(new Option<>(buildedOption.getObject().toString()));

	test4(configurationOption);
	test4(new Option<>(configurationOption.getObject()));
	test4(new Option<>(configurationOption.getObject().toString()));

	//
	//
	//

	buildedOption = builder.//
		withNoValues().//
		withUnsetSelection().//
		build();

	configurationOption.setSelectionMode(SelectionMode.UNSET);
	configurationOption.clearValues();

	test5(buildedOption);
	test5(new Option<>(buildedOption.getObject()));
	test5(new Option<>(buildedOption.getObject().toString()));

	test5(configurationOption);
	test5(new Option<>(configurationOption.getObject()));
	test5(new Option<>(configurationOption.getObject().toString()));

	//
	//
	//

	buildedOption = builder.//
		withValues(Arrays.asList("a", "b", "c")).//
		build();

	configurationOption.setValues(Arrays.asList("a", "b", "c"));

	test5_1(buildedOption);
	test5_1(new Option<>(buildedOption.getObject()));
	test5_1(new Option<>(buildedOption.getObject().toString()));

	test5_1(configurationOption);
	test5_1(new Option<>(configurationOption.getObject()));
	test5_1(new Option<>(configurationOption.getObject().toString()));

	Option<String> clearedOption = builder.clear().build();

	test1(clearedOption);

	Option<Integer> option2 = new Option<Integer>(Integer.class);
	option2.setValue(10);

	testIntegerOption(option2);
	testIntegerOption(new Option<>(option2.getObject()));
	testIntegerOption(new Option<>(option2.getObject().toString()));

	//
	// null value test
	//
	ex.expect(IllegalArgumentException.class);
	configurationOption.setValue(null);
	configurationOption.addValue(null);
    }

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void optionInConfigurationTest() throws Exception {

	FileSource configurationFilesSource = new FileSource();

	Configuration configuration = new Configuration(configurationFilesSource);

	Setting setting = new Setting();
	setting.setIdentifier("settingId");

	Option<Integer> intOption = IntegerOptionBuilder.get(Integer.class).//
		withKey("key1").//
		withValue(10).//
		build();

	Option<BrokeringStrategy> strategyOption = new Option<>(BrokeringStrategy.class);
	strategyOption.setKey("key2");
	strategyOption.setBase64EncodedValue();
	strategyOption.setValue(BrokeringStrategy.DISTRIBUTED);

	setting.addOption(intOption);
	setting.addOption(strategyOption);

	configuration.put(setting);

	configuration.flush();

	/////

	configuration = new Configuration(configurationFilesSource);

	Optional<Setting> optional = configuration.get("settingId");

	Option<Integer> optIntOption = optional.get().getOption("key1", Integer.class).get();

	Integer value = optIntOption.getValue();
	Assert.assertEquals(new Integer(10), value);

	Option<BrokeringStrategy> optStrategyOption = optional.get().getOption("key2", BrokeringStrategy.class).get();

	BrokeringStrategy str = optStrategyOption.getValue();
	Assert.assertEquals(BrokeringStrategy.DISTRIBUTED, str);

	//
	//
	//

    }

    private void testIntegerOption(Option<Integer> option2) {

	Integer value = option2.getValue();
	Assert.assertEquals(new Integer(10), value);
    }

    private void test1(Option<String> configurationOption) {

	Assert.assertTrue(configurationOption.getValues().isEmpty());
	Assert.assertTrue(configurationOption.getObject().getString("type").equals("option"));

	Assert.assertFalse(configurationOption.isRequired());
	Assert.assertEquals(SelectionMode.UNSET, configurationOption.getSelectionMode());

	Assert.assertTrue(configurationOption.isVisible());
	Assert.assertTrue(configurationOption.isEditable());

	Assert.assertTrue(configurationOption.isEnabled());

	Assert.assertFalse(configurationOption.getDescription().isPresent());

	Assert.assertFalse(configurationOption.isAdvanced());

	Assert.assertTrue(configurationOption.canBeDisabled());

	Assert.assertFalse(configurationOption.getInputPattern().isPresent());

	Assert.assertFalse(configurationOption.isTextAreaEnabled());
    }

    /**
     * @param option
     * @param original
     */
    private void test2(Option<String> option) {

	Assert.assertFalse(option.isEnabled());

	List<String> values = option.getValues().stream().sorted().collect(Collectors.toList());

	Assert.assertEquals(3, values.size());

	Assert.assertEquals("a", values.get(0));
	Assert.assertEquals("b", values.get(1));
	Assert.assertEquals("c", values.get(2));

	String key = option.getKey();
	Assert.assertEquals("lettersOption", key);

	String label = option.getLabel();
	Assert.assertEquals("Choose the letter", label);

	Optional<String> value = option.getOptionalValue();
	Assert.assertEquals("a", value.get());

	boolean mandatory = option.isRequired();
	Assert.assertTrue(mandatory);

	SelectionMode multiSelectionMode = option.getSelectionMode();
	Assert.assertEquals(SelectionMode.SINGLE, multiSelectionMode);

	boolean visible = option.isVisible();
	Assert.assertFalse(visible);

	boolean editable = option.isEditable();
	Assert.assertFalse(editable);

	boolean advanced = option.isAdvanced();
	Assert.assertTrue(advanced);

	boolean canBeDisabled = option.canBeDisabled();
	Assert.assertFalse(canBeDisabled);

	Optional<InputPattern> inputPattern = option.getInputPattern();
	Assert.assertEquals(InputPattern.ALPHANUMERIC, inputPattern.get());

	String type = option.getObject().getString("type");
	Assert.assertEquals("option", type);

	boolean textArea = option.isTextAreaEnabled();
	Assert.assertTrue(textArea);
    }

    private void test3(Option<String> option) {

	Assert.assertTrue(option.isEditable());
	Assert.assertFalse(option.isRequired());
	Assert.assertFalse(option.isAdvanced());
	Assert.assertTrue(option.canBeDisabled());

	SelectionMode multiSelectionMode = option.getSelectionMode();
	Assert.assertEquals(SelectionMode.MULTI, multiSelectionMode);

	List<String> values = option.getValues();

	List<String> list = values.stream().sorted().collect(Collectors.toList());
	Assert.assertEquals("X", list.get(0));
	Assert.assertEquals("Y", list.get(1));
	Assert.assertEquals("a", list.get(2));

	Assert.assertEquals(5, list.size());
    }

    private void test4(Option<String> configurationOption) {

	Assert.assertTrue(configurationOption.getValues().isEmpty());
    }

    private void test5(Option<String> configurationOption) {

	Assert.assertTrue(configurationOption.getValues().isEmpty());
	Assert.assertNull(configurationOption.getValue());

	Assert.assertEquals(SelectionMode.UNSET, configurationOption.getSelectionMode());
    }

    private void test5_1(Option<String> configurationOption) {

	List<String> values = configurationOption.getValues();
	values.sort(String::compareTo);

	Assert.assertEquals("a", values.get(0));
	Assert.assertEquals("a", configurationOption.getValue());
	Assert.assertEquals("b", values.get(1));
	Assert.assertEquals("b", values.get(1));
    }
}
