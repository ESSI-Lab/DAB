package eu.essi_lab.messages.bond.parser;

import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.BondFactory;
import eu.essi_lab.messages.bond.BondOperator;
import eu.essi_lab.messages.bond.LogicalBond;
import eu.essi_lab.model.resource.MetadataElement;

/**
 * Tests the {@link NotBondParser} utility parsing a series of bonds
 * 
 * @author boldrini
 */
public class NotBondParserTest {

    private NotBondParser parser;
    Bond property1Bond;
    Bond property2Bond;
    Bond source1Bond;
    Bond source2Bond;

    LogicalBond s1p1Bond;
    LogicalBond s2p1Bond;
    LogicalBond orBond;
    LogicalBond notBond;
    LogicalBond notBond2;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void init() {
	this.parser = new NotBondParser();
	property1Bond = BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.TITLE, "precipitation");
	property2Bond = BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.TITLE, "temperature");
	source1Bond = BondFactory.createSourceIdentifierBond("source1");
	source2Bond = BondFactory.createSourceIdentifierBond("source2");
	s1p1Bond = BondFactory.createAndBond(property1Bond, source1Bond);
	s2p1Bond = BondFactory.createAndBond(property1Bond, source2Bond);
	orBond = BondFactory.createOrBond(property1Bond, source1Bond);
	notBond = BondFactory.createNotBond(property1Bond);
	notBond2 = BondFactory.createNotBond(property2Bond);

    }

    /**
     * Tests null -> null
     *
     */
    @Test
    public void testBondParser0() {
	Set<Bond> result = parser.parseBond(null);
	Set<Bond> expected = new HashSet<>();
	expected.add(null);
	Assert.assertEquals(expected, result);
    }

    /**
     * Tests P1 -> {}
     *
     */
    @Test
    public void testBondParser1() {
	Set<Bond> result = parser.parseBond(property1Bond);
	Set<Bond> expected = new HashSet<>();
	Assert.assertEquals(expected, result);
    }

    /**
     * Tests P1 AND S1 -> {}
     *
     */
    @Test
    public void testBondParser2() {
	Set<Bond> result = parser.parseBond(s1p1Bond);
	Set<Bond> expected = new HashSet<>();
	Assert.assertEquals(expected, result);
    }

 

    /**
     * Tests NOT(P1) -> NOT(P1)
     *
     */
    @Test
    public void testBondParser4() {
	Set<Bond> result = parser.parseBond(notBond);
	Set<Bond> expected = new HashSet<>();
	expected.add(notBond);
	Assert.assertEquals(expected, result);

    }

    /**
     * Tests P1 AND (S1 OR (P2 AND NOT(P1)) ) -> NOT(P1)
     *
     */
    @Test
    public void testBondParser5() {
	Bond p2Andnotp1 = BondFactory.createAndBond(property2Bond, notBond);
	Bond s1Orp2Andnotp1 = BondFactory.createOrBond(source1Bond,p2Andnotp1 );
	LogicalBond targetBond = BondFactory.createAndBond(property1Bond,s1Orp2Andnotp1 );
	Set<Bond> result = parser.parseBond(targetBond);
	Set<Bond> expected = new HashSet<>();
	expected.add(notBond);
	Assert.assertEquals(expected, result);
    }
    
    /**
     * Tests NOT(P2) AND (S1 OR (P2 AND NOT(P1)) ) -> NOT(P1), NOT(P2)
     *
     */
    @Test
    public void testBondParser6() {
	Bond p2Andnotp1 = BondFactory.createAndBond(property2Bond, notBond);
	Bond s1Orp2Andnotp1 = BondFactory.createOrBond(source1Bond,p2Andnotp1 );
	LogicalBond targetBond = BondFactory.createAndBond(notBond2,s1Orp2Andnotp1 );
	Set<Bond> result = parser.parseBond(targetBond);
	Set<Bond> expected = new HashSet<>();
	expected.add(notBond);
	expected.add(notBond2);
	Assert.assertEquals(expected, result);
    }

}
