package eu.essi_lab.discover.executor;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.ReducedDiscoveryMessage;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.bond.BondFactory;
import eu.essi_lab.messages.bond.LogicalBond;
import eu.essi_lab.messages.count.DiscoveryCountResponse;
import eu.essi_lab.model.exceptions.ErrorInfo;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.request.executor.discover.Distributor;
import eu.essi_lab.request.executor.query.IDistributedQueryExecutor;
import eu.essi_lab.request.executor.query.IQueryExecutor.Type;

/**
 * Tests the distributor query execution, by setting a predefined mocked set of query submitters and a series of queries
 * varying in their page (i.e. start record and count). In particular, the good distributor will return records from
 * three query submitters:
 * <ol>
 * <li><b>Query submitter 1</b>: 1, 2, 3</li>
 * <li><b>Query submitter 2</b>: 4, 5, 6</li>
 * <li><b>Query submitter 3</b>: 7, 8, 9</li>
 * </o>
 * The bad distributor has a different query submitter 2, which will gave always an exception:
 * <ol>
 * <li><b>Query submitter 1</b>: 1, 2, 3</li>
 * <li><b>Query submitter 2</b>: gives an exception</li>
 * <li><b>Query submitter 3</b>: 7, 8, 9</li>
 * </o>
 * 
 * @author boldrini
 */
public class DistributorTest {

    List<IDistributedQueryExecutor> querySubmitters = new ArrayList<>();
    List<IDistributedQueryExecutor> badQuerySubmitters = new ArrayList<>();
    private DiscoveryMessage message;
    private Distributor distributor;

    private IDistributedQueryExecutor querySubmitter1;
    private IDistributedQueryExecutor querySubmitter2;
    private IDistributedQueryExecutor querySubmitter3;
    private IDistributedQueryExecutor querySubmitterE;
    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    private Distributor badDistributor;

    @Before
    public void init() throws GSException {
	this.message = new DiscoveryMessage();
	LogicalBond testBond = BondFactory.createOrBond( //
		BondFactory.createSourceIdentifierBond("source1"), //
		BondFactory.createSourceIdentifierBond("source2"), //
		BondFactory.createSourceIdentifierBond("source3"));//
	message.setUserBond(testBond);
	message.setPermittedBond(testBond);
	message.setNormalizedBond(testBond);

	querySubmitter1 = getDistributedQuerySubmitterMock("source1", new GSResource[] { getR("1"), getR("2"), getR("3") });
	querySubmitter2 = getDistributedQuerySubmitterMock("source2", new GSResource[] { getR("4"), getR("5"), getR("6") });
	querySubmitter3 = getDistributedQuerySubmitterMock("source3", new GSResource[] { getR("7"), getR("8"), getR("9") });
	querySubmitters.add(querySubmitter1);
	querySubmitters.add(querySubmitter2);
	querySubmitters.add(querySubmitter3);
	this.distributor = new Distributor();
	distributor.setQuerySubmitters(querySubmitters);

	querySubmitterE = getExceptionMock("source2", new GSResource[] { getR("4"), getR("5"), getR("6") });
	badQuerySubmitters.add(querySubmitter1);
	badQuerySubmitters.add(querySubmitterE);
	badQuerySubmitters.add(querySubmitter3);
	this.badDistributor = new Distributor();
	badDistributor.setQuerySubmitters(badQuerySubmitters);

    }

    private GSResource getR(String identifier) {
	Dataset ret = new Dataset();
	ret.setOriginalId(identifier);

	return ret;
    }

    private IDistributedQueryExecutor getDistributedQuerySubmitterMock(String sourceIdentifier, GSResource[] resources) throws GSException {
	IDistributedQueryExecutor ret = Mockito.mock(IDistributedQueryExecutor.class);
	Mockito.when(ret.getType()).thenReturn(Type.DISTRIBUTED);
	Mockito.when(ret.getSourceIdentifier()).thenReturn(sourceIdentifier);
	Mockito.doAnswer(new Answer<SimpleEntry<String, DiscoveryCountResponse>>() {
	    @Override
	    public SimpleEntry<String, DiscoveryCountResponse> answer(InvocationOnMock invocation) throws Throwable {

		DiscoveryCountResponse countResult = new DiscoveryCountResponse();
		countResult.setCount(resources.length);

		return new SimpleEntry<String, DiscoveryCountResponse>(sourceIdentifier, countResult);
	    }
	}).when(ret).count(ArgumentMatchers.any(ReducedDiscoveryMessage.class));
	Mockito.doAnswer(new Answer<ResultSet<GSResource>>() {
	    @Override
	    public ResultSet<GSResource> answer(InvocationOnMock invocation) throws Throwable {
		Page page = (Page) invocation.getArguments()[1];
		int start = page.getStart() - 1;
		int count = page.getSize();
		ResultSet<GSResource> ret = new ResultSet<GSResource>();
		for (int i = start; i < Math.min(start + count, resources.length); i++) {
		    ret.getResultsList().add(resources[i]);
		}
		return ret;
	    }
	}).when(ret).retrieve(ArgumentMatchers.any(ReducedDiscoveryMessage.class), ArgumentMatchers.any(Page.class));
	return ret;
    }

