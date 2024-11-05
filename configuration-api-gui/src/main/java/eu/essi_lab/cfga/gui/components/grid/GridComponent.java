package eu.essi_lab.cfga.gui.components.grid;

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.function.ValueProvider;

import eu.essi_lab.cfga.Configuration;
import eu.essi_lab.cfga.ConfigurationChangeListener.ConfigurationChangeEvent;
import eu.essi_lab.cfga.gui.components.SettingComponentFactory;
import eu.essi_lab.cfga.gui.components.TabContainer;
import eu.essi_lab.cfga.gui.components.setting.SettingComponent;
import eu.essi_lab.cfga.gui.dialog.NotificationDialog;
import eu.essi_lab.cfga.setting.Setting;

/**
 * @author Fabrizio
 */
@SuppressWarnings("serial")
public class GridComponent extends Grid<HashMap<String, String>> {

    private GridInfo gridInfo;
    private HeaderRow filterRow;
    private GridFilter gridFilter;
    private ListDataProvider<HashMap<String, String>> dataProvider;
    static List<Checkbox> CHECKS;

    /**
     * @param gridInfo
     * @param list
     * @param configuration
     * @param container
     * @param readOnly
     * @param refresh
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public GridComponent(//
	    GridInfo gridInfo, //
	    List<Setting> list, //
	    Configuration configuration, //
	    TabContainer container, //
	    boolean readOnly, //
	    boolean refresh) {

	CHECKS = new ArrayList<Checkbox>();

	configuration.addChangeEventListener(event -> {

	    // updates the checks list in case of settings removal
	    if (event.getEventType() == ConfigurationChangeEvent.SETTING_REMOVED) {

		String identifier = event.getSetting().get().getIdentifier();

		Checkbox checkbox = CHECKS.//
			stream().//
			filter(c -> c.getId().get().equals(identifier)).//
			findFirst().//
			get();

		CHECKS.remove(checkbox);
	    }
	});

	//
	//
	//

	this.gridInfo = gridInfo;

	if (!refresh) {

	    GridFilter.clearValuesCache();
	    ColumnsHider.clearValuesCache();
	}

	//
	//
	//

	if (!gridInfo.getContextMenuItems().isEmpty()) {

	    GridContextMenu<HashMap<String, String>> menu = addContextMenu();

	    gridInfo.getContextMenuItems().forEach(cmi -> {

		if (cmi.withTopDivider()) {

		    menu.add(new Hr());
		}

		menu.addItem(cmi.getItemText(), event -> {

		    Optional<HashMap<String, String>> item = event.getItem();

		    if (!item.isPresent() || item.get().isEmpty()) {

			NotificationDialog.getWarningDialog("No row selected").open();
			return;
		    }

		    HashMap<String, Boolean> map = new HashMap<>();
		    CHECKS.forEach(check -> map.put(check.getId().get(), check.getValue()));

		    Setting setting = configuration.list().//
			    stream().//
			    filter(s -> s.getIdentifier().equals(item.get().get("identifier"))).//
			    findFirst().//
			    get();

		    cmi.onClick(event, container, configuration, setting, map);
		});

		if (cmi.withBottomDivider()) {

		    menu.add(new Hr());
		}
	    }

	    );
	}

	//
	//
	//

	UI.getCurrent().getPage().retrieveExtendedClientDetails(receiver -> {

	    int screenHeight = receiver.getScreenHeight();
	    setHeight(screenHeight - 420, Unit.PIXELS);
	});

	getStyle().set("font-size", "14px");

	addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_NO_ROW_BORDERS, GridVariant.LUMO_ROW_STRIPES);

	//
	//
	//

	ArrayList<HashMap<String, String>> items = createItems(list);

	setItems(items);

	//
	//
	//

	setSelectionMode(Grid.SelectionMode.NONE);

	//
	// shows the setting component when the user clicks on a row
	//
	setItemDetailsRenderer(createItemDetailsRenderer(configuration, readOnly, container));

	//
	//
	//

	dataProvider = (ListDataProvider) getDataProvider();

	gridInfo.getColumnsDescriptors().forEach(descriptor -> {

	    Grid.Column<HashMap<String, String>> column = null;

	    if (descriptor.getRenderer().isPresent()) {

		column = addColumn(descriptor.getRenderer().get());

	    } else {

		column = addColumn(new MapValueProvider(descriptor.getColumnName()));

		column.setEditorComponent(new TextArea());
	    }

	    column.setKey(descriptor.getColumnName());

	    column.setResizable(true);

	    if (!descriptor.isColumnVisible()) {

		column.setVisible(false);
	    }

	    column.setSortable(descriptor.isSortable());//

	    if (descriptor.getComparator().isPresent()) {

		column.setComparator(descriptor.getComparator().get());
	    }

	    if (descriptor.getColumnName().equals(ColumnDescriptor.POSITIONAL_COLUMN_NAME)) {

		column.setHeader("");

	    } else {

		column.setHeader(descriptor.getColumnName());
	    }

	    if (descriptor.getColumnWidth() > 0) {

		column.setWidth(descriptor.getColumnWidth() + "px");

	    } else {

		column.setAutoWidth(true);
	    }

	    if (descriptor.isFiltered() || descriptor.getRenderer().isPresent()) {

		addFilter(descriptor, column);
	    }
	});

	//
	//
	//

	setPageSize(gridInfo.getPageSize());
    }

    /**
     * @return
     */
    public Component createColumnsHider() {

	return new ColumnsHider(this, gridInfo);
    }

