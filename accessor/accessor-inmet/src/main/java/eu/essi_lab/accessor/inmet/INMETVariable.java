package eu.essi_lab.accessor.inmet;

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

import eu.essi_lab.model.resource.InterpolationType;

public enum INMETVariable {

    /**
     * 
     */

    PRECIPITATION("Precipitation", "millimeter", InterpolationType.TOTAL, "mm", "Precipitation", 54);

    String label;
    String unit;
    String description;
    InterpolationType dataType;
    String abbreviation;
    Integer unitsID;

    INMETVariable(String label, String unit, InterpolationType dataType, String abbreviation, String description, Integer unitID) {
	this.label = label;
	this.unit = unit;
	this.dataType = dataType;
	this.abbreviation = abbreviation;
	this.description = description;
	this.unitsID = unitID;
    }

    public String getLabel() {
	return label;
    }

    public void setLabel(String label) {
	this.label = label;
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

    public void setDataType(InterpolationType dataType) {
	this.dataType = dataType;
    }

    public String getUnit() {
	return unit;
    }

    @Override
    public String toString() {
	return label;
    }

    public static INMETVariable decode(String variable) {
	for (INMETVariable ret : INMETVariable.values()) {
	    if (ret.toString().contains(variable) || variable.contains(ret.toString())) {
		return ret;
	    }
	}
	return null;
    }

    public InterpolationType getDataType() {
	return dataType;
    }
}