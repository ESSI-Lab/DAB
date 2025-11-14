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

import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.radiobutton.RadioGroupVariant;

import eu.essi_lab.cfga.gui.components.setting.group.listener.RadioComponentsHandlerListener;

/**
 * @author Fabrizio
 */
public class RadioComponentsHandler extends GroupComponentsHandler<RadioButtonGroup<String>> {

    private final RadioButtonGroup<String> radioButtonGroup;

    /**
     * 
     */
    public RadioComponentsHandler() {

	radioButtonGroup = new RadioButtonGroup<>();

	radioButtonGroup.setLabel("");
	radioButtonGroup.addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);
	radioButtonGroup.getStyle().set("width", "100%");

	radioButtonGroup.addValueChangeListener(new RadioComponentsHandlerListener(this));
    }

    @Override
    public void applySelection() {

	String value = getSelection().iterator().next();

	getGroupComponent().setValue(value);
    }

    @Override
    public RadioButtonGroup<String> getGroupComponent() {

	return radioButtonGroup;
    }
}
