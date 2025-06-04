package eu.essi_lab.cfga.gui.components.option.listener;

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
import java.util.Optional;
import java.util.stream.Collectors;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.HasValue.ValueChangeEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;

import eu.essi_lab.cfga.gui.components.ComponentFactory;
import eu.essi_lab.cfga.gui.components.listener.AbstractValueChangeListener;
import eu.essi_lab.cfga.gui.components.listener.ButtonChangeListener;
import eu.essi_lab.cfga.gui.components.listener.OnKeyUpValidationListener;
import eu.essi_lab.cfga.gui.dialog.ConfirmationDialog;
import eu.essi_lab.cfga.option.Option;
import eu.essi_lab.cfga.option.ValuesLoader;
import eu.essi_lab.lib.utils.GSLoggerFactory;

/**
 * @author Fabrizio
 */
@SuppressWarnings("serial")
public class OptionValuesLoaderListener implements ButtonChangeListener {

    private Option<?> option;
    private Select<String> singleSelect;
    private MultiSelectComboBox<String> multiSelect;
    private UI ui;

    /**
     * @param option
     * @param singleSelect
     */
    public OptionValuesLoaderListener(Option<?> option, Select<String> singleSelect) {
	this.option = option;
	this.singleSelect = singleSelect;
	this.singleSelect.setReadOnly(true);
	this.ui = UI.getCurrent();

    }

    /**
     * @param option
     * @param multiSelect
     */
    public OptionValuesLoaderListener(Option<?> option, MultiSelectComboBox<String> multiSelect) {
	this.option = option;
	this.multiSelect = multiSelect;
	this.multiSelect.setReadOnly(true);
	this.ui = UI.getCurrent();

    }

    @Override
    public void handleEvent(ClickEvent<Button> event) {

	ValuesLoader<?> loader = option.getLoader().get();

	//
	// loads values after the input is provided by the user
	//

	if (loader.requestInput()) {

	    InputDialog inputDialog = new InputDialog(loader);

	    ui.access(() -> {

		inputDialog.open();

		ui.push();
	    });

	    return;
	}

	//
	// loads values directly
	//

	load(loader, Optional.empty());
    }

    /**
     * @param loader
     * @param input
     */
    private void load(ValuesLoader<?> loader, Optional<String> input) {

	ui.access(() -> {

	    if (singleSelect != null) {

		singleSelect.setLabel("Loading values, please wait...");

	    } else {

		multiSelect.setLabel("Loading values, please wait...");
	    }

	    ui.push();
	});

	loader.load((values, exception) -> {

	    //
	    // in case of exception, set the select read-only and invalid, then shows an error message
	    //
	    if (exception.isPresent()) {

		ui.access(() -> {

		    if (singleSelect != null) {

			singleSelect.setReadOnly(true);
			singleSelect.setErrorMessage("Error occurred");
			singleSelect.setInvalid(true);
			singleSelect.setLabel("");

		    } else {

			multiSelect.setReadOnly(true);
			multiSelect.setErrorMessage("Error occurred");
			multiSelect.setInvalid(true);
			multiSelect.setLabel("");
		    }

		    ui.push();
		});

		return;
	    }

	    GSLoggerFactory.getLogger(ComponentFactory.class).debug("Loaded values: " + values);

	    //
	    // set the values to the option
	    //
	    List<String> stringValues = values.stream().map(v -> v.toString()).collect(Collectors.toList());

	    option.setObjectValues(stringValues);

	    //
	    // updates the select
	    //
	    ui.access(() -> {

		if (singleSelect != null) {

		    singleSelect.setReadOnly(false);
		    singleSelect.setItems(stringValues);
		    singleSelect.setLabel("");
		    singleSelect.setInvalid(false);

		} else {

		    multiSelect.setReadOnly(false);
		    multiSelect.setItems(stringValues);
		    multiSelect.setLabel("");
		    multiSelect.setInvalid(false);
		}

		ui.push();
	    });

	}, input);
    }

    /**
     * @author Fabrizio
     */
    private class InputDialog extends ConfirmationDialog implements OnKeyUpValidationListener<String> {

	/**
	 * @param inputText
	 */
	public InputDialog(ValuesLoader<?> loader) {

	    setModal(false);
	    setWidth(600, Unit.PIXELS);
	    setTitle("Input required");

	    addToCloseAll();

	    VerticalLayout layout = ComponentFactory.createNoSpacingNoMarginVerticalLayout();
	    setContent(layout);

	    Label inputLabel = ComponentFactory.createLabel(loader.getRequestInputText());
	    inputLabel.setWidthFull();

	    layout.add(inputLabel);

	    TextField textField = new TextField();
	    textField.getStyle().set("font-size", "14px");
	    textField.setWidthFull();

	    textField.setErrorMessage("An input is required");
	    textField.setRequired(true);
	    textField.setInvalid(true);

	    textField.addKeyUpListener(this);

	    textField.addValueChangeListener(new AbstractValueChangeListener() {

		@Override
		protected void handleEvent(ValueChangeEvent<?> event) {
		    // nothing to do here, just reset the IdleTracker
		}
	    });

	    layout.add(textField);

	    //
	    //
	    //

	    setCloseOnConfirm(false);

	    setOnConfirmListener((event) -> {

		if (textField.getValue() == null || textField.getValue().isEmpty()) {

		    textField.setInvalid(true);

		    return;
		}

		Optional<String> input = Optional.of(textField.getValue());

		load(loader, input);

		close();
	    });
	}

	@Override
	public boolean isInvalid(String value) {

	    return (value == null || value.isEmpty());
	}
    }
}
