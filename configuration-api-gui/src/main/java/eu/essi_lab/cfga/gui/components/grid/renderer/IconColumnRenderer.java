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

import com.vaadin.flow.component.icon.Icon;

/**
 * @author Fabrizio
 */
public abstract class IconColumnRenderer extends GridColumnRenderer<Icon> {

    /**
     * 
     */
    private static final long serialVersionUID = -6824495335399746205L;

    /**
     * 
     */
    public IconColumnRenderer() {

    }

    /**
     * @param consumer
     */
    public IconColumnRenderer(Consumer<HashMap<String, String>> consumer) {

	super(consumer);
    }

    @Override
    public Icon createComponent(HashMap<String, String> item) {

	component = createIcon(item);
	component.setId(item.get("identifier"));

	if (consumer != null) {
	    component.addClickListener(event -> {

		consumer.accept(item);
	    });
	}

	return component;
    }

    /**
     * @param item
     * @return
     */
    protected abstract Icon createIcon(HashMap<String, String> item);

}
