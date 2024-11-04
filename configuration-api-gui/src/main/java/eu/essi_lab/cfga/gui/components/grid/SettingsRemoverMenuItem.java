/**
 * 
 */
package eu.essi_lab.cfga.gui.components.grid;

import java.util.HashMap;

import com.vaadin.flow.component.grid.contextmenu.GridContextMenu.GridContextMenuItemClickEvent;

/**
 * @author Fabrizio
 */
public class SettingsRemoverMenuItem implements ContextMenuItem {

    @Override
    public void onClick(GridContextMenuItemClickEvent<HashMap<String, String>> event, HashMap<String, Boolean> selected) {

	System.out.println(selected);
    }

    @Override
    public boolean withSeparator() {

	return true;
    }

    @Override
    public String getItemText() {

	return "Remove selected settings";
    }

}
