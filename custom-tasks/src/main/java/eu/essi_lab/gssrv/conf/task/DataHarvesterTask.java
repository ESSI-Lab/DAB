package eu.essi_lab.gssrv.conf.task;

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

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.quartz.JobExecutionContext;

import eu.essi_lab.access.augmenter.DataCacheAugmenter;
import eu.essi_lab.access.datacache.DataCacheConnector;
import eu.essi_lab.access.datacache.DataCacheConnectorFactory;
import eu.essi_lab.access.datacache.StatisticsRecord;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.setting.dc_connector.DataCacheConnectorSetting;
import eu.essi_lab.cfga.gs.task.AbstractCustomTask;
import eu.essi_lab.cfga.gs.task.OptionsKey;
import eu.essi_lab.cfga.scheduler.SchedulerJobStatus;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.TaskListExecutor;
import eu.essi_lab.messages.JobStatus.JobPhase;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.pdk.rsm.access.AccessQueryUtils;

/**
 * @author boldrini
 */
public class DataHarvesterTask extends AbstractCustomTask {

    public enum DataHarvesterTaskOptions implements OptionsKey {
	SOURCE_ID, THREADS_COUNT, MAX_RECORDS;
    }

    @Override
    public void doJob(JobExecutionContext context, SchedulerJobStatus status) throws Exception {

	log(status, "Data harvester task STARTED");

	// INIT CACHE CONNECTOR

	DataCacheConnector dataCacheConnector = DataCacheConnectorFactory.getDataCacheConnector();

	if (dataCacheConnector == null) {
	    DataCacheConnectorSetting setting = ConfigurationWrapper.getDataCacheConnectorSetting();
	    dataCacheConnector = DataCacheConnectorFactory.newDataCacheConnector(setting);
	    if (dataCacheConnector == null) {
		GSLoggerFactory.getLogger(getClass()).error("Issues initializing the data cache connector");
		return;
	    }
	    String cachedDays = setting.getOptionValue(DataCacheConnector.CACHED_DAYS).get();
	    String flushInterval = setting.getOptionValue(DataCacheConnector.FLUSH_INTERVAL_MS).get();
	    String maxBulkSize = setting.getOptionValue(DataCacheConnector.MAX_BULK_SIZE).get();
	    dataCacheConnector.configure(DataCacheConnector.MAX_BULK_SIZE, maxBulkSize);
	    dataCacheConnector.configure(DataCacheConnector.FLUSH_INTERVAL_MS, flushInterval);
	    dataCacheConnector.configure(DataCacheConnector.CACHED_DAYS, cachedDays);
	    DataCacheConnectorFactory.setDataCacheConnector(dataCacheConnector);
	}

	// SETTINGS RETRIEVAL

	Optional<EnumMap<DataHarvesterTaskOptions, String>> taskOptions = readTaskOptions(context, DataHarvesterTaskOptions.class);
	if (taskOptions.isEmpty()) {
	    GSLoggerFactory.getLogger(getClass()).error("No options specified");
	    return;
	}
	String sourceId = taskOptions.get().get(DataHarvesterTaskOptions.SOURCE_ID);
	if (sourceId == null) {
	    GSLoggerFactory.getLogger(getClass()).error("No source id option specified");
	    return;
	}
	String threadsCountString = taskOptions.get().get(DataHarvesterTaskOptions.THREADS_COUNT);
	Integer threadsCount = 1;
	if (threadsCountString != null) {
	    threadsCount = Integer.parseInt(threadsCountString);
	}

	String maxRecordsString = taskOptions.get().get(DataHarvesterTaskOptions.MAX_RECORDS);
	Integer maxRecords = null;
	if (maxRecordsString != null) {
	    maxRecords = Integer.parseInt(maxRecordsString);
	}
	
	int recordsDone = 0;

	while (true) {

	    // CHECKING CANCELED JOB

	    if (ConfigurationWrapper.isJobCanceled(context)) {
		GSLoggerFactory.getLogger(getClass()).info("Data harvester task CANCELED source id {} ", sourceId);

		status.setPhase(JobPhase.CANCELED);
		break;
	    }

	    List<SimpleEntry<String, Date>> nextExpectedRecords = dataCacheConnector.getExpectedAvailableRecords(sourceId);

	    GSLoggerFactory.getLogger(getClass()).info("Data harvester task STARTED source id {} threads {} expected available records {}",
		    sourceId, threadsCount, nextExpectedRecords.size());

	    TaskListExecutor<Boolean> taskList = new TaskListExecutor<>(threadsCount);

	    if (nextExpectedRecords.isEmpty()) {
		SimpleEntry<String, Date> nextRecord = dataCacheConnector.getNextExpectedRecord(sourceId);
		if (nextRecord == null) {
		    Thread.sleep(1000);
		    GSLoggerFactory.getLogger(getClass()).error("[DATA-CACHE] Source {} No next records", sourceId);
		    continue;
		}
		Date expectedDate = nextRecord.getValue();
		GSLoggerFactory.getLogger(getClass()).info("[DATA-CACHE] Source {} Sleeping until {}", sourceId, expectedDate);
		while (new Date().before(expectedDate)) {
		    try {
			Thread.sleep(5000);
		    } catch (Exception e) {
			e.printStackTrace();
		    }
		}
		GSLoggerFactory.getLogger(getClass()).info("[DATA-CACHE] Source {} Slept until {}", sourceId, expectedDate);
	    }

	    for (int i = 0; i < nextExpectedRecords.size(); i++) {

		
		final int f = i;
		final DataCacheConnector dcc = dataCacheConnector;
		final String fid = sourceId;

		taskList.addTask(new Callable<Boolean>() {

		    @Override
		    public Boolean call() throws Exception {
			String onlineId = null;
			Date expectedDate = null;
			try {
			    SimpleEntry<String, Date> nextExpectedRecord = nextExpectedRecords.get(f);
			    onlineId = nextExpectedRecord.getKey();
			    // if (!onlineId.equals("C767E059DB53440EFCFDC329C43AE834C2C4399B")) {
			    // return false;
			    // }
			    GSLoggerFactory.getLogger(getClass()).info("[DATA-CACHE] Augmenting expected available record {} {}/{}",
				    onlineId, (f + 1), nextExpectedRecords.size());
			    expectedDate = nextExpectedRecord.getValue();
			    if (onlineId.startsWith("urn")) {
				dcc.deleteBefore(null, fid, onlineId);
			    }
			    ResultSet<GSResource> resultSet = AccessQueryUtils.findResource(onlineId, Optional.empty(), onlineId);
			    if (!resultSet.getResultsList().isEmpty()) {
				if (resultSet.getResultsList().size() > 1) {
				    GSLoggerFactory.getLogger(getClass())
					    .warn("[DATA-CACHE] More than one resource for the same online identifier {}", onlineId);
				}
				DataCacheAugmenter dca = new DataCacheAugmenter();				
				dca.augment(resultSet.getResultsList().get(0));
				
			    }
			} catch (Exception e) {
			    e.printStackTrace();
			    if (dcc != null) {
				try {
				    dcc.writeStatistics(new StatisticsRecord(new Date(), fid, onlineId, 0, "DHT-" + e.getMessage(),
					    expectedDate, null));
				} catch (Exception e1) {
				    e1.printStackTrace();
				}
			    }

			}
			return true;
		    }
		});
		recordsDone++;
		if (maxRecords!=null && recordsDone>=maxRecords) {
		    break;
		}

	    }

	    List<Future<Boolean>> futures = taskList.executeAndWait();

	    Integer success = 0;
	    Integer cancelled = 0;
	    Integer interrupted = 0;
	    List<Throwable> exceptions = new ArrayList<>();

	    for (int i = 0; i < futures.size(); i++) {

		Future<Boolean> future = futures.get(i);

		try {
		    future.get();
		    success++;
		} catch (CancellationException ex) {
		    cancelled++;
		} catch (InterruptedException ie) {
		    interrupted++;
		} catch (ExecutionException ee) {

		    GSLoggerFactory.getLogger(getClass()).error(ee.getMessage(), ee);

		    Throwable cause = ee.getCause();
		    exceptions.add(cause);

		}
	    }
	    GSLoggerFactory.getLogger(getClass()).info(
		    "[DATA-CACHE] harvest task ended total {} success {} cancelled {} interrupted {} exceptions {}", futures.size(),
		    success, cancelled, interrupted, exceptions.size());

	    for (Throwable throwable : exceptions) {
		GSLoggerFactory.getLogger(getClass()).error("[DATA-CACHE] {}", throwable.getMessage());
		throwable.printStackTrace();
	    }

	}

	log(status, "Data harvester task ENDED");
    }

    @Override
    public String getName() {

	return "Data harvester task";
    }
}
