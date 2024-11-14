/**
 * 
 */
package eu.essi_lab.gssrv.conf.task;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2024 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.quartz.JobExecutionContext;

import eu.essi_lab.cdk.harvest.wrapper.ConnectorWrapperSetting;
import eu.essi_lab.cfga.Configuration;
import eu.essi_lab.cfga.ConfigurationSource;
import eu.essi_lab.cfga.SelectionUtils;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.setting.SchedulerViewSetting;
import eu.essi_lab.cfga.gs.setting.accessor.AccessorSetting;
import eu.essi_lab.cfga.gs.setting.connector.HarvestedConnectorSetting;
import eu.essi_lab.cfga.gs.setting.harvesting.HarvestingSetting;
import eu.essi_lab.cfga.gs.setting.harvesting.HarvestingSettingLoader;
import eu.essi_lab.cfga.gs.task.AbstractCustomTask;
import eu.essi_lab.cfga.scheduler.Scheduler;
import eu.essi_lab.cfga.scheduler.SchedulerFactory;
import eu.essi_lab.cfga.scheduler.SchedulerJobStatus;
import eu.essi_lab.cfga.setting.scheduling.Scheduling;

/**
 * @author Fabrizio
 */
public class ConfigurationEditorTask extends AbstractCustomTask {

    @Override
    public void doJob(JobExecutionContext context, SchedulerJobStatus status) throws Exception {

	Configuration configuration = ConfigurationWrapper.getConfiguration().get();

	//
	//
	//

	ConfigurationSource source = configuration.getSource();
	source.backup();

	//
	//
	//

	SchedulerViewSetting schedulerSetting = ConfigurationWrapper.getSchedulerSetting();

	Scheduler scheduler = SchedulerFactory.getScheduler(schedulerSetting);

	//
	//
	//

	List<String> augmenterTypes = Arrays.asList(//
		"EasyAccessAugmenter", //
		"WHOSUnitsAugmenter", //
		"WHOSVariableAugmenter", //
		"WHOSRiverVariableAugmenter");

	HarvestingSetting harvestingSetting = createSetting(//
		"WCS", //
		Optional.of("WCS Connector 1.1.1"), //
		"source_id", //
		"Source label", //
		"http://", //
		augmenterTypes);

	//
	// scheduling
	//

	Date startDate = new Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(10));

	Scheduling scheduling = harvestingSetting.getScheduling();
	scheduling.setEnabled(true);

	scheduling.setRunIndefinitely();
	scheduling.setRepeatInterval(1000, TimeUnit.DAYS);

	scheduling.setStartTime(startDate);

	scheduler.schedule(harvestingSetting);
	
	SelectionUtils.deepClean(harvestingSetting);

	//
	//
	//

	SelectionUtils.deepClean(harvestingSetting);

	configuration.put(harvestingSetting);

	//
	//
	//

	configuration.flush();
    }

    /**
     * @param startTime
     * @param accessorType
     * @param connectorType
     * @param sourceId
     * @param sourceLabel
     * @param sourceEndpoint
     * @param augmenterTypes
     * @return
     */
    private HarvestingSetting createSetting(//
	    String accessorType, //
	    Optional<String> connectorType, //
	    String sourceId, //
	    String sourceLabel, //
	    String sourceEndpoint, //
	    List<String> augmenterTypes

    ) {

	HarvestingSetting harvSetting = HarvestingSettingLoader.load();

	harvSetting.selectAccessorSetting(s -> s.getAccessorType().equals(accessorType));

	harvSetting.setName(sourceLabel);

	//
	// source
	//

	AccessorSetting accessorSetting = harvSetting.getSelectedAccessorSetting();

	accessorSetting.getGSSourceSetting().setSourceIdentifier(sourceId);
	accessorSetting.getGSSourceSetting().setSourceLabel(sourceLabel);
	accessorSetting.getGSSourceSetting().setSourceEndpoint(sourceEndpoint);

	//
	// augmenters
	//

	harvSetting.getAugmentersSetting().select(s -> //

	augmenterTypes.contains(s.getConfigurableType()));

	//
	// optional wrapped connector
	//

	if (connectorType.isPresent()) {

	    HarvestedConnectorSetting connectorSetting = accessorSetting.getHarvestedConnectorSetting();

	    if (connectorSetting instanceof ConnectorWrapperSetting) {

		@SuppressWarnings("rawtypes")
		ConnectorWrapperSetting wrapper = (ConnectorWrapperSetting) connectorSetting;
		wrapper.selectConnectorType(connectorType.get());
	    }
	}

	//
	//
	//

	return harvSetting;

    }

    @Override
    public String getName() {

	return "Configuration editor task";
    }

}
