package eu.essi_lab.request.executor.discover;

import static eu.essi_lab.request.executor.query.IQueryExecutor.Type.DATABASE;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;

import eu.essi_lab.adk.distributed.IDistributedAccessor;
import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.Page;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.bond.BondFactory;
import eu.essi_lab.messages.bond.LogicalBond;
import eu.essi_lab.messages.count.DiscoveryCountResponse;
import eu.essi_lab.model.BrokeringStrategy;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.request.executor.IDiscoveryExecutor;
import eu.essi_lab.request.executor.discover.submitter.DatabaseQueryExecutor;

/**
 * @author ilsanto
 */
public class DiscoverExecutorTest {

    private Logger logger = GSLoggerFactory.getLogger(DiscoverExecutorTest.class);

    @Test
    public void testDiscoverExecutorServiceLoader() {

	ServiceLoader<IDiscoveryExecutor> loader = ServiceLoader.load(IDiscoveryExecutor.class);

	Iterator<IDiscoveryExecutor> it = loader.iterator();

	boolean found = false;

	while (it.hasNext()) {

	    if (DiscoveryExecutor.class.isAssignableFrom(it.next().getClass()))
		found = true;

	}

	Assert.assertTrue("Can not find " + IDiscoveryExecutor.class + " via Java Service Loader of class " + IDiscoveryExecutor.class,
		found);
    }

//    @Test
//    public void paging_test_1() throws GSException {
//
//	List<GSSource> sources = new ArrayList<>();
//	List<IDistributedAccessor> accessorList = new ArrayList<>();
//
//	/**
//	 * This means that ALL database sources have a total of 1 result. All database resources have ids hr_#
//	 */
//	DatabaseQueryExecutor dataBaseQueryExecutor = initDataBaseQueryExecutor(1);
//
//	int globalStart = 1;
//	int globalCount = 3;
//
//	/**
//	 * This adds a harvested source
//	 */
//	createDataBaseSource(sources);
//
//	/**
//	 * This creates a distributed source which has a total of 3 results. All distributed source resources have ids dr_#_# where the
//	 * first # is the number of the source starting from 0 (therefore in this case: 1)
//	 */
//	createDistributedSource(sources, accessorList, 3);
//
//	/**
//	 * This adds a harvested source
//	 */
//	createDataBaseSource(sources);
//
//	/**
//	 * This creates a distributed source which has a total of 2 results. All distributed source resources have ids dr_#_# where the
//	 * first # is the number of the source starting from 0 (therefore in this case: 3)
//	 */
//	createDistributedSource(sources, accessorList, 2);
//
//	/**
//	 * Here I define the ordered list of expected results (with result idenfiers) according to the GI-suite source ranking policy.
//	 * TODO
//	 * document GI-suite source ranking policy
//	 *
//	 * In this case, database sources have in total 1 resource (see comment above initDataBaseQueryExecutor invokation). Thus the
//	 * first expected identifier is hr_0.
//	 * Second and third results come from distributed sources. Since the first distributed source has a total of 3 results, both
//	 * second and third results come from this source, and have ids dr_1_0 and dr_1_1.
//	 */
//	String[] expectedIdentifiers = new String[] { "hr_0", "dr_1_0", "dr_1_1" };
//
//	initAndRun(accessorList, sources, dataBaseQueryExecutor, expectedIdentifiers, globalCount, globalStart);
//
//    }
//
//    /**
//     * The test is described in the body of {@link #paging_test_1()}
//     *
//     * @throws GSException
//     */
//    @Test
//    public void paging_test_2() throws GSException {
//
//	List<GSSource> sources = new ArrayList<>();
//	List<IDistributedAccessor> accessorList = new ArrayList<>();
//
//	DatabaseQueryExecutor dataBaseQueryExecutor = initDataBaseQueryExecutor(1);
//
//	int globalStart = 1;
//	int globalCount = 4;
//
//	createDataBaseSource(sources);
//
//	createDistributedSource(sources, accessorList, 3);
//
//	createDataBaseSource(sources);
//
//	createDistributedSource(sources, accessorList, 2);
//
//	String[] expectedIdentifiers = new String[] { "hr_0", "dr_1_0", "dr_1_1", "dr_1_2" };
//
//	initAndRun(accessorList, sources, dataBaseQueryExecutor, expectedIdentifiers, globalCount, globalStart);
//
//    }
//
//    /**
//     * The test is described in the body of {@link #paging_test_1()}
//     *
//     * @throws GSException
//     */
//    @Test
//    public void paging_test_3() throws GSException {
//
//	List<GSSource> sources = new ArrayList<>();
//	List<IDistributedAccessor> accessorList = new ArrayList<>();
//
//	DatabaseQueryExecutor dataBaseQueryExecutor = initDataBaseQueryExecutor(1);
//
//	int globalStart = 2;
//	int globalCount = 4;
//
//	createDataBaseSource(sources);
//
//	createDistributedSource(sources, accessorList, 3);
//
//	createDataBaseSource(sources);
//
//	createDistributedSource(sources, accessorList, 2);
//
//	String[] expectedIdentifiers = new String[] { "dr_1_0", "dr_1_1", "dr_1_2", "dr_3_0" };
//
//	initAndRun(accessorList, sources, dataBaseQueryExecutor, expectedIdentifiers, globalCount, globalStart);
//
//    }
//
//    /**
//     * The test is described in the body of {@link #paging_test_1()}
//     *
//     * @throws GSException
//     */
//    @Test
//    public void paging_test_4() throws GSException {
//
//	List<GSSource> sources = new ArrayList<>();
//	List<IDistributedAccessor> accessorList = new ArrayList<>();
//
//	DatabaseQueryExecutor dataBaseQueryExecutor = initDataBaseQueryExecutor(1);
//
//	int globalStart = 2;
//	int globalCount = 4;
//
//	createDataBaseSource(sources);
//
//	createDistributedSource(sources, accessorList, 3);
//
//	createDistributedSource(sources, accessorList, 2);
//
//	createDataBaseSource(sources);
//
//	String[] expectedIdentifiers = new String[] { "dr_1_0", "dr_1_1", "dr_1_2", "dr_2_0" };
//
//	initAndRun(accessorList, sources, dataBaseQueryExecutor, expectedIdentifiers, globalCount, globalStart);
//
//    }
//
//    /**
//     * The test is described in the body of {@link #paging_test_1()}
//     *
//     * @throws GSException
//     */
//    @Test
//    public void paging_test_5() throws GSException {
//
//	List<GSSource> sources = new ArrayList<>();
//	List<IDistributedAccessor> accessorList = new ArrayList<>();
//
//	DatabaseQueryExecutor dataBaseQueryExecutor = initDataBaseQueryExecutor(1);
//
//	int globalStart = 2;
//	int globalCount = 4;
//
//	createDataBaseSource(sources);
//
//	createDistributedSource(sources, accessorList, 1);
//
//	createDistributedSource(sources, accessorList, 2);
//
//	createDataBaseSource(sources);
//
//	String[] expectedIdentifiers = new String[] { "dr_1_0", "dr_2_0", "dr_2_1" };
//
//	initAndRun(accessorList, sources, dataBaseQueryExecutor, expectedIdentifiers, globalCount, globalStart);
//
//    }
//
//    /**
//     * The test is described in the body of {@link #paging_test_1()}
//     *
//     * @throws GSException
//     */
//    @Test
//    public void paging_test_6() throws GSException {
//
//	List<GSSource> sources = new ArrayList<>();
//	List<IDistributedAccessor> accessorList = new ArrayList<>();
//
//	DatabaseQueryExecutor dataBaseQueryExecutor = initDataBaseQueryExecutor(10);
//
//	int globalStart = 2;
//	int globalCount = 4;
//
//	createDataBaseSource(sources);
//
//	createDistributedSource(sources, accessorList, 1);
//
//	createDistributedSource(sources, accessorList, 2);
//
//	createDataBaseSource(sources);
//
//	String[] expectedIdentifiers = new String[] { "hr_1", "hr_2", "hr_3", "hr_4" };
//
//	initAndRun(accessorList, sources, dataBaseQueryExecutor, expectedIdentifiers, globalCount, globalStart);
//
//    }
//
//    /**
//     * The test is described in the body of {@link #paging_test_1()}
//     *
//     * @throws GSException
//     */
//    @Test
//    public void paging_test_7() throws GSException {
//
//	List<GSSource> sources = new ArrayList<>();
//	List<IDistributedAccessor> accessorList = new ArrayList<>();
//
//	DatabaseQueryExecutor dataBaseQueryExecutor = initDataBaseQueryExecutor(10);
//
//	int globalStart = 11;
//	int globalCount = 2;
//
//	createDataBaseSource(sources);
//
//	createDistributedSource(sources, accessorList, 1);
//
//	createDistributedSource(sources, accessorList, 2);
//
//	createDataBaseSource(sources);
//
//	String[] expectedIdentifiers = new String[] { "dr_1_0", "dr_2_0" };
//
//	initAndRun(accessorList, sources, dataBaseQueryExecutor, expectedIdentifiers, globalCount, globalStart);
//
//    }
//
//    /**
//     * The test is described in the body of {@link #paging_test_1()}
//     *
//     * @throws GSException
//     */
//    @Test
//    public void paging_test_8() throws GSException {
//
//	List<GSSource> sources = new ArrayList<>();
//	List<IDistributedAccessor> accessorList = new ArrayList<>();
//
//	DatabaseQueryExecutor dataBaseQueryExecutor = initDataBaseQueryExecutor(10);
//
//	int globalStart = 11;
//	int globalCount = 2;
//
//	createDistributedSource(sources, accessorList, 1);
//
//	createDataBaseSource(sources);
//
//	createDistributedSource(sources, accessorList, 2);
//
//	createDataBaseSource(sources);
//
//	String[] expectedIdentifiers = new String[] { "dr_0_0", "dr_2_0" };
//
//	initAndRun(accessorList, sources, dataBaseQueryExecutor, expectedIdentifiers, globalCount, globalStart);
//
//    }

