package eu.essi_lab.profiler.os;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2025 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

public class OSBox {

    public enum CardinalPoint {

	/**
	 * 
	 */
	WEST,
	/**
	 * 
	 */
	SOUTH,
	/**
	 * 
	 */
	EAST,
	/**
	 * 
	 */
	NORTH
    }

    private String west;
    private String south;
    private String east;
    private String north;
    private Double doubleWest;
    private Double doubleSouth;
    private Double doubleEast;
    private Double doubleNorth;

    /**
     * Implementation note: the antimeridian-crossed values are supported (see tests)
     * 
     * @param box
     * @throws IllegalArgumentException
     */
    public OSBox(String box) throws IllegalArgumentException {

	String[] split = box.split(",");
	if (split == null || split.length < 4) {
	    throw new IllegalArgumentException(
		    "Invalid bbox format: "+box+". See http://www.opensearch.org/Specifications/OpenSearch/Extensions/Geo/1.0/Draft_1#The_.22box.22_parameter for more info'");
	}

	this.west = split[0];
	this.south = split[1];
	this.east = split[2];
	this.north = split[3];

	doubleWest = OSRequestParser.parseDouble(west);
	doubleSouth = OSRequestParser.parseDouble(south);
	doubleEast = OSRequestParser.parseDouble(east);
	doubleNorth = OSRequestParser.parseDouble(north);

	if (doubleWest == null || doubleSouth == null || doubleEast == null || doubleNorth == null) {
	    throw new IllegalArgumentException("Invalid bbox values. Double values required");
	}

	if (doubleWest > 180 || doubleWest < -180 || doubleSouth > 90 || doubleSouth < -90 || doubleEast > 180 || doubleEast < -180
		|| doubleNorth > 90 || doubleNorth < -90 || doubleSouth > doubleNorth) {
	    throw new IllegalArgumentException(
		    "Invalid bbox values: "+box+". See http://www.opensearch.org/Specifications/OpenSearch/Extensions/Geo/1.0/Draft_1#The_.22box.22_parameter for more info");
	}
    }

    public OSBox(String west, String south, String east, String north) throws IllegalArgumentException {

	this(west + "," + south + "," + east + "," + north);
    }

    public String getString(CardinalPoint point) {

	switch (point) {
	case WEST:
	    return west;
	case SOUTH:
	    return south;
	case EAST:
	    return east;
	case NORTH:
	default:
	    return north;
	}
    }

    public Double getDouble(CardinalPoint point) {

	switch (point) {
	case WEST:
	    return doubleWest;
	case SOUTH:
	    return doubleSouth;
	case EAST:
	    return doubleEast;
	case NORTH:
	default:
	    return doubleNorth;
	}
    }

    public String toString() {

	return west + "," + south + "," + east + "," + north;
    }

}
