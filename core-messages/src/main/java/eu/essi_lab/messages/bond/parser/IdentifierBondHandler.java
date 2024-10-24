package eu.essi_lab.messages.bond.parser;

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

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.BondOperator;
import eu.essi_lab.messages.bond.LogicalBond;
import eu.essi_lab.messages.bond.LogicalBond.LogicalOperator;
import eu.essi_lab.messages.bond.QueryableBond;
import eu.essi_lab.messages.bond.ResourcePropertyBond;
import eu.essi_lab.messages.bond.RuntimeInfoElementBond;
import eu.essi_lab.messages.bond.SimpleValueBond;
import eu.essi_lab.messages.bond.SpatialBond;
import eu.essi_lab.messages.bond.ViewBond;
import eu.essi_lab.model.resource.MetadataElement;

/**
 * Parse a bond to gather all identifiers involved in the query, specified by simple value bonds using the identifier
 * metadata element.
 * Moreover it records if the bond is in canonical get record by identifiers form.
 * 
 * @author boldrini
 */
public class IdentifierBondHandler implements DiscoveryBondHandler {

    HashSet<String> identifiers = new HashSet<>();

    // initialized to true, the value might change to false during parsing
    private Boolean queryByIdentifiers = true;

    /**
     * Returns true if and only if this bond has one of the two forms :
     * 1) simple query by identifier bond (e.g. ID=ID1)
     * 2) OR composed query by identifiers (e.g. ID=ID1 OR ID=ID2 OR ... OR ID=IDN).
     * 
     * @return
     */
    public Boolean isCanonicalQueryByIdentifiers() {
	return queryByIdentifiers;
    }

    /**
     * The identifiers found during the parsing
     * 
     * @return
     */
    public List<String> getIdentifiers() {

	return identifiers.stream().collect(Collectors.toList());
    }

    public IdentifierBondHandler(Bond bond) {
	DiscoveryBondParser parser = new DiscoveryBondParser(bond);
	if (bond == null) {
	    queryByIdentifiers = false;
	    return;
	}
	queryByIdentifiers = true;
	parser.parse(this);
    }

    @Override
    public void separator() {
	// TODO Auto-generated method stub
    }

    @Override
    public void simpleValueBond(SimpleValueBond bond) {
	customBond(bond);
    }

    @Override
    public void spatialBond(SpatialBond b) {
	queryByIdentifiers = false;

    }

    @Override
    public void startLogicalBond(LogicalBond b) {
	if (!b.getLogicalOperator().equals(LogicalOperator.OR)) {
	    queryByIdentifiers = false;
	}

    }

    @Override
    public void endLogicalBond(LogicalBond b) {
	if (!b.getLogicalOperator().equals(LogicalOperator.OR)) {
	    queryByIdentifiers = false;
	}

    }

    @Override
    public void resourcePropertyBond(ResourcePropertyBond bond) {
	queryByIdentifiers = false;
    }

    @Override
    public void customBond(QueryableBond<String> bond) {

	if (bond.getProperty().equals(MetadataElement.IDENTIFIER) && bond.getOperator().equals(BondOperator.EQUAL)) {
	    String identifier = bond.getPropertyValue();
	    identifiers.add(identifier);
	} else {
	    queryByIdentifiers = false;
	}
    }

    @Override
    public void viewBond(ViewBond bond) {
	queryByIdentifiers = false;

    }

    @Override
    public void nonLogicalBond(Bond bond) {

    }

    @Override
    public void runtimeInfoElementBond(RuntimeInfoElementBond bond) {
	// TODO Auto-generated method stub

    }

}
