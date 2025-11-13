package eu.essi_lab.cfga.gui.extension;

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

import com.vaadin.flow.component.tabs.Tabs.Orientation;

import eu.essi_lab.cfga.setting.ObjectExtension;

/**
 * @author Fabrizio
 */
public class ComponentInfo implements ObjectExtension {

    private String componentName;
    private Orientation orientation;
    private TabDescriptor tabDescriptor;
    private boolean forceReadOnly;

    /**
     * 
     */
    public ComponentInfo() {

	setOrientation(Orientation.VERTICAL);
	setForceReadOnly(true);
    }

    /**
     * @return
     */
    public String getComponentName() {

	return componentName;
    }

    /**
     * @param componentName
     */
    public void setComponentName(String componentName) {

	this.componentName = componentName;
    }

    /**
     * @param forceReadOnly
     */
    public void setForceReadOnly(boolean forceReadOnly) {

	this.forceReadOnly = forceReadOnly;
    }

    /**
     * @return
     */
    public boolean isForceReadOnlySet() {

	return forceReadOnly;
    }

    /**
     * @return
     */
    public Orientation getOrientation() {

	return orientation;
    }

    /**
     * @param orientation
     */
    public void setOrientation(Orientation orientation) {

	this.orientation = orientation;
    }

    /**
     * @return
     */
    public Optional<TabDescriptor> getTabInfo() {

	return Optional.ofNullable(tabDescriptor);
    }

    /**
     * @param tabDescriptor
     */
    public void setTabInfo(TabDescriptor tabDescriptor) {

	this.tabDescriptor = tabDescriptor;
    }

    @Override
    public boolean equals(Object o) {

	if (o instanceof ComponentInfo other) {

	    return this.getComponentName().equals(other.getComponentName());
	}

	return false;
    }
}
