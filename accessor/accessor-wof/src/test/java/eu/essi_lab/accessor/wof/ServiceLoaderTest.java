package eu.essi_lab.accessor.wof;

import java.util.List;
import java.util.ServiceLoader;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.accessor.ina.INAAccessor;
import eu.essi_lab.accessor.ina.INAConnector;
import eu.essi_lab.accessor.ispra.ISPRAConnector;
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

	    Assert.assertEquals(3, accessors.size());

	    Assert.assertEquals(INAAccessor.class, accessors.get(0).getClass());
	    Assert.assertEquals(CUAHSIHISCentralAccessor.class, accessors.get(1).getClass());
	    Assert.assertEquals(CUAHSIHISServerAccessor.class, accessors.get(2).getClass());

	}

	{
	    List<IHarvestedAccessor> accessors = AccessorFactory.getHarvestedAccessors(LookupPolicy.ALL);

	    accessors.sort((a1, a2) -> a1.getClass().getName().compareTo(a2.getClass().getName()));

	    Assert.assertEquals(3, accessors.size());

	    Assert.assertEquals(INAAccessor.class, accessors.get(0).getClass());
	    Assert.assertEquals(CUAHSIHISCentralAccessor.class, accessors.get(1).getClass());
	    Assert.assertEquals(CUAHSIHISServerAccessor.class, accessors.get(2).getClass());
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

	Assert.assertTrue(//
		StreamUtils
			.iteratorToStream(//
				loader.iterator())
			.filter(c -> c.getClass().equals(INAConnector.class)).//
			findFirst().isPresent());//
	

	Assert.assertTrue(//
		StreamUtils
			.iteratorToStream(//
				loader.iterator())
			.filter(c -> c.getClass().equals(ISPRAConnector.class)).//
			findFirst().isPresent());//

	Assert.assertTrue(//
		StreamUtils
			.iteratorToStream(//
				loader.iterator())
			.filter(c -> c.getClass().equals(CUAHSIHISCentralConnector.class)).//
			findFirst().isPresent());//

	Assert.assertTrue(//
		StreamUtils
			.iteratorToStream(//
				loader.iterator())
			.filter(c -> c.getClass().equals(CUAHSIHISServerConnector.class)).//
			findFirst().isPresent());//
	
	
    }

    @Test
    public void resourceMapperTest() {

	ServiceLoader<IResourceMapper> loader = ServiceLoader.load(IResourceMapper.class);

	Assert.assertTrue(StreamUtils.iteratorToStream(loader.iterator()).//
		filter(c -> c.getSupportedOriginalMetadataSchema().equals(CommonNameSpaceContext.WML1_NS_URI)).//
		findFirst().//
		isPresent());
	
	Assert.assertTrue(StreamUtils.iteratorToStream(loader.iterator()).//
		filter(c -> c.getSupportedOriginalMetadataSchema().equals(CommonNameSpaceContext.WML1_ISPRA_NS_URI)).//
		findFirst().//
		isPresent());

    }

    @SuppressWarnings("rawtypes")
    @Test
    public void configurableTest() {

	ServiceLoader<Configurable> loader = ServiceLoader.load(Configurable.class);

	Assert.assertTrue(
		StreamUtils.iteratorToStream(loader.iterator()).filter(c -> c.getClass().getName().equals(INAAccessor.class.getName())).//
			findFirst().//
			isPresent());

	Assert.assertTrue(StreamUtils.iteratorToStream(loader.iterator())
		.filter(c -> c.getClass().getName().equals(CUAHSIHISCentralAccessor.class.getName())).//
		findFirst().//
		isPresent());

	Assert.assertTrue(StreamUtils.iteratorToStream(loader.iterator())
		.filter(c -> c.getClass().getName().equals(CUAHSIHISServerAccessor.class.getName())).//
		findFirst().//
		isPresent());

	Assert.assertTrue(
		StreamUtils.iteratorToStream(loader.iterator()).filter(c -> c.getClass().getName().equals(INAConnector.class.getName())).//
			findFirst().//
			isPresent());

	Assert.assertTrue(StreamUtils.iteratorToStream(loader.iterator())
		.filter(c -> c.getClass().getName().equals(CUAHSIHISCentralConnector.class.getName())).//
		findFirst().//
		isPresent());

	Assert.assertTrue(StreamUtils.iteratorToStream(loader.iterator())
		.filter(c -> c.getClass().getName().equals(CUAHSIHISServerConnector.class.getName())).//
		findFirst().//
		isPresent());
	

	Assert.assertTrue(StreamUtils.iteratorToStream(loader.iterator())
		.filter(c -> c.getClass().getName().equals(ISPRAConnector.class.getName())).//
		findFirst().//
		isPresent());
    }

}
