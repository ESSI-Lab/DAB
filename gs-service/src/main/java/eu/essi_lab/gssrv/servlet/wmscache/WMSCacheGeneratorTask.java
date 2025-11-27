package eu.essi_lab.gssrv.servlet.wmscache;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
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

import java.util.EnumMap;
import java.util.Optional;

import org.quartz.JobExecutionContext;

import eu.essi_lab.cfga.gs.task.AbstractEmbeddedTask;
import eu.essi_lab.cfga.gs.task.OptionsKey;
import eu.essi_lab.cfga.scheduler.SchedulerJobStatus;
import eu.essi_lab.lib.utils.GSLoggerFactory;

/**
 * @author boldrini
 */
public class WMSCacheGeneratorTask extends AbstractEmbeddedTask {

    public enum WMSCacheGeneratorTaskOptions implements OptionsKey {
	CACHE_SIZE;
    }

    @Override
    public void doJob(JobExecutionContext context, SchedulerJobStatus status) throws Exception {

	log(status, "WMS Cache generator task STARTED");

	Optional<EnumMap<WMSCacheGeneratorTaskOptions, String>> taskOptions = readTaskOptions(context, WMSCacheGeneratorTaskOptions.class);
	if (taskOptions.isEmpty() || taskOptions.get().isEmpty()) {
	    GSLoggerFactory.getLogger(getClass())
		    .error("No options specified. Options should be new line separated and in the form key=value");
	    return;
	}

	String cacheSize = taskOptions.get().get(WMSCacheGeneratorTaskOptions.CACHE_SIZE);
	if (cacheSize == null || cacheSize.isEmpty()) {
	    GSLoggerFactory.getLogger(getClass()).info("No cache size option specified, using default");
	    cacheSize = "100";
	}
	
	WMSCache.getInstance().refreshCache(Integer.parseInt(cacheSize));
	

	log(status, "WMS Cache generator task ENDED");
    }


    @Override
    public String getName() {

	return "WMS Cache generator task";
    }

    @Override
    public ExecutionStage getExecutionStage() {

	return ExecutionStage.AFTER_HARVESTING_END;
    }

}
