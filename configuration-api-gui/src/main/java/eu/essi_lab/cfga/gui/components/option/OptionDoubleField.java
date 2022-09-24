package eu.essi_lab.cfga.gui.components.option;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2022 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import com.vaadin.flow.component.textfield.NumberField;

import eu.essi_lab.cfga.gui.components.listener.OnKeyUpValidationListener;
import eu.essi_lab.cfga.gui.components.option.listener.OptionValueChangeListener;
import eu.essi_lab.cfga.option.Option;

@SuppressWarnings("serial")
public class OptionDoubleField extends NumberField implements OnKeyUpValidationListener<Double> {

    private Double minValue;
    private Double maxValue;

    /**
     * @param option
     * @param forceReadonly
     */
    @SuppressWarnings("deprecation")
    public OptionDoubleField(Option<?> option, boolean forceReadonly) {

	setHasControls(true);

	setPreventInvalidInput(true);

	addValueChangeListener(new OptionValueChangeListener(option));

	addKeyUpListener(this);

	if (option.getValue() != null) {

//	    GSLoggerFactory.getLogger(getClass()).debug("Double value: " + option.getValue());

	    setValue(Double.valueOf(option.getValue().toString()));

	} else {

//	    GSLoggerFactory.getLogger(getClass()).debug("No value present");
	}

	if (option.getMinValue().isPresent()) {

	    minValue = (Double) option.getMinValue().get();

	    setMin((double) option.getMinValue().get());
	}

	if (option.getMaxValue().isPresent()) {

	    maxValue = (Double) option.getMaxValue().get();

	    setMax((double) option.getMaxValue().get());
	}

	if (option.isRequired()) {

	    setRequired(true);

	    setRequiredIndicatorVisible(true);
	    setErrorMessage("A value is required");

	    if (option.getValue() == null) {
		setInvalid(true);
	    }
	}

	if (!option.isEditable() || forceReadonly) {

	    setReadOnly(true);
	}

	if (option.canBeDisabled()) {

	    setEnabled(option.isEnabled());
	}
    }

    @Override
    public boolean isInvalid(Double value) {

	boolean ltThanMin = false;
	if (minValue != null && value != null) {

	    ltThanMin = value < minValue;
	}

	boolean gtThanMax = false;
	if (maxValue != null && value != null) {

	    gtThanMax = value > maxValue;
	}

	return (ltThanMin || gtThanMax || (isRequiredBoolean() && value == null));

    }

}