    /**
     * The test is described in the body of {@link #paging_test_1()}
     *
     * @throws GSException
     */
//    @Test
//    public void paging_test_9() throws GSException {
//
//	List<GSSource> sources = new ArrayList<>();
//	List<IDistributedAccessor> accessorList = new ArrayList<>();
//
//	DatabaseQueryExecutor dataBaseQueryExecutor = initDataBaseQueryExecutor(10);
//
//	int globalStart = 11;
//	int globalCount = 2;
//
//	createMixedSecondLevelSource(sources, accessorList, 1);
//
//	createDataBaseSource(sources);
//
//	createDistributedSource(sources, accessorList, 2);
//
//	createDataBaseSource(sources);
//
//	String[] expectedIdentifiers = new String[] { "mx_0_0", "dr_2_0" };
//
//	BondReducer bondReducer = Mockito.mock(BondReducer.class);
//
//	Mockito.doReturn(true).when(bondReducer).mixedSourceSecondLevelBond(Mockito.any(), Mockito.any());
//
//	initAndRun(accessorList, sources, dataBaseQueryExecutor, expectedIdentifiers, globalCount, globalStart, bondReducer);
//
//    }

    /**
     * The test is described in the body of {@link #paging_test_1()}
     *
     * @throws GSException
     */
//    @Test
//    public void paging_test_10() throws GSException {
//
//	List<GSSource> sources = new ArrayList<>();
//	List<IDistributedAccessor> accessorList = new ArrayList<>();
//
//	DatabaseQueryExecutor dataBaseQueryExecutor = initDataBaseQueryExecutor(10);
//
//	int globalStart = 11;
//	int globalCount = 2;
//
//	createMixedSecondLevelSource(sources, accessorList, 1);
//
//	createDataBaseSource(sources);
//
//	createDistributedSource(sources, accessorList, 2);
//
//	createDataBaseSource(sources);
//
//	String[] expectedIdentifiers = new String[] { "dr_2_0", "dr_2_1" };
//
//	BondReducer bondReducer = Mockito.mock(BondReducer.class);
//
//	Mockito.doReturn(false).when(bondReducer).mixedSourceSecondLevelBond(Mockito.any(), Mockito.any());
//
//	initAndRun(accessorList, sources, dataBaseQueryExecutor, expectedIdentifiers, globalCount, globalStart, bondReducer);
//
//    }

