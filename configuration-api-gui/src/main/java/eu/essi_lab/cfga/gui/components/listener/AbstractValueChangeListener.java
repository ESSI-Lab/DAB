package eu.essi_lab.cfga.gui.components.listener;

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

import com.vaadin.flow.component.HasValue.ValueChangeEvent;
import com.vaadin.flow.component.HasValue.ValueChangeListener;

import eu.essi_lab.cfga.gui.IdleTracker;

@SuppressWarnings("serial")
public abstract class AbstractValueChangeListener implements ValueChangeListener<ValueChangeEvent<?>> {

    protected boolean discardServerSideEvents;

    /**
     * 
     */
    public AbstractValueChangeListener() {

	discardServerSideEvents = true;
    }

    @Override
    public final void valueChanged(ValueChangeEvent<?> event) {

	if (discardServerSideEvents && !event.isFromClient()) {

	    return;
	}

	IdleTracker.getInstance().reset();

	handleEvent(event);
    }

    /**
     * @param event
     */
    protected abstract void handleEvent(ValueChangeEvent<?> event);
}
