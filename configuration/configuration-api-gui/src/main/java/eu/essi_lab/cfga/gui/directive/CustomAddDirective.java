package eu.essi_lab.cfga.gui.directive;

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

/**
 * @author Fabrizio
 */
public class CustomAddDirective extends Directive {

    /**
     *
     */
    private ComponentEventListener<ClickEvent<Button>> listener;

    /**
     *
     */
    public CustomAddDirective() {

	super("ADD");
    }

    /**
     *
     * @param listener
     */
    public CustomAddDirective(ComponentEventListener<ClickEvent<Button>> listener) {

	super("ADD");

	this.listener = listener;
    }

    /**
     * @param name
     * @param settingClass
     */
    public CustomAddDirective(String name, ComponentEventListener<ClickEvent<Button>> listener) {

	super(name);

	this.listener = listener;
    }

    /**
     * @return
     */
    public ComponentEventListener<ClickEvent<Button>> getListener() {

	return listener;
    }

    /**
     * @param listener
     */
    public void setListener(ComponentEventListener<ClickEvent<Button>> listener) {

	this.listener = listener;
    }
}