    private void createMixedSecondLevelSource(List<GSSource> sourceList, List<IDistributedAccessor> accessorList, int numberOfResources)
	    throws GSException {

	Integer sourceNumber = sourceList.size();
	GSSource source = new GSSource();
	source.setUniqueIdentifier("sid" + sourceNumber);
	source.setLabel("label" + sourceNumber);
	source.setBrokeringStrategy(BrokeringStrategy.MIXED);

	List<GSResource> list = new ArrayList<>();

	for (int i = 0; i < numberOfResources; i++) {
	    GSResource dr1 = new Dataset();
	    dr1.setOriginalId("mx_" + sourceNumber + "_" + i);
	    list.add(dr1);
	}

	accessorList.add(initDistributedAccessor(source, list));

	sourceList.add(source);

    }

    private void initAndRun(List<IDistributedAccessor> accessorList, List<GSSource> sources, DatabaseQueryExecutor dataBaseQueryExecutor,
	    String[] expectedIdentifiers, int globalCount, int globalStart) throws GSException {

	PagingTestInit pti = new PagingTestInit();

	pti.setAccessorList(accessorList);
	pti.setSources(sources);
	pti.setDataBaseQueryExecutor(dataBaseQueryExecutor);
	pti.setExpectedIdentifiers(expectedIdentifiers);
	pti.setGlobalCount(globalCount);
	pti.setGlobalStart(globalStart);
	testPaging(pti);
    }

