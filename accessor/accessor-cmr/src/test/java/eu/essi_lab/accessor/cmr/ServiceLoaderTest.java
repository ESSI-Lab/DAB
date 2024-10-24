package eu.essi_lab.accessor.cmr;

import java.util.List;
import java.util.ServiceLoader;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.accessor.cmr.cwic.distributed.CWICCMRGranulesAccessor;
import eu.essi_lab.accessor.cmr.cwic.distributed.CWICCMRGranulesConnector;
import eu.essi_lab.accessor.cmr.cwic.harvested.CWICCMROpensearchAccessor;
import eu.essi_lab.accessor.cmr.cwic.harvested.CWICCMROpensearchCollectionMapper;
import eu.essi_lab.accessor.cmr.cwic.harvested.CWICCMROpensearchConnector;
import eu.essi_lab.accessor.cmr.distributed.CMRGranulesMapper;
import eu.essi_lab.accessor.cmr.distributed.CMRIDNAccessor;
import eu.essi_lab.accessor.cmr.distributed.CMRIDNGranulesConnector;
import eu.essi_lab.accessor.cmr.harvested.CMRIDNOpensearchAccessor;
import eu.essi_lab.accessor.cmr.harvested.CMRIDNOpensearchCollectionMapper;
import eu.essi_lab.accessor.cmr.harvested.CMRIDNOpensearchConnector;
import eu.essi_lab.accessor.csw.CSWAccessor;
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

	    Assert.assertEquals(2, accessors.size());

	    accessors.sort((a1, a2) -> a1.getClass().getSimpleName().compareTo(a2.getClass().getSimpleName()));

	    Assert.assertEquals(CMRIDNAccessor.class, accessors.get(0).getClass());

	    Assert.assertEquals(CMRIDNAccessor.TYPE, accessors.get(0).getType());

	    Assert.assertEquals(CWICCMRGranulesAccessor.class, accessors.get(1).getClass());

	    Assert.assertEquals(CWICCMRGranulesAccessor.TYPE, accessors.get(1).getType());

	}

	{
	    List<IDistributedAccessor> accessors = AccessorFactory.getDistributedAccessors(LookupPolicy.ALL);

	    Assert.assertEquals(2, accessors.size());

	    accessors.sort((a1, a2) -> a1.getClass().getSimpleName().compareTo(a2.getClass().getSimpleName()));

	    Assert.assertEquals(CMRIDNAccessor.class, accessors.get(0).getClass());

	    Assert.assertEquals(CMRIDNAccessor.TYPE, accessors.get(0).getType());

	    Assert.assertEquals(CWICCMRGranulesAccessor.class, accessors.get(1).getClass());

	    Assert.assertEquals(CWICCMRGranulesAccessor.TYPE, accessors.get(1).getType());
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

	    Assert.assertEquals(2, accessors.size());

	    accessors.sort((a1, a2) -> a1.getClass().getSimpleName().compareTo(a2.getClass().getSimpleName()));

	    Assert.assertEquals(CMRIDNOpensearchAccessor.class, accessors.get(0).getClass());

	    Assert.assertEquals(CMRIDNOpensearchAccessor.TYPE, accessors.get(0).getType());

	    Assert.assertEquals(CWICCMROpensearchAccessor.class, accessors.get(1).getClass());

	    Assert.assertEquals(CWICCMROpensearchAccessor.TYPE, accessors.get(1).getType());
	}

	{
	    List<IHarvestedAccessor> accessors = AccessorFactory.getHarvestedAccessors(LookupPolicy.ALL);

	    Assert.assertEquals(4, accessors.size());

	    accessors.sort((a1, a2) -> a1.getClass().getSimpleName().compareTo(a2.getClass().getSimpleName()));

	    Assert.assertEquals(CMRIDNOpensearchAccessor.class, accessors.get(0).getClass());

	    Assert.assertEquals(CMRIDNOpensearchAccessor.TYPE, accessors.get(0).getType());

	    Assert.assertEquals(CSWAccessor.class, accessors.get(1).getClass());

	    Assert.assertEquals(CSWAccessor.TYPE, accessors.get(1).getType());

	    Assert.assertEquals(CWICCMROpensearchAccessor.class, accessors.get(2).getClass());

	    Assert.assertEquals(CWICCMROpensearchAccessor.TYPE, accessors.get(2).getType());
	    
	    Assert.assertEquals(THREDDSAccessor.class, accessors.get(3).getClass());

	    Assert.assertEquals(THREDDSAccessor.TYPE, accessors.get(3).getType());
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

	// Assert.assertEquals(GBIFMixedDistributedAccessor.class, accessor.getClass());
    }

    @SuppressWarnings("rawtypes")
    // @Test
    public void harvestedAccessorFactoryTest() throws GSException {

	{
	    GSSource gsSource = new GSSource();
	    gsSource.setEndpoint("https://cmr.earthdata.nasa.gov/opensearch/collections.atom?");
	    gsSource.setBrokeringStrategy(BrokeringStrategy.HARVESTED);

	    IHarvestedAccessor accessor = AccessorFactory.getHarvestedAccessor(gsSource).get();

	    //
	    // Both connectors supports the same source
	    //

	    Assert.assertEquals(CMRIDNOpensearchAccessor.class, accessor.getClass());
	    Assert.assertEquals(CWICCMROpensearchAccessor.class, accessor.getClass());
	}
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void distributedQueryConnectorTest() {

	ServiceLoader<IDistributedQueryConnector> loader = ServiceLoader.load(IDistributedQueryConnector.class);

	Assert.assertTrue(StreamUtils.iteratorToStream(loader.iterator()).//
		filter(c -> c.getClass().equals(CMRIDNGranulesConnector.class))//
		.findFirst().isPresent());

	Assert.assertTrue(StreamUtils.iteratorToStream(loader.iterator()).//
		filter(c -> c.getClass().equals(CWICCMRGranulesConnector.class))//
		.findFirst().isPresent());
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void harvestedQueryConnectorTest() {

	ServiceLoader<IHarvestedQueryConnector> loader = ServiceLoader.load(IHarvestedQueryConnector.class);

	Assert.assertTrue(StreamUtils.iteratorToStream(loader.iterator()).//
		filter(c -> c.getClass().equals(CMRIDNOpensearchConnector.class))//
		.findFirst().isPresent());

	Assert.assertTrue(StreamUtils.iteratorToStream(loader.iterator()).//
		filter(c -> c.getClass().equals(CWICCMROpensearchConnector.class))//
		.findFirst().isPresent());
    }

    @Test
    public void resourceMapperTest() {

	ServiceLoader<IResourceMapper> loader = ServiceLoader.load(IResourceMapper.class);

	Assert.assertTrue(StreamUtils.iteratorToStream(loader.iterator()).//
		filter(c -> c.getClass().equals(CWICCMROpensearchCollectionMapper.class)).findFirst().isPresent());

	Assert.assertTrue(StreamUtils.iteratorToStream(loader.iterator()).//
		filter(c -> c.getClass().equals(CMRGranulesMapper.class)).findFirst().isPresent());

	Assert.assertTrue(StreamUtils.iteratorToStream(loader.iterator()).//
		filter(c -> c.getClass().equals(CMRIDNOpensearchCollectionMapper.class)).findFirst().isPresent());

    }

    @SuppressWarnings("rawtypes")
    @Test
    public void configurableTest() {

	ServiceLoader<Configurable> loader = ServiceLoader.load(Configurable.class);

	Assert.assertTrue(StreamUtils.iteratorToStream(loader.iterator()).//
		filter(c -> c.getClass().equals(CMRIDNGranulesConnector.class))//
		.findFirst().isPresent());

	Assert.assertTrue(StreamUtils.iteratorToStream(loader.iterator()).//
		filter(c -> c.getClass().equals(CWICCMRGranulesConnector.class))//
		.findFirst().isPresent());

	Assert.assertTrue(StreamUtils.iteratorToStream(loader.iterator()).//
		filter(c -> c.getClass().equals(CMRIDNOpensearchConnector.class))//
		.findFirst().isPresent());

	Assert.assertTrue(StreamUtils.iteratorToStream(loader.iterator()).//
		filter(c -> c.getClass().equals(CWICCMROpensearchConnector.class))//
		.findFirst().isPresent());

	Assert.assertTrue(StreamUtils.iteratorToStream(loader.iterator()).//
		filter(c -> c.getClass().equals(CMRIDNOpensearchAccessor.class))//
		.findFirst().isPresent());

	Assert.assertTrue(StreamUtils.iteratorToStream(loader.iterator()).//
		filter(c -> c.getClass().equals(CWICCMROpensearchAccessor.class))//
		.findFirst().isPresent());

	Assert.assertTrue(StreamUtils.iteratorToStream(loader.iterator()).//
		filter(c -> c.getClass().equals(CMRIDNAccessor.class))//
		.findFirst().isPresent());

	Assert.assertTrue(StreamUtils.iteratorToStream(loader.iterator()).//
		filter(c -> c.getClass().equals(CWICCMRGranulesAccessor.class))//
		.findFirst().isPresent());

    }

}
