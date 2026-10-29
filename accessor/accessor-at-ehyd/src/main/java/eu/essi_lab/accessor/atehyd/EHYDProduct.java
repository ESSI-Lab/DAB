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
import java.util.List;
import java.util.Optional;

import eu.essi_lab.model.resource.InterpolationType;

/**
 * Archive products (checked data published in the hydrographic yearbooks) downloadable from eHYD as CSV files, one file per
 * station and product. The identifier of each product is the file type used by the eHYD station list (e.g.
 * "owf_wasserst_tagesmittel"), its prefix is the station category ("owf" = surface water, "gw" = groundwater)
 *
 * @author boldrini
 */
public enum EHYDProduct {

    /**
     * Daily mean water level
     */
    OWF_WATER_LEVEL_DAILY_MEAN("owf_wasserst_tagesmittel", "W-Tagesmittel", "Water level", "daily mean", "cm",
	    InterpolationType.AVERAGE_SUCC, "P1D"), //
    /**
     * Monthly minimum water level (the time of the minimum is given)
     */
    OWF_WATER_LEVEL_MONTHLY_MIN("owf_wasserst_monatsminima", "W-Monatsminima", "Water level", "monthly minimum", "cm",
	    InterpolationType.MIN, "P1M"), //
    /**
     * Monthly maximum water level (the time of the maximum is given)
     */
    OWF_WATER_LEVEL_MONTHLY_MAX("owf_wasserst_monatsmaxima", "W-Monatsmaxima", "Water level", "monthly maximum", "cm",
	    InterpolationType.MAX, "P1M"), //
    /**
     * Daily mean discharge
     */
    OWF_DISCHARGE_DAILY_MEAN("owf_abfl_tagesmittel", "Q-Tagesmittel", "Discharge", "daily mean", "m³/s", InterpolationType.AVERAGE_SUCC,
	    "P1D"), //
    /**
     * Monthly minimum discharge (the time of the minimum is given)
     */
    OWF_DISCHARGE_MONTHLY_MIN("owf_abfl_monatsminima", "Q-Monatsminima", "Discharge", "monthly minimum", "m³/s", InterpolationType.MIN,
	    "P1M"), //
    /**
     * Monthly maximum discharge (the time of the maximum is given)
     */
    OWF_DISCHARGE_MONTHLY_MAX("owf_abfl_monatsmaxima", "Q-Monatsmaxima", "Discharge", "monthly maximum", "m³/s", InterpolationType.MAX,
	    "P1M"), //
    /**
     * Monthly mean water temperature
     */
    OWF_WATER_TEMPERATURE_MONTHLY_MEAN("owf_wassertemp_monatsmittel", "WT-Monatsmittel", "Water temperature", "monthly mean", "°C",
	    InterpolationType.AVERAGE_SUCC, "P1M"), //
    /**
     * Daily suspended sediment load
     */
    OWF_SUSPENDED_SEDIMENT_DAILY_LOAD("owf_schwebstoff_tagesfracht", "Schwebstoff-Tagesfracht", "Suspended sediment load", "daily total",
	    "t", InterpolationType.TOTAL_SUCC, "P1D"), //
    /**
     * Daily bedload
     */
    OWF_BEDLOAD_DAILY_LOAD("owf_geschiebe_tagesfracht", "Geschiebe-Tagesfracht", "Bedload", "daily total", "t",
	    InterpolationType.TOTAL_SUCC, "P1D"), //
    /**
     * Monthly mean groundwater level
     */
    GW_LEVEL_MONTHLY_MEAN("gw_monatsmittel", "Grundwasserstand-Monatsmittel", "Groundwater level", "monthly mean", "m ü.A.",
	    InterpolationType.AVERAGE_SUCC, "P1M"), //
    /**
     * Annual minimum groundwater level (the time of the minimum is given)
     */
    GW_LEVEL_ANNUAL_MIN("gw_jahresminima", "Grundwasserstand-Jahresminima", "Groundwater level", "annual minimum", "m ü.A.",
	    InterpolationType.MIN, "P1Y"), //
    /**
     * Annual maximum groundwater level (the time of the maximum is given)
     */
    GW_LEVEL_ANNUAL_MAX("gw_jahresmaxima", "Grundwasserstand-Jahresmaxima", "Groundwater level", "annual maximum", "m ü.A.",
	    InterpolationType.MAX, "P1Y"), //
    /**
     * Monthly mean groundwater temperature
     */
    GW_TEMPERATURE_MONTHLY_MEAN("gw_temp_monatsmittel", "Grundwassertemperatur-Monatsmittel", "Groundwater temperature", "monthly mean",
	    "°C", InterpolationType.AVERAGE_SUCC, "P1M");

