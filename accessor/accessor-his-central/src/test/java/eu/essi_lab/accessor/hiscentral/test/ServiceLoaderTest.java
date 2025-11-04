package eu.essi_lab.accessor.hiscentral.test;

import java.util.List;
import java.util.ServiceLoader;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.accessor.hiscentral.basilicata.HISCentralBasilicataAccessor;
import eu.essi_lab.accessor.hiscentral.bolzano.HISCentralBolzanoAccessor;
import eu.essi_lab.accessor.hiscentral.emilia.HISCentralEmiliaAccessor;
import eu.essi_lab.accessor.hiscentral.friuli.HISCentralFriuliAccessor;
import eu.essi_lab.accessor.hiscentral.lazio.HISCentralLazioAccessor;
import eu.essi_lab.accessor.hiscentral.liguria.HISCentralLiguriaAccessor;
import eu.essi_lab.accessor.hiscentral.lombardia.HISCentralLombardiaAccessor;
import eu.essi_lab.accessor.hiscentral.marche.HISCentralMarcheAccessor;
import eu.essi_lab.accessor.hiscentral.marche.HISCentralMarcheConnector;
import eu.essi_lab.accessor.hiscentral.piemonte.HISCentralPiemonteAccessor;
import eu.essi_lab.accessor.hiscentral.puglia.HISCentralPugliaAccessor;
import eu.essi_lab.accessor.hiscentral.puglia.arpa.HISCentralARPAPugliaAccessor;
import eu.essi_lab.accessor.hiscentral.sardegna.HISCentralSardegnaAccessor;
import eu.essi_lab.accessor.hiscentral.toscana.HISCentralToscanaAccessor;
import eu.essi_lab.accessor.hiscentral.toscana.HISCentralToscanaConnector;
import eu.essi_lab.accessor.hiscentral.umbria.HISCentralUmbriaAccessor;
import eu.essi_lab.accessor.hiscentral.valdaosta.HISCentralValdaostaAccessor;
import eu.essi_lab.accessor.hiscentral.veneto.HISCentralVenetoAccessor;
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
    public void accessorLoaderTest(){ 
    
    {

	
	    List<IHarvestedAccessor> accessors = AccessorFactory.getHarvestedAccessors(LookupPolicy.SPECIFIC);

	    accessors.sort((a1, a2) -> a1.getClass().getName().compareTo(a2.getClass().getName()));

	    Assert.assertEquals(16, accessors.size());

	    Assert.assertEquals(HISCentralBasilicataAccessor.class, accessors.get(0).getClass());
	    Assert.assertEquals(HISCentralBolzanoAccessor.class, accessors.get(1).getClass());
	    Assert.assertEquals(HISCentralEmiliaAccessor.class, accessors.get(2).getClass());
	    Assert.assertEquals(HISCentralFriuliAccessor.class, accessors.get(3).getClass());
	    Assert.assertEquals(HISCentralLazioAccessor.class, accessors.get(4).getClass());
	    Assert.assertEquals(HISCentralLiguriaAccessor.class, accessors.get(5).getClass());
	    Assert.assertEquals(HISCentralLombardiaAccessor.class, accessors.get(6).getClass());
	    Assert.assertEquals(HISCentralMarcheAccessor.class, accessors.get(7).getClass());
	    Assert.assertEquals(HISCentralPiemonteAccessor.class, accessors.get(8).getClass());
	    Assert.assertEquals(HISCentralPugliaAccessor.class, accessors.get(9).getClass());
	    Assert.assertEquals(HISCentralARPAPugliaAccessor.class, accessors.get(10).getClass());
	    Assert.assertEquals(HISCentralSardegnaAccessor.class, accessors.get(11).getClass());
	    Assert.assertEquals(HISCentralToscanaAccessor.class, accessors.get(12).getClass());
	    Assert.assertEquals(HISCentralUmbriaAccessor.class, accessors.get(13).getClass());
	    Assert.assertEquals(HISCentralValdaostaAccessor.class, accessors.get(14).getClass());
	    Assert.assertEquals(HISCentralVenetoAccessor.class, accessors.get(15).getClass());

	}

	{
	    List<IHarvestedAccessor> accessors = AccessorFactory.getHarvestedAccessors(LookupPolicy.ALL);

	    accessors.sort((a1, a2) -> a1.getClass().getName().compareTo(a2.getClass().getName()));


	    Assert.assertEquals(16, accessors.size());
	    
	    Assert.assertEquals(HISCentralBasilicataAccessor.class, accessors.get(0).getClass());
	    Assert.assertEquals(HISCentralBolzanoAccessor.class, accessors.get(1).getClass());
	    Assert.assertEquals(HISCentralEmiliaAccessor.class, accessors.get(2).getClass());
	    Assert.assertEquals(HISCentralFriuliAccessor.class, accessors.get(3).getClass());
	    Assert.assertEquals(HISCentralLazioAccessor.class, accessors.get(4).getClass());
	    Assert.assertEquals(HISCentralLiguriaAccessor.class, accessors.get(5).getClass());
	    Assert.assertEquals(HISCentralLombardiaAccessor.class, accessors.get(6).getClass());
	    Assert.assertEquals(HISCentralMarcheAccessor.class, accessors.get(7).getClass());
	    Assert.assertEquals(HISCentralPiemonteAccessor.class, accessors.get(8).getClass());
	    Assert.assertEquals(HISCentralPugliaAccessor.class, accessors.get(9).getClass());
	    Assert.assertEquals(HISCentralARPAPugliaAccessor.class, accessors.get(10).getClass());
	    Assert.assertEquals(HISCentralSardegnaAccessor.class, accessors.get(11).getClass());
	    Assert.assertEquals(HISCentralToscanaAccessor.class, accessors.get(12).getClass());
	    Assert.assertEquals(HISCentralUmbriaAccessor.class, accessors.get(13).getClass());
	    Assert.assertEquals(HISCentralValdaostaAccessor.class, accessors.get(14).getClass());
	    Assert.assertEquals(HISCentralVenetoAccessor.class, accessors.get(15).getClass());
	    
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

	Assert.assertEquals(16, StreamUtils.iteratorToStream(loader.iterator()).count());


	Assert.assertTrue(//
		StreamUtils
			.iteratorToStream(//
				loader.iterator())
			.anyMatch(c -> c.getClass().equals(HISCentralMarcheConnector.class)));//
	
	Assert.assertTrue(//
		StreamUtils
			.iteratorToStream(//
				loader.iterator())
			.anyMatch(c -> c.getClass().equals(HISCentralToscanaConnector.class)));//
    }

    @Test
    public void resourceMapperTest() {

	ServiceLoader<IResourceMapper> loader = ServiceLoader.load(IResourceMapper.class);
	
	
	Assert.assertTrue(StreamUtils.iteratorToStream(loader.iterator()).//
		anyMatch(c -> c.getSupportedOriginalMetadataSchema().equals(CommonNameSpaceContext.HISCENTRAL_VALDAOSTA_NS_URI)));
	
	Assert.assertTrue(StreamUtils.iteratorToStream(loader.iterator()).//
		anyMatch(c -> c.getSupportedOriginalMetadataSchema().equals(CommonNameSpaceContext.HISCENTRAL_LAZIO_NS_URI)));
	
	Assert.assertTrue(StreamUtils.iteratorToStream(loader.iterator()).//
		anyMatch(c -> c.getSupportedOriginalMetadataSchema().equals(CommonNameSpaceContext.HISCENTRAL_VENETO_NS_URI)));
	
	Assert.assertTrue(StreamUtils.iteratorToStream(loader.iterator()).//
		anyMatch(c -> c.getSupportedOriginalMetadataSchema().equals(CommonNameSpaceContext.HISCENTRAL_LIGURIA_NS_URI)));
	
	Assert.assertTrue(StreamUtils.iteratorToStream(loader.iterator()).//
		anyMatch(c -> c.getSupportedOriginalMetadataSchema().equals(CommonNameSpaceContext.HISCENTRAL_FRIULI_NS_URI)));

	Assert.assertTrue(StreamUtils.iteratorToStream(loader.iterator()).//
		anyMatch(c -> c.getSupportedOriginalMetadataSchema().equals(CommonNameSpaceContext.HISCENTRAL_MARCHE_NS_URI)));
	
	Assert.assertTrue(StreamUtils.iteratorToStream(loader.iterator()).//
		anyMatch(c -> c.getSupportedOriginalMetadataSchema().equals(CommonNameSpaceContext.HISCENTRAL_TOSCANA_NS_URI)));
	
	Assert.assertTrue(StreamUtils.iteratorToStream(loader.iterator()).//
		anyMatch(c -> c.getSupportedOriginalMetadataSchema().equals(CommonNameSpaceContext.HISCENTRAL_UMBRIA_NS_URI)));
	

    }

    @SuppressWarnings("rawtypes")
    @Test
    public void configurableTest() {

	ServiceLoader<Configurable> loader = ServiceLoader.load(Configurable.class);

	Assert.assertTrue(StreamUtils.iteratorToStream(loader.iterator())
		.anyMatch(c -> c.getClass().getName().equals(HISCentralVenetoAccessor.class.getName())));
	
	Assert.assertTrue(StreamUtils.iteratorToStream(loader.iterator())
		.anyMatch(c -> c.getClass().getName().equals(HISCentralFriuliAccessor.class.getName())));

	Assert.assertTrue(StreamUtils.iteratorToStream(loader.iterator())
		.anyMatch(c -> c.getClass().getName().equals(HISCentralMarcheConnector.class.getName())));
	
	Assert.assertTrue(StreamUtils.iteratorToStream(loader.iterator())
		.anyMatch(c -> c.getClass().getName().equals(HISCentralToscanaAccessor.class.getName())));

	Assert.assertTrue(StreamUtils.iteratorToStream(loader.iterator())
		.anyMatch(c -> c.getClass().getName().equals(HISCentralToscanaConnector.class.getName())));

    }
}
