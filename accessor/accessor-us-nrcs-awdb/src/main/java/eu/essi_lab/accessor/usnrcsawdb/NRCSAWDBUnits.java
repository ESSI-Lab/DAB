package eu.essi_lab.accessor.usnrcsawdb;

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

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * AWDB stores values in English units; each element also declares its metric unit. This class converts the English
 * values to the metric units when the conversion is known, otherwise values are kept in the stored units.
 *
 * @author boldrini
 */
public class NRCSAWDBUnits {

    private NRCSAWDBUnits() {
    }

    /**
     * @param storedUnit
     * @param metricUnit
     * @return the units the values will be published in
     */
    public static String getTargetUnit(String storedUnit, String metricUnit) {

	if (isConvertible(storedUnit, metricUnit)) {
	    return metricUnit;
	}

	return storedUnit;
    }

    /**
     * @param value a value in the stored units
     * @param storedUnit
     * @param metricUnit
     * @return the value in the units returned by {@link #getTargetUnit(String, String)}
     */
    public static BigDecimal convert(BigDecimal value, String storedUnit, String metricUnit) {

	if (value == null || !isConvertible(storedUnit, metricUnit) || storedUnit.equals(metricUnit)) {
	    return value;
	}

	double v = value.doubleValue();
	double ret;

	switch (storedUnit + ">" + metricUnit) {
	case "in>mm":
	    ret = v * 25.4;
	    break;
	case "in>cm":
	    ret = v * 2.54;
	    break;
	case "ft>m":
	    ret = v * 0.3048;
	    break;
	case "degF>degC":
	    ret = (v - 32.0) * 5.0 / 9.0;
	    break;
	case "mph>km/hr":
	    ret = v * 1.609344;
	    break;
	case "mile>km":
	    ret = v * 1.609344;
	    break;
	case "cfs>m3/s":
	    ret = v * 0.028316846592;
	    break;
	case "ac_ft>dam^3":
	    ret = v * 1.2334818375475;
	    break;
	case "inch_Hg>mbar":
	    ret = v * 33.8638866667;
	    break;
	default:
	    return value;
	}

	return new BigDecimal(ret).setScale(3, RoundingMode.HALF_UP).stripTrailingZeros();
    }

    /**
     * @param storedUnit
     * @param metricUnit
     * @return
     */
    private static boolean isConvertible(String storedUnit, String metricUnit) {

	if (storedUnit == null || metricUnit == null) {
	    return false;
	}

	if (storedUnit.equals(metricUnit)) {
	    return true;
	}

	switch (storedUnit + ">" + metricUnit) {
	case "in>mm":
	case "in>cm":
	case "ft>m":
	case "degF>degC":
	case "mph>km/hr":
	case "mile>km":
	case "cfs>m3/s":
	case "ac_ft>dam^3":
	case "inch_Hg>mbar":
	    return true;
	default:
	    return false;
	}
    }
}
