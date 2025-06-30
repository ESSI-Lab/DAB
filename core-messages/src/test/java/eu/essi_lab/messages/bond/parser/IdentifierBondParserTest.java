package eu.essi_lab.messages.bond.parser;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.BondFactory;
import eu.essi_lab.messages.bond.BondOperator;
import eu.essi_lab.messages.bond.CustomBondFactory;
import eu.essi_lab.messages.bond.spatial.SpatialExtent;
import eu.essi_lab.model.Queryable;
import eu.essi_lab.model.resource.MetadataElement;

public class IdentifierBondParserTest {

    /**
     * test with bond == null
     */
    @Test
    public void test1() {
	Bond bond = null;
	IdentifierBondHandler parser = new IdentifierBondHandler(bond);
	Assert.assertFalse(parser.isCanonicalQueryByIdentifiers());
	Assert.assertTrue(parser.getIdentifiers().isEmpty());
    }

    /**
     * test with bond == ID=id1
     */
    @Test
    public void test2() {
	Bond bond = BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.IDENTIFIER, "id1");
	IdentifierBondHandler parser = new IdentifierBondHandler(bond);
	Assert.assertTrue(parser.isCanonicalQueryByIdentifiers());
	Assert.assertEquals(1, parser.getIdentifiers().size());
	Assert.assertEquals("id1", parser.getIdentifiers().get(0));
    }

    /**
     * test with bond == spatialBond
     */
    @Test
    public void test3() {
	Bond bond = BondFactory.createSpatialEntityBond(BondOperator.BBOX, new SpatialExtent(-90, -180, 90, 180));
	IdentifierBondHandler parser = new IdentifierBondHandler(bond);
	Assert.assertFalse(parser.isCanonicalQueryByIdentifiers());
	Assert.assertTrue(parser.getIdentifiers().isEmpty());
    }

    /**
     * test with bond == resourcePropertyBond
     */
    @Test
    public void test4() {
	Bond bond = BondFactory.createIsISOCompliantBond(true);
	IdentifierBondHandler parser = new IdentifierBondHandler(bond);
	Assert.assertFalse(parser.isCanonicalQueryByIdentifiers());
	Assert.assertTrue(parser.getIdentifiers().isEmpty());
    }

    /**
     * test with bond == customBond
     */
    @Test
    public void test5() {
	Queryable queryable = MetadataElement.ABSTRACT;
	Bond bond = CustomBondFactory.createCustomBond(queryable, BondOperator.EQUAL, "test");
	IdentifierBondHandler parser = new IdentifierBondHandler(bond);
	Assert.assertFalse(parser.isCanonicalQueryByIdentifiers());
	Assert.assertTrue(parser.getIdentifiers().isEmpty());
    }

    /**
     * test with bond == ID=id1 OR ID=id2
     */
    @Test
    public void test6() {
	Bond bond = BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.IDENTIFIER, "id1");
	Bond bond2 = BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.IDENTIFIER, "id2");
	Bond logicalBond = BondFactory.createOrBond(bond, bond2);
	IdentifierBondHandler parser = new IdentifierBondHandler(logicalBond);
	Assert.assertTrue(parser.isCanonicalQueryByIdentifiers());
	Assert.assertEquals(2, parser.getIdentifiers().size());
	Assert.assertTrue(parser.getIdentifiers().contains("id1"));
	Assert.assertTrue(parser.getIdentifiers().contains("id2"));
    }

    /**
     * test with bond == ID=id1 AND ID=id2
     */
    @Test
    public void test7() {
	Bond bond = BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.IDENTIFIER, "id1");
	Bond bond2 = BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.IDENTIFIER, "id2");
	Bond logicalBond = BondFactory.createAndBond(bond, bond2);
	IdentifierBondHandler parser = new IdentifierBondHandler(logicalBond);
	Assert.assertFalse(parser.isCanonicalQueryByIdentifiers());

    }

    /**
     * test with bond == ID=id1 OR ID=id2 OR spatialBond
     */
    @Test
    public void test8() {
	Bond bond = BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.IDENTIFIER, "id1");
	Bond bond2 = BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.IDENTIFIER, "id2");
	Bond bond3 = BondFactory.createSpatialEntityBond(BondOperator.BBOX, new SpatialExtent(-90, -180, 90, 180));
	Bond logicalBond = BondFactory.createOrBond(bond, bond2, bond3);
	IdentifierBondHandler parser = new IdentifierBondHandler(logicalBond);
	Assert.assertFalse(parser.isCanonicalQueryByIdentifiers());

    }

}
