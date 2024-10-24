package eu.essi_lab.cfga.setting.test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.cfga.Selectable.SelectionMode;
import eu.essi_lab.cfga.setting.Setting;
import eu.essi_lab.cfga.setting.scheduling.SchedulerWorkerSetting;

/**
 * @author Fabrizio
 */
public class SettingSelectionTest {

    /**
     * Scheduling cannot be selected
     */
    @Test
    public void schedulingSelectionTest() {

	SchedulerWorkerSetting workerSetting = new SchedulerWorkerSetting();

	Assert.assertNotNull(workerSetting.getScheduling());

	workerSetting.select(s -> true);

	Optional<Setting> findFirst = workerSetting.getSettings().stream().filter(s -> s.isSelected()).findFirst();

	Assert.assertFalse(findFirst.isPresent());

	workerSetting.clean();

	Assert.assertNotNull(workerSetting.getScheduling());
    }

    @Test
    public void selectionTest2() {

	Setting mainSetting = new Setting();

	int settingsCount = 10;

	for (int i = 0; i < settingsCount; i++) {

	    Setting setting = new Setting();
	    setting.setName(String.valueOf(i));
	    mainSetting.addSetting(setting);
	}

	long count = mainSetting.getSettings().stream().filter(s -> s.isSelected()).count();
	Assert.assertEquals(0, count);

	//
	// selects all
	//

	mainSetting.select(s -> true);

	count = mainSetting.getSettings().stream().filter(s -> s.isSelected()).count();
	Assert.assertEquals(settingsCount, count);

	//
	// selects 1, the other are unselected
	//

	mainSetting.select(s -> s.getName().equals("0"));

	count = mainSetting.getSettings().stream().filter(s -> s.isSelected()).count();
	Assert.assertEquals(1, count);

	count = mainSetting.getSettings().stream().filter(s -> !s.isSelected()).count();
	Assert.assertEquals(settingsCount - 1, count);

	//
	// selects 1, the last wins, the other are unselected
	//

	mainSetting.select(s -> s.getName().equals("0"));
	mainSetting.select(s -> s.getName().equals("1"));
	mainSetting.select(s -> s.getName().equals("2"));
	mainSetting.select(s -> s.getName().equals("3"));
	mainSetting.select(s -> s.getName().equals("4"));

	count = mainSetting.getSettings().stream().filter(s -> s.isSelected()).count();
	Assert.assertEquals(1, count);

	Setting setting = mainSetting.getSettings().stream().filter(s -> s.isSelected()).findFirst().get();
	Assert.assertEquals("4", setting.getName());

	count = mainSetting.getSettings().stream().filter(s -> !s.isSelected()).count();
	Assert.assertEquals(settingsCount - 1, count);

	//
	// selects 5, the other are unselected
	//

	List<String> asList = Arrays.asList("0", "1", "2", "3", "4");

	mainSetting.select(s -> asList.contains(s.getName()));

	count = mainSetting.getSettings().stream().filter(s -> s.isSelected()).count();
	Assert.assertEquals(settingsCount - 5, count);

	count = mainSetting.getSettings().stream().filter(s -> !s.isSelected()).count();
	Assert.assertEquals(settingsCount - 5, count);

	boolean anyMatch = mainSetting.getSettings().stream().filter(s -> s.isSelected()).anyMatch(s -> asList.contains(s.getName()));
	Assert.assertTrue(anyMatch);

	List<Setting> collect = mainSetting.getSettings().stream().filter(s -> s.isSelected())
		.sorted((s1, s2) -> s1.getName().compareTo(s2.getName())).collect(Collectors.toList());

	Assert.assertEquals("0", collect.get(0).getName());
	Assert.assertEquals("1", collect.get(1).getName());
	Assert.assertEquals("2", collect.get(2).getName());
	Assert.assertEquals("3", collect.get(3).getName());
	Assert.assertEquals("4", collect.get(4).getName());

	//
	// deselects all
	//

	mainSetting.select(s -> false);

	count = mainSetting.getSettings().stream().filter(s -> s.isSelected()).count();
	Assert.assertEquals(0, count);

	count = mainSetting.getSettings().stream().filter(s -> !s.isSelected()).count();
	Assert.assertEquals(settingsCount, count);
    }

