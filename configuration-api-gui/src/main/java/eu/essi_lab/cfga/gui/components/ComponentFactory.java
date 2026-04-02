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

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.accordion.*;
import com.vaadin.flow.component.button.*;
import com.vaadin.flow.component.details.*;
import com.vaadin.flow.component.details.Details.*;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.*;
import eu.essi_lab.cfga.gui.components.listener.*;
import eu.essi_lab.cfga.gui.components.tabs.*;
import eu.essi_lab.cfga.gui.directive.*;

import java.util.function.*;

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
    public static Accordion createAccordion(Component content, String expand, String collapse) {

	Accordion accordion = new Accordion();

	// accordion.getElement().getStyle().set("margin-left", "13px");
	accordion.getElement().getStyle().set("margin-top", "0px");
	accordion.getElement().getStyle().set("width", "100%");

	AccordionPanel accordionPanel = new AccordionPanel();
	accordionPanel.addThemeVariants(DetailsVariant.REVERSE, DetailsVariant.FILLED, DetailsVariant.SMALL);
	accordionPanel.setOpened(false);
	accordionPanel.setSummaryText(expand);
	accordionPanel.removeAll();
	accordionPanel.add(content);
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
    public static Switch createSwitch(boolean value, boolean enabled) {

	return createSwitch(Switch.Size.NORMAL, value, enabled, null);
    }

    /**
     * @param value
     * @param enabled
     * @param listener
     * @return
     */
    public static Switch createSwitch(boolean value, boolean enabled, AbstractValueChangeListener listener) {

	return createSwitch(Switch.Size.NORMAL, value, enabled, listener);
    }

    /**
     * @param size
     * @param value
     * @param enabled
     * @param listener
     * @return
     */
    public static Switch createSwitch(Switch.Size size, boolean value, boolean enabled, AbstractValueChangeListener listener) {

	Switch switch_ = new Switch(size);

	if (listener != null) {
	    switch_.addValueChangeListener(listener);
	}

	switch_.getStyle().set("padding-bottom", "3px");

	switch_.setValue(value);

	switch_.setEnabled(enabled);

	return switch_;
    }

    /**
     * @return
     */
    public static CustomButton createReloadButton() {

	CustomButton reloadButton = new CustomButton("RELOAD");
	reloadButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
	reloadButton.setId("reloadButton");
	reloadButton.setWidth(110, Unit.PIXELS);
	reloadButton.getStyle().set("color", "var(--_lumo-button-primary-background)");
	reloadButton.getStyle().set("background-color", "hsl(214deg 2.39% 36.14% / 14%)");
	//	reloadButton.getStyle().set("margin-left", "15px");
	//	reloadButton.getStyle().set("border", "1px solid hsl(0deg 0% 81%)");
	//	reloadButton.getStyle().set("border-radius", "0px");

	return reloadButton;
    }

    /**
     * @param text
     * @param addDirective
     * @return
     */
    public static Button createCustomAddDirectiveButton(CustomAddDirective addDirective) {

	ConfigurationViewButton button = new ConfigurationViewButton(addDirective.getName());
	button.setTooltip(addDirective.getDescription().orElse("Add setting"));
	button.setWidth(110, Unit.PIXELS);
	button.addThemeVariants(ButtonVariant.LUMO_SMALL);
	button.getStyle().set("margin-left", "3px");

	button.addEnabledStyle("color", "white");
	button.addEnabledStyle("background-color", "var(--_lumo-button-primary-background)");
	button.addEnabledStyle("border", "none");

	button.addClickListener(addDirective.getListener());

	EnabledGroupManager.getInstance().add(button);

	return button;
    }

    /**
     * @return
     */
    public static CopyToClipboardButton createCopyToClipboardButton() {

	return new CopyToClipboardButton();
    }

    /**
     * @param supplier
     * @return
     */
    public static CopyToClipboardButton createCopyToClipboardButton(Supplier<String> supplier) {

	return new CopyToClipboardButton(supplier);
    }

    /**
     * @param label
     * @param widthFull
     * @param fontSize
     * @return
     */
    public static Span createSpan(String text, boolean widthFull, int fontSize) {

	Span span = new Span(text);
	if (widthFull) {
	    span.setWidthFull();
	}

	if (fontSize > 0) {
	    span.getStyle().set("font-size", fontSize + "px");
	}

	return span;
    }

    /**
     * @param label
     * @return
     */
    public static Span createSpan() {

	return createSpan("", -1);
    }

    /**
     * @param text
     * @return
     */
    public static Span createSpan(String text) {

	return createSpan(text, -1);
    }

    /**
     * @param label
     * @param sizePx
     * @return
     */
    public static Span createSpan(String label, int sizePx) {

	return createSpan(label, true, sizePx);
    }

    /**
     * @return
     */
    public static Div createDiv() {

	Div div = new Div();
	div.setWidthFull();

	return div;
    }

    /**
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
