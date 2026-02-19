/**
 *
 */
package eu.essi_lab.cfga.rest;

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

import eu.essi_lab.cdk.harvest.wrapper.*;
import eu.essi_lab.cfga.*;
import eu.essi_lab.cfga.gs.*;
import eu.essi_lab.cfga.gs.setting.accessor.*;
import eu.essi_lab.cfga.gs.setting.connector.*;
import eu.essi_lab.cfga.gs.setting.harvesting.*;
import eu.essi_lab.cfga.rest.source.*;
import eu.essi_lab.cfga.rest.source.PutSourceRequest.*;
import eu.essi_lab.cfga.setting.scheduling.*;
import eu.essi_lab.lib.utils.*;

import javax.ws.rs.core.Response.*;
import java.util.*;
import java.util.concurrent.*;

import static eu.essi_lab.cfga.rest.source.PutSourceRequest.SourceType.*;

/**
 * @author Fabrizio
 */
public class HarvestingSettingUtils {

    /**
     * @param request
     * @param target
     * @return
     */
    public static HarvestingSetting build(EditSourceRequest request, HarvestingSetting target) {

	HarvestedConnectorSetting currentConnectorSetting = target.getSelectedAccessorSetting().getHarvestedConnectorSetting();

	String currentType = target.getSelectedAccessorSetting().getAccessorType().replace(" Connector", "");

	if (currentConnectorSetting instanceof ConnectorWrapperSetting<?> wrapped) {

	    currentType = wrapped.getSelectedConnector().getType().//
		    replace("Connector ", "").// for connectors with a version, like "WCS Connector 1.1.0"
		    replace(" Connector", "");// for connectors with no version, like "CSW Connector"
	}

	String currentLabel = target.getSelectedAccessorSetting().getSource().getLabel();

	String currentEndpoint = target.getSelectedAccessorSetting().getSource().getEndpoint();

	List<String> currentDep = target.getSelectedAccessorSetting().getGSSourceSetting().getSourceDeployment();

	String sourceID = request.readString(PutSourceRequest.SOURCE_ID).get();

	String type = request.readString(PutSourceRequest.SERVICE_TYPE).orElse(currentType);

	SourceType sourceType = LabeledEnum.valueOf(SourceType.class, type).get();

	String sourceEndpoint = request.readString(PutSourceRequest.SOURCE_ENDPOINT).orElse(currentEndpoint);

	String label = request.readString(PutSourceRequest.SOURCE_LABEL).orElse(currentLabel);

	List<String> sourceDep = request.read(PutSourceRequest.SOURCE_DEPLOYMENT).isEmpty()
		? currentDep
		: request.readStrings(PutSourceRequest.SOURCE_DEPLOYMENT);

	return buildFromSourceType(sourceType, sourceID, label, sourceEndpoint, sourceDep);
    }

    /**
     * @param service
     * @return
     */
    public static HarvestingSetting build(PutSourceRequest request) {

	String type = request.readString(PutSourceRequest.SERVICE_TYPE).get();

	SourceType sourceType = LabeledEnum.valueOf(SourceType.class, type).get();

	String sourceEndpoint = request.readString(PutSourceRequest.SOURCE_ENDPOINT).get();
	String sourceID = request.readString(PutSourceRequest.SOURCE_ID).get();

	String label = request.readString(PutSourceRequest.SOURCE_LABEL).get();

	List<String> sourceDep = request.readStrings(PutSourceRequest.SOURCE_DEPLOYMENT);

	return buildFromSourceType(sourceType, sourceID, label, sourceEndpoint, sourceDep);
    }

    /**
     * @param request
     * @param scheduling
     * @return
     */
    public static void udpate(HarvestSchedulingRequest request, Scheduling scheduling) {

	Optional<String> startTime = request.readString(HarvestSchedulingRequest.START_TIME);

	Optional<String> interval = request.readString(HarvestSchedulingRequest.REPEAT_INTERVAL);
	Optional<String> unit = request.readString(HarvestSchedulingRequest.REPEAT_INTERVAL_UNIT);

	scheduling.setEnabled(true);

	if (interval.isPresent()) {

	    scheduling.setRunIndefinitely();

	    HarvestSchedulingRequest.RepeatIntervalUnit intUnit = LabeledEnum.valueOf(HarvestSchedulingRequest.RepeatIntervalUnit.class,
		    unit.get()).get();

	    switch (intUnit) {
	    case MINUTES:
		scheduling.setRepeatInterval(Integer.parseInt(interval.get()), TimeUnit.MINUTES);
		break;
	    case DAYS:
		scheduling.setRepeatInterval(Integer.parseInt(interval.get()), TimeUnit.DAYS);
		break;
	    case HOURS:
		scheduling.setRepeatInterval(Integer.parseInt(interval.get()), TimeUnit.HOURS);
		break;
	    case MONTHS:
		scheduling.setRepeatInterval(Integer.parseInt(interval.get()) * 30, TimeUnit.DAYS);
		break;
	    case WEEKS:
		scheduling.setRepeatInterval(Integer.parseInt(interval.get()) * 7, TimeUnit.DAYS);
		break;
	    }

	} else {

	    scheduling.setRunOnce();
	}

	if (startTime.isPresent()) {

	    scheduling.setStartTime(startTime.get());

	} else {

	    scheduling.setStartNow();
	}
    }

