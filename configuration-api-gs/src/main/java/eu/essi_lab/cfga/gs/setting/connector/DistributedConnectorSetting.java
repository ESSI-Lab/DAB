package eu.essi_lab.cfga.gs.setting.connector;

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

import eu.essi_lab.cfga.setting.KeyValueOptionDecorator;
import eu.essi_lab.model.BrokeringStrategy;

/**
 * @author Fabrizio
 */
public abstract class DistributedConnectorSetting extends ConnectorSetting implements KeyValueOptionDecorator{

    /**
     * 
     */
    public DistributedConnectorSetting() {

	setName(initSettingName());
	
	addKeyValueOption();

	setConfigurableType(initConnectorType());
    }

    /**
     * @param name
     * @param configurableType
     * @return
     */
    public static DistributedConnectorSetting create(String name, String configurableType) {

	return new DistributedConnectorSetting() {

	    @Override
	    protected String initConnectorType() {

		return configurableType;
	    }

	    @Override
	    protected String initSettingName() {

		return name;
	    }
	};
    }

    /**
     * @param object
     */
    public DistributedConnectorSetting(JSONObject object) {

	super(object);
    }

    /**
     * @param object
     */
    public DistributedConnectorSetting(String object) {

	super(object);
    }

    /**
     * 
     */
    protected BrokeringStrategy getBrokeringStrategy() {

	return BrokeringStrategy.DISTRIBUTED;
    }

}
