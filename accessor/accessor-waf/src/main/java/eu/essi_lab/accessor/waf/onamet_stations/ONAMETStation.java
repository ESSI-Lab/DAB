package eu.essi_lab.accessor.waf.onamet_stations;

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

import java.util.Arrays;
import java.util.List;

/**
 * @author Fabrizio
 */
public class ONAMETStation {

    /**
     * [id, name, location, lat, lon, UTM-19Q-X, UTM-19Q-Y, elevation, parameter, institution, country, phone, email]
     */
    private String[] array;

    /**
     * @param array
     */
    public ONAMETStation(String[] array) {

	this.array = array;
    }

    /**
     * @return the id
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
     * @return the location
     */
    public String getLocation() {

	return array[2];
    }

    /**
     * @return the lat
     */
    public String getLat() {

	return array[3];
    }

    /**
     * @return the lon
     */
    public String getLon() {

	return array[4];
    }

    /**
     * @return the elevation
     */
    public String getElevation() {

	return array[7];
    }

    /**
     * @return the parameters
     */
    public List<String> getParameters() {

	return Arrays.asList(array[8].split(" "));
    }

    /**
     * @return the institution
     */
    public String getInstitution() {

	return array[9];
    }

    /**
     * @return the country
     */
    public String getCountry() {

	return array[10];
    }

    /**
     * @return the phone
     */
    public String getPhone() {

	return array[11];
    }

    /**
     * @return the email
     */
    public String getEmail() {

	return array[12];
    }

}
