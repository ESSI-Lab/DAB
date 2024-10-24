package eu.essi_lab.accessor.worldcereal.harvested;

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

import org.json.JSONObject;

import eu.essi_lab.cfga.gs.setting.connector.HarvestedConnectorSetting;

/**
 * @author Fabrizio
 */
public class WorldCerealConnectorSetting extends HarvestedConnectorSetting {

    /**
     * 
     */
    private int DEFAULT_PAGE_SIZE = 10;

    /**
     * 
     */
    public WorldCerealConnectorSetting() {
    }

    /**
     * @param object
     */
    public WorldCerealConnectorSetting(JSONObject object) {

	super(object);
    }

    /**
     * @param object
     */
    public WorldCerealConnectorSetting(String object) {

	super(object);
    }

    @Override
    protected String initConnectorType() {

	return WorldCerealConnector.TYPE;
    }

    @Override
    protected String initSettingName() {

	return "WorldCereal Connector settings";
    }

    /**
     * @return
     */
    protected int getDefaultPageSize() {

	return DEFAULT_PAGE_SIZE;
    }
}
