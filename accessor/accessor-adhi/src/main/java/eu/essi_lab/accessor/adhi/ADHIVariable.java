package eu.essi_lab.accessor.adhi;

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

import java.util.Optional;

import eu.essi_lab.model.resource.InterpolationType;

/**
 * The discharge time series published by ADHI, one file per station in a zip file of the dataset. Monthly files have rows
 * "year,month,mean,maximum,minimum,missing days", annual files have rows "year,value" (annual mean), "year,month,day,value"
 * (annual maximum and annual minimum 7-day discharge, with the date of occurrence) or "year,q5,q10,q25,q50,q75,q90,q95,q99"
 * (annual percentiles). Missing values are "NaN"
 *
 * @author boldrini
 */
public enum ADHIVariable {

    MONTHLY_MEAN("Monthly mean discharge", "mean of the daily discharges of the month", ADHIVariable.MONTHLY_FILE, 2, true,
	    InterpolationType.AVERAGE), //
    MONTHLY_MAX("Monthly maximum discharge", "maximum daily discharge of the month", ADHIVariable.MONTHLY_FILE, 3, true,
	    InterpolationType.MAX_DAILY_AVERAGES), //
    MONTHLY_MIN("Monthly minimum discharge", "minimum daily discharge of the month", ADHIVariable.MONTHLY_FILE, 4, true,
	    InterpolationType.MIN_DAILY_AVERAGES), //
    ANNUAL_MEAN("Annual mean discharge", "mean of the daily discharges of the year", ADHIVariable.ANNUAL_MEAN_FILE, 1, false,
	    InterpolationType.AVERAGE), //
    ANNUAL_MAX("Annual maximum discharge", "maximum daily discharge of the year", ADHIVariable.ANNUAL_MAX_FILE, 3, false,
	    InterpolationType.MAX_DAILY_AVERAGES), //
    ANNUAL_7DAY_MIN("Annual minimum 7-day discharge", "minimum 7-day discharge (7-day average of the daily discharges) of the year",
	    ADHIVariable.ANNUAL_7DAY_MIN_FILE, 3, false, InterpolationType.MIN), //
    ANNUAL_Q05("Annual 5th percentile of daily discharge", "5th percentile of the daily discharges of the year",
	    ADHIVariable.ANNUAL_PERCENTILES_FILE, 1, false, InterpolationType.STATISTICAL), //
    ANNUAL_Q10("Annual 10th percentile of daily discharge", "10th percentile of the daily discharges of the year",
	    ADHIVariable.ANNUAL_PERCENTILES_FILE, 2, false, InterpolationType.STATISTICAL), //
    ANNUAL_Q25("Annual 25th percentile of daily discharge", "25th percentile of the daily discharges of the year",
	    ADHIVariable.ANNUAL_PERCENTILES_FILE, 3, false, InterpolationType.STATISTICAL), //
    ANNUAL_Q50("Annual median daily discharge", "50th percentile (median) of the daily discharges of the year",
	    ADHIVariable.ANNUAL_PERCENTILES_FILE, 4, false, InterpolationType.STATISTICAL), //
    ANNUAL_Q75("Annual 75th percentile of daily discharge", "75th percentile of the daily discharges of the year",
	    ADHIVariable.ANNUAL_PERCENTILES_FILE, 5, false, InterpolationType.STATISTICAL), //
    ANNUAL_Q90("Annual 90th percentile of daily discharge", "90th percentile of the daily discharges of the year",
	    ADHIVariable.ANNUAL_PERCENTILES_FILE, 6, false, InterpolationType.STATISTICAL), //
    ANNUAL_Q95("Annual 95th percentile of daily discharge", "95th percentile of the daily discharges of the year",
	    ADHIVariable.ANNUAL_PERCENTILES_FILE, 7, false, InterpolationType.STATISTICAL), //
    ANNUAL_Q99("Annual 99th percentile of daily discharge", "99th percentile of the daily discharges of the year",
	    ADHIVariable.ANNUAL_PERCENTILES_FILE, 8, false, InterpolationType.STATISTICAL);

    static final String MONTHLY_FILE = "ADHI_MonthlySeries.zip";
    static final String ANNUAL_MEAN_FILE = "ADHI_AnnualMean.zip";
    static final String ANNUAL_MAX_FILE = "ADHI_AnnualMax.zip";
    static final String ANNUAL_7DAY_MIN_FILE = "ADHI_Annual7DayMin.zip";
    static final String ANNUAL_PERCENTILES_FILE = "ADHI_AnnualPercentiles.zip";

    private final String label;
    private final String description;
    private final String fileName;
    private final int column;
    private final boolean monthly;
    private final InterpolationType interpolation;

    /**
     * @param label
     * @param description
     * @param fileName
     * @param column
     * @param monthly
     * @param interpolation
     */
    ADHIVariable(String label, String description, String fileName, int column, boolean monthly, InterpolationType interpolation) {

	this.label = label;
	this.description = description;
	this.fileName = fileName;
	this.column = column;
	this.monthly = monthly;
	this.interpolation = interpolation;
    }

    /**
     * @return e.g. "Monthly mean discharge"
     */
    public String getLabel() {

	return label;
    }

    /**
     * @return e.g. "mean of the daily discharges of the month"
     */
    public String getDescription() {

	return description;
    }

    /**
     * @return the name of the zip file of the dataset, e.g. "ADHI_MonthlySeries.zip"
     */
    public String getFileName() {

	return fileName;
    }

    /**
     * @return the index of the column of the value in the rows of the station files
     */
    public int getColumn() {

	return column;
    }

    /**
     * @return true for monthly series, false for annual series
     */
    public boolean isMonthly() {

	return monthly;
    }

    /**
     * @return the ISO 8601 duration of the time step, "P1M" or "P1Y"
     */
    public String getResolution() {

	return monthly ? "P1M" : "P1Y";
    }

    /**
     * @return
     */
    public InterpolationType getInterpolation() {

	return interpolation;
    }

    /**
     * @param code
     * @return
     */
    public static Optional<ADHIVariable> decode(String code) {

	if (code == null) {
	    return Optional.empty();
	}

	for (ADHIVariable variable : values()) {
	    if (variable.name().equalsIgnoreCase(code.trim())) {
		return Optional.of(variable);
	    }
	}

	return Optional.empty();
    }
}
