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
import com.vaadin.flow.component.button.*;
import com.vaadin.flow.component.grid.*;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.*;
import com.vaadin.flow.component.orderedlayout.*;
import com.vaadin.flow.component.tabs.*;
import com.vaadin.flow.component.textfield.*;
import com.vaadin.flow.component.upload.*;
import com.vaadin.flow.data.renderer.*;
import com.vaadin.flow.server.streams.*;
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

public class ViewsDescriptor extends AbstractGridDescriptor<ViewsDescriptor.GridData> {

    private static final String ID_COLUMN = "Id";
    private static final String LABEL_COLUMN = "Label";
    private static final String CREATOR_COLUMN = "Creator";
    private static final String SOURCE_DEPLOYMENT_COLUMN = "Source Deployment";
    private static final String CREATION_COLUMN = "Creation";

    /**
     *
     */
    public ViewsDescriptor() {

	setLabel("Views");

	getGrid().setSelectionMode(Grid.SelectionMode.MULTI);

	getGrid().addColumn(ViewsDescriptor.GridData::getPosition).//
		setHeader("").//
		setKey("#").//
		setWidth("30px");

	Grid.Column<ViewsDescriptor.GridData> idCol = addSortableResizableColumn(ID_COLUMN, ViewsDescriptor.GridData::getIdentifier, 300);

	Grid.Column<ViewsDescriptor.GridData> labelCol = addSortableResizableColumn(LABEL_COLUMN, ViewsDescriptor.GridData::getLabel, 300);

	Grid.Column<ViewsDescriptor.GridData> depCol = addSortableResizableColumn(SOURCE_DEPLOYMENT_COLUMN,
		ViewsDescriptor.GridData::getDeployment, 300);

	Grid.Column<ViewsDescriptor.GridData> creatorCol = addSortableResizableColumn(CREATOR_COLUMN, ViewsDescriptor.GridData::getCreator,
		200);

	Grid.Column<ViewsDescriptor.GridData> creationCol = addSortableColumn(CREATION_COLUMN, ViewsDescriptor.GridData::getCreationTime,
		150);

	//
	//
	//

	HeaderRow filterRow = getGrid().appendHeaderRow();

	addFilterField(filterRow, creatorCol);
	addFilterField(filterRow, creationCol);
	addFilterField(filterRow, depCol);
	addFilterField(filterRow, idCol);
	addFilterField(filterRow, labelCol);

	//
	//
	//

	addContextMenu(selectionMap -> {

	    try {

		List<String> ids = selectionMap.keySet().stream().filter(selectionMap::get).toList();

		for (String id : ids) {

		    getWriter().removeView(id);
		}

		update(getVerticalLayout());

		NotificationDialog.getInfoDialog(ids.size() == 1 ? "View correctly removed" : "Views correctly removed").open();

	    } catch (Exception e) {

		GSLoggerFactory.getLogger(getClass()).error(e);

		NotificationDialog.getErrorDialog("Error occurred, unable to remove views: " + e.getMessage(), 700).open();
	    }
	});

	//
	//
	//

	String desc = "Click 'Reload' to update the list of stored views.\n\nViews are provided both with JSON and XML encoding.\n\n";
	desc += "To edit a view click the toggle button to enable the 'Editable' mode. When done, click the button beside to ";
	desc += "update the view in the database, and click the toggle button again to enable the 'Read-only' mode.\n\n";
	desc += "To remove one or more views, use the checkbox to select and click the mouse right button to open the contextual ";
	desc += "menu, than click 'Remove selected views'";

	TabContentDescriptor descriptor = TabContentDescriptorBuilder.get().//
		withCustomAddDirective(e -> openAddViewDialog()).//
		withShowDirective(desc, false).//
		withComponent(getVerticalLayout()).//
		reloadable(() -> update(getVerticalLayout())).//
		build();

	setIndex(GSTabIndex.VIEWS.getIndex());
	addContentDescriptor(descriptor);

	//
	//
	//

	update(getVerticalLayout());
    }

    @Override
    protected Class<GridData> getGridDataModel() {

	return GridData.class;
    }

