package eu.essi_lab.accessor.kisters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

import org.json.JSONObject;

import eu.essi_lab.cfga.gs.setting.connector.HarvestedConnectorSetting;
import eu.essi_lab.cfga.option.BooleanChoice;
import eu.essi_lab.cfga.option.BooleanChoiceOptionBuilder;
import eu.essi_lab.cfga.option.Option;
import eu.essi_lab.cfga.option.StringOptionBuilder;
import eu.essi_lab.lib.utils.LabeledEnum;

/**
 * @author Fabrizio
 */
public class KISTERSConnectorSetting extends HarvestedConnectorSetting {

    public final String KISTERS_SITE_NAME = "KISTERS_SITE_NAME";
    public static final String KISTERS_TS_GROUP_IDS = "KISTERS_TS_GROUP_IDS";
    public static final String KISTERS_TS_NAME_REGEX = "KISTERS_TS_NAME_REGEX";

    /**
     * 
     */
    public KISTERSConnectorSetting() {
	Option<String> option = StringOptionBuilder.get().//
		withKey(KISTERS_SITE_NAME).//
		withLabel("Site name (empty for all sites)").//
		withValue("").//
		required().//
		cannotBeDisabled().//
		build();

	addOption(option);

	Option<String> groupsOption = StringOptionBuilder.get().//
		withKey(KISTERS_TS_GROUP_IDS).//
		withLabel("Comma separated identifiers of the time series groups to harvest (empty to harvest all the time series of"
			+ " each station)").//
		withValue("").//
		cannotBeDisabled().//
		build();

	addOption(groupsOption);

	Option<String> regexOption = StringOptionBuilder.get().//
		withKey(KISTERS_TS_NAME_REGEX).//
		withLabel("Regular expression the time series name must match (empty for all time series)").//
		withValue("").//
		cannotBeDisabled().//
		build();

	addOption(regexOption);
    }

    /**
     * @return
     */
    public String getSiteName() {

	return getOption(KISTERS_SITE_NAME, String.class).get().getValue();
    }

    /**
     * @return the identifiers of the time series groups to harvest, an empty list if all the time series of each station must be
     *         harvested (settings created before this option was added have no such option)
     */
    public List<String> getTimeSeriesGroupIds() {

	String value = getOption(KISTERS_TS_GROUP_IDS, String.class).//
		map(Option::getValue).//
		orElse("");

	if (value == null) {

	    return new ArrayList<>();
	}

	return Arrays.stream(value.split(",")).//
		map(String::trim).//
		filter(v -> !v.isEmpty()).//
		collect(Collectors.toList());
    }

    /**
     * @param groupIds
     */
    public void setTimeSeriesGroupIds(String groupIds) {

	getOption(KISTERS_TS_GROUP_IDS, String.class).get().setValue(groupIds);
    }

    /**
     * @return the regular expression the time series name must match, if set
     */
    public Optional<String> getTimeSeriesNameRegex() {

	return getOption(KISTERS_TS_NAME_REGEX, String.class).//
		map(Option::getValue).//
		map(String::trim).//
		filter(v -> !v.isEmpty());
    }

    /**
     * @param regex
     */
    public void setTimeSeriesNameRegex(String regex) {

	getOption(KISTERS_TS_NAME_REGEX, String.class).get().setValue(regex);
    }

    /**
     * @param object
     */
    public KISTERSConnectorSetting(JSONObject object) {

	super(object);
    }

    /**
     * @param object
     */
    public KISTERSConnectorSetting(String object) {

	super(object);
    }

    @Override
    protected String initConnectorType() {

	return KISTERSConnector.TYPE;
    }

    @Override
    protected String initSettingName() {

	return "KISTERS Connector settings";
    }

}
