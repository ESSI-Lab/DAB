package eu.essi_lab.accessor.aviso.test;

import java.util.List;
import java.util.ServiceLoader;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.accessor.aviso.AVISOAccessor;
import eu.essi_lab.accessor.aviso.AVISOConnector;
import eu.essi_lab.accessor.aviso.AVISOMapper;
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

	    Assert.assertEquals(AVISOAccessor.class, accessors.get(0).getClass());

	}

	{
	    List<IHarvestedAccessor> accessors = AccessorFactory.getHarvestedAccessors(LookupPolicy.ALL);

	    accessors.sort((a1, a2) -> a1.getClass().getName().compareTo(a2.getClass().getName()));

	    Assert.assertEquals(1, accessors.size());

	    Assert.assertEquals(AVISOAccessor.class, accessors.get(0).getClass());
	}

	{
	    List<IHarvestedAccessor> accessors = AccessorFactory.getHarvestedAccessors(LookupPolicy.MIXED);

	    Assert.assertEquals(0, accessors.size());
	}
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void harvestedQueryConnectorTest() {

	ServiceLoader<IHarvestedQueryConnector> loader = ServiceLoader.load(IHarvestedQueryConnector.class);

	Assert.assertEquals(1, StreamUtils.iteratorToStream(loader.iterator()).count());

	Assert.assertTrue(//
		StreamUtils
			.iteratorToStream(//
				loader.iterator())
			.anyMatch(c -> c.getClass().equals(AVISOConnector.class)));//
    }

    @Test
    public void resourceMapperTest() {

	ServiceLoader<IResourceMapper> loader = ServiceLoader.load(IResourceMapper.class);

	Assert.assertTrue(StreamUtils.iteratorToStream(loader.iterator()).//
		anyMatch(c -> c.getSupportedOriginalMetadataSchema().equals(AVISOMapper.METADATA_SCHEMA)));

    }

    @SuppressWarnings("rawtypes")
    @Test
    public void configurableTest() {

	ServiceLoader<Configurable> loader = ServiceLoader.load(Configurable.class);

	Assert.assertTrue(
		StreamUtils.iteratorToStream(loader.iterator()).anyMatch(c -> c.getClass().getName().equals(AVISOAccessor.class.getName())));

	Assert.assertTrue(
		StreamUtils.iteratorToStream(loader.iterator()).anyMatch(c -> c.getClass().getName().equals(AVISOConnector.class.getName())));

    }
}
