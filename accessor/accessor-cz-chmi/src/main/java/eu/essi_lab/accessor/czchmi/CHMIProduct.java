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

/**
 * The CHMI surface water datasets. The operational dataset is made of the files of the current day (now/data/{objID}.json) and of
 * the previous days of the current year (recent/data/{yyyyMMdd}_{objID}.json), while each historical dataset is made of yearly files
 * of validated data (historical/data/{directory}/H_{objID}_{code}_{yyyy}.json)
 *
 * @author boldrini
 */
public enum CHMIProduct {

    /**
     * Operational, not validated 10 minutes data (now and recent directories)
     */
    OPERATIONAL(null, null, false), //
    /**
     * Daily means (water level, discharge, water temperature, suspended sediment concentration)
     */
    DAILY("daily", "DQ", true), //
    /**
     * Hourly means (water level, discharge)
     */
    HOURLY("hourly", "HQ", false), //
    /**
     * Validated instantaneous water levels
     */
    MEASURED_WATER_LEVEL("measured_water_level", "OH", false), //
    /**
     * Validated instantaneous water temperatures
     */
    MEASURED_TEMPERATURE("measured_temperature", "OT", false), //
    /**
     * Monthly means (discharge, water temperature)
     */
    MONTHLY("monthly", "MQ", true), //
    /**
     * Monthly maximum discharges
     */
    MONTHLY_MAX("monthly_max", "MMQ", false), //
    /**
     * Naturalised monthly discharges
     */
    NATURAL("natural", "NATQ", true), //
    /**
     * Morning (manual) water levels
     */
    MORNING_WATER_LEVEL("morning_water_level", "OH7", false), //
    /**
     * Morning (manual) water temperatures
     */
    MORNING_TEMPERATURE("morning_temperature", "OT7", false), //
    /**
     * Discharges measured with a flowmeter
     */
    FLOWMETER("flowmeter", "OF", false);

    private final String directory;
    private final String code;
    private final boolean probed;

    /**
     * @param directory
     * @param code
     * @param probed
     */
    private CHMIProduct(String directory, String code, boolean probed) {

	this.directory = directory;
	this.code = code;
	this.probed = probed;
    }

    /**
     * @return the directory of the yearly files in historical/data/, or null for the operational dataset
     */
    public String getDirectory() {

	return directory;
    }

    /**
     * @return the code of the yearly files, e.g. "DQ" for H_0-203-1-001000_DQ_2025.json
     */
    public String getCode() {

	return code;
    }

    /**
     * @return true if the yearly files contain a variable set of series, so that the latest file of each station is read at
     *         harvesting time to know which series are available
     */
    public boolean isProbed() {

	return probed;
    }

    /**
     * @return
     */
    public boolean isHistorical() {

	return directory != null;
    }
}
