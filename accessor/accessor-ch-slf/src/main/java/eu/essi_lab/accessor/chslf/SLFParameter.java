package eu.essi_lab.accessor.chslf;

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

import eu.essi_lab.model.resource.InterpolationType;

/**
 * The parameters published by the SLF Measurement API, as documented in https://measurement-api.slf.ch/openapi.json
 *
 * @author boldrini
 */
public enum SLFParameter {

    IMIS_HS(SLFNetwork.IMIS, "HS", "Snow depth", "Height of snowpack, total thickness of snowpack, instantaneous", "cm",
	    InterpolationType.CONTINUOUS, null, "PT30M"), //
    IMIS_TA(SLFNetwork.IMIS, "TA_30MIN_MEAN", "Air temperature", "Air temperature, mean over last 30 minutes", "°C",
	    InterpolationType.AVERAGE_PREC, "PT30M", "PT30M"), //
    IMIS_RH(SLFNetwork.IMIS, "RH_30MIN_MEAN", "Relative humidity", "Relative humidity, mean over last 30 minutes", "%",
	    InterpolationType.AVERAGE_PREC, "PT30M", "PT30M"), //
    IMIS_TSS(SLFNetwork.IMIS, "TSS_30MIN_MEAN", "Snow surface temperature", "Snow surface temperature, mean over last 30 minutes", "°C",
	    InterpolationType.AVERAGE_PREC, "PT30M", "PT30M"), //
    IMIS_TS0(SLFNetwork.IMIS, "TS0_30MIN_MEAN", "Snow temperature at the ground",
	    "Snow temperature at the ground, mean over last 30 minutes", "°C", InterpolationType.AVERAGE_PREC, "PT30M", "PT30M"), //
    IMIS_TS25(SLFNetwork.IMIS, "TS25_30MIN_MEAN", "Snow temperature at 25 cm",
	    "Snow temperature at 25 cm above ground, mean over last 30 minutes", "°C", InterpolationType.AVERAGE_PREC, "PT30M", "PT30M"), //
    IMIS_TS50(SLFNetwork.IMIS, "TS50_30MIN_MEAN", "Snow temperature at 50 cm",
	    "Snow temperature at 50 cm above ground, mean over last 30 minutes", "°C", InterpolationType.AVERAGE_PREC, "PT30M", "PT30M"), //
    IMIS_TS100(SLFNetwork.IMIS, "TS100_30MIN_MEAN", "Snow temperature at 100 cm",
	    "Snow temperature at 100 cm above ground, mean over last 30 minutes", "°C", InterpolationType.AVERAGE_PREC, "PT30M",
	    "PT30M"), //
    IMIS_RSWR(SLFNetwork.IMIS, "RSWR_30MIN_MEAN", "Reflected shortwave radiation",
	    "Reflected short wave radiation, mean over last 30 minutes", "W/m2", InterpolationType.AVERAGE_PREC, "PT30M", "PT30M"), //
    IMIS_VW(SLFNetwork.IMIS, "VW_30MIN_MEAN", "Wind speed", "Wind speed, vectorial mean over last 30 minutes", "m/s",
	    InterpolationType.AVERAGE_PREC, "PT30M", "PT30M"), //
    IMIS_VW_MAX(SLFNetwork.IMIS, "VW_30MIN_MAX", "Wind speed maximum", "Wind speed, max 5 seconds measurement within last 30 minutes",
	    "m/s", InterpolationType.MAX_PREC, "PT30M", "PT30M"), //
    IMIS_DW(SLFNetwork.IMIS, "DW_30MIN_MEAN", "Wind direction", "Wind direction, vectorial mean over last 30 minutes", "°",
	    InterpolationType.AVERAGE_PREC, "PT30M", "PT30M"), //
    IMIS_DW_SD(SLFNetwork.IMIS, "DW_30MIN_SD", "Wind direction standard deviation",
	    "Wind direction, standard deviation over last 30 minutes", "°", InterpolationType.STATISTICAL, "PT30M", "PT30M"), //

