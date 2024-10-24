package eu.essi_lab.cfga.setting.editable.notgs.test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import eu.essi_lab.cfga.EditableSetting;
import eu.essi_lab.cfga.option.Option;
import eu.essi_lab.cfga.option.StringOptionBuilder;
import eu.essi_lab.cfga.option.ValuesLoader;
import eu.essi_lab.cfga.setting.Setting;

/**
 * @author Fabrizio
 */
public class EditableTestSetting extends Setting implements EditableSetting {

    /**
     * 
     */
    public EditableTestSetting() {

	setIdentifier("mainSetting");
	setSelectionMode(SelectionMode.SINGLE);

	Option<String> mainOption = StringOptionBuilder.get().//
		withKey("mainOption").//
		withValues(Arrays.asList("a1", "a2", "a3")).//
		withSingleSelection().//
		build();

	addOption(mainOption);

	//
	//
	//

	Setting setting1 = new Setting();
	setting1.setIdentifier("setting1");
	setting1.setSelectionMode(SelectionMode.MULTI);

	Option<String> option1 = StringOptionBuilder.get().//
		withKey("option1").//
		withValues(Arrays.asList("b1", "b2", "b3")).//
		withSingleSelection().//
		build();

	setting1.addOption(option1);

	addSetting(setting1);

	//
	//
	//

	Setting setting2 = new Setting();
	setting2.setIdentifier("setting2");
	setting2.setSelectionMode(SelectionMode.MULTI);

	Option<String> option2 = StringOptionBuilder.get().//
		withKey("option2").//
		withValues(Arrays.asList("c1", "c2", "c3")).//
		withSingleSelection().//
		build();

	setting2.addOption(option2);

	//
	// special case
	//
	Option<String> option3 = StringOptionBuilder.get().//
		withKey("option3").//
		withValuesLoader(new ResetAndSelectTestLoader()).//
		withSingleSelection().//
		build();

	setting2.addOption(option3);

	addSetting(setting2);

	//
	//
	//

	Setting setting3 = new Setting();
	setting3.setIdentifier("setting3");

	addSetting(setting3);
    }

    /**
     * @author Fabrizio
     */
    public static class ResetAndSelectTestLoader extends ValuesLoader<String> {

	@Override
	protected List<String> loadValues(Optional<String> input) throws Exception {

	    return Arrays.asList("option3value1", "option3value2", "option3value3");
	}
    }

    /**
     * @return
     */
    public Option<String> getMainOption() {

	return getOption("mainOption", String.class).get();
    }

    /**
     * @return
     */
    public Setting getSetting1() {

	return getSettings().stream().filter(s -> s.getIdentifier().contains("1")).findFirst().get();
    }

    /**
     * @return
     */
    public Setting getSetting2() {

	return getSettings().stream().filter(s -> s.getIdentifier().contains("2")).findFirst().get();
    }

    /**
     * @return
     */
    public Optional<Setting> getSetting3() {

	return getSettings().stream().filter(s -> s.getIdentifier().contains("3")).findFirst();
    }
}
