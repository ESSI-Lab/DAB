/**
 * 
 */
package eu.essi_lab.cfga.gui.components.grid.renderer;

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

import java.util.HashMap;
import java.util.function.Consumer;

import com.vaadin.flow.component.checkbox.Checkbox;

/**
 * Not used
 * 
 * @deprecated
 * @author Fabrizio
 */
public class CheckBoxColumnRenderer extends GridColumnRenderer<Checkbox> {

    /**
     * 
     */
    private static final long serialVersionUID = 233009253707393876L;
    private Consumer<HashMap<String, String>> consumer;

    /**
    * 
    */
    public CheckBoxColumnRenderer() {

    }

    /**
     * @param consumer
     */
    public CheckBoxColumnRenderer(Consumer<HashMap<String, String>> consumer) {

	this.consumer = consumer;
    }

    @Override
    public Checkbox createComponent(HashMap<String, String> item) {

	Checkbox component = new Checkbox();
	component.setId(item.get("identifier"));

	if (consumer != null) {
	    component.addClickListener(event -> {

		consumer.accept(item);
	    });
	}

	return component;
    }
}