    IMIS_RR(SLFNetwork.IMIS_PRECIPITATION, "RR_10MIN_SUM", "Precipitation", "Precipitation, sum over last 10 minutes", "mm",
	    InterpolationType.TOTAL_PREC, "PT10M", "PT10M"), //

    IMIS_DAILY_HS(SLFNetwork.IMIS_DAILY_SNOW, "HS", "Snow depth (daily)", "Height of snowpack, total thickness of snowpack, daily value",
	    "cm", InterpolationType.CONTINUOUS, null, "P1D"), //
    IMIS_DAILY_HN(SLFNetwork.IMIS_DAILY_SNOW, "HN_1D", "New snow height (24 hours)", "Height of new snow during the last 24 hours",
	    "cm", InterpolationType.TOTAL_PREC, "P1D", "P1D"), //

    STUDY_PLOT_HS(SLFNetwork.STUDY_PLOT, "HS", "Snow depth (manual)", "Height of snowpack, total thickness of snowpack, manual observation",
	    "cm", InterpolationType.CONTINUOUS, null, "P1D"), //
    STUDY_PLOT_HN(SLFNetwork.STUDY_PLOT, "HN_1D", "New snow height (24 hours, manual)",
	    "Height of new snow during the last 24 hours, manual observation", "cm", InterpolationType.TOTAL_PREC, "P1D", "P1D"), //
    STUDY_PLOT_HNW(SLFNetwork.STUDY_PLOT, "HNW_1D", "New snow water equivalent (24 hours, manual)",
	    "Water equivalent of new snow during the last 24 hours, manual observation", "mm", InterpolationType.TOTAL_PREC, "P1D", "P1D");

    private final SLFNetwork network;
    private final String code;
    private final String label;
    private final String description;
    private final String units;
    private final InterpolationType interpolation;
    private final String aggregationDuration;
    private final String resolutionDuration;

    /**
     * @param network
     * @param code
     * @param label
     * @param description
     * @param units
     * @param interpolation
     * @param aggregationDuration
     * @param resolutionDuration
     */
    private SLFParameter(SLFNetwork network, String code, String label, String description, String units, InterpolationType interpolation,
	    String aggregationDuration, String resolutionDuration) {

	this.network = network;
	this.code = code;
	this.label = label;
	this.description = description;
	this.units = units;
	this.interpolation = interpolation;
	this.aggregationDuration = aggregationDuration;
	this.resolutionDuration = resolutionDuration;
    }

    /**
     * @return
     */
    public SLFNetwork getNetwork() {

	return network;
    }

    /**
     * @return the property name used in the API JSON responses
     */
    public String getCode() {

	return code;
    }

    /**
     * @return
     */
    public String getLabel() {

	return label;
    }

    /**
     * @return
     */
    public String getDescription() {

	return description;
    }

    /**
     * @return
     */
    public String getUnits() {

	return units;
    }

    /**
     * @return
     */
    public InterpolationType getInterpolation() {

	return interpolation;
    }

    /**
     * @return the ISO 8601 aggregation duration, or null for instantaneous values
     */
    public String getAggregationDuration() {

	return aggregationDuration;
    }

    /**
     * @return the ISO 8601 time resolution
     */
    public String getResolutionDuration() {

	return resolutionDuration;
    }

    /**
     * @param network
     * @return
     */
    public static List<SLFParameter> getParameters(SLFNetwork network) {

	List<SLFParameter> ret = new ArrayList<>();
	for (SLFParameter parameter : values()) {
	    if (parameter.getNetwork() == network) {
		ret.add(parameter);
	    }
	}

	return ret;
    }

    /**
     * @param network
     * @param code
     * @return
     */
    public static SLFParameter decode(SLFNetwork network, String code) {

	for (SLFParameter parameter : values()) {
	    if (parameter.getNetwork() == network && parameter.getCode().equals(code)) {
		return parameter;
	    }
	}

	return null;
    }
}
