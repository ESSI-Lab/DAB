package eu.essi_lab.accessor.derlpwasser;

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.json.JSONObject;

import eu.essi_lab.cfga.gs.setting.connector.HarvestedConnectorSetting;
import eu.essi_lab.cfga.option.BooleanChoice;
import eu.essi_lab.cfga.option.BooleanChoiceOptionBuilder;
import eu.essi_lab.cfga.option.Option;
import eu.essi_lab.cfga.option.StringOptionBuilder;
import eu.essi_lab.lib.utils.LabeledEnum;

/**
 * @author boldrini
 */
public class RLPWasserConnectorSetting extends HarvestedConnectorSetting {

    public static final String RLP_PARAMETERS = "RLP_PARAMETERS";
    public static final String RLP_SITES = "RLP_SITES";
    public static final String RLP_INCLUDE_MUNICIPAL = "RLP_INCLUDE_MUNICIPAL";
    public static final String RLP_INCLUDE_EXTERNAL = "RLP_INCLUDE_EXTERNAL";

    public static final String DEFAULT_PARAMETERS = "W,Q";

    /**
     *
     */
    public RLPWasserConnectorSetting() {

	Option<String> parametersOption = StringOptionBuilder.get().//
		withKey(RLP_PARAMETERS).//
		withLabel("Comma separated parameters to harvest (W = water level, Q = discharge)").//
		withValue(DEFAULT_PARAMETERS).//
		required().//
		cannotBeDisabled().//
		build();

	addOption(parametersOption);

	Option<String> sitesOption = StringOptionBuilder.get().//
		withKey(RLP_SITES).//
		withLabel("Comma separated measurement site numbers to harvest (empty for all the sites)").//
		withValue("").//
		cannotBeDisabled().//
		build();

	addOption(sitesOption);

	Option<BooleanChoice> municipalOption = BooleanChoiceOptionBuilder.get().//
		withKey(RLP_INCLUDE_MUNICIPAL).//
		withLabel("Include the measurement sites operated by municipalities and districts (Kommunale Pegel)").//
		withSingleSelection().//
		withValues(LabeledEnum.values(BooleanChoice.class)).//
		withSelectedValue(BooleanChoice.TRUE).//
		cannotBeDisabled().//
		build();

	addOption(municipalOption);

	Option<BooleanChoice> externalOption = BooleanChoiceOptionBuilder.get().//
		withKey(RLP_INCLUDE_EXTERNAL).//
		withLabel("Include the measurement sites of other agencies (federal waterways, neighbouring states and countries)").//
		withSingleSelection().//
		withValues(LabeledEnum.values(BooleanChoice.class)).//
		withSelectedValue(BooleanChoice.TRUE).//
		cannotBeDisabled().//
		build();

	addOption(externalOption);
    }

    /**
     * @param object
     */
    public RLPWasserConnectorSetting(JSONObject object) {

	super(object);
    }

    /**
     * @param object
     */
    public RLPWasserConnectorSetting(String object) {

	super(object);
    }

    /**
     * @return
     */
    public List<RLPWasserParameter> getParameters() {

	String value = getOption(RLP_PARAMETERS, String.class).//
		map(Option::getValue).//
		orElse(DEFAULT_PARAMETERS);

	List<RLPWasserParameter> ret = new ArrayList<>();
	for (String code : split(value)) {
	    Optional<RLPWasserParameter> parameter = RLPWasserParameter.decode(code);
	    if (parameter.isPresent() && !ret.contains(parameter.get())) {
		ret.add(parameter.get());
	    }
	}

	return ret;
    }

    /**
     * @param parameters
     */
    public void setParameters(String parameters) {

	getOption(RLP_PARAMETERS, String.class).get().setValue(parameters);
    }

    /**
     * @return the site numbers to harvest, empty for all
     */
    public List<String> getSites() {

	return split(getOption(RLP_SITES, String.class).map(Option::getValue).orElse(""));
    }

    /**
     * @param sites
     */
    public void setSites(String sites) {

	getOption(RLP_SITES, String.class).get().setValue(sites);
    }

    /**
     * @return
     */
    public boolean isIncludeMunicipal() {

	return getBoolean(RLP_INCLUDE_MUNICIPAL);
    }

    /**
     * @param include
     */
    public void setIncludeMunicipal(boolean include) {

	getOption(RLP_INCLUDE_MUNICIPAL, BooleanChoice.class).get().select(v -> v == BooleanChoice.fromBoolean(include));
    }

    /**
     * @return
     */
    public boolean isIncludeExternal() {

	return getBoolean(RLP_INCLUDE_EXTERNAL);
    }

    /**
     * @param include
     */
    public void setIncludeExternal(boolean include) {

	getOption(RLP_INCLUDE_EXTERNAL, BooleanChoice.class).get().select(v -> v == BooleanChoice.fromBoolean(include));
    }

    /**
     * @param key
     * @return
     */
    private boolean getBoolean(String key) {

	return getOption(key, BooleanChoice.class).//
		map(option -> BooleanChoice.toBoolean(option.getSelectedValue())).//
		orElse(true);
    }

    /**
     * @param value
     * @return
     */
    private static List<String> split(String value) {

	if (value == null) {
	    return new ArrayList<>();
	}

	return Arrays.stream(value.split(",")).//
		map(String::trim).//
		filter(v -> !v.isEmpty()).//
		collect(Collectors.toList());
    }

    @Override
    protected String initConnectorType() {

	return RLPWasserConnector.TYPE;
    }

    @Override
    protected String initSettingName() {

	return "RLP Wasser Connector settings";
    }
}
