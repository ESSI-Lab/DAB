package eu.essi_lab.accessor.polytope;

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

import java.util.List;

public class PolytopeStation {

    String stationCode; // e.g. Ams01

    String name; // Ams01

    String minLat; // e.g. 52.378

    String minLon; // e.g. 48.942ù

    String maxLat; // e.g. 52.378

    String maxLon; // e.g. 48.942

    String minElevation; //

    String maxElevation;

//    String startDate; // e.g. 20150602 (yyyymmdd)
//
//    String endDate; // e.g. 20150902 (yyyymmdd)
//
//    String startTime; // e.g. 1500 (yyyymmdd)
//
//    String endTime; // e.g. 1500 (yyyymmdd)
    
    String startDateTime; // 2022-05-25T15:00:00z
    
    String endDateTime; // 2022-05-25T15:00:00z

    String icao; //

    String state; //

    String country; //

    List<List<Double>> multiPoint;

    public PolytopeStation() {

    }

    public PolytopeStation(String stationCode, String name, List<List<Double>> multipoint, String minLat, String minLon, String maxLat,
	    String maxLon, String minElevation, String maxElevation, String startDateTime, String endDateTime) {
	this.stationCode = stationCode;
	this.name = name;
	this.multiPoint = multipoint;
	this.minLat = minLat;
	this.minLon = minLon;
	this.maxLat = maxLat;
	this.maxLon = maxLon;
	this.minElevation = minElevation;
	this.maxElevation = maxElevation;
	this.startDateTime = startDateTime;
	this.endDateTime = endDateTime;

    }
    
    
    public String getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(String startDateTime) {
        this.startDateTime = startDateTime;
    }
    
    public String getEndDateTime() {
        return endDateTime;
    }

    public void setEndDateTime(String endDateTime) {
        this.endDateTime = endDateTime;
    }

    public List<List<Double>> getMultiPoint() {
	return multiPoint;
    }

    public void setMultiPoint(List<List<Double>> multiPoint) {
	this.multiPoint = multiPoint;
    }

    public String getStationCode() {
	return stationCode;
    }

    public void setStationCode(String stationCode) {
	this.stationCode = stationCode;
    }

    public String getName() {
	return name;
    }

    public void setName(String name) {
	this.name = name;
    }

    public String getMinLat() {
	return minLat;
    }

    public void setMinLat(String lat) {
	this.minLat = lat;
    }

    public String getMinLon() {
	return minLon;
    }

    public void setMinLon(String lon) {
	this.minLon = lon;
    }

    public String getMaxLat() {
	return maxLat;
    }

    public void setMaxLat(String lat) {
	this.maxLat = lat;
    }

    public String getMaxLon() {
	return maxLon;
    }

    public void setMaxLon(String lon) {
	this.maxLon = lon;
    }

    public String getMinElevation() {
	return minElevation;
    }

    public void setMinElevation(String elevation) {
	this.minElevation = elevation;
    }

    public String getMaxElevation() {
	return maxElevation;
    }

    public void setMaxElevation(String elevation) {
	this.maxElevation = elevation;
    }

    public String getIcao() {
	return icao;
    }

    public void setIcao(String icao) {
	this.icao = icao;
    }

    public String getState() {
	return state;
    }

    public void setState(String state) {
	this.state = state;
    }

    public String getCountry() {
	return country;
    }

    public void setCountry(String country) {
	this.country = country;
    }

}
