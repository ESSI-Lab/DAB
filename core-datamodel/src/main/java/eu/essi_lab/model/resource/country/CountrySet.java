package eu.essi_lab.model.resource.country;

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

import java.util.HashSet;

import eu.essi_lab.model.resource.Country;

public class CountrySet {
    private HashSet<Country> countries = new HashSet<>();

    public HashSet<Country> getCountries() {
	return countries;
    }

    public void setCountries(HashSet<Country> countries) {
	this.countries = countries;
    }

    public static CountrySet decode(String countries) {
	CountrySet ret = new CountrySet();
	Country c = Country.decode(countries);
	if (c != null) {
	    ret.countries.add(c);
	} else {
	    if (countries.toLowerCase().equals("europe")) {
		Europe europe = new Europe();
		ret.countries.addAll(europe);
	    }
	    if (countries.toLowerCase().equals("global")) {
		for (Country child : Country.values()) {
		    ret.countries.add(child);
		}
	    }
	    if (countries.toLowerCase().equals("caucasus")) {
		Caucasus set = new Caucasus();
		ret.countries.addAll(set);
	    }
	    if (countries.equals("IGAD Region")) {
		IGADRegion set = new IGADRegion();
		ret.countries.addAll(set);
	    }
	    if (countries.equals("Latin America and Caribbean")) {
		LatinAmericaAndCaribbean set = new LatinAmericaAndCaribbean();
		ret.countries.addAll(set);
	    }
	    if (countries.equals("GEO SUR")) {
		GEOSUR set = new GEOSUR();
		ret.countries.addAll(set);
	    }
	    if (countries.equals("West Africa, ECOWAS member states")) {
		ECOWAS set = new ECOWAS();
		ret.countries.addAll(set);
	    }
	}
	return ret;
    }
}
