package eu.essi_lab.accessor.nlrijkswaterstaat;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Technologies and Environmental Intelligence (ITIAm)/ESSI-Lab
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

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.json.JSONObject;

import eu.essi_lab.cfga.gs.setting.connector.HarvestedConnectorSetting;
import eu.essi_lab.cfga.option.BooleanChoice;
import eu.essi_lab.cfga.option.BooleanChoiceOptionBuilder;
import eu.essi_lab.cfga.option.IntegerOptionBuilder;
import eu.essi_lab.cfga.option.Option;
import eu.essi_lab.cfga.option.StringOptionBuilder;
import eu.essi_lab.lib.utils.LabeledEnum;

/**
 * @author boldrini
 */
public class RijkswaterstaatConnectorSetting extends HarvestedConnectorSetting {

    public static final String RWS_BEGIN_SEARCH_MAX_REQUESTS = "RWS_BEGIN_SEARCH_MAX_REQUESTS";
    public static final String RWS_EXACT_BEGIN = "RWS_EXACT_BEGIN";
    public static final String RWS_PROCESS_TYPES = "RWS_PROCESS_TYPES";
    public static final String RWS_LOCATION_CODE_PREFIX = "RWS_LOCATION_CODE_PREFIX";

    /**
     * With 6 requests the begin of a series is approximated within (end - 1700) / 64, i.e. about 5 years
     */
    public static final int DEFAULT_BEGIN_SEARCH_MAX_REQUESTS = 6;

    /**
     *
     */
    public RijkswaterstaatConnectorSetting() {

	Option<Integer> maxRequestsOption = IntegerOptionBuilder.get().//
		withKey(RWS_BEGIN_SEARCH_MAX_REQUESTS).//
		withLabel("Maximum number of requests per series to approximate the time of its first observation (0 = no search)").//
		withValue(DEFAULT_BEGIN_SEARCH_MAX_REQUESTS).//
		cannotBeDisabled().//
		build();

	addOption(maxRequestsOption);

	Option<BooleanChoice> exactBeginOption = BooleanChoiceOptionBuilder.get().//
		withKey(RWS_EXACT_BEGIN).//
		withLabel("Exact time of the first observation: refine the search until the interval is shorter than one day, then"
			+ " download it (about 20 requests per series, the maximum number of requests is ignored)").//
		withSingleSelection().//
		withValues(LabeledEnum.values(BooleanChoice.class)).//
		withSelectedValue(BooleanChoice.FALSE).//
		cannotBeDisabled().//
		build();

	addOption(exactBeginOption);

	Option<String> processTypesOption = StringOptionBuilder.get().//
		withKey(RWS_PROCESS_TYPES).//
		withLabel("Comma separated process types to harvest (meting = measurements, verwachting = forecasts, astronomisch = astronomical)").//
		withValue(RijkswaterstaatClient.MEASUREMENT_PROCESS_TYPE).//
		required().//
		cannotBeDisabled().//
		build();

	addOption(processTypesOption);

	Option<String> locationPrefixOption = StringOptionBuilder.get().//
		withKey(RWS_LOCATION_CODE_PREFIX).//
		withLabel("Location code prefix (empty for all locations)").//
		withValue("").//
		cannotBeDisabled().//
		build();

	addOption(locationPrefixOption);
    }

    /**
     * @param object
     */
    public RijkswaterstaatConnectorSetting(JSONObject object) {

	super(object);
    }

    /**
     * @param object
     */
    public RijkswaterstaatConnectorSetting(String object) {

	super(object);
    }

    /**
     * @return
     */
    public int getBeginSearchMaxRequests() {

	return getOption(RWS_BEGIN_SEARCH_MAX_REQUESTS, Integer.class).//
		map(Option::getValue).//
		orElse(DEFAULT_BEGIN_SEARCH_MAX_REQUESTS);
    }

    /**
     * @return true if the time of the first observation of each series must be found exactly
     */
    public boolean isExactBegin() {

	return getOption(RWS_EXACT_BEGIN, BooleanChoice.class).//
		map(option -> BooleanChoice.toBoolean(option.getSelectedValue())).//
		orElse(false);
    }

    /**
     * @param exact
     */
    public void setExactBegin(boolean exact) {

	getOption(RWS_EXACT_BEGIN, BooleanChoice.class).get().select(v -> v == BooleanChoice.fromBoolean(exact));
    }

    /**
     * @param maxRequests
     */
    public void setBeginSearchMaxRequests(int maxRequests) {

	getOption(RWS_BEGIN_SEARCH_MAX_REQUESTS, Integer.class).get().setValue(maxRequests);
    }

    /**
     * @return
     */
    public List<String> getProcessTypes() {

	String value = getOption(RWS_PROCESS_TYPES, String.class).//
		map(Option::getValue).//
		orElse(RijkswaterstaatClient.MEASUREMENT_PROCESS_TYPE);

	return Arrays.stream(value.split(",")).//
		map(String::trim).//
		filter(type -> !type.isEmpty()).//
		collect(Collectors.toList());
    }

    /**
     * @return
     */
    public String getLocationCodePrefix() {

	return getOption(RWS_LOCATION_CODE_PREFIX, String.class).map(Option::getValue).orElse("");
    }

    /**
     * @param prefix
     */
    public void setLocationCodePrefix(String prefix) {

	getOption(RWS_LOCATION_CODE_PREFIX, String.class).get().setValue(prefix);
    }

    @Override
    protected String initConnectorType() {

	return RijkswaterstaatConnector.TYPE;
    }

    @Override
    protected String initSettingName() {

	return "Rijkswaterstaat Connector settings";
    }
}
