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

import java.util.Optional;

import org.json.JSONObject;

import eu.essi_lab.cfga.option.IntegerOptionBuilder;
import eu.essi_lab.cfga.option.Option;
import eu.essi_lab.model.BrokeringStrategy;

/**
 * @author Fabrizio
 */
public abstract class HarvestedConnectorSetting extends ConnectorSetting {

    /**
     * 
     */
    private static final String MAX_RECORDS_OPTION_KEY = "maxRecords";

    /**
     * 
     */
    private static final String PAGE_SIZE_OPTION_KEY = "pageSize";

    public HarvestedConnectorSetting() {

	setName(initSettingName());

	setConfigurableType(initConnectorType());

	{

	    Option<Integer> option = IntegerOptionBuilder.get().//
		    withLabel("Max records").//
		    withKey(MAX_RECORDS_OPTION_KEY).//
		    withDescription("Leave unset to harvest all available source records").//
		    cannotBeDisabled().//
		    build();

	    addOption(option);
	}

	{

	    Option<Integer> option = IntegerOptionBuilder.get().//
		    withLabel("Page size").//
		    withKey(PAGE_SIZE_OPTION_KEY).//
		    withValue(50).//
		    cannotBeDisabled().//
		    build();

	    addOption(option);
	}

    }

    /**
     * @param name
     * @param configurableType
     * @return
     */
    public static HarvestedConnectorSetting create(String name, String configurableType) {

	return new HarvestedConnectorSetting() {

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
    public HarvestedConnectorSetting(JSONObject object) {

	super(object);
    }

    /**
     * @param object
     */
    public HarvestedConnectorSetting(String object) {

	super(object);
    }

    /**
     * Returns <code>true</code> (default) to show the max records option, <code>false</code> otherwise
     */
    public boolean enableMaxRecordsOption() {

	return true;
    }

    /**
     * @return
     */
    public int getSelectedPageSize() {

	return getOption(PAGE_SIZE_OPTION_KEY, Integer.class).get().getValue();

    }

    /**
     * @param maxRecords
     */
    public void selectPageSize(int pageSize) {

	getOption(PAGE_SIZE_OPTION_KEY, Integer.class).get().setValue(pageSize);
    }

    /**
     * @return
     */
    public Optional<Integer> getMaxRecords() {

	return getOption(MAX_RECORDS_OPTION_KEY, Integer.class).get().getOptionalValue();
    }

    /**
     * @param maxRecords
     */
    public void setMaxRecords(int maxRecords) {

	getOption(MAX_RECORDS_OPTION_KEY, Integer.class).get().setValue(maxRecords);
    }

    /**
     * @return
     */
    public boolean isMaxRecordsUnlimited() {

	return !getMaxRecords().isPresent();
    }

    /**
     * 
     */
    protected BrokeringStrategy getBrokeringStrategy() {

	return BrokeringStrategy.HARVESTED;
    }

}
