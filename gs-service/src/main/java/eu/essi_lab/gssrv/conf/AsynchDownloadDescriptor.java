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
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.*;
import com.vaadin.flow.component.orderedlayout.*;
import com.vaadin.flow.component.textfield.*;
import com.vaadin.flow.data.provider.*;
import com.vaadin.flow.data.renderer.*;
import com.vaadin.flow.data.value.*;
import eu.essi_lab.api.database.*;
import eu.essi_lab.api.database.factory.*;
import eu.essi_lab.cfga.gs.*;
import eu.essi_lab.cfga.gs.setting.harvesting.*;
import eu.essi_lab.cfga.gui.components.*;
import eu.essi_lab.cfga.gui.components.grid.renderer.*;
import eu.essi_lab.cfga.gui.components.tabs.descriptor.*;
import eu.essi_lab.cfga.option.*;
import eu.essi_lab.cfga.scheduler.*;
import eu.essi_lab.cfga.setting.*;
import eu.essi_lab.cfga.setting.scheduling.*;
import eu.essi_lab.lib.utils.*;
import eu.essi_lab.model.*;
import eu.essi_lab.model.exceptions.*;
import eu.essi_lab.profiler.om.scheduling.*;

import java.sql.*;
import java.util.*;
import java.util.Date;
import java.util.stream.*;

/**
 * @author Fabrizio
 */
public class AsynchDownloadDescriptor extends TabDescriptor {

    private final VerticalLayout verticalLayout;
    private final Grid<GridData> grid;
    private final GridFilter gridFilter;

    private static final String DOWNLOAD_NAME_COLUMN = "Download name";
    private static final String EMAIL_COLUMN = "E-mail";
    private static final String BUCKET_COLUMN = "Bucket";
    private static final String MAX_DOWNLOAD_SIZE_COLUMN = "Max. download size";
    private static final String MAX_DOWNLOAD_PART_SIZE_COLUMN = "Max. download part. size";
    private static final String REQUEST_URL_COLUMN = "Request URL";
    private static final String OPERATION_ID_COLUMN = "Operation ID";
    private static final String EMAIL_NOTIFICATIONS_COLUMN = "E-mail notifications";
    private static final String STATUS_COLUMN = "Status";
    private static final String HOST_COLUMN = "Host";

