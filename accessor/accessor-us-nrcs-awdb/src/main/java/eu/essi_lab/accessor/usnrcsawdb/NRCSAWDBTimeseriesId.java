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

import java.util.Optional;

import org.json.JSONObject;

/**
 * Identifies an AWDB timeseries (a station element), e.g. "301:CA:SNTL/WTEQ:*:1/DAILY", as station triplet, element
 * (in the AWDB format elementCode:heightDepth:ordinal) and duration. It is used as online resource name.
 *
 * @author boldrini
 */
public class NRCSAWDBTimeseriesId {

    private static final String ANY_HEIGHT_DEPTH = "*";

    private final String triplet;
    private final String elementCode;
    private final String heightDepth;
    private final int ordinal;
    private final String duration;

    /**
     * @param triplet
     * @param elementCode
     * @param heightDepth null if the element has no height or depth
     * @param ordinal
     * @param duration
     */
    public NRCSAWDBTimeseriesId(String triplet, String elementCode, String heightDepth, int ordinal, String duration) {

	this.triplet = triplet;
	this.elementCode = elementCode;
	this.heightDepth = heightDepth;
	this.ordinal = ordinal;
	this.duration = duration;
    }

    /**
     * @param triplet
     * @param stationElement a station element as returned by the AWDB stations endpoint
     * @return
     */
    public static NRCSAWDBTimeseriesId of(String triplet, JSONObject stationElement) {

	return new NRCSAWDBTimeseriesId(//
		triplet, //
		stationElement.getString(NRCSAWDBClient.ELEMENT_CODE), //
		getHeightDepth(stationElement), //
		stationElement.optInt(NRCSAWDBClient.ELEMENT_ORDINAL, 1), //
		stationElement.getString(NRCSAWDBClient.ELEMENT_DURATION));
    }

    /**
     * @param id
     * @return
     */
    public static Optional<NRCSAWDBTimeseriesId> parse(String id) {

	if (id == null) {
	    return Optional.empty();
	}

	String[] split = id.split("/");
	if (split.length != 3) {
	    return Optional.empty();
	}

	String[] element = split[1].split(":");
	if (element.length != 3) {
	    return Optional.empty();
	}

	try {
	    String heightDepth = element[1].equals(ANY_HEIGHT_DEPTH) ? null : element[1];
	    return Optional.of(new NRCSAWDBTimeseriesId(split[0], element[0], heightDepth, Integer.parseInt(element[2]), split[2]));
	} catch (NumberFormatException e) {
	    return Optional.empty();
	}
    }

    /**
     * @param stationElement
     * @return true if the given station element is the one identified by this id
     */
    public boolean matches(JSONObject stationElement) {

	return elementCode.equals(stationElement.optString(NRCSAWDBClient.ELEMENT_CODE)) && //
		ordinal == stationElement.optInt(NRCSAWDBClient.ELEMENT_ORDINAL, 1) && //
		duration.equals(stationElement.optString(NRCSAWDBClient.ELEMENT_DURATION)) && //
		String.valueOf(heightDepth).equals(String.valueOf(getHeightDepth(stationElement)));
    }

    /**
     * @param stationElement
     * @return
     */
    private static String getHeightDepth(JSONObject stationElement) {

	if (!stationElement.has(NRCSAWDBClient.ELEMENT_HEIGHT_DEPTH) || stationElement.isNull(NRCSAWDBClient.ELEMENT_HEIGHT_DEPTH)) {
	    return null;
	}

	return stationElement.get(NRCSAWDBClient.ELEMENT_HEIGHT_DEPTH).toString();
    }

    /**
     * @return
     */
    public String getTriplet() {

	return triplet;
    }

    /**
     * @return
     */
    public String getElementCode() {

	return elementCode;
    }

    /**
     * @return the height (positive) or depth (negative) in inches, or null
     */
    public String getHeightDepth() {

	return heightDepth;
    }

    /**
     * @return
     */
    public int getOrdinal() {

	return ordinal;
    }

    /**
     * @return
     */
    public String getDuration() {

	return duration;
    }

    /**
     * @return the element in the format used by the AWDB API: elementCode:heightDepth:ordinal
     */
    public String getElementString() {

	return elementCode + ":" + (heightDepth == null ? ANY_HEIGHT_DEPTH : heightDepth) + ":" + ordinal;
    }

    @Override
    public String toString() {

	return triplet + "/" + getElementString() + "/" + duration;
    }
}
