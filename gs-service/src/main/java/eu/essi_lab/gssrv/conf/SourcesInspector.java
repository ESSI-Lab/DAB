/**
 *
 */
package eu.essi_lab.gssrv.conf;

import java.util.ArrayList;
import java.util.HashMap;

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

import java.util.List;
import java.util.stream.Collectors;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;

import eu.essi_lab.api.database.Database;
import eu.essi_lab.api.database.DatabaseFolder;
import eu.essi_lab.api.database.SourceStorageWorker;
import eu.essi_lab.api.database.factory.DatabaseFactory;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gui.extension.*;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.StringUtils;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;

/**
 * @author Fabrizio
 */
public class SourcesInspector extends ComponentInfo {

    private final TabDescriptor descriptor;
    private VerticalLayout verticalLayout;
    private Grid<GridData> grid;
    private GridFilter gridFilter;

    private static final String NAME_COLUMN = "Name";
    private static final String ID_COLUMN = "Source id";
    private static final String SIZE_COLUMN = "Size";

    /**
     *
     */
    public SourcesInspector() {

	setName("Sources inspection");

	verticalLayout = new VerticalLayout();
	verticalLayout.getStyle().set("margin-top", "15px");

	verticalLayout.setWidthFull();
	verticalLayout.setHeightFull();

	//
	//
	//

	grid = new Grid<>(GridData.class, false);

	grid.addColumn(GridData::getPosition).//
		setWidth("60px").//
		setFlexGrow(0);

	Column<GridData> nameColumn = grid.addColumn(GridData::getSourceName).//
		setHeader(NAME_COLUMN).//
		setKey(NAME_COLUMN).//
		setWidth("350px").//
		setSortable(true).//
		setResizable(true);//

	Column<GridData> idColumn = grid.addColumn(GridData::getSourceId).//
		setHeader(ID_COLUMN).//
		setKey(ID_COLUMN).//
		setWidth("350px").//
		setSortable(true).//
		setResizable(true);//

	grid.addColumn(GridData::getFormattedSize).//
		setHeader(SIZE_COLUMN).//
		setKey(SIZE_COLUMN).//
		setWidth("100px").//
		setFlexGrow(0).//
		setSortable(true).//
		setComparator((sd1, sd2) -> sd1.getSize().compareTo(sd2.getSize()));

	grid.addColumn(new ComponentRenderer<>(gd -> {

	    Label label = new Label();
	    label.setText(gd.getDataFolder());
	    label.getStyle().set("font-weight", gd.isWritingFolder() ? "bold" : "normal");

	    return label;

	})).setHeader("Data #").//
		setWidth("100px").//
		setFlexGrow(0).//
		setSortable(true);//

	grid.addColumn(GridData::getPercentage).//
		setKey("%").//
		setWidth("100px").//
		setFlexGrow(0).//
		setHeader("%").//
		setSortable(true);

	grid.getStyle().set("font-size", "14px");

	grid.setWidthFull();

	grid.addThemeVariants(GridVariant.LUMO_WRAP_CELL_CONTENT);

	UI.getCurrent().getPage().retrieveExtendedClientDetails(receiver -> {

	    int screenHeight = receiver.getScreenHeight();
	    grid.setHeight(screenHeight - 370, Unit.PIXELS);
	});

	//
	//
	//

	HeaderRow filterRow = grid.appendHeaderRow();

	gridFilter = new GridFilter();

	addFilterField(filterRow, nameColumn);
	addFilterField(filterRow, idColumn);

	//
	//
	//

	verticalLayout.add(grid);

	//
	//
	//

	descriptor = TabDescriptorBuilder.get().//
		withLabel(getName()).//
		withShowDirective("Click \"Reload\" to show the list of all the harvested sources, referenced by "
		+ "name and identifier, along with the number of harvested records (\"Size\") and the percentage related to the total "
		+ "number of records in the database (visible at the bottom of the \"Size\" column). \"Data #\" indicates the logical data folder (#1 or #2) where the "
		+ "source records are stored").//
		withComponent(verticalLayout).//
		reloadable(() -> update(verticalLayout)).//
		build();
    }

    /**
     *
     * @return
     */
    public TabDescriptor getDescriptor() {

	return descriptor;
    }

    /**
     * @param verticalLayout
     * @return
     */
    private void update(VerticalLayout verticalLayout) {

	final List<GridData> sdList = new ArrayList<>();

	Database db = getDatabase();

	List<DatabaseFolder> dataFolders = getDataFolders(db);

	ConfigurationWrapper.getHarvestedAndMixedSources().//
		parallelStream().//
		// filter(sourceFilter).//
			forEach(s -> {//

		    sdList.addAll(dataFolders.stream().//
			    filter(f -> DatabaseFolder.computeSourceId(db, f).equals(s.getUniqueIdentifier())).//
			    map(f -> new GridData(f, s)).//
			    collect(Collectors.toList()));
		});

	double total = sdList.stream().mapToInt(sd -> sd.getSize().intValue()).sum();

	grid.getColumnByKey(SIZE_COLUMN).setFooter(StringUtils.format(total));

	List<GridData> sortedList = sdList.stream().//
		sorted((sd1, sd2) -> sd2.getSize().compareTo(sd1.getSize())).//
		peek(sd -> sd.setTotal(total)).//
		collect(Collectors.toList());

	grid.setItems(sortedList);

	//
	//
	//

	@SuppressWarnings("unchecked")
	ListDataProvider<GridData> dataProvider = (ListDataProvider<GridData>) grid.getDataProvider();

	dataProvider.setFilter(item -> gridFilter.test(item));
    }

