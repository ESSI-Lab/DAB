/**
 * 
 */
package eu.essi_lab.cfga.gui.components.grid.renderer;

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

import java.io.Serial;
import java.util.HashMap;
import java.util.Optional;

import com.vaadin.flow.component.icon.Icon;

/**
 * @author Fabrizio
 */
public abstract class IconColumnRenderer extends GridColumnRenderer<Icon> {

    /**
     * 
     */
    @Serial
    private static final long serialVersionUID = -6824495335399746205L;

    /**
     * 
     */
    public IconColumnRenderer() {
    }

    @Override
    public Icon createComponent(HashMap<String, String> item) {

	Icon component = createIcon(item);
	component.setId(item.get("identifier"));

	getToolTip(item).ifPresent(component::setTooltipText);

	return component;
    }

    /**
     * @return
     */
    public static Icon getEmptyIcon() {

	Icon icon = new Icon();
	icon.setSize("0px");

	return icon;
    }

    /**
     * @param item
     * @return
     */
    protected abstract Icon createIcon(HashMap<String, String> item);

    /**
     * @param item
     * @return
     */
    protected Optional<String> getToolTip(HashMap<String, String> item) {

	return Optional.empty();
    }
}
