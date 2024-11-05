package eu.essi_lab.cfga.gui.components.option;

import java.util.stream.Collectors;

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

	//
	// Option
	//
	if (option.getValue() != null) {

	    // GSLoggerFactory.getLogger(getClass()).debug("Primitive option value: " + option.getValue());

	    String value = StringValuesReader.readValues(option).//
		    stream().//
		    collect(Collectors.joining("\n"));

	    setValue(value);

	} else {

	    // GSLoggerFactory.getLogger(getClass()).debug("No value present");
	}

	//
	// Required
	//
	if (option.isRequired()) {

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
