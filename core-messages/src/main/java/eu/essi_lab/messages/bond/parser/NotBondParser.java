package eu.essi_lab.messages.bond.parser;

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

import java.util.HashSet;
import java.util.Set;

import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.LogicalBond;
import eu.essi_lab.messages.bond.LogicalBond.LogicalOperator;
import eu.essi_lab.messages.bond.QueryableBond;
import eu.essi_lab.messages.bond.ResourcePropertyBond;
import eu.essi_lab.messages.bond.RuntimeInfoElementBond;
import eu.essi_lab.messages.bond.SimpleValueBond;
import eu.essi_lab.messages.bond.SpatialBond;
import eu.essi_lab.messages.bond.ViewBond;

/**
 * Utility class that parses the given bond, extracting the bonds that are notBond.
 * 
 * @author boldrini
 */
public class NotBondParser {

    public class NotBondHandler implements DiscoveryBondHandler {

	Set<Bond> bonds = new HashSet<>();

	public Set<Bond> getBonds() {
	    return bonds;
	}

	@Override
	public void separator() {

	}

	@Override
	public void simpleValueBond(SimpleValueBond bond) {

	}

	@Override
	public void spatialBond(SpatialBond b) {

	}

	@Override
	public void customBond(QueryableBond<String> bond) {

	}

	@Override
	public void startLogicalBond(LogicalBond b) {
	    if (b.getLogicalOperator().equals(LogicalOperator.NOT)) {
		this.bonds.add(b);
	    }

	}

	@Override
	public void endLogicalBond(LogicalBond b) {

	}

	@Override
	public void resourcePropertyBond(ResourcePropertyBond bond) {

	}

	@Override
	public void viewBond(ViewBond bond) {

	}

	@Override
	public void nonLogicalBond(Bond bond) {
	    // TODO Auto-generated method stub

	}

	@Override
	public void runtimeInfoElementBond(RuntimeInfoElementBond bond) {
	    // TODO Auto-generated method stub
	    
	}

    }

    /**
     * Extracts the NOT bonds in a bond
     * 
     * @param bond
     * @return the set of not bonds
     */
    public Set<Bond> parseBond(Bond bond) {
	Set<Bond> ret = new HashSet<>();
	if (bond == null) {
	    ret.add(null);
	    return ret;
	}
	DiscoveryBondParser parser = new DiscoveryBondParser(bond);
	NotBondHandler abh = new NotBondHandler();
	parser.parse(abh);
	return abh.getBonds();
    }

}
