package eu.essi_lab.profiler.esri.feature.query.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Test;

import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.BondOperator;
import eu.essi_lab.messages.bond.LogicalBond;
import eu.essi_lab.messages.bond.LogicalBond.LogicalOperator;
import eu.essi_lab.messages.bond.SimpleValueBond;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.profiler.esri.feature.FeatureLayer1StationsArctic;
import eu.essi_lab.profiler.esri.feature.Field;

public class ESRIParserTest {

    @Test
    public void test() {

	FeatureLayer1StationsArctic layer = new FeatureLayer1StationsArctic();
	List<Field> fields = layer.getFields();

	ESRIParser parser = new ESRIParser();
	assertEquals("(((UPPER(country)='NORWAY')AND(wmo_region=6)))",
		parser.removeSpaces("(((UPPER(country) = 'NORWAY') AND (wmo_region = 6)))"));
	assertEquals("(((UPPER(country)='Great Britain')AND(wmo_region=6)))",
		parser.removeSpaces("(((UPPER(country) = 'Great Britain') AND (wmo_region = 6)))"));
	assertEquals("(((UPPER(country)='Great Britain')AND(wmo_region='United Kingdom')))",
		parser.removeSpaces("(((UPPER(country) = 'Great Britain') AND (wmo_region = 'United Kingdom')))"));
	assertEquals("1=1", parser.removeSpaces(" 1=1"));
	assertEquals("1=1", parser.removeSpaces("1=1 "));
	assertEquals("1=1", parser.removeSpaces("1 =1"));
	assertEquals("1=1", parser.removeSpaces("1= 1"));
	assertEquals("1=1", parser.removeSpaces("1 = 1"));
	assertEquals("1=1", parser.removeSpaces(" 1 = 1 "));
	assertEquals("1=1", parser.removeSpaces("1=1"));
	assertEquals("", parser.removeSpaces(""));

	assertEquals(null, parser.parse("1=1", fields));

	Bond bond;

	bond = parser.parse("(UPPER(country) = 'NORWAY')", fields);
	if (bond instanceof SimpleValueBond) {
	    SimpleValueBond svb = (SimpleValueBond) bond;
	    assertEquals(BondOperator.TEXT_SEARCH, svb.getOperator());
	    assertEquals(MetadataElement.COUNTRY, svb.getProperty());
	    assertEquals("NORWAY", svb.getPropertyValue());
	} else {
	    fail();
	}

	bond = parser.parse("(((UPPER(country)='NORWAY')AND(wmo_region=6)))", fields);
	if (bond instanceof LogicalBond) {
	    LogicalBond lb = (LogicalBond) bond;
	    LogicalOperator operator = lb.getLogicalOperator();
	    if (!operator.equals(LogicalOperator.AND)) {
		fail();
	    }
	    List<Bond> operands = lb.getOperands();
	    bond = operands.get(0);
	    if (bond instanceof SimpleValueBond) {
		SimpleValueBond svb = (SimpleValueBond) bond;
		assertEquals(BondOperator.TEXT_SEARCH, svb.getOperator());
		assertEquals(MetadataElement.COUNTRY, svb.getProperty());
		assertEquals("NORWAY", svb.getPropertyValue());
	    } else {
		fail();
	    }
	    bond = operands.get(1);
	    if (bond instanceof SimpleValueBond) {
		SimpleValueBond svb = (SimpleValueBond) bond;
		assertEquals(BondOperator.EQUAL, svb.getOperator());
		assertEquals(MetadataElement.WMO_REGION, svb.getProperty());
		assertEquals("6", svb.getPropertyValue());
	    } else {
		fail();
	    }
	} else {
	    fail();
	}

    }

}
