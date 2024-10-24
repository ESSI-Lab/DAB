package eu.essi_lab.model.resource.country;

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

import eu.essi_lab.model.resource.Country;

public class IGADRegion extends java.util.ArrayList<Country> {

    public IGADRegion() {
	add(Country.DJIBOUTI);
	add(Country.ETHIOPIA);
	add(Country.KENYA);
	add(Country.SOMALIA);
	add(Country.SUDAN);
	add(Country.UGANDA);

    }

    public static void main(String[] args) {
	IGADRegion europe = new IGADRegion();
	europe.printISO3Codes();
    }

    private void printISO3Codes() {
	for (Country country : this) {
	    System.out.println(country.getISO3());
	}
    }

}
