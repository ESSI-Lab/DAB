package eu.essi_lab.accessor.agrostac;

import java.util.List;
import java.util.ServiceLoader;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.accessor.agrostac.distributed.AgrostacGranulesAccessor;
import eu.essi_lab.accessor.agrostac.distributed.AgrostacGranulesConnector;
import eu.essi_lab.accessor.agrostac.distributed.AgrostacGranulesMapper;
import eu.essi_lab.accessor.agrostac.harvested.AgrostacAccessor;
import eu.essi_lab.accessor.agrostac.harvested.AgrostacCollectionMapper;
import eu.essi_lab.accessor.agrostac.harvested.AgrostacConnector;
import eu.essi_lab.adk.AccessorFactory;
import eu.essi_lab.adk.AccessorFactory.LookupPolicy;
import eu.essi_lab.adk.distributed.IDistributedAccessor;
import eu.essi_lab.adk.harvest.IHarvestedAccessor;
import eu.essi_lab.cdk.harvest.IHarvestedQueryConnector;
import eu.essi_lab.cdk.query.IDistributedQueryConnector;
import eu.essi_lab.cfga.Configurable;
import eu.essi_lab.lib.utils.StreamUtils;
import eu.essi_lab.model.BrokeringStrategy;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.ommdk.IResourceMapper;

/**
 * @author Fabrizio
 */
public class ServiceLoaderTest {

    @Test
    public void mixedConfigurationTest() {

	{

	    IDistributedAccessor<?> iDistributedAccessor = AccessorFactory.getDistributedAccessors(LookupPolicy.MIXED).//
		    stream().//
		    findFirst().//
		    get();

	    IHarvestedAccessor<?> iHarvestedAccessor = AccessorFactory.getHarvestedAccessors(LookupPolicy.MIXED).//
		    stream().//
		    findFirst().//
		    get();

	    // accessors must share the same connectors
	    String identifier1 = iDistributedAccessor.getSetting().getDistributedConnectorSetting().getIdentifier();
	    String identifier2 = iHarvestedAccessor.getSetting().getDistributedConnectorSetting().getIdentifier();

	    Assert.assertEquals(identifier1, identifier2);

	    // accessors must share the same connectors
	    String identifier3 = iDistributedAccessor.getSetting().getHarvestedConnectorSetting().getIdentifier();
	    String identifier4 = iHarvestedAccessor.getSetting().getHarvestedConnectorSetting().getIdentifier();

	    Assert.assertEquals(identifier3, identifier4);
	}
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void accessorLoaderTest() {

	{
	    List<IDistributedAccessor> accessors = AccessorFactory.getDistributedAccessors(LookupPolicy.MIXED);

	    Assert.assertEquals(1, accessors.size());

	    Assert.assertEquals(AgrostacGranulesAccessor.class, accessors.getFirst().getClass());

	    Assert.assertEquals(AgrostacGranulesAccessor.TYPE, accessors.getFirst().getType());

	}

	{
	    List<IDistributedAccessor> accessors = AccessorFactory.getDistributedAccessors(LookupPolicy.ALL);

	    Assert.assertEquals(1, accessors.size());

	    Assert.assertEquals(AgrostacGranulesAccessor.class, accessors.getFirst().getClass());
	}

	{
	    List<IDistributedAccessor> accessors = AccessorFactory.getDistributedAccessors(LookupPolicy.SPECIFIC);

	    Assert.assertEquals(0, accessors.size());
	}

	//
	//
	//

	{
	    List<IHarvestedAccessor> accessors = AccessorFactory.getHarvestedAccessors(LookupPolicy.MIXED);

	    Assert.assertEquals(1, accessors.size());

	    Assert.assertEquals(AgrostacAccessor.class, accessors.getFirst().getClass());

	    Assert.assertEquals(AgrostacAccessor.TYPE, accessors.getFirst().getType());
	}

    }

    @SuppressWarnings("rawtypes")
    // @Test
    public void distributedAccessorFactoryTest() {

	GSSource gsSource = new GSSource();
	gsSource.setEndpoint("http://");
	gsSource.setBrokeringStrategy(BrokeringStrategy.DISTRIBUTED);

	// NextGEOSSGranulesConnector.supports(source) always return false
	IDistributedAccessor accessor = AccessorFactory.getDistributedAccessor(gsSource).get();

	Assert.assertEquals(AgrostacGranulesAccessor.class, accessor.getClass());
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void harvestedAccessorFactoryTest() throws GSException {

	GSSource gsSource = new GSSource();
	gsSource.setEndpoint("agrostac-test.containers.wur.nl/agrostac");
	gsSource.setBrokeringStrategy(BrokeringStrategy.HARVESTED);

	IHarvestedAccessor accessor = AccessorFactory.getHarvestedAccessor(gsSource).get();

	Assert.assertEquals(AgrostacAccessor.class, accessor.getClass());
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void distributedQueryConnectorTest() {

	ServiceLoader<IDistributedQueryConnector> loader = ServiceLoader.load(IDistributedQueryConnector.class);

	Assert.assertEquals(AgrostacGranulesConnector.class, loader.iterator().next().getClass());
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void harvestedQueryConnectorTest() {

	ServiceLoader<IHarvestedQueryConnector> loader = ServiceLoader.load(IHarvestedQueryConnector.class);

	Assert.assertEquals(AgrostacConnector.class, loader.iterator().next().getClass());
    }

    @Test
    public void resourceMapperTest() {

	ServiceLoader<IResourceMapper> loader = ServiceLoader.load(IResourceMapper.class);

	Assert.assertTrue(StreamUtils.iteratorToStream(loader.iterator()).//
		anyMatch(c -> c.getSupportedOriginalMetadataSchema().equals(AgrostacGranulesMapper.WORLDCEREAL_GRANULES_SCHEME_URI)));

	Assert.assertTrue(StreamUtils.iteratorToStream(loader.iterator()).//
		anyMatch(c -> c.getSupportedOriginalMetadataSchema().equals(AgrostacCollectionMapper.SCHEMA_URI)));

    }

    @SuppressWarnings("rawtypes")
    @Test
    public void configurableTest() {

	ServiceLoader<Configurable> loader = ServiceLoader.load(Configurable.class);

	Assert.assertTrue(StreamUtils.iteratorToStream(loader.iterator())
		.anyMatch(c -> c.getClass().getName().equals(AgrostacGranulesConnector.class.getName())));

	Assert.assertTrue(StreamUtils.iteratorToStream(loader.iterator())
		.anyMatch(c -> c.getClass().getName().equals(AgrostacGranulesAccessor.class.getName())));

	Assert.assertTrue(StreamUtils.iteratorToStream(loader.iterator())
		.anyMatch(c -> c.getClass().getName().equals(AgrostacConnector.class.getName())));

	Assert.assertTrue(StreamUtils.iteratorToStream(loader.iterator())
		.anyMatch(c -> c.getClass().getName().equals(AgrostacAccessor.class.getName())));

    }

}
