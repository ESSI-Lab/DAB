/**
 * 
 */
package eu.essi_lab.cfga.gui.components.grid;

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

import java.util.ArrayList;
import java.util.List;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import eu.essi_lab.cfga.gui.components.ComponentFactory;
import eu.essi_lab.lib.utils.StringUtils;

/**
 * @author Fabrizio
 */
public class LegendBuilder {

    private final List<VerticalLayout> legendParts;

    /**
     * 
     */
    private LegendBuilder() {

	legendParts = new ArrayList<>();
    }

    /**
     * @return
     */
    public static LegendBuilder get() {

	return new LegendBuilder();
    }

    /**
     * @param label
     * @param icon
     * @param partWidth
     * @param iconMarginLeft
     * @param partMarginLeft
     * @return
     */
    public LegendBuilder addLegendPart(String label, Icon icon, int partWidth) {

	return addLegendPart(label, icon, 0, partWidth, 0);
    }

    /**
     * @param label
     * @param icon
     * @param partWidth
     * @param partMarginLeft
     * @return
     */
    public LegendBuilder addLegendPart(String label, Icon icon, int partWidth, int partMarginLeft) {

	return addLegendPart(label, icon, 0, partWidth, partMarginLeft);
    }

    /**
     * @param label
     * @param icon
     * @param iconMarginLeft
     * @param partWidth
     * @param partMarginLeft
     * @return
     */
    public LegendBuilder addLegendPart(String label, Icon icon, int iconMarginLeft, int partWidth, int partMarginLeft) {

	icon.getStyle().set("width", "30px !important");
	icon.getStyle().set("height", "15px !important");

	if (iconMarginLeft != 0) {
	    icon.getStyle().set("margin-left", iconMarginLeft + "px");
	}

	VerticalLayout part = crateLegendPart(label, icon, partWidth, partMarginLeft);

	legendParts.add(part);

	return this;
    }

    /**
     * @return
     */
    public Component build(String legendName) {

	HorizontalLayout layout = ComponentFactory.createNoSpacingNoMarginHorizontalLayout();

	layout.setId("grid-component-legend-" + StringUtils.encodeUTF8(legendName));

	layout.getStyle().set("padding-top", "3px");
	layout.getStyle().set("padding-bottom", "3px");
	layout.getStyle().set("padding-left", "3px");
	layout.getStyle().set("padding-right", "15px");

	Label legendLabel = ComponentFactory.createLabel(legendName, false, 12);
	legendLabel.getStyle().set("font-weight", "bold");
	legendLabel.getStyle().set("margin-right", "5px");
	legendLabel.getStyle().set("margin-top", "-2px");

	layout.add(legendLabel);

	legendParts.forEach(layout::add);

	return layout;
    }

    /**
     * @param name
     * @param icon
     * @return
     */
    private VerticalLayout crateLegendPart(String name, Icon icon, int width, int marginLeft) {

	VerticalLayout vl = ComponentFactory.createNoSpacingNoMarginVerticalLayout();
	vl.getStyle().set("padding","0px");

	vl.getStyle().set("width", width + "px");
	if (marginLeft != 0) {
	    vl.getStyle().set("margin-left", marginLeft + "px");
	}
	vl.add(ComponentFactory.createLabel(name, false, 10));
	vl.add(icon);

	return vl;
    }
}
