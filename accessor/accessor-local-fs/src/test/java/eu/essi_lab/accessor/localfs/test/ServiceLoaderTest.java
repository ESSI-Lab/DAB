package eu.essi_lab.accessor.localfs.test;

import java.util.List;
import java.util.ServiceLoader;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.accessor.localfs.LocalFileSystemAccessor;
import eu.essi_lab.accessor.localfs.LocalFileSystemConnector;
import eu.essi_lab.accessor.localfs.cswtestdata.CSWTestDataAccessor;
import eu.essi_lab.accessor.localfs.cswtestdata.CSWTestDataConnector;
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

	    accessors.sort((a1, a2) -> a2.getClass().getName().compareTo(a1.getClass().getName()));

	    Assert.assertEquals(2, accessors.size());

	    Assert.assertEquals(CSWTestDataAccessor.class, accessors.get(0).getClass());
	    Assert.assertEquals(LocalFileSystemAccessor.class, accessors.get(1).getClass());
	}

	{
	    List<IHarvestedAccessor> accessors = AccessorFactory.getHarvestedAccessors(LookupPolicy.ALL);

	    accessors.sort((a1, a2) -> a2.getClass().getName().compareTo(a1.getClass().getName()));

	    Assert.assertEquals(2, accessors.size());

	    Assert.assertEquals(CSWTestDataAccessor.class, accessors.get(0).getClass());
	    Assert.assertEquals(LocalFileSystemAccessor.class, accessors.get(1).getClass());
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
			.filter(c -> c.getClass().equals(LocalFileSystemConnector.class)).//
			findFirst().isPresent());//

	Assert.assertTrue(//
		StreamUtils
			.iteratorToStream(//
				loader.iterator())
			.filter(c -> c.getClass().equals(CSWTestDataConnector.class)).//
			findFirst().isPresent());//

    }

    @SuppressWarnings("rawtypes")
    @Test
    public void configurableTest() {

	ServiceLoader<Configurable> loader = ServiceLoader.load(Configurable.class);

	Assert.assertTrue(StreamUtils.iteratorToStream(loader.iterator())
		.filter(c -> c.getClass().getName().equals(LocalFileSystemAccessor.class.getName())).//
		findFirst().//
		isPresent());

	Assert.assertTrue(StreamUtils.iteratorToStream(loader.iterator())
		.filter(c -> c.getClass().getName().equals(LocalFileSystemConnector.class.getName())).//
		findFirst().//
		isPresent());

	Assert.assertTrue(StreamUtils.iteratorToStream(loader.iterator())
		.filter(c -> c.getClass().getName().equals(CSWTestDataAccessor.class.getName())).//
		findFirst().//
		isPresent());

	Assert.assertTrue(StreamUtils.iteratorToStream(loader.iterator())
		.filter(c -> c.getClass().getName().equals(CSWTestDataConnector.class.getName())).//
		findFirst().//
		isPresent());

    }
}
