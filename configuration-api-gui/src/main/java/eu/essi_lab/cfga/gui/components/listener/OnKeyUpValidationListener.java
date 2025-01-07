package eu.essi_lab.cfga.gui.components.listener;

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

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Focusable;
import com.vaadin.flow.component.HasValidation;
import com.vaadin.flow.component.KeyUpEvent;

/**
 * This listener allows to check the input values server-side every time a keyboard button is released.<br>
 * Since, server-side, the text field value is updated only when the focus is lost, the only way to check the current
 * text field value, is to lose (blur) the focus when the key is pressed, and get it again (focus) when the check is
 * ended
 * 
 * @author Fabrizio
 * @param <T>
 */
@FunctionalInterface
public interface OnKeyUpValidationListener<T> extends ComponentEventListener<KeyUpEvent> {

    /**
     * @param value
     * @return
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public default void onComponentEvent(KeyUpEvent event) {

	((Focusable) event.getSource()).blur();

	T value = (T) ((AbstractField) event.getSource()).getValue();

	((HasValidation) event.getSource()).setInvalid(isInvalid(value));

	((Focusable) event.getSource()).focus();
    }

    /**
     * @param value
     * @return
     */
    public boolean isInvalid(T value);
}
