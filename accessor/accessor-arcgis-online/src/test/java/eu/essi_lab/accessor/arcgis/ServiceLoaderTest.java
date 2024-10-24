package eu.essi_lab.accessor.arcgis;

import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.adk.AccessorFactory;
import eu.essi_lab.adk.AccessorFactory.LookupPolicy;
import eu.essi_lab.adk.distributed.IDistributedAccessor;
import eu.essi_lab.cdk.query.IDistributedQueryConnector;
import eu.essi_lab.cfga.Configurable;
import eu.essi_lab.lib.utils.StreamUtils;
import eu.essi_lab.model.BrokeringStrategy;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.ommdk.IResourceMapper;

/**
 * @author Fabrizio
 */
public class ServiceLoaderTest {

    @SuppressWarnings("rawtypes")
    @Test
    public void accessorLoaderTest() {

	{
	    List<IDistributedAccessor> disAccessors = AccessorFactory.getDistributedAccessors(LookupPolicy.SPECIFIC);

	    Assert.assertEquals(1, disAccessors.size());

	    Assert.assertEquals(AGOLAccessor.class, disAccessors.get(0).getClass());
	}
	{
	    List<IDistributedAccessor> disAccessors = AccessorFactory.getDistributedAccessors(LookupPolicy.ALL);

	    Assert.assertEquals(1, disAccessors.size());

	    Assert.assertEquals(AGOLAccessor.class, disAccessors.get(0).getClass());

	}

	{
	    List<IDistributedAccessor> disAccessors = AccessorFactory.getDistributedAccessors(LookupPolicy.MIXED);

	    Assert.assertEquals(0, disAccessors.size());
	}
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void accessorFactoryTest() {

	GSSource gsSource = new GSSource();
	gsSource.setEndpoint("http://www.arcgis.com/sharing/rest/search");
	gsSource.setBrokeringStrategy(BrokeringStrategy.DISTRIBUTED);

	IDistributedAccessor accessor = AccessorFactory.getDistributedAccessor(gsSource).get();

	Assert.assertEquals(AGOLAccessor.class, accessor.getClass());

	gsSource.setEndpoint("http://");
	Optional<IDistributedAccessor> optional = AccessorFactory.getDistributedAccessor(gsSource);
	Assert.assertFalse(optional.isPresent());
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void queryConnectorTest() {

	ServiceLoader<IDistributedQueryConnector> loader = ServiceLoader.load(IDistributedQueryConnector.class);

	Assert.assertEquals(AGOLConnector.class, loader.iterator().next().getClass());
    }

    @Test
    public void resourceMapperTest() {

	ServiceLoader<IResourceMapper> loader = ServiceLoader.load(IResourceMapper.class);

	Assert.assertEquals(AGOLMapper.class, loader.iterator().next().getClass());
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void configurableTest() {

	ServiceLoader<Configurable> loader = ServiceLoader.load(Configurable.class);

	Assert.assertTrue(
		StreamUtils.iteratorToStream(loader.iterator()).filter(c -> c.getClass().getName().equals(AGOLAccessor.class.getName())).//
			findFirst().//
			isPresent());

	Assert.assertTrue(
		StreamUtils.iteratorToStream(loader.iterator()).filter(c -> c.getClass().getName().equals(AGOLConnector.class.getName())).//
			findFirst().//
			isPresent());

    }

}
