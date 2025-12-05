package eu.essi_lab.cfga.gui.components.option;

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

import com.vaadin.flow.component.textfield.TextArea;

import eu.essi_lab.cfga.gui.components.StringValuesReader;
import eu.essi_lab.cfga.gui.components.option.listener.OptionValueChangeListener;
import eu.essi_lab.cfga.option.Option;

/**
 * @author Fabrizio
 */
@SuppressWarnings("serial")
public class OptionTextArea extends TextArea {

    /**
     * @param option
     * @param forceReadonly
     */
    public OptionTextArea(Option<?> option, boolean forceReadonly) {

	getStyle().set("font-size","14px");

	//
	// Option
	//
	if (option.getValue() != null) {

	    // GSLoggerFactory.getLogger(getClass()).debug("Primitive option value: " + option.getValue());

	    //
	    //
	    String value = String.join("\n", StringValuesReader.readValues(option));

	    setValue(value);

	} else {

	    // GSLoggerFactory.getLogger(getClass()).debug("No value present");
	}

	//
	// Required
	//
	if (option.isRequired() && !forceReadonly) {

	    setRequired(true);

	    setRequiredIndicatorVisible(true);
	    setErrorMessage("A value is required");

	    if (option.getValue() == null) {
		setInvalid(true);
	    }
	}

	//
	// Editable
	//
	if (!option.isEditable() || forceReadonly) {

	    setReadOnly(true);
	}

	//
	// State
	//
	if (option.canBeDisabled()) {

	    setEnabled(option.isEnabled());
	}

	//
	// Listener
	//
	addValueChangeListener(new OptionValueChangeListener(option));
    }
}
