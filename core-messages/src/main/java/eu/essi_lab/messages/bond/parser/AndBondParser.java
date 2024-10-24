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
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;

/**
 * Utility class that parses the given bond, extracting the bonds that are in AND. It expects that the bond to parse is
 * a combination of bonds in AND, otherwise it will throw an exception.
 * 
 * @author boldrini
 */
public class AndBondParser {

    public class AndBondHandler implements DiscoveryBondHandler {

	Set<Bond> bonds = new HashSet<>();
	private String exceptionMessage = null;
	private String errorId = null;
	public static final String AND_BOND_PARSER_UNEXPECTED_OPERATOR = "AND_BOND_PARSER_UNEXPECTED_OPERATOR";

	public Set<Bond> getBonds() throws GSException {

	    if (exceptionMessage != null) {
		throwException();
	    }

	    return bonds;
	}

	@Override
	public void separator() {

	}

	@Override
	public void simpleValueBond(SimpleValueBond bond) {
	    this.bonds.add(bond);

	}

	@Override
	public void spatialBond(SpatialBond b) {
	    this.bonds.add(b);

	}

	@Override
	public void customBond(QueryableBond<String> bond) {
	    this.bonds.add(bond);

	}

	@Override
	public void startLogicalBond(LogicalBond b) {
	    if (!b.getLogicalOperator().equals(LogicalOperator.AND)) {
		this.errorId = "AND_BOND_PARSER_UNEXPECTED_OPERATOR";
		this.exceptionMessage = "Unexpected logical operator: " + b.getLogicalOperator();

	    }

	}

	@Override
	public void endLogicalBond(LogicalBond b) {

	}

	private void throwException() throws GSException {
	    
	    ErrorInfo info = new ErrorInfo();
	    info.setCaller(this.getClass());
	    info.setErrorDescription(exceptionMessage);
	    info.setErrorId(errorId);
	    info.setErrorType(ErrorInfo.ERRORTYPE_INTERNAL);
	    info.setErrorCorrection("Please provide valid arguments to the And Bond Parser");
	    info.setSeverity(ErrorInfo.SEVERITY_ERROR);

	    throw GSException.createException(info);
	}

	@Override
	public void resourcePropertyBond(ResourcePropertyBond bond) {
	    this.bonds.add(bond);

	}

	@Override
	public void viewBond(ViewBond bond) {
	    this.bonds.add(bond);

	}

	@Override
	public void nonLogicalBond(Bond bond) {
	    this.bonds.add(bond);
	}

	@Override
	public void runtimeInfoElementBond(RuntimeInfoElementBond bond) {
	    // TODO Auto-generated method stub

	}

    }

    /**
     * Extracts the bonds that are in AND.
     * 
     * @param bond
     * @return
     * @throws GSException in case the given bond is not a series of bond in AND
     */
    public Set<Bond> parseBond(Bond bond) throws GSException {
	Set<Bond> ret = new HashSet<>();
	if (bond == null) {
	    ret.add(null);
	    return ret;
	}
	DiscoveryBondParser parser = new DiscoveryBondParser(bond);
	AndBondHandler abh = new AndBondHandler();
	parser.parse(abh);
	return abh.getBonds();
    }

}
