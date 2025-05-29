package eu.essi_lab.messages.bond.parser;

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

import java.util.List;

import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.LogicalBond;
import eu.essi_lab.messages.bond.QueryableBond;
import eu.essi_lab.messages.bond.ResourcePropertyBond;
import eu.essi_lab.messages.bond.RuntimeInfoElementBond;
import eu.essi_lab.messages.bond.SimpleValueBond;
import eu.essi_lab.messages.bond.SpatialBond;
import eu.essi_lab.messages.bond.ViewBond;

/**
 * Parses a given {@link Bond} and generates events for a supplied {@link DiscoveryBondHandler}
 *
 * @author Fabrizio
 * @see DiscoveryBondHandler
 */
public class DiscoveryBondParser implements BondParser<DiscoveryBondHandler> {

    private DiscoveryBondHandler handler;
    private Bond bond;

    /**
     * @param message
     */
    public DiscoveryBondParser(DiscoveryMessage message) {

	this.bond = message.getPermittedBond();
    }

    /**
     * @param bond
     */
    public DiscoveryBondParser(Bond bond) {

	this.bond = bond;
    }

    /**
     * Parses the given {@link Bond} and generates events for the supplied {@link DiscoveryBondHandler}
     *
     * @param handler
     */
    public void parse(DiscoveryBondHandler handler) {

	this.handler = handler;
	this.parse(bond);
    }

    private void parse(Bond bond) {

	if (bond == null) {

	    return;
	}

	if (bond instanceof LogicalBond) {

	    LogicalBond b = (LogicalBond) bond;
	    List<Bond> operands = b.getOperands();
	    Bond[] array = operands.toArray(new Bond[] {});

	    handler.startLogicalBond(b);

	    for (int i = 0; i < array.length; i++) {

		Bond operand = array[i];
		parse(operand);

		if (i < array.length - 1) {

		    handler.separator();
		}
	    }

	    handler.endLogicalBond(b);

	} else {

	    handler.nonLogicalBond(bond);

	    parseNonLogicalBond(bond);
	}
    }

    private void parseNonLogicalBond(Bond bond) {

	if (bond instanceof SpatialBond) {

	    SpatialBond b = (SpatialBond) bond;

	    handler.spatialBond(b);

	} else if (bond instanceof SimpleValueBond) {

	    SimpleValueBond b = (SimpleValueBond) bond;

	    handler.simpleValueBond(b);

	} else if (bond instanceof ResourcePropertyBond) {

	    ResourcePropertyBond b = (ResourcePropertyBond) bond;

	    handler.resourcePropertyBond(b);

	} else if (bond instanceof RuntimeInfoElementBond) {

	    RuntimeInfoElementBond b = (RuntimeInfoElementBond) bond;

	    handler.runtimeInfoElementBond(b);

	} else if (bond instanceof QueryableBond<?>) {

	    @SuppressWarnings("unchecked")
	    QueryableBond<String> b = (QueryableBond<String>) bond;

	    handler.customBond(b);

	} else if (bond instanceof ViewBond) {

	    ViewBond viewBond = (ViewBond) bond;

	    handler.viewBond(viewBond);
	}
    }
}
