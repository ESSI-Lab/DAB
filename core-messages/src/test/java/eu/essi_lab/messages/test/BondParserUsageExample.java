package eu.essi_lab.messages.test;

import java.util.ArrayList;

import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.BondFactory;
import eu.essi_lab.messages.bond.BondOperator;
import eu.essi_lab.messages.bond.LogicalBond;
import eu.essi_lab.messages.bond.LogicalBond.LogicalOperator;
import eu.essi_lab.messages.bond.QueryableBond;
import eu.essi_lab.messages.bond.ResourcePropertyBond;
import eu.essi_lab.messages.bond.RuntimeInfoElementBond;
import eu.essi_lab.messages.bond.SimpleValueBond;
import eu.essi_lab.messages.bond.SpatialBond;
import eu.essi_lab.messages.bond.ViewBond;
import eu.essi_lab.messages.bond.parser.DiscoveryBondHandler;
import eu.essi_lab.messages.bond.parser.DiscoveryBondParser;
import eu.essi_lab.messages.bond.spatial.SpatialExtent;
import eu.essi_lab.model.resource.MetadataElement;

public class BondParserUsageExample {

    public static void main(String[] args) {

	// --------------------------------
	// creates the first and bond list
	//
	ArrayList<Bond> firstBondList = new ArrayList<>();

	SpatialExtent spatialExtent = new SpatialExtent(0, 0, 0, 0);
	SpatialBond spatialBond = BondFactory.createSpatialEntityBond(BondOperator.CONTAINS, spatialExtent);
	firstBondList.add(spatialBond);

	SimpleValueBond searchTermsBond = BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.TITLE, "pippo");
	firstBondList.add(searchTermsBond);

	String startTime = "1900";
	SimpleValueBond startTimeBond = BondFactory.createSimpleValueBond(BondOperator.GREATER_OR_EQUAL, MetadataElement.TEMP_EXTENT_BEGIN,
		startTime);
	firstBondList.add(startTimeBond);

	String endTime = "2000";
	SimpleValueBond endTimeBond = BondFactory.createSimpleValueBond(BondOperator.GREATER_OR_EQUAL, MetadataElement.TEMP_EXTENT_END,
		endTime);
	firstBondList.add(endTimeBond);

	LogicalBond firstAndBond = BondFactory.createAndBond(firstBondList.toArray(new Bond[] {}));

	// --------------------------------
	// creates the second and bond list
	//
	ArrayList<Bond> secondBondList = new ArrayList<>();

	spatialExtent = new SpatialExtent(1, 1, 1, 1);
	spatialBond = BondFactory.createSpatialEntityBond(BondOperator.CONTAINS, spatialExtent);
	secondBondList.add(spatialBond);

	searchTermsBond = BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.TITLE, "ciccio");
	secondBondList.add(searchTermsBond);

	startTime = "2010";
	startTimeBond = BondFactory.createSimpleValueBond(BondOperator.GREATER_OR_EQUAL, MetadataElement.TEMP_EXTENT_BEGIN, startTime);
	secondBondList.add(startTimeBond);

	endTime = "2020";
	endTimeBond = BondFactory.createSimpleValueBond(BondOperator.GREATER_OR_EQUAL, MetadataElement.TEMP_EXTENT_END, endTime);
	secondBondList.add(endTimeBond);

	LogicalBond secondAndBond = BondFactory.createAndBond(secondBondList.toArray(new Bond[] {}));

	// ---------------------------------------
	// creates a not bond
	//
	LogicalBond notBond = BondFactory.createNotBond(secondAndBond);

	// ---------------------------------------
	// inserts the two and bonds in an or bond
	//
	ArrayList<Bond> logicalBondsList = new ArrayList<>();
	logicalBondsList.add(firstAndBond);
	logicalBondsList.add(secondAndBond);
	logicalBondsList.add(notBond);

	LogicalBond orBond = BondFactory.createOrBond(logicalBondsList.toArray(new Bond[] {}));

	// ------------------------------------------
	// creates a bond handler
	//
	DiscoveryBondHandler bondHandler = new DiscoveryBondHandler() {

	    int indentation;

	    @Override
	    public void separator() {

		// System.out.println(createIndentation()+"AND BOND");
	    }

	    @Override
	    public void customBond(QueryableBond<String> B) {

		System.out.println(createIndentation() + "CUSTOM BOND: " + B);

	    }

	    @Override
	    public void simpleValueBond(SimpleValueBond b) {

		System.out.println(createIndentation() + "SIMPLE ELEMENT BOND: " + b);
	    }

	    @Override
	    public void resourcePropertyBond(ResourcePropertyBond b) {

		System.out.println(createIndentation() + "RESOURCE PROPERTY BOND: " + b);

	    }

	    @Override
	    public void spatialBond(SpatialBond b) {

		SpatialExtent bbox = (SpatialExtent) b.getPropertyValue();
		System.out.println(createIndentation() + "BBOX: " + bbox);
	    }

	    @Override
	    public void startLogicalBond(LogicalBond b) {

		LogicalOperator logicalOperator = b.getLogicalOperator();
		System.out.println(createIndentation() + "START OF: " + logicalOperator);

		indentation++;
	    }

	    @Override
	    public void endLogicalBond(LogicalBond b) {

		indentation--;

		LogicalOperator logicalOperator = b.getLogicalOperator();
		System.out.println(createIndentation() + "END OF: " + logicalOperator);
	    }

	    String createIndentation() {
		String out = "";
		for (int i = 0; i < indentation; i++) {
		    out += ">";
		}
		return out + " ";
	    }

	    @Override
	    public void viewBond(ViewBond bond) {
		System.out.println(createIndentation() + "VIEW BOND: " + bond);

	    }

	    @Override
	    public void nonLogicalBond(Bond bond) {
	
	    }

	    @Override
	    public void runtimeInfoElementBond(RuntimeInfoElementBond bond) {
		// TODO Auto-generated method stub
		
	    }

	};

	// ------------------------------------------
	// creates the bond handler
	//

	DiscoveryBondParser bondParser = new DiscoveryBondParser(orBond);
	bondParser.parse(bondHandler);

    }
}
