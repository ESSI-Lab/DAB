package eu.essi_lab.cfga.gs.task;

import java.util.Optional;

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

import org.quartz.JobExecutionContext;

import eu.essi_lab.cfga.gs.setting.harvesting.HarvestingSetting;
import eu.essi_lab.cfga.gs.setting.harvesting.HarvestingSettingLoader;
import eu.essi_lab.cfga.scheduler.SchedulerUtils;
import eu.essi_lab.cfga.scheduler.Task;
import eu.essi_lab.cfga.setting.SettingUtils;
import eu.essi_lab.cfga.setting.scheduling.SchedulerWorkerSetting;
import eu.essi_lab.model.GSSource;

/**
 * @author Fabrizio
 */
public interface CustomTask extends Task {

    /**
     * @param context
     * @return
     */
    public default CustomTaskSetting retrieveSetting(JobExecutionContext context) {

	SchedulerWorkerSetting workerSetting = SchedulerUtils.getSetting(context);

	switch (workerSetting.getGroup()) {

	//
	// custom task embedded in a HarvestingSetting
	//
	case HARVESTING:

	    HarvestingSetting harvestingSetting = SettingUtils.downCast(workerSetting, HarvestingSettingLoader.load().getClass());

	    return harvestingSetting.getCustomTaskSetting().get();

	//
	// stand-alone custom task
	//
	case CUSTOM_TASK:
	default:

	}

	return SettingUtils.downCast(workerSetting, CustomTaskSetting.class);
    }

    /**
     * @param context
     * @return
     */
    default Optional<String> readTaskOptions(JobExecutionContext context) {

	CustomTaskSetting setting = retrieveSetting(context);

	return setting.getTaskOptions();
    }

    /**
     * @return
     */
    public default boolean clearMessagesBeforeStoreStatus() {

	return false;
    }

    /**
     * @param source
     */
    public void setSource(GSSource source);

    /**
     * @return
     */
    public Optional<GSSource> getSource();

    /**
     * @return
     */
    public String getName();
}
