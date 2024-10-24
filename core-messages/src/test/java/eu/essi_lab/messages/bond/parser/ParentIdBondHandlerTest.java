package eu.essi_lab.messages.bond.parser;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import eu.essi_lab.messages.bond.BondFactory;
import eu.essi_lab.messages.bond.BondOperator;
import eu.essi_lab.messages.bond.SimpleValueBond;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.MetadataElement;

/**
 * @author ilsanto
 */
public class ParentIdBondHandlerTest {

    @Test
    public void singleParent() {

	String parentid1 = "parentid1";

	SimpleValueBond parentBond = BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.PARENT_IDENTIFIER, parentid1);

	DiscoveryBondParser parser = new DiscoveryBondParser(parentBond);

	ParentIdBondHandler parentBondHandler = new ParentIdBondHandler();
	parser.parse(parentBondHandler);

	Assert.assertTrue(parentBondHandler.isParentIdFound());

	Assert.assertEquals(parentid1, parentBondHandler.getParentValue());

    }

    /**
     * This test is ignored because multiparents is not supported at the moment
     *
     *
     * @throws GSException
     */
    @Test
    @Ignore
    public void multiParent() {

	String parentid1 = "parentid1";
	String parentid2 = "parentid2";

	SimpleValueBond parentBond = BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.PARENT_IDENTIFIER, parentid1);

	SimpleValueBond parentBond2 = BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.PARENT_IDENTIFIER, parentid2);

	DiscoveryBondParser parser = new DiscoveryBondParser(BondFactory.createOrBond(parentBond, parentBond2));

	ParentIdBondHandler parentBondHandler = new ParentIdBondHandler();

	parser.parse(parentBondHandler);

	Assert.assertTrue(parentBondHandler.isParentIdFound());

	Assert.assertEquals(parentid1, parentBondHandler.getParentValue());

    }

}