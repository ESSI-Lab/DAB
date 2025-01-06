package eu.essi_lab.accessor.ana;

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

import eu.essi_lab.model.resource.InterpolationType;

public enum ANAVariable {

    /**
     * The values of variables are taken from the following README:
     * http://dd.weather.gc.ca/hydrometric/doc/hydrometric_README.txt
     */

    CHUVA("Chuva", "millimeter", "Length", "mm", "Chuva", 54,InterpolationType.TOTAL_PREC,"*:Chuva"), //
    NIVEL("Nivel", "Centimetre", "Length", "cm", "River Level", 47,InterpolationType.CONTINUOUS,"*:Nivel"), //
    VAZAO("Vazao", "Cubic metre per second", "Flow", "m^3/s", "River Flow", 36,InterpolationType.CONTINUOUS,"*:Vazao"); //

    String label;
    String unit;
    String description;
    String dataType;
    String abbreviation;
    Integer unitsID;
    InterpolationType interpolation;
    String xpath;

    ANAVariable(String label, String unit, String dataType, String abbreviation, String description, Integer unitID,
	    InterpolationType interpolation,String xpath) {
	this.label = label;
	this.unit = unit;
	this.dataType = dataType;
	this.abbreviation = abbreviation;
	this.description = description;
	this.unitsID = unitID;
	this.interpolation = interpolation;
	this.xpath = xpath;
    }

    public String getLabel() {
	return label;
    }
    
    public String getXPath() {
	return xpath;
    }

    public void setLabel(String label) {
	this.label = label;
    }

    public InterpolationType getInterpolation() {
	return interpolation;
    }

    public void setInterpolation(InterpolationType interpolation) {
	this.interpolation = interpolation;
    }

    public String getDescription() {
	return description;
    }

    public void setDescription(String description) {
	this.description = description;
    }

    public String getAbbreviation() {
	return abbreviation;
    }

    public void setAbbreviation(String abbreviation) {
	this.abbreviation = abbreviation;
    }

    public Integer getUnitsID() {
	return unitsID;
    }

    public void setUnitsID(Integer unitsID) {
	this.unitsID = unitsID;
    }

    public void setUnit(String unit) {
	this.unit = unit;
    }

    public void setDataType(String dataType) {
	this.dataType = dataType;
    }

    public String getUnit() {
	return unit;
    }

    @Override
    public String toString() {
	return label;
    }

    public static ANAVariable decode(String variable) {
	for (ANAVariable ret : ANAVariable.values()) {
	    if (ret.toString().contains(variable) || variable.contains(ret.toString())) {
		return ret;
	    }
	}
	return null;
    }

    public String getDataType() {
	return dataType;
    }
}
