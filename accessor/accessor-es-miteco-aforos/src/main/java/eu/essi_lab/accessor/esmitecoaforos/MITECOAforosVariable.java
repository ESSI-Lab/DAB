package eu.essi_lab.accessor.esmitecoaforos;

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
 * Variables published by the Anuario de Aforos, each one being a column of a data table of each basin folder of the archive
 * (see the "Descripción tablas Anuario de Aforos" document)
 *
 * @author boldrini
 */
public enum MITECOAforosVariable {

    // daily tables

    RIVER_LEVEL_DAILY(MITECOAforosStationType.RIVER, "afliq.csv", "altura", true, "m", "Water level", "Altura media diaria",
	    InterpolationType.AVERAGE_SUCC), //
    RIVER_DISCHARGE_DAILY(MITECOAforosStationType.RIVER, "afliq.csv", "caudal", true, "m3/s", "Discharge", "Caudal medio diario",
	    InterpolationType.AVERAGE_SUCC), //
    CANAL_LEVEL_DAILY(MITECOAforosStationType.CANAL, "afliqc.csv", "altura", true, "m", "Water level", "Altura media diaria",
	    InterpolationType.AVERAGE_SUCC), //
    CANAL_DISCHARGE_DAILY(MITECOAforosStationType.CANAL, "afliqc.csv", "caudal", true, "m3/s", "Discharge", "Caudal medio diario",
	    InterpolationType.AVERAGE_SUCC), //
    RESERVOIR_STORAGE_DAILY(MITECOAforosStationType.RESERVOIR, "afliqe.csv", "reserva", true, "hm3", "Reservoir storage",
	    "Reserva diaria", InterpolationType.CONTINUOUS), //
    RESERVOIR_OUTFLOW_DAILY(MITECOAforosStationType.RESERVOIR, "afliqe.csv", "salida", true, "m3/s", "Reservoir outflow",
	    "Salida media diaria", InterpolationType.AVERAGE_SUCC), //
    RESERVOIR_INFLOW_DAILY(MITECOAforosStationType.RESERVOIR, "afliqe.csv", "entrada", true, "m3/s", "Reservoir inflow",
	    "Entrada media diaria", InterpolationType.AVERAGE_SUCC), //

    // monthly tables

    RIVER_LEVEL_MONTHLY(MITECOAforosStationType.RIVER, "mensual_a.csv", "hmedmes", false, "m", "Water level", "Altura media mensual",
	    InterpolationType.AVERAGE_SUCC), //
    RIVER_DISCHARGE_MONTHLY(MITECOAforosStationType.RIVER, "mensual_a.csv", "qmedmes", false, "m3/s", "Discharge",
	    "Caudal medio mensual", InterpolationType.AVERAGE_SUCC), //
    CANAL_LEVEL_MONTHLY(MITECOAforosStationType.CANAL, "mensual_c.csv", "hmedmes", false, "m", "Water level", "Altura media mensual",
	    InterpolationType.AVERAGE_SUCC), //
    CANAL_DISCHARGE_MONTHLY(MITECOAforosStationType.CANAL, "mensual_c.csv", "qmedmes", false, "m3/s", "Discharge",
	    "Caudal medio mensual", InterpolationType.AVERAGE_SUCC), //
    RESERVOIR_STORAGE_MONTHLY(MITECOAforosStationType.RESERVOIR, "mensual_e.csv", "resmes", false, "hm3", "Reservoir storage",
	    "Reserva mensual", InterpolationType.CONTINUOUS), //
    RESERVOIR_OUTFLOW_MONTHLY(MITECOAforosStationType.RESERVOIR, "mensual_e.csv", "salmes", false, "hm3", "Reservoir outflow volume",
	    "Salida mensual", InterpolationType.TOTAL_SUCC), //
    RESERVOIR_INFLOW_MONTHLY(MITECOAforosStationType.RESERVOIR, "mensual_e.csv", "entmes", false, "hm3", "Reservoir inflow volume",
	    "Entrada mensual", InterpolationType.TOTAL_SUCC), //
    PICHE_EVAPORATION_MONTHLY(MITECOAforosStationType.EVAPORATION, "evap.csv", "evapiche", false, "mm/d", "Evaporation (Piche)",
	    "Evaporación Piche media mensual", InterpolationType.AVERAGE_SUCC), //
    PAN_EVAPORATION_MONTHLY(MITECOAforosStationType.EVAPORATION, "evap.csv", "evatanque", false, "mm/d", "Evaporation (pan)",
	    "Evaporación en tanque media mensual", InterpolationType.AVERAGE_SUCC), //
    MAX_AIR_TEMPERATURE_MONTHLY(MITECOAforosStationType.EVAPORATION, "evap.csv", "temedmax", false, "°C",
	    "Air temperature (mean of daily maxima)", "Temperatura del aire media mensual de las máximas diarias",
	    InterpolationType.AVERAGE_SUCC), //
    MIN_AIR_TEMPERATURE_MONTHLY(MITECOAforosStationType.EVAPORATION, "evap.csv", "temedmin", false, "°C",
	    "Air temperature (mean of daily minima)", "Temperatura del aire media mensual de las mínimas diarias",
	    InterpolationType.AVERAGE_SUCC), //
    PRECIPITATION_MONTHLY(MITECOAforosStationType.EVAPORATION, "evap.csv", "prectotmes", false, "mm", "Precipitation",
	    "Precipitación total mensual", InterpolationType.TOTAL_SUCC), //
    WIND_SPEED_MONTHLY(MITECOAforosStationType.EVAPORATION, "evap.csv", "velviento", false, "km/h", "Wind speed",
	    "Velocidad del viento media mensual", InterpolationType.AVERAGE_SUCC), //
    SUNSHINE_DURATION_MONTHLY(MITECOAforosStationType.EVAPORATION, "evap.csv", "insol", false, "h", "Sunshine duration",
	    "Horas mensuales de insolación", InterpolationType.TOTAL_SUCC);

