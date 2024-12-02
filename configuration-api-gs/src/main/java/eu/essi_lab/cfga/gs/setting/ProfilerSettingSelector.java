/**
 * 
 */
package eu.essi_lab.cfga.gs.setting;

import java.util.List;
import java.util.stream.Collectors;

import eu.essi_lab.cfga.ConfigurableLoader;
import eu.essi_lab.cfga.Selector;
import eu.essi_lab.cfga.setting.Setting;
import eu.essi_lab.cfga.setting.SettingUtils;

/**
 * @author Fabrizio
 */
public class ProfilerSettingSelector<P extends ProfilerSetting> extends Selector<P> {

    /**
     * 
     */
    public ProfilerSettingSelector() {

	setName("Select profiler type");
    }

    @SuppressWarnings("unchecked")
    @Override
    protected List<P> initSettings() {

	List<? extends Setting> collect = ConfigurableLoader.//
		loadToList().//
		stream().//
		filter(c -> c.getSetting() != null).//
		filter(c -> ProfilerSetting.class.isAssignableFrom(c.getSetting().getSettingClass())).//
		map(c -> SettingUtils.create(c.getSetting().getSettingClass())).//
		collect(Collectors.toList());

	return (List<P>) collect;
    }
}