    /**
     * Station category of surface water products
     */
    public static final String SURFACE_WATER = "owf";

    /**
     * Station category of groundwater products
     */
    public static final String GROUNDWATER = "gw";

    private final String id;
    private final String fileName;
    private final String property;
    private final String statistic;
    private final String defaultUnit;
    private final InterpolationType interpolation;
    private final String aggregation;

    /**
     * @param id
     * @param fileName
     * @param property
     * @param statistic
     * @param defaultUnit
     * @param interpolation
     * @param aggregation
     */
    private EHYDProduct(String id, String fileName, String property, String statistic, String defaultUnit,
	    InterpolationType interpolation, String aggregation) {

	this.id = id;
	this.fileName = fileName;
	this.property = property;
	this.statistic = statistic;
	this.defaultUnit = defaultUnit;
	this.interpolation = interpolation;
	this.aggregation = aggregation;
    }

    /**
     * @return the file type used by eHYD, e.g. "owf_wasserst_tagesmittel"
     */
    public String getId() {

	return id;
    }

    /**
     * @return the station category, e.g. "owf"
     */
    public String getCategory() {

	return id.substring(0, id.indexOf('_'));
    }

    /**
     * @return the German name of the product, also used as file name prefix (e.g. "W-Tagesmittel" for
     *         "W-Tagesmittel-201582.csv")
     */
    public String getFileName() {

	return fileName;
    }

    /**
     * @param hzbnr
     * @return the name of the file of the given station, e.g. "W-Tagesmittel-201582.csv"
     */
    public String getFileName(String hzbnr) {

	return fileName + "-" + hzbnr + ".csv";
    }

    /**
     * @return the English name of the observed property, e.g. "Water level"
     */
    public String getProperty() {

	return property;
    }

    /**
     * @return e.g. "daily mean"
     */
    public String getStatistic() {

	return statistic;
    }

    /**
     * @return e.g. "Water level, daily mean"
     */
    public String getLabel() {

	return property + ", " + statistic;
    }

    /**
     * @return the unit used when the file header does not report it
     */
    public String getDefaultUnit() {

	return defaultUnit;
    }

    /**
     * @return
     */
    public InterpolationType getInterpolation() {

	return interpolation;
    }

    /**
     * @return the aggregation period (ISO 8601 duration), e.g. "P1D"
     */
    public String getAggregation() {

	return aggregation;
    }

    /**
     * @param category
     * @return the products of the given station category
     */
    public static List<EHYDProduct> getProducts(String category) {

	List<EHYDProduct> ret = new ArrayList<>();
	for (EHYDProduct product : values()) {
	    if (product.getCategory().equals(category)) {
		ret.add(product);
	    }
	}

	return ret;
    }

    /**
     * @param id the product identifier (e.g. "owf_wasserst_tagesmittel") or the enum name
     * @return
     */
    public static Optional<EHYDProduct> decode(String id) {

	if (id == null) {
	    return Optional.empty();
	}

	for (EHYDProduct product : values()) {
	    if (product.id.equalsIgnoreCase(id.trim()) || product.name().equalsIgnoreCase(id.trim())) {
		return Optional.of(product);
	    }
	}

	return Optional.empty();
    }
}