    /**
     *
     */
    public AsynchDownloadDescriptor() {

	setLabel("Downloads");

	verticalLayout = new VerticalLayout();
	verticalLayout.getStyle().set("margin-top", "-5px");

	verticalLayout.setWidthFull();
	verticalLayout.setHeightFull();

	//
	//
	//

	grid = new Grid<>(GridData.class, false);

	grid.getStyle().set("font-size", "13px");

	Grid.Column<GridData> downalodNameColumn = grid.addColumn(GridData::getDownloadName).//
		setHeader(DOWNLOAD_NAME_COLUMN).//
		setKey(DOWNLOAD_NAME_COLUMN).//
		setWidth("70px").//
		setSortable(true).//
		setResizable(true);//

	Grid.Column<GridData> emailColumn = grid.addColumn(GridData::getEmail).//
		setHeader(EMAIL_COLUMN).//
		setKey(EMAIL_COLUMN).//
		setWidth("100px").//
		setSortable(true).//
		setResizable(true);//

	Grid.Column<GridData> reqURLColumn = grid.addColumn(GridData::getRequestURL).//
		setHeader(REQUEST_URL_COLUMN).//
		setKey(REQUEST_URL_COLUMN).//
		setWidth("350px").//
		setFlexGrow(0).//
		setSortable(true).//
		setResizable(true);//

	Grid.Column<GridData> statusColumn = grid.addColumn(GridData::getStatus).//
		setHeader(STATUS_COLUMN).//
		setKey(STATUS_COLUMN).//
		setWidth("70px").//
		setFlexGrow(0).//
		setSortable(true);//

	Grid.Column<GridData> hostColumn = grid.addColumn(GridData::getHost).//
		setHeader(HOST_COLUMN).//
		setKey(HOST_COLUMN).//
		setWidth("110px").//
		setFlexGrow(0).//
		setSortable(true).//
		setResizable(true);//

	Grid.Column<GridData> opIdColumn = grid.addColumn(GridData::getOperationID).//
		setHeader(OPERATION_ID_COLUMN).//
		setKey(OPERATION_ID_COLUMN).//
		setWidth("200px").//
		setFlexGrow(0).//
		setSortable(true).//
		setResizable(true);//

	Grid.Column<GridData> bucketColumn = grid.addColumn(GridData::getBucket).//
		setHeader(BUCKET_COLUMN).//
		setKey(BUCKET_COLUMN).//
		setWidth("100px").//
		setFlexGrow(0).//
		setSortable(true);

	Grid.Column<GridData> maxDownloadSizeColumn = grid.addColumn(GridData::getMaxDownloadSize).//
		setHeader(MAX_DOWNLOAD_SIZE_COLUMN).//
		setKey(MAX_DOWNLOAD_SIZE_COLUMN).//
		setWidth("100px").//
		setFlexGrow(0).//
		setSortable(true);

	Grid.Column<GridData> maxDownloadPartSizeColumn = grid.addColumn(GridData::getMaxDownloadPartSize).//
		setHeader(MAX_DOWNLOAD_PART_SIZE_COLUMN).//
		setKey(MAX_DOWNLOAD_PART_SIZE_COLUMN).//
		setWidth("100px").//
		setFlexGrow(0).//
		setSortable(true);

	Grid.Column<GridData> emailNotificationsColumn = grid.addColumn(GridData::getEmailNotifications).//
		setHeader(EMAIL_NOTIFICATIONS_COLUMN).//
		setKey(EMAIL_NOTIFICATIONS_COLUMN).//
		setWidth("110px").//
		setFlexGrow(0).//
		setSortable(true);

	//
	//
	//

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

	gridFilter = new GridFilter();

	addFilterField(filterRow, hostColumn);
	addFilterField(filterRow, statusColumn);
	addFilterField(filterRow, bucketColumn);
	addFilterField(filterRow, maxDownloadSizeColumn);
	addFilterField(filterRow, reqURLColumn);
	addFilterField(filterRow, opIdColumn);
	addFilterField(filterRow, maxDownloadPartSizeColumn);
	addFilterField(filterRow, emailNotificationsColumn);
	addFilterField(filterRow, downalodNameColumn);
	addFilterField(filterRow, emailColumn);

	//
	//
	//

	verticalLayout.add(grid);

	//
	//
	//

	TabContentDescriptor descriptor = TabContentDescriptorBuilder.get().//
		withShowDirective("Click 'Reload' to show the list of asynchronous downloads (running and waiting)", false).//
		withComponent(verticalLayout).//
		reloadable(() -> update(verticalLayout)).//
		build();

	setIndex(GSTabIndex.ASYNC_DOWNLOADS.getIndex());
	addContentDescriptor(descriptor);

	//
	//
	//

	update(verticalLayout);
    }

    /**
     * @return
     */
    private List<OMSchedulerSetting> getSettings() {

	List<OMSchedulerSetting> settings = new ArrayList<>();

	Scheduler scheduler = SchedulerFactory.getScheduler(ConfigurationWrapper.getSchedulerSetting());

	try {

	    List<OMSchedulerSetting> exec = scheduler.//
		    listExecutingSettings().//
		    stream().//
		    filter(s -> s.getGroup() == SchedulerWorkerSetting.SchedulingGroup.ASYNCH_ACCESS).//
		    map(s -> SettingUtils.downCast(s, OMSchedulerSetting.class)).//
		    peek(s -> s.setFiredTime(new Date())).//
		    toList();

	    settings = scheduler.//
		    listScheduledSettings().//
		    stream().//
		    filter(s -> s.getGroup() == SchedulerWorkerSetting.SchedulingGroup.ASYNCH_ACCESS).//
		    filter(s -> !exec.stream().map(Setting::getIdentifier).toList().contains(s.getIdentifier())).//
		    map(s -> SettingUtils.downCast(s, OMSchedulerSetting.class)).//
		    collect(Collectors.toList());

	    settings.addAll(exec);

	    //
	    //
	    //

	    List<SchedulerJobStatus> statusList = scheduler.getJobStatuslist();

	    settings.forEach(setting -> {

		Optional<SchedulerJobStatus> jobStatus = getJobStatus(statusList, setting);

		jobStatus.ifPresent(status -> {

		    setting.getObject().put("status", status.getPhase().getLabel());
		    setting.getObject().put("host", status.getHostName().orElse(""));
		});

	    });

	} catch (Exception e) {

	    settings = new ArrayList<>();

	    GSLoggerFactory.getLogger(HarvestingSetting.class).error("Error occurred: {}", e.getMessage(), e);
	}

	return settings;
    }

