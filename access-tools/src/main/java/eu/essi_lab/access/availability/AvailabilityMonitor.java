package eu.essi_lab.access.availability;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
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

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.stats.ResponseItem;
import eu.essi_lab.messages.stats.StatisticsMessage;
import eu.essi_lab.messages.stats.StatisticsResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.ResourceProperty;
import eu.essi_lab.request.executor.IStatisticsExecutor;

public class AvailabilityMonitor {

    private static AvailabilityMonitor instance = null;

    private AvailabilityMonitor() {
    }

    public static AvailabilityMonitor getInstance() {
	if (instance == null) {
	    instance = new AvailabilityMonitor();
	}
	return instance;
    }

    private static HashMap<String, Date> lastDownloadDatesbySource = null;
    private static HashMap<String, Date> lastFailedDownloadDatesbySource = null;

    static {

	ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

	Runnable task = new Runnable() {

	    @Override
	    public void run() {
		HashMap<String, Date> good = new HashMap<String, Date>();
		HashMap<String, Date> failed = new HashMap<String, Date>();
		GSLoggerFactory.getLogger(getClass()).info("Running availability monitor task");
		StatisticsMessage statisticsMessage = new StatisticsMessage();
		List<GSSource> allSources = ConfigurationWrapper.getAllSources();
		// set the required properties
		statisticsMessage.setSources(allSources);
		statisticsMessage.setDataBaseURI(ConfigurationWrapper.getDatabaseURI());
		statisticsMessage.groupBy(ResourceProperty.SOURCE_ID);
		Page page = new Page();
		page.setStart(1);
		page.setSize(1000);
		statisticsMessage.setPage(page);
		statisticsMessage.computeMax(Arrays.asList(ResourceProperty.LAST_DOWNLOAD_DATE,ResourceProperty.LAST_FAILED_DOWNLOAD_DATE));
		ServiceLoader<IStatisticsExecutor> loader = ServiceLoader.load(IStatisticsExecutor.class);
		IStatisticsExecutor executor = loader.iterator().next();
		StatisticsResponse response;
		try {
		    response = executor.compute(statisticsMessage);
		    List<ResponseItem> items = response.getItems();
		    for (ResponseItem responseItem : items) {
			String sourceId = responseItem.getGroupedBy().isPresent() ? responseItem.getGroupedBy().get() : null;
			String dateString = responseItem.getMax(ResourceProperty.LAST_DOWNLOAD_DATE).get().getValue();
			if (dateString != null && !dateString.isEmpty()) {
			    Optional<Date> optionalDate = ISO8601DateTimeUtils.parseISO8601ToDate(dateString);
			    if (optionalDate.isPresent()) {
				good.put(sourceId, optionalDate.get());
			    }
			}
			String failedDateString = responseItem.getMax(ResourceProperty.LAST_FAILED_DOWNLOAD_DATE).get().getValue();
			if (failedDateString != null && !failedDateString.isEmpty()) {
			    Optional<Date> optionalDate = ISO8601DateTimeUtils.parseISO8601ToDate(failedDateString);
			    if (optionalDate.isPresent()) {
				failed.put(sourceId, optionalDate.get());
			    }
			}
		    }
		} catch (GSException e) {
		    e.printStackTrace();
		}
		lastDownloadDatesbySource = good;
		lastFailedDownloadDatesbySource = failed;
	    }
	};

	// Schedule the task to run every 5 seconds with an initial delay of 0 seconds
	scheduler.scheduleAtFixedRate(task, 0, 10, TimeUnit.MINUTES);

    }

    public Date getLastDownloadDate(String sourceId) {
	GSLoggerFactory.getLogger(getClass()).info("asked for last download date for source {}",sourceId);
	while (lastDownloadDatesbySource == null) {		
	    try {
		Thread.sleep(1000);
	    } catch (InterruptedException e) {
		e.printStackTrace();
	    }
	}
	return lastDownloadDatesbySource.get(sourceId);
    }
    
    public Date getLastFailedDownloadDate(String sourceId) {
	while (lastFailedDownloadDatesbySource == null) {
	    try {
		Thread.sleep(1000);
	    } catch (InterruptedException e) {
		e.printStackTrace();
	    }
	}
	return lastFailedDownloadDatesbySource.get(sourceId);
    }

}
