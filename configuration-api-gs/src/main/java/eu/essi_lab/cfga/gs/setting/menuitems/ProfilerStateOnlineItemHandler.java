/**
 * 
 */
package eu.essi_lab.cfga.gs.setting.menuitems;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.vaadin.flow.component.grid.contextmenu.GridContextMenu.GridContextMenuItemClickEvent;

import eu.essi_lab.cfga.Configuration;
import eu.essi_lab.cfga.SelectionUtils;
import eu.essi_lab.cfga.gs.setting.ProfilerSetting;
import eu.essi_lab.cfga.gui.components.TabContainer;
import eu.essi_lab.cfga.gui.components.grid.GridMenuItemHandler;
import eu.essi_lab.cfga.setting.Setting;
import eu.essi_lab.cfga.setting.SettingUtils;

/**
 * @author Fabrizio
 */
public class ProfilerStateOnlineItemHandler extends GridMenuItemHandler {

    /**
     * 
     */
    public ProfilerStateOnlineItemHandler() {
    }

    /**
     * @param withTopDivider
     * @param withBottomDivider
     */
    public ProfilerStateOnlineItemHandler(boolean withTopDivider, boolean withBottomDivider) {

	super(withTopDivider, withBottomDivider);
    }

    @Override
    public void onClick(//
	    GridContextMenuItemClickEvent<HashMap<String, String>> event, //
	    TabContainer tabContainer, //
	    Configuration configuration, //
	    Optional<Setting> setting, //
	    HashMap<String, Boolean> selection) {

	List<ProfilerSetting> settings = configuration.//
		list(ProfilerSetting.class, false).//
		stream().//
		filter(s -> selection.containsKey(s.getIdentifier()) && selection.get(s.getIdentifier())).//

		map(s -> SelectionUtils.resetAndSelect(s, false)).//

		map(s -> (ProfilerSetting) SettingUtils.downCast(s, s.getSettingClass())).//

		peek(s -> {

		    s.setOnline(online());
		    SelectionUtils.deepClean(s);

		}).//

		collect(Collectors.toList());

	settings.forEach(s -> configuration.replace(s));

	tabContainer.render(true);
    }

    /**
     * @return
     */
    protected boolean online() {

	return true;
    }

    @Override
    public String getItemText() {

	return "Turn online selected profilers";
    }

    @Override
    public boolean isEnabled(//
	    HashMap<String, String> eventItem, //
	    TabContainer tabContainer, //
	    Configuration configuration, //
	    Setting setting, //
	    HashMap<String, Boolean> selection) {

	return selection.values().stream().anyMatch(v -> v == true);
    }
}