    /**
     * @param component
     */
    public void addSettingComponent(SettingComponent component) {

	HashMap<String, String> item = createItems(Arrays.asList(component.getSetting())).get(0);

	this.dataProvider.getItems().add(item);

	this.dataProvider.refreshAll();
    }

    /**
     * @param oldComponent
     * @param newComponent
     */
    public void replaceSettingComponent(SettingComponent oldComponent, SettingComponent newComponent) {

	HashMap<String, String> hashMap = dataProvider.getItems().stream()
		.filter(map -> map.get("identifier").equals(oldComponent.getSetting().getIdentifier())).findFirst().get();

	this.dataProvider.getItems().remove(hashMap);

	HashMap<String, String> items = createItems(Arrays.asList(newComponent.getSetting())).get(0);

	this.dataProvider.getItems().add(items);

	this.dataProvider.refreshAll();
    }

    /**
     * Setting id is also required since when we get here, the setting has already been removed from the configuration
     * so the method SettingComponent.getSetting would fail because the setting would be searched in the configuration
     * 
     * @param component
     * @param settingIdentifier
     */
    public void removeSettingComponent(SettingComponent component, String settingIdentifier) {

	HashMap<String, String> items = dataProvider.getItems().stream().filter(map -> map.get("identifier").equals(settingIdentifier))
		.findFirst().get();

	this.dataProvider.getItems().remove(items);

	this.dataProvider.refreshAll();
    }

    /**
     * @param configuration
     * @param readOnly
     * @param container
     * @return
     */
    private Renderer<HashMap<String, String>> createItemDetailsRenderer(Configuration configuration, boolean readOnly,
	    TabContainer container) {

	return new ComponentRenderer<>((source) -> {

	    String settingId = source.get("identifier");

	    SettingComponent comp = SettingComponentFactory.createSettingComponent(//
		    configuration, //
		    settingId, //
		    readOnly, //
		    container);

	    comp.getElement().getStyle().set("border", "2px solid gray");
	    comp.getElement().getStyle().set("padding-left", "10px");
	    comp.getElement().getStyle().set("padding-right", "10px");
	    comp.getElement().getStyle().set("width", "auto");

	    return comp;
	});
    }

    /**
     * @param descriptor
     * @param column
     */
    private void addFilter(ColumnDescriptor descriptor, Grid.Column<HashMap<String, String>> column) {

	//
	//
	//

	if (filterRow == null) {

	    filterRow = appendHeaderRow();

	    gridFilter = new GridFilter();

	    dataProvider.setFilter(item -> gridFilter.test(item));
	}

	//
	//
	//

	if (descriptor.hasCheckBox()) {

	    Checkbox checkbox = new Checkbox();

	    filterRow.getCell(column).setComponent(checkbox);

	    checkbox.addClickListener(event -> {

		Boolean value = event.getSource().getValue();

		GridComponent.CHECKS.forEach(c -> c.setValue(value));
	    });

	} else {

	    TextField filterField = new TextField();

	    filterField.addValueChangeListener(event -> {

		gridFilter.filter(descriptor.getColumnName(), event.getValue());

		dataProvider.refreshAll();
	    });

	    filterField.setValueChangeMode(ValueChangeMode.EAGER);

	    filterRow.getCell(column).setComponent(filterField);

	    filterField.setSizeFull();
	    filterField.setPlaceholder("Filter");
	    filterField.getElement().setAttribute("focus-target", "");

	    Optional<String> value = GridFilter.getValue(descriptor.getColumnName());
	    if (value.isPresent()) {

		filterField.setValue(value.get());
	    }
	}
    }

    /**
     * A {@link Grid} of {@link Setting} would be to heavy to
     * handle, so we convert each {@link Setting} in a {@link HashMap} of strings, according to the value providers
     * retrieved from the given <code>gridInfo</code>
     * 
     * @param gridInfo
     * @param list
     * @return
     */
    private ArrayList<HashMap<String, String>> createItems(List<Setting> list) {

	ArrayList<HashMap<String, String>> items = new ArrayList<HashMap<String, String>>();

	list.forEach(setting -> {

	    HashMap<String, String> map = new HashMap<>();

	    map.put("identifier", setting.getIdentifier());

	    gridInfo.getColumnsDescriptors().forEach(desc -> {

		String columnName = desc.getColumnName();

		ValueProvider<Setting, String> valueProvider = desc.getValueProvider();

		if (valueProvider != null) {

		    String value = valueProvider.apply(setting);

		    map.put(columnName, value);
		}
	    });

	    items.add(map);
	});

	return items;
    }

    /**
     * Provides values from the given map, according to the supplied column name which is
     * used as map key
     * 
     * @author Fabrizio
     */
    private class MapValueProvider implements ValueProvider<HashMap<String, String>, String> {

	private String column;

	/**
	 * The column name used as map key
	 * 
	 * @param column
	 */
	private MapValueProvider(String column) {

	    this.column = column;
	}

	@Override
	public String apply(HashMap<String, String> source) {

	    if (column.equals(ColumnDescriptor.POSITIONAL_COLUMN_NAME)) {

		return String.valueOf(//
			GridComponent.this.getListDataView().//
				getItems().//
				collect(Collectors.toList()).//
				indexOf(source) + 1);
	    }

	    return source.get(column);
	}
    }
}
