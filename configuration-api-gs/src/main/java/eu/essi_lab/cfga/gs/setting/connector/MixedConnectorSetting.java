package eu.essi_lab.cfga.gs.setting.connector;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2022 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import eu.essi_lab.model.BrokeringStrategy;

/**
 * @author Fabrizio
 */
public class MixedConnectorSetting extends ConnectorSetting {

    public static final String MIXED_CONNECTOR_SETTING_ID = "mixedConnectorSetting";

    /**
     * 
     */
    private static final String HARVESTED_CONNECTOR_SETTING_OPTION_KEY = "harvestedConnectorSetting";
    /**
     * 
     */
    private static final String DISTRIBUTED_CONNECTOR_SETTING_OPTION_KEY = "distributedConnectorSetting";

    public MixedConnectorSetting() {

	setIdentifier(MIXED_CONNECTOR_SETTING_ID);
    }

    /**
     * @param object
     */
    public MixedConnectorSetting(JSONObject object) {

	super(object);
    }

    /**
     * @param object
     */
    public MixedConnectorSetting(String object) {

	super(object);
    }

    /**
     * @param name
     * @param harvestedSetting
     * @param distSetting
     * @return
     */
    public static MixedConnectorSetting create(String name, //
	    HarvestedConnectorSetting harvestedSetting, //
	    DistributedConnectorSetting distSetting) {

	MixedConnectorSetting setting = new MixedConnectorSetting();

	setting.setName(name);

	setting.setHarvestedConnectorSetting(harvestedSetting);
	setting.setDistributedConnectorSetting(distSetting);

	return setting;
    }

    /**
     * @return
     */
    public HarvestedConnectorSetting getHarvestedConnectorSetting() {

	return getSetting(//
		HARVESTED_CONNECTOR_SETTING_OPTION_KEY, //
		HarvestedConnectorSetting.class, //
		false).//
			get();
    }

    /**
     * @param type
     */
    public void setHarvestedConnectorSetting(HarvestedConnectorSetting setting) {

	setting.setIdentifier(HARVESTED_CONNECTOR_SETTING_OPTION_KEY);

	addSetting(setting);
    }

    /**
     * @return
     */
    public DistributedConnectorSetting getDistributedConnectorSetting() {

	return getSetting(//
		DISTRIBUTED_CONNECTOR_SETTING_OPTION_KEY, //
		DistributedConnectorSetting.class, //
		false).//
			get();
    }

    /**
     * @param type
     */
    public void setDistributedConnectorSetting(DistributedConnectorSetting setting) {

	setting.setIdentifier(DISTRIBUTED_CONNECTOR_SETTING_OPTION_KEY);

	addSetting(setting);
    }

    /**
     * 
     */
    protected BrokeringStrategy getBrokeringStrategy() {

	return BrokeringStrategy.MIXED;
    }

    @Override
    protected String initConnectorType() {

	return null;
    }

    @Override
    protected String initSettingName() {

	return null;
    }
}
