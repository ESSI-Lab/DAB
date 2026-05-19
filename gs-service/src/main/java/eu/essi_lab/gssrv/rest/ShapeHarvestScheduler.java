package eu.essi_lab.gssrv.rest;

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

import java.util.Optional;

import eu.essi_lab.cfga.Configuration;
import eu.essi_lab.cfga.SelectionUtils;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.setting.harvesting.HarvestingSetting;
import eu.essi_lab.cfga.gs.setting.harvesting.HarvestingSettingLoader;
import eu.essi_lab.cfga.gs.setting.harvesting.SchedulerSupport;
import eu.essi_lab.cfga.rest.HarvestingSettingUtils;
import eu.essi_lab.cfga.rest.SettingFinder;
import eu.essi_lab.cfga.rest.source.HarvestSchedulingRequest;
import eu.essi_lab.cfga.rest.source.PutSourceRequest;
import eu.essi_lab.cfga.scheduler.Scheduler;
import eu.essi_lab.cfga.scheduler.SchedulerFactory;
import eu.essi_lab.cfga.setting.Setting;
import eu.essi_lab.cfga.setting.SettingUtils;
import eu.essi_lab.cfga.gs.setting.SchedulerViewSetting;
import eu.essi_lab.cfga.setting.scheduling.Scheduling;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.JobStatus.JobPhase;

/**
 * Schedules an immediate one-off harvest for a source (same behaviour as the configuration REST API).
 */
public class ShapeHarvestScheduler {

    private ShapeHarvestScheduler() {
    }

    /**
     * @param sourceId
     * @return error message when scheduling failed
     */
    public static Optional<String> scheduleHarvestNow(String sourceId) {

	if (isHarvestingUnderway(sourceId)) {

	    return Optional.of("Harvesting of source '" + sourceId + "' is already in progress");
	}

	HarvestSchedulingRequest harvestSourceRequest = new HarvestSchedulingRequest();
	harvestSourceRequest.put(PutSourceRequest.SOURCE_ID, sourceId);

	SettingFinder<HarvestingSetting> finder = HarvestingSettingUtils.getHarvestingSettingFinder(harvestSourceRequest);

	if (finder.getErrorResponse().isPresent()) {

	    return Optional.of("Harvesting source not found: " + sourceId);
	}

	String settingId = finder.getSetting().get().getIdentifier();

	HarvestingSetting setting = ConfigurationWrapper.getHarvestingSettings().//
		stream().//
		filter(s -> s.getIdentifier().equals(settingId)).//
		findFirst().//
		orElse(null);

	if (setting == null) {

	    return Optional.of("Harvesting setting not found for source: " + sourceId);
	}

	setting = SettingUtils.downCast(SelectionUtils.resetAndSelect(setting, false), HarvestingSettingLoader.load().getClass());

	Scheduling scheduling = setting.getScheduling();

	HarvestingSettingUtils.udpate(harvestSourceRequest, scheduling);

	SelectionUtils.deepClean(setting);
	SelectionUtils.deepAfterClean(setting);

	Configuration configuration = ConfigurationWrapper.getConfiguration().get();

	boolean replaced = configuration.replace(setting);

	if (!replaced) {

	    return Optional.of("Unable to update harvesting schedule for source: " + sourceId);
	}

	try {

	    configuration.flush();

	} catch (Exception ex) {

	    GSLoggerFactory.getLogger(ShapeHarvestScheduler.class).error(ex);

	    return Optional.of("Unable to save harvesting schedule: " + ex.getMessage());
	}

	SchedulerViewSetting schedulerSetting = ConfigurationWrapper.getSchedulerSetting();

	Scheduler scheduler = SchedulerFactory.getScheduler(schedulerSetting);

	try {
	    boolean contains = scheduler.//
		    listScheduledSettings().//
		    stream().//
		    map(Setting::getIdentifier).//
		    toList().//
		    contains(settingId);

	    if (contains) {

		scheduler.reschedule(setting);

	    } else {

		scheduler.schedule(setting);
	    }

	} catch (Exception ex) {

	    GSLoggerFactory.getLogger(ShapeHarvestScheduler.class).error(ex);

	    return Optional.of("Unable to schedule harvest: " + ex.getMessage());
	}

	GSLoggerFactory.getLogger(ShapeHarvestScheduler.class).info("Scheduled harvest now for source {}", sourceId);

	return Optional.empty();
    }

    private static boolean isHarvestingUnderway(String sourceId) {

	SchedulerSupport support = SchedulerSupport.getInstance();
	support.update();

	return ConfigurationWrapper.getHarvestingSettings().//
		stream().//
		filter(s -> s.getSelectedAccessorSetting().getSource().getUniqueIdentifier().equals(sourceId)).//
		anyMatch(s -> support.getJobPhase(s).equals(JobPhase.RUNNING.getLabel()));
    }
}
