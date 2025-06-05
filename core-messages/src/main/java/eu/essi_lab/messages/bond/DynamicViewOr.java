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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DynamicViewOr extends DynamicView {

    public static final String OR_PREFIX = "or";

    public DynamicViewOr() {

    }

    @Override
    public String getPrefix() {
	return OR_PREFIX;
    }

    @Override
    public String getLabel() {

	String ret = "";

	for (String arg : arguments) {
	    ret += arg + " OR ";
	}
	ret = ret.substring(0, ret.length() - 4);

	return "(" + ret + ")";
    }

    @Override
    public Bond getDynamicBond() {
	List<Bond> operands = new ArrayList<>();
	for (String argument : arguments) {
	    // the arguments can be dynamic views or db views
	    Optional<DynamicView> dynamicView = DynamicView.resolveDynamicView(argument);
	    if (dynamicView.isPresent()) {
		operands.add(dynamicView.get().getBond());
	    } else {
		operands.add(BondFactory.createViewBond(argument));
	    }
	}
	return BondFactory.createOrBond(operands);
    }

}
