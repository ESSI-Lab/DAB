package eu.essi_lab.accessor.czchmi;

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
 * The CHMI time series types (TSCON_ID of the metadata files meta2.json), with the provider (Czech) description and unit. The model
 * forecasts of the operational dataset (H_F, Q_F) are not included
 *
 * @author boldrini
 */
public enum CHMIParameter {

    /**
     * Operational water level, completed with the validated instantaneous water levels of the historical archive
     */
    H(CHMIProduct.OPERATIONAL, "Water level", "Vodní stav", "CM", "PT10M", InterpolationType.CONTINUOUS, null), //
    /**
     * Operational discharge
     */
    Q(CHMIProduct.OPERATIONAL, "Discharge", "Průtok", "M3_S", "PT10M", InterpolationType.CONTINUOUS, null), //
    /**
     * Operational water temperature (10 minutes values of the current day, hourly values of the previous days), completed with the
     * validated instantaneous water temperatures of the historical archive
     */
    TH(CHMIProduct.OPERATIONAL, "Water temperature", "Teplota vody", "0C", null, InterpolationType.CONTINUOUS, null), //

    HD(CHMIProduct.DAILY, "Daily mean water level", "Průměrné denní vodní stavy", "CM", "P1D", InterpolationType.AVERAGE_SUCC,
	    "P1D"), //
    QD(CHMIProduct.DAILY, "Daily mean discharge", "Průměrné denní průtoky", "M3_S", "P1D", InterpolationType.AVERAGE_SUCC, "P1D"), //
    TD(CHMIProduct.DAILY, "Daily mean water temperature", "Průměrné denní teploty vody", "0C", "P1D", InterpolationType.AVERAGE_SUCC,
	    "P1D"), //
    PD(CHMIProduct.DAILY, "Daily mean suspended sediment concentration", "Průměrné denní koncentrace plavenin", "G_M3", "P1D",
	    InterpolationType.AVERAGE_SUCC, "P1D"), //

    HH(CHMIProduct.HOURLY, "Hourly mean water level", "Průměrné hodinové vodní stavy", "CM", "PT1H", InterpolationType.AVERAGE_SUCC,
	    "PT1H"), //
    QH(CHMIProduct.HOURLY, "Hourly mean discharge", "Průměrné hodinové průtoky", "M3_S", "PT1H", InterpolationType.AVERAGE_SUCC,
	    "PT1H"), //

    HC(CHMIProduct.MEASURED_WATER_LEVEL, "Water level (validated)", "Měřené okamžité vodní stavy", "CM", "PT10M",
	    InterpolationType.CONTINUOUS, null), //
    TO(CHMIProduct.MEASURED_TEMPERATURE, "Water temperature (validated)", "Měřené okamžité teploty vody", "0C", "PT10M",
	    InterpolationType.CONTINUOUS, null), //

    QM(CHMIProduct.MONTHLY, "Monthly mean discharge", "Průměrné měsíční průtoky", "M3_S", "P1M", InterpolationType.AVERAGE_SUCC,
	    "P1M"), //
    TM(CHMIProduct.MONTHLY, "Monthly mean water temperature", "Průměrné měsíční teploty vody", "0C", "P1M",
	    InterpolationType.AVERAGE_SUCC, "P1M"), //
    /**
     * The time of each value is the time of the monthly maximum
     */
    QK(CHMIProduct.MONTHLY_MAX, "Monthly maximum discharge", "Maximální měsíční průtoky", "M3_S", null, InterpolationType.STATISTICAL,
	    null), //

    QNEX(CHMIProduct.NATURAL, "Monthly naturalised discharge (including groundwater abstractions)",
	    "Neovlivněné měsíční průtoky včetně ovlivnění PZV", "M3_S", "P1M", InterpolationType.AVERAGE_SUCC, "P1M"), //
    QNEY(CHMIProduct.NATURAL, "Monthly naturalised discharge (excluding groundwater abstractions)",
	    "Neovlivněné měsíční průtoky bez ovlivnění PZV", "M3_S", "P1M", InterpolationType.AVERAGE_SUCC, "P1M"), //

    HR(CHMIProduct.MORNING_WATER_LEVEL, "Morning water level", "Ranní vodní stavy", "CM", "P1D", InterpolationType.CONTINUOUS, null), //
    TR(CHMIProduct.MORNING_TEMPERATURE, "Morning water temperature", "Měřené ranní teploty vody", "0C", "P1D",
	    InterpolationType.CONTINUOUS, null), //

    QC(CHMIProduct.FLOWMETER, "Measured discharge", "Měřené okamžité průtoky", "M3_S", null, InterpolationType.CONTINUOUS, null);

    private final CHMIProduct product;
    private final String label;
    private final String providerName;
    private final String unit;
    private final String resolution;
    private final InterpolationType interpolation;
    private final String aggregation;

    /**
     * @param product
     * @param label
     * @param providerName
     * @param unit
     * @param resolution
     * @param interpolation
     * @param aggregation
     */
    private CHMIParameter(CHMIProduct product, String label, String providerName, String unit, String resolution,
	    InterpolationType interpolation, String aggregation) {

	this.product = product;
	this.label = label;
	this.providerName = providerName;
	this.unit = unit;
	this.resolution = resolution;
	this.interpolation = interpolation;
	this.aggregation = aggregation;
    }

    /**
     * @return the dataset of the series
     */
    public CHMIProduct getProduct() {

	return product;
    }

    /**
     * @return the English name, e.g. "Daily mean discharge"
     */
    public String getLabel() {

	return label;
    }

    /**
     * @return the CHMI (Czech) name, e.g. "Průměrné denní průtoky"
     */
    public String getProviderName() {

	return providerName;
    }

    /**
     * @return the CHMI unit code, e.g. "M3_S"
     */
    public String getUnit() {

	return unit;
    }

    /**
     * @return the ISO 8601 time resolution, or null if not regular
     */
    public String getResolution() {

	return resolution;
    }

    /**
     * @return
     */
    public InterpolationType getInterpolation() {

	return interpolation;
    }

    /**
     * @return the ISO 8601 aggregation period, or null for instantaneous values
     */
    public String getAggregation() {

	return aggregation;
    }

    /**
     * @return the historical series of validated instantaneous values used to complete this operational series in the past, if any
     */
    public Optional<CHMIParameter> getHistoricalCounterpart() {

	switch (this) {
	case H:
	    return Optional.of(HC);
	case TH:
	    return Optional.of(TO);
	default:
	    return Optional.empty();
	}
    }

    /**
     * @param product
     * @return the series of the given dataset
     */
    public static List<CHMIParameter> getParameters(CHMIProduct product) {

	List<CHMIParameter> ret = new ArrayList<>();
	for (CHMIParameter parameter : values()) {
	    if (parameter.getProduct() == product) {
		ret.add(parameter);
	    }
	}

	return ret;
    }

    /**
     * @param code the time series type, e.g. "QD"
     * @return
     */
    public static Optional<CHMIParameter> decode(String code) {

	if (code == null) {
	    return Optional.empty();
	}

	for (CHMIParameter parameter : values()) {
	    if (parameter.name().equalsIgnoreCase(code.trim())) {
		return Optional.of(parameter);
	    }
	}

	return Optional.empty();
    }
}
