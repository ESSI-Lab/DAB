package eu.essi_lab.cfga.gui.components.option.listener;

import java.util.Arrays;

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

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.HasValue.ValueChangeEvent;

import eu.essi_lab.cfga.gui.components.listener.AbstractValueChangeListener;
import eu.essi_lab.cfga.gui.components.option.OptionDoubleField;
import eu.essi_lab.cfga.gui.components.option.OptionIntegerField;
import eu.essi_lab.cfga.gui.components.option.OptionTextArea;
import eu.essi_lab.cfga.gui.components.option.OptionTextField;
import eu.essi_lab.cfga.option.ISODateTime;
import eu.essi_lab.cfga.option.Option;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.LabeledEnum;

/**
 * @author Fabrizio
 */
@SuppressWarnings("serial")
public class OptionValueChangeListener extends AbstractValueChangeListener {

    private Option<?> option;

    /**
     * @param option
     */
    public OptionValueChangeListener(Option<?> option) {

	this.option = option;
    }

    @Override
    protected void handleEvent(ValueChangeEvent<?> event) {

	Object value = event.getValue();

	HasValue<?, ?> hasValue = event.getHasValue();

	Class<?> valueClass = option.getValueClass();

	// GSLoggerFactory.getLogger(getClass()).debug("Source component class: " +
	// hasValue.getClass().getSimpleName());
	//
	// GSLoggerFactory.getLogger(getClass()).debug("Value to set: " + value);
	//
	// GSLoggerFactory.getLogger(getClass()).debug("Value class: " + value.getClass().getSimpleName());
	//
	// GSLoggerFactory.getLogger(getClass()).debug("Option value class: " + valueClass.getSimpleName());

	//
	// Single value from a text field
	//
	if (hasValue instanceof OptionTextField) {

	    // GSLoggerFactory.getLogger(getClass()).debug("Single value from a restricted text field");

	    OptionTextField field = (OptionTextField) hasValue;

	    //
	    // the second case is required to support the clear button
	    // in the text fields
	    //
	    if (!field.isInvalid() || (value != null && value.toString().isEmpty())) {

		setObjectValue(valueClass, option, value);

	    } else {

		// GSLoggerFactory.getLogger(getClass()).debug("Invalid field, value not set");
	    }

	    return;
	}

	//
	// Single value from a text area
	//
	if (hasValue instanceof OptionTextArea) {

	    // GSLoggerFactory.getLogger(getClass()).debug("Single value from a restricted text field");

	    OptionTextArea area = (OptionTextArea) hasValue;

	    //
	    //
	    //
	    if (!area.isInvalid()) {

		setObjectValue(valueClass, option, value);

	    } else {

		// GSLoggerFactory.getLogger(getClass()).debug("Invalid field, value not set");
	    }

	    return;
	}

	//
	// Single value from an integer field
	//
	if (hasValue instanceof OptionIntegerField) {

	    // GSLoggerFactory.getLogger(getClass()).debug("Single value from an integer field");

	    OptionIntegerField field = (OptionIntegerField) hasValue;

	    //
	    // the second case is required to support the clear button
	    // in the text fields
	    //
	    if (!field.isInvalid() || (value != null && value.toString().isEmpty())) {

		setObjectValue(valueClass, option, value);

	    } else {

		// GSLoggerFactory.getLogger(getClass()).debug("Invalid field, value not set");
	    }

	    return;
	}

	//
	// Single value from an double field
	//
	if (hasValue instanceof OptionDoubleField) {

	    // GSLoggerFactory.getLogger(getClass()).debug("Single value from a double field");

	    OptionDoubleField field = (OptionDoubleField) hasValue;

	    //
	    // the second case is required to support the clear button
	    // in the text fields
	    //
	    if (!field.isInvalid() || (value != null && value.toString().isEmpty())) {

		setObjectValue(valueClass, option, value);
	    } else {

		// GSLoggerFactory.getLogger(getClass()).debug("Invalid field, value not set");
	    }

	    return;
	}

	//
	// Single value from a date time picker
	//
	if (valueClass.equals(ISODateTime.class)) {

	    // GSLoggerFactory.getLogger(getClass()).debug("Single value from a date picker");

	    ISODateTime isoDateTime = ISODateTime.fromValue(value.toString());
	    option.setObjectValue(isoDateTime);

	    return;
	}

	//
	// Single labeled enum value from a select
	//
	if (LabeledEnum.class.isAssignableFrom(valueClass)) {

	    // GSLoggerFactory.getLogger(getClass()).debug("Single labeled enum value from a select");

	    @SuppressWarnings({ "unchecked" })
	    LabeledEnum enumValue = (LabeledEnum) LabeledEnum.valueOf(//
		    (Class<? extends LabeledEnum>) valueClass, //
		    value.toString()).get();

	    option.select(v -> v.equals(enumValue));

	    Object selectedValue = option.getSelectedValue();
	    if (selectedValue == null) {

		GSLoggerFactory.getLogger(getClass()).warn("Selection failed for option {}", option.getKey());
	    }

	    return;
	}

	//
	// Single enum value from a select
	//
	if (Enum.class.isAssignableFrom(valueClass)) {

	    // GSLoggerFactory.getLogger(getClass()).debug("Single enum value from a select");

	    @SuppressWarnings({ "unchecked", "rawtypes" })
	    Enum enumValue = Enum.valueOf((Class<? extends Enum>) valueClass, value.toString());

	    option.select(v -> v.equals(enumValue));

	    Object selectedValue = option.getSelectedValue();
	    if (selectedValue == null) {

		GSLoggerFactory.getLogger(getClass()).warn("Selection failed for option {}", option.getKey());
	    }

	    return;
	}

	//
	// Multiple primitive values from a select
	//
	if (value instanceof Set) {

	    // GSLoggerFactory.getLogger(getClass()).debug("Multiple primitive values from a select");

	    Set<?> set = ((Set<?>) value).//
		    stream().//
		    map(v -> castObjectValue(valueClass, v)).//
		    collect(Collectors.toSet());

	    option.select(v -> set.contains(v));

	    List<?> selectedValues = option.getSelectedValues();
	    if (selectedValues.isEmpty()) {

		GSLoggerFactory.getLogger(getClass()).warn("Selection failed for option {}", option.getKey());
	    }

	    return;
	}

	//
	// Single primitive value from a select
	//
	// GSLoggerFactory.getLogger(getClass()).debug("Single primitive value from a select");

	Object castedValue = castObjectValue(valueClass, value);

	option.select(v -> v.equals(castedValue));

	Object selectedValue = option.getSelectedValue();
	if (selectedValue == null) {

	    GSLoggerFactory.getLogger(getClass()).warn("Selection failed for option {}", option.getKey());
	}
    }

