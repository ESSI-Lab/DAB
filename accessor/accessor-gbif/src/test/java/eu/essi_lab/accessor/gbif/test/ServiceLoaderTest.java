package eu.essi_lab.accessor.gbif.test;

import java.util.List;
import java.util.ServiceLoader;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.accessor.gbif.distributed.GBIFDistributedConnector;
import eu.essi_lab.accessor.gbif.distributed.GBIFMapper;
import eu.essi_lab.accessor.gbif.distributed.GBIFMixedDistributedAccessor;
import eu.essi_lab.accessor.gbif.harvested.GBIFCollectionMapper;
import eu.essi_lab.accessor.gbif.harvested.GBIFHarvestedConnector;
import eu.essi_lab.accessor.gbif.harvested.GBIFMixedHarvestedAccessor;
import eu.essi_lab.accessor.oaipmh.OAIPMHAccessor;
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

	    Assert.assertEquals(GBIFMixedDistributedAccessor.class, accessors.get(0).getClass());

	    Assert.assertEquals(GBIFMixedDistributedAccessor.ACCESSOR_TYPE, accessors.get(0).getType());

	}

	{
	    List<IDistributedAccessor> accessors = AccessorFactory.getDistributedAccessors(LookupPolicy.ALL);

	    Assert.assertEquals(1, accessors.size());

	    Assert.assertEquals(GBIFMixedDistributedAccessor.class, accessors.get(0).getClass());
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

	    Assert.assertEquals(GBIFMixedHarvestedAccessor.class, accessors.get(0).getClass());

	    Assert.assertEquals(GBIFMixedHarvestedAccessor.ACCESSOR_TYPE, accessors.get(0).getType());
	}

	{
	    List<IHarvestedAccessor> accessors = AccessorFactory.getHarvestedAccessors(LookupPolicy.ALL);
	    accessors.sort((a1, a2) -> a1.getClass().getName().compareTo(a2.getClass().getName()));

	    Assert.assertEquals(2, accessors.size());

	    Assert.assertEquals(GBIFMixedHarvestedAccessor.class, accessors.get(0).getClass());
	    Assert.assertEquals(OAIPMHAccessor.class, accessors.get(1).getClass());
	}

	{
	    List<IHarvestedAccessor> accessors = AccessorFactory.getHarvestedAccessors(LookupPolicy.SPECIFIC);

	    Assert.assertEquals(1, accessors.size());

	    Assert.assertEquals(OAIPMHAccessor.class, accessors.get(0).getClass());
	}
    }

    @SuppressWarnings("rawtypes")
    // @Test
    public void distributedAccessorFactoryTest() {

	GSSource gsSource = new GSSource();
	gsSource.setEndpoint("http://api.gbif.org/v1/");
	gsSource.setBrokeringStrategy(BrokeringStrategy.DISTRIBUTED);

	// GBIFDistributedConnector.supports(source) always return false
	IDistributedAccessor accessor = AccessorFactory.getDistributedAccessor(gsSource).get();

	Assert.assertEquals(GBIFMixedDistributedAccessor.class, accessor.getClass());
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void harvestedAccessorFactoryTest() throws GSException {

	GSSource gsSource = new GSSource();
	gsSource.setEndpoint("http://api.gbif.org/v1/");
	gsSource.setBrokeringStrategy(BrokeringStrategy.HARVESTED);

	IHarvestedAccessor accessor = AccessorFactory.getHarvestedAccessor(gsSource).get();

	Assert.assertEquals(GBIFMixedHarvestedAccessor.class, accessor.getClass());
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void distributedQueryConnectorTest() {

	ServiceLoader<IDistributedQueryConnector> loader = ServiceLoader.load(IDistributedQueryConnector.class);

	Assert.assertEquals(GBIFDistributedConnector.class, loader.iterator().next().getClass());
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void harvestedQueryConnectorTest() {

	ServiceLoader<IHarvestedQueryConnector> loader = ServiceLoader.load(IHarvestedQueryConnector.class);

	Assert.assertEquals(GBIFHarvestedConnector.class, loader.iterator().next().getClass());
    }

    @Test
    public void resourceMapperTest() {

	ServiceLoader<IResourceMapper> loader = ServiceLoader.load(IResourceMapper.class);

	Assert.assertTrue(StreamUtils.iteratorToStream(loader.iterator()).//
		filter(c -> c.getSupportedOriginalMetadataSchema().equals(GBIFCollectionMapper.GBIF_COLLECTION_MAPPER_SCHEME_URI))
		.findFirst().isPresent());

	Assert.assertTrue(StreamUtils.iteratorToStream(loader.iterator()).//
		filter(c -> c.getSupportedOriginalMetadataSchema().equals(GBIFMapper.GBIFOCCURRENCE_SCHEMA)).findFirst().isPresent());

    }

    @SuppressWarnings("rawtypes")
    @Test
    public void configurableTest() {

	ServiceLoader<Configurable> loader = ServiceLoader.load(Configurable.class);

	Assert.assertTrue(StreamUtils.iteratorToStream(loader.iterator())
		.filter(c -> c.getClass().getName().equals(GBIFDistributedConnector.class.getName())).//
		findFirst().//
		isPresent());

	Assert.assertTrue(StreamUtils.iteratorToStream(loader.iterator())
		.filter(c -> c.getClass().getName().equals(GBIFMixedDistributedAccessor.class.getName())).//
		findFirst().//
		isPresent());

	Assert.assertTrue(StreamUtils.iteratorToStream(loader.iterator())
		.filter(c -> c.getClass().getName().equals(GBIFHarvestedConnector.class.getName())).//
		findFirst().//
		isPresent());

	Assert.assertTrue(StreamUtils.iteratorToStream(loader.iterator())
		.filter(c -> c.getClass().getName().equals(GBIFMixedHarvestedAccessor.class.getName())).//
		findFirst().//
		isPresent());

    }

}
