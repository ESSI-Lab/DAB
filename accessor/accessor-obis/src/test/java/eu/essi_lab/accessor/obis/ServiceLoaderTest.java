package eu.essi_lab.accessor.obis;

import java.util.List;
import java.util.ServiceLoader;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.accessor.obis.distributed.OBISGranulesConnector;
import eu.essi_lab.accessor.obis.distributed.OBISGranulesResultMapper;
import eu.essi_lab.accessor.obis.distributed.OBISMixedDistributedAccessor;
import eu.essi_lab.accessor.obis.harvested.OBISConnector;
import eu.essi_lab.accessor.obis.harvested.OBISMixedHarvestedAccessor;
import eu.essi_lab.accessor.obis.harvested.OBISResourceMapper;
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

	    Assert.assertEquals(OBISMixedDistributedAccessor.class, accessors.get(0).getClass());

	    Assert.assertEquals(OBISMixedDistributedAccessor.TYPE, accessors.get(0).getType());

	}

	{
	    List<IDistributedAccessor> accessors = AccessorFactory.getDistributedAccessors(LookupPolicy.ALL);

	    Assert.assertEquals(1, accessors.size());

	    Assert.assertEquals(OBISMixedDistributedAccessor.class, accessors.get(0).getClass());
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

	    Assert.assertEquals(OBISMixedHarvestedAccessor.class, accessors.get(0).getClass());

	    Assert.assertEquals(OBISMixedHarvestedAccessor.TYPE, accessors.get(0).getType());
	}

	{
	    List<IHarvestedAccessor> accessors = AccessorFactory.getHarvestedAccessors(LookupPolicy.ALL);
	    accessors.sort((a1, a2) -> a1.getClass().getName().compareTo(a2.getClass().getName()));

	    Assert.assertEquals(1, accessors.size());

	    Assert.assertEquals(OBISMixedHarvestedAccessor.class, accessors.get(0).getClass());
	}

	{
	    List<IHarvestedAccessor> accessors = AccessorFactory.getHarvestedAccessors(LookupPolicy.SPECIFIC);

	    Assert.assertEquals(0, accessors.size());
	}
    }

    @SuppressWarnings("rawtypes")
    // @Test
    public void distributedAccessorFactoryTest() {

	GSSource gsSource = new GSSource();
	gsSource.setEndpoint("http://api.iobis.org");
	gsSource.setBrokeringStrategy(BrokeringStrategy.DISTRIBUTED);

	// OBISGranulesConnector.supports(source) always return false
	IDistributedAccessor accessor = AccessorFactory.getDistributedAccessor(gsSource).get();

	Assert.assertEquals(OBISMixedDistributedAccessor.class, accessor.getClass());
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void harvestedAccessorFactoryTest() throws GSException {

	GSSource gsSource = new GSSource();
	gsSource.setEndpoint("http://api.iobis.org");
	gsSource.setBrokeringStrategy(BrokeringStrategy.HARVESTED);

	IHarvestedAccessor accessor = AccessorFactory.getHarvestedAccessor(gsSource).get();

	Assert.assertEquals(OBISMixedHarvestedAccessor.class, accessor.getClass());
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void distributedQueryConnectorTest() {

	ServiceLoader<IDistributedQueryConnector> loader = ServiceLoader.load(IDistributedQueryConnector.class);

	Assert.assertEquals(OBISGranulesConnector.class, loader.iterator().next().getClass());
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void harvestedQueryConnectorTest() {

	ServiceLoader<IHarvestedQueryConnector> loader = ServiceLoader.load(IHarvestedQueryConnector.class);

	Assert.assertEquals(OBISConnector.class, loader.iterator().next().getClass());
    }

    @Test
    public void resourceMapperTest() {

	ServiceLoader<IResourceMapper> loader = ServiceLoader.load(IResourceMapper.class);

	Assert.assertTrue(StreamUtils.iteratorToStream(loader.iterator()).//
		anyMatch(c -> c.getSupportedOriginalMetadataSchema().equals(OBISGranulesResultMapper.OBIS_GRANULES_SCHEME_URI)));

	Assert.assertTrue(StreamUtils.iteratorToStream(loader.iterator()).//
		anyMatch(c -> c.getSupportedOriginalMetadataSchema().equals(OBISResourceMapper.OBIS_SCHEME_URI)));

    }

    @SuppressWarnings("rawtypes")
    @Test
    public void configurableTest() {

	ServiceLoader<Configurable> loader = ServiceLoader.load(Configurable.class);

	Assert.assertTrue(StreamUtils.iteratorToStream(loader.iterator())
		.anyMatch(c -> c.getClass().getName().equals(OBISGranulesConnector.class.getName())));

	Assert.assertTrue(StreamUtils.iteratorToStream(loader.iterator())
		.anyMatch(c -> c.getClass().getName().equals(OBISMixedDistributedAccessor.class.getName())));

	Assert.assertTrue(
		StreamUtils.iteratorToStream(loader.iterator()).anyMatch(c -> c.getClass().getName().equals(OBISConnector.class.getName())));

	Assert.assertTrue(StreamUtils.iteratorToStream(loader.iterator())
		.anyMatch(c -> c.getClass().getName().equals(OBISMixedHarvestedAccessor.class.getName())));

    }

}
