package eu.essi_lab.gssrv.conf;

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.grid.*;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.*;
import com.vaadin.flow.component.textfield.*;
import com.vaadin.flow.data.provider.*;
import com.vaadin.flow.data.renderer.*;
import com.vaadin.flow.data.value.*;
import eu.essi_lab.api.database.*;
import eu.essi_lab.api.database.factory.*;
import eu.essi_lab.cfga.gs.*;
import eu.essi_lab.cfga.gui.components.*;
import eu.essi_lab.cfga.gui.components.tabs.descriptor.*;
import eu.essi_lab.lib.utils.*;
import eu.essi_lab.model.*;
import eu.essi_lab.model.exceptions.*;

import java.util.*;
import java.util.stream.*;

/**
 * @author Fabrizio
 */
public class AsynchDownloadDescriptor extends TabDescriptor {

    private final VerticalLayout verticalLayout;
    private final Grid<AsynchDownloadDescriptor.GridData> grid;
    private final AsynchDownloadDescriptor.GridFilter gridFilter;

    private static final String DOWNLOAD_NAME_COLUMN = "Download name";
    private static final String EMAIL_COLUMN = "E-mail";
    private static final String BUCKET_COLUMN = "Bucket";
    private static final String MAX_DOWNLOAD_SIZE_COLUMN = "Max. download size";
    private static final String REQUEST_URL_COLUMN = "Request URL";
    private static final String PUBLIC_URL_COLUMN = "Public URL";
    private static final String OPERATION_ID_COLUMN = "Operation ID";
    private static final String EMAIL_NOTIFICATIONS_COLUMN = "E-mail notifications";

    /**
     *
     */
    public AsynchDownloadDescriptor() {

	setLabel("Downloads");

	verticalLayout = new VerticalLayout();
	verticalLayout.getStyle().set("margin-top", "15px");

	verticalLayout.setWidthFull();
	verticalLayout.setHeightFull();

	//
	//
	//

	grid = new Grid<>(AsynchDownloadDescriptor.GridData.class, false);

	grid.getStyle().set("font-size", "13px");

	grid.addColumn(AsynchDownloadDescriptor.GridData::getPosition).//
		setWidth("60px").//
		setFlexGrow(0);

	Grid.Column<AsynchDownloadDescriptor.GridData> nameColumn = grid.addColumn(AsynchDownloadDescriptor.GridData::getSourceName).//
		setHeader(DOWNLOAD_NAME_COLUMN).//
		setKey(DOWNLOAD_NAME_COLUMN).//
		setWidth("350px").//
		setSortable(true).//
		setResizable(true);//

	Grid.Column<AsynchDownloadDescriptor.GridData> idColumn = grid.addColumn(AsynchDownloadDescriptor.GridData::getSourceId).//
		setHeader(EMAIL_COLUMN).//
		setKey(EMAIL_COLUMN).//
		setWidth("350px").//
		setSortable(true).//
		setResizable(true);//

	grid.addColumn(AsynchDownloadDescriptor.GridData::getFormattedSize).//
		setHeader(BUCKET_COLUMN).//
		setKey(BUCKET_COLUMN).//
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

	grid.addColumn(AsynchDownloadDescriptor.GridData::getPercentage).//
		setKey("%").//
		setWidth("100px").//
		setFlexGrow(0).//
		setHeader("%").//
		setSortable(true);

	grid.setWidthFull();

	grid.addThemeVariants(GridVariant.LUMO_WRAP_CELL_CONTENT);

	UI.getCurrent().getPage().retrieveExtendedClientDetails(receiver -> {

	    int screenHeight = receiver.getScreenHeight();
	    grid.setHeight(screenHeight - ComponentFactory.MIN_HEIGHT_OFFSET - 400, Unit.PIXELS);
	});

	//
	//
	//

	HeaderRow filterRow = grid.appendHeaderRow();

	gridFilter = new AsynchDownloadDescriptor.GridFilter();

	addFilterField(filterRow, nameColumn);
	addFilterField(filterRow, idColumn);

	//
	//
	//

	verticalLayout.add(grid);

	//
	//
	//

	TabContentDescriptor descriptor = TabContentDescriptorBuilder.get().//
		withComponent(verticalLayout).//
		build();

	setIndex(GSTabIndex.ASYNC_DOWNLOADS.getIndex());
	addContentDescriptor(descriptor);
    }

    /**
     * @param verticalLayout
     * @return
     */
    private void update(VerticalLayout verticalLayout) {

	final List<AsynchDownloadDescriptor.GridData> sdList = Collections.synchronizedList(new ArrayList<>());

	Database db = getDatabase();

	List<DatabaseFolder> dataFolders = getDataFolders(db);

	ConfigurationWrapper.getHarvestedAndMixedSources().//
		parallelStream().//
		// filter(sourceFilter).//
			forEach(s -> {//

		    sdList.addAll(dataFolders.stream().//
			    filter(f -> DatabaseFolder.computeSourceId(db, f).equals(s.getUniqueIdentifier())).//
			    map(f -> new AsynchDownloadDescriptor.GridData(f, s)).//
			    toList());
		});

	double total = sdList.stream().mapToInt(sd -> sd.getSize().intValue()).sum();

	grid.getColumnByKey(BUCKET_COLUMN).setFooter(StringUtils.format(total));

	List<AsynchDownloadDescriptor.GridData> sortedList = sdList.stream().//
		sorted((sd1, sd2) -> sd2.getSize().compareTo(sd1.getSize())).//
		peek(sd -> sd.setTotal(total)).//
		collect(Collectors.toList());

	grid.setItems(sortedList);

	//
	//
	//

	@SuppressWarnings("unchecked")
	ListDataProvider<AsynchDownloadDescriptor.GridData> dataProvider = (ListDataProvider<AsynchDownloadDescriptor.GridData>) grid.getDataProvider();

	dataProvider.setFilter(item -> gridFilter.test(item));
    }

    /**
     * @param filterRow
     * @param column
     */
    private void addFilterField(HeaderRow filterRow, Grid.Column<AsynchDownloadDescriptor.GridData> column) {

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
	    GSLoggerFactory.getLogger(AsynchDownloadDescriptor.class).error(e);
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
	    GSLoggerFactory.getLogger(AsynchDownloadDescriptor.class).error(e);
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

	private final HashMap<String, String> valuesMap;

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
	public boolean test(AsynchDownloadDescriptor.GridData gridData) {

	    String sourceName = valuesMap.get(DOWNLOAD_NAME_COLUMN);
	    String sourceId = valuesMap.get(EMAIL_COLUMN);

	    boolean nameMatch = sourceName != null && gridData.getSourceName().toLowerCase().contains(sourceName);
	    boolean idMatch = sourceId != null && gridData.getSourceId().toLowerCase().contains(sourceId);

	    if (sourceName != null && sourceId != null) { // both selected

		return nameMatch && idMatch;
	    }

	    if (sourceName != null) { // only name

		return nameMatch;
	    }

	    if (sourceId != null) { // only id

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

	private final String sourceLabel;
	private final double size;
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

	    return o instanceof AsynchDownloadDescriptor.GridData && ((AsynchDownloadDescriptor.GridData) o).getSourceName()
		    .equals(this.getSourceName()) &&
		    //
		    ((AsynchDownloadDescriptor.GridData) o).getSize().equals(this.getSize()) && //
		    ((AsynchDownloadDescriptor.GridData) o).getDataFolder().equals(this.getDataFolder());
	}

    }
}