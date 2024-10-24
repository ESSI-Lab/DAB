package eu.essi_lab.profiler.opensearch.test;

import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.LogicalBond;
import eu.essi_lab.messages.bond.QueryableBond;
import eu.essi_lab.messages.bond.ResourcePropertyBond;
import eu.essi_lab.messages.bond.RuntimeInfoElementBond;
import eu.essi_lab.messages.bond.SimpleValueBond;
import eu.essi_lab.messages.bond.SpatialBond;
import eu.essi_lab.messages.bond.SpatialExtent;
import eu.essi_lab.messages.bond.ViewBond;
import eu.essi_lab.messages.bond.parser.DiscoveryBondHandler;
import eu.essi_lab.model.resource.MetadataElement;

/**
 * Handles the bonds creating an OS query string
 * 
 * @author Fabrizio
 */
public class OSBondHandler implements DiscoveryBondHandler {

    private String queryString;
    private boolean bboxFound;

    public OSBondHandler() {

	queryString = "";
    }

    public String getQueryString() {

	if (queryString.endsWith("&")) {
	    queryString = queryString.substring(0, queryString.length() - 1);
	}

	return queryString;
    }

    @Override
    public void separator() {

	// System.out.println("AND BOND");
    }

    @Override
    public void simpleValueBond(SimpleValueBond b) {

	// System.out.println("QUERYABLE BOND: " + b);

	// OperatorType operator = b.getOperator();
	// System.out.println("Operator: " + operator);

	MetadataElement queryable = b.getProperty();
	// System.out.println("Queryable: " + queryable);

	String value = b.getPropertyValue();
	// System.out.println("Value: " + value);

	switch (queryable) {
	case TITLE:
	    queryString += "st=" + value + "&";
	    break;
	case TEMP_EXTENT_BEGIN:
	    queryString += "ts=" + value + "&";
	    break;
	case TEMP_EXTENT_END:
	    queryString += "te=" + value + "&";
	    break;
	}
    }

    @Override
    public void spatialBond(SpatialBond b) {

	SpatialExtent bbox = (SpatialExtent) b.getPropertyValue();

	if (!bboxFound) {
	    queryString += "bbox=" + bbox.getWest() + "," + bbox.getSouth() + "," + bbox.getEast() + "," + bbox.getNorth() + "&";

	} else {
	    queryString = queryString.replace("bbox=",
		    "bbox=" + bbox.getWest() + "," + bbox.getSouth() + "," + bbox.getEast() + "," + bbox.getNorth() + "_");
	}

	bboxFound = true;

	// System.out.println("BBOX: " + bbox);
    }

    @Override
    public void startLogicalBond(LogicalBond b) {

	// LogicalOperator logicalOperator = b.getLogicalOperator();
	// System.out.println("START OF: " + logicalOperator);
    }

    @Override
    public void endLogicalBond(LogicalBond b) {

	// LogicalOperator logicalOperator = b.getLogicalOperator();
	// System.out.println("END OF: " + logicalOperator);
    }

    @Override
    public void resourcePropertyBond(ResourcePropertyBond bond) {
    }

    @Override
    public void customBond(QueryableBond<String> bond) {
    }

    @Override
    public void viewBond(ViewBond bond) {
	// this should not happen

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
