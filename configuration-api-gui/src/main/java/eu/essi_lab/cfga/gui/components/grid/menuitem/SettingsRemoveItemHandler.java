/**
 * 
 */
package eu.essi_lab.cfga.gui.components.grid.menuitem;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu.GridContextMenuItemClickEvent;
import com.vaadin.flow.component.textfield.TextArea;

import eu.essi_lab.cfga.Configuration;
import eu.essi_lab.cfga.gui.components.tabs.TabContent;
import eu.essi_lab.cfga.gui.components.grid.GridMenuItemHandler;
import eu.essi_lab.cfga.gui.dialog.ConfirmationDialog;
import eu.essi_lab.cfga.setting.Setting;

/**
 * @author Fabrizio
 */
public class SettingsRemoveItemHandler extends GridMenuItemHandler {

    /**
     * 
     */
    public SettingsRemoveItemHandler() {

    }

    /**
     * @param withTopDivider
     * @param withBottomDivider
     */
    public SettingsRemoveItemHandler(boolean withTopDivider, boolean withBottomDivider) {

	super(withTopDivider, withBottomDivider);
    }

    @Override
    public void onClick(//
	    GridContextMenuItemClickEvent<HashMap<String, String>> event, //
	    TabContent tabContent, //
	    Configuration configuration, //
	    Optional<Setting> setting, //
	    HashMap<String, Boolean> selection) {

	String names = configuration.//
		list().//
		stream().//
		filter(s -> selection.containsKey(s.getIdentifier()) && selection.get(s.getIdentifier())).//
		map(s -> "- " + s.getName()).//
		collect(Collectors.joining("\n"));

	ConfirmationDialog dialog = new ConfirmationDialog();
	dialog.setTitle("Remove selected settings");
	dialog.setHeight(500, Unit.PIXELS);
	dialog.setWidth(600, Unit.PIXELS);

	String text = "Are you sure you want to remove the following settings?\n\n";
	text += names;

	TextArea textArea = new TextArea();
	textArea.setHeight(365, Unit.PIXELS);
	textArea.setValue(text);
	textArea.setWidth(580, Unit.PIXELS);
	textArea.getStyle().set("font-size", "14px");
	textArea.setReadOnly(true);

	dialog.setContent(textArea);
	dialog.open();

	//
	//
	//

	List<String> ids = configuration.//
		list().//
		stream().//
		filter(s -> selection.containsKey(s.getIdentifier()) && selection.get(s.getIdentifier())).//
		map(Setting::getIdentifier).//
		collect(Collectors.toList());

	dialog.setOnConfirmListener(e -> {

	    configuration.remove(ids);

	    tabContent.removeSettingComponents(ids);
	});
    }

    @Override
    public boolean isEnabled(//
	    HashMap<String, String> eventItem, //
	    TabContent tabContent, //
	    Configuration configuration, //
	    Setting setting, //
	    HashMap<String, Boolean> selection) {

	return selection.values().stream().anyMatch(v -> v == true);
    }

    @Override
    public String getItemText() {

	return "Remove selected settings";
    }

}
