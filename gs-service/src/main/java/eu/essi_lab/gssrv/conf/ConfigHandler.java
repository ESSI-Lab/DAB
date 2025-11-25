package eu.essi_lab.gssrv.conf;

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

import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.tabs.*;
import eu.essi_lab.cfga.gs.*;
import eu.essi_lab.cfga.gui.extension.*;

/**
 * @author Fabrizio
 */
public class ConfigHandler extends ComponentInfo {

    private final TabDescriptor descriptor;

    /**
     *
     */
    public ConfigHandler() {

	setName("Configuration");

	Div mainLayout = new Div();
	mainLayout.setWidthFull();

	TabSheet tabSheet = new TabSheet();

	ConfigExporter configExporter = new ConfigExporter();
	ConfigImporter configImporter = new ConfigImporter();

	tabSheet.add("Import", configImporter.getMainLayout());

	tabSheet.add("Export", configExporter.getMainLayout());

	mainLayout.add(tabSheet);

	//
	//
	//

	descriptor = TabDescriptorBuilder.get().//
		withLabel(getName()).//
		withComponent(mainLayout).//
		build();

    }

    /**
     * @return
     */
    public TabDescriptor getDescriptor() {

	return descriptor;
    }
}
