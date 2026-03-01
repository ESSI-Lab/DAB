/**
 *
 */
package eu.essi_lab.gssrv.conf;

import java.util.*;

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

import java.util.stream.*;

import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.*;
import com.vaadin.flow.data.renderer.ComponentRenderer;

import eu.essi_lab.api.database.Database;
import eu.essi_lab.api.database.DatabaseFolder;
import eu.essi_lab.api.database.SourceStorageWorker;
import eu.essi_lab.api.database.factory.DatabaseFactory;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gui.components.tabs.descriptor.*;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.StringUtils;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;

/**
 * @author Fabrizio
 */
public class RecordsInspector extends AbstractGridDescriptor<RecordsInspector.GridData> {

    private final TabContentDescriptor descriptor;

    private static final String NAME_COLUMN = "Name";
    private static final String ID_COLUMN = "Source id";
    private static final String SOURCE_DEP_COLUMN = "Source deployment";
    private static final String SIZE_COLUMN = "Size";
    private static final String PERCENTAGE_COLUMN = "%";

    /**
     *
     */
    public RecordsInspector() {

	getGrid().addColumn(GridData::getPosition).//
		setWidth("60px").//
		setFlexGrow(0);

	Column<GridData> nameColumn = addSortableResizableColumn(NAME_COLUMN, GridData::getSourceName, 350);

	Column<GridData> idColumn = addSortableResizableColumn(ID_COLUMN, GridData::getIdentifier, 300);

	Column<GridData> deploymentColumn = addSortableResizableColumn(SOURCE_DEP_COLUMN, GridData::getSourceDeployment, 300);

	getGrid().addColumn(GridData::getFormattedSize).//
		setHeader(SIZE_COLUMN).//
		setKey(SIZE_COLUMN).//
		setWidth("100px").//
		setSortable(true).//
		setComparator(Comparator.comparing(GridData::getSize));

	getGrid().addColumn(new ComponentRenderer<>(gd -> {

	    Label label = new Label();
	    label.setText(gd.getDataFolder());
	    label.getStyle().set("font-weight", gd.isWritingFolder() ? "bold" : "normal");

	    return label;

	})).setHeader("Data #").//
		setWidth("100px").//
		setSortable(true);//

	getGrid().addColumn(GridData::getPercentage).//
		setKey(PERCENTAGE_COLUMN).//
		setWidth("100px").//
		setHeader(PERCENTAGE_COLUMN).//
		setSortable(true);

	//
	//
	//

	HeaderRow filterRow = getGrid().appendHeaderRow();

	addFilterField(filterRow, nameColumn);
	addFilterField(filterRow, idColumn);
	addFilterField(filterRow, deploymentColumn);

	//
	//
	//

	descriptor = TabContentDescriptorBuilder.get().//

		withLabel("Records inspection").//
		withShowDirective("Click \"Reload\" to show the list of all the harvested sources, referenced by "
		+ "name and identifier, along with the number of harvested records (\"Size\") and the percentage related to the total "
		+ "number of records in the database (visible at the bottom of the \"Size\" column).\n\n\"Data #\" indicates the logical data folder (#1 or #2) where the "
		+ "source records are stored", false).//
		withComponent(getVerticalLayout()).//
		reloadable(() -> update(getVerticalLayout())).//
		build();
    }

    @Override
    protected List<GridData> getItems() {

	return List.of();
    }

    /**
     * @return
     */
    public TabContentDescriptor get() {

	return descriptor;
    }

