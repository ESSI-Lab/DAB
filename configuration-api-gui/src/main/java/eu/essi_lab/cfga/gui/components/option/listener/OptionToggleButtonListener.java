package eu.essi_lab.cfga.gui.components.option.listener;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2024 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import com.vaadin.flow.component.HasValue.ValueChangeEvent;

import eu.essi_lab.cfga.gui.IdleTracker;
import eu.essi_lab.cfga.gui.components.listener.AbstractValueChangeListener;
import eu.essi_lab.cfga.gui.components.option.OptionComponent;

/**
 * @author Fabrizio
 */
@SuppressWarnings("serial")
public class OptionToggleButtonListener extends AbstractValueChangeListener {

    private OptionComponent optionComponent;

    /**
     * @param optionComponent
     */
    public OptionToggleButtonListener(OptionComponent optionComponent) {

	this.optionComponent = optionComponent;
    }

    @Override
    protected void handleEvent(ValueChangeEvent<?> event) {

	IdleTracker.getInstance().reset();

	boolean enabled = (Boolean) event.getValue();
	optionComponent.onToggleStateChanged(enabled);
    }
}
