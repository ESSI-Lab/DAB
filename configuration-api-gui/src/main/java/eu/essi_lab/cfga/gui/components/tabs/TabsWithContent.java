package eu.essi_lab.cfga.gui.components.tabs;

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

import java.util.HashMap;
import java.util.Map;

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.tabs.Tabs.SelectedChangeEvent;

import eu.essi_lab.cfga.ConfigurationChangeListener;
import eu.essi_lab.cfga.gui.components.*;

/**
 * In Vaadin tabs are just tabs, they have no control on the tab content.<br>
 * This component allows to
 * set a content to the tabs, and to select the content according to its tab.
 *
 * @apiNote from Vaadin version 23.4.0 {@link com.vaadin.flow.component.tabs.TabSheet} provides
 * this functionality
 * 
 * @author Fabrizio
 */
@SuppressWarnings("serial")
public class TabsWithContent extends Tabs implements ComponentEventListener<SelectedChangeEvent>, ConfigurationChangeListener {

    private Map<Tab, Renderable> tabsToContent;
    private final Div contentDiv;

    /**
     * 
     */
    public TabsWithContent() {

	tabsToContent = new HashMap<>();
	contentDiv = new Div();

	setOrientation(Orientation.VERTICAL);
	addSelectedChangeListener(this);
    }

    @Override
    public void onComponentEvent(SelectedChangeEvent event) {

	tabsToContent.values().forEach(tabContent -> tabContent.getComponent().setVisible(false));

	Renderable renderable = tabsToContent.get(getSelectedTab());

	if (renderable != null) {

	    renderable.getComponent().setVisible(true);

	    if (!renderable.isRendered()) {

		renderable.render();
	    }
	}
    }

    @Override
    public void configurationChanged(ConfigurationChangeEvent event) {
	switch (event.getEventType()) {
	case ConfigurationChangeEvent.CONFIGURATION_FLUSHED:
	case ConfigurationChangeEvent.SETTING_PUT:
	case ConfigurationChangeEvent.SETTING_REMOVED:
	case ConfigurationChangeEvent.SETTING_REPLACED:

	    tabsToContent.values().forEach(tabContent -> tabContent.setRendered(false));
	}
    }

    /**
     * @param index
     * @param label
     * @param renderable
     * @param fontSizePx
     * @return
     */
    public Tab addTab(int index, String label, Renderable renderable, int fontSizePx) {

	renderable.getComponent().setVisible(index == 0);

	Tab tab = new Tab(label);

	tab.getStyle().set("font-size", fontSizePx + "px");

	add(tab);

	tabsToContent.put(tab, renderable);

	contentDiv.add(renderable.getComponent());

	return tab;
    }

    /**
     * @param index
     * @param label
     * @param renderable
     * @return
     */
    public Tab addTab(int index, String label, Renderable renderable) {

	return addTab(index, label, renderable, 20);
    }

    /**
     * @return the content
     */
    public Div getContent() {

	return contentDiv;
    }

    /**
     * 
     */
    public void clear() {

	tabsToContent = new HashMap<>();

	contentDiv.removeAll();

	removeAll();
    }
}
