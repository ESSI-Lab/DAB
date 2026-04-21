package eu.essi_lab.cfga.setting.editable.notgs.test;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.json.JSONException;
import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.cfga.EditableSetting;
import eu.essi_lab.cfga.Selectable.SelectionMode;
import eu.essi_lab.cfga.SelectionUtils;
import eu.essi_lab.cfga.option.Option;
import eu.essi_lab.cfga.option.ValuesLoader;
import eu.essi_lab.cfga.setting.Setting;

public class EditSettingTest {

    /**
     * 
     */
    @Test
    public void notEditableTestSetting1Test() {

	NotEditableTestSetting1 originalSetting = new NotEditableTestSetting1();

	Assert.assertFalse(EditableSetting.test(originalSetting));
    }

    /**
     * 
     */
    @Test
    public void notEditableTestSetting2Test() {

	NotEditableTestSetting2 originalSetting = new NotEditableTestSetting2();

	Assert.assertFalse(EditableSetting.test(originalSetting));
    }

    /**
     * @throws JSONException
     * @throws ClassNotFoundException
     * @throws InterruptedException
     */
    @Test
    public void editableTestSettingTest() throws ClassNotFoundException, JSONException, InterruptedException {

	EditableTestSetting originalSetting = new EditableTestSetting();

	Assert.assertTrue(EditableSetting.test(originalSetting));

	//
	// makes the init values test
	//
	initSettingsAndValuesTest(originalSetting, true);

	//
	// makes some selection on main setting
	//
	originalSetting.getMainOption().select(v -> v.equals("a1"));

	//
	// makes some selection on setting 1
	//
	originalSetting.getSetting1().setSelected(true);

	originalSetting.getSetting1().getOptions().get(0).select(v -> v.equals("b2"));

	//
	// makes some selection on setting 2
	//
	originalSetting.getSetting2().setSelected(true);

	originalSetting.getSetting2().getOption("option2", String.class).get().select(v -> v.equals("c3"));

	// simulation of user behaviour. the option3 loader is called, the values are
	// loaded and the user selects "option3value2"
	ValuesLoader<String> loader = originalSetting.getSetting2().getOption("option3", String.class).get().getLoader().get();
	loader.load((values, exception) -> {

	    originalSetting.getSetting2().getOption("option3", String.class).get().setValues(values);
	    originalSetting.getSetting2().getOption("option3", String.class).get().select(v -> v.equals("option3value2"));

	}, Optional.empty());

	// loading is async, so we wait one second
	Thread.sleep(1000);

	// we check the option 3 values, to verify loader is working
	Option<String> option3 = originalSetting.getSetting2().getOption("option3", String.class).get();
	List<String> values = option3.getValues().stream().sorted(String::compareTo).collect(Collectors.toList());
	Assert.assertEquals("option3value1", values.get(0));
	Assert.assertEquals("option3value2", values.get(1));
	Assert.assertEquals("option3value3", values.get(2));

	//
	// makes the selection test
	//
	selectionTest(originalSetting, true);

	//
	// removes unselected
	//
	SelectionUtils.deepClean(originalSetting);

	//
	// makes the values and selection test after deep clean
	// selection is the same except setting3 that has been removed
	//
	afterRemovalSettingsAndValuesTest(originalSetting);
	selectionTest(originalSetting, false);

	//
	// applies the resetAndSelect method
	//

	Setting resetAndSelectedSetting = SelectionUtils.resetAndSelect(originalSetting, true);

	Assert.assertEquals(resetAndSelectedSetting.getSettingClass(), originalSetting.getSettingClass());

	Assert.assertEquals(resetAndSelectedSetting.getSettingClass(), originalSetting.getSettingClass());

	//
	// --- THIS IS THE MAIN TEST ---
	//

	// 1) We expect that the values of the reset and selected settings are the same of the original
	// except for option 3 since the values has been loaded after the initialization, so now we expect
	// to find not the 3 loaded values, but just one value, the one selected by the user
	//

	initSettingsAndValuesTest(resetAndSelectedSetting, false);

	//
	// 2) The selection must be the same of the original, with the setting 3 included
	//
	selectionTest(resetAndSelectedSetting, true);

    }

    /**
     * @param resetAndSelectTestSetting
     */
    private void afterRemovalSettingsAndValuesTest(EditableTestSetting resetAndSelectTestSetting) {

	SelectionMode selMode = resetAndSelectTestSetting.getSelectionMode();
	Assert.assertEquals(SelectionMode.UNSET, selMode);

	List<Setting> settings = resetAndSelectTestSetting.getSettings();
	Assert.assertEquals(2, settings.size());
	settings.sort((s1, s2) -> s1.getIdentifier().compareTo(s2.getIdentifier()));

	selMode = settings.get(0).getSelectionMode();
	Assert.assertEquals(SelectionMode.UNSET, selMode);

	selMode = settings.get(1).getSelectionMode();
	Assert.assertEquals(SelectionMode.UNSET, selMode);

	Assert.assertEquals("setting1", settings.get(0).getIdentifier());
	Assert.assertEquals("setting2", settings.get(1).getIdentifier());

	List<String> mainOptionValues = resetAndSelectTestSetting.getMainOption().getValues();
	Assert.assertEquals(1, mainOptionValues.size());
	Assert.assertEquals("a1", mainOptionValues.get(0));

	//
	//
	//

	Option<String> option1 = resetAndSelectTestSetting.getSetting1().getOptions(String.class).get(0);
	List<String> option1values = option1.getValues();
	Assert.assertEquals(1, option1values.size());
	Assert.assertEquals("b2", option1values.get(0));

	//
	//
	//

	Option<String> option2 = resetAndSelectTestSetting.getSetting2().getOption("option2", String.class).get();
	List<String> option2values = option2.getValues();
	Assert.assertEquals(1, option2values.size());
	Assert.assertEquals("c3", option2values.get(0));

	//
	//
	//

	Option<String> option3 = resetAndSelectTestSetting.getSetting2().getOption("option3", String.class).get();
	List<String> option3values = option3.getValues();
	Assert.assertEquals(1, option3values.size());
	Assert.assertEquals("option3value2", option3values.get(0));

    }

