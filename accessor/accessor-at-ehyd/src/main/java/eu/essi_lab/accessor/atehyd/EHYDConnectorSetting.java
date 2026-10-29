package eu.essi_lab.accessor.atehyd;

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
public class EHYDConnectorSetting extends HarvestedConnectorSetting {

    public static final String EHYD_PRODUCTS = "EHYD_PRODUCTS";
    public static final String EHYD_GROUNDWATER = "EHYD_GROUNDWATER";
    public static final String EHYD_STATIONS = "EHYD_STATIONS";

    /**
     * All the surface water products
     */
    public static final String DEFAULT_PRODUCTS = EHYDProduct.getProducts(EHYDProduct.SURFACE_WATER).stream().//
	    map(EHYDProduct::getId).//
	    collect(Collectors.joining(","));

    /**
     *
     */
    public EHYDConnectorSetting() {

	Option<String> productsOption = StringOptionBuilder.get().//
		withKey(EHYD_PRODUCTS).//
		withLabel("Comma separated eHYD products to harvest (owf_wasserst_tagesmittel, owf_wasserst_monatsminima,"
			+ " owf_wasserst_monatsmaxima, owf_abfl_tagesmittel, owf_abfl_monatsminima, owf_abfl_monatsmaxima,"
			+ " owf_wassertemp_monatsmittel, owf_schwebstoff_tagesfracht, owf_geschiebe_tagesfracht, gw_monatsmittel,"
			+ " gw_jahresminima, gw_jahresmaxima, gw_temp_monatsmittel)").//
		withValue(DEFAULT_PRODUCTS).//
		cannotBeDisabled().//
		build();

	addOption(productsOption);

	Option<BooleanChoice> groundwaterOption = BooleanChoiceOptionBuilder.get().//
		withKey(EHYD_GROUNDWATER).//
		withLabel("Harvest also all the groundwater products (about 3800 stations, 13000 series)").//
		withSingleSelection().//
		withValues(LabeledEnum.values(BooleanChoice.class)).//
		withSelectedValue(BooleanChoice.FALSE).//
		cannotBeDisabled().//
		build();

	addOption(groundwaterOption);

	Option<String> stationsOption = StringOptionBuilder.get().//
		withKey(EHYD_STATIONS).//
		withLabel("Comma separated HZB numbers of the stations to harvest (empty for all the stations)").//
		withValue("").//
		cannotBeDisabled().//
		build();

	addOption(stationsOption);
    }

    /**
     * @param object
     */
    public EHYDConnectorSetting(JSONObject object) {

	super(object);
    }

    /**
     * @param object
     */
    public EHYDConnectorSetting(String object) {

	super(object);
    }

    /**
     * @return the selected products, with the groundwater products if enabled
     */
    public List<EHYDProduct> getProducts() {

	String value = getOption(EHYD_PRODUCTS, String.class).//
		map(Option::getValue).//
		orElse(DEFAULT_PRODUCTS);

	List<EHYDProduct> ret = new ArrayList<>();
	for (String id : split(value)) {
	    Optional<EHYDProduct> product = EHYDProduct.decode(id);
	    if (product.isPresent() && !ret.contains(product.get())) {
		ret.add(product.get());
	    }
	}

	if (isGroundwater()) {
	    for (EHYDProduct product : EHYDProduct.getProducts(EHYDProduct.GROUNDWATER)) {
		if (!ret.contains(product)) {
		    ret.add(product);
		}
	    }
	}

	return ret;
    }

    /**
     * @param products
     */
    public void setProducts(String products) {

	getOption(EHYD_PRODUCTS, String.class).get().setValue(products);
    }

    /**
     * @return true if the groundwater products must be harvested
     */
    public boolean isGroundwater() {

	return getOption(EHYD_GROUNDWATER, BooleanChoice.class).//
		map(option -> BooleanChoice.toBoolean(option.getSelectedValue())).//
		orElse(false);
    }

    /**
     * @param groundwater
     */
    public void setGroundwater(boolean groundwater) {

	getOption(EHYD_GROUNDWATER, BooleanChoice.class).get().select(v -> v == BooleanChoice.fromBoolean(groundwater));
    }

    /**
     * @return the HZB numbers of the stations to harvest, empty for all
     */
    public List<String> getStations() {

	return split(getOption(EHYD_STATIONS, String.class).map(Option::getValue).orElse(""));
    }

    /**
     * @param stations
     */
    public void setStations(String stations) {

	getOption(EHYD_STATIONS, String.class).get().setValue(stations);
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

	return EHYDConnector.TYPE;
    }

    @Override
    protected String initSettingName() {

	return "eHYD Connector settings";
    }
}
