package eu.essi_lab.accessor.oaipmh.test;

import java.util.List;
import java.util.ServiceLoader;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.accessor.oaipmh.OAIPMHAccessor;
import eu.essi_lab.accessor.oaipmh.OAIPMHConnector;
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

	    List<IHarvestedAccessor> disAccessors = AccessorFactory.getHarvestedAccessors(LookupPolicy.SPECIFIC);

	    Assert.assertEquals(2, disAccessors.size());

	    Assert.assertEquals(OAIPMHAccessor.class, disAccessors.get(0).getClass());
	}
	{
	    List<IHarvestedAccessor> disAccessors = AccessorFactory.getHarvestedAccessors(LookupPolicy.ALL);

	    Assert.assertEquals(2, disAccessors.size());

	    Assert.assertEquals(OAIPMHAccessor.class, disAccessors.get(0).getClass());
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

	Assert.assertEquals(OAIPMHConnector.class, loader.iterator().next().getClass());
    }

    @Test
    public void resourceMapperTest() {

	ServiceLoader<IResourceMapper> loader = ServiceLoader.load(IResourceMapper.class);

	Assert.assertTrue(StreamUtils.iteratorToStream(loader.iterator()).//
		filter(c -> c.getSupportedOriginalMetadataSchema().equals(CommonNameSpaceContext.OAI_NS_URI)).//
		findFirst().//
		isPresent());

    }

    @SuppressWarnings("rawtypes")
    @Test
    public void configurableTest() {

	ServiceLoader<Configurable> loader = ServiceLoader.load(Configurable.class);

	Assert.assertTrue(
		StreamUtils.iteratorToStream(loader.iterator()).filter(c -> c.getClass().getName().equals(OAIPMHAccessor.class.getName())).//
			findFirst().//
			isPresent());

	Assert.assertTrue(
		StreamUtils.iteratorToStream(loader.iterator()).filter(c -> c.getClass().getName().equals(OAIPMHConnector.class.getName())).//
			findFirst().//
			isPresent());

    }

}
