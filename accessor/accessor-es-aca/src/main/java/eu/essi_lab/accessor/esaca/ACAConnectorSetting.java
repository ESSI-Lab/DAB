package eu.essi_lab.accessor.esaca;

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
import java.util.stream.Collectors;

import org.json.JSONObject;

import eu.essi_lab.cfga.gs.setting.connector.HarvestedConnectorSetting;
import eu.essi_lab.cfga.option.Option;
import eu.essi_lab.cfga.option.StringOptionBuilder;

/**
 * @author boldrini
 */
public class ACAConnectorSetting extends HarvestedConnectorSetting {

    public static final String ACA_COMPONENT_TYPES = "ACA_COMPONENT_TYPES";
    public static final String ACA_COMPONENTS = "ACA_COMPONENTS";

    /**
     * River gauging stations, reservoirs, piezometers and rain gauges
     */
    public static final String DEFAULT_COMPONENT_TYPES = "aforament,embassament,piezometre,pluviometre";

    /**
     *
     */
    public ACAConnectorSetting() {

	Option<String> typesOption = StringOptionBuilder.get().//
		withKey(ACA_COMPONENT_TYPES).//
		withLabel("Comma separated component types to harvest (aforament = river gauging stations, embassament = reservoirs,"
			+ " piezometre = piezometers, pluviometre = rain gauges)").//
		withValue(DEFAULT_COMPONENT_TYPES).//
		required().//
		cannotBeDisabled().//
		build();

	addOption(typesOption);

	Option<String> componentsOption = StringOptionBuilder.get().//
		withKey(ACA_COMPONENTS).//
		withLabel("Comma separated component (station) codes to harvest, e.g. 080060-001 (empty for all the components)").//
		withValue("").//
		cannotBeDisabled().//
		build();

	addOption(componentsOption);
    }

    /**
     * @param object
     */
    public ACAConnectorSetting(JSONObject object) {

	super(object);
    }

    /**
     * @param object
     */
    public ACAConnectorSetting(String object) {

	super(object);
    }

    /**
     * @return
     */
    public List<String> getComponentTypes() {

	List<String> ret = split(getOption(ACA_COMPONENT_TYPES, String.class).//
		map(Option::getValue).//
		orElse(DEFAULT_COMPONENT_TYPES)).//
		stream().//
		map(String::toLowerCase).//
		distinct().//
		collect(Collectors.toList());

	return ret.isEmpty() ? split(DEFAULT_COMPONENT_TYPES) : ret;
    }

    /**
     * @param types
     */
    public void setComponentTypes(String types) {

	getOption(ACA_COMPONENT_TYPES, String.class).get().setValue(types);
    }

    /**
     * @return the component codes to harvest, empty for all
     */
    public List<String> getComponents() {

	return split(getOption(ACA_COMPONENTS, String.class).map(Option::getValue).orElse(""));
    }

    /**
     * @param components
     */
    public void setComponents(String components) {

	getOption(ACA_COMPONENTS, String.class).get().setValue(components);
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

	return ACAConnector.TYPE;
    }

    @Override
    protected String initSettingName() {

	return "ACA Connector settings";
    }
}
