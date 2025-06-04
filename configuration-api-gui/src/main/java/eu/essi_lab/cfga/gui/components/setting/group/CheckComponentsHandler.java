package eu.essi_lab.cfga.gui.components.setting.group;

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

import java.util.Set;

import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.checkbox.CheckboxGroupVariant;

import eu.essi_lab.cfga.gui.components.setting.group.listener.CheckComponentsHandlerListener;

/**
 * @author Fabrizio
 */
public class CheckComponentsHandler extends GroupComponentsHandler<CheckboxGroup<String>> {

    private CheckboxGroup<String> checkBoxGroup;

    /**
    * 
    */
    public CheckComponentsHandler() {

	checkBoxGroup = new CheckboxGroup<>();

	checkBoxGroup.setLabel("");
	checkBoxGroup.addThemeVariants(CheckboxGroupVariant.LUMO_VERTICAL);
	checkBoxGroup.getStyle().set("width", "100%");

	checkBoxGroup.addSelectionListener(new CheckComponentsHandlerListener(this));
    }

    @Override
    public void applySelection() {

	Set<String> selection = getSelection();

	getGroupComponent().setValue(selection);
    }

    @Override
    public CheckboxGroup<String> getGroupComponent() {

	return checkBoxGroup;
    }
}
