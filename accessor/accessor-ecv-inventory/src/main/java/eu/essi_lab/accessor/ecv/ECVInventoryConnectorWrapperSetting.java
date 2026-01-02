package eu.essi_lab.accessor.ecv;

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

import eu.essi_lab.cdk.harvest.wrapper.ConnectorWrapperSetting;

/**
 * @author Fabrizio
 */
public class ECVInventoryConnectorWrapperSetting extends ConnectorWrapperSetting<ECVInventoryConnector> {

    @Override
    protected String getDefaultConnectorType() {

	return ECVInventoryConnector.TYPE;
    }

    @Override
    protected String getConnectorsTypeOptionLabel() {

	return "ECV Connector type";
    }

    @Override
    protected String getOptionKey() {

	return "ecvConnectorType";
    }

    @Override
    protected Class<ECVInventoryConnector> getWrappedConnectorClass() {

	return ECVInventoryConnector.class;
    }

    @Override
    protected String initConnectorType() {

	return ECVInventoryConnectorWrapper.TYPE;
    }

    @Override
    protected String initSettingName() {

	return "ECV Connector settings";
    }
}
