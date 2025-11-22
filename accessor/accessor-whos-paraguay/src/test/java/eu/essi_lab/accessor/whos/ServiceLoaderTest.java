package eu.essi_lab.accessor.whos;

import java.util.List;
import java.util.ServiceLoader;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.accessor.whos.automatic.AutomaticSystemAccessor;
import eu.essi_lab.accessor.whos.automatic.AutomaticSystemConnector;
import eu.essi_lab.accessor.whos.sigedac.SIGEDACAccessor;
import eu.essi_lab.accessor.whos.sigedac.SIGEDACConnector;
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
	    List<IHarvestedAccessor> accessors = AccessorFactory.getHarvestedAccessors(LookupPolicy.SPECIFIC);

	    accessors.sort((a1, a2) -> a1.getClass().getName().compareTo(a2.getClass().getName()));

	    Assert.assertEquals(2, accessors.size());

	    Assert.assertEquals(AutomaticSystemAccessor.class, accessors.get(0).getClass());
	    
	    Assert.assertEquals(SIGEDACAccessor.class, accessors.get(1).getClass());

	}

	{
	    List<IHarvestedAccessor> accessors = AccessorFactory.getHarvestedAccessors(LookupPolicy.ALL);

	    accessors.sort((a1, a2) -> a1.getClass().getName().compareTo(a2.getClass().getName()));

	    Assert.assertEquals(2, accessors.size());

	    Assert.assertEquals(AutomaticSystemAccessor.class, accessors.get(0).getClass());
	    
	    Assert.assertEquals(SIGEDACAccessor.class, accessors.get(1).getClass());
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

	Assert.assertEquals(2, StreamUtils.iteratorToStream(loader.iterator()).count());

	Assert.assertTrue(//
		StreamUtils
			.iteratorToStream(//
				loader.iterator())
			.anyMatch(c -> c.getClass().equals(AutomaticSystemConnector.class)));//
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void configurableTest() {

	ServiceLoader<Configurable> loader = ServiceLoader.load(Configurable.class);

	Assert.assertTrue(
		StreamUtils.iteratorToStream(loader.iterator()).anyMatch(c -> c.getClass().getName().equals(AutomaticSystemAccessor.class.getName())));

	Assert.assertTrue(
		StreamUtils.iteratorToStream(loader.iterator()).anyMatch(c -> c.getClass().getName().equals(AutomaticSystemConnector.class.getName())));
	
	Assert.assertTrue(
		StreamUtils.iteratorToStream(loader.iterator()).anyMatch(c -> c.getClass().getName().equals(SIGEDACAccessor.class.getName())));
	
	Assert.assertTrue(
		StreamUtils.iteratorToStream(loader.iterator()).anyMatch(c -> c.getClass().getName().equals(SIGEDACConnector.class.getName())));

    }
}
