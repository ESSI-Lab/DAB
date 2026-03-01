package eu.essi_lab.gssrv.conf;

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
import com.vaadin.flow.component.grid.*;
import com.vaadin.flow.component.grid.contextmenu.*;
import com.vaadin.flow.component.orderedlayout.*;
import com.vaadin.flow.component.textfield.*;
import com.vaadin.flow.data.provider.*;
import com.vaadin.flow.data.renderer.*;
import com.vaadin.flow.data.value.*;
import com.vaadin.flow.function.*;
import eu.essi_lab.cfga.gui.components.*;
import eu.essi_lab.cfga.gui.components.tabs.descriptor.*;
import eu.essi_lab.cfga.gui.dialog.*;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

/**
 * @author Fabrizio
 */
public abstract class AbstractGridDescriptor<G extends GridDataModel> extends TabDescriptor {

    private final VerticalLayout verticalLayout;
    private final Grid<G> grid;
    private final AbstractGridFilter<G> gridFilter;

    /**
     *
     */
    public AbstractGridDescriptor() {

	verticalLayout = ComponentFactory.createNoSpacingNoMarginVerticalLayout();
	verticalLayout.getStyle().set("margin-top", "5px");
	verticalLayout.getStyle().set("padding", "0px");
	verticalLayout.setWidthFull();
	verticalLayout.setHeightFull();

	grid = new Grid<>(getGridDataModel(), false);
	grid.getStyle().set("font-size", "13px");
	grid.setWidthFull();
	grid.addThemeVariants(GridVariant.LUMO_WRAP_CELL_CONTENT);

	gridFilter = createGridFilter();

	verticalLayout.add(grid);

	createItemDetailsRenderer().ifPresent(grid::setItemDetailsRenderer);

	UI.getCurrent().getPage().retrieveExtendedClientDetails(receiver -> {

	    int screenHeight = receiver.getScreenHeight();
	    grid.setHeight(screenHeight - ComponentFactory.MIN_HEIGHT_OFFSET - 100, Unit.PIXELS);
	});
    }

    /**
     * @param name
     * @param valueProvider
     * @param width
     * @return
     */
    protected Grid.Column<G> addSortableResizableColumn(String name, ValueProvider<G, ?> valueProvider, int width) {

	return getGrid().addColumn(valueProvider).//
		setHeader(name).//
		setKey(name).//
		setWidth(width + "px").//
		setSortable(true).//
		setResizable(true);
    }

    /**
     * @param name
     * @param valueProvider
     * @param width
     * @return
     */
    protected Grid.Column<G> addSortableColumn(String name, ValueProvider<G, ?> valueProvider, int width) {

	return getGrid().addColumn(valueProvider).//
		setHeader(name).//
		setKey(name).//
		setWidth(width + "px").//
		setSortable(true);
    }

    /**
     * @return
     */
    protected Optional<Renderer<G>> createItemDetailsRenderer() {

	return Optional.empty();
    }

    /**
     * @param filterRow
     * @param column
     */
    protected void addFilterField(HeaderRow filterRow, Grid.Column<G> column) {

	TextField filterField = new TextField();
	filterField.getStyle().set("font-size", "13px");

	filterField.addValueChangeListener(event -> {

	    gridFilter.filter(column.getKey(), event.getValue());
	    getGrid().getDataProvider().refreshAll();
	});

	filterField.setValueChangeMode(ValueChangeMode.EAGER);
	filterField.setSizeFull();
	filterField.setPlaceholder("Filter");
	filterField.getElement().setAttribute("focus-target", "");

	filterRow.getCell(column).setComponent(filterField);
    }

    /**
     * @param consumer
     */
    protected void addContextMenu(Consumer<HashMap<String, Boolean>> consumer) {

	GridContextMenu<G> menu = getGrid().addContextMenu();

	menu.addGridContextMenuOpenedListener(event -> {

	    GridContextMenu<G> source = event.getSource();

	    GridMenuItem<G> menuItem = source.getItems().getFirst();

	    Optional<G> eventItem = event.getItem();

	    if (eventItem.isPresent()) {

		String viewId = eventItem.get().getIdentifier();

		HashMap<String, Boolean> selectionMap = createSelectionMap(getGrid());

		menuItem.setEnabled(selectionMap.get(viewId));
	    }
	});

	GridMenuItem<G> removeViewItem = menu.addItem("Remove selected item", event -> {

	    HashMap<String, Boolean> selectionMap = createSelectionMap(getGrid());

	    ConfirmationDialog dialog = new ConfirmationDialog(

		    "Click 'Confirm' to permanently delete the selected items", evt -> {

		consumer.accept(selectionMap);
	    });

	    dialog.setWidth(670, Unit.PIXELS);
	    dialog.setTitle("Items removal");
	    dialog.getContentLayout().getStyle().set("font-size", "13px");
	    dialog.open();
	});

	removeViewItem.setId("remove-item");
    }

    /**
     * @return
     */
    protected abstract List<G> getItems();

    /**
     * @return
     */
    protected abstract AbstractGridFilter<G> createGridFilter();

    /**
     * @return
     */
    protected AbstractGridFilter<G> getGridFilter() {

	return gridFilter;
    }

    /**
     * @param verticalLayout
     * @return
     */
    protected void update(VerticalLayout verticalLayout) {

	List<G> list = getItems();

	getGrid().setItems(list);

	@SuppressWarnings("unchecked")
	ListDataProvider<G> dataProvider = (ListDataProvider<G>) getGrid().getDataProvider();

	dataProvider.setFilter(gridFilter::test);
    }

    /**
     * @param grid
     * @return
     */
    private HashMap<String, Boolean> createSelectionMap(Grid<G> grid) {

	List<String> selIds = grid.getSelectedItems().//
		stream().//
		map(G::getIdentifier).//
		toList();

	Stream<G> items = grid.getListDataView().getItems();

	HashMap<String, Boolean> map = new HashMap<>();

	items.forEach(item -> {

	    String identifier = item.getIdentifier();

	    if (selIds.contains(identifier)) {

		map.put(identifier, true);

	    } else {

		map.put(identifier, false);
	    }
	});

	return map;
    }

    /**
     * @return
     */
    public VerticalLayout getVerticalLayout() {

	return verticalLayout;
    }

    /**
     * @return
     */
    public Grid<G> getGrid() {

	return grid;
    }

    /**
     * @return
     */
    protected abstract Class<G> getGridDataModel();
}
