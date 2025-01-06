package eu.essi_lab.cfga.gs.setting.dc_connector;

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

import org.json.JSONObject;

import eu.essi_lab.cfga.EditableSetting;
import eu.essi_lab.cfga.setting.ConfigurableSetting;

/**
 * @author Fabrizio
 */
public abstract class DataCacheConnectorSetting extends ConfigurableSetting implements EditableSetting {

    /**
     * 
     */
    public DataCacheConnectorSetting() {
    }

    /**
     * @param object
     */
    public DataCacheConnectorSetting(JSONObject object) {

	super(object);
    }

    /**
     * @param object
     */
    public DataCacheConnectorSetting(String object) {

	super(object);
    }

    /**
     * @return
     */
    public abstract void setDataConnectorType(String type);

    /**
     * @return
     */
    public abstract void setDatabaseUri(String uri);

    /**
     * @return
     */
    public abstract void setDatabasePassword(String password);

    /**
     * @return
     */
    public abstract void setDatabaseName(String name);

    /**
     * @return
     */
    public abstract void setDatabaseUser(String user);

    /**
     * @param optionName
     * @return
     */
    public abstract void setOptionValue(String optionName, String value);

    /**
     * @return
     */
    public abstract String getDataConnectorType();

    /**
     * @return
     */
    public abstract String getDatabaseUri();

    /**
     * @return
     */
    public abstract String getDatabasePassword();

    /**
     * @return
     */
    public abstract String getDatabaseName();

    /**
     * @return
     */
    public abstract String getDatabaseUser();

    /**
     * @param optionName
     * @return
     */
    public abstract Optional<String> getOptionValue(String optionName);

}
