/**
 * 
 */
package eu.essi_lab.gssrv.rest.conf;

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

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import eu.essi_lab.cdk.harvest.wrapper.ConnectorWrapperSetting;
import eu.essi_lab.cfga.SelectionUtils;
import eu.essi_lab.cfga.gs.setting.accessor.AccessorSetting;
import eu.essi_lab.cfga.gs.setting.connector.HarvestedConnectorSetting;
import eu.essi_lab.cfga.gs.setting.harvesting.HarvestingSetting;
import eu.essi_lab.cfga.gs.setting.harvesting.HarvestingSettingLoader;
import eu.essi_lab.cfga.setting.scheduling.Scheduling;
import eu.essi_lab.gssrv.conf.task.ResourcesComparatorTask;
import eu.essi_lab.gssrv.rest.conf.requests.source.HarvestSchedulingRequest;
import eu.essi_lab.gssrv.rest.conf.requests.source.PutSourceRequest;
import eu.essi_lab.gssrv.rest.conf.requests.source.HarvestSchedulingRequest.RepeatIntervalUnit;
import eu.essi_lab.gssrv.rest.conf.requests.source.PutSourceRequest.SourceType;
import eu.essi_lab.lib.utils.LabeledEnum;

/**
 * @author Fabrizio
 */
public class HarvestingSettingUtils {

    /**
     * @param request
     * @param model
     * @return
     */
    public static HarvestingSetting build(PutSourceRequest request, HarvestingSetting model) {

	HarvestedConnectorSetting modelConnectorSetting = model.getSelectedAccessorSetting().getHarvestedConnectorSetting();

	String modelType = model.getSelectedAccessorSetting().getAccessorType().replace(" Connector", "");

	if (modelConnectorSetting instanceof ConnectorWrapperSetting) {

	    ConnectorWrapperSetting<?> wrapped = (ConnectorWrapperSetting<?>) modelConnectorSetting;
	    modelType = wrapped.getSelectedConnector().getType().//
		    replace("Connector ", "").// for connectors with a version, like "WCS Connector 1.1.0"
		    replace(" Connector", "");// for connectors with no version, like "CSW Connector"
	}

	String modelLabel = model.getSelectedAccessorSetting().getSource().getLabel();

	String modelEndpoint = model.getSelectedAccessorSetting().getSource().getEndpoint();

	String sourceID = request.read(PutSourceRequest.SOURCE_ID).get().toString();

	String type = request.read(PutSourceRequest.SERVICE_TYPE).isEmpty() ? modelType
		: request.read(PutSourceRequest.SERVICE_TYPE).get().toString();

	String sourceEndpoint = request.read(PutSourceRequest.SOURCE_ENDPOINT).isEmpty() ? modelEndpoint
		: request.read(PutSourceRequest.SOURCE_ENDPOINT).get().toString();

	String label = request.read(PutSourceRequest.SOURCE_LABEL).isEmpty() ? modelLabel
		: request.read(PutSourceRequest.SOURCE_LABEL).get().toString();

	SourceType sourceType = LabeledEnum.valueOf(SourceType.class, type).get();

	return build(sourceType, sourceID, label, sourceEndpoint);
    }

    /**
     * @param service
     * @return
     */
    public static HarvestingSetting build(PutSourceRequest request) {

	String type = request.read(PutSourceRequest.SERVICE_TYPE).get().toString();

	SourceType sourceType = LabeledEnum.valueOf(SourceType.class, type).get();

	String sourceEndpoint = request.read(PutSourceRequest.SOURCE_ENDPOINT).get().toString();
	String sourceID = request.read(PutSourceRequest.SOURCE_ID).get().toString();

	String label = request.read(PutSourceRequest.SOURCE_LABEL).get().toString();

	return build(sourceType, sourceID, label, sourceEndpoint);
    }

    /**
     * @param request
     * @param scheduling
     * @return
     */
    public static void udpate(HarvestSchedulingRequest request, Scheduling scheduling) {

	Optional<String> startTime = request.read(HarvestSchedulingRequest.START_TIME).map(v -> v.toString());

	Optional<String> interval = request.read(HarvestSchedulingRequest.REPEAT_INTERVAL).map(v -> v.toString());
	Optional<String> unit = request.read(HarvestSchedulingRequest.REPEAT_INTERVAL_UNIT).map(v -> v.toString());

	scheduling.setEnabled(true);

	if (interval.isPresent()) {

	    scheduling.setRunIndefinitely();

	    RepeatIntervalUnit intUnit = LabeledEnum.valueOf(RepeatIntervalUnit.class, unit.get()).get();
	    switch (intUnit) {
	    case MINUTES:
		scheduling.setRepeatInterval(Integer.valueOf(interval.get().toString()), TimeUnit.MINUTES);
		break;
	    case DAYS:
		scheduling.setRepeatInterval(Integer.valueOf(interval.get().toString()), TimeUnit.DAYS);
		break;
	    case HOURS:
		scheduling.setRepeatInterval(Integer.valueOf(interval.get().toString()), TimeUnit.HOURS);
		break;
	    case MONTHS:
		scheduling.setRepeatInterval(Integer.valueOf(interval.get().toString()) * 30, TimeUnit.DAYS);
		break;
	    case WEEKS:
		scheduling.setRepeatInterval(Integer.valueOf(interval.get().toString()) * 7, TimeUnit.DAYS);
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
    private static HarvestingSetting build(//
	    SourceType sourceType, //
	    String sourceID, //
	    String label, //
	    String sourceEndpoint) {

	return switch (sourceType) {
	case CSW ->

		build(//
			"CSW", //
			Optional.empty(), //
			sourceID, //
			label, //
			sourceEndpoint);

	case WCS_100 ->

		build(//
			"WCS", //
			Optional.of("WCS Connector 1.0.0"), //
			sourceID, //
			label, //
			sourceEndpoint);

	case WCS_110 ->

		build(//
			"WCS", //
			Optional.of("WCS Connector 1.1.0"), //
			sourceID, //
			label, //
			sourceEndpoint);

	case WCS_111 ->

		build(//
			"WCS", //
			Optional.of("WCS Connector 1.1.1"), //
			sourceID, //
			label, //
			sourceEndpoint);

	case WFS_110 ->

		build(//
			"WFS", //
			Optional.of("WFS Connector 1.1.0"), //
			sourceID, //
			label, //
			sourceEndpoint);

	case WMS_111 ->

		build(//
			"WMS", //
			Optional.of("WMS Connector 1.1.1"), //
			sourceID, //
			label, //
			sourceEndpoint);

	case WMS_130 ->

		build(//
			"WMS", //
			Optional.of("WMS Connector 1.3.0"), //
			sourceID, //
			label, //
			sourceEndpoint);

	default -> throw new IllegalArgumentException("Unexpected value: " + sourceType);

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
    private static HarvestingSetting build(//
	    String accessorType, //
	    Optional<String> wrappedConnectorType, //
	    String sourceId, //
	    String sourceLabel, //
	    String sourceEndpoint

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
	harvSetting.getCustomTaskSetting().get().selectTaskName(new ResourcesComparatorTask().getName());

	//
	//
	//

	SelectionUtils.deepClean(harvSetting);

	//
	//
	//

	return harvSetting;
    }
}
