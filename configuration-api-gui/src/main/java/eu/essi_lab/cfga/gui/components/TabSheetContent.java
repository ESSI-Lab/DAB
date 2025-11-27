package eu.essi_lab.cfga.gui.components;

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

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.tabs.*;
import eu.essi_lab.cfga.gui.extension.*;

import java.util.*;

/**
 * @author Fabrizio
 */
public class TabSheetContent extends TabSheet implements Renderable {

    private boolean rendered;
    private final List<TabContent> list;

    /**
     *
     */
    public TabSheetContent() {

	setWidthFull();
	list = new ArrayList<>();
    }

    /**
     * @param desc
     * @param content
     */
    public void add(String label, TabContent content) {

	super.add(label, content);

	list.add(content);
    }

    @Override
    public void setRendered(boolean rendered) {

	this.rendered = true;
    }

    @Override
    public void render(boolean refresh) {

	list.forEach(rend -> rend.render(refresh));//
    }

    @Override
    public boolean isRendered() {

	return rendered;
    }

    @Override
    public Component getComponent() {

	return this;
    }
}
