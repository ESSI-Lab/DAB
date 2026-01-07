package eu.essi_lab.cfga.gui.components;

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

import com.vaadin.componentfactory.ToggleButton;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.accordion.AccordionPanel;
import com.vaadin.flow.component.button.*;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.details.Details.OpenedChangeEvent;
import com.vaadin.flow.component.details.DetailsVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.*;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import eu.essi_lab.cfga.gui.components.tabs.*;

/**
 * @author Fabrizio
 */
public class ComponentFactory {

    /**
     *
     */
    public static final int MIN_HEIGHT_OFFSET = 330;

    /**
     * @param id
     * @return
     */
    public static VerticalLayout createVerticalLayout(String id) {

	VerticalLayout layout = new VerticalLayout();
	// layout.getStyle().set("border","1px solid black");
	if (id != null) {
	    layout.setId(id);
	}

	return layout;
    }



    /**
     * @return
     */
    public static VerticalLayout createVerticalLayout() {

	return createVerticalLayout(null);
    }

    /**
     * @return
     */
    public static HorizontalLayout createHorizontalLayout() {

	return createHorizontalLayout(null);
    }

    /**
     * @param id
     * @return
     */
    public static HorizontalLayout createHorizontalLayout(String id) {

	HorizontalLayout layout = new HorizontalLayout();
	if (id != null) {
	    layout.setId(id);
	}

	return layout;
    }

    /**
     * @return
     */
    public static VerticalLayout createNoSpacingNoMarginVerticalLayout() {

	return createNoSpacingNoMarginVerticalLayout(null);
    }

    /**
     * @param id
     * @return
     */
    public static VerticalLayout createNoSpacingNoMarginVerticalLayout(String id) {

	VerticalLayout layout = createVerticalLayout(id);

	layout.setMargin(false);
	layout.setSpacing(false);

	return layout;
    }

    /**
     * @param id
     * @return
     */
    public static TabContent createTabContent(String id) {

	TabContent content = new TabContent();
	content.setId(id);

	return content;
    }

    /**
     * @return
     */
    public static HorizontalLayout createNoSpacingNoMarginHorizontalLayout() {

	return createNoSpacingNoMarginHorizontalLayout(null);
    }

    /**
     * @param id
     * @return
     */
    public static HorizontalLayout createNoSpacingNoMarginHorizontalLayout(String id) {

	HorizontalLayout layout = createHorizontalLayout(id);

	layout.setMargin(false);
	layout.setSpacing(false);

	return layout;
    }

    /**
     * @param content
     * @return
     */
    @SuppressWarnings("serial")
    public static Accordion createAccordion(Component content, String expand, String collapse) {

	Accordion accordion = new Accordion();

	// accordion.getElement().getStyle().set("margin-left", "13px");
	accordion.getElement().getStyle().set("margin-top", "0px");
	accordion.getElement().getStyle().set("width", "100%");

	AccordionPanel accordionPanel = new AccordionPanel();
	accordionPanel.addThemeVariants(DetailsVariant.REVERSE, DetailsVariant.FILLED, DetailsVariant.SMALL);
	accordionPanel.setOpened(false);
	accordionPanel.setSummaryText(expand);
	accordionPanel.setContent(content);
	accordionPanel.addOpenedChangeListener((ComponentEventListener<OpenedChangeEvent>) event -> {

	    boolean opened = event.isOpened();
	    if (opened) {
		accordionPanel.setSummaryText(collapse);
	    } else {
		accordionPanel.setSummaryText(expand);
	    }
	});

	accordion.close();

	accordion.add(accordionPanel);

	return accordion;
    }

    /**
     * @param content
     * @return
     */
    @SuppressWarnings("serial")
    public static Details createDetails(Component component, String expand, String collapse) {

	Details details = new Details(expand, component);

	details.getElement().getStyle().set("margin-top", "0px");
	details.getElement().getStyle().set("width", "100%");

	details.addOpenedChangeListener((ComponentEventListener<OpenedChangeEvent>) event -> {

	    boolean opened = event.isOpened();
	    if (opened) {
		details.setSummaryText(collapse);
	    } else {
		details.setSummaryText(expand);
	    }
	});

	details.addThemeVariants(DetailsVariant.REVERSE, DetailsVariant.FILLED);

	return details;
    }

    /**
     * @param content
     * @return
     */
    public static Details createDetails(String summary, Component component) {

	Details details = new Details(summary, component);

	details.setId("details-for-setting-" + summary);

	details.getElement().getStyle().set("margin-top", "0px");
	details.getElement().getStyle().set("width", "100%");

	details.addThemeVariants(DetailsVariant.REVERSE, DetailsVariant.FILLED);

	return details;
    }

    /**
     * @param value
     * @param enabled
     * @return
     */
    public static ToggleButton createToggleButton(boolean value, boolean enabled) {

	ToggleButton toggle = new ToggleButton();

	toggle.getStyle().set("align-items", "baseline");
	toggle.getStyle().set("padding-bottom", "3px");

	toggle.setValue(value);

	toggle.setEnabled(enabled);

	return toggle;
    }

    /**
     *
     * @return
     */
    public static CustomButton createReloadButton(){

	CustomButton reloadButton = new CustomButton("RELOAD", VaadinIcon.REFRESH.create());
	reloadButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
	reloadButton.setId("reloadButton");
	reloadButton.setWidth(150, Unit.PIXELS);
	reloadButton.getStyle().set("margin-left", "15px");
	reloadButton.getStyle().set("border", "1px solid hsl(0deg 0% 81%)");
	reloadButton.getStyle().set("border-radius", "0px");

	return reloadButton;
    }

    /**
     * @param label
     * @param widthFull
     * @param fontSize
     * @return
     */
    public static Label createLabel(String label, boolean widthFull, int fontSize) {

	Label out = new Label(label);
	if (widthFull) {
	    out.setWidthFull();
	}

	if (fontSize > 0) {
	    out.getStyle().set("font-size", fontSize + "px");
	}

	return out;
    }

    /**
     * @param label
     * @return
     */
    public static Label createLabel() {

	return createLabel("", -1);
    }

    /**
     * @param label
     * @return
     */
    public static Label createLabel(String label) {

	return createLabel(label, -1);
    }

    /**
     * @param label
     * @param sizePx
     * @return
     */
    public static Label createLabel(String label, int sizePx) {

	return createLabel(label, true, sizePx);
    }

    /**
     * @return
     */
    public static Div createDiv() {

	return new Div();
    }

    /**
     *
     * @return
     */
    public static Div createSeparator() {

	return createSeparator("#e8ebef");
    }

    /**
     * @param color
     * @return
     */
    public static Div createSeparator(String color) {

	Div separator = new Div();
	separator.setWidthFull();
	separator.setHeight("1px");
	separator.getStyle().set("background-color", color);

	return separator;
    }

    /**
     * @param defaultColor
     * @return
     */
    public static Hr createHr(boolean defaultColor) {

	Hr hr = new Hr();
	if (!defaultColor) {
	    hr.getStyle().set("background", "lightgray");
	}

	return hr;
    }

    /**
     * @param color
     * @return
     */
    public static Hr createHr(String color) {

	Hr hr = new Hr();
	hr.getStyle().set("background", color);

	return hr;
    }

    /**
     * @return
     */
    public static Hr createHr() {

	return createHr(false);
    }
}