    /**
     * @param filterRow
     * @param column
     */
    private void addFilterField(HeaderRow filterRow, Column<GridData> column) {

	TextField filterField = new TextField();

	filterField.addValueChangeListener(event -> {

	    gridFilter.filter(column.getKey(), event.getValue());
	    grid.getDataProvider().refreshAll();
	});

	filterField.setValueChangeMode(ValueChangeMode.EAGER);
	filterField.setSizeFull();
	filterField.setPlaceholder("Filter");
	filterField.getElement().setAttribute("focus-target", "");

	filterRow.getCell(column).setComponent(filterField);
    }

    /**
     * @param db
     * @return
     */
    private List<DatabaseFolder> getDataFolders(Database db) {

	try {
	    return db.getDataFolders();

	} catch (GSException e) {
	    GSLoggerFactory.getLogger(SourcesInspector.class).error(e);
	}

	return null;
    }

    /**
     * @param f
     * @return
     */
    private static int getFolderSize(DatabaseFolder f) {

	try {
	    return f.size();
	} catch (Exception e) {
	    GSLoggerFactory.getLogger(SourcesInspector.class).error(e);
	}

	return 0;
    }

    /**
     * @return
     */
    private Database getDatabase() {

	try {
	    return DatabaseFactory.get(ConfigurationWrapper.getStorageInfo());
	} catch (GSException e) {
	    GSLoggerFactory.getLogger(getClass()).error(e);
	}

	return null;
    }

    /**
     * @author Fabrizio
     */
    private class GridFilter {

	private HashMap<String, String> valuesMap;

	/**
	 *
	 */
	private GridFilter() {

	    valuesMap = new HashMap<>();
	}

	/**
	 * @param gridData
	 * @return
	 */
	public boolean test(GridData gridData) {

	    String sourceName = valuesMap.get(NAME_COLUMN);
	    String sourceId = valuesMap.get(ID_COLUMN);

	    boolean nameMatch = sourceName != null && gridData.getSourceName().toLowerCase().contains(sourceName);
	    boolean idMatch = sourceId != null && gridData.getSourceId().toLowerCase().contains(sourceId);

	    if (sourceName != null && sourceId != null) { // both selected

		return nameMatch && idMatch;
	    }

	    if (sourceName != null && sourceId == null) { // only name

		return nameMatch;
	    }

	    if (sourceName == null && sourceId != null) { // only id

		return idMatch;
	    }

	    return true; // none
	}

	/**
	 * @param columnKey
	 * @param value
	 */
	public void filter(String columnKey, String value) {

	    valuesMap.put(columnKey, value.isEmpty() ? null : value);
	}
    }

    /**
     * @author Fabrizio
     */
    private class GridData {

	private String sourceLabel;
	private double size;
	private double total;
	private String dataFolder;
	private String sourceId;
	private boolean writingFolder;

	/**
	 * @param folder
	 * @param source
	 */
	private GridData(DatabaseFolder folder, GSSource source) {

	    this.sourceLabel = source.getLabel();
	    this.sourceId = source.getUniqueIdentifier();
	    this.size = getFolderSize(folder);
	    this.dataFolder = folder.getName().contains("data-1") ? "1" : "2";
	    try {
		this.writingFolder = folder.getBinary(SourceStorageWorker.WRITING_FOLDER_TAG) != null;
	    } catch (Exception e) {
		GSLoggerFactory.getLogger(getClass()).error(e);
	    }
	}

	/**
	 * @return the writingFolder
	 */
	public boolean isWritingFolder() {

	    return writingFolder;
	}

	/**
	 * @return
	 */
	public String getDataFolder() {

	    return dataFolder;
	}

	/**
	 * @return
	 */
	public String getPosition() {

	    return String.valueOf(//
		    grid.getListDataView().//
			    getItems().//
			    collect(Collectors.toList()).//
			    indexOf(this) + 1);
	}

	/**
	 * @param total
	 * @return
	 */
	public void setTotal(double total) {

	    this.total = total;
	}

	/**
	 * @return
	 */
	public String getPercentage() {

	    return StringUtils.format((size / total) * 100);
	}

	/**
	 * @return
	 */
	public String getSourceName() {

	    return sourceLabel;
	}

	/**
	 * @return
	 */
	public String getSourceId() {

	    return sourceId;
	}

	/**
	 * @return the size
	 */
	public String getFormattedSize() {

	    return StringUtils.format(size);
	}

	/**
	 * @return the size
	 */
	public Double getSize() {

	    return size;
	}

	@Override
	public String toString() {

	    return getSourceName() + ":" + getSize();
	}

	@Override
	public boolean equals(Object o) {

	    return o instanceof GridData && ((GridData) o).getSourceName().equals(this.getSourceName()) && //
		    ((GridData) o).getSize().equals(this.getSize()) && //
		    ((GridData) o).getDataFolder().equals(this.getDataFolder());
	}
    }
}
