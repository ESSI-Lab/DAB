package eu.essi_lab.accessor.fedeo.test;

import java.util.List;
import java.util.ServiceLoader;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.accessor.csw.CSWAccessor;
import eu.essi_lab.accessor.fedeo.distributed.FEDEOGranulesConnector;
import eu.essi_lab.accessor.fedeo.distributed.FEDEOGranulesMetadataSchemas;
import eu.essi_lab.accessor.fedeo.distributed.FEDEOMixedDistributedAccessor;
import eu.essi_lab.accessor.fedeo.harvested.FEDEOCollectionConnector;
import eu.essi_lab.accessor.fedeo.harvested.FEDEOCollectionMapper;
import eu.essi_lab.accessor.fedeo.harvested.FEDEOMixedHarvestedAccessor;
import eu.essi_lab.accessor.thredds.THREDDSAccessor;
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

	    Assert.assertEquals(FEDEOMixedDistributedAccessor.class, accessors.get(0).getClass());

	    Assert.assertEquals(FEDEOMixedDistributedAccessor.ACCESSOR_TYPE, accessors.get(0).getType());

	}

	{
	    List<IDistributedAccessor> accessors = AccessorFactory.getDistributedAccessors(LookupPolicy.ALL);

	    Assert.assertEquals(1, accessors.size());

	    Assert.assertEquals(FEDEOMixedDistributedAccessor.class, accessors.get(0).getClass());
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

	    Assert.assertEquals(FEDEOMixedHarvestedAccessor.class, accessors.get(0).getClass());

	    Assert.assertEquals(FEDEOMixedHarvestedAccessor.ACCESSOR_TYPE, accessors.get(0).getType());
	}

	{
	    List<IHarvestedAccessor> accessors = AccessorFactory.getHarvestedAccessors(LookupPolicy.ALL);
	    accessors.sort((a1,a2) -> a1.getSetting().getName().compareTo(a2.getSetting().getName()));
	    
	    Assert.assertEquals(3, accessors.size());

	    Assert.assertEquals(CSWAccessor.class, accessors.get(0).getClass());
	    Assert.assertEquals(FEDEOMixedHarvestedAccessor.class, accessors.get(1).getClass());
	    Assert.assertEquals(THREDDSAccessor.class, accessors.get(2).getClass());
	}

	{
	    List<IHarvestedAccessor> accessors = AccessorFactory.getHarvestedAccessors(LookupPolicy.SPECIFIC);
	    accessors.sort((a1,a2) -> a1.getSetting().getName().compareTo(a2.getSetting().getName()));

	    Assert.assertEquals(2, accessors.size());
	    
	    Assert.assertEquals(CSWAccessor.class, accessors.get(0).getClass());
	    Assert.assertEquals(THREDDSAccessor.class, accessors.get(1).getClass());
	}
    }

    @SuppressWarnings("rawtypes")
    // @Test
    public void distributedAccessorFactoryTest() {

	GSSource gsSource = new GSSource();
	gsSource.setEndpoint("http://fedeo.ceos.org/opensearch");
	gsSource.setBrokeringStrategy(BrokeringStrategy.DISTRIBUTED);

	// FEDEOGranulesConnector.supports(source) always return false
	IDistributedAccessor accessor = AccessorFactory.getDistributedAccessor(gsSource).get();

	Assert.assertEquals(FEDEOMixedDistributedAccessor.class, accessor.getClass());
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void harvestedAccessorFactoryTest() throws GSException {

	GSSource gsSource = new GSSource();
	gsSource.setEndpoint("http://fedeo.ceos.org/opensearch");
	gsSource.setBrokeringStrategy(BrokeringStrategy.HARVESTED);

	IHarvestedAccessor accessor = AccessorFactory.getHarvestedAccessor(gsSource).get();

	Assert.assertEquals(FEDEOMixedHarvestedAccessor.class, accessor.getClass());
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void distributedQueryConnectorTest() {

	ServiceLoader<IDistributedQueryConnector> loader = ServiceLoader.load(IDistributedQueryConnector.class);

	Assert.assertEquals(FEDEOGranulesConnector.class, loader.iterator().next().getClass());
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void harvestedQueryConnectorTest() {

	ServiceLoader<IHarvestedQueryConnector> loader = ServiceLoader.load(IHarvestedQueryConnector.class);

	Assert.assertEquals(FEDEOCollectionConnector.class, loader.iterator().next().getClass());
    }

    @Test
    public void resourceMapperTest() {

	ServiceLoader<IResourceMapper> loader = ServiceLoader.load(IResourceMapper.class);

	Assert.assertTrue(StreamUtils.iteratorToStream(loader.iterator()).//
		filter(c -> c.getSupportedOriginalMetadataSchema().equals(FEDEOCollectionMapper.SCHEMA_URI)).findFirst().isPresent());

	Assert.assertTrue(StreamUtils.iteratorToStream(loader.iterator()).//
		filter(c -> c.getSupportedOriginalMetadataSchema().equals(FEDEOGranulesMetadataSchemas.ATOM_ENTRY_FEDEO.toString()))
		.findFirst().isPresent());

    }

    @SuppressWarnings("rawtypes")
    @Test
    public void configurableTest() {

	ServiceLoader<Configurable> loader = ServiceLoader.load(Configurable.class);

	Assert.assertTrue(StreamUtils.iteratorToStream(loader.iterator())
		.filter(c -> c.getClass().getName().equals(FEDEOGranulesConnector.class.getName())).//
		findFirst().//
		isPresent());

	Assert.assertTrue(StreamUtils.iteratorToStream(loader.iterator())
		.filter(c -> c.getClass().getName().equals(FEDEOMixedDistributedAccessor.class.getName())).//
		findFirst().//
		isPresent());

	Assert.assertTrue(StreamUtils.iteratorToStream(loader.iterator())
		.filter(c -> c.getClass().getName().equals(FEDEOCollectionConnector.class.getName())).//
		findFirst().//
		isPresent());

	Assert.assertTrue(StreamUtils.iteratorToStream(loader.iterator())
		.filter(c -> c.getClass().getName().equals(FEDEOMixedHarvestedAccessor.class.getName())).//
		findFirst().//
		isPresent());

    }

}
