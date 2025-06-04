package eu.essi_lab.cfga.gui.components.option;

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
import java.util.Optional;

import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

/**
 * @author Fabrizio
 */
@SuppressWarnings("serial")
public class OptionComponentLayout extends VerticalLayout {

    private List<OptionComponent> components;
    private Details details;

    /**
     * 
     */
    public OptionComponentLayout(String id) {

	setId(id);
	setMargin(false);
	setSpacing(false);

	components = new ArrayList<OptionComponent>();
    }

    /**
     * @param details
     */
    public void setCompactModeDetails(Details details) {

	this.details = details;
    }

    /**
     * @return
     */
    public Optional<Details> getDetails() {

	return Optional.ofNullable(details);
    }

    /**
     * @return
     */
    public List<OptionComponent> getOptionComponents() {

	return components;
    }
}
