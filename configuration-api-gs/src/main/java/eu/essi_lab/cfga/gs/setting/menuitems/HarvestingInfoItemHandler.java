package eu.essi_lab.cfga.gs.setting.menuitems;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2025 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
import java.util.Optional;

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
public class HarvestingInfoItemHandler extends GridMenuItemHandler {

    /**
     * 
     */
    public HarvestingInfoItemHandler() {

    }

    /**
     * @param withTopDivider
     * @param withBottomDivider
     */
    public HarvestingInfoItemHandler(boolean withTopDivider, boolean withBottomDivider) {

	super(withTopDivider, withBottomDivider);
    }

    @Override
    public void onClick(//
	    GridContextMenuItemClickEvent<HashMap<String, String>> event, //
	    TabContent tabContent, //
	    Configuration configuration, //
	    Optional<Setting> setting, //
	    HashMap<String, Boolean> selection) {

	Optional<HashMap<String, String>> item = event.getItem();

	String values = formatRowValues(item);

	ConfirmationDialog dialog = new ConfirmationDialog();
	dialog.setTitle("Harvesting info");
	dialog.setHeight(500, Unit.PIXELS);
	dialog.setWidth(600, Unit.PIXELS);
	dialog.getConfirmButton().setVisible(false);
	dialog.setCancelText("Close");

	TextArea textArea = new TextArea();
	textArea.setValue(values);
	textArea.setWidth(580, Unit.PIXELS);
	textArea.setHeight(375, Unit.PIXELS);
	textArea.getStyle().set("font-size", "14px");

	textArea.setReadOnly(true);

	dialog.setContent(textArea);
	dialog.open();
    }

    @Override
    public boolean isEnabled(//
	    HashMap<String, String> eventItem, //
	    TabContent tabContent, //
	    Configuration configuration, //
	    Setting setting, //
	    HashMap<String, Boolean> selection) {

	return eventItem.get("Info") != null && !eventItem.get("Info").isEmpty();
    }

    @Override
    public String getItemText() {

	return "Harvesting info";
    }

    /**
     * @param rowItem
     * @return
     */
    private String formatRowValues(Optional<HashMap<String, String>> rowItem) {

	if (rowItem.isEmpty()) {
	    return "";
	}

	StringBuilder builder = new StringBuilder();
	String name = rowItem.get().get("Name");
	String type = rowItem.get().get("Type");
	String id = rowItem.get().get("Id");
	String comment = rowItem.get().get("Comment");
	String repeat = rowItem.get().get("Repeat (D-h)");
	String status = rowItem.get().get("Status");
	String firedTime = rowItem.get().get("Fired time");
	String endTime = rowItem.get().get("End time");
	String elTime = rowItem.get().get("El. time (HH:mm:ss)");
	String nextFireTime = rowItem.get().get("Next fire time");
	String size = rowItem.get().get("Size");
	String info = rowItem.get().get("Info");

	builder.append("- Name: ").append(name).append("\n");
	if (type != null) {
	    builder.append("- Type: ").append(type).append("\n");
	}

	if (id != null) {
	    builder.append("- Id: ").append(id).append("\n");
	}

	if (comment != null && !comment.isEmpty()) {
	    builder.append("- Comment: ").append(comment).append("\n");
	}
	if (repeat != null && !repeat.isEmpty()) {
	    builder.append("- Repeat (D-h): ").append(repeat).append("\n");
	}
	if (status != null && !status.isEmpty()) {
	    builder.append("- Status: ").append(status).append("\n");
	}
	if (firedTime != null && !firedTime.isEmpty()) {
	    builder.append("- Fired time: ").append(firedTime).append("\n");
	}
	if (endTime != null && !endTime.isEmpty()) {
	    builder.append("- End time: ").append(endTime).append("\n");
	}
	if (elTime != null && !elTime.isEmpty()) {
	    builder.append("- El. time (HH:mm:ss): ").append(elTime).append("\n");
	}
	if (nextFireTime != null && !nextFireTime.isEmpty()) {
	    builder.append("- Next fire time: ").append(nextFireTime).append("\n");
	}
	if (size != null && !size.isEmpty()) {
	    builder.append("- Size: ").append(size).append("\n");
	}
	if (info != null && !info.isEmpty()) {
	    builder.append(info).append("\n");
	}

	return builder.toString();
    }
}
