package eu.essi_lab.accessor.trigger;

import java.util.List;
import java.util.ServiceLoader;

import org.junit.Assert;
import org.junit.Test;

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

	    List<Class> classes = StreamUtils.iteratorToStream(disAccessors.iterator()).map(Object::getClass).//
		    collect(java.util.stream.Collectors.toList());

	    Assert.assertTrue(classes.contains(TRIGGERAccessor.class));
	    Assert.assertTrue(classes.contains(AggregatedTRIGGERAccessor.class));
	}
	{
	    List<IHarvestedAccessor> disAccessors = AccessorFactory.getHarvestedAccessors(LookupPolicy.ALL);

	    Assert.assertEquals(2, disAccessors.size());

	    List<Class> classes = StreamUtils.iteratorToStream(disAccessors.iterator()).map(Object::getClass).//
		    collect(java.util.stream.Collectors.toList());

	    Assert.assertTrue(classes.contains(TRIGGERAccessor.class));
	    Assert.assertTrue(classes.contains(AggregatedTRIGGERAccessor.class));
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

	List<Class> classes = StreamUtils.iteratorToStream(loader.iterator()).map(Object::getClass).//
		collect(java.util.stream.Collectors.toList());

	Assert.assertTrue(classes.contains(TRIGGERConnector.class));
	Assert.assertTrue(classes.contains(AggregatedTRIGGERConnector.class));
    }

    @Test
    public void resourceMapperTest() {

	ServiceLoader<IResourceMapper> loader = ServiceLoader.load(IResourceMapper.class);

	Assert.assertTrue(StreamUtils.iteratorToStream(loader.iterator()).//
		filter(c -> c.getSupportedOriginalMetadataSchema().equals(CommonNameSpaceContext.TRIGGER)).//
		findFirst().//
		isPresent());

	Assert.assertTrue(StreamUtils.iteratorToStream(loader.iterator()).//
		filter(c -> c.getSupportedOriginalMetadataSchema().equals(CommonNameSpaceContext.AGGREGATED_TRIGGER)).//
		findFirst().//
		isPresent());

    }

    @SuppressWarnings("rawtypes")
    @Test
    public void configurableTest() {

	ServiceLoader<Configurable> loader = ServiceLoader.load(Configurable.class);

	Assert.assertTrue(StreamUtils.iteratorToStream(loader.iterator())
		.filter(c -> c.getClass().getName().equals(TRIGGERAccessor.class.getName())).//
		findFirst().//
		isPresent());

	Assert.assertTrue(StreamUtils.iteratorToStream(loader.iterator())
		.filter(c -> c.getClass().getName().equals(TRIGGERConnector.class.getName())).//
		findFirst().//
		isPresent());

	Assert.assertTrue(StreamUtils.iteratorToStream(loader.iterator())
		.filter(c -> c.getClass().getName().equals(AggregatedTRIGGERAccessor.class.getName())).//
		findFirst().//
		isPresent());

	Assert.assertTrue(StreamUtils.iteratorToStream(loader.iterator())
		.filter(c -> c.getClass().getName().equals(AggregatedTRIGGERConnector.class.getName())).//
		findFirst().//
		isPresent());

    }

}
