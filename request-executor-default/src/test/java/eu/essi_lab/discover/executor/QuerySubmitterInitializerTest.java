//package eu.essi_lab.discover.executor;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import org.junit.Assert;
//import org.junit.Before;
//import org.junit.Rule;
//import org.junit.Test;
//import org.junit.rules.ExpectedException;
//import org.mockito.ArgumentMatchers;
//import org.mockito.Mockito;
//
//import eu.essi_lab.adk.AccessorFactory;
//import eu.essi_lab.adk.distributed.IDistributedAccessor;
//import eu.essi_lab.messages.DiscoveryMessage;
//import eu.essi_lab.model.BrokeringStrategy;
//import eu.essi_lab.model.GSSource;
//import eu.essi_lab.model.exceptions.GSException;
//import eu.essi_lab.request.executor.discover.QueryExecutorInitializer;
//import eu.essi_lab.request.executor.query.IQueryExecutor;
//
///**
// * Checks that the query submitter creates the correct {@link IQueryExecutor} set for the given discovery message.
// *
// * @author boldrini
// */
//public class QuerySubmitterInitializerTest {
//
//    @Rule
//    public ExpectedException expectedException = ExpectedException.none();
//    private QueryExecutorInitializer initializer;
//
//    @Before
//    public void init() {
//
//	this.initializer = Mockito.spy(new QueryExecutorInitializer());
//	AccessorFactory accessorFactory = Mockito.mock(AccessorFactory.class);
//	IDistributedAccessor distributedAccessor = Mockito.mock(IDistributedAccessor.class);
//	try {
//	    Mockito.when(accessorFactory.getDistributedAccessor(ArgumentMatchers.any(GSSource.class))).thenReturn(distributedAccessor);
//	} catch (GSException e) {
//	    e.printStackTrace();
//	}
//	this.initializer.setAccessorFactory(accessorFactory);
////	BrokeringStrategyResolver strategyResolver = new BrokeringStrategyResolver();
////	this.initializer.setStrategyResolver(strategyResolver);
//	Mockito.doReturn(null).when(initializer).getYellowPages(ArgumentMatchers.any());
//    }
//
//    @Test
//    public void exceptionWithUnconfiguredSource() throws GSException {
//	expectedException.expect(GSException.class);
//	DiscoveryMessage message = new DiscoveryMessage();
//	List<GSSource> sources = new ArrayList<>();
//	GSSource unconfiguredSource = new GSSource();
//	sources.add(unconfiguredSource);
//	message.setSources(sources);
//	initializer.initQueryExecutors(message);
//    }
//
//    @Test
//    public void testWithDistributedBlock() throws GSException {
//	DiscoveryMessage message = new DiscoveryMessage();
//	List<GSSource> sources = new ArrayList<>();
//	sources.add(getDistributedSource());
//	sources.add(getDistributedSource());
//	sources.add(getDistributedSource());
//	message.setSources(sources);
//	List<IQueryExecutor> querySubmitters = initializer.initQueryExecutors(message);
//	Assert.assertEquals(3, querySubmitters.size());
//    }
//
//    @Test
//    public void testWithOneMixed() throws GSException {
//	DiscoveryMessage message = new DiscoveryMessage();
//	List<GSSource> sources = new ArrayList<>();
//	sources.add(getMixedSource());
//	message.setSources(sources);
//	List<IQueryExecutor> querySubmitters = initializer.initQueryExecutors(message);
//	Assert.assertEquals(1, querySubmitters.size());
//    }
//
//    @Test
//    public void testWithHarvestedBlockBefore() throws GSException {
//	DiscoveryMessage message = new DiscoveryMessage();
//	List<GSSource> sources = new ArrayList<>();
//	sources.add(getHarvestedSource());
//	sources.add(getHarvestedSource());
//	sources.add(getHarvestedSource());
//	sources.add(getDistributedSource());
//	message.setSources(sources);
//	List<IQueryExecutor> querySubmitters = initializer.initQueryExecutors(message);
//	Assert.assertEquals(2, querySubmitters.size());
//    }
//
//    @Test
//    public void testWithCentralHarvestedBlock() throws GSException {
//	DiscoveryMessage message = new DiscoveryMessage();
//	List<GSSource> sources = new ArrayList<>();
//	sources.add(getDistributedSource());
//	sources.add(getHarvestedSource());
//	sources.add(getHarvestedSource());
//	sources.add(getHarvestedSource());
//	sources.add(getDistributedSource());
//	message.setSources(sources);
//	List<IQueryExecutor> querySubmitters = initializer.initQueryExecutors(message);
//	Assert.assertEquals(3, querySubmitters.size());
//    }
//
//    @Test
//    public void testWithHarvestedBlockAfter() throws GSException {
//	DiscoveryMessage message = new DiscoveryMessage();
//	List<GSSource> sources = new ArrayList<>();
//	sources.add(getDistributedSource());
//	sources.add(getHarvestedSource());
//	sources.add(getHarvestedSource());
//	sources.add(getHarvestedSource());
//	message.setSources(sources);
//	List<IQueryExecutor> querySubmitters = initializer.initQueryExecutors(message);
//	Assert.assertEquals(2, querySubmitters.size());
//    }
//
//    private GSSource getDistributedSource() {
//	GSSource harvestedSource = new GSSource();
//	harvestedSource.setBrokeringStrategy(BrokeringStrategy.DISTRIBUTED);
//	return harvestedSource;
//    }
//
//    private GSSource getHarvestedSource() {
//	GSSource harvestedSource = new GSSource();
//	harvestedSource.setBrokeringStrategy(BrokeringStrategy.HARVESTED);
//	return harvestedSource;
//    }
//
//    private GSSource getMixedSource() {
//	GSSource mixedSource = new GSSource();
//	mixedSource.setBrokeringStrategy(BrokeringStrategy.MIXED);
//	return mixedSource;
//    }
//
//}
