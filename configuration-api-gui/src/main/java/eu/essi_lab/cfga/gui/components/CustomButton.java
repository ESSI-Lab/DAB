package eu.essi_lab.cfga.gui.components;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
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

import java.util.HashMap;
import java.util.Map;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;

/**
 * @author Fabrizio
 */
@SuppressWarnings("serial")
public class CustomButton extends Button {

    private Map<String, String> enabledStyleMap;
    private Map<String, String> disabledStyleMap;

    /**
     * 
     */
    public CustomButton() {
    }

    /**
     * @param text
     */
    public CustomButton(String text) {
	super(text);
    }

    /**
     * @param icon
     */
    public CustomButton(Component icon) {
	super(icon);
    }

    /**
     * @param text
     * @param icon
     */
    public CustomButton(String text, Component icon) {
	super(text, icon);
    }

    /**
     * @param text
     * @param clickListener
     */
    public CustomButton(String text, ComponentEventListener<ClickEvent<Button>> clickListener) {
	super(text, clickListener);
    }

    /**
     * @param icon
     * @param clickListener
     */
    public CustomButton(Component icon, ComponentEventListener<ClickEvent<Button>> clickListener) {
	super(icon, clickListener);

    }

    /**
     * @param text
     * @param icon
     * @param clickListener
     */
    public CustomButton(String text, Component icon, ComponentEventListener<ClickEvent<Button>> clickListener) {

	super(text, icon, clickListener);
    }

    @Override
    public void setEnabled(boolean enabled) {

	super.setEnabled(enabled);

	if (enabled) {

	    getEnabledStyleMap().keySet().forEach(property -> getStyle().set(property, getEnabledStyleMap().get(property)));

	} else {

	    getDisabledStyleMap().keySet().forEach(property -> getStyle().set(property, getDisabledStyleMap().get(property)));
	}
    }

    /**
     * @param property
     * @param value
     */
    public void addEnabledStyle(String property, String value) {

	getEnabledStyleMap().put(property, value);

	if (isEnabled()) {

	    getStyle().set(property, value);
	}
    }

    /**
     * @param property
     * @param value
     */
    public void addDisabledStyle(String property, String value) {

	getDisabledStyleMap().put(property, value);

	if (!isEnabled()) {

	    getStyle().set(property, value);
	}
    }

    /**
     * @param tooltip
     */
    public void setTooltip(String tooltip) {

	getElement().setAttribute("title", tooltip);
    }

    /**
     * @return
     */
    private Map<String, String> getEnabledStyleMap() {

	if (enabledStyleMap == null) {
	    enabledStyleMap = new HashMap<>();
	}

	return enabledStyleMap;
    }

    /**
     * @return
     */
    private Map<String, String> getDisabledStyleMap() {

	if (disabledStyleMap == null) {
	    disabledStyleMap = new HashMap<>();
	}

	return disabledStyleMap;
    }
}
