/**
 * 
 */
package eu.essi_lab.gssrv.rest.conf;

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

import java.util.Optional;

import eu.essi_lab.cdk.harvest.wrapper.ConnectorWrapperSetting;
import eu.essi_lab.cfga.SelectionUtils;
import eu.essi_lab.cfga.gs.setting.accessor.AccessorSetting;
import eu.essi_lab.cfga.gs.setting.connector.HarvestedConnectorSetting;
import eu.essi_lab.cfga.gs.setting.harvesting.HarvestingSetting;
import eu.essi_lab.cfga.gs.setting.harvesting.HarvestingSettingLoader;
import eu.essi_lab.gssrv.rest.conf.PutSourceRequest.SourceType;
import eu.essi_lab.lib.utils.LabeledEnum;

/**
 * @author Fabrizio
 */
public class HarvestingSettingBuilder {

    /**
     * @param service
     * @return
     */
    public static HarvestingSetting build(PutSourceRequest request) {

	String type = request.read(PutSourceRequest.SOURCE_TYPE).get().toString();

	SourceType sourceType = LabeledEnum.valueOf(SourceType.class, type).get();

	String sourceEndpoint = request.read(PutSourceRequest.SOURCE_ENDPOINT).get().toString();
	String sourceID = request.read(PutSourceRequest.SOURCE_ID).get().toString();

	String label = request.read(PutSourceRequest.SOURCE_LABEL).get().toString();

	HarvestingSetting harvSetting = null;

	switch (sourceType) {
	case CSW:

	    harvSetting = createSetting(//
		    "CSW", //
		    Optional.empty(), //
		    sourceID, //
		    label, //
		    sourceEndpoint);

	    break;

	case WCS_111: {

	    String wrappedConnectorType = "WCS Connector 1.1.1";

	    harvSetting = createSetting(//
		    "WCS", //
		    Optional.of(wrappedConnectorType), //
		    sourceID, //
		    label, //
		    sourceEndpoint);

	    break;
	}

	case WFS_110: {

	    String wrappedConnectorType = "WFS Connector 1.1.0";

	    harvSetting = createSetting(//
		    "WFS", //
		    Optional.of(wrappedConnectorType), //
		    sourceID, //
		    label, //
		    sourceEndpoint);

	    break;
	}

	case WMS_111: {

	    String wrappedConnectorType = "WMS Connector 1.1.1";

	    harvSetting = createSetting(//
		    "WMS", //
		    Optional.of(wrappedConnectorType), //
		    sourceID, //
		    label, //
		    sourceEndpoint);

	    break;
	}

	case WMS_130: {

	    String wrappedConnectorType = "WMS Connector 1.3.0";

	    harvSetting = createSetting(//
		    "WMS", //
		    Optional.of(wrappedConnectorType), //
		    sourceID, //
		    label, //
		    sourceEndpoint);

	    break;
	}

	}

	return harvSetting;
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
    private static HarvestingSetting createSetting(//
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
	//
	//

	SelectionUtils.deepClean(harvSetting);

	//
	//
	//

	return harvSetting;
    }

    // /**
    // * @param accessorType
    // * @param connectorType
    // * @param sourceId
    // * @param sourceLabel
    // * @param sourceEndpoint
    // * @param schedulerDelay
    // * @return
    // */
    // private static HarvestingSetting createSetting(//
    // String accessorType, //
    // Optional<String> connectorType, //
    // String sourceId, //
    // String sourceLabel, //
    // String sourceEndpoint,//
    // String startTime
    //
    // ) {
    //
    // HarvestingSetting harvSetting = HarvestingSettingLoader.load();
    //
    // harvSetting.selectAccessorSetting(s -> s.getAccessorType().equals(accessorType));
    //
    // harvSetting.setName(sourceLabel);
    //
    // //
    // // scheduling
    // //
    //
    // Scheduling scheduling = harvSetting.getScheduling();
    // scheduling.setEnabled(true);
    //
    // scheduling.setRunIndefinitely();
    // scheduling.setRepeatInterval(1000, TimeUnit.DAYS);
    //
    // scheduling.setStartTime(startTime);
    //
    // //
    // // source
    // //
    //
    // AccessorSetting accessorSetting = harvSetting.getSelectedAccessorSetting();
    //
    // accessorSetting.getGSSourceSetting().setSourceIdentifier(sourceId);
    // accessorSetting.getGSSourceSetting().setSourceLabel(sourceLabel);
    // accessorSetting.getGSSourceSetting().setSourceEndpoint(sourceEndpoint);
    //
    // //
    // // optional wrapped connector
    // //
    //
    // if (connectorType.isPresent()) {
    //
    // HarvestedConnectorSetting connectorSetting = accessorSetting.getHarvestedConnectorSetting();
    //
    // if (connectorSetting instanceof ConnectorWrapperSetting) {
    //
    // @SuppressWarnings("rawtypes")
    // ConnectorWrapperSetting wrapper = (ConnectorWrapperSetting) connectorSetting;
    // wrapper.selectConnectorType(connectorType.get());
    // }
    // }
    //
    // //
    // //
    // //
    //
    // SelectionUtils.deepClean(harvSetting);
    //
    // //
    // //
    // //
    //
    // return harvSetting;
    // }
}