    private IDistributedQueryExecutor getExceptionMock(String sourceIdentifier, GSResource[] resources) throws GSException {
	IDistributedQueryExecutor ret = Mockito.mock(IDistributedQueryExecutor.class);
	Mockito.when(ret.getType()).thenReturn(Type.DISTRIBUTED);
	Mockito.when(ret.getSourceIdentifier()).thenReturn(sourceIdentifier);
	Mockito.doAnswer(new Answer<SimpleEntry<String, DiscoveryCountResponse>>() {
	    @Override
	    public SimpleEntry<String, DiscoveryCountResponse> answer(InvocationOnMock invocation) throws Throwable {

		DiscoveryCountResponse countResult = new DiscoveryCountResponse();
		countResult.setCount(resources.length);

		return new SimpleEntry<String, DiscoveryCountResponse>(sourceIdentifier, countResult);
	    }
	}).when(ret).count(ArgumentMatchers.any(ReducedDiscoveryMessage.class));
	Mockito.doAnswer(new Answer<ResultSet<GSResource>>() {
	    @Override
	    public ResultSet<GSResource> answer(InvocationOnMock invocation) throws Throwable {
		GSException ex = GSException.createException(getClass(), "error", "error", ErrorInfo.ERRORTYPE_INTERNAL,
			ErrorInfo.SEVERITY_FATAL, "errorId");
		throw ex;
	    }
	}).when(ret).retrieve(ArgumentMatchers.any(ReducedDiscoveryMessage.class), ArgumentMatchers.any(Page.class));
	return ret;
    }

    /**
     * @throws GSException
     */
    @Test
    public void test1() throws GSException {
	message.setPage(new Page(2));
	ResultSet<GSResource> resultSet = distributor.retrieve(message);
	assertEquals(resultSet.getResultsList(), new GSResource[] { getR("1"), getR("2") });
	// The bad distributor source2 will return an exception
	resultSet = badDistributor.retrieve(message);
	assertEquals(resultSet.getResultsList(), new GSResource[] { getR("1"), getR("2") });
	Assert.assertEquals(0, resultSet.getException().getErrorInfoList().size());

    }

    @Test
    public void test1_2() throws GSException {
	message.setPage(new Page(1, 2));
	ResultSet<GSResource> resultSet = distributor.retrieve(message);
	assertEquals(resultSet.getResultsList(), new GSResource[] { getR("1"), getR("2") });
	// The bad distributor source2 will return an exception
	resultSet = badDistributor.retrieve(message);
	assertEquals(resultSet.getResultsList(), new GSResource[] { getR("1"), getR("2") });
	Assert.assertEquals(0, resultSet.getException().getErrorInfoList().size());

    }

    @Test
    public void test2() throws GSException {
	message.setPage(new Page(3));
	ResultSet<GSResource> resultSet = distributor.retrieve(message);
	assertEquals(resultSet.getResultsList(), new GSResource[] { getR("1"), getR("2"), getR("3") });
	// The bad distributor source2 will return an exception
	resultSet = badDistributor.retrieve(message);
	assertEquals(resultSet.getResultsList(), new GSResource[] { getR("1"), getR("2"), getR("3") });
	Assert.assertEquals(0, resultSet.getException().getErrorInfoList().size());
    }

    @Test
    public void test4() throws GSException {
	message.setPage(new Page(2, 3));
	ResultSet<GSResource> resultSet = distributor.retrieve(message);
	assertEquals(resultSet.getResultsList(), new GSResource[] { getR("2"), getR("3"), getR("4") });
	// The bad distributor source2 will return an exception
	resultSet = badDistributor.retrieve(message);
	assertEquals(resultSet.getResultsList(), new GSResource[] { getR("2"), getR("3") });
	Assert.assertEquals(1, resultSet.getException().getErrorInfoList().size());
    }

    @Test
    public void test5() throws GSException {
	message.setPage(new Page(2, 6));
	ResultSet<GSResource> resultSet = distributor.retrieve(message);
	assertEquals(resultSet.getResultsList(), new GSResource[] { getR("2"), getR("3"), getR("4"), getR("5"), getR("6"), getR("7") });
	// The bad distributor source2 will return an exception
	resultSet = badDistributor.retrieve(message);
	assertEquals(resultSet.getResultsList(), new GSResource[] { getR("2"), getR("3"), getR("7") });
	Assert.assertEquals(1, resultSet.getException().getErrorInfoList().size());

    }

    private void assertEquals(List<GSResource> actuals, GSResource[] expecteds) {
	Assert.assertEquals(expecteds.length, actuals.size());
	for (int i = 0; i < expecteds.length; i++) {
	    GSResource expected = expecteds[i];
	    GSResource actual = actuals.get(i);
	    Assert.assertEquals(expected.getOriginalId(), actual.getOriginalId());
	}
    }
}
