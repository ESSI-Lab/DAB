/**
 * 
 */
package eu.essi_lab.cfga.gui.components.grid.renderer;

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

import java.io.Serial;
import java.util.HashMap;
import java.util.Optional;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.data.renderer.ComponentRenderer;

/**
 * @author Fabrizio
 */
public abstract class GridColumnRenderer<C extends Component> extends ComponentRenderer<C, HashMap<String, String>> {

    /**
     * 
     */
    @Serial
    private static final long serialVersionUID = -6816629717419845396L;

    /**
    * 
    */
    public GridColumnRenderer() {

    }

    /**
     * @return
     */
    public Optional<Component> getLegend() {

	return Optional.empty();
    }

    @Override
    public abstract C createComponent(HashMap<String, String> item);

}
