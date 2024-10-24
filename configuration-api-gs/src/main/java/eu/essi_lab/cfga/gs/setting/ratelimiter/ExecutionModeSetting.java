package eu.essi_lab.cfga.gs.setting.ratelimiter;

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

import eu.essi_lab.cfga.option.IntegerOptionBuilder;
import eu.essi_lab.cfga.option.Option;
import eu.essi_lab.cfga.option.StringOptionBuilder;
import eu.essi_lab.cfga.setting.Setting;

/**
 * @author Fabrizio
 */
public class ExecutionModeSetting extends Setting {

    private static final String MAX_REQUESTS_PER_IP_OPTION_KEY = "execModeMaxReqPerIPOption";
    private static final String MAX_CONCURRENT_REQUESTS_OPTION_KEY = "execModeMaxConcurrentReqOption";
    private static final String MAX_CONCURRENT_REQUESTS_PER_IP_OPTION_KEY = "execModeMaxConcurrentReqPerIPOption";
    private static final String DB_OPTION_KEY = "execModeDBOption";

    /**
     * 
     */
    public ExecutionModeSetting() {

	enableCompactMode(true);

	setCanBeDisabled(false);
	setEditable(false);

	Option<Integer> maxRequestsPerIP = IntegerOptionBuilder.get().//
		withKey(MAX_REQUESTS_PER_IP_OPTION_KEY).//
		withLabel("Max overall requests per IP").//
		withValue(10).//
		cannotBeDisabled().//
		build();

	addOption(maxRequestsPerIP);

	Option<Integer> maxConcurrentRequests = IntegerOptionBuilder.get().//
		withKey(MAX_CONCURRENT_REQUESTS_OPTION_KEY).//
		withLabel("Max concurrent requests").//
		withValue(5).//
		cannotBeDisabled().//
		build();

	addOption(maxConcurrentRequests);

	Option<Integer> maxConcurrentRequestsPerIP = IntegerOptionBuilder.get().//
		withKey(MAX_CONCURRENT_REQUESTS_PER_IP_OPTION_KEY).//
		withLabel("Max concurrent requests per IP").//
		withValue(1).//
		cannotBeDisabled().//
		build();

	addOption(maxConcurrentRequestsPerIP);

	Option<String> db = StringOptionBuilder.get().//
		withKey(DB_OPTION_KEY).//
		withLabel("DB").//
		withValue("default").//
		cannotBeDisabled().//
		build();

	addOption(db);
    }

    /**
     * @return
     */
    public Integer getMaxRequestsPerIP() {

	return getOption(MAX_REQUESTS_PER_IP_OPTION_KEY, Integer.class).get().getValue();
    }

    /**
     * @return
     */
    public Integer getMaxConcurrentRequests() {

	return getOption(MAX_CONCURRENT_REQUESTS_OPTION_KEY, Integer.class).get().getValue();
    }

    /**
     * @return
     */
    public Integer getMaxConcurrentRequestsPerIP() {

	return getOption(MAX_CONCURRENT_REQUESTS_PER_IP_OPTION_KEY, Integer.class).get().getValue();
    }

    /**
     * @return
     */
    public String getDB() {

	return getOption(DB_OPTION_KEY, String.class).get().getValue();
    }
}