    @Test
    public void selectionTest() {

	Setting mainSetting = new Setting();

	SelectionMode multiSelectionMode = mainSetting.getSelectionMode();
	Assert.assertEquals(SelectionMode.UNSET, multiSelectionMode);

	//
	//
	//

	initialStateSelectionTest(mainSetting);
	initialStateSelectionTest(new Setting(mainSetting.getObject()));
	initialStateSelectionTest(new Setting(mainSetting.getObject().toString()));

	//
	// with a multi mode set nothing changes
	//

	mainSetting.setSelectionMode(SelectionMode.SINGLE);

	initialStateSelectionTest(mainSetting);
	initialStateSelectionTest(new Setting(mainSetting.getObject()));
	initialStateSelectionTest(new Setting(mainSetting.getObject().toString()));

	//
	// adds two settings, none is selected
	//

	Setting setting1 = new Setting();
	setting1.setName("setting1");

	mainSetting.addSetting(setting1);

	Setting setting2 = new Setting();
	setting2.setName("setting2");

	mainSetting.addSetting(setting2);

	//
	// unset again the multi mode and tries to remove the unselected
	//
	mainSetting.setSelectionMode(SelectionMode.UNSET);
	mainSetting.clean();

	multiModeUnsetNoneSelectedRemoveUnselectedTest(mainSetting);
	multiModeUnsetNoneSelectedRemoveUnselectedTest(new Setting(mainSetting.getObject()));
	multiModeUnsetNoneSelectedRemoveUnselectedTest(new Setting(mainSetting.getObject().toString()));

	//
	// with a multi mode set, since all are unselected, 0 settings must remain
	//
	mainSetting.setSelectionMode(SelectionMode.SINGLE);
	mainSetting.clean();

	multiModeSetNoneSelectedRemoveUnselectedTest(mainSetting);
	multiModeSetNoneSelectedRemoveUnselectedTest(new Setting(mainSetting.getObject()));
	multiModeSetNoneSelectedRemoveUnselectedTest(new Setting(mainSetting.getObject().toString()));

	//
	// set the radio multi mode again and adds the settings again and select the first without remove the other
	//
	mainSetting.setSelectionMode(SelectionMode.SINGLE);

	mainSetting.addSetting(setting1);
	mainSetting.addSetting(setting2);

	mainSetting.select(s -> s.getName().contains("1"));

	multiModeSetSetting1SelectedNoRemovalTest(mainSetting);
	multiModeSetSetting1SelectedNoRemovalTest(new Setting(mainSetting.getObject()));
	multiModeSetSetting1SelectedNoRemovalTest(new Setting(mainSetting.getObject().toString()));

	//
	// now removes the unselected, so only setting 1 should remain
	//
	mainSetting.clean();

	multiModeSetSetting1SelectedUnselectedRemovedTest(mainSetting);
	multiModeSetSetting1SelectedUnselectedRemovedTest(new Setting(mainSetting.getObject()));
	multiModeSetSetting1SelectedUnselectedRemovedTest(new Setting(mainSetting.getObject().toString()));

	//
	// set the radio multi mode again and unselect also setting 1,
	// then removes the unselected so no settings should remain
	//
	mainSetting.setSelectionMode(SelectionMode.SINGLE);

	setting1.setSelected(false);
	mainSetting.clean();

	multiModeSetSetting1UnselectedUnselectedRemovalTest(mainSetting);
	multiModeSetSetting1UnselectedUnselectedRemovalTest(new Setting(mainSetting.getObject()));
	multiModeSetSetting1UnselectedUnselectedRemovalTest(new Setting(mainSetting.getObject().toString()));
    }

    private void multiModeSetSetting1UnselectedUnselectedRemovalTest(Setting mainSetting) {

	Assert.assertEquals(SelectionMode.UNSET, mainSetting.getSelectionMode());

	List<Setting> settings = mainSetting.getSettings();
	Assert.assertEquals(0, settings.size());
    }

    private void multiModeSetSetting1SelectedUnselectedRemovedTest(Setting mainSetting) {

	Assert.assertEquals(SelectionMode.UNSET, mainSetting.getSelectionMode());

	List<Setting> settings = mainSetting.getSettings();
	Assert.assertEquals(1, settings.size());

	Assert.assertEquals(1, settings.stream().filter(s -> s.isSelected()).count());

	Setting selSetting = settings.stream().filter(s -> s.isSelected()).findFirst().get();
	Assert.assertEquals("setting1", selSetting.getName());
    }

    private void multiModeSetSetting1SelectedNoRemovalTest(Setting mainSetting) {

	List<Setting> settings = mainSetting.getSettings();
	Assert.assertEquals(2, settings.size());

	Assert.assertEquals(1, settings.stream().filter(s -> s.isSelected()).count());

	Setting selSetting = settings.stream().filter(s -> s.isSelected()).findFirst().get();
	Assert.assertEquals("setting1", selSetting.getName());
    }

    private void multiModeSetNoneSelectedRemoveUnselectedTest(Setting mainSetting) {

	Assert.assertEquals(SelectionMode.UNSET, mainSetting.getSelectionMode());

	List<Setting> settings = mainSetting.getSettings();
	Assert.assertEquals(0, settings.size());
    }

    private void multiModeUnsetNoneSelectedRemoveUnselectedTest(Setting mainSetting) {

	Assert.assertEquals(SelectionMode.UNSET, mainSetting.getSelectionMode());

	List<Setting> settings = mainSetting.getSettings();
	Assert.assertEquals(2, settings.size());

	Assert.assertEquals(0, settings.stream().filter(s -> s.isSelected()).count());
    }

    private void initialStateSelectionTest(Setting mainSetting) {

	List<Setting> settings = mainSetting.getSettings();
	Assert.assertEquals(0, settings.size());

	boolean selected = mainSetting.isSelected();
	Assert.assertFalse(selected);

	mainSetting.select(p -> true);

	mainSetting.clean();

	mainSetting.getSettings();
	Assert.assertEquals(0, settings.size());

	selected = mainSetting.isSelected();
	Assert.assertFalse(selected);
    }
}
