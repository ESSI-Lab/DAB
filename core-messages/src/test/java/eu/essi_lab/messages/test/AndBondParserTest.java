package eu.essi_lab.messages.test;

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
import eu.essi_lab.messages.bond.parser.AndBondParser;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.MetadataElement;

/**
 * Tests the AndParser utility parsing a series of bonds
 * 
 * @author boldrini
 */
public class AndBondParserTest {

    private AndBondParser parser;
    Bond property1Bond;
    Bond property2Bond;
    Bond source1Bond;
    Bond source2Bond;

    LogicalBond s1p1Bond;
    LogicalBond s2p1Bond;
    LogicalBond orBond;
    LogicalBond notBond;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void init() {
	this.parser = new AndBondParser();
	property1Bond = BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.TITLE, "precipitation");
	property2Bond = BondFactory.createSimpleValueBond(BondOperator.TEXT_SEARCH, MetadataElement.TITLE, "temperature");
	source1Bond = BondFactory.createSourceIdentifierBond("source1");
	source2Bond = BondFactory.createSourceIdentifierBond("source2");
	s1p1Bond = BondFactory.createAndBond(property1Bond, source1Bond);
	s2p1Bond = BondFactory.createAndBond(property1Bond, source2Bond);
	orBond = BondFactory.createOrBond(property1Bond, source1Bond);
	notBond = BondFactory.createNotBond(property1Bond);

    }

    /**
     * Tests null -> null
     * 
     * @throws GSException
     */
    @Test
    public void testBondParser0() throws GSException {
	Set<Bond> result = parser.parseBond(null);
	Set<Bond> expected = new HashSet<>();
	expected.add(null);
	Assert.assertEquals(expected, result);
    }

    /**
     * Tests P1 -> P1
     * 
     * @throws GSException
     */
    @Test
    public void testBondParser1() throws GSException {
	Set<Bond> result = parser.parseBond(property1Bond);
	Set<Bond> expected = new HashSet<>();
	expected.add(property1Bond);
	Assert.assertEquals(expected, result);
    }

    /**
     * Tests P1 AND S1 -> P1, S1
     * 
     * @throws GSException
     */
    @Test
    public void testBondParser2() throws GSException {
	Set<Bond> result = parser.parseBond(s1p1Bond);
	Set<Bond> expected = new HashSet<>();
	expected.add(property1Bond);
	expected.add(source1Bond);
	Assert.assertEquals(expected, result);
    }

    /**
     * Tests P1 OR S1 -> Exception
     * 
     * @throws GSException
     */
    @Test
    public void testBondParser3() throws GSException {
	expectedException.expect(GSException.class);
	parser.parseBond(this.orBond);
    }

    /**
     * Tests NOT(P1) -> Exception
     * 
     * @throws GSException
     */
    @Test
    public void testBondParser4() throws GSException {
	expectedException.expect(GSException.class);
	parser.parseBond(notBond);

    }

    /**
     * Tests P1 AND (S1 OR (P2 AND S2) ) -> Exception
     * 
     * @throws GSException
     */
    @Test
    public void testBondParser5() throws GSException {
	expectedException.expect(GSException.class);
	Bond p2Ands2 = BondFactory.createAndBond(property2Bond, source2Bond);
	Bond s1Orp2Ands2 = BondFactory.createOrBond(source1Bond,p2Ands2 );
	LogicalBond targetBond = BondFactory.createAndBond(property1Bond,s1Orp2Ands2 );
	Set<Bond> result = parser.parseBond(targetBond);
	Set<Bond> expected = new HashSet<>();
	expected.add(property1Bond);
	expected.add(source1Bond);
	Assert.assertEquals(expected, result);
    }

}
