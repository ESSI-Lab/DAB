package eu.essi_lab.accessor.csw;

import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

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

	    List<IHarvestedAccessor> accessors = AccessorFactory.getHarvestedAccessors(LookupPolicy.SPECIFIC);

	    accessors.sort((a1, a2) -> a1.getClass().getName().compareTo(a2.getClass().getName()));

	    Assert.assertEquals(2, accessors.size());

	    Assert.assertEquals(CSWAccessor.class, accessors.get(0).getClass());
	}

	{
	    List<IHarvestedAccessor> accessors = AccessorFactory.getHarvestedAccessors(LookupPolicy.ALL);

	    accessors.sort((a1, a2) -> a1.getClass().getName().compareTo(a2.getClass().getName()));

	    Assert.assertEquals(2, accessors.size());

	    Assert.assertEquals(CSWAccessor.class, accessors.get(0).getClass());
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
			.filter(c -> c.getClass().equals(CSWConnectorWrapper.class)).//
			findFirst().isPresent());//

    }

    @Test
    public void wrappedConnectorTest() {

	ServiceLoader<CSWConnector> loader = ServiceLoader.load(CSWConnector.class);

	Assert.assertEquals(25, StreamUtils.iteratorToStream(loader.iterator()).count());

	List<String> types = StreamUtils.iteratorToStream(loader.iterator()).map(c -> c.getType()).collect(Collectors.toList());

	Assert.assertTrue(types.contains(CSWConnector.TYPE));
	Assert.assertTrue(types.contains(CSWBLUECLOUDConnector.TYPE));
	Assert.assertTrue(types.contains(CSWC3SConnector.TYPE));
	Assert.assertTrue(types.contains(CSWCEDACCIConnector.TYPE));
	Assert.assertTrue(types.contains(CSWCMEMSConnector.TYPE));
	Assert.assertTrue(types.contains(CSWCMRConnector.TYPE));
	Assert.assertTrue(types.contains(CSWEEASDIConnector.TYPE));
	Assert.assertTrue(types.contains(CSWEURACConnector.TYPE));
	Assert.assertTrue(types.contains(CSWEVK2Connector.TYPE));
	Assert.assertTrue(types.contains(CSWGEOSURConnector.TYPE));
	Assert.assertTrue(types.contains(CSWGetConnector.TYPE));
	Assert.assertTrue(types.contains(CSWGFDRRConnector.TYPE));
	Assert.assertTrue(types.contains(CSWMCP1Connector.TYPE));
	Assert.assertTrue(types.contains(CSWMCP2Connector.TYPE));
	Assert.assertTrue(types.contains(CSWMultiConnector.TYPE));
	Assert.assertTrue(types.contains(CSWNODCConnector.TYPE));
	Assert.assertTrue(types.contains(CSWRCMRDConnector.TYPE));
	Assert.assertTrue(types.contains(CSWSAEONConnector.TYPE));
	Assert.assertTrue(types.contains(CSWSHMIConnector.TYPE));
	Assert.assertTrue(types.contains(CSWTWAPConnector.TYPE));
	Assert.assertTrue(types.contains(CSWUNESCO_IHPConnector.TYPE));
	Assert.assertTrue(types.contains(CSWUNOOSAConnector.TYPE));
	Assert.assertTrue(types.contains(CSWTHREDDSConnector.TYPE));
	Assert.assertTrue(types.contains(CSWDIONEConnector.TYPE));
	Assert.assertTrue(types.contains(CSWEMODNETConnector.TYPE));
	Assert.assertTrue(types.contains(CSWAGAMEConnector.TYPE));
    }

    @Test
    public void resourceMapperTest() {

	ServiceLoader<IResourceMapper> loader = ServiceLoader.load(IResourceMapper.class);

	Assert.assertTrue(StreamUtils.iteratorToStream(loader.iterator()).//
		filter(c -> c.getSupportedOriginalMetadataSchema().equals(CommonNameSpaceContext.BLUECLOUD_NS_URI)).//
		findFirst().//
		isPresent());

    }

    @SuppressWarnings("rawtypes")
    @Test
    public void configurableTest() {

	ServiceLoader<Configurable> loader = ServiceLoader.load(Configurable.class);

	Assert.assertTrue(
		StreamUtils.iteratorToStream(loader.iterator()).filter(c -> c.getClass().getName().equals(CSWAccessor.class.getName())).//
			findFirst().//
			isPresent());

	Assert.assertTrue(StreamUtils.iteratorToStream(loader.iterator())
		.filter(c -> c.getClass().getName().equals(CSWConnectorWrapper.class.getName())).//
		findFirst().//
		isPresent());
    }

}
