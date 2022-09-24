package eu.essi_lab.cfga.gui.dialog;

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

import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

import eu.essi_lab.cfga.gui.components.ComponentFactory;
import eu.essi_lab.cfga.gui.components.listener.ButtonChangeListener;

/**
 * @author Fabrizio
 */
@SuppressWarnings("serial")
public class ConfirmationDialog extends EnhancedDialog {

    private Button confirmButton;
    private Button cancelButton;

    private ButtonChangeListener onConfirmListener;
    private ButtonChangeListener onCancelListener;
    private boolean closeOnConfirm;
    private boolean closeOnCancel;

    /**
     * 
     */
    public ConfirmationDialog() {

	this(null, null);
    }

    /**
     * @param text
     * @param onConfirmListener
     */
    public ConfirmationDialog(String text, ButtonChangeListener onConfirmListener) {

	this.onConfirmListener = onConfirmListener;

	setWidth(500, Unit.PIXELS);

	setCloseOnConfirm(true);
	setCloseOnCancel(true);

	//
	// Title
	//

	setTitle("Confirmation");

	//
	// Content
	//

	if (text != null) {

	    Label label = ComponentFactory.createLabel(text, 15);

	    setContent(label);
	}

	//
	// Footer
	//

	confirmButton = new Button("Confirm", e -> {

	    if (this.onConfirmListener != null) {

		this.onConfirmListener.onComponentEvent(e);
	    }

	    if (closeOnConfirm) {
		close();
	    }
	});

	confirmButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
	confirmButton.getStyle().set("color", "red");
	confirmButton.getStyle().set("border", "1px solid #80808061");
	confirmButton.getStyle().set("padding", "0px");
	confirmButton.getStyle().set("padding-left", "5px");
	confirmButton.getStyle().set("padding-right", "5px");
	confirmButton.getStyle().set("background-color", "#8080804d");

	cancelButton = new Button("Cancel", e -> {

	    if (this.onCancelListener != null) {

		this.onCancelListener.onComponentEvent(e);
	    }

	    if (closeOnCancel) {
		close();
	    }
	});

	cancelButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
	cancelButton.getStyle().set("margin-left", "5px");
	cancelButton.getStyle().set("border", "1px solid #80808061");
	cancelButton.getStyle().set("padding", "0px");
	cancelButton.getStyle().set("padding-left", "5px");
	cancelButton.getStyle().set("padding-right", "5px");
	cancelButton.getStyle().set("background-color", "#8080804d");

	HorizontalLayout buttonsLayout = ComponentFactory.createNoSpacingNoMarginHorizontalLayout();
	buttonsLayout.getStyle().set("padding", "5px");
	buttonsLayout.getStyle().set("width", "auto");
	buttonsLayout.getStyle().set("margin", "auto");

	buttonsLayout.add(confirmButton, cancelButton);

	setFooter(buttonsLayout);
    }

    /**
     * @param closeOnConfirm
     */
    public void setCloseOnConfirm(boolean closeOnConfirm) {

	this.closeOnConfirm = closeOnConfirm;
    }

    /**
     * @param closeOnCancel
     */
    public void setCloseOnCancel(boolean closeOnCancel) {

	this.closeOnCancel = closeOnCancel;
    }

    /**
     * @param onConfirmListener
     */
    public void setOnConfirmListener(ButtonChangeListener onConfirmListener) {

	this.onConfirmListener = onConfirmListener;
    }

    /**
     * @param onCancelListener
     */
    public void setOnCancelListener(ButtonChangeListener onCancelListener) {

	this.onCancelListener = onCancelListener;
    }

    /**
     * @param confirmText
     */
    public void setConfirmText(String confirmText) {

	this.confirmButton.setText(confirmText);
    }

    /**
     * @param cancelText
     */
    public void setCancelText(String cancelText) {

	this.cancelButton.setText(cancelText);
    }

    /**
     * @return
     */
    public Button getConfirmButton() {
	return confirmButton;
    }

    /**
     * @param confirmButton
     */
    public void setConfirmButton(Button confirmButton) {
	this.confirmButton = confirmButton;
    }

    /**
     * @return
     */
    public Button getCancelButton() {
	return cancelButton;
    }

    /**
     * @param cancelButton
     */
    public void setCancelButton(Button cancelButton) {
	this.cancelButton = cancelButton;
    }

}
