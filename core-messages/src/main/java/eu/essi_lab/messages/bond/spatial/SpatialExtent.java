package eu.essi_lab.messages.bond.spatial;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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
 * @author Fabrizio
 */
public class SpatialExtent implements SpatialEntity {

    private double west;
    private double south;
    private double east;
    private double north;
    private String name;

    /**
     * 
     */
    public SpatialExtent() {
    }

    /**
     * @param south
     * @param west
     * @param north
     * @param east
     */
    public SpatialExtent(double south, double west, double north, double east) {
	this.west = west;
	this.south = south;
	this.east = east;
	this.north = north;
    }

    /**
     * @return
     */
    public String getName() {

	return name;
    }

    /**
     * @param name
     */
    public void setName(String name) {

	this.name = name;
    }

    /**
     * @return
     */
    public double getWest() {

	return west;
    }

    /**
     * @param west
     */
    public void setWest(double west) {

	this.west = west;
    }

    /**
     * @return
     */
    public double getSouth() {

	return south;
    }

    /**
     * @param south
     */
    public void setSouth(double south) {

	this.south = south;
    }

    /**
     * @return
     */
    public double getEast() {

	return east;
    }

    /**
     * @param east
     */
    public void setEast(double east) {

	this.east = east;
    }

    /**
     * @return
     */
    public double getNorth() {

	return north;
    }

    /**
     * @param north
     */
    public void setNorth(double north) {

	this.north = north;
    }

    @Override
    public boolean equals(Object obj) {

	if (obj instanceof SpatialExtent spatialExtent) {

	    double tol = 0.000000001;

	    return Math.abs(getEast() - spatialExtent.getEast()) < tol && //
		    Math.abs(getNorth() - spatialExtent.getNorth()) < tol && //
		    Math.abs(getSouth() - spatialExtent.getSouth()) < tol && //
		    Math.abs(getWest() - spatialExtent.getWest()) < tol;
	}

	return false;
    }

    @Override
    public SpatialExtent clone() {

	return new SpatialExtent(getSouth(), getWest(), getNorth(), getEast());
    }

    @Override
    public String toString() {

	return "south(" + getSouth() + ")," + "west(" + getWest() + ")," + "north(" + getNorth() + ")," + "east(" + getEast() + ")";
    }
}
