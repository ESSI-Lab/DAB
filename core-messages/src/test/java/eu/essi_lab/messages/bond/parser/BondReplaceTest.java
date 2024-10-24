/**
 * 
 */
package eu.essi_lab.messages.bond.parser;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.BondFactory;
import eu.essi_lab.messages.bond.BondOperator;
import eu.essi_lab.messages.bond.LogicalBond;
import eu.essi_lab.model.resource.MetadataElement;

/**
 * @author Fabrizio
 */
public class BondReplaceTest {

//    @Test
    public void replaceWithValuesTest() {

	LogicalBond orBond = BondFactory.createOrBond(//
		BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.TITLE, "title1"), //
		BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.ABSTRACT, "abstract1"));

	LogicalBond orBond1 = BondFactory.createOrBond(//
		BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.TITLE, "title11"), //
		BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.ABSTRACT, "abstract11"));

	LogicalBond andBond = BondFactory.createAndBond(//
		orBond, //
		BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.TITLE, "title2"), //
		BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.ABSTRACT, "abstract2") //
	);

	LogicalBond andBond2 = BondFactory.createAndBond(//
		andBond, //
		orBond1, //
		BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.TITLE, "title3"), //
		BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.ABSTRACT, "abstract3") //
	);

	System.out.println(andBond2);

	System.out.println("------");

	Bond replace = andBond2.replaceWithValues(//
		Arrays.asList("title1", "abstract1", "title2", "abstract2", "title3", "abstract3", "title11", "abstract11"), //
		Arrays.asList("title10", "abstract10", "title20", "abstract20", "title30", "abstract30", "title111", "abstract111"));

	String replaced = replace.toString();

	String expected = "(((title = title10 OR\n";
	expected += "abstract = abstract10) AND\n";
	expected += "title = title20 AND\n";
	expected += "abstract = abstract20) AND\n";
	expected += "(title = title111 OR\n";
	expected += "abstract = abstract111) AND\n";
	expected += "title = title30 AND\n";
	expected += "abstract = abstract30)";
	
	Assert.assertEquals(expected, replaced);
    }
    
    @Test
    public void replaceWithBondTest() {

	LogicalBond orBond = BondFactory.createOrBond(//
		BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.TITLE, "title1"), //
		BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.ABSTRACT, "abstract1"));

	LogicalBond orBond1 = BondFactory.createOrBond(//
		BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.TITLE, "title2"), //
		BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.ABSTRACT, "abstract2"));

	LogicalBond andBond = BondFactory.createAndBond(//
		orBond, //
		BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.TITLE, "title3"), //
		BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.ABSTRACT, "abstract3") //
	);

	LogicalBond andBond2 = BondFactory.createAndBond(//
		andBond, //
		orBond1, //
		BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.TITLE, "title4"), //
		BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.ABSTRACT, "abstract4") //
	);
	
	// ------------------------
	
	
	LogicalBond orBondReplace1 = BondFactory.createOrBond(//
		BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.TITLE, "title11"), //
		BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.ABSTRACT, "abstract11"));
 
	LogicalBond orBondReplace2 = BondFactory.createOrBond(//
 		BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.TITLE, "title22"), //
		BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.ABSTRACT, "abstract22") //
	);

	 	

	System.out.println(andBond2);

	System.out.println("------");

	Bond replace = andBond2.replaceWithBonds(//
		Arrays.asList("title1", "abstract2"), //
		Arrays.asList(orBondReplace1, orBondReplace2));

	String replaced = replace.toString();
	
	System.out.println(replaced);

	String expected = "((((title = title11 OR\n";
	expected += "abstract = abstract11) OR\n";
	expected += "abstract = abstract1) AND\n";
	expected += "title = title3 AND\n";
	expected += "abstract = abstract3) AND\n";
	expected += "(title = title2 OR\n";
	expected += "(title = title22 OR\n";
	expected += "abstract = abstract22)) AND\n";
	expected += "title = title4 AND\n";
	expected += "abstract = abstract4)";
	
	Assert.assertEquals(expected, replaced);
    }

}
