package eu.essi_lab.cfga.gui.dialog;

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
import com.vaadin.flow.component.button.*;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.*;
import eu.essi_lab.cfga.gui.components.*;
import eu.essi_lab.cfga.gui.components.listener.*;

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

	getMainLayout().getStyle().set("padding", "0px");

	getContentLayout().getStyle().set("padding", "15px");
	getHeaderLayout().getStyle().set("padding", "15px");
	getContentLayout().getStyle().set("padding-left", "20px");
	getHeaderLayout().getStyle().set("padding-left", "20px");

	getHeaderLayout().getStyle().set("background-color", "white");

	setWidth(500, Unit.PIXELS);

	setCloseOnConfirm(true);
	setCloseOnCancel(true);

	//
	// Header
	//

	setHeader("Confirmation");

	//
	// Content
	//

	if (text != null) {

	    Div div = ComponentFactory.createDiv();

	    text = text.replace("\n", "<br>");

	    div.getElement().setProperty("innerHTML", text);

	    setContent(div);
	}

	//
	// Footer
	//

	cancelButton = new Button("Cancel", e -> {

	    if (this.onCancelListener != null) {

		this.onCancelListener.onComponentEvent(e);
	    }

	    if (closeOnCancel) {
		close();
	    }
	});

	cancelButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

	confirmButton = new Button("Confirm", e -> {

	    if (this.onConfirmListener != null) {

		this.onConfirmListener.onComponentEvent(e);
	    }

	    if (closeOnConfirm) {
		close();
	    }
	});

	confirmButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

	HorizontalLayout buttonsLayout = ComponentFactory.createNoSpacingNoMarginHorizontalLayout();
	buttonsLayout.setWidthFull();
	buttonsLayout.getStyle().set("background-color", "#f3f5f7");
	buttonsLayout.getStyle().set("padding", "15px");

	Div div = ComponentFactory.createDiv();
	div.setWidthFull();

	buttonsLayout.add(cancelButton, div, confirmButton);

	setFooter(buttonsLayout);
    }

    /**
     * @param text
     */
    public void setHeader(String text) {

	setHeader(new H3(text));
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
