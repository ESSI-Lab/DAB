/**
 * 
 */
package eu.essi_lab.lib.net.nominatim.query;

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

/**
 * @author Fabrizio
 */
public class StructuredQuery extends NominatimQuery {

    private String amenity; // name and/or type of POI
    private String street; // house number and street name
    private String city; // city
    private String county; // county
    private String state; // state
    private String country; // country
    private String postalcode; // postal code

    /**
     * 
     */
    public StructuredQuery() {

    }

    /**
     * @return
     */
    public String getAmenity() {

	return amenity;
    }

    /**
     * @param amenity
     */
    public void setAmenity(String amenity) {

	this.amenity = amenity;
	addParam(amenity, amenity);
    }

    /**
     * @return
     */
    public String getStreet() {

	return street;
    }

    /**
     * @param street
     */
    public void setStreet(String street) {

	this.street = street;
	addParam(street, street);
    }

    /**
     * @return
     */
    public String getCity() {

	return city;
    }

    /**
     * @param city
     */
    public void setCity(String city) {

	this.city = city;
	addParam(city, city);
    }

    /**
     * @return
     */
    public String getCounty() {

	return county;
    }

    /**
     * @param county
     */
    public void setCounty(String county) {

	this.county = county;
	addParam(county, county);
    }

    /**
     * @return
     */
    public String getState() {

	return state;
    }

    /**
     * @param state
     */
    public void setState(String state) {

	this.state = state;
	addParam(state, state);
    }

    /**
     * @return
     */
    public String getCountry() {

	return country;
    }

    /**
     * @param country
     */
    public void setCountry(String country) {

	this.country = country;
    }

    /**
     * @return
     */
    public String getPostalcode() {

	return postalcode;
    }

    /**
     * @param postalCode
     */
    public void setPostalcode(String postalCode) {

	this.postalcode = postalCode;
    }
}