    private void initAndRun(List<IDistributedAccessor> accessorList, List<GSSource> sources, DatabaseQueryExecutor dataBaseQueryExecutor,
	    String[] expectedIdentifiers, int globalCount, int globalStart, BondReducer reducer) throws GSException {

	PagingTestInit pti = new PagingTestInit();

	pti.setAccessorList(accessorList);
	pti.setSources(sources);
	pti.setDataBaseQueryExecutor(dataBaseQueryExecutor);
	pti.setExpectedIdentifiers(expectedIdentifiers);
	pti.setGlobalCount(globalCount);
	pti.setGlobalStart(globalStart);
	pti.setBondReducer(reducer);
	testPaging(pti);
    }

    private class PagingTestInit {
	private List<IDistributedAccessor> accessorList;
	private DatabaseQueryExecutor dataBaseQueryExecutor;
	private List<GSSource> sources;
	private int globalStart;
	private int globalCount;
	private String[] expectedIdentifiers;
	private BondReducer bondReducer;

	public List<IDistributedAccessor> getAccessorList() {
	    return accessorList;
	}

	public void setAccessorList(List<IDistributedAccessor> accessorList) {
	    this.accessorList = accessorList;
	}

	public DatabaseQueryExecutor getDataBaseQueryExecutor() {
	    return dataBaseQueryExecutor;
	}

	public void setDataBaseQueryExecutor(DatabaseQueryExecutor dataBaseQueryExecutor) {
	    this.dataBaseQueryExecutor = dataBaseQueryExecutor;
	}

	public List<GSSource> getSources() {
	    return sources;
	}

	public void setSources(List<GSSource> sources) {
	    this.sources = sources;
	}

	public int getGlobalStart() {
	    return globalStart;
	}

	public void setGlobalStart(int globalStart) {
	    this.globalStart = globalStart;
	}

	public int getGlobalCount() {
	    return globalCount;
	}

	public void setGlobalCount(int globalCount) {
	    this.globalCount = globalCount;
	}

	public String[] getExpectedIdentifiers() {
	    return expectedIdentifiers;
	}

	public void setExpectedIdentifiers(String[] expectedIdentifiers) {
	    this.expectedIdentifiers = expectedIdentifiers;
	}

	public void setBondReducer(BondReducer reducer) {
	    bondReducer = reducer;
	}

	public BondReducer getBondReducer() {
	    return bondReducer;
	}
    }

