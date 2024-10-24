package eu.essi_lab.accessor.nextgeoss;

import java.util.List;
import java.util.ServiceLoader;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.accessor.nextgeoss.distributed.NextGEOSSGranulesAccessor;
import eu.essi_lab.accessor.nextgeoss.distributed.NextGEOSSGranulesConnector;
import eu.essi_lab.accessor.nextgeoss.distributed.NextGEOSSGranulesMetadataSchemas;
import eu.essi_lab.accessor.nextgeoss.harvested.NextGEOSSCollectionMapper;
import eu.essi_lab.accessor.nextgeoss.harvested.NextGEOSSOpenSearchAccessor;
import eu.essi_lab.accessor.nextgeoss.harvested.NextGEOSSOpensearchConnector;
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

	    Assert.assertEquals(NextGEOSSGranulesAccessor.class, accessors.get(0).getClass());

	    Assert.assertEquals(NextGEOSSGranulesAccessor.TYPE, accessors.get(0).getType());

	}

	{
	    List<IDistributedAccessor> accessors = AccessorFactory.getDistributedAccessors(LookupPolicy.ALL);

	    Assert.assertEquals(1, accessors.size());

	    Assert.assertEquals(NextGEOSSGranulesAccessor.class, accessors.get(0).getClass());
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

	    Assert.assertEquals(NextGEOSSOpenSearchAccessor.class, accessors.get(0).getClass());

	    Assert.assertEquals(NextGEOSSOpenSearchAccessor.TYPE, accessors.get(0).getType());
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

	Assert.assertEquals(NextGEOSSGranulesAccessor.class, accessor.getClass());
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void harvestedAccessorFactoryTest() throws GSException {

	GSSource gsSource = new GSSource();
	gsSource.setEndpoint("catalogue.nextgeoss.eu/opensearch");
	gsSource.setBrokeringStrategy(BrokeringStrategy.HARVESTED);

	IHarvestedAccessor accessor = AccessorFactory.getHarvestedAccessor(gsSource).get();

	Assert.assertEquals(NextGEOSSOpenSearchAccessor.class, accessor.getClass());
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void distributedQueryConnectorTest() {

	ServiceLoader<IDistributedQueryConnector> loader = ServiceLoader.load(IDistributedQueryConnector.class);

	Assert.assertEquals(NextGEOSSGranulesConnector.class, loader.iterator().next().getClass());
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void harvestedQueryConnectorTest() {

	ServiceLoader<IHarvestedQueryConnector> loader = ServiceLoader.load(IHarvestedQueryConnector.class);

	Assert.assertEquals(NextGEOSSOpensearchConnector.class, loader.iterator().next().getClass());
    }

    @Test
    public void resourceMapperTest() {

	ServiceLoader<IResourceMapper> loader = ServiceLoader.load(IResourceMapper.class);

	Assert.assertTrue(StreamUtils.iteratorToStream(loader.iterator()).//
		filter(c -> c.getSupportedOriginalMetadataSchema().equals(NextGEOSSGranulesMetadataSchemas.ATOM_ENTRY_NEXTGEOSS.toString()))
		.findFirst().isPresent());

	Assert.assertTrue(StreamUtils.iteratorToStream(loader.iterator()).//
		filter(c -> c.getSupportedOriginalMetadataSchema().equals(NextGEOSSCollectionMapper.SCHEMA_URI)).findFirst().isPresent());

    }

    @SuppressWarnings("rawtypes")
    @Test
    public void configurableTest() {

	ServiceLoader<Configurable> loader = ServiceLoader.load(Configurable.class);

	Assert.assertTrue(StreamUtils.iteratorToStream(loader.iterator())
		.filter(c -> c.getClass().getName().equals(NextGEOSSGranulesConnector.class.getName())).//
		findFirst().//
		isPresent());

	Assert.assertTrue(StreamUtils.iteratorToStream(loader.iterator())
		.filter(c -> c.getClass().getName().equals(NextGEOSSGranulesAccessor.class.getName())).//
		findFirst().//
		isPresent());

	Assert.assertTrue(StreamUtils.iteratorToStream(loader.iterator())
		.filter(c -> c.getClass().getName().equals(NextGEOSSOpensearchConnector.class.getName())).//
		findFirst().//
		isPresent());

	Assert.assertTrue(StreamUtils.iteratorToStream(loader.iterator())
		.filter(c -> c.getClass().getName().equals(NextGEOSSOpenSearchAccessor.class.getName())).//
		findFirst().//
		isPresent());

    }

}