    /**
     * @param setting
     * @return
     */
    private synchronized Optional<SchedulerJobStatus> getJobStatus(List<SchedulerJobStatus> statusList, Setting setting) {

	return statusList.//
		stream().//
		filter(s -> s.getSettingId().equals(setting.getIdentifier())).//
		findFirst();

    }

    /**
     * @param verticalLayout
     * @return
     */
    private void update(VerticalLayout verticalLayout) {

	List<GridData> list = getSettings(). //
		stream().//
		map(GridData::new).//
		sorted((sd1, sd2) -> sd2.getEmail().compareTo(sd1.getEmail())).//
		collect(Collectors.toList());

	grid.setItems(list);

	@SuppressWarnings("unchecked")
	ListDataProvider<AsynchDownloadDescriptor.GridData> dataProvider = (ListDataProvider<GridData>) grid.getDataProvider();

	dataProvider.setFilter(gridFilter::test);
    }

    /**
     * @param filterRow
     * @param column
     */
    private void addFilterField(HeaderRow filterRow, Grid.Column<AsynchDownloadDescriptor.GridData> column) {

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
	public boolean test(GridData gridData) {

	    boolean match = true;

	    for (String colum : SELECTION_MAP.keySet()) {

		String itemValue = switch (colum) {
		    case BUCKET_COLUMN -> gridData.getBucket();
		    case DOWNLOAD_NAME_COLUMN -> gridData.getDownloadName();
		    case EMAIL_COLUMN -> gridData.getEmail();
		    case EMAIL_NOTIFICATIONS_COLUMN -> gridData.getEmailNotifications();
		    case HOST_COLUMN -> gridData.getHost();
		    case MAX_DOWNLOAD_PART_SIZE_COLUMN -> gridData.getMaxDownloadPartSize();
		    case MAX_DOWNLOAD_SIZE_COLUMN -> gridData.getMaxDownloadSize();
		    case OPERATION_ID_COLUMN -> gridData.getOperationID();
		    case REQUEST_URL_COLUMN -> gridData.getRequestURL();
		    case STATUS_COLUMN -> gridData.getStatus();
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

	private final OMSchedulerSetting setting;

	/**
	 * @param setting
	 */
	private GridData(OMSchedulerSetting setting) {

	    this.setting = setting;
	}

	/**
	 * @return
	 */
	public String getDownloadName() {

	    return Optional.ofNullable(setting.getAsynchDownloadName()).orElse("");
	}

	/**
	 * @return
	 */
	public String getEmail() {

	    return Optional.ofNullable(setting.getEmail()).orElse("");
	}

	/**
	 * @return the size
	 */
	public String getBucket() {

	    return Optional.ofNullable(setting.getBucket()).orElse("");
	}

	/**
	 * @return
	 */
	public String getMaxDownloadSize() {

	    String def = Optional.ofNullable(ConfigurationWrapper.getDefaultMaxDownloadSizeMB()).map(String::valueOf).orElse("");

	    return Optional.ofNullable(setting.getMaxDownloadSizeMB()).map(String::valueOf).orElse(def);

	}

	/**
	 * @return
	 */
	public String getMaxDownloadPartSize() {

	    String def = Optional.ofNullable(ConfigurationWrapper.getDefaultMaxDownloadPartSizeMB()).map(String::valueOf).orElse("");

	    return Optional.ofNullable(setting.getMaxDownloadPartSizeMB()).map(String::valueOf).orElse(def);
	}

	/**
	 * @return
	 */
	public String getRequestURL() {

	    return Optional.ofNullable(setting.getRequestURL()).orElse("");
	}

	/**
	 * @return
	 */
	public String getStatus() {

	    return setting.getObject().optString("status", "Waiting");
	}

	/**
	 * @return
	 */
	public String getHost() {

	    return setting.getObject().optString("host", "-");
	}

	/**
	 * @return
	 */
	public String getOperationID() {

	    return Optional.ofNullable(setting.getOperationId()).orElse("");
	}

	/**
	 * @return
	 */
	public String getEmailNotifications() {

	    return Optional.ofNullable(setting.getEmailNotifications()).map(v -> v.equals("true") ? "yes" : "no").orElse("");
	}

	@Override
	public String toString() {

	    return getDownloadName() + ":" + getEmail();
	}

	@Override
	public boolean equals(Object o) {

	    return o instanceof GridData && ((GridData) o).getOperationID().equals(this.getOperationID());
	}
    }
}
