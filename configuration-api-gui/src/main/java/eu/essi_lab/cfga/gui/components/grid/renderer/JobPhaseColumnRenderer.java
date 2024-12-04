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
import java.util.Optional;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import eu.essi_lab.cfga.gui.components.ComponentFactory;
import eu.essi_lab.messages.JobStatus.JobPhase;

/**
 * @author Fabrizio
 */
public class JobPhaseColumnRenderer extends IconColumnRenderer {

    private static final long serialVersionUID = -5355241303442699327L;

    /**
     * 
     */
    public JobPhaseColumnRenderer() {

    }

    /**
     * @return
     */
    public Optional<Component> getLegend() {
    
        HorizontalLayout layout = ComponentFactory.createNoSpacingNoMarginHorizontalLayout();
        layout.getStyle().set("padding-top", "3px");
        layout.getStyle().set("padding-bottom", "3px");
        layout.getStyle().set("padding-left", "3px");
        layout.getStyle().set("padding-right", "15px");
    
        Label legendLabel = ComponentFactory.createLabel("Status", false, 12);
        legendLabel.getStyle().set("font-weight", "bold");
        legendLabel.getStyle().set("margin-right", "-10px");
        legendLabel.getStyle().set("margin-top", "-2px");
    
        layout.add(legendLabel);
    
        layout.add(crateLegendPart(JobPhase.RUNNING_LABEL, createIcon(JobPhase.RUNNING_LABEL, true, 3), 50, 5));
    
        layout.add(crateLegendPart(JobPhase.CANCELED_LABEL, createIcon(JobPhase.CANCELED_LABEL, true, 5), 50, -8));
    
        layout.add(crateLegendPart(JobPhase.COMPLETED_LABEL, createIcon(JobPhase.COMPLETED_LABEL, true, 10), 55, -5));
    
        layout.add(crateLegendPart(JobPhase.ERROR_LABEL, createIcon(JobPhase.ERROR_LABEL, true, -5), 30, 0));
    
        layout.add(crateLegendPart(JobPhase.RESCHEDULED_LABEL, createIcon(JobPhase.RESCHEDULED_LABEL, true, 10), 60, -5));
    
        return Optional.of(layout);
    }

    @Override
    protected Icon createIcon(HashMap<String, String> item) {
    
        String status = item.get("Status");
        return createIcon(status);
    }

    @Override
    protected Optional<String> getToolTip(HashMap<String, String> item) {
    
        String status = item.get("Status");
        switch (status) {
    
        case JobPhase.RUNNING_LABEL:
        case JobPhase.CANCELED_LABEL:
        case JobPhase.COMPLETED_LABEL:
        case JobPhase.ERROR_LABEL:
        case JobPhase.RESCHEDULED_LABEL:
    
            return Optional.of(status);
        }
    
        return Optional.empty();
    }

    /**
     * @param name
     * @param icon
     * @return
     */
    private VerticalLayout crateLegendPart(String name, Icon icon, int width, int marginLeft) {

	VerticalLayout vl = ComponentFactory.createNoSpacingNoMarginVerticalLayout();
	vl.getStyle().set("width", String.valueOf(width) + "px");
	vl.getStyle().set("margin-left", marginLeft + "px");
	vl.add(ComponentFactory.createLabel(name, false, 10));
	vl.add(icon);

	return vl;
    }

    /**
     * @param status
     * @return
     */
    private Icon createIcon(String status, boolean fixedSize, Integer marginLeft) {

	Icon icon = null;

	switch (status) {

	case JobPhase.RUNNING_LABEL:

	    icon = VaadinIcon.PLAY_CIRCLE_O.create();

	    break;

	case JobPhase.CANCELED_LABEL:

	    icon = VaadinIcon.CLOSE_CIRCLE_O.create();

	    break;

	case JobPhase.COMPLETED_LABEL:

	    icon = VaadinIcon.CHECK_SQUARE_O.create();

	    break;

	case JobPhase.ERROR_LABEL:

	    icon = VaadinIcon.WARNING.create();

	    break;

	case JobPhase.RESCHEDULED_LABEL:

	    icon = VaadinIcon.ALARM.create();

	    break;

	default:
	    return IconColumnRenderer.getEmptyIcon();
	}

	if (fixedSize) {

	    icon.getStyle().set("width", "30px !important");
	    icon.getStyle().set("height", "15px !important");
	}

	if (marginLeft != null) {

	    icon.getStyle().set("margin-left", String.valueOf(marginLeft) + "px");
	}

	return icon;
    }

    /**
     * @param status
     * @return
     */
    private Icon createIcon(String status) {

	return createIcon(status, false, null);
    }
}
