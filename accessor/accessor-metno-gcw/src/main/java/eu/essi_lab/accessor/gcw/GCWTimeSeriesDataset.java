package eu.essi_lab.accessor.gcw;

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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

/**
 * The content of an OPeNDAP dataset of the GCW catalogue relevant for harvesting: global attributes, station location,
 * time extent and the time series it contains (one for each variable, or for each variable and vertical level)
 *
 * @author boldrini
 */
public class GCWTimeSeriesDataset {

    /**
     * Keys of the station description
     */
    public static final String STATION_ID = "id";
    public static final String STATION_LATITUDE = "latitude";
    public static final String STATION_LONGITUDE = "longitude";
    public static final String STATION_ALTITUDE = "altitude";

    /**
     * Keys of the series descriptions
     */
    public static final String SERIES_VARIABLE = "variable";
    public static final String SERIES_LEVEL_INDEX = "levelIndex";
    public static final String SERIES_LEVEL_VALUE = "levelValue";
    public static final String SERIES_LEVEL_NAME = "levelName";
    public static final String SERIES_LEVEL_UNITS = "levelUnits";
    public static final String SERIES_LEVEL_POSITIVE = "levelPositive";

    private final Map<String, String> globalAttributes = new LinkedHashMap<>();
    private final JSONObject station = new JSONObject();
    private final List<JSONObject> series = new ArrayList<>();
    private Long begin;
    private Long end;
    private int timeSize;

    /**
     * @return the string global attributes
     */
    public Map<String, String> getGlobalAttributes() {

	return globalAttributes;
    }

    /**
     * @return the station identifier and location, when available
     */
    public JSONObject getStation() {

	return station;
    }

    /**
     * @return the series descriptions: variable name and attributes, plus the vertical level of profile series
     */
    public List<JSONObject> getSeries() {

	return series;
    }

    /**
     * @return the first time step in milliseconds, or null
     */
    public Long getBegin() {

	return begin;
    }

    /**
     * @param begin
     */
    public void setBegin(Long begin) {

	this.begin = begin;
    }

    /**
     * @return the last time step in milliseconds, or null
     */
    public Long getEnd() {

	return end;
    }

    /**
     * @param end
     */
    public void setEnd(Long end) {

	this.end = end;
    }

    /**
     * @return the number of time steps
     */
    public int getTimeSize() {

	return timeSize;
    }

    /**
     * @param timeSize
     */
    public void setTimeSize(int timeSize) {

	this.timeSize = timeSize;
    }
}
