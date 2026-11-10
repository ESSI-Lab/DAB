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

/**
 * The SLF measurement networks exposed by the SLF Measurement API, each with its own station list and measurement
 * endpoints
 *
 * @author boldrini
 */
public enum SLFNetwork {

    /**
     * IMIS automatic stations, 30 minutes measurements
     */
    IMIS("imis", "IMIS automatic station", "imis/stations", "imis/measurements", "imis/station/{code}/measurements", false),

    /**
     * IMIS automatic stations, 10 minutes precipitation measurements
     */
    IMIS_PRECIPITATION("imis-precipitation", "IMIS automatic station", "imis/stations", "imis/measurements-precipitation",
	    "imis/station/{code}/measurements-precipitation", false),

    /**
     * IMIS automatic stations, daily snow height and new snow (only available as a bulk request for all the stations)
     */
    IMIS_DAILY_SNOW("imis-daily-snow", "IMIS automatic station", "imis/stations", "imis/daily-snow", null, false),

    /**
     * Manual observations at study plots, daily (only during the winter season)
     */
    STUDY_PLOT("study-plot", "SLF study plot", "study-plot/stations", "study-plot/measurements", "study-plot/station/{code}/measurements",
	    true);

    private final String id;
    private final String label;
    private final String stationsPath;
    private final String bulkMeasurementsPath;
    private final String stationMeasurementsPath;
    private final boolean seasonal;

    /**
     * @param id
     * @param label
     * @param stationsPath
     * @param bulkMeasurementsPath
     * @param stationMeasurementsPath
     * @param seasonal
     */
    private SLFNetwork(String id, String label, String stationsPath, String bulkMeasurementsPath, String stationMeasurementsPath,
	    boolean seasonal) {

	this.id = id;
	this.label = label;
	this.stationsPath = stationsPath;
	this.bulkMeasurementsPath = bulkMeasurementsPath;
	this.stationMeasurementsPath = stationMeasurementsPath;
	this.seasonal = seasonal;
    }

    /**
     * @return the identifier used in the online resource names
     */
    public String getId() {

	return id;
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
    public String getStationsPath() {

	return stationsPath;
    }

    /**
     * @return the path returning the last 24 hours of measurements of all the stations
     */
    public String getBulkMeasurementsPath() {

	return bulkMeasurementsPath;
    }

    /**
     * @param stationCode
     * @return the path returning the measurements of the given station, or null if the network has no per station
     *         endpoint
     */
    public String getStationMeasurementsPath(String stationCode) {

	if (stationMeasurementsPath == null) {
	    return null;
	}

	return stationMeasurementsPath.replace("{code}", stationCode);
    }

    /**
     * @return true if the network only measures during the winter season, so that the availability of a parameter
     *         can't be inferred from the last measurements
     */
    public boolean isSeasonal() {

	return seasonal;
    }

    /**
     * @param id
     * @return
     */
    public static SLFNetwork fromId(String id) {

	for (SLFNetwork network : values()) {
	    if (network.getId().equals(id)) {
		return network;
	    }
	}

	return null;
    }
}
