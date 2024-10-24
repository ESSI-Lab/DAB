
package eu.essi_lab.accessor.inpe;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2024 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.nio.charset.StandardCharsets;

import org.apache.commons.codec.binary.Base64;

import eu.essi_lab.lib.utils.GSLoggerFactory;

public class INPEQueryParameters {
    String south = "";
    String north = "";
    String west = "";
    String east = "";
    String startDate = "";
    String endDate = "";
    String satelliteId = "";
    String sensor = "";

    public INPEQueryParameters(String south, String north, String west, String east, String startDate, String endDate, String satelliteId,
	    String sensor) {
	super();
	this.south = south;
	this.north = north;
	this.west = west;
	this.east = east;
	this.startDate = startDate;
	this.endDate = endDate;
	this.satelliteId = satelliteId;
	this.sensor = sensor;
    }

    public INPEQueryParameters(String encodedId) {
	String resolved = decodeBase64(encodedId);
	String[] split = resolved.split(",");
	south = split[0];
	north = split[1];
	west = split[2];
	east = split[3];
	startDate = split[4];
	endDate = split[5];
	satelliteId = split[6];
    }

    public String getSouth() {
	return south;
    }

    public void setSouth(String south) {
	this.south = south;
    }

    public String getNorth() {
	return north;
    }

    public void setNorth(String north) {
	this.north = north;
    }

    public String getWest() {
	return west;
    }

    public void setWest(String west) {
	this.west = west;
    }

    public String getEast() {
	return east;
    }

    public void setEast(String east) {
	this.east = east;
    }

    public String getStartDate() {
	return startDate;
    }

    public void setStartDate(String startDate) {
	this.startDate = startDate;
    }

    public String getEndDate() {
	return endDate;
    }

    public void setEndDate(String endDate) {
	this.endDate = endDate;
    }

    public String getSatelliteId() {
	return satelliteId;
    }

    public void setSatelliteId(String satelliteId) {
	this.satelliteId = satelliteId;
    }

    public String encode() {
	String strings[] = new String[] { south, north, west, east, startDate, endDate, satelliteId };
	String ret = "";
	for (String string : strings) {
	    ret += string + ",";
	}
	ret = ret.substring(0, ret.length() - 1);
	return encodeBase64(ret);
    }

    public static String encodeBase64(String string) {
	try {
	    byte[] res = Base64.encodeBase64(string.getBytes(StandardCharsets.UTF_8));
	    return new String(res, StandardCharsets.UTF_8);
	} catch (Exception e) {
	    GSLoggerFactory.getLogger(INPEQueryParameters.class).error(e.getMessage());
	    GSLoggerFactory.getLogger(INPEQueryParameters.class).error("Base 64 encoding failed for: " + string);
	    return null;
	}
    }

    public static String decodeBase64(String string) {
	try {
	    byte[] test2 = Base64.decodeBase64(string.getBytes(StandardCharsets.UTF_8));
	    return new String(test2, StandardCharsets.UTF_8);
	} catch (Exception e) {
	    GSLoggerFactory.getLogger(INPEQueryParameters.class).error(e.getMessage());
	    GSLoggerFactory.getLogger(INPEQueryParameters.class).error("Base 64 decoding failed for: " + string);
	    return null;
	}
    }

}
