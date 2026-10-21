package eu.essi_lab.gssrv.conf.task;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Technologies and Environmental Intelligence (ITIAm)/ESSI-Lab
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

import java.util.EnumMap;
import java.util.List;
import java.util.Optional;

import org.quartz.JobExecutionContext;

import eu.essi_lab.access.datacache.DataCacheConnector;
import eu.essi_lab.access.datacache.DataCacheConnectorFactory;
import eu.essi_lab.access.datacache.SourceCacheStats;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.setting.dc_connector.DataCacheConnectorSetting;
import eu.essi_lab.cfga.gs.task.AbstractCustomTask;
import eu.essi_lab.cfga.gs.task.OptionsKey;
import eu.essi_lab.cfga.scheduler.SchedulerJobStatus;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.JobStatus.JobPhase;
import eu.essi_lab.model.GSSource;

/**
 * Removes all the data of a source from the data cache: values, harvesting statistics, series statuses and stations.<br>
 * The source is given with the <code>SOURCE_ID</code> option (e.g. <code>sourceId=my-source</code>); if missing, the
 * source the task is attached to is used. The task does nothing if no source is specified, so that the whole cache is
 * never removed by mistake
 *
 * @author boldrini
 */
public class DataCacheSourceRemovalTask extends AbstractCustomTask {

    public enum DataCacheSourceRemovalTaskOptions implements OptionsKey {
	SOURCE_ID;
    }

    @Override
    public String getName() {

	return "Data cache source removal task";
    }

    @Override
    public void doJob(JobExecutionContext context, SchedulerJobStatus status) throws Exception {

	log(status, "Data cache source removal task STARTED");

	String sourceId = null;

	Optional<EnumMap<DataCacheSourceRemovalTaskOptions, String>> taskOptions = readTaskOptions(context,
		DataCacheSourceRemovalTaskOptions.class);
	if (taskOptions.isPresent()) {
	    sourceId = taskOptions.get().get(DataCacheSourceRemovalTaskOptions.SOURCE_ID);
	}
	if ((sourceId == null || sourceId.isBlank()) && getSource().isPresent()) {
	    sourceId = getSource().get().getUniqueIdentifier();
	}
	if (sourceId == null || sourceId.isBlank()) {
	    status.setPhase(JobPhase.ERROR);
	    log(status, "No source specified: set the sourceId option or attach the task to a source");
	    return;
	}
	String id = sourceId.trim();

	Optional<GSSource> source = ConfigurationWrapper.getAllSources().stream().filter(s -> s.getUniqueIdentifier().equals(id))
		.findFirst();
	String sourceName = source.map(s -> s.getLabel() + " (" + id + ")").orElse(id);
	if (source.isEmpty()) {
	    log(status, "Source " + id + " not found in the configuration, removing its values anyway");
	}

	DataCacheConnectorSetting setting = ConfigurationWrapper.getDataCacheConnectorSetting();
	DataCacheConnector dataCacheConnector = DataCacheConnectorFactory.newDataCacheConnector(setting);
	if (dataCacheConnector == null) {
	    status.setPhase(JobPhase.ERROR);
	    log(status, "Issues initializing the data cache connector");
	    return;
	}

	try {

	    log(status, "Removing " + countValues(dataCacheConnector, id) + " values of source " + sourceName + " from the data cache");

	    long deleted = dataCacheConnector.deleteBefore(null, id, null);

	    log(status, "Removed " + deleted + " values of source " + sourceName);

	    long statistics = dataCacheConnector.deleteStatistics(id);

	    log(status, "Removed " + statistics + " statistics of source " + sourceName);

	    long statuses = dataCacheConnector.deleteSeriesStatus(id);

	    log(status, "Removed " + statuses + " series statuses of source " + sourceName);

	    dataCacheConnector.deleteStations(id);

	    log(status, "Removed stations of source " + sourceName);

	} finally {

	    try {
		dataCacheConnector.close();
	    } catch (Exception e) {
		GSLoggerFactory.getLogger(getClass()).error("Error closing the data cache connector", e);
	    }
	}

	log(status, "Data cache source removal task ENDED");
    }

    /**
     * @param dataCacheConnector
     * @param sourceId
     * @return the number of values of the given source in the data cache
     * @throws Exception
     */
    private long countValues(DataCacheConnector dataCacheConnector, String sourceId) throws Exception {

	SourceCacheStats stats = dataCacheConnector.getCacheStatsPerSource(List.of(sourceId)).get(sourceId);

	return stats != null && stats.getRecordCount() != null ? stats.getRecordCount() : 0;
    }
}