    private final MITECOAforosStationType stationType;
    private final String fileName;
    private final String column;
    private final boolean daily;
    private final String unit;
    private final String label;
    private final String spanishLabel;
    private final InterpolationType interpolation;

    /**
     * @param stationType
     * @param fileName
     * @param column
     * @param daily
     * @param unit
     * @param label
     * @param spanishLabel
     * @param interpolation
     */
    private MITECOAforosVariable(MITECOAforosStationType stationType, String fileName, String column, boolean daily, String unit,
	    String label, String spanishLabel, InterpolationType interpolation) {

	this.stationType = stationType;
	this.fileName = fileName;
	this.column = column;
	this.daily = daily;
	this.unit = unit;
	this.label = label;
	this.spanishLabel = spanishLabel;
	this.interpolation = interpolation;
    }

    /**
     * @return
     */
    public MITECOAforosStationType getStationType() {

	return stationType;
    }

    /**
     * @return the name of the data table in each basin folder, e.g. "afliq.csv"
     */
    public String getFileName() {

	return fileName;
    }

    /**
     * @return the column of the data table, e.g. "caudal"
     */
    public String getColumn() {

	return column;
    }

    /**
     * @return true for daily values (date column "fecha", dd/MM/yyyy), false for monthly values (date column "anomes", yyyyMM)
     */
    public boolean isDaily() {

	return daily;
    }

    /**
     * @return the ISO 8601 duration of the time step, "P1D" or "P1M"
     */
    public String getResolution() {

	return daily ? "P1D" : "P1M";
    }

    /**
     * @return the unit abbreviation, e.g. "m3/s"
     */
    public String getUnit() {

	return unit;
    }

    /**
     * @return the English name of the observed property, e.g. "Discharge"
     */
    public String getLabel() {

	return label;
    }

    /**
     * @return the Spanish description of the provider, e.g. "Caudal medio diario"
     */
    public String getSpanishLabel() {

	return spanishLabel;
    }

    /**
     * @return
     */
    public InterpolationType getInterpolation() {

	return interpolation;
    }

    /**
     * @param code the variable name, e.g. "RIVER_DISCHARGE_DAILY"
     * @return
     */
    public static Optional<MITECOAforosVariable> decode(String code) {

	if (code == null) {
	    return Optional.empty();
	}

	for (MITECOAforosVariable variable : values()) {
	    if (variable.name().equalsIgnoreCase(code.trim())) {
		return Optional.of(variable);
	    }
	}

	return Optional.empty();
    }
}
