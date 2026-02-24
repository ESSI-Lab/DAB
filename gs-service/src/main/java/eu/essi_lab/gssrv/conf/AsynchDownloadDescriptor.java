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

import com.vaadin.flow.component.grid.*;
import eu.essi_lab.cfga.gs.*;
import eu.essi_lab.cfga.gs.setting.harvesting.*;
import eu.essi_lab.cfga.gui.components.tabs.descriptor.*;
import eu.essi_lab.cfga.gui.dialog.*;
import eu.essi_lab.cfga.scheduler.*;
import eu.essi_lab.cfga.scheduler.Scheduler;
import eu.essi_lab.cfga.scheduler.SchedulerFactory;
import eu.essi_lab.cfga.setting.*;
import eu.essi_lab.cfga.setting.scheduling.*;
import eu.essi_lab.lib.utils.*;
import eu.essi_lab.profiler.om.scheduling.*;

import java.util.*;
import java.util.Date;
import java.util.stream.*;

/**
 * @author Fabrizio
 */
public class AsynchDownloadDescriptor extends AbstractGridDescriptor<AsynchDownloadDescriptor.GridData> {

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

	getGrid().setSelectionMode(Grid.SelectionMode.MULTI);

	Grid.Column<GridData> downloadNameColumn = addSortableResizableColumn(DOWNLOAD_NAME_COLUMN, GridData::getDownloadName, 200);

	Grid.Column<GridData> emailColumn = addSortableResizableColumn(EMAIL_COLUMN, GridData::getEmail, 250);

	Grid.Column<GridData> reqURLColumn = addSortableResizableColumn(REQUEST_URL_COLUMN, GridData::getRequestURL, 550);

	Grid.Column<GridData> statusColumn = addSortableColumn(STATUS_COLUMN, GridData::getStatus, 110);

	Grid.Column<GridData> hostColumn = addSortableResizableColumn(HOST_COLUMN, GridData::getHost, 110);

	Grid.Column<GridData> opIdColumn = addSortableResizableColumn(OPERATION_ID_COLUMN, GridData::getIdentifier, 300);

	Grid.Column<GridData> bucketColumn = addSortableColumn(BUCKET_COLUMN, GridData::getBucket, 100);

	Grid.Column<GridData> maxDownloadSizeColumn = addSortableColumn(MAX_DOWNLOAD_SIZE_COLUMN, GridData::getMaxDownloadSize, 100);

	Grid.Column<GridData> maxDownloadPartSizeColumn = addSortableColumn(MAX_DOWNLOAD_PART_SIZE_COLUMN, GridData::getMaxDownloadPartSize,
		100);

	Grid.Column<GridData> emailNotificationsColumn = addSortableColumn(EMAIL_NOTIFICATIONS_COLUMN, GridData::getEmailNotifications,
		110);

	//
	//
	//

	HeaderRow filterRow = getGrid().appendHeaderRow();

	addFilterField(filterRow, hostColumn);
	addFilterField(filterRow, statusColumn);
	addFilterField(filterRow, bucketColumn);
	addFilterField(filterRow, maxDownloadSizeColumn);
	addFilterField(filterRow, reqURLColumn);
	addFilterField(filterRow, opIdColumn);
	addFilterField(filterRow, maxDownloadPartSizeColumn);
	addFilterField(filterRow, emailNotificationsColumn);
	addFilterField(filterRow, downloadNameColumn);
	addFilterField(filterRow, emailColumn);

	//
	//
	//

	//
	//

	addContextMenu(selectionMap -> {

	    try {

		List<String> ids = selectionMap.keySet().stream().filter(selectionMap::get).toList();

		SchedulerSetting schedulerSetting = ConfigurationWrapper.getSchedulerSetting();

		Scheduler scheduler = SchedulerFactory.getScheduler(schedulerSetting);

		List<OMSchedulerSetting> list = getSettings().stream(). //
			filter(set -> ids.contains(set.getOperationId())). //
			toList();//

		for (OMSchedulerSetting setting : list) {

		    scheduler.unschedule(setting);
		}

		update(getVerticalLayout());

		NotificationDialog.getInfoDialog(ids.size() == 1 ? "Item correctly removed" : "Items correctly removed").open();

	    } catch (Exception e) {

		GSLoggerFactory.getLogger(getClass()).error(e);

		NotificationDialog.getErrorDialog("Error occurred, unable to remove selected items: " + e.getMessage(), 700).open();
	    }
	});

	//
	//
	//

	TabContentDescriptor descriptor = TabContentDescriptorBuilder.get().//
		withShowDirective("Click 'Reload' to show the list of asynchronous downloads (running and waiting)", false).//
		withComponent(getVerticalLayout()).//
		reloadable(() -> update(getVerticalLayout())).//
		build();

	setIndex(GSTabIndex.ASYNC_DOWNLOADS.getIndex());
	addContentDescriptor(descriptor);

	//
	//
	//

	update(getVerticalLayout());
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
     * @return
     */
    @Override
    protected List<GridData> getItems() {

	return getSettings(). //
		stream().//
		map(GridData::new).//
		sorted((sd1, sd2) -> sd2.getEmail().compareTo(sd1.getEmail())).//
		collect(Collectors.toList());

    }

    /**
     * @author Fabrizio
     */
    public static class GridFilter extends AbstractGridFilter<GridData> {

	@Override
	protected String getItemValue(String colum, GridData gridData) {

	    return switch (colum) {
		case BUCKET_COLUMN -> gridData.getBucket();
		case DOWNLOAD_NAME_COLUMN -> gridData.getDownloadName();
		case EMAIL_COLUMN -> gridData.getEmail();
		case EMAIL_NOTIFICATIONS_COLUMN -> gridData.getEmailNotifications();
		case HOST_COLUMN -> gridData.getHost();
		case MAX_DOWNLOAD_PART_SIZE_COLUMN -> gridData.getMaxDownloadPartSize();
		case MAX_DOWNLOAD_SIZE_COLUMN -> gridData.getMaxDownloadSize();
		case OPERATION_ID_COLUMN -> gridData.getIdentifier();
		case REQUEST_URL_COLUMN -> gridData.getRequestURL();
		case STATUS_COLUMN -> gridData.getStatus();
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
    public static class GridData implements GridDataModel {

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
	@Override
	public String getIdentifier() {

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

	    return o instanceof GridData && ((GridData) o).getIdentifier().equals(this.getIdentifier());
	}
    }
}
