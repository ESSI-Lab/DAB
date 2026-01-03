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

import eu.essi_lab.model.resource.Country;

public class GEOSUR extends java.util.ArrayList<Country> {

    public GEOSUR() {
	add(Country.ARGENTINA);
	add(Country.BELIZE);
	add(Country.BRAZIL);
	add(Country.CHILE);
	add(Country.COLOMBIA);
	add(Country.COSTA_RICA);
	add(Country.DOMINICAN_REPUBLIC);
	add(Country.ECUADOR);
	add(Country.EL_SALVADOR);
	add(Country.GUATEMALA);
	add(Country.HONDURAS);
	add(Country.JAMAICA);
	add(Country.MEXICO);
	add(Country.NICARAGUA);
	add(Country.PANAMA);
	add(Country.PARAGUAY);
	add(Country.PERU);
	add(Country.TRINIDAD_AND_TOBAGO);
	add(Country.URUGUAY);
	add(Country.VENEZUELA_BOLIVARIAN_REPUBLIC_OF);
	


	

    }

    public static void main(String[] args) {
	GEOSUR europe = new GEOSUR();
	europe.printISO3Codes();
    }

    private void printISO3Codes() {
	for (Country country : this) {
	    System.out.println(country.getISO3());
	}
    }

}
