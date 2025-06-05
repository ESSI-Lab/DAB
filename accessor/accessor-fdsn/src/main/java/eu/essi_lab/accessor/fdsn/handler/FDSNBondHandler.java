package eu.essi_lab.accessor.fdsn.handler;

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

import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.BondOperator;
import eu.essi_lab.messages.bond.LogicalBond;
import eu.essi_lab.messages.bond.LogicalBond.LogicalOperator;
import eu.essi_lab.messages.bond.QueryableBond;
import eu.essi_lab.messages.bond.ResourcePropertyBond;
import eu.essi_lab.messages.bond.RuntimeInfoElementBond;
import eu.essi_lab.messages.bond.SimpleValueBond;
import eu.essi_lab.messages.bond.SpatialBond;
import eu.essi_lab.messages.bond.SpatialExtent;
import eu.essi_lab.messages.bond.ViewBond;
import eu.essi_lab.messages.bond.parser.DiscoveryBondHandler;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.model.resource.ResourceProperty;

/**
 * @author Fabrizio
 */
public class FDSNBondHandler implements DiscoveryBondHandler {

    private String queryString;
    private boolean bboxFound;
    private boolean unsupported;
    private boolean ignoreComplexQuery;
    private String eventOrder;

    public FDSNBondHandler() {
	queryString = "";
	setIgnoreComplexQuery(true);
    }

    /**
     * @param queryString
     */
    public FDSNBondHandler(String queryString) {

	this.queryString = queryString;
	setIgnoreComplexQuery(true);
    }

    /**
     * @param eventOrder
     */
    public void setEventOrder(String eventOrder) {

	this.eventOrder = eventOrder;
    }

    /**
     * @param ignoreComplexQuery
     */
    public void setIgnoreComplexQuery(boolean ignoreComplexQuery) {

	this.ignoreComplexQuery = ignoreComplexQuery;
    }

    /**
     * Returns the parsed query string or <code>null</code> if the handled bond has one or more not supported parameters
     * 
     * @param page
     * @return
     */
    public String getQueryString(Page page) {

	if (unsupported && ignoreComplexQuery) {
	    return null;
	}

	if (queryString.endsWith("&")) {
	    queryString = queryString.substring(0, queryString.length() - 1);
	}

	queryString += page == null ? "" : "&offset=" + page.getStart() + "&limit=" + page.getSize();

	if (eventOrder != null) {

	    queryString += "&orderby=" + eventOrder;
	}

	return queryString;
    }

    @Override
    public void spatialBond(SpatialBond b) {

	SpatialExtent bbox = (SpatialExtent) b.getPropertyValue();

	if (!bboxFound) {
	    queryString += "minlon=" + bbox.getWest() + "&minlat=" + bbox.getSouth() + "&maxlon=" + bbox.getEast() + "&maxlat="
		    + bbox.getNorth() + "&";

	}
	bboxFound = true;
    }

    @Override
    public void simpleValueBond(SimpleValueBond bond) {

	MetadataElement queryable = bond.getProperty();
	// System.out.println("Queryable: " + queryable);

	String value = bond.getPropertyValue();
	// System.out.println("Value: " + value);

	switch (queryable) {
	case IDENTIFIER:
	    queryString += "eventid=" + value + "&";
	    break;
	case TEMP_EXTENT_BEGIN:
	    queryString += "starttime=" + value + "&";
	    break;
	case TEMP_EXTENT_END:
	    queryString += "endtime=" + value + "&";
	    break;
	case QML_MAGNITUDE_VALUE:

	    BondOperator operatorMagnitude = bond.getOperator();
	    if (operatorMagnitude.equals(BondOperator.GREATER) || operatorMagnitude.equals(BondOperator.GREATER_OR_EQUAL)) {
		queryString += "minmag=" + value + "&";
	    } else if (operatorMagnitude.equals(BondOperator.LESS) || operatorMagnitude.equals(BondOperator.LESS_OR_EQUAL)) {
		queryString += "maxmag=" + value + "&";
	    }
	    break;
	case QML_DEPTH_VALUE:
	    BondOperator operatorDepth = bond.getOperator();
	    if (operatorDepth.equals(BondOperator.GREATER) || operatorDepth.equals(BondOperator.GREATER_OR_EQUAL)) {
		queryString += "mindepth=" + value + "&";
	    } else if (operatorDepth.equals(BondOperator.LESS) || operatorDepth.equals(BondOperator.LESS_OR_EQUAL)) {
		queryString += "maxdepth=" + value + "&";
	    }
	    break;
	case QML_MAGNITUDE_TYPE:
	    queryString += "magtype=" + value + "&";
	    break;
	default:
	    unsupported = true;
	    break;
	}
    }

    @Override
    public void startLogicalBond(LogicalBond b) {

	if (b.getLogicalOperator() != LogicalOperator.AND) {
	    unsupported = true;
	}
    }

    @SuppressWarnings("incomplete-switch")
    @Override
    public void resourcePropertyBond(ResourcePropertyBond bond) {

	if (bond.getProperty() == ResourceProperty.RESOURCE_TIME_STAMP) {

	    switch (bond.getOperator()) {
	    case LESS:
	    case LESS_OR_EQUAL:
		queryString += "endtime=" + bond.getPropertyValue() + "&";
		return;
	    case GREATER:
	    case GREATER_OR_EQUAL:
		queryString += "starttime=" + bond.getPropertyValue() + "&";
		return;
	    }
	}

	unsupported = true;
    }

    @Override
    public void endLogicalBond(LogicalBond b) {
    }

    @Override
    public void separator() {
    }

    @Override
    public void customBond(QueryableBond<String> bond) {
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
