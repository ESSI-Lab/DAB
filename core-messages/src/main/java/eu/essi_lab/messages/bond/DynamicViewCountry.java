package eu.essi_lab.messages.bond;

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

import eu.essi_lab.model.resource.MetadataElement;

public class DynamicViewCountry extends DynamicView {

    public static final String COUNTRY_PREFIX = "country";

    public DynamicViewCountry() {

    }

    public DynamicViewCountry(String sourceId) {
	super();
	setPostfix(sourceId);
    }

    @Override
    public String getPrefix() {
	return COUNTRY_PREFIX;
    }

    @Override
    public String getLabel() {
	return "Dyanamic view for country: " + arguments.get(0);
    }

    @Override
    public Bond getDynamicBond() {
	return BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.COUNTRY_ISO3, arguments.get(0));
    }

}
