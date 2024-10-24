package eu.essi_lab.cfga.gui.components.option;

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

import java.util.List;
import java.util.stream.Collectors;

import com.vaadin.flow.component.HasValue.ValueChangeEvent;
import com.vaadin.flow.component.HasValue.ValueChangeListener;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import eu.essi_lab.cfga.Configuration;
import eu.essi_lab.cfga.gui.components.ComponentFactory;
import eu.essi_lab.cfga.gui.components.SettingComponentFactory;
import eu.essi_lab.cfga.option.Option;
import eu.essi_lab.cfga.option.OptionValueMapper;
import eu.essi_lab.cfga.setting.Setting;
import eu.essi_lab.lib.utils.GSLoggerFactory;

/**
 * Draft component for options having {@link Setting} as value. The idea is to use a {@link OptionValueMapper} which
 * converts the {@link Setting}s
 * to their names in order to avoid to load the entire {@link Setting} content as option value
 * 
 * @author Fabrizio
 */
@SuppressWarnings("serial")
public class OptionSettingComponent extends VerticalLayout {

    /**
     * @param configuration
     * @param setting
     * @param option
     */
    @SuppressWarnings("incomplete-switch")
    public OptionSettingComponent(//
	    Configuration configuration, //
	    Setting setting, //
	    Option<? extends Setting> option) {

	setId("option-of-settings-component-for-option-" + option.getKey());
	setSizeFull();

	setMargin(false);
	setSpacing(false);

	getStyle().set("padding", "0px");

	switch (option.getSelectionMode()) {

	case SINGLE: {

	    ComboBox<String> select = new ComboBox<String>();
	    select.setWidthFull();
	    select.getStyle().set("margin-bottom", "10px");

	    add(select);

	    select.setRequired(option.isRequired());
	    select.setRequiredIndicatorVisible(option.isRequired());

	    GSLoggerFactory.getLogger(ComponentFactory.class).debug("Initialing multi select for option: " + option.getKey());

	    //
	    //
	    //

	    List<String> values = option.//
		    getValues().//
		    stream().//
		    map(s -> s.getName()).//
		    sorted().//
		    collect(Collectors.toList());

	    GSLoggerFactory.getLogger(ComponentFactory.class).debug("Values: " + values);

	    select.setItems(values);

	    //
	    //
	    //

	    if (option.canBeDisabled()) {

		select.setEnabled(option.isEnabled());
	    }

	    if (!option.isEditable()) {

		select.setReadOnly(true);
	    }

	    select.addValueChangeListener(new ValueChangeListener<ValueChangeEvent<?>>() {

		@Override
		public void valueChanged(ValueChangeEvent<?> event) {

		    getChildren().filter(c -> !(c instanceof ComboBox)).forEach(c -> remove(c));

		    Object value = event.getValue();

		    option.select(s -> s.getName().equals(value.toString()));

		    Setting selectedValue = option.getSelectedValue();

		    setting.addSetting(selectedValue);

		    add(SettingComponentFactory.createSettingComponent(configuration, selectedValue.getIdentifier(), false));
		}
	    });

	    break;
	}

	case MULTI:

	    MultiSelectComboBox<String> select = new MultiSelectComboBox<>();

	    select.setWidthFull();
	    select.getStyle().set("margin-bottom", "10px");

	    add(select);

	    select.setRequired(option.isRequired());
	    select.setRequiredIndicatorVisible(option.isRequired());

	    GSLoggerFactory.getLogger(ComponentFactory.class).debug("Initialing multi select for option: " + option.getKey());

	    //
	    //
	    //

	    List<String> values = option.//
		    getValues().//
		    stream().//
		    map(s -> s.getName()).//
		    sorted().//
		    collect(Collectors.toList());

	    GSLoggerFactory.getLogger(ComponentFactory.class).debug("Values: " + values);

	    select.setItems(values);

	    //
	    //
	    //

	    if (option.canBeDisabled()) {

		select.setEnabled(option.isEnabled());
	    }

	    if (!option.isEditable()) {

		select.setReadOnly(true);
	    }

	    select.addValueChangeListener(new ValueChangeListener<ValueChangeEvent<?>>() {

		@Override
		public void valueChanged(ValueChangeEvent<?> event) {

		    getChildren().filter(c -> !(c instanceof ComboBox)).forEach(c -> remove(c));

		    Object value = event.getValue();

		    option.select(s -> s.getName().equals(value.toString()));

		    Setting selectedValue = option.getSelectedValue();

		    setting.addSetting(selectedValue);

		    add(SettingComponentFactory.createSettingComponent(configuration, selectedValue.getIdentifier(), false));
		}
	    });
	}
    }
}
