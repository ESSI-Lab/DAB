package eu.essi_lab.cfga.gui.extension.directive;

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

import java.util.Optional;

import com.vaadin.flow.data.provider.SortDirection;

/**
 * @author Fabrizio
 */
public class ShowDirective extends Directive {

    /**
     * 
     */
    private SortDirection sortDirection;

    public ShowDirective() {
    }

    /**
     * @param name
     * @param settingClass
     */
    public ShowDirective(String name) {

	super(name);
    }

    /**
     * @param name
     * @param confirmationPolicy
     */
    public ShowDirective(String name, SortDirection direction) {

	super(name);

	setSortDirection(direction);
    }

    /**
     * @return the sortDirection
     */
    public Optional<SortDirection> getSortDirection() {

	return Optional.ofNullable(sortDirection);

    }

    /**
     * @param sortDirection
     */
    public void setSortDirection(SortDirection sortDirection) {

	this.sortDirection = sortDirection;
    }
}
