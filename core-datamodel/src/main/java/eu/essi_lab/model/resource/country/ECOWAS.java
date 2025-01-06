package eu.essi_lab.model.resource.country;

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

import eu.essi_lab.model.resource.Country;

public class ECOWAS extends java.util.ArrayList<Country> {

    public ECOWAS() {
	add(Country.CABO_VERDE);
	add(Country.GAMBIA);
	add(Country.GUINEA);
	add(Country.GUINEABISSAU);
	add(Country.LIBERIA);
	add(Country.MALI);
	add(Country.SENEGAL);
	add(Country.SIERRA_LEONE);
	add(Country.BENIN);
	add(Country.BURKINA_FASO);
	add(Country.GHANA);
	add(Country.CÔTE_DIVOIRE);
	add(Country.NIGER);
	add(Country.NIGERIA);
	add(Country.TOGO);
	


	

    }

    public static void main(String[] args) {
	ECOWAS europe = new ECOWAS();
	europe.printISO3Codes();
    }

    private void printISO3Codes() {
	for (Country country : this) {
	    System.out.println(country.getISO3());
	}
    }

}