    /**
     * @param sourceType
     * @param sourceID
     * @param label
     * @param sourceEndpoint
     * @return
     */
    private static HarvestingSetting buildFromSourceType(//
	    SourceType sourceType, //
	    String sourceID, //
	    String label, //
	    String sourceEndpoint,//
	    List<String> sourceDep) { //

	return switch (sourceType) {
	    case CSW -> buildFromAccessorType(//
		    "CSW", //
		    Optional.empty(), //
		    sourceID, //
		    label, //
		    sourceEndpoint,
		    sourceDep);

	    case WCS_100 -> buildFromAccessorType(//
		    "WCS", //
		    Optional.of("WCS Connector 1.0.0"), //
		    sourceID, //
		    label, //
		    sourceEndpoint,
		    sourceDep);

	    case WCS_110 -> buildFromAccessorType(//
		    "WCS", //
		    Optional.of("WCS Connector 1.1.0"), //
		    sourceID, //
		    label, //
		    sourceEndpoint,
		    sourceDep);

	    case WCS_111 -> buildFromAccessorType(//
		    "WCS", //
		    Optional.of("WCS Connector 1.1.1"), //
		    sourceID, //
		    label, //
		    sourceEndpoint,
		    sourceDep);

	    case WFS_110 -> buildFromAccessorType(//
		    "WFS", //
		    Optional.of("WFS Connector 1.1.0"), //
		    sourceID, //
		    label, //
		    sourceEndpoint,
		    sourceDep);

	    case WMS_111 -> buildFromAccessorType(//
		    "WMS", //
		    Optional.of("WMS Connector 1.1.1"), //
		    sourceID, //
		    label, //
		    sourceEndpoint,
		    sourceDep);

	    case WMS_130 -> buildFromAccessorType(//
		    "WMS", //
		    Optional.of("WMS Connector 1.3.0"), //
		    sourceID, //
		    label, //
		    sourceEndpoint,
		    sourceDep);
	};
    }

    /**
     * @param accessorType
     * @param wrappedConnectorType
     * @param sourceId
     * @param sourceLabel
     * @param sourceEndpoint
     * @param schedulerDelay
     * @return
     */
    private static HarvestingSetting buildFromAccessorType(//
	    String accessorType, //
	    Optional<String> wrappedConnectorType, //
	    String sourceId, //
	    String sourceLabel, //
	    String sourceEndpoint,//
	    List<String> sourceDep

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
	sourceDep.forEach(dep -> accessorSetting.getGSSourceSetting().addSourceDeployment(dep));

	//
	// optional wrapped connector
	//

	if (wrappedConnectorType.isPresent()) {

	    HarvestedConnectorSetting connectorSetting = accessorSetting.getHarvestedConnectorSetting();

	    if (connectorSetting instanceof ConnectorWrapperSetting) {

		@SuppressWarnings("rawtypes")
		ConnectorWrapperSetting wrapper = (ConnectorWrapperSetting) connectorSetting;
		wrapper.selectConnectorType(wrappedConnectorType.get());
	    }
	}

	//
	// set the ResourcesComparatorTask as custom task
	//

	harvSetting.getCustomTaskSetting().get().setEnabled(true);
	harvSetting.getCustomTaskSetting().get().selectTaskName("Resources comparator task");

	//
	//
	//

	SelectionUtils.deepClean(harvSetting);

	//
	//
	//

	return harvSetting;
    }

    /**
     * @param request
     * @return
     */
    public static SettingFinder<HarvestingSetting> getHarvestingSettingFinder(ConfigRequest request) {

	Optional<String> optSourceId = request.readString(PutSourceRequest.SOURCE_ID);

	HarvestingSetting setting = null;

	if (optSourceId.isEmpty()) {

	    return new SettingFinder<HarvestingSetting>(
		    ConfigRequest.buildErrorResponse(Status.METHOD_NOT_ALLOWED, "Missing source identifier"));

	} else {

	    String sourceId = optSourceId.get();

	    if (ConfigurationWrapper.getAllSources().//
		    stream().//
		    noneMatch(s -> s.getUniqueIdentifier().equals(sourceId))) {

		return new SettingFinder<HarvestingSetting>(
			ConfigRequest.buildErrorResponse(Status.NOT_FOUND, "Source with id '" + sourceId + "' not found"));
	    }

	    setting = ConfigurationWrapper.getHarvestingSettings().//
		    stream().//
		    filter(s -> s.getSelectedAccessorSetting().getSource().getUniqueIdentifier().equals(sourceId)).//
		    findFirst().//
		    get();
	}

	return new SettingFinder<HarvestingSetting>(setting);
    }
}
