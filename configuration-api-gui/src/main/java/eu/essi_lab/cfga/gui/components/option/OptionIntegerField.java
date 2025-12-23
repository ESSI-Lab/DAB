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

import com.vaadin.flow.component.textfield.IntegerField;

import eu.essi_lab.cfga.gui.components.listener.OnKeyUpValidationListener;
import eu.essi_lab.cfga.gui.components.option.listener.OptionValueChangeListener;
import eu.essi_lab.cfga.option.Option;

@SuppressWarnings("serial")
public class OptionIntegerField extends IntegerField implements OnKeyUpValidationListener<Integer> {

    private Integer minValue;
    private Integer maxValue;

    /**
     * @param option
     * @param forceReadonly
     */
    public OptionIntegerField(Option<?> option, boolean forceReadonly) {

	getStyle().set("font-size","14px");

	setHasControls(true);

	setPreventInvalidInput(true);

	addValueChangeListener(new OptionValueChangeListener(option));

	addKeyUpListener(this);

	if (option.getValue() != null) {

//	    GSLoggerFactory.getLogger(getClass()).debug("Integer value: " + option.getValue());

	    setValue(Integer.valueOf(option.getValue().toString()));

	} else {

//	    GSLoggerFactory.getLogger(getClass()).debug("No value present");
	}

	if (option.getMinValue().isPresent()) {

	    minValue = (Integer) option.getMinValue().get();

	    setMin((int) option.getMinValue().get());
	}

	if (option.getMaxValue().isPresent()) {

	    maxValue = (Integer) option.getMaxValue().get();

	    setMax((int) option.getMaxValue().get());
	}

	if (option.isRequired() && !forceReadonly) {

	    setRequired(true);

	    setRequiredIndicatorVisible(true);
	    setErrorMessage("Required value");

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
    public boolean isInvalid(Integer value) {

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
