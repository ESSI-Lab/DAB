package eu.essi_lab.accessor.canswe;

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

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

/**
 * A station of the CanSWE NetCDF file (an index of the station_id dimension) with its sparse series: for each variable
 * the indexes of the time dimension having a valid value, in increasing order, and the values
 *
 * @author boldrini
 */
public class CanSWEStation {

    /**
     * WMO codes of the type_mes variable
     */
    private static final String[] MEASUREMENT_TYPES = new String[] { //
	    "Multi point manual snow survey", //
	    "Single point manual snow water equivalent measurement", //
	    "Snow pillow or snow scale", //
	    "Passive gamma", //
	    "GNSS/GPS methods", //
	    "Cosmic ray attenuation", //
	    "Time domain reflectometry" };

    private String id;
    private String name;
    private String nameSecondary;
    private String nameTertiary;
    private String idSecondary;
    private String idTertiary;
    private String source;
    private long measurementType = -1;
    private double latitude = Double.NaN;
    private double longitude = Double.NaN;
    private double elevation = Double.NaN;

    private final Map<String, int[]> timeIndexes = new HashMap<>();
    private final Map<String, float[]> values = new HashMap<>();

    /**
     * @param variable
     * @return the indexes of the time dimension having a valid value of the given variable, in increasing order
     */
    public int[] getTimeIndexes(String variable) {

	return timeIndexes.getOrDefault(variable, new int[] {});
    }

    /**
     * @param variable
     * @return the valid values of the given variable, aligned with {@link #getTimeIndexes(String)}
     */
    public float[] getValues(String variable) {

	return values.getOrDefault(variable, new float[] {});
    }

    /**
     * @param variable
     * @param times
     * @param vals
     */
    void setSeries(String variable, int[] times, float[] vals) {

	timeIndexes.put(variable, times);
	values.put(variable, vals);
    }

    /**
     * @return the station metadata as JSON
     */
    public JSONObject toJSON() {

	JSONObject ret = new JSONObject();
	ret.put("station_id", id);
	put(ret, "station_name", name);
	put(ret, "station_name_sec", nameSecondary);
	put(ret, "station_name_ter", nameTertiary);
	put(ret, "station_id_sec", idSecondary);
	put(ret, "station_id_ter", idTertiary);
	put(ret, "source", source);
	if (measurementType >= 0) {
	    ret.put("type_mes", measurementType);
	    getMeasurementTypeLabel().ifPresent(l -> ret.put("type_mes_label", l));
	}
	if (!Double.isNaN(elevation)) {
	    ret.put("elevation", elevation);
	}
	return ret;
    }

    /**
     * @return the WMO label of the measurement method
     */
    public java.util.Optional<String> getMeasurementTypeLabel() {

	if (measurementType >= 0 && measurementType < MEASUREMENT_TYPES.length) {
	    return java.util.Optional.of(MEASUREMENT_TYPES[(int) measurementType]);
	}
	return java.util.Optional.empty();
    }

    private static void put(JSONObject object, String key, String value) {

	if (value != null && !value.trim().isEmpty() && !value.trim().equalsIgnoreCase("nan")) {
	    object.put(key, value.trim());
	}
    }

    public String getId() {
	return id;
    }

    void setId(String id) {
	this.id = id;
    }

    public String getName() {
	return name;
    }

    void setName(String name) {
	this.name = name;
    }

    void setNameSecondary(String nameSecondary) {
	this.nameSecondary = nameSecondary;
    }

    void setNameTertiary(String nameTertiary) {
	this.nameTertiary = nameTertiary;
    }

    void setIdSecondary(String idSecondary) {
	this.idSecondary = idSecondary;
    }

    void setIdTertiary(String idTertiary) {
	this.idTertiary = idTertiary;
    }

    public String getSource() {
	return source;
    }

    void setSource(String source) {
	this.source = source;
    }

    public long getMeasurementType() {
	return measurementType;
    }

    void setMeasurementType(long measurementType) {
	this.measurementType = measurementType;
    }

    public double getLatitude() {
	return latitude;
    }

    void setLatitude(double latitude) {
	this.latitude = latitude;
    }

    public double getLongitude() {
	return longitude;
    }

    void setLongitude(double longitude) {
	this.longitude = longitude;
    }

    public double getElevation() {
	return elevation;
    }

    void setElevation(double elevation) {
	this.elevation = elevation;
    }
}
