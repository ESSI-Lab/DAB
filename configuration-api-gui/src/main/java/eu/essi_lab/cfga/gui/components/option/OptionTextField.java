package eu.essi_lab.cfga.gui.components.option;

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

import com.vaadin.flow.component.textfield.*;
import eu.essi_lab.cfga.gui.components.*;
import eu.essi_lab.cfga.gui.components.listener.*;
import eu.essi_lab.cfga.gui.components.option.listener.*;
import eu.essi_lab.cfga.option.*;

import java.util.*;
import java.util.regex.*;

@SuppressWarnings("serial")
public class OptionTextField extends TextField implements OnKeyUpValidationListener<String> {

    /**
     *
     */
    public OptionTextField() {

//	setPreventInvalidInput(true);
	setClearButtonVisible(true);
	getStyle().set("font-size","14px");

	addKeyUpListener(this);
    }

    /**
     * @param option
     * @param forceReadonly
     */
    public OptionTextField(Option<?> option, boolean forceReadonly) {

	this();
	setId("option-text-field-for-"+option.getKey());

	//
	// Option
	//
	if (option.getValue() != null) {

	    // GSLoggerFactory.getLogger(getClass()).debug("Primitive option value: " + option.getValue());

	    String values = String.join(",", StringValuesReader.readValues(option));

	    setValue(values);

	} else {

	    // GSLoggerFactory.getLogger(getClass()).debug("No value present");
	}

	//
	// Required
	//
	if (option.isRequired() && !forceReadonly) {

	    setRequired(true);

	    setRequiredIndicatorVisible(true);
	    setErrorMessage("Required value");

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
	// Pattern
	//
	Optional<InputPattern> optionalInputPattern = option.getInputPattern();

	if (optionalInputPattern.isPresent()) {

	    InputPattern inputPattern = optionalInputPattern.get();

	    if (option.isRequired()) {

		setPattern(inputPattern.getRequiredPattern());
	    } else {

		setPattern(inputPattern.getPattern());
	    }
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

    @Override
    public boolean isInvalid(String value) {

	if (getPattern() == null && !isRequired()) {
	    return false;
	}

	boolean matches = true;

	if (getPattern() != null) {

	    Pattern pattern = Pattern.compile(getPattern());
	    Matcher matcher = pattern.matcher(value);

	    matches = matcher.matches();
	}

	return (!matches || (isRequired() && value.isEmpty()));
    }
}
