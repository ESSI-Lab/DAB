package eu.essi_lab.cfga.request.executor.test;

import java.util.AbstractMap.SimpleEntry;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import eu.essi_lab.adk.distributed.IDistributedAccessor;
import eu.essi_lab.cfga.gs.ConfigurationWrapper;
import eu.essi_lab.cfga.gs.DefaultConfiguration;
import eu.essi_lab.identifierdecorator.IdentifierDecorator;
import eu.essi_lab.messages.DiscoveryMessage;
import eu.essi_lab.messages.ReducedDiscoveryMessage;
import eu.essi_lab.messages.ResultSet;
import eu.essi_lab.messages.bond.Bond;
import eu.essi_lab.messages.bond.BondFactory;
import eu.essi_lab.messages.bond.BondOperator;
import eu.essi_lab.messages.count.DiscoveryCountResponse;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.Dataset;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.MetadataElement;
import eu.essi_lab.request.executor.discover.submitter.DistributedQueryExecutor;
import eu.essi_lab.request.executor.query.IQueryExecutor.Type;

public class DistributedQueryExecutorTest {

    private static final String ORIGINAL_ID = "oid1";

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private DistributedQueryExecutor createExecutor() throws GSException {

	@SuppressWarnings("rawtypes")
	IDistributedAccessor accessor = Mockito.mock(IDistributedAccessor.class);
	ResultSet<GSResource> resultSet = new ResultSet<>();

	Dataset originalDataset = new Dataset();
	originalDataset.setOriginalId(ORIGINAL_ID);

	resultSet.getResultsList().add(originalDataset);
	Mockito.when(accessor.query(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(resultSet);

	String sourceId = "sourceId";

	GSSource source = new GSSource();
	source.setUniqueIdentifier(sourceId);
	originalDataset.setSource(source);

	DistributedQueryExecutor executor = new DistributedQueryExecutor(accessor, sourceId);

	IdentifierDecorator identifierDecorator = new IdentifierDecorator();
	executor.setIdentifierDecorator(identifierDecorator);

	return executor;
    }

    @Test
    public void test() throws Exception {

	DistributedQueryExecutor executor = createExecutor();

	DiscoveryMessage message = new DiscoveryMessage();
	ReducedDiscoveryMessage rdm = new ReducedDiscoveryMessage(message, null);

	ResultSet<GSResource> resultSet = executor.retrieve(rdm, null);

	// waits the executor service of the DistributedQueryExecutor which stores the YP
	Thread.sleep(1000);

	List<GSResource> resources = resultSet.getResultsList();
	Assert.assertEquals(1, resources.size());
	GSResource resource = resources.get(0);
	Assert.assertEquals(ORIGINAL_ID, resource.getOriginalId().get());
	Assert.assertEquals(resource.getPublicId(), resource.getPrivateId());
	// System.out.println("Public id: "+resource.getPublicId());
	Assert.assertNotEquals(ORIGINAL_ID, resource.getPublicId());

	Bond reducedBond = BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.IDENTIFIER, resource.getPublicId());
	message.setUserBond(reducedBond);
	ReducedDiscoveryMessage reducedDiscoveryMessage = new ReducedDiscoveryMessage(message, reducedBond);
	ResultSet<GSResource> result = executor.retrieve(reducedDiscoveryMessage, null);

	// waits the executor service of the DistributedQueryExecutor which stores the YP
	Thread.sleep(1000);

	Assert.assertEquals(1, result.getResultsList().size());
	Assert.assertEquals(resource.getPublicId(), result.getResultsList().get(0).getPublicId());
	SimpleEntry<String, DiscoveryCountResponse> countSet = executor.count(reducedDiscoveryMessage);
	Assert.assertNotNull(countSet);
	Assert.assertEquals(1, countSet.getValue().getCount());

	reducedBond = BondFactory.createSimpleValueBond(BondOperator.EQUAL, MetadataElement.IDENTIFIER, resource.getOriginalId().get());
	message.setUserBond(reducedBond);
	reducedDiscoveryMessage = new ReducedDiscoveryMessage(message, reducedBond);
	result = executor.retrieve(reducedDiscoveryMessage, null);

	// waits the executor service of the DistributedQueryExecutor which stores the YP
	Thread.sleep(5000);

	Assert.assertTrue(result.getResultsList().isEmpty());
	countSet = executor.count(reducedDiscoveryMessage);
	Assert.assertNotNull(countSet);
	Assert.assertEquals(0, countSet.getValue().getCount());
    }

    @Test
    public void simpleTest() throws GSException {
	
	DistributedQueryExecutor executor = createExecutor();

	Assert.assertEquals(Type.DISTRIBUTED, executor.getType());
    }

    @Test
    public void testException1() throws GSException {
	
	DiscoveryMessage message = new DiscoveryMessage();
	ReducedDiscoveryMessage rdm = new ReducedDiscoveryMessage(message, null);
	thrown.expect(GSException.class);

	DistributedQueryExecutor executor = createExecutor();

	executor.setIdentifierDecorator(null);
	executor.retrieve(rdm, null);
    }

    @Before
    public void before() throws Exception {

	DefaultConfiguration defaultConfiguration = new DefaultConfiguration();
	defaultConfiguration.clean();
	
	ConfigurationWrapper.setConfiguration(defaultConfiguration);
    }
}
