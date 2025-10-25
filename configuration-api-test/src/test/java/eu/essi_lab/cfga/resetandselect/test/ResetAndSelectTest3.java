/**
 * 
 */
package eu.essi_lab.cfga.resetandselect.test;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.cfga.SelectionUtils;
import eu.essi_lab.cfga.option.Option;
import eu.essi_lab.cfga.option.StringOptionBuilder;
import eu.essi_lab.cfga.setting.Setting;

/**
 * @author Fabrizio
 */
public class ResetAndSelectTest3 {

    /**
     * @author Fabrizio
     */
    public static class MultiSelectionOptionSetting extends Setting {

	/**
	 * 
	 */
	public MultiSelectionOptionSetting() {

	    addOption(StringOptionBuilder.get().withKey("optionKey").//
		    withMultiSelection().//
		    withValues(Arrays.asList("a", "b", "c")).//
		    withSelectedValue("a").//
		    build());
	}
    }

    @Test
    public void multiSelectionOptionsWithNoSelectedValuesTest() {

	MultiSelectionOptionSetting setting = new MultiSelectionOptionSetting();

	Option<String> option = setting.getOption("optionKey", String.class).get();

	List<String> selectedValues = option.getSelectedValues();

	Assert.assertEquals(1, selectedValues.size());
	Assert.assertEquals("a", selectedValues.get(0));

	//
	//
	//

	option.select(v -> false); // empty selection

	//
	//
	//

	option = setting.getOption("optionKey", String.class).get();

	selectedValues = option.getSelectedValues();

	Assert.assertEquals(0, selectedValues.size());

	//
	//
	//

	Setting resetAndSelect = SelectionUtils.resetAndSelect(setting, false);

	Option<String> option2 = resetAndSelect.getOption("optionKey", String.class).get();

	selectedValues = option2.getSelectedValues();

	Assert.assertEquals(0, selectedValues.size());

	//
	//
	//

	option.select(v -> v.equals("b"));

	Setting resetAndSelect2 = SelectionUtils.resetAndSelect(setting, false);

	Option<String> option3 = resetAndSelect2.getOption("optionKey", String.class).get();

	selectedValues = option3.getSelectedValues();

	Assert.assertEquals(1, selectedValues.size());

	Assert.assertEquals("b", selectedValues.get(0));
    }
}
