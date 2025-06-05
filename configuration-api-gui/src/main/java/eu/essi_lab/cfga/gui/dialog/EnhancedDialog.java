package eu.essi_lab.cfga.gui.dialog;

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

import java.util.ArrayList;
import java.util.List;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import eu.essi_lab.cfga.gui.components.ComponentFactory;

/**
 * @author Fabrizio
 */
@SuppressWarnings("serial")
public class EnhancedDialog extends Dialog {

    private VerticalLayout layout;
    private HorizontalLayout header;
    private VerticalLayout footer;
    private VerticalLayout content;
    private Button closeButton;
    private Label titleLabel;

    private ComponentEventListener<ClickEvent<Button>> onCloseListener;

    private static List<EnhancedDialog> dialogs = new ArrayList<EnhancedDialog>();

    /**
     * 
     */
    public EnhancedDialog() {

	this(null);
    }

    /**
     * @param onCloseListener
     */
    public EnhancedDialog(ComponentEventListener<ClickEvent<Button>> onCloseListener) {

	setModal(true);
	setCloseOnEsc(false);
	setCloseOnOutsideClick(false);
	getElement().getStyle().set("padding", "0px");
	setOnCloseButtonListener(onCloseListener);

	//
	//
	//

	layout = ComponentFactory.createNoSpacingNoMarginVerticalLayout();
	layout.setSizeFull();
	layout.getStyle().set("padding", "5px");
	layout.setId("mainLayout");

	add(layout);

	header = ComponentFactory.createNoSpacingNoMarginHorizontalLayout();
	header.setWidthFull();
	header.setHeight("40px");
	header.getStyle().set("padding", "0px");
	header.getStyle().set("padding", "5px");
	header.getStyle().set("margin-top", "5px");
	header.getStyle().set("background-color", "lightgray");
	header.getStyle().set("border-radius", "3px");
	header.setId("header");

	layout.add(header);

	// ----

	content = ComponentFactory.createNoSpacingNoMarginVerticalLayout();
	content.setSizeFull();
	content.getStyle().set("padding", "5px");
	content.getStyle().set("margin-top", "10px");
	content.getStyle().set("margin-bottom", "10px");
	content.setId("content");
		
	layout.add(content);

	// ----

	footer = ComponentFactory.createNoSpacingNoMarginVerticalLayout();
	footer.setSizeFull();
	footer.getStyle().set("padding", "0px");
	footer.setId("footer");
	
	layout.add(footer);

	//
	//
	//

	titleLabel = ComponentFactory.createLabel();
	titleLabel.getStyle().set("font-weight", "bold");

	header.add(titleLabel);

	closeButton = new Button(new Icon(VaadinIcon.CLOSE_SMALL), e -> {

	    if (this.onCloseListener != null) {

		this.onCloseListener.onComponentEvent(e);
	    }

	    close();
	});

	closeButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
	closeButton.getStyle().set("width", "10px");
	closeButton.getStyle().set("height", "25px");
	closeButton.getStyle().set("background", "none");

	header.add(closeButton);

	//
	// draggable by default
	//
	setDraggable(true);
    }

    /**
    * 
    */
    public void setDraggable(boolean draggable) {

	super.setDraggable(draggable);

	if (draggable) {

	    footer.addClassName("draggable");
	    header.addClassName("draggable");

	} else {

	    footer.removeClassName("draggable");
	    header.removeClassName("draggable");
	}
    }

    /**
     * @param onCloseListener
     */
    public void setOnCloseButtonListener(ComponentEventListener<ClickEvent<Button>> onCloseListener) {

	this.onCloseListener = onCloseListener;
    }

    /**
     * @param header
     */
    public void setHeader(Component header) {

	this.header.removeAll();

	this.header.add(header);
    }

    /**
     * @param text
     */
    public void setTitle(String text) {

	titleLabel.setText(text);
    }

    /**
     * @param content
     */
    public void setContent(Component content) {

	this.content.removeAll();

	this.content.add(content);
    }

    /**
     * @param footer
     */
    public void setFooter(Component footer) {

	this.footer.removeAll();

	this.footer.add(footer);
    }

    /**
     * @return
     */
    public VerticalLayout getContentLayout() {

	return content;
    }

    /**
     * @return
     */
    public HorizontalLayout getHeaderLayout() {

	return header;
    }

    /**
     * @return
     */
    public VerticalLayout getFooterLayout() {

	return footer;
    }
    
    /**
     * @return
     */
    public VerticalLayout getMainLayout() {

	return layout;
    }

    /**
     * @return
     */
    public Label getTitleLabel() {

	return titleLabel;
    }

    /**
     * @return
     */
    public Button getCloseButton() {

	return closeButton;
    }

    /**
     * 
     */
    public void addToCloseAll() {
    
        dialogs.add(this);
    }
    
    /**
     * 
     */
    public void removeFromCloseAll() {
    
        dialogs.remove(this);
    }

    /**
     * 
     */
    public static void closeAll() {

	dialogs.forEach(d -> d.close());
    }
}
