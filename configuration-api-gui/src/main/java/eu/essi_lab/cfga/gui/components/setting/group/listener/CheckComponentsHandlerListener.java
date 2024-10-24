package eu.essi_lab.cfga.gui.components.setting.group.listener;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2024 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.util.Set;

import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.data.selection.MultiSelectionEvent;
import com.vaadin.flow.data.selection.MultiSelectionListener;

import eu.essi_lab.cfga.gui.IdleTracker;
import eu.essi_lab.cfga.gui.components.setting.group.CheckComponentsHandler;

/**
 * @author Fabrizio
 */
@SuppressWarnings("serial")
public class CheckComponentsHandlerListener implements MultiSelectionListener<CheckboxGroup<String>, String> {

    private CheckComponentsHandler handler;

    /**
     * @param handler
     */
    public CheckComponentsHandlerListener(CheckComponentsHandler handler) {

	this.handler = handler;
    }

    @Override
    public void selectionChange(MultiSelectionEvent<CheckboxGroup<String>, String> event) {

	String value = "NONE";

	Set<String> addedSelection = event.getAddedSelection();
	Set<String> allSelectedItems = event.getAllSelectedItems();

	if (!addedSelection.isEmpty()) {
	    value = addedSelection.iterator().next();

	} else if (!allSelectedItems.isEmpty()) {
	    value = allSelectedItems.iterator().next();
	}

	handler.setVisibility(value);

	if (event.isFromClient()) {

	    IdleTracker.getInstance().reset();

	    handler.getGroupSetting().select(set -> allSelectedItems.contains(set.getName()));
	}
    }
}
