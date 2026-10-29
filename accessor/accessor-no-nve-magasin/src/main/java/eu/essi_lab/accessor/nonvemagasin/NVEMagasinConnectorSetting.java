package eu.essi_lab.accessor.nonvemagasin;

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
import eu.essi_lab.cfga.option.Option;
import eu.essi_lab.cfga.option.StringOptionBuilder;

/**
 * @author boldrini
 */
public class NVEMagasinConnectorSetting extends HarvestedConnectorSetting {

    public static final String NVE_MAGASIN_PROPERTIES = "NVE_MAGASIN_PROPERTIES";
    public static final String NVE_MAGASIN_AREA_TYPES = "NVE_MAGASIN_AREA_TYPES";

    public static final String DEFAULT_PROPERTIES = "fyllingsgrad,fylling_TWh,kapasitet_TWh";
    public static final String DEFAULT_AREA_TYPES = "NO,EL,VASS";

    /**
     *
     */
    public NVEMagasinConnectorSetting() {

	Option<String> propertiesOption = StringOptionBuilder.get().//
		withKey(NVE_MAGASIN_PROPERTIES).//
		withLabel("Comma separated properties to harvest (fyllingsgrad = filling ratio, fylling_TWh = filling in TWh,"
			+ " kapasitet_TWh = capacity in TWh)").//
		withValue(DEFAULT_PROPERTIES).//
		required().//
		cannotBeDisabled().//
		build();

	addOption(propertiesOption);

	Option<String> areaTypesOption = StringOptionBuilder.get().//
		withKey(NVE_MAGASIN_AREA_TYPES).//
		withLabel("Comma separated area types to harvest (NO = whole country, EL = electricity price areas NO1-NO5,"
			+ " VASS = watercourse regions; empty for all)").//
		withValue(DEFAULT_AREA_TYPES).//
		cannotBeDisabled().//
		build();

	addOption(areaTypesOption);
    }

    /**
     * @param object
     */
    public NVEMagasinConnectorSetting(JSONObject object) {

	super(object);
    }

    /**
     * @param object
     */
    public NVEMagasinConnectorSetting(String object) {

	super(object);
    }

    /**
     * @return
     */
    public List<NVEMagasinProperty> getMagasinProperties() {

	String value = getOption(NVE_MAGASIN_PROPERTIES, String.class).//
		map(Option::getValue).//
		orElse(DEFAULT_PROPERTIES);

	List<NVEMagasinProperty> ret = new ArrayList<>();
	for (String code : split(value)) {
	    Optional<NVEMagasinProperty> property = NVEMagasinProperty.decode(code);
	    if (property.isPresent() && !ret.contains(property.get())) {
		ret.add(property.get());
	    }
	}

	return ret;
    }

    /**
     * @param properties
     */
    public void setMagasinProperties(String properties) {

	getOption(NVE_MAGASIN_PROPERTIES, String.class).get().setValue(properties);
    }

    /**
     * @return the area types to harvest, empty for all
     */
    public List<String> getAreaTypes() {

	return split(getOption(NVE_MAGASIN_AREA_TYPES, String.class).map(Option::getValue).orElse(DEFAULT_AREA_TYPES));
    }

    /**
     * @param areaTypes
     */
    public void setAreaTypes(String areaTypes) {

	getOption(NVE_MAGASIN_AREA_TYPES, String.class).get().setValue(areaTypes);
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

	return NVEMagasinConnector.TYPE;
    }

    @Override
    protected String initSettingName() {

	return "NVE Magasinstatistikk Connector settings";
    }
}
