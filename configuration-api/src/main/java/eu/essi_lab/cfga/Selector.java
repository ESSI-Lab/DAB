/**
 * 
 */
package eu.essi_lab.cfga;

import java.util.List;
import java.util.stream.Collectors;

import eu.essi_lab.cfga.setting.Setting;
import eu.essi_lab.cfga.setting.SettingUtils;

/**
 * Specialized kind of {@link Setting} used to select other settings
 * 
 * @author Fabrizio
 */
public abstract class Selector<S extends Setting> extends Setting {

    /**
     * 
     */
    public Selector() {

	setName("Selector");

	setSelectionMode(SelectionMode.SINGLE);

	initSettings().forEach(s -> addSetting(s));

	getSettings().//
		stream().//
		sorted((s1, s2) -> s1.getName().compareTo(s2.getName())).//
		findFirst().//
		get().//
		setSelected(true);
    }

    /**
     * Initializes the settings to select
     * 
     * @return
     */
    protected abstract List<S> initSettings();

    /**
     * @return
     */
    @SuppressWarnings("unchecked")
    public List<S> getSelectedSettings() {

	return (List<S>) getSettings().//
		stream().filter(s -> s.isSelected()).//
		map(s -> SettingUtils.downCast(s, s.getSettingClass())).//
		collect(Collectors.toList());
    }
}
