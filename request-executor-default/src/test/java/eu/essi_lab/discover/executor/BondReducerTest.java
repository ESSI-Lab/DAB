package eu.essi_lab.discover.executor;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.BondFactory;
import eu.essi_lab.messages.bond.BondOperator;
import eu.essi_lab.messages.bond.LogicalBond;
import eu.essi_lab.messages.bond.ResourcePropertyBond;
import eu.essi_lab.messages.bond.SimpleValueBond;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.model.resource.ResourceProperty;
import eu.essi_lab.request.executor.discover.BondReducer;
import eu.essi_lab.request.executor.discover.BrokeringStrategyResolver;
import eu.essi_lab.request.executor.discover.Distributor;
import eu.essi_lab.request.executor.discover.submitter.DistributedQueryExecutor;

/**
 * These tests the bond reduction made by the {@link Distributor} delegating to the {@link BondReducer} when submitting
 * a query to a {@link
 * DistributedQueryExecutor}
 *
 * @author boldrini
 */
public class BondReducerTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    Bond property1Bond;
    Bond source1Bond;
    Bond source2Bond;

    LogicalBond s1p1Bond;
    LogicalBond s2p1Bond;
    LogicalBond orBond;
    SimpleValueBond parentBond;
    private BondReducer reducer;

    @Before
    public void init() {
	this.reducer = new BondReducer();
	property1Bond = BondFactory.createSimpleValueBond(BondOperator.LIKE, MetadataElement.TITLE, "precipitation");
	source1Bond = BondFactory.createSourceIdentifierBond("source1");
	source2Bond = BondFactory.createSourceIdentifierBond("source2");
	s1p1Bond = BondFactory.createAndBond(property1Bond, source1Bond);
	s2p1Bond = BondFactory.createAndBond(property1Bond, source2Bond);
	orBond = BondFactory.createOrBond(s1p1Bond, s2p1Bond);

	parentBond = BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.PARENT_IDENTIFIER, "testParentId");

    }

//    @Test
//    public void testSecondLevelFromDifferentMixed() throws GSException {
//
//	GSSource source = Mockito.mock(GSSource.class);
//
//	String sourceid = "sourceid";
//
//	Mockito.doReturn(sourceid).when(source).getUniqueIdentifier();
//
//	GSSource source2 = Mockito.mock(GSSource.class);
//
//	String sourceid2 = "sourceid2";
//
//	Mockito.doReturn(sourceid2).when(source2).getUniqueIdentifier();
//
//	DiscoveryMessage message = Mockito.mock(DiscoveryMessage.class);
//
//	Mockito.doReturn(parentBond).when(message).getNormalizedBond();
//
//	ResultSet<GSResource> results = Mockito.mock(ResultSet.class);
//
//	List<GSResource> list = new ArrayList<>();
//
//	GSResource r = Mockito.mock(GSResource.class);
//
//	Mockito.doReturn(source2).when(r).getSource();
//	list.add(r);
//
//	Mockito.doReturn(list).when(results).getResultsList();
//
//	Boolean secondlevel = new BrokeringStrategyResolver().mixedSourceSecondLevelBond(source, message);
//
//	Assert.assertEquals(false, secondlevel);
//    }

    @Test
    public void testSecondLevelFromNonSecondLevelQuery() throws GSException {

	GSSource source = Mockito.mock(GSSource.class);

	String sourceid = "sourceid";

	Mockito.doReturn(sourceid).when(source).getUniqueIdentifier();

	DiscoveryMessage message = Mockito.mock(DiscoveryMessage.class);

	ResourcePropertyBond bond = Mockito.mock(ResourcePropertyBond.class);
	ResourceProperty property = ResourceProperty.SOURCE_ID;

	Mockito.doReturn(property).when(bond).getProperty();

	Mockito.doReturn(sourceid).when(bond).getPropertyValue();

	Mockito.doReturn(bond).when(message).getNormalizedBond();

	Boolean secondlevel = new BrokeringStrategyResolver().isMixedSecondLevel(source, message);

	Assert.assertEquals(false, secondlevel);

    }

//    @Test
//    public void testSecondLevel() throws GSException {
//
//	GSSource source = Mockito.mock(GSSource.class);
//
//	String sourceid = "sourceid";
//
//	Mockito.doReturn(sourceid).when(source).getUniqueIdentifier();
//
//	DiscoveryMessage message = Mockito.mock(DiscoveryMessage.class);
//
//	Mockito.doReturn(parentBond).when(message).getNormalizedBond();
//
//	ResultSet<GSResource> results = Mockito.mock(ResultSet.class);
//
//	List<GSResource> list = new ArrayList<>();
//
//	GSResource r = Mockito.mock(GSResource.class);
//
//	Mockito.doReturn(source).when(r).getSource();
//	list.add(r);
//
//	Mockito.doReturn(list).when(results).getResultsList();
//
//	Boolean secondlevel = new BrokeringStrategyResolver().mixedSourceSecondLevelBond(source, message);
//
//	Assert.assertEquals(true, secondlevel);
//
//    }

    /**
     * Tests bond reduction:
     * <p>
     * NOT(P1), null -> Exception
     *
     * @throws GSException
     */
    @Test
    public void testUnexpectedBond() throws GSException {
	expectedException.expect(GSException.class);
	reducer.getReducedBond(BondFactory.createNotBond(property1Bond), null);
    }

    /**
     * Tests bond reduction:
     * <p>
     * P1, null -> Exception
     *
     * @throws GSException
     */
    @Test
    public void testBondReduction0() throws GSException {
	expectedException.expect(GSException.class);
	reducer.getReducedBond(property1Bond, null);
    }

    /**
     * Tests bond reduction:
     * <p>
     * P1, S1 -> Exception
     *
     * @throws GSException
     */
    @Test
    public void testBondReduction1() throws GSException {
	expectedException.expect(GSException.class);
	reducer.getReducedBond(property1Bond, "source1");
    }

    /**
     * Tests bond reduction:
     * <p>
     * S1, S1 -> null
     *
     * @throws GSException
     */
    @Test
    public void testBondReduction2() throws GSException {
	Bond reducedBond = reducer.getReducedBond(source1Bond, "source1");
	Assert.assertEquals(null, reducedBond);
    }

    /**
     * Tests bond reduction:
     * <p>
     * P1 AND S1, S1 -> P1
     *
     * @throws GSException
     */
    @Test
    public void testBondReduction3() throws GSException {
	Bond reducedBond = reducer.getReducedBond(s1p1Bond, "source1");
	Assert.assertEquals(property1Bond, reducedBond);
    }

    /**
     * Tests bond reduction:
     * <p>
     * P1 AND S2, S1 -> Exception
     *
     * @throws GSException
     */
    @Test
    public void testBondReduction4() throws GSException {
	expectedException.expect(GSException.class);
	reducer.getReducedBond(s2p1Bond, "source1");
    }

    /**
     * Tests bond reduction:
     * <p>
     * P1 AND S1 OR P1 AND S2, S1 -> P1
     *
     * @throws GSException
     */
    @Test
    public void testBondReduction5() throws GSException {
	Bond reducedBond = reducer.getReducedBond(orBond, "source1");
	Assert.assertEquals(property1Bond, reducedBond);
    }

}
