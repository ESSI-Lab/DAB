package eu.essi_lab.accessor.waf.onamet_stations;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 - 2022 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

/**
 * @author Fabrizio
 */
public class ONAMETParameter {

    /**
     * @author Fabrizio
     */
    public enum ONAMETParameterId {

	/**
	 * 
	 */
	PE("Pressure"),
	/**
	 * 
	 */
	TA("Temperature"),
	/**
	 * 
	 */
	HR("Relative humidity"),
	/**
	 * 
	 */
	PR("Precipitation"),
	/**
	 * 
	 */
	DV("Wind direction"),
	/**
	 * 
	 */
	VV("Wind force"),
	/**
	 * 
	 */
	RS("Solar radiation");

	private String name;

	/**
	 * @param name
	 */
	private ONAMETParameterId(String name) {

	    this.name = name;
	}

	/**
	 * @return the name
	 */
	public String getName() {

	    return name;
	}
    }

    /**
     * [parameter, name, units, instrument, valid range, resolution, interpolation, aggregation duration]
     */
    private String[] array;

    /**
     * @param array
     */
    public ONAMETParameter(String[] array) {

	this.array = array;
    }

    /**
     * @param list
     * @param parameterId
     * @return
     */
    public static ONAMETParameter find(List<String[]> list, String parameterId) {

	return list.stream().//
		filter(array -> array[0].equals(parameterId)).//
		map(array -> new ONAMETParameter(array)).//
		findFirst().//
		get();
    }

    /**
     * @return the parameter
     */
    public String getId() {

	return array[0];
    }

    /**
     * @return the name
     */
    public String getName() {
	return array[1];
    }

    /**
     * @return the units
     */
    public String getUnits() {
	return array[2];
    }

    /**
     * @return the instrument
     */
    public String getInstrument() {
	return array[3];
    }

    /**
     * @return the valid_range
     */
    public String getValidRange() {
	return array[4];
    }

    /**
     * @return the resolution
     */
    public String getResolution() {
	return array[5];
    }

    /**
     * @return the interpolation
     */
    public String getInterpolation() {
	return array[6];
    }

    /**
     * @return the aggregation
     */
    public String getAggregation() {
	return array[7];
    }

    /**
     * @return the duration
     */
    public String getDuration() {
	return array[8];
    }

}
