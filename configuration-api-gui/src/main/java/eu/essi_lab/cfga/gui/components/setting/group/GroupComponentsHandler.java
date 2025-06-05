package eu.essi_lab.cfga.gui.components.setting.group;

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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.data.provider.AbstractListDataView;
import com.vaadin.flow.data.provider.HasListDataView;

import eu.essi_lab.cfga.setting.Setting;

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

    private HashMap<String, ArrayList<Component>> itemToComponentsMap;
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
			c.setVisible(key.equals(value));
		    } else {
			c.setVisible(false);
		    }
		} else {
		    c.setVisible(key.equals(value));
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

	Set<String> selection = getGroupSetting().//
		getSettings().//
		stream().//
		filter(Setting::isSelected).//
		map(s -> s.getName()).//
		collect(Collectors.toSet());

	return selection;
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

	list.forEach(c -> c.setVisible(false));

	ArrayList<Component> compList = getItemToComponentsMap().get(name);
	if (compList == null) {
	    compList = new ArrayList<>();
	    getItemToComponentsMap().put(name, compList);
	}

	compList.addAll(list);
    }

    /**
     * @param name The radio button or check box label, e.g: "GBIF Accessor", "OAIPMH Accessor"
     * @param component
     */
    public void addComponent(String name, Component component) {

	addComponents(name, Arrays.asList(component));
    }

    /**
     * @param name The radio button or check box label, e.g: "GBIF Accessor, "OAIPMH Accessor"
     */
    public void addItem(String name) {

	AbstractListDataView<String> listDataView = getListDataView();

	List<String> list = listDataView.getItems().collect(Collectors.toList());

	if (!list.contains(name)) {
	    list.add(name);
	}

	setItems(list);
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

}
