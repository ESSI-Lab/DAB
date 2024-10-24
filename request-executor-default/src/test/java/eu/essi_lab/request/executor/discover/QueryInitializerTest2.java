package eu.essi_lab.request.executor.discover;

import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import eu.essi_lab.authorization.converter.IRequestAuthorizationConverter;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.BondFactory;
import eu.essi_lab.messages.bond.BondOperator;
import eu.essi_lab.messages.bond.LogicalBond;
import eu.essi_lab.messages.bond.ResourcePropertyBond;
import eu.essi_lab.messages.bond.SimpleValueBond;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.MetadataElement;

public class QueryInitializerTest2 {
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private QueryInitializer queryInitializer;
    private DiscoveryMessage message;
    private IRequestAuthorizationConverter requestAuthorizationConverter;

    @Before
    public void init() {
	this.queryInitializer = new QueryInitializer();
	this.requestAuthorizationConverter = Mockito.mock(IRequestAuthorizationConverter.class);

	this.queryInitializer.setRequestAuthorizationConverter(requestAuthorizationConverter);
	message = new DiscoveryMessage();

    }

    @Test
    public void test() throws GSException {

	ResourcePropertyBond s0Bond = BondFactory.createSourceIdentifierBond("S-0");

	ArrayList<Bond> sourceBonds = new ArrayList<Bond>();
	for (int i = 0; i < 15; i++) {
	    sourceBonds.add(BondFactory.createSourceIdentifierBond("S-" + i));
	}
	LogicalBond sourceOrBonds = BondFactory.createOrBond(sourceBonds);

	SimpleValueBond svb = BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.PARENT_IDENTIFIER, "ID23");

	Bond authorized = BondFactory.createAndBond(s0Bond,
		BondFactory.createAndBond(sourceOrBonds, BondFactory.createAndBond(s0Bond, svb)));

	Mockito.when(requestAuthorizationConverter.generateAuthorizedBond(ArgumentMatchers.any(DiscoveryMessage.class)))
		.thenReturn(authorized);
	queryInitializer.initializeQuery(message);

	Assert.assertEquals(BondFactory.createAndBond(s0Bond, svb), message.getNormalizedBond());
    }

}