    private void testPaging(PagingTestInit init) throws GSException {

	List<IDistributedAccessor> accessorList = init.getAccessorList();
	DatabaseQueryExecutor dataBaseQueryExecutor = init.getDataBaseQueryExecutor();
	List<GSSource> sources = init.getSources();
	int globalStart = init.getGlobalStart();
	int globalCount = init.getGlobalCount();
	String[] expectedIdentifiers = init.getExpectedIdentifiers();

	QueryExecutorInitializer initializer = createQueryExecutorInitializer();

	BrokeringStrategyResolver strategyResolver = new BrokeringStrategyResolver();

	if (init.getBondReducer() != null)
//	    strategyResolver.setReducer(init.getBondReducer());

//	initializer.setStrategyResolver(strategyResolver);
//	initAccessorFactory(initializer, accessorList);

	Mockito.doReturn(dataBaseQueryExecutor).when(initializer).createDatabaseQueryExecutor();

	LogicalBond testBond = initBond(sources);

	DiscoveryMessage discoveryMessage = new DiscoveryMessage();

	Page globalPage = Mockito.mock(Page.class);

	Mockito.doReturn(globalStart).when(globalPage).getStart();
	Mockito.doReturn(globalCount).when(globalPage).getSize();

	discoveryMessage.setPage(globalPage);

	discoveryMessage.setSources(sources);

	discoveryMessage.setUserBond(testBond);

	DiscoveryExecutor executor = new DiscoveryExecutor();

	executor.setQueryExecutorInitializer(initializer);

	ResultSet<GSResource> result = executor.retrieve(discoveryMessage);

	List<GSResource> resList = result.getResultsList();

	for (GSResource r : resList)
	    logger.info("Retrieved record id {}", r.getOriginalId());

	assertEquals(result.getResultsList(), expectedIdentifiers);
    }

    private QueryExecutorInitializer createQueryExecutorInitializer() {
	QueryExecutorInitializer initializer = Mockito.spy(new QueryExecutorInitializer());

//	GSYellowPage yp = Mockito.mock(GSYellowPage.class);
//
//	Mockito.doReturn(yp).when(initializer).getYellowPages(Mockito.any());
	return initializer;
    }

    private void createDistributedSource(List<GSSource> sourceList, List<IDistributedAccessor> accessorList, int numberOfResources)
	    throws GSException {

	Integer sourceNumber = sourceList.size();
	GSSource source = new GSSource();
	source.setUniqueIdentifier("sid" + sourceNumber);
	source.setLabel("label" + sourceNumber);
	source.setBrokeringStrategy(BrokeringStrategy.DISTRIBUTED);

	List<GSResource> list = new ArrayList<>();

	for (int i = 0; i < numberOfResources; i++) {
	    GSResource dr1 = new Dataset();
	    dr1.setOriginalId("dr_" + sourceNumber + "_" + i);
	    list.add(dr1);
	}

	accessorList.add(initDistributedAccessor(source, list));

	sourceList.add(source);

    }

    private void createDataBaseSource(List<GSSource> sourceList) {

	Integer sourceNumber = sourceList.size();
	GSSource source = new GSSource();
	source.setUniqueIdentifier("sid" + sourceNumber);
	source.setLabel("label" + sourceNumber);
	source.setBrokeringStrategy(BrokeringStrategy.HARVESTED);

	sourceList.add(source);

    }

    private LogicalBond initBond(List<GSSource> sources) {
	LogicalBond testBond = BondFactory.createOrBond();

	for (GSSource source : sources)
	    testBond.getOperands().add(BondFactory.createSourceIdentifierBond(source.getUniqueIdentifier()));

	return testBond;
    }

//    private void initAccessorFactory(QueryExecutorInitializer initializer, List<IDistributedAccessor> accessors) throws GSException {
//	AccessorFactory factory = Mockito.mock(AccessorFactory.class);
//
//	Mockito.doAnswer(new Answer<IDistributedAccessor>() {
//	    @Override
//	    public IDistributedAccessor answer(InvocationOnMock invocation) throws Throwable {
//
//		GSSource source = invocation.getArgument(0);
//
//		for (IDistributedAccessor accessor : accessors) {
//		    if (accessor.getSource().getUniqueIdentifier().equalsIgnoreCase(source.getUniqueIdentifier()))
//			return accessor;
//		}
//
//		throw new Exception("Unexpected source " + source.getUniqueIdentifier());
//	    }
//
//	}).when(factory).getDistributedAccessor((GSSource) Mockito.any());
//
//	initializer.setAccessorFactory(factory);
//    }

