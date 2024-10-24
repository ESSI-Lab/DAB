package eu.essi_lab.request.executor.discover;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.bond.BondFactory;
import eu.essi_lab.messages.bond.BondOperator;
import eu.essi_lab.messages.bond.SimpleValueBond;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.request.executor.IDiscoveryExecutor;

/**
 * @author ilsanto
 */
@Ignore
public class BondReducerTest2 {

    @Test
    public void testSecondLevelQuerySingleParent() throws GSException {

	String parentid1 = "testParentId";

	IDiscoveryExecutor executor = Mockito.mock(IDiscoveryExecutor.class);

	BondReducer reducer = new BondReducer();

	GSSource source = Mockito.mock(GSSource.class);

	String sourceid = "sourceid";

	Mockito.doReturn(sourceid).when(source).getUniqueIdentifier();

	DiscoveryMessage message = Mockito.mock(DiscoveryMessage.class);

	SimpleValueBond parentBond = BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.PARENT_IDENTIFIER, parentid1);

	Mockito.doReturn(parentBond).when(message).getNormalizedBond();

	ResultSet<GSResource> results = Mockito.mock(ResultSet.class);

	List<GSResource> list = new ArrayList<>();

	GSResource r = Mockito.mock(GSResource.class);

	Mockito.doReturn(source).when(r).getSource();
	list.add(r);

	Mockito.doReturn(list).when(results).getResultsList();

	Mockito.doReturn(results).when(executor).retrieve(Mockito.any());

	Assert.assertTrue(new BrokeringStrategyResolver().isMixedSecondLevel(source, message));

	Mockito.verify(message, Mockito.times(1)).addParentGSResource(Mockito.any());

    }

    /**
     * This test is ignored because multiparents is not supported at the moment
     *
     * @throws GSException
     */
    @Test
    @Ignore
    public void testSecondLevelQueryMultiParents() throws GSException {

	String parentid1 = "testParentId";
	String parentid2 = "testParentId2";

	IDiscoveryExecutor executor = Mockito.mock(IDiscoveryExecutor.class);

	BondReducer reducer = new BondReducer();

	GSSource source = Mockito.mock(GSSource.class);

	String sourceid = "sourceid";

	Mockito.doReturn(sourceid).when(source).getUniqueIdentifier();

	DiscoveryMessage message = Mockito.mock(DiscoveryMessage.class);

	SimpleValueBond parentBond1 = BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.PARENT_IDENTIFIER, parentid1);

	SimpleValueBond parentBond2 = BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.PARENT_IDENTIFIER, parentid2);

	Mockito.doReturn(BondFactory.createOrBond(parentBond1, parentBond2)).when(message).getNormalizedBond();

	ResultSet<GSResource> results = Mockito.mock(ResultSet.class);

	List<GSResource> list = new ArrayList<>();

	GSResource r = Mockito.mock(GSResource.class);
	GSResource r2 = Mockito.mock(GSResource.class);

	Mockito.doReturn(source).when(r).getSource();
	Mockito.doReturn(source).when(r2).getSource();

	list.add(r);

	list.add(r2);

	Mockito.doReturn(list).when(results).getResultsList();

	Mockito.doReturn(results).when(executor).retrieve(Mockito.any());

	Assert.assertTrue(new BrokeringStrategyResolver().isMixedSecondLevel(source, message));

	Mockito.verify(message, Mockito.times(2)).addParentGSResource(Mockito.any());

    }

    @Test
    public void testSecondLevelQuerySingleParentDifferentSource() throws GSException {

	String parentid1 = "testParentId";

	IDiscoveryExecutor executor = Mockito.mock(IDiscoveryExecutor.class);

	BondReducer reducer = new BondReducer();

	GSSource source = Mockito.mock(GSSource.class);

	String sourceid = "sourceid";

	Mockito.doReturn(sourceid).when(source).getUniqueIdentifier();

	GSSource source2 = Mockito.mock(GSSource.class);

	String sourceid2 = "sourceid2";

	Mockito.doReturn(sourceid2).when(source2).getUniqueIdentifier();

	DiscoveryMessage message = Mockito.mock(DiscoveryMessage.class);

	SimpleValueBond parentBond = BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.PARENT_IDENTIFIER, parentid1);

	Mockito.doReturn(parentBond).when(message).getNormalizedBond();

	ResultSet<GSResource> results = Mockito.mock(ResultSet.class);

	List<GSResource> list = new ArrayList<>();

	GSResource r = Mockito.mock(GSResource.class);

	Mockito.doReturn(source2).when(r).getSource();
	list.add(r);

	Mockito.doReturn(list).when(results).getResultsList();

	Mockito.doReturn(results).when(executor).retrieve(Mockito.any());

	Assert.assertFalse(new BrokeringStrategyResolver().isMixedSecondLevel(source, message));

	Mockito.verify(message, Mockito.times(0)).addParentGSResource(Mockito.any());

    }

}