package eu.essi_lab.cfga.gs.setting.connector;

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
import java.util.Properties;

import org.json.JSONObject;

import eu.essi_lab.cfga.setting.KeyValueOptionDecorator;
import eu.essi_lab.model.BrokeringStrategy;

/**
 * @author Fabrizio
 */
public abstract class HarvestedConnectorSetting extends ConnectorSetting implements KeyValueOptionDecorator {

    /**
     * 
     */
    private static final String MAX_RECORDS_OPTION_KEY = "maxRecords";

    /**
     * 
     */
    private static final String PAGE_SIZE_OPTION_KEY = "pageSize";

    /**
     * 
     */
    private static final int DEFAULT_MAX_RECORDS = 0;

    /**
     * 
     */
    private static final int DEFAULT_PAGE_SIZE = 50;

    /**
     * 
     */
    public HarvestedConnectorSetting() {

	setName(initSettingName());

	setConfigurableType(initConnectorType());

	addKeyValueOption();

	putKeyValue(MAX_RECORDS_OPTION_KEY, String.valueOf(DEFAULT_MAX_RECORDS));
	putKeyValue(PAGE_SIZE_OPTION_KEY, String.valueOf(getDefaultPageSize()));
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
    public int getPageSize() {

	return getKeyValueOptions().//
		map(p -> Integer.valueOf(p.getOrDefault(PAGE_SIZE_OPTION_KEY, DEFAULT_PAGE_SIZE).toString())).//
		orElse(DEFAULT_PAGE_SIZE);

    }

    /**
     * @param maxRecords
     */
    public void setPageSize(int pageSize) {

	putKeyValue(PAGE_SIZE_OPTION_KEY, String.valueOf(pageSize));
    }

    /**
     * @return
     */
    public Optional<Integer> getMaxRecords() {

	Integer max = getKeyValueOptions().//
		map(p -> Integer.valueOf(p.getOrDefault(MAX_RECORDS_OPTION_KEY, DEFAULT_MAX_RECORDS).toString())).//
		orElse(DEFAULT_MAX_RECORDS);

	return max == 0 ? Optional.empty() : Optional.of(max);
    }

    /**
     * @param maxRecords
     */
    public void setMaxRecords(int maxRecords) {

	putKeyValue(MAX_RECORDS_OPTION_KEY, String.valueOf(maxRecords));
    }

    /**
     * @return
     */
    public boolean isMaxRecordsUnlimited() {

	return getMaxRecords().isEmpty();
    }

    /**
     * @return
     */
    public boolean preserveIdentifiers() {

	Optional<Properties> keyValueOptions = getKeyValueOptions();

	if (keyValueOptions.isPresent()) {
	    
	    return Boolean.valueOf(//
		    keyValueOptions.get().getProperty("preserveIds", "false"));
	}

	return false;
    }

    /**
     * 
     */
    protected BrokeringStrategy getBrokeringStrategy() {

	return BrokeringStrategy.HARVESTED;
    }

    /**
     * @return
     */
    protected int getDefaultPageSize() {

	return DEFAULT_PAGE_SIZE;
    }

}