    /**
     * @param resetAndSelectSetting
     * @param setting3present
     */
    private void selectionTest(Setting resetAndSelectSetting, boolean setting3present) {

	//
	//
	//

	List<String> mainOptionSelectedValues = resetAndSelectSetting.getOptions(String.class).get(0).getSelectedValues();
	Assert.assertEquals(1, mainOptionSelectedValues.size());
	Assert.assertEquals("a1", mainOptionSelectedValues.get(0));

	//
	//
	//

	List<Setting> settings = resetAndSelectSetting.getSettings();
	settings.sort((s1, s2) -> s1.getIdentifier().compareTo(s2.getIdentifier()));

	//
	//
	//

	Assert.assertTrue(settings.get(0).isSelected());
	List<String> option1SelValues = settings.get(0).getOptions(String.class).get(0).getSelectedValues();
	Assert.assertEquals(1, option1SelValues.size());
	Assert.assertEquals("b2", option1SelValues.get(0));

	//
	//
	//

	Assert.assertTrue(settings.get(1).isSelected());

	//
	//
	//

	List<String> option2SelValues = settings.get(1).getOption("option2", String.class).get().getSelectedValues();

	Assert.assertEquals(1, option2SelValues.size());
	Assert.assertEquals("c3", option2SelValues.get(0));

	//
	//
	//
	Option<String> option3 = settings.get(1).getOption("option3", String.class).get();
	List<String> opt3selValues = option3.getSelectedValues();
	Assert.assertEquals(1, opt3selValues.size());
	Assert.assertEquals("option3value2", opt3selValues.get(0));

	if (setting3present) {

	    Assert.assertFalse(settings.get(2).isSelected());

	} else {

	    Assert.assertTrue(settings.size() == 2);
	}

    }

    /**
     * @param resetAndSelectSetting
     */
    private void initSettingsAndValuesTest(Setting resetAndSelectSetting, boolean initialiazation) {

	SelectionMode selMode = resetAndSelectSetting.getSelectionMode();
	Assert.assertEquals(SelectionMode.SINGLE, selMode);

	//
	// 3 settings
	//
	List<Setting> settings = resetAndSelectSetting.getSettings();
	Assert.assertEquals(3, settings.size());

	settings.sort((s1, s2) -> s1.getIdentifier().compareTo(s2.getIdentifier()));

	selMode = settings.get(0).getSelectionMode();
	Assert.assertEquals(SelectionMode.MULTI, selMode);

	selMode = settings.get(1).getSelectionMode();
	Assert.assertEquals(SelectionMode.MULTI, selMode);

	//
	//
	//

	List<String> mainOptionValues = resetAndSelectSetting.getOption("mainOption", String.class).get().getValues();
	Assert.assertEquals(3, mainOptionValues.size());
	Assert.assertEquals("a1", mainOptionValues.get(0));
	Assert.assertEquals("a2", mainOptionValues.get(1));
	Assert.assertEquals("a3", mainOptionValues.get(2));

	//
	//
	//

	Option<String> option1 = settings.get(0).getOption("option1", String.class).get();
	List<String> option1values = option1.getValues();
	Assert.assertEquals(3, option1values.size());
	Assert.assertEquals("b1", option1values.get(0));
	Assert.assertEquals("b2", option1values.get(1));
	Assert.assertEquals("b3", option1values.get(2));

	//
	//
	//

	Option<String> option2 = settings.get(1).getOption("option2", String.class).get();

	List<String> option2values = option2.getValues();
	Assert.assertEquals(3, option2values.size());
	Assert.assertEquals("c1", option2values.get(0));
	Assert.assertEquals("c2", option2values.get(1));
	Assert.assertEquals("c3", option2values.get(2));

	Option<String> option3 = settings.get(1).getOption("option3", String.class).get();

	if (initialiazation) {

	    // this option has no values during initialization, since they must be still loaded
	    List<String> option3values = option3.getValues();
	    Assert.assertEquals(0, option3values.size());

	} else {

	    // now the option after the reset and select method has values equals
	    // to the selected values, that is "option3value2"

	    List<String> option3values = option3.getValues();
	    Assert.assertEquals(1, option3values.size());

	    Assert.assertEquals("option3value2", option3values.get(0));
	}

    }
}
