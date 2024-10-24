package eu.essi_lab.accessor.socat.test;

import java.util.List;
import java.util.ServiceLoader;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.accessor.socat.SOCATAccessor;
import eu.essi_lab.accessor.socat.SOCATConnector;
import eu.essi_lab.adk.AccessorFactory;
import eu.essi_lab.adk.AccessorFactory.LookupPolicy;
import eu.essi_lab.adk.harvest.IHarvestedAccessor;
import eu.essi_lab.cdk.harvest.IHarvestedQueryConnector;
import eu.essi_lab.cfga.Configurable;
import eu.essi_lab.lib.utils.StreamUtils;

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

	    Assert.assertEquals(SOCATAccessor.class, disAccessors.get(0).getClass());
	}
	{
	    List<IHarvestedAccessor> disAccessors = AccessorFactory.getHarvestedAccessors(LookupPolicy.ALL);

	    Assert.assertEquals(1, disAccessors.size());

	    Assert.assertEquals(SOCATAccessor.class, disAccessors.get(0).getClass());
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

	Assert.assertEquals(SOCATConnector.class, loader.iterator().next().getClass());
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void configurableTest() {

	ServiceLoader<Configurable> loader = ServiceLoader.load(Configurable.class);

	Assert.assertTrue(
		StreamUtils.iteratorToStream(loader.iterator()).filter(c -> c.getClass().getName().equals(SOCATAccessor.class.getName())).//
			findFirst().//
			isPresent());

	Assert.assertTrue(
		StreamUtils.iteratorToStream(loader.iterator()).filter(c -> c.getClass().getName().equals(SOCATConnector.class.getName())).//
			findFirst().//
			isPresent());

    }

}
