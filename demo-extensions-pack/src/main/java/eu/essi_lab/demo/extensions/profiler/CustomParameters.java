package eu.essi_lab.demo.extensions.profiler;

/*-
 * #%L
 * Discovery and Access Broker (DAB) Community Edition (CE)
 * %%
 * Copyright (C) 2021 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
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

import java.util.Optional;

import eu.essi_lab.demo.extensions.CustomQueryable;
import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.BondOperator;
import eu.essi_lab.messages.bond.CustomBondFactory;
import eu.essi_lab.pdk.wrt.WebRequestParameter;
public abstract class CustomParameters {

    /**
     *
     */
    public static final WebRequestParameter START = new WebRequestParameter("start", "int", "1") {
	@Override
	public Optional<Bond> asBond(String value, String... relatedValues) {
	    return Optional.empty();
	}
    };
    /**
     *
     */
    public static final WebRequestParameter COUNT = new WebRequestParameter("count", "int", "10") {
	@Override
	public Optional<Bond> asBond(String value, String... relatedValues) {
	    return Optional.empty();
	}
    };
    /**
     *
     */
    public static final WebRequestParameter ONLINE_NAME = new WebRequestParameter( //
	    CustomQueryable.ONLINE_NAME.getName(), "string", null) {

	@Override
	public Optional<Bond> asBond(String value, String... relatedValues) {

	    if (value == null || value.equals("")) {
		return Optional.empty();
	    }

	    return Optional.of(CustomBondFactory.createCustomBond(CustomQueryable.ONLINE_NAME, BondOperator.EQUAL, value));
	}
    };
    /**
     *
     */
    public static final WebRequestParameter CONTACT_CITY = new WebRequestParameter(//
	    CustomQueryable.CONTACT_CITY.getName(), "string", null) {

	@Override
	public Optional<Bond> asBond(String value, String... relatedValues) {

	    if (value == null || value.equals("")) {
		return Optional.empty();
	    }

	    return Optional.of(CustomBondFactory.createCustomBond(CustomQueryable.CONTACT_CITY, BondOperator.EQUAL, value));
	}
    };

    private CustomParameters() {
	//force use static methods
    }

}