    /**
     *
     */
    private void openAddViewDialog() {

	final EnhancedDialog dialog = new EnhancedDialog();
	dialog.setHeight(200, Unit.PIXELS);
	dialog.setHeader("Add view");
	dialog.open();

	Upload upload = new Upload();

	InMemoryUploadHandler handler = UploadHandler.inMemory((metadata, bytes) -> {

	    try {

		String mimeType = metadata.contentType();

		InputStream viewStream = new ByteArrayInputStream(bytes);

		View view = mimeType.equals("application/json")
			? ViewFactory.fromJSONStream(viewStream)
			: ViewFactory.fromXMLStream(viewStream);

		if (getReader().getView(view.getId()).isPresent()) {

		    NotificationDialog.getErrorDialog("A view with id '" + view.getId() + "' already exists").open();

		    upload.clearFileList();
		    return;
		}

		getWriter().store(view);

		update(getVerticalLayout());

		dialog.close();

		NotificationDialog.getInfoDialog("View correctly added").open();

	    } catch (Exception ex) {

		upload.clearFileList();

		GSLoggerFactory.getLogger(getClass()).error(ex);

		NotificationDialog.getErrorDialog("Error occurred, unable to add view: " + ex.getMessage()).open();

		upload.interruptUpload();
	    }
	});

	upload.setUploadHandler(handler);

	upload.setMaxFiles(1);
	upload.setDropAllowed(true);
	upload.setAcceptedFileTypes("application/json", ".json", "application/xml", ".xml");
	upload.setDropLabel(ComponentFactory.createSpan("Drop file here"));

	Button localUploadButton = new Button("Select view file (JSON or XML)");
	localUploadButton.getStyle().set("font-size", "14px");
	localUploadButton.setWidth("330px");
	localUploadButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

	upload.setUploadButton(localUploadButton);

	dialog.setContent(upload);
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
     * @return
     */
    @Override
    protected List<GridData> getItems() {

	return getViews(). //
		stream().//
		map(v -> new GridData(v, getGrid())).//
		sorted((v1, v2) -> v2.getIdentifier().compareTo(v1.getIdentifier())).//
		toList();
    }

    /**
     * @param configuration
     * @param readOnly
     * @param container
     * @return
     */
    @Override
    protected Optional<Renderer<GridData>> createItemDetailsRenderer() {

	return Optional.of(new ComponentRenderer<>((gridData) -> {

	    try {

		View view = getReader().getView(gridData.getIdentifier()).get();

		TextArea jsonArea = createTextArea(ViewFactory.toJSONObject(view).toString(3));
		TextArea xmlArea = createTextArea(ViewFactory.toXMLString(view));

		CopyToClipboardButton copyButton = ComponentFactory.createCopyToClipboardButton(jsonArea::getValue);
		copyButton.getStyle().set("margin-top", "-5px");
		copyButton.getStyle().set("margin-bottom", "-10px");
		copyButton.getStyle().set("margin-left", "15px");

		TabSheet tabSheet = new TabSheet();
		tabSheet.getStyle().set("font-size", "13px");
		tabSheet.setHeightFull();
		tabSheet.setWidthFull();
		tabSheet.add("JSON encoding", jsonArea);
		tabSheet.add("XML encoding", xmlArea);
		tabSheet.addSelectedChangeListener(
			evt -> copyButton.setSupplier(tabSheet.getSelectedIndex() == 0 ? jsonArea::getValue : xmlArea::getValue));

		VerticalLayout layout = ComponentFactory.createNoSpacingNoMarginVerticalLayout();

		layout.getStyle().set("margin-top", "-20px");
		layout.add(tabSheet);

		HorizontalLayout buttonsLayout = ComponentFactory.createNoSpacingNoMarginHorizontalLayout();
		layout.add(buttonsLayout);

		Span readOnlyLabel = ComponentFactory.createSpan("Read-only", 13);
		readOnlyLabel.getStyle().set("margin-left", "16px");
		readOnlyLabel.getStyle().set("margin-top", "8px");
		readOnlyLabel.setWidth("65px");

		Switch switch_ = ComponentFactory.createSwitch(false, true);
		switch_.setTooltipText("Edit view");

		Button updateButton = new Button(VaadinIcon.UPLOAD.create());
		updateButton.getStyle().set("margin-top", "-5px");
		updateButton.setTooltipText("Update view");
		updateButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
		updateButton.getStyle().set("margin-left", "5px");
		updateButton.getStyle().set("border", "1px solid hsl(0deg 0% 81%)");
		updateButton.getStyle().set("border-radius", "0px");
		updateButton.setEnabled(false);
		updateButton.addClickListener(evt -> {

		    View modified = null;

		    try {
			switch (tabSheet.getSelectedIndex()) {
			case 0 -> modified = ViewFactory.fromJSONObject(jsonArea.getValue());
			case 1 -> modified = ViewFactory.fromXMLString(xmlArea.getValue());
			}

			getWriter().store(modified);

			NotificationDialog.getInfoDialog("View correctly updated").open();

			updateButton.setEnabled(false);

			switch_.setValue(false);

			readOnlyLabel.setText("Read-only");

			update(getVerticalLayout());

		    } catch (Exception ex) {

			NotificationDialog.getErrorDialog("Error occurred, unable to update view: " + ex.getMessage()).open();

			GSLoggerFactory.getLogger(getClass()).error(ex);
		    }
		});

		switch_.addValueChangeListener(evt -> {

		    readOnlyLabel.setText(evt.getValue() ? "Editable" : "Read-only");
		    updateButton.setEnabled(evt.getValue());

		    switch (tabSheet.getSelectedIndex()) {
		    case 0 -> {

			jsonArea.setReadOnly(!evt.getValue());

			if (evt.getValue()) { // JSON editing

			    jsonArea.removeClassName("text-area-readonly");
			    jsonArea.addClassName("text-area-default");

			    tabSheet.getTabAt(1).setEnabled(false);

			} else { // JSON readonly

			    jsonArea.addClassName("text-area-readonly");
			    jsonArea.removeClassName("text-area-default");

			    tabSheet.getTabAt(1).setEnabled(true);
			}
		    }

		    case 1 -> {

			xmlArea.setReadOnly(!evt.getValue());

			if (evt.getValue()) { // XML editing

			    xmlArea.removeClassName("text-area-readonly");
			    xmlArea.addClassName("text-area-default");

			    tabSheet.getTabAt(0).setEnabled(false);

			} else { // XML readonly

			    xmlArea.addClassName("text-area-readonly");
			    xmlArea.removeClassName("text-area-default");

			    tabSheet.getTabAt(0).setEnabled(true);
			}
		    }
		    }
		});

		buttonsLayout.add(readOnlyLabel);
		buttonsLayout.add(switch_);
		buttonsLayout.add(updateButton);

		buttonsLayout.add(copyButton);

		return layout;

	    } catch (Exception e) {

		GSLoggerFactory.getLogger(ViewsDescriptor.class).error("Error occurred: {}", e.getMessage(), e);
	    }

	    return null;
	}));
    }

    /**
     * @return
     */
    private TextArea createTextArea(String string) {

	TextArea area = new TextArea();
	area.getStyle().set("font-size", "13px");
	area.getStyle().set("vertical-overflow", "auto");
	area.addClassName("text-area-readonly");
	area.setHeightFull();
	area.setWidthFull();
	area.setReadOnly(true);
	area.setMaxHeight(500, Unit.PIXELS);
	area.setValue(string);

	return area;
    }

    /**
     * @author Fabrizio
     */
    protected static class GridFilter extends AbstractGridFilter<GridData> {

	@Override
	protected String getItemValue(String colum, GridData gridData) {

	    return switch (colum) {
		case CREATOR_COLUMN -> gridData.getCreator();
		case ID_COLUMN -> gridData.getIdentifier();
		case LABEL_COLUMN -> gridData.getLabel();
		case SOURCE_DEPLOYMENT_COLUMN -> gridData.getDeployment();
		case CREATION_COLUMN -> gridData.getCreationTime();
		default -> null;
	    };
	}
    }

    @Override
    protected GridFilter createGridFilter() {

	return new GridFilter();
    }

    /**
     * @author Fabrizio
     */
    protected static class GridData implements GridDataModel {

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
			    stream().map(GridData::getIdentifier).toList().//
			    indexOf(view.getId()) + 1);
	}

	@Override
	public String getIdentifier() {

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

	    return getIdentifier() + ":" + getLabel();
	}

	@Override
	public boolean equals(Object o) {

	    return o instanceof ViewsDescriptor.GridData && ((ViewsDescriptor.GridData) o).getIdentifier().equals(this.getIdentifier());
	}
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
