package eu.essi_lab.pdk;

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

import java.util.Optional;

import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.BondFactory;
import eu.essi_lab.messages.bond.BondOperator;
import eu.essi_lab.messages.bond.LogicalBond;
import eu.essi_lab.model.resource.MetadataElement;

public class BondUtils {
    /**
     * @param value
     * @param element
     * @return
     */
    public static Optional<Bond> createBond(BondOperator operator, String value, MetadataElement element) {

	if (value == null || value.equals("")) {
	    return Optional.empty();
	}

	if (value.contains(" AND ")) {
	    String[] split = value.split(" AND ");
	    LogicalBond bond = BondFactory.createAndBond();
	    for (String s : split) {
		bond.getOperands().add(BondFactory.createSimpleValueBond(operator, element, s.trim()));
	    }
	    return Optional.of(bond);
	}

	if (value.contains(" OR ")) {
	    String[] split = value.split(" OR ");
	    LogicalBond bond = BondFactory.createOrBond();
	    for (String s : split) {
		bond.getOperands().add(BondFactory.createSimpleValueBond(operator, element, s.trim()));
	    }
	    return Optional.of(bond);
	}

	return Optional.of(BondFactory.createSimpleValueBond(operator, element, value.trim()));
    }
}
