package eu.essi_lab.cfga.gui.components.setting.edit_put;

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

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.*;
import eu.essi_lab.cfga.gui.components.*;
import eu.essi_lab.cfga.gui.components.listener.*;
import eu.essi_lab.cfga.gui.dialog.*;
import eu.essi_lab.cfga.setting.validation.*;

import java.util.*;

/**
 * @author Fabrizio
 */
@SuppressWarnings("serial")
public class SettingConfirmationDialog extends ConfirmationDialog {

    /**
     * @param validationResponse
     * @param onConfirmListener
     */
    public SettingConfirmationDialog(ValidationResponse validationResponse, ButtonChangeListener onConfirmListener) {
	
	addToCloseAll();

	setWidth(600, Unit.PIXELS);

	//
	// when confirming, then adds the new or edited setting
	//
	setOnConfirmListener(onConfirmListener);

	//
	// Content
	//
	getContentLayout().getStyle().set("padding", "0px");

	VerticalLayout content = ComponentFactory.createNoSpacingNoMarginVerticalLayout();
	content.getStyle().set("padding-left", "10px");
	content.getStyle().set("padding-bottom", "5px");

	setContent(content);

	//
	// Footer
	//
	getFooterLayout().getStyle().set("border-top", "1px solid lightgray");

	Span infoText = ComponentFactory.createSpan("Are you sure to proceed?");

	if (validationResponse != null) {

	    //
	    // Errors
	    //
	    List<String> errors = validationResponse.getErrors();

	    if (!errors.isEmpty()) {

		infoText = ComponentFactory.createSpan("Please fix the errors in order to proceed");

		setTitle("Validation failed");

		Span errorsLabel = ComponentFactory.createSpan("The following errors are detected:", 15);
		content.add(errorsLabel);

		errors.forEach(error -> {

		    Span label = ComponentFactory.createSpan("- " + error);
		    label.getStyle().set("font-size", "15px");
		    label.getStyle().set("color", "red");

		    content.add(label);
		});

		getConfirmButton().setVisible(false);
		setCancelText("Close");
	    }

	    //
	    // Warnings
	    //
	    List<String> warnings = validationResponse.getWarnings();

	    if (!warnings.isEmpty()) {

		Span warningsLabel = ComponentFactory.createSpan("The following warnings are detected", 15);

		warningsLabel.getStyle().set("margin-top", "10px");

		content.add(warningsLabel);

		warnings.forEach(warn -> {

		    Span label = ComponentFactory.createSpan("- " + warn, 15);
		    label.getStyle().set("color", "#6f6f00");

		    content.add(label);
		});
	    }
	}

	int margin = infoText.getText().equals("Are you sure to proceed?") ? 5 : 20;

	infoText.getStyle().set("margin-top", margin + "px");
	infoText.getStyle().set("margin-bottom", "10px");

	content.add(infoText);
    }
}
