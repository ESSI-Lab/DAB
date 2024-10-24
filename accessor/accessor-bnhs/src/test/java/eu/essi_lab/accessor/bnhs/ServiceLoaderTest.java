package eu.essi_lab.accessor.bnhs;

import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.accessor.canada.CANADAMSCAccessor;
import eu.essi_lab.accessor.cehq.CEHQAccessor;
import eu.essi_lab.accessor.imo.IMOAccessor;
import eu.essi_lab.accessor.nve.NVEAccessor;
import eu.essi_lab.accessor.odatahidro.ODataHidrologyAccessor;
import eu.essi_lab.accessor.rihmi.RIHMIAccessor;
import eu.essi_lab.accessor.usgswatersrv.USGSAccessor;
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

	    Assert.assertEquals(8, accessors.size());

	    Assert.assertEquals(BNHSAccessor.class, accessors.get(0).getClass());
	    Assert.assertEquals(CANADAMSCAccessor.class, accessors.get(1).getClass());
	    Assert.assertEquals(CEHQAccessor.class, accessors.get(2).getClass());
	    Assert.assertEquals(IMOAccessor.class, accessors.get(3).getClass());
	    Assert.assertEquals(NVEAccessor.class, accessors.get(4).getClass());
	    Assert.assertEquals(ODataHidrologyAccessor.class, accessors.get(5).getClass());
	    Assert.assertEquals(RIHMIAccessor.class, accessors.get(6).getClass());
	    Assert.assertEquals(USGSAccessor.class, accessors.get(7).getClass());
	}

	{
	    List<IHarvestedAccessor> accessors = AccessorFactory.getHarvestedAccessors(LookupPolicy.ALL);

	    accessors.sort((a1, a2) -> a1.getClass().getName().compareTo(a2.getClass().getName()));

	    Assert.assertEquals(8, accessors.size());

	    Assert.assertEquals(BNHSAccessor.class, accessors.get(0).getClass());
	    Assert.assertEquals(CANADAMSCAccessor.class, accessors.get(1).getClass());
	    Assert.assertEquals(CEHQAccessor.class, accessors.get(2).getClass());
	    Assert.assertEquals(IMOAccessor.class, accessors.get(3).getClass());
	    Assert.assertEquals(NVEAccessor.class, accessors.get(4).getClass());
	    Assert.assertEquals(ODataHidrologyAccessor.class, accessors.get(5).getClass());
	    Assert.assertEquals(RIHMIAccessor.class, accessors.get(6).getClass());
	    Assert.assertEquals(USGSAccessor.class, accessors.get(7).getClass());
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

	Assert.assertEquals(8, StreamUtils.iteratorToStream(loader.iterator()).count());

	List<String> names = StreamUtils.iteratorToStream(loader.iterator()).//
		map(c -> c.getClass().getName()).//
		sorted().//
		collect(Collectors.toList());

	Assert.assertEquals("eu.essi_lab.accessor.bnhs.BNHSConnector", names.get(0));
	Assert.assertEquals("eu.essi_lab.accessor.canada.CANADAMSCConnector", names.get(1));
	Assert.assertEquals("eu.essi_lab.accessor.cehq.CEHQConnector", names.get(2));
	Assert.assertEquals("eu.essi_lab.accessor.imo.IMOConnector", names.get(3));
	Assert.assertEquals("eu.essi_lab.accessor.nve.NVEConnector", names.get(4));
	Assert.assertEquals("eu.essi_lab.accessor.odatahidro.ODataHidrologyConnector", names.get(5));
	Assert.assertEquals("eu.essi_lab.accessor.rihmi.RIHMIConnector", names.get(6));
	Assert.assertEquals("eu.essi_lab.accessor.usgswatersrv.USGSConnector", names.get(7));

    }

    @Test
    public void resourceMapperTest() {

	ServiceLoader<IResourceMapper> loader = ServiceLoader.load(IResourceMapper.class);

	Assert.assertTrue(StreamUtils.iteratorToStream(loader.iterator()).//
		filter(c -> c.getSupportedOriginalMetadataSchema().equals("BNHS")).//

		findFirst().//
		isPresent());

    }

    @SuppressWarnings("rawtypes")
    @Test
    public void configurableTest() {

	ServiceLoader<Configurable> loader = ServiceLoader.load(Configurable.class);

	Assert.assertTrue(
		StreamUtils.iteratorToStream(loader.iterator()).filter(c -> c.getClass().getName().equals(BNHSAccessor.class.getName())).//
			findFirst().//
			isPresent());

	Assert.assertTrue(
		StreamUtils.iteratorToStream(loader.iterator()).filter(c -> c.getClass().getName().equals(BNHSConnector.class.getName())).//
			findFirst().//
			isPresent());

    }
}
