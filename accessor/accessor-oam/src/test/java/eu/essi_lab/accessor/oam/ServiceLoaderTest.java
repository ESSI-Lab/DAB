package eu.essi_lab.accessor.oam;

import java.util.List;
import java.util.ServiceLoader;

import org.junit.Assert;
import org.junit.Test;

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

	    List<IHarvestedAccessor> disAccessors = AccessorFactory.getHarvestedAccessors(LookupPolicy.SPECIFIC);

	    Assert.assertEquals(1, disAccessors.size());

	    Assert.assertEquals(OAMAccessor.class, disAccessors.get(0).getClass());
	}
	{
	    List<IHarvestedAccessor> disAccessors = AccessorFactory.getHarvestedAccessors(LookupPolicy.ALL);

	    Assert.assertEquals(1, disAccessors.size());

	    Assert.assertEquals(OAMAccessor.class, disAccessors.get(0).getClass());
	}

	{
	    List<IHarvestedAccessor> disAccessors = AccessorFactory.getHarvestedAccessors(LookupPolicy.MIXED);

	    Assert.assertEquals(0, disAccessors.size());
	}
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void harvestedQueryConnectorTest() {

	ServiceLoader<IHarvestedQueryConnector> loader = ServiceLoader.load(IHarvestedQueryConnector.class);

	Assert.assertEquals(OAMConnector.class, loader.iterator().next().getClass());
    }

    @Test
    public void resourceMapperTest() {

	ServiceLoader<IResourceMapper> loader = ServiceLoader.load(IResourceMapper.class);

	Assert.assertTrue(StreamUtils.iteratorToStream(loader.iterator()).//
		filter(c -> c.getSupportedOriginalMetadataSchema().equals(OAMMapper.OAM_SCHEMA_URI)).//
		findFirst().//
		isPresent());

    }

    @SuppressWarnings("rawtypes")
    @Test
    public void configurableTest() {

	ServiceLoader<Configurable> loader = ServiceLoader.load(Configurable.class);

	Assert.assertTrue(
		StreamUtils.iteratorToStream(loader.iterator()).filter(c -> c.getClass().getName().equals(OAMAccessor.class.getName())).//
			findFirst().//
			isPresent());

	Assert.assertTrue(
		StreamUtils.iteratorToStream(loader.iterator()).filter(c -> c.getClass().getName().equals(OAMConnector.class.getName())).//
			findFirst().//
			isPresent());

    }

}