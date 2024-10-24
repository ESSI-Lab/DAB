package eu.essi_lab.accessor.wms;

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

import eu.essi_lab.accessor.wms._1_1_1.WMS_1_1_1Connector;
import eu.essi_lab.cdk.harvest.wrapper.ConnectorWrapperSetting;

/**
 * @author Fabrizio
 */
public class WMSConnectorWrapperSetting extends ConnectorWrapperSetting<WMSConnector> {

    @Override
    protected String initConnectorType() {

	return WMSConnectorWrapper.TYPE;
    }

    @Override
    protected String initSettingName() {

	return "WMS Connector settings";
    }

    @Override
    protected String getDefaultConnectorType() {

	return WMS_1_1_1Connector.TYPE;
    }

    @Override
    protected String getConnectorsTypeOptionLabel() {

	return "WMS Connector type";
    }

    @Override
    protected String getOptionKey() {

	return "wmsConnectorType";
    }

    @Override
    protected Class<WMSConnector> getWrappedConnectorClass() {

	return WMSConnector.class;
    }
}
