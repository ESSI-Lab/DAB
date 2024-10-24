package eu.essi_lab.request.executor.discover;

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

import java.util.ArrayList;

import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.BondFactory;
import eu.essi_lab.messages.bond.LogicalBond;
import eu.essi_lab.messages.bond.ResourcePropertyBond;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.ResourceProperty;

/**
 * Utility class used by the {@link Distributor} to calculate a bond reduced with respect to a source (i.e. the subset
 * of the original bond
 * that is pertinent to the given source). It will raise exception if the arguments are not consistent (e.g. it was
 * asked to calculate a
 * reduced bond on source1 and source1 is not a {@link ResourcePropertyBond} in the original bond, or the original bond
 * is not in normal
 * form.
 *
 * @author boldrini
 */
public class BondReducer {
    public static final String REDUCED_BOND_NULL_SOURCE = "REDUCED_BOND_NULL_SOURCE";
    public static final String REDUCED_BOND_SOURCE_NOT_FOUND = "REDUCED_BOND_SOURCE_NOT_FOUND";
    public static final String REDUCED_BOND_UNEXPECTED_BOND = "REDUCED_BOND_UNEXPECTED_BOND";
    public static final String SOURCE_NOT_FOUND_MESSAGE = "The given source was not found in the original bond to be reduced";
    public static final String UNEXPECTED_BOND_MESSAGE = "The given bond was unexpected (e.g. not in normalized form)";

    public BondReducer() {
    }

    /**
     * Given a normalized bond, returns the reduced bond correspondent to the given source identifier.
     *
     * @param bond
     * @param sourceIdentifier
     * @return the reduced bond, that is the subset of the original bond that is pertinent to the given source
     * @throws GSException in case illegal arguments preventing the bond reduction are passed to this method
     */
    public Bond getReducedBond(Bond bond, String sourceIdentifier) throws GSException {

	if (sourceIdentifier == null) {
	    throw createException(REDUCED_BOND_NULL_SOURCE, "Unable to get reduced bond on source null");
	}

	if (bond == null) {
	    throw createException(REDUCED_BOND_SOURCE_NOT_FOUND, SOURCE_NOT_FOUND_MESSAGE);
	}

	if (bond instanceof LogicalBond) {
	    
	    LogicalBond logicalBond = (LogicalBond) bond;

	    switch (logicalBond.getLogicalOperator()) {
	    case AND:

		ArrayList<Bond> restBonds = new ArrayList<>();
		ResourcePropertyBond sourceBond = null;

		for (Bond child : logicalBond.getOperands()) {
		    if (isSourceIdentifierBond(child)) {
			ResourcePropertyBond mySourceBond = (ResourcePropertyBond) child;
			if (mySourceBond.getPropertyValue().equals(sourceIdentifier)) {
			    sourceBond = mySourceBond;
			}
		    } else {
			restBonds.add(child);
		    }
		}

		if (sourceBond != null) {
		    if (restBonds.isEmpty()) {
			return null;
		    }
		    if (restBonds.size() == 1) {
			return restBonds.iterator().next();
		    }
		    return BondFactory.createAndBond(restBonds);
		}

		throw createException(REDUCED_BOND_SOURCE_NOT_FOUND, SOURCE_NOT_FOUND_MESSAGE);

	    case OR:

		Bond reducedBond = null;
		int children = 0;
		int exceptions = 0;

		for (Bond child : logicalBond.getOperands()) {
		    children++;
		    try {
			reducedBond = getReducedBond(child, sourceIdentifier);
		    } catch (GSException e) {
			// in case the source is not found
			exceptions++;
		    }
		    if (reducedBond != null) {
			return reducedBond;
		    }
		}

		if (children == exceptions) {
		    throw createException(REDUCED_BOND_SOURCE_NOT_FOUND, SOURCE_NOT_FOUND_MESSAGE);
		}

		return reducedBond;
	    default:
		throw createException(REDUCED_BOND_UNEXPECTED_BOND, UNEXPECTED_BOND_MESSAGE);
	    }
	} else if (isSourceIdentifierBond(bond)) {

	    ResourcePropertyBond sourceBond = (ResourcePropertyBond) bond;

	    if (sourceBond.getPropertyValue().equals(sourceIdentifier)) {
		return null;
	    } else {
		throw createException(REDUCED_BOND_SOURCE_NOT_FOUND, SOURCE_NOT_FOUND_MESSAGE);
	    }
	} else {
	    throw createException(REDUCED_BOND_SOURCE_NOT_FOUND, SOURCE_NOT_FOUND_MESSAGE);
	}
    }

    private GSException createException(String errorId, String errorDescription) {

	return GSException.createException(//
		BrokeringStrategyResolver.class, //
		errorDescription, //
		null, //
		ErrorInfo.ERRORTYPE_INTERNAL, //
		ErrorInfo.SEVERITY_ERROR, //
		errorId);
    }

    private boolean isSourceIdentifierBond(Bond bond) {

	return BondFactory.isResourcePropertyBond(bond, ResourceProperty.SOURCE_ID);
    }
}
