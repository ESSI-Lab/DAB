package eu.essi_lab.cfga.gui.components;

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

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;

/**
 * @author Fabrizio
 */
@SuppressWarnings("serial")
public class ConfigurationViewButton extends CustomButton {

    /**
     * 
     */
    public ConfigurationViewButton() {

	init();
    }

    /**
     * @param text
     */
    public ConfigurationViewButton(String text) {

	super(text);

	init();
    }

    /**
     * @param icon
     */
    public ConfigurationViewButton(Component icon) {

	super(icon);

	init();
    }

    /**
     * @param text
     * @param icon
     */
    public ConfigurationViewButton(String text, Component icon) {

	super(text, icon);

	init();
    }

    /**
     * @param text
     * @param clickListener
     */
    public ConfigurationViewButton(String text, ComponentEventListener<ClickEvent<Button>> clickListener) {

	super(text, clickListener);

	init();
    }

    /**
     * @param icon
     * @param clickListener
     */
    public ConfigurationViewButton(Component icon, ComponentEventListener<ClickEvent<Button>> clickListener) {

	super(icon, clickListener);

	init();
    }

    /**
     * @param text
     * @param icon
     * @param clickListener
     */
    public ConfigurationViewButton(String text, Component icon, ComponentEventListener<ClickEvent<Button>> clickListener) {

	super(text, icon, clickListener);

	init();
    }

    /**
     * 
     */
    protected void init() {

	addDisabledStyle("color", "lightgray");
	addDisabledStyle("background-color", "white");
	addDisabledStyle("border", "1px solid lightgray");
    }
}
