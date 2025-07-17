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

public class RowInfo {
    private String siteCount = null;
    private String attributeCount = null;
    public String getSiteCount() {
        return siteCount;
    }

    public void setSiteCount(String siteCount) {
        this.siteCount = siteCount;
    }

    public String getAttributeCount() {
        return attributeCount;
    }

    public void setAttributeCount(String attributeCount) {
        this.attributeCount = attributeCount;
    }

    public String getTimeseriesCount() {
        return timeseriesCount;
    }

    public void setTimeseriesCount(String timeseriesCount) {
        this.timeseriesCount = timeseriesCount;
    }

    public String getBegin() {
        return begin;
    }

    public void setBegin(String begin) {
        this.begin = begin;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    public Double getWest() {
        return west;
    }

    public void setWest(Double west) {
        this.west = west;
    }

    public Double getSouth() {
        return south;
    }

    public void setSouth(Double south) {
        this.south = south;
    }

    public Double getEast() {
        return east;
    }

    public void setEast(Double east) {
        this.east = east;
    }

    public Double getNorth() {
        return north;
    }

    public void setNorth(Double north) {
        this.north = north;
    }

    private String timeseriesCount = null;
    private String begin = null;
    private String end = null;
    private Double west = null;
    private Double south = null;
    private Double east = null;
    private Double north = null;
    private String uniqueVariableCode = null;
    private String variableCode = null;
    private String variableName = null;
    private String variableURI = null;
    private String variableDescription = null;
    private String variableUnits = null;
    private String variableUnitsURI = null;
    private String interpolation = null;
    private String interpolationSupport = null;
    private String aggregationPeriod = null;
    private String interval = null;
    private String intendedObservationSpacing = null;
    private String interpolationSupportUnits = null;

    public String getUniqueVariableCode() {
	return uniqueVariableCode;
    }

    public void setUniqueVariableCode(String uniqueVariableCode) {
	this.uniqueVariableCode = uniqueVariableCode;
    }

    public String getVariableCode() {
	return variableCode;
    }

    public void setVariableCode(String variableCode) {
	this.variableCode = variableCode;
    }

    public String getVariableName() {
	return variableName;
    }

    public void setVariableName(String variableName) {
	this.variableName = variableName;
    }

    public String getVariableURI() {
	return variableURI;
    }

    public void setVariableURI(String variableURI) {
	this.variableURI = variableURI;
    }

    public String getVariableDescription() {
	return variableDescription;
    }

    public void setVariableDescription(String variableDescription) {
	this.variableDescription = variableDescription;
    }

    public String getVariableUnits() {
	return variableUnits;
    }

    public void setVariableUnits(String variableUnits) {
	this.variableUnits = variableUnits;
    }

    public String getVariableUnitsURI() {
	return variableUnitsURI;
    }

    public void setVariableUnitsURI(String variableUnitsURI) {
	this.variableUnitsURI = variableUnitsURI;
    }

    public String getInterpolation() {
	return interpolation;
    }

    public void setInterpolation(String interpolation) {
	this.interpolation = interpolation;
    }

    public String getInterpolationSupport() {
	return interpolationSupport;
    }

    public void setInterpolationSupport(String interpolationSupport) {
	this.interpolationSupport = interpolationSupport;
    }

    public String getAggregationPeriod() {
	return aggregationPeriod;
    }

    public void setAggregationPeriod(String aggregationPeriod) {
	this.aggregationPeriod = aggregationPeriod;
    }

    public String getInterval() {
	return interval;
    }

    public void setInterval(String interval) {
	this.interval = interval;
    }

    public String getIntendedObservationSpacing() {
	return intendedObservationSpacing;
    }

    public void setIntendedObservationSpacing(String intendedObservationSpacing) {
	this.intendedObservationSpacing = intendedObservationSpacing;
    }

    public String getInterpolationSupportUnits() {
	return interpolationSupportUnits;
    }

    public void setInterpolationSupportUnits(String interpolationSupportUnits) {
	this.interpolationSupportUnits = interpolationSupportUnits;
    }

    public String getCountry() {
	return country;
    }

    public void setCountry(String country) {
	this.country = country;
    }

    public String getCountryISO3() {
	return countryISO3;
    }

    public void setCountryISO3(String countryISO3) {
	this.countryISO3 = countryISO3;
    }

    private String country = null;
    private String countryISO3 = null;
}
