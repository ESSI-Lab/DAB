package eu.essi_lab.cfga.gui.components.setting.group;

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

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.checkbox.*;
import com.vaadin.flow.component.radiobutton.*;
import com.vaadin.flow.data.provider.*;
import eu.essi_lab.cfga.gui.components.setting.*;
import eu.essi_lab.cfga.setting.*;

import java.util.*;
import java.util.stream.*;

/**
 * @author Fabrizio
 */
public abstract class GroupComponentsHandler<T extends Component> {

    /**
     * @author Fabrizio
     */
    public enum GroupType {

	/**
	 *
	 */
	RADIO,
	/**
	 *
	 */
	CHECK
    }

    private final HashMap<String, ArrayList<Component>> itemToComponentsMap;
    protected Setting groupSetting;

    /**
     *
     */
    public GroupComponentsHandler() {

	itemToComponentsMap = new HashMap<>();
    }

    /**
     * @param value
     */
    public void setVisibility(String value) {

	for (String key : getItemToComponentsMap().keySet()) {

	    ArrayList<Component> list = getItemToComponentsMap().get(key);

	    boolean groupChild = list.stream().anyMatch(GroupComponentsHandler.this::isGroupComponent);

	    for (Component c : list) {

		if (groupChild) {
		    if (isGroupComponent(c)) {

			setVisible(c, key.equals(value));

		    } else {

			setVisible(c, false);
		    }
		} else {

		    setVisible(c, key.equals(value));
		}
	    }
	}
    }

    /**
     *
     */
    public abstract void applySelection();

    /**
     * @param c
     * @return
     */
    protected boolean isGroupComponent(Component c) {

	return c instanceof CheckboxGroup<?> || c instanceof RadioButtonGroup<?>;
    }

    /**
     * @return
     */
    protected Set<String> getSelection() {

	return getGroupSetting().//
		getSettings().//
		stream().//
		filter(Setting::isSelected).//
		map(Setting::getName).//
		collect(Collectors.toSet());
    }

    /**
     * Set the {@link Setting} which groups all its children {@link Setting}, that is the root {@link Setting}
     *
     * @param groupSetting
     */
    public void setGroupSetting(Setting groupSetting) {

	this.groupSetting = groupSetting;
    }

    /**
     * @param name The radio button or check box label, e.g: "GBIF Accessor", "OAIPMH Accessor"
     * @param list
     */
    public void addComponents(String name, List<Component> list) {

	synchronized (SettingComponent.class) {

	    list.forEach(c -> setVisible(c, false));

	    ArrayList<Component> compList = getItemToComponentsMap().computeIfAbsent(name, k -> new ArrayList<>());

	    compList.addAll(list);
	}
    }

    /**
     * @param name The radio button or check box label, e.g: "GBIF Accessor", "OAIPMH Accessor"
     * @param component
     */
    public void addComponent(String name, Component component) {

	addComponents(name, Collections.singletonList(component));
    }

    /**
     * @param name The radio button or check box label, e.g: "GBIF Accessor, "OAIPMH Accessor"
     */
    public void addItem(String name) {

	synchronized (SettingComponent.class) {

	    AbstractListDataView<String> listDataView = getListDataView();

	    List<String> list = listDataView.getItems().collect(Collectors.toList());

	    if (!list.contains(name)) {
		list.add(name);
	    }

	    setItems(list);
	}
    }

    /**
     * @return
     */
    public List<String> getItems() {

	AbstractListDataView<String> listDataView = getListDataView();

	return listDataView.getItems().collect(Collectors.toList());
    }

    /**
     * @return
     */
    public HashMap<String, ArrayList<Component>> getItemToComponentsMap() {

	return itemToComponentsMap;
    }

    /**
     * @return
     */
    public Setting getGroupSetting() {

	return groupSetting;
    }

    /**
     * @return
     */
    protected abstract T getGroupComponent();

    /**
     * @param items
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void setItems(List<String> items) {

	((HasListDataView) getGroupComponent()).setItems(items);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private AbstractListDataView<String> getListDataView() {

	return (AbstractListDataView<String>) ((HasListDataView) getGroupComponent()).getListDataView();
    }

    /**
     *
     * @param component
     * @param visible
     */
    private void setVisible(Component component, boolean visible) {

	component.getStyle().set("display", visible ? "block":"none");
    }


}
