package eu.essi_lab.accessor.oaipmh.whole;

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

import org.json.JSONObject;

import eu.essi_lab.accessor.oaipmh.OAIPMHConnectorSetting;

/**
 * @author Fabrizio
 */
public class WholeOAIPMHConnectorSetting extends OAIPMHConnectorSetting {

    /**
     * 
     */
    public WholeOAIPMHConnectorSetting() {

	super();

	setConfigurableType(WholeOAIPMHConnector.CONNECTOR_TYPE);
    }

    /**
     * @param object
     */
    public WholeOAIPMHConnectorSetting(JSONObject object) {

	super(object);
    }

    /**
     * @param object
     */
    public WholeOAIPMHConnectorSetting(String object) {

	super(object);
    }

    @Override
    protected String initConnectorType() {

	return WholeOAIPMHConnector.CONNECTOR_TYPE;
    }

    @Override
    protected String initSettingName() {

	return "OAIPMH - Not selective Connector settings";
    }
}