    /**
     * @param valueClass
     * @param option
     * @param value
     */
    private void setObjectValue(Class<?> valueClass, Option<?> option, Object value) {

	//
	// the second case is required to support the clear button
	// in the text fields
	//
	if (value == null || value.toString().isEmpty()) {

	    option.clearValues();
	    return;
	}

	//
	// for Integer and String values, multi comma separated values are
	// also supported (e.g: 1,5,7 or a,b,c)
	//

	if (valueClass.equals(Double.class)) {

	    Double double_ = Double.valueOf(value.toString());
	    option.setObjectValue(double_);

	} else if (valueClass.equals(Integer.class)) {

	    if (option.isMultiValue()) {

		List<Integer> values = Arrays.asList(value.toString().split(",")).//
			stream().//
			map(v -> Integer.valueOf(v)).//
			collect(Collectors.toList());

		option.setObjectValues(values);

	    } else {

		Integer integer = Integer.valueOf(value.toString());
		option.setObjectValue(integer);
	    }

	} else if (valueClass.equals(String.class)) {

	    if (option.isMultiValue()) {

		List<String> values = Arrays.asList(value.toString().split(",")).//
			stream().//
			collect(Collectors.toList());

		option.setObjectValues(values);

	    } else {

		option.setObjectValue(value.toString());
	    }
	}
    }

    /**
     * @param valueClass
     * @param value
     */
    @SuppressWarnings("unchecked")
    private <T> T castObjectValue(Class<T> valueClass, Object value) {

	if (valueClass.equals(Integer.class)) {

	    Integer integer = Integer.valueOf(value.toString());
	    return (T) integer;

	} else if (valueClass.equals(Double.class)) {

	    Double double_ = Double.valueOf(value.toString());
	    return (T) double_;
	}

	return (T) value.toString();
    }

}
