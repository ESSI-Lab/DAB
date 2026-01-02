package eu.essi_lab.accessor.wcs;

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

import eu.essi_lab.accessor.wcs_1_0_0.WCSConnector_100;
import eu.essi_lab.cdk.harvest.wrapper.ConnectorWrapperSetting;

/**
 * @author Fabrizio
 */
public class WCSConnectorWrapperSetting extends ConnectorWrapperSetting<WCSConnector> {

    /**
     * 
     */
    private static final String WCS_CONNECTOR_NAME_OPTION_KEY = "wcsConnectorType";

    @Override
    protected String initConnectorType() {

	return WCSConnectorWrapper.TYPE;
    }

    @Override
    protected String initSettingName() {

	return "WCS Connector settings";
    }

    @Override
    protected String getDefaultConnectorType() {

	return WCSConnector_100.TYPE;
    }

    @Override
    protected String getConnectorsTypeOptionLabel() {

	return "WCS Connector type";
    }

    @Override
    protected String getOptionKey() {

	return WCS_CONNECTOR_NAME_OPTION_KEY;
    }

    @Override
    protected Class<WCSConnector> getWrappedConnectorClass() {

	return WCSConnector.class;
    }
}
