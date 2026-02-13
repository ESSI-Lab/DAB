package eu.essi_lab.gssrv.conf;

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.*;
import com.vaadin.flow.component.grid.*;
import com.vaadin.flow.component.grid.contextmenu.*;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.orderedlayout.*;
import com.vaadin.flow.component.tabs.*;
import com.vaadin.flow.component.textfield.*;
import com.vaadin.flow.component.upload.*;
import com.vaadin.flow.component.upload.receivers.*;
import com.vaadin.flow.data.provider.*;
import com.vaadin.flow.data.renderer.*;
import com.vaadin.flow.data.value.*;
import eu.essi_lab.api.database.*;
import eu.essi_lab.api.database.factory.*;
import eu.essi_lab.cfga.gs.*;
import eu.essi_lab.cfga.gs.setting.database.*;
import eu.essi_lab.cfga.gs.setting.harvesting.*;
import eu.essi_lab.cfga.gui.components.*;
import eu.essi_lab.cfga.gui.components.tabs.descriptor.*;
import eu.essi_lab.cfga.gui.dialog.*;
import eu.essi_lab.lib.utils.*;
import eu.essi_lab.messages.bond.*;
import eu.essi_lab.messages.bond.jaxb.*;
import eu.essi_lab.model.exceptions.*;

import java.io.*;
import java.util.*;
import java.util.stream.*;

public class ViewsDescriptor extends TabDescriptor {

    private final VerticalLayout verticalLayout;
    private final Grid<ViewsDescriptor.GridData> grid;
    private final ViewsDescriptor.GridFilter gridFilter;

    private static final String ID_COLUMN = "Id";
    private static final String LABEL_COLUMN = "Label";
    private static final String CREATOR_COLUMN = "Creator";
    private static final String OWNER_COLUMN = "Owner";
    private static final String VISIBILITY_COLUMN = "Visibility";
    private static final String SOURCE_DEPLOYMENT_COLUMN = "Source Deployment";
    private static final String CREATION_COLUMN = "Creation";
    private static final String EXPIRATION_TIME = "Expiration";

    /**
     *
     */
    public ViewsDescriptor() {

	setLabel("Views");

	verticalLayout = new VerticalLayout();
	verticalLayout.getStyle().set("margin-top", "-5px");

	verticalLayout.setWidthFull();
	verticalLayout.setHeightFull();

	//
	//
	//

	grid = new Grid<>(ViewsDescriptor.GridData.class, false);

	grid.setSelectionMode(Grid.SelectionMode.MULTI);

	grid.getStyle().set("font-size", "13px");

	grid.addColumn(ViewsDescriptor.GridData::getPosition).//
		setHeader("").//
		setKey("#").//
		setWidth("30px");

	Grid.Column<ViewsDescriptor.GridData> idCol = grid.addColumn(ViewsDescriptor.GridData::getId).//
		setHeader(ID_COLUMN).//
		setKey(ID_COLUMN).//
		setWidth("300px").//
		setSortable(true).//
		setResizable(true);//

	Grid.Column<ViewsDescriptor.GridData> labelCol = grid.addColumn(ViewsDescriptor.GridData::getLabel).//
		setHeader(LABEL_COLUMN).//
		setKey(LABEL_COLUMN).//
		setWidth("300px").//
		setSortable(true).//
		setResizable(true);//

	Grid.Column<ViewsDescriptor.GridData> depCol = grid.addColumn(ViewsDescriptor.GridData::getDeployment).//
		setHeader(SOURCE_DEPLOYMENT_COLUMN).//
		setKey(SOURCE_DEPLOYMENT_COLUMN).//
		setWidth("300px").//
		setFlexGrow(0).//
		setSortable(true).//
		setResizable(true);//

	Grid.Column<ViewsDescriptor.GridData> creatorCol = grid.addColumn(ViewsDescriptor.GridData::getCreator).//
		setHeader(CREATOR_COLUMN).//
		setKey(CREATOR_COLUMN).//
		setWidth("200px").//
		setFlexGrow(0).//
		setSortable(true).//
		setResizable(true);//

	//	Grid.Column<ViewsDescriptor.GridData> ownerCol = grid.addColumn(ViewsDescriptor.GridData::getOwner).//
	//		setHeader(OWNER_COLUMN).//
	//		setKey(OWNER_COLUMN).//
	//		setWidth("100px").//
	//		setFlexGrow(0).//
	//		setSortable(true).//
	//		setResizable(true);//

	Grid.Column<ViewsDescriptor.GridData> creationCol = grid.addColumn(ViewsDescriptor.GridData::getCreationTime).//
		setHeader(CREATION_COLUMN).//
		setKey(CREATION_COLUMN).//
		setWidth("150px").//
		setFlexGrow(0).//
		setSortable(true);//
	//
	//	Grid.Column<ViewsDescriptor.GridData> expirationCol = grid.addColumn(ViewsDescriptor.GridData::getExpirationTime).//
	//		setHeader(EXPIRATION_TIME).//
	//		setKey(EXPIRATION_TIME).//
	//		setWidth("150px").//
	//		setFlexGrow(0).//
	//		setSortable(true);

	//	Grid.Column<ViewsDescriptor.GridData> visibilityCol = grid.addColumn(ViewsDescriptor.GridData::getVisibility).//
	//		setHeader(VISIBILITY_COLUMN).//
	//		setKey(VISIBILITY_COLUMN).//
	//		setWidth("100px").//
	//		setFlexGrow(0).//
	//		setSortable(false);

	//
	//
	//

	grid.setWidthFull();
	grid.addThemeVariants(GridVariant.LUMO_WRAP_CELL_CONTENT);

	grid.setItemDetailsRenderer(createItemDetailsRenderer());

	UI.getCurrent().getPage().retrieveExtendedClientDetails(receiver -> {

	    int screenHeight = receiver.getScreenHeight();
	    grid.setHeight(screenHeight - ComponentFactory.MIN_HEIGHT_OFFSET - 400, Unit.PIXELS);
	});

	//
	//
	//

	HeaderRow filterRow = grid.appendHeaderRow();

	gridFilter = new ViewsDescriptor.GridFilter();

	addFilterField(filterRow, creatorCol);
	//	addFilterField(filterRow, ownerCol);
	addFilterField(filterRow, creationCol);
	addFilterField(filterRow, depCol);
	//	addFilterField(filterRow, visibilityCol);
	//	addFilterField(filterRow, expirationCol);
	addFilterField(filterRow, idCol);
	addFilterField(filterRow, labelCol);

	//
	//
	//

	GridContextMenu<ViewsDescriptor.GridData> menu = grid.addContextMenu();

	menu.addGridContextMenuOpenedListener(event -> {

	    GridContextMenu<ViewsDescriptor.GridData> source = event.getSource();

	    GridMenuItem<ViewsDescriptor.GridData> menuItem = source.getItems().getFirst();

	    Optional<ViewsDescriptor.GridData> eventItem = event.getItem();

	    if (eventItem.isPresent()) {

		String viewId = eventItem.get().getId();

		HashMap<String, Boolean> selectionMap = createSelectionMap(grid);

		menuItem.setEnabled(selectionMap.get(viewId));
	    }
	});

	GridMenuItem<ViewsDescriptor.GridData> removeViewItem = menu.addItem("Remove selected views", event -> {

	    Optional<ViewsDescriptor.GridData> eventItem = event.getItem();

	    HashMap<String, Boolean> selectionMap = createSelectionMap(grid);

	    ConfirmationDialog dialog = new ConfirmationDialog(

		    "Click 'Confirm' to permanently deleted the selected views", evt -> {

		try {

		    List<String> ids = selectionMap.keySet().stream().filter(selectionMap::get).toList();

		    for (String id : ids) {

			getWriter().removeView(id);
		    }

		    update(verticalLayout);

		    NotificationDialog.getInfoDialog(ids.size() == 1 ? "View correctly removed" : "Views correctly removed").open();

		} catch (Exception e) {

		    GSLoggerFactory.getLogger(getClass()).error(e);

		    NotificationDialog.getErrorDialog("Error occurred, unable to removeviews: " + e.getMessage(), 700).open();
		}
	    });

	    dialog.setWidth(670, Unit.PIXELS);
	    dialog.setTitle("Views removal");
	    dialog.getContentLayout().getStyle().set("font-size", "14px");
	    dialog.open();
	});

	removeViewItem.setId("remove-view");

	//
	//
	//

	verticalLayout.add(grid);

	//
	//
	//

	TabContentDescriptor descriptor = TabContentDescriptorBuilder.get().//
		withCustomAddDirective(e -> openUploadViewDialog()).//
		withShowDirective("Click 'Reload' to show the list of stored views", false).//
		withComponent(verticalLayout).//
		reloadable(() -> update(verticalLayout)).//
		build();

	setIndex(GSTabIndex.VIEWS.getIndex());
	addContentDescriptor(descriptor);

	//
	//
	//

	GridFilter.SELECTION_MAP.clear();

	update(verticalLayout);
    }

    /**
     *
     */
    private void openUploadViewDialog() {

	final EnhancedDialog dialog = new EnhancedDialog();
	dialog.setHeight(200, Unit.PIXELS);
	dialog.setTitle("Add view");
	dialog.open();

	MemoryBuffer memoryBuffer = new MemoryBuffer();

	Upload upload = new Upload(memoryBuffer);
	upload.addFinishedListener(event -> {

	    try {

		String mimeType = memoryBuffer.getFileData().getMimeType();

		InputStream viewStream = memoryBuffer.getInputStream();

		View view = mimeType.equals("application/json")
			? ViewFactory.fromJSONStream(viewStream)
			: ViewFactory.fromXMLStream(viewStream);

		if(getReader().getView(view.getId()).isPresent()) {

		    NotificationDialog.getErrorDialog("A view with id '"+view.getId()+"' already exists").open();

		    upload.clearFileList();
		    return;
		}

		getWriter().store(view);

		update(verticalLayout);

		dialog.close();

		NotificationDialog.getInfoDialog("View correctly added").open();

	    } catch (Exception ex) {

		dialog.close();

		GSLoggerFactory.getLogger(getClass()).error(ex);

		NotificationDialog.getErrorDialog("Error occurred, unable to add view: " + ex.getMessage()).open();

		upload.interruptUpload();
	    }
	});

	upload.setMaxFiles(1);
	upload.setDropAllowed(true);
	upload.setAcceptedFileTypes("application/json", ".json", "application/xml", ".xml");
	upload.setDropLabel(new Label("Drop file here"));

	Button localUploadButton = new Button("Select view file (JSON or XML)");
	localUploadButton.getStyle().set("font-size", "14px");
	localUploadButton.setWidth("330px");
	localUploadButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

	upload.setUploadButton(localUploadButton);

	dialog.setContent(upload);

    }

    /**
     * @param grid
     * @return
     */
    private HashMap<String, Boolean> createSelectionMap(Grid<ViewsDescriptor.GridData> grid) {

	List<String> selIds = grid.getSelectedItems().//
		stream().//
		map(GridData::getId).//
		toList();

	Stream<ViewsDescriptor.GridData> items = grid.getListDataView().getItems();

	HashMap<String, Boolean> map = new HashMap<>();

	items.forEach(item -> {

	    String identifier = item.getId();

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
    private List<View> getViews() {

	try {

	    return getReader().getViews();

	} catch (Exception e) {

	    GSLoggerFactory.getLogger(HarvestingSetting.class).error("Error occurred: {}", e.getMessage(), e);
	}

	return new ArrayList<>();
    }

    /**
     * @param verticalLayout
     * @return
     */
    private void update(VerticalLayout verticalLayout) {

	List<ViewsDescriptor.GridData> list = getViews(). //
		stream().//
		map(v -> new GridData(v, grid)).//
		sorted((v1, v2) -> v2.getId().compareTo(v1.getId())).//
		toList();

	grid.setItems(list);

	@SuppressWarnings("unchecked")
	ListDataProvider<ViewsDescriptor.GridData> dataProvider = (ListDataProvider<ViewsDescriptor.GridData>) grid.getDataProvider();

	dataProvider.setFilter(gridFilter::test);
    }

    /**
     * @param configuration
     * @param readOnly
     * @param container
     * @return
     */
    private Renderer<ViewsDescriptor.GridData> createItemDetailsRenderer() {

	return new ComponentRenderer<>((gridData) -> {

	    try {

		Optional<View> view = getReader().getView(gridData.getId());

		Div jsonArea = new Div();
		jsonArea.getStyle().set("font-size", "13px");
		jsonArea.setHeightFull();
		jsonArea.setWidthFull();

		String jsonView = ViewFactory.toJSONObject(view.get()).toString(3);
		jsonArea.getElement().setProperty("innerHTML", "<pre> " + escape(jsonView) + " </pre>");

		Div xmlArea = new Div();
		xmlArea.getStyle().set("font-size", "13px");
		xmlArea.setHeightFull();
		xmlArea.setWidthFull();

		String xmlString = ViewFactory.asXMLString(view.get());
		xmlArea.getElement().setProperty("innerHTML", "<pre>" + escape(xmlString) + "</pre>");

		TabSheet tabSheet = new TabSheet();
		tabSheet.getStyle().set("font-size", "13px");
		tabSheet.setHeightFull();
		tabSheet.setWidthFull();
		tabSheet.add("JSON encoding", jsonArea);
		tabSheet.add("XML encoding", xmlArea);

		return tabSheet;

	    } catch (Exception e) {

		GSLoggerFactory.getLogger(ViewsDescriptor.class).error("Error occurred: {}", e.getMessage(), e);
	    }

	    return null;
	});
    }

    /**
     * @param filterRow
     * @param column
     */
    private void addFilterField(HeaderRow filterRow, Grid.Column<ViewsDescriptor.GridData> column) {

	TextField filterField = new TextField();
	filterField.getStyle().set("font-size", "13px");

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
     * @author Fabrizio
     */
    private static class GridFilter {

	private static final HashMap<String, String> SELECTION_MAP = new HashMap<>();

	/**
	 *
	 */
	private GridFilter() {
	}

	/**
	 * @param gridData
	 * @return
	 */
	public boolean test(ViewsDescriptor.GridData gridData) {

	    boolean match = true;

	    for (String colum : SELECTION_MAP.keySet()) {

		String itemValue = switch (colum) {
		    case CREATOR_COLUMN -> gridData.getCreator();
		    case ID_COLUMN -> gridData.getId();
		    case LABEL_COLUMN -> gridData.getLabel();
		    case EXPIRATION_TIME -> gridData.getExpirationTime();
		    case VISIBILITY_COLUMN -> gridData.getVisibility();
		    case OWNER_COLUMN -> gridData.getOwner();
		    case SOURCE_DEPLOYMENT_COLUMN -> gridData.getDeployment();
		    case CREATION_COLUMN -> gridData.getCreationTime();
		    default -> null;
		};

		match &= itemValue == null || itemValue.toLowerCase().contains(SELECTION_MAP.get(colum).toLowerCase());
	    }

	    return match;
	}

	/**
	 * @param columnKey
	 * @param value
	 */
	public void filter(String columnKey, String value) {

	    SELECTION_MAP.put(columnKey, value);
	}
    }

    /**
     * @author Fabrizio
     */
    private static class GridData {

	private final View view;
	private final Grid<ViewsDescriptor.GridData> grid;

	/**
	 * @param setting
	 */
	private GridData(View view, Grid<ViewsDescriptor.GridData> grid) {

	    this.view = view;
	    this.grid = grid;
	}

	/**
	 * @return
	 */
	public String getPosition() {

	    return String.valueOf(//

		    grid.getListDataView().//
			    getItems().//
			    toList().//
			    stream().map(GridData::getId).toList().//
			    indexOf(view.getId()) + 1);
	}

	/**
	 * @return
	 */
	public String getId() {

	    return Optional.ofNullable(view.getId()).orElse("");
	}

	/**
	 * @return
	 */
	public String getLabel() {

	    return Optional.ofNullable(view.getLabel()).orElse("");
	}

	/**
	 * @return the size
	 */
	public String getCreator() {

	    return Optional.ofNullable(view.getCreator()).orElse("");
	}

	/**
	 * @return
	 */
	public String getOwner() {

	    return Optional.ofNullable(view.getOwner()).map(String::valueOf).orElse("");

	}

	/**
	 * @return
	 */
	public String getVisibility() {

	    return Optional.ofNullable(view.getVisibility()).map(String::valueOf).orElse("");
	}

	/**
	 * @return
	 */
	public String getCreationTime() {

	    return Optional.ofNullable(view.getCreationTime()).map(ISO8601DateTimeUtils::getISO8601DateTime).orElse("");
	}

	/**
	 * @return
	 */
	public String getDeployment() {

	    return Optional.ofNullable(view.getSourceDeployment()).orElse("");
	}

	/**
	 * @return
	 */
	public String getExpirationTime() {

	    return Optional.ofNullable(view.getExpirationTime()).map(ISO8601DateTimeUtils::getISO8601DateTime).orElse("");
	}

	@Override
	public String toString() {

	    return getId() + ":" + getLabel();
	}

	@Override
	public boolean equals(Object o) {

	    return o instanceof ViewsDescriptor.GridData && ((ViewsDescriptor.GridData) o).getId().equals(this.getId());
	}
    }

    /**
     * @param text
     * @return
     */
    private static String escape(String text) {

	return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    /**
     * @return
     * @throws GSException
     */
    private static DatabaseWriter getWriter() throws GSException {

	DatabaseSetting setting = ConfigurationWrapper.getDatabaseSetting();

	return DatabaseProviderFactory.getWriter(setting.asStorageInfo());
    }

    /**
     * @return
     * @throws GSException
     */
    private static DatabaseReader getReader() throws GSException {

	DatabaseSetting setting = ConfigurationWrapper.getDatabaseSetting();

	return DatabaseProviderFactory.getReader(setting.asStorageInfo());
    }
}
