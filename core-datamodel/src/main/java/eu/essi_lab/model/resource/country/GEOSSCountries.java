package eu.essi_lab.model.resource.country;

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

import java.util.HashMap;

import eu.essi_lab.model.resource.Country;

public class GEOSSCountries {

    private HashMap<Country, Integer> map = new HashMap<>();

    public GEOSSCountries() {
	add("Australia", 2);
	add("Austria", 1);
	add("Brazil", 1);
	add("Canada", 5);
	add("Georgia", 1);
	add("Chile", 2);
	add("China", 1);
	add("Cyprus", 1);
	add("Denmark", 1);
	add("Europe", 38);
	add("Finland", 1);
	add("France", 3);
	add("Germany", 7);
//	add("Global", 25);
	add("Haiti", 3);
	add("Iceland", 1);
	add("IGAD Region", 1);
	add("India", 1);
	add("Italy", 8);
	add("Japan", 3);
	add("Kenya", 1);
	add("GEO SUR", 1);
	add("Moldova", 1);
	add("Nepal", 1);
	add("Netherlands", 2);
	add("New Zealand", 4);
	add("Norway", 3);
	add("Russia", 2);
	add("South Africa", 1);
	add("Spain", 3);
	add("Sweden", 1);
	add("Switzerland", 5);
	add("United Kingdom", 2);
	add("United States of America", 59);
	add("West Africa, ECOWAS member states", 1);
    }

    private void add(String country, int total) {
	CountrySet set = CountrySet.decode(country);
	if (set.getCountries().isEmpty()) {
	    System.out.println("Error decoding: " + country);
	    return;
	}
	for (Country c : set.getCountries()) {
	    add(c, total);
	}

    }

    private void add(Country c, int total) {
	Integer actual = map.get(c);
	if (actual == null) {
	    actual = 0;
	}
	map.put(c, actual + total);

    }

    public static void main(String[] args) {
	GEOSSCountries gc = new GEOSSCountries();
	gc.print();
    }

    private void print() {
	for (Country c : map.keySet()) {
	    System.out.println(c.getShortName() + "\t" + map.get(c));
	}

    }
}
