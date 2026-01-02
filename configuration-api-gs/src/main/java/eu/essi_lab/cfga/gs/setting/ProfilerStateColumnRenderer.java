/**
 * 
 */
package eu.essi_lab.cfga.gs.setting;

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

import java.util.HashMap;
import java.util.Optional;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;

import eu.essi_lab.cfga.gui.components.grid.LegendBuilder;
import eu.essi_lab.cfga.gui.components.grid.renderer.IconColumnRenderer;

/**
 * @author Fabrizio
 */
@SuppressWarnings("serial")
public class ProfilerStateColumnRenderer extends IconColumnRenderer {

    /**
     * @return
     */
    public Optional<Component> getLegend() {

	Component component = LegendBuilder.get().//
		addLegendPart("Online", createIcon("Online"), -1, 35, 0).//
		addLegendPart("Offline", createIcon("Offline"), -1, 35, 0).//
		build("State");

	return Optional.of(component);
    }

    /**
     * 
     */
    @Override
    protected Icon createIcon(HashMap<String, String> item) {

	return createIcon(item.get("State"));
    }

    @Override
    protected Optional<String> getToolTip(HashMap<String, String> item) {

	String status = item.get("State");
	return switch (status) {
	case "Online", "Offline" -> Optional.of(status);
	default -> Optional.empty();
	};
    }

    /**
     * @param status
     * @return
     */
    private Icon createIcon(String status) {

	return status.equals("Online") ? VaadinIcon.SIGNAL.create() : VaadinIcon.BAN.create();
    }
}