    /**
     * Initializes a mock for the DatabaseQueryExecutor. The DatabaseQueryExecutor will return the provided count in response to count
     * method and a subset of the provided resultList in response to retrieve method (the subset starts at page.getStart-1 and contains
     * page.getCount elements)
     *
     * @param count
     * @return
     * @throws GSException
     */
    private DatabaseQueryExecutor initDataBaseQueryExecutor(int count) throws GSException {
	List<GSResource> resultList = new ArrayList<>();

	for (int i = 0; i < count; i++) {

	    GSResource harvested = new Dataset();
	    harvested.setOriginalId("hr_" + i);
	    resultList.add(harvested);
	}

	DatabaseQueryExecutor dataBaseQueryExecutor = Mockito.mock(DatabaseQueryExecutor.class);

	DiscoveryCountResponse countResult = Mockito.mock(DiscoveryCountResponse.class);

	Mockito.doReturn(count).when(countResult).getCount();

	AbstractMap.SimpleEntry<String, DiscoveryCountResponse> countPair = new AbstractMap.SimpleEntry<String, DiscoveryCountResponse>("dbid", countResult);

	Mockito.doReturn(countPair).when(dataBaseQueryExecutor).count(Mockito.any());

	Mockito.doReturn(DATABASE).when(dataBaseQueryExecutor).getType();

	Mockito.doReturn("dbid").when(dataBaseQueryExecutor).getSourceIdentifier();

	Mockito.doAnswer(new Answer<ResultSet<GSResource>>() {
	    @Override
	    public ResultSet<GSResource> answer(InvocationOnMock invocation) throws Throwable {

		Page requestedPage = invocation.getArgument(1);

		int requestedCount = requestedPage.getSize();

		int requestedStart = requestedPage.getStart();

		logger.debug("Executing mocked db query with start {} and count {}", requestedStart, requestedCount);

		List<GSResource> returnList = new ArrayList<>();

		for (int i = requestedStart - 1; i < (requestedCount + requestedStart - 1); i++)
		    returnList.add(resultList.get(i));

		return new ResultSet<>(returnList);
	    }
	}).when(dataBaseQueryExecutor).retrieve(Mockito.any(), Mockito.any());

	return dataBaseQueryExecutor;
    }

    /**
     * Initializes a mock for the distributed accessor. The accessor will return the size of the provided resultList in response to count
     * method and a ubset of the provided resultList in response to query method (the subset starts at page.getStart-1 and contains
     * page.getCount elements)
     *
     * @param resultList
     * @return
     * @throws GSException
     */
    private IDistributedAccessor initDistributedAccessor(GSSource source, List<GSResource> resultList) throws GSException {

	IDistributedAccessor accessor = Mockito.mock(IDistributedAccessor.class);

	DiscoveryCountResponse countResultDistributed = Mockito.mock(DiscoveryCountResponse.class);

	Mockito.doReturn(resultList.size()).when(countResultDistributed).getCount();

	Mockito.doReturn(countResultDistributed).when(accessor).count(Mockito.any());

	Mockito.doAnswer(new Answer<ResultSet<GSResource>>() {
	    @Override
	    public ResultSet<GSResource> answer(InvocationOnMock invocation) throws Throwable {

		Page requestedPage = invocation.getArgument(1);

		int requestedCount = requestedPage.getSize();

		int requestedStart = requestedPage.getStart();

		List<GSResource> returnList = new ArrayList<>();

		logger.debug("Executing mocked distributed query on source {} with start {} and count {}", source.getUniqueIdentifier(),
			requestedStart, requestedCount);

		for (int i = requestedStart - 1; i < (requestedCount + requestedStart - 1); i++)
		    returnList.add(resultList.get(i));

		return new ResultSet<>(returnList);
	    }
	}).when(accessor).query(Mockito.any(), Mockito.any());

	Mockito.doReturn(source).when(accessor).getSource();

	return accessor;
    }

    private void assertEquals(List<GSResource> actuals, String[] expecteds) {
	Assert.assertEquals(expecteds.length, actuals.size());

	for (int i = 0; i < expecteds.length; i++) {
	    String expected = expecteds[i];
	    GSResource actual = actuals.get(i);

	    Assert.assertEquals(expected, actual.getOriginalId());
	}
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