    /**
     * @param verticalLayout
     * @return
     */
    @Override
    protected void update(VerticalLayout verticalLayout) {

	final List<GridData> sdList = Collections.synchronizedList(new ArrayList<>());

	Database db = getDatabase();

	List<DatabaseFolder> dataFolders = getDataFolders(db);

	ConfigurationWrapper.getHarvestedAndMixedSources().//
		parallelStream().//
		forEach(s -> {//

	    sdList.addAll(dataFolders.stream().//
		    filter(f -> DatabaseFolder.computeSourceId(db, f).equals(s.getUniqueIdentifier())).//
		    map(f -> new GridData(f, s)).//
		    toList());
	});

	double total = sdList.stream().mapToInt(sd -> sd.getSize().intValue()).sum();

	updateTotalFooter(total);

	updatePercentageFooter(100);

	List<GridData> sortedList = sdList.stream().//
		sorted((sd1, sd2) -> sd2.getSize().compareTo(sd1.getSize())).//
		peek(sd -> sd.setTotal(total)).//
		collect(Collectors.toList());

	getGrid().setItems(sortedList);

	//
	//
	//

	@SuppressWarnings("unchecked")
	ListDataProvider<GridData> dataProvider = (ListDataProvider<GridData>) getGrid().getDataProvider();

	dataProvider.addDataProviderListener(l -> {

	    Stream<GridData> stream = l.getSource().fetch(new Query<>());

	    updateTotalFooter(stream.mapToDouble(GridData::getSize).sum());

	    stream = l.getSource().fetch(new Query<>());

	    updatePercentageFooter(stream.mapToDouble(GridData::getPercentageDouble).sum());
	});

	dataProvider.setFilter(item -> getGridFilter().test(item));
    }

    /**
     * @param total
     */
    private void updateTotalFooter(double total) {

	Label label_ = new Label();
	label_.setText(StringUtils.format(total));
	label_.getStyle().set("font-weight", "bold");

	getGrid().getColumnByKey(SIZE_COLUMN).setFooter(label_);
    }

    /**
     * @param total
     */
    private void updatePercentageFooter(double total) {

	Label label_ = new Label();
	label_.setText(StringUtils.format(total));
	label_.getStyle().set("font-weight", "bold");

	getGrid().getColumnByKey(PERCENTAGE_COLUMN).setFooter(label_);
    }

    /**
     * @param db
     * @return
     */
    private List<DatabaseFolder> getDataFolders(Database db) {

	try {
	    return db.getDataFolders();

	} catch (GSException e) {
	    GSLoggerFactory.getLogger(RecordsInspector.class).error(e);
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
	    GSLoggerFactory.getLogger(RecordsInspector.class).error(e);
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
    protected static class GridFilter extends AbstractGridFilter<GridData> {

	@Override
	protected String getItemValue(String colum, GridData gridData) {

	    return switch (colum) {
		case NAME_COLUMN -> gridData.getSourceName();
		case ID_COLUMN -> gridData.getIdentifier();
		case SOURCE_DEP_COLUMN -> gridData.getSourceDeployment();
		default -> null;
	    };
	}
    }

    @Override
    protected GridFilter createGridFilter() {

	return new GridFilter();
    }

    @Override
    protected Class<GridData> getGridDataModel() {

	return GridData.class;
    }

    /**
     * @author Fabrizio
     */
    protected class GridData implements GridDataModel {

	private final String sourceLabel;
	private final double size;
	private final String sourceDeployment;
	private double total;
	private final String dataFolder;
	private final String sourceId;
	private boolean writingFolder;

	/**
	 * @param folder
	 * @param source
	 */
	private GridData(DatabaseFolder folder, GSSource source) {

	    this.sourceLabel = source.getLabel();
	    this.sourceId = source.getUniqueIdentifier();
	    this.size = getFolderSize(folder);
	    this.sourceDeployment = String.join(",", source.getDeployment());
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
		    getGrid().getListDataView().//
			    getItems().//
			    toList().//
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
	public double getPercentageDouble() {

	    return (size / total) * 100;
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
	@Override
	public String getIdentifier() {

	    return sourceId;
	}

	/**
	 * @return
	 */
	public String getSourceDeployment() {

	    return Optional.ofNullable(sourceDeployment).orElse("");
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
