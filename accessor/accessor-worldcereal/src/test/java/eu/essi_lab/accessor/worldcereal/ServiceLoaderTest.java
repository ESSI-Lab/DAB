package eu.essi_lab.accessor.worldcereal;

import java.util.List;
import java.util.ServiceLoader;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.accessor.worldcereal.distributed.WorldCerealGranulesAccessor;
import eu.essi_lab.accessor.worldcereal.distributed.WorldCerealGranulesConnector;
import eu.essi_lab.accessor.worldcereal.distributed.WorldCerealGranulesMapper;
import eu.essi_lab.accessor.worldcereal.harvested.WorldCerealCollectionMapper;
import eu.essi_lab.accessor.worldcereal.harvested.WorldCerealAccessor;
import eu.essi_lab.accessor.worldcereal.harvested.WorldCerealConnector;
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

	    Assert.assertEquals(WorldCerealGranulesAccessor.class, accessors.get(0).getClass());

	    Assert.assertEquals(WorldCerealGranulesAccessor.TYPE, accessors.get(0).getType());

	}

	{
	    List<IDistributedAccessor> accessors = AccessorFactory.getDistributedAccessors(LookupPolicy.ALL);

	    Assert.assertEquals(1, accessors.size());

	    Assert.assertEquals(WorldCerealGranulesAccessor.class, accessors.get(0).getClass());
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

	    Assert.assertEquals(WorldCerealAccessor.class, accessors.get(0).getClass());

	    Assert.assertEquals(WorldCerealAccessor.TYPE, accessors.get(0).getType());
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

	Assert.assertEquals(WorldCerealGranulesAccessor.class, accessor.getClass());
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void harvestedAccessorFactoryTest() throws GSException {

	GSSource gsSource = new GSSource();
	gsSource.setEndpoint("ewoc-rdm-api.iiasa.ac.at/data/");
	gsSource.setBrokeringStrategy(BrokeringStrategy.HARVESTED);

	IHarvestedAccessor accessor = AccessorFactory.getHarvestedAccessor(gsSource).get();

	Assert.assertEquals(WorldCerealAccessor.class, accessor.getClass());
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void distributedQueryConnectorTest() {

	ServiceLoader<IDistributedQueryConnector> loader = ServiceLoader.load(IDistributedQueryConnector.class);

	Assert.assertEquals(WorldCerealGranulesConnector.class, loader.iterator().next().getClass());
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void harvestedQueryConnectorTest() {

	ServiceLoader<IHarvestedQueryConnector> loader = ServiceLoader.load(IHarvestedQueryConnector.class);

	Assert.assertEquals(WorldCerealConnector.class, loader.iterator().next().getClass());
    }

    @Test
    public void resourceMapperTest() {

	ServiceLoader<IResourceMapper> loader = ServiceLoader.load(IResourceMapper.class);

	Assert.assertTrue(StreamUtils.iteratorToStream(loader.iterator()).//
		anyMatch(c -> c.getSupportedOriginalMetadataSchema().equals(WorldCerealGranulesMapper.WORLDCEREAL_GRANULES_SCHEME_URI)));

	Assert.assertTrue(StreamUtils.iteratorToStream(loader.iterator()).//
		anyMatch(c -> c.getSupportedOriginalMetadataSchema().equals(WorldCerealCollectionMapper.SCHEMA_URI)));

    }

    @SuppressWarnings("rawtypes")
    @Test
    public void configurableTest() {

	ServiceLoader<Configurable> loader = ServiceLoader.load(Configurable.class);

	Assert.assertTrue(StreamUtils.iteratorToStream(loader.iterator())
		.anyMatch(c -> c.getClass().getName().equals(WorldCerealGranulesConnector.class.getName())));

	Assert.assertTrue(StreamUtils.iteratorToStream(loader.iterator())
		.anyMatch(c -> c.getClass().getName().equals(WorldCerealGranulesAccessor.class.getName())));

	Assert.assertTrue(StreamUtils.iteratorToStream(loader.iterator())
		.anyMatch(c -> c.getClass().getName().equals(WorldCerealConnector.class.getName())));

	Assert.assertTrue(StreamUtils.iteratorToStream(loader.iterator())
		.anyMatch(c -> c.getClass().getName().equals(WorldCerealAccessor.class.getName())));

    }

}
