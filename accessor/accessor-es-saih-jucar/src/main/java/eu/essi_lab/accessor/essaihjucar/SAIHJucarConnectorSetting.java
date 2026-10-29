package eu.essi_lab.accessor.essaihjucar;

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

import eu.essi_lab.accessor.essaihjucar.SAIHJucarVariableType.SAIHJucarCategory;
import eu.essi_lab.cfga.gs.setting.connector.HarvestedConnectorSetting;
import eu.essi_lab.cfga.option.Option;
import eu.essi_lab.cfga.option.StringOptionBuilder;

/**
 * @author boldrini
 */
public class SAIHJucarConnectorSetting extends HarvestedConnectorSetting {

    public static final String SAIH_JUCAR_CATEGORIES = "SAIH_JUCAR_CATEGORIES";
    public static final String SAIH_JUCAR_VARIABLES = "SAIH_JUCAR_VARIABLES";

    /**
     *
     */
    public static final String DEFAULT_CATEGORIES = "AFOROS,EMBALSES,LLUVIAS";

    /**
     *
     */
    public SAIHJucarConnectorSetting() {

	Option<String> categoriesOption = StringOptionBuilder.get().//
		withKey(SAIH_JUCAR_CATEGORIES).//
		withLabel("Comma separated categories to harvest (AFOROS = river discharge, EMBALSES = reservoir level, volume, inflow"
			+ " and outflows, LLUVIAS = precipitation intensity)").//
		withValue(DEFAULT_CATEGORIES).//
		required().//
		cannotBeDisabled().//
		build();

	addOption(categoriesOption);

	Option<String> variablesOption = StringOptionBuilder.get().//
		withKey(SAIH_JUCAR_VARIABLES).//
		withLabel("Comma separated variable identifiers (idVariable) to harvest (empty for all the variables of the categories)").//
		withValue("").//
		cannotBeDisabled().//
		build();

	addOption(variablesOption);
    }

    /**
     * @param object
     */
    public SAIHJucarConnectorSetting(JSONObject object) {

	super(object);
    }

    /**
     * @param object
     */
    public SAIHJucarConnectorSetting(String object) {

	super(object);
    }

    /**
     * @return
     */
    public List<SAIHJucarCategory> getCategories() {

	String value = getOption(SAIH_JUCAR_CATEGORIES, String.class).//
		map(Option::getValue).//
		orElse(DEFAULT_CATEGORIES);

	List<SAIHJucarCategory> ret = new ArrayList<>();
	for (String name : split(value)) {
	    Optional<SAIHJucarCategory> category = SAIHJucarCategory.decode(name);
	    if (category.isPresent() && !ret.contains(category.get())) {
		ret.add(category.get());
	    }
	}

	return ret;
    }

    /**
     * @param categories
     */
    public void setCategories(String categories) {

	getOption(SAIH_JUCAR_CATEGORIES, String.class).get().setValue(categories);
    }

    /**
     * @return the identifiers of the variables to harvest, empty for all
     */
    public List<String> getVariables() {

	return split(getOption(SAIH_JUCAR_VARIABLES, String.class).map(Option::getValue).orElse(""));
    }

    /**
     * @param variables
     */
    public void setVariables(String variables) {

	getOption(SAIH_JUCAR_VARIABLES, String.class).get().setValue(variables);
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

	return SAIHJucarConnector.TYPE;
    }

    @Override
    protected String initSettingName() {

	return "SAIH Júcar Connector settings";
    }
}
