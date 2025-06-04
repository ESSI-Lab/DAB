package eu.essi_lab.profiler.semantic;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
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

public class Stats {
    private String siteCount;

    public void setSiteCount(String siteCount) {
        this.siteCount = siteCount;
    }

    public void setUniqueAttributeCount(String uniqueAttributeCount) {
        this.uniqueAttributeCount = uniqueAttributeCount;
    }

    public void setAttributeCount(String attributeCount) {
        this.attributeCount = attributeCount;
    }

    public void setTimeSeriesCount(String timeSeriesCount) {
        this.timeSeriesCount = timeSeriesCount;
    }

    public void setEast(double east) {
        this.east = east;
    }

    public void setNorth(double north) {
        this.north = north;
    }

    public void setWest(double west) {
        this.west = west;
    }

    public void setSouth(double south) {
        this.south = south;
    }

    public void setBegin(String begin) {
        this.begin = begin;
    }

    public void setEnd(String end) {
        this.end = end;
    }
    
    public void setMinimumAltitude(String altitude) {
        this.minimumAltitude= altitude;
    }

    public void setMaximumAltitude(String altitude) {
        this.maximumAltitude = altitude;
    }

    public String getSiteCount() {
	return siteCount;
    }

    public String getUniqueAttributeCount() {
	return uniqueAttributeCount;
    }

    public String getAttributeCount() {
	return attributeCount;
    }

    public String getTimeSeriesCount() {
	return timeSeriesCount;
    }

    public double getEast() {
	return east;
    }

    public double getNorth() {
	return north;
    }

    public double getWest() {
	return west;
    }

    public double getSouth() {
	return south;
    }

    public String getBegin() {
	return begin;
    }

    public String getEnd() {
	return end;
    }

    private String uniqueAttributeCount;
    private String attributeCount;
    private String timeSeriesCount;
    private double east;
    private double north;
    private double west;
    private double south;
    private String begin;
    private String end;
    private String minimumAltitude;
    private String maximumAltitude;

    public String getMinimumAltitude() {
        return minimumAltitude;
    }

    public String getMaximumAltitude() {
        return maximumAltitude;
    }
}
