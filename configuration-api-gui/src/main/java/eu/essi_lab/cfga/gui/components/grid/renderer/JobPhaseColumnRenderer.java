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

import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;

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

    @Override
    protected Icon createIcon(HashMap<String, String> item) {

	String status = item.get("Status");
	switch (status) {

	case JobPhase.RUNNING_LABEL:

	    return VaadinIcon.PLAY_CIRCLE_O.create();

	case JobPhase.CANCELED_LABEL:

	    return VaadinIcon.CLOSE_CIRCLE_O.create();

	case JobPhase.COMPLETED_LABEL:

	    return VaadinIcon.CHECK_SQUARE_O.create();

	case JobPhase.ERROR_LABEL:

	    return VaadinIcon.WARNING.create();

	case JobPhase.RESCHEDULED_LABEL:

	    return VaadinIcon.ALARM.create();
	}

	return IconColumnRenderer.getEmptyIcon();
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
}