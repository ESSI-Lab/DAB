package eu.essi_lab.gssrv.conf;

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.grid.*;
import com.vaadin.flow.component.grid.contextmenu.*;
import com.vaadin.flow.component.orderedlayout.*;
import com.vaadin.flow.component.tabs.*;
import com.vaadin.flow.component.textfield.*;
import com.vaadin.flow.data.provider.*;
import com.vaadin.flow.data.renderer.*;
import com.vaadin.flow.data.value.*;
import eu.essi_lab.api.database.*;
import eu.essi_lab.api.database.factory.*;
import eu.essi_lab.cfga.*;
import eu.essi_lab.cfga.gs.*;
import eu.essi_lab.cfga.gs.setting.database.*;
import eu.essi_lab.cfga.gs.setting.harvesting.*;
import eu.essi_lab.cfga.gui.components.*;
import eu.essi_lab.cfga.gui.components.grid.*;
import eu.essi_lab.cfga.gui.components.setting.*;
import eu.essi_lab.cfga.gui.components.tabs.*;
import eu.essi_lab.cfga.gui.components.tabs.descriptor.*;
import eu.essi_lab.cfga.scheduler.*;
import eu.essi_lab.cfga.setting.*;
import eu.essi_lab.lib.utils.*;
import eu.essi_lab.messages.bond.*;
import eu.essi_lab.messages.bond.jaxb.*;
import eu.essi_lab.model.exceptions.*;
import eu.essi_lab.profiler.om.scheduling.*;

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

	GridMenuItem<ViewsDescriptor.GridData> gridMenuItem = menu.addItem("Remove view", event -> {

	    Optional<ViewsDescriptor.GridData> eventItem = event.getItem();

	    System.out.println(eventItem.get().getLabel());
	});

	menu.addGridContextMenuOpenedListener(event -> {

	    GridContextMenu<ViewsDescriptor.GridData> source = event.getSource();

	    List<GridMenuItem<ViewsDescriptor.GridData>> menuItems = source.getItems();

	    Optional<ViewsDescriptor.GridData> eventItem = event.getItem();

	    for (GridMenuItem<ViewsDescriptor.GridData> menuItem : menuItems) {

		String menuItemId = menuItem.getId().get();

		if (eventItem.isPresent()) {

		    //		    menuItem.setEnabled(gmih.isEnabled(eventItem.get(), content, configuration, setting.get(), map));

		} else {

		    // if no context is selected, only the non-contextual items are enabled
		    //		    menuItem.setEnabled(!gmih.isContextual());
		}
	    }




	});

	gridMenuItem.setId("remove-view");

	//
	//
	//

	verticalLayout.add(grid);

	//
	//
	//

	TabContentDescriptor descriptor = TabContentDescriptorBuilder.get().//
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
     * @return
     */
    private List<View> getViews() {

	try {

	    DatabaseSetting setting = ConfigurationWrapper.getDatabaseSetting();

	    DatabaseReader reader = DatabaseProviderFactory.getReader(setting.asStorageInfo());

	    GSLoggerFactory.getLogger(ViewsDescriptor.class).info("getViews STARTED");

	    List<View> views = reader.getViews();

	    GSLoggerFactory.getLogger(ViewsDescriptor.class).info("getViews ENDED");

	    return views;

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

		DatabaseSetting setting = ConfigurationWrapper.getDatabaseSetting();

		DatabaseReader reader = DatabaseProviderFactory.getReader(setting.asStorageInfo());

		Optional<View> view = reader.getView(gridData.getId());

		String jsonView = ViewFactory.toJSONObject(view.get()).toString(3);

		TextArea jsonArea = new TextArea();
		jsonArea.getStyle().set("font-size", "13px");
		jsonArea.setHeightFull();
		jsonArea.setWidthFull();
		jsonArea.setValue(jsonView);
		jsonArea.setReadOnly(true);

		String xmlString = ViewFactory.asXMLString(view.get());

		TextArea xmlArea = new TextArea();
		xmlArea.getStyle().set("font-size", "13px");
		xmlArea.setHeightFull();
		xmlArea.setWidthFull();
		xmlArea.setValue(xmlString);
		xmlArea.setReadOnly(true);

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
}
