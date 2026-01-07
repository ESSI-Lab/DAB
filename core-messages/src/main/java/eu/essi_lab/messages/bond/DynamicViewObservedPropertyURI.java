package eu.essi_lab.messages.bond;

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

import eu.essi_lab.model.resource.MetadataElement;

public class DynamicViewObservedPropertyURI extends DynamicView {

    public static final String OBSERVED_PROPERTY_URI_PREFIX = "observedPropertyURI";

    public DynamicViewObservedPropertyURI() {

    }

    public DynamicViewObservedPropertyURI(String sourceId) {
	super();
	setPostfix(sourceId);
    }

    @Override
    public String getPrefix() {
	return OBSERVED_PROPERTY_URI_PREFIX;
    }

    @Override
    public String getLabel() {
	return "Dyanamic view for observed property URI: " + arguments.getFirst();
    }

    @Override
    public Bond getDynamicBond() {
	return BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.OBSERVED_PROPERTY_URI, arguments.getFirst());
    }

}
