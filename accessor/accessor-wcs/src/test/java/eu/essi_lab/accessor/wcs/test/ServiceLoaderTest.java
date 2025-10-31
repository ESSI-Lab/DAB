package eu.essi_lab.accessor.wcs.test;

import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.accessor.wcs.WCSAccessor;
import eu.essi_lab.accessor.wcs.WCSConnector;
import eu.essi_lab.accessor.wcs.WCSConnectorWrapper;
import eu.essi_lab.accessor.wcs_1_0_0.WCSConnector_100;
import eu.essi_lab.accessor.wcs_1_0_0_TDS.WCSConnector_100_TDS;
import eu.essi_lab.accessor.wcs_1_1_0.WCSConnector_110;
import eu.essi_lab.accessor.wcs_1_1_1.WCSConnector_111;
import eu.essi_lab.accessor.wcs_2_0_1.WCSConnector_201;
import eu.essi_lab.adk.AccessorFactory;
import eu.essi_lab.adk.AccessorFactory.LookupPolicy;
import eu.essi_lab.adk.harvest.IHarvestedAccessor;
import eu.essi_lab.cdk.harvest.IHarvestedQueryConnector;
import eu.essi_lab.cfga.Configurable;
import eu.essi_lab.lib.utils.StreamUtils;
import eu.essi_lab.ommdk.IResourceMapper;

/**
 * @author Fabrizio
 */
public class ServiceLoaderTest {

    @SuppressWarnings("rawtypes")
    @Test
    public void accessorLoaderTest() {

	{

	    List<IHarvestedAccessor> accessors = AccessorFactory.getHarvestedAccessors(LookupPolicy.SPECIFIC);

	    accessors.sort((a1, a2) -> a1.getClass().getName().compareTo(a2.getClass().getName()));

	    Assert.assertEquals(1, accessors.size());

	    Assert.assertEquals(WCSAccessor.class, accessors.getFirst().getClass());
	}

	{
	    List<IHarvestedAccessor> accessors = AccessorFactory.getHarvestedAccessors(LookupPolicy.ALL);

	    accessors.sort((a1, a2) -> a1.getClass().getName().compareTo(a2.getClass().getName()));

	    Assert.assertEquals(1, accessors.size());

	    Assert.assertEquals(WCSAccessor.class, accessors.getFirst().getClass());
	}

	{
	    List<IHarvestedAccessor> accessors = AccessorFactory.getHarvestedAccessors(LookupPolicy.MIXED);

	    Assert.assertEquals(0, accessors.size());
	}
    }

    @Test
    public void wrappedConnectorTest() {

	ServiceLoader<WCSConnector> loader = ServiceLoader.load(WCSConnector.class);

	Assert.assertEquals(5, StreamUtils.iteratorToStream(loader.iterator()).count());

	List<String> types = StreamUtils.iteratorToStream(loader.iterator()).map(c -> c.getType()).toList();

	Assert.assertTrue(types.contains(WCSConnector_100.TYPE));
	Assert.assertTrue(types.contains(WCSConnector_100_TDS.TYPE));
	Assert.assertTrue(types.contains(WCSConnector_110.TYPE));
	Assert.assertTrue(types.contains(WCSConnector_111.TYPE));
	Assert.assertTrue(types.contains(WCSConnector_201.TYPE));
    }

    @Test
    public void harvestedQueryConnectorTest() {

	long count = StreamUtils.iteratorToStream(ServiceLoader.load(IHarvestedQueryConnector.class).iterator()).count();
	Assert.assertEquals(1, count);

	Assert.assertTrue(//
		StreamUtils.iteratorToStream(ServiceLoader.load(IHarvestedQueryConnector.class).iterator())
			.anyMatch(c -> c.getClass().equals(WCSConnectorWrapper.class)));//

    }

    @Test
    public void resourceMapperTest() {

	ServiceLoader<IResourceMapper> loader = ServiceLoader.load(IResourceMapper.class);

	Assert.assertTrue(StreamUtils.iteratorToStream(loader.iterator()).//
		anyMatch(c -> c.getSupportedOriginalMetadataSchema().equals(WCSConnector.WCS_SCHEME + WCSConnector_100.class.getSimpleName())));

	Assert.assertTrue(StreamUtils.iteratorToStream(loader.iterator()).//
		anyMatch(c -> c.getSupportedOriginalMetadataSchema().equals(WCSConnector.WCS_SCHEME + WCSConnector_110.class.getSimpleName())));

	Assert.assertTrue(StreamUtils.iteratorToStream(loader.iterator()).//
		anyMatch(c -> c.getSupportedOriginalMetadataSchema().equals(WCSConnector.WCS_SCHEME + WCSConnector_111.class.getSimpleName())));

    }

    @SuppressWarnings("rawtypes")
    @Test
    public void configurableTest() {

	ServiceLoader<Configurable> loader = ServiceLoader.load(Configurable.class);

	Assert.assertTrue(
		StreamUtils.iteratorToStream(loader.iterator()).anyMatch(c -> c.getClass().getName().equals(WCSAccessor.class.getName())));

	Assert.assertTrue(StreamUtils.iteratorToStream(loader.iterator())
		.anyMatch(c -> c.getClass().getName().equals(WCSConnectorWrapper.class.getName())));
    }

}
