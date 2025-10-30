package eu.essi_lab.accessor.ana;

import java.util.List;
import java.util.ServiceLoader;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.accessor.ana.sar.ANASARAccessor;
import eu.essi_lab.accessor.ana.sar.ANASARConnector;
import eu.essi_lab.adk.AccessorFactory;
import eu.essi_lab.adk.AccessorFactory.LookupPolicy;
import eu.essi_lab.adk.harvest.IHarvestedAccessor;
import eu.essi_lab.cdk.harvest.IHarvestedQueryConnector;
import eu.essi_lab.cfga.Configurable;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
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

	    Assert.assertEquals(2, accessors.size());

	    Assert.assertEquals(ANAAccessor.class, accessors.get(0).getClass());
	    Assert.assertEquals(ANASARAccessor.class, accessors.get(1).getClass());

	}

	{
	    List<IHarvestedAccessor> accessors = AccessorFactory.getHarvestedAccessors(LookupPolicy.ALL);

	    accessors.sort((a1, a2) -> a1.getClass().getName().compareTo(a2.getClass().getName()));

	    Assert.assertEquals(2, accessors.size());

	    Assert.assertEquals(ANAAccessor.class, accessors.get(0).getClass());
	    Assert.assertEquals(ANASARAccessor.class, accessors.get(1).getClass());
	}

	{
	    List<IHarvestedAccessor> accessors = AccessorFactory.getHarvestedAccessors(LookupPolicy.MIXED);

	    Assert.assertEquals(0, accessors.size());
	}
    }

    @Test
    public void harvestedQueryConnectorTest() {

	long count = StreamUtils.iteratorToStream(ServiceLoader.load(IHarvestedQueryConnector.class).iterator()).count();
	Assert.assertEquals(2, count);

	Assert.assertTrue(//
		StreamUtils.iteratorToStream(ServiceLoader.load(IHarvestedQueryConnector.class).iterator())
			.anyMatch(c -> c.getClass().equals(ANAConnector.class)));//

	Assert.assertTrue(//
		StreamUtils.iteratorToStream(ServiceLoader.load(IHarvestedQueryConnector.class).iterator())
			.anyMatch(c -> c.getClass().equals(ANASARConnector.class)));//

    }

    @Test
    public void resourceMapperTest() {

	ServiceLoader<IResourceMapper> loader = ServiceLoader.load(IResourceMapper.class);

	Assert.assertTrue(StreamUtils.iteratorToStream(loader.iterator()).//
		anyMatch(c -> c.getSupportedOriginalMetadataSchema().equals(CommonNameSpaceContext.ANA_SAR_URI)));

	Assert.assertTrue(StreamUtils.iteratorToStream(loader.iterator()).//
		anyMatch(c -> c.getSupportedOriginalMetadataSchema().equals(CommonNameSpaceContext.ANA_URI)));

    }

    @SuppressWarnings("rawtypes")
    @Test
    public void configurableTest() {

	ServiceLoader<Configurable> loader = ServiceLoader.load(Configurable.class);

	Assert.assertTrue(StreamUtils.iteratorToStream(loader.iterator()).//
		anyMatch(c -> c.getClass().getName().equals(ANAAccessor.class.getName())));

	Assert.assertTrue(//
		StreamUtils.iteratorToStream(loader.iterator()).//
			anyMatch(c -> c.getClass().getName().equals(ANASARAccessor.class.getName())));

	Assert.assertTrue(//
		StreamUtils.iteratorToStream(loader.iterator()).//
			anyMatch(c -> c.getClass().getName().equals(ANAConnector.class.getName())));

	Assert.assertTrue(//
		StreamUtils.iteratorToStream(loader.iterator()).//
			anyMatch(c -> c.getClass().getName().equals(ANASARConnector.class.getName())));
    }

}
