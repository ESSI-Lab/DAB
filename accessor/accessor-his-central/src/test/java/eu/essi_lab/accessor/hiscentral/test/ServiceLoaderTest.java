package eu.essi_lab.accessor.hiscentral.test;

import eu.essi_lab.accessor.hiscentral.basilicata.*;
import eu.essi_lab.accessor.hiscentral.bolzano.*;
import eu.essi_lab.accessor.hiscentral.emilia.*;
import eu.essi_lab.accessor.hiscentral.emilia.simc.*;
import eu.essi_lab.accessor.hiscentral.friuli.*;
import eu.essi_lab.accessor.hiscentral.lazio.*;
import eu.essi_lab.accessor.hiscentral.liguria.*;
import eu.essi_lab.accessor.hiscentral.lombardia.*;
import eu.essi_lab.accessor.hiscentral.marche.*;
import eu.essi_lab.accessor.hiscentral.piemonte.*;
import eu.essi_lab.accessor.hiscentral.puglia.*;
import eu.essi_lab.accessor.hiscentral.puglia.arpa.*;
import eu.essi_lab.accessor.hiscentral.sardegna.*;
import eu.essi_lab.accessor.hiscentral.toscana.*;
import eu.essi_lab.accessor.hiscentral.umbria.*;
import eu.essi_lab.accessor.hiscentral.valdaosta.*;
import eu.essi_lab.accessor.hiscentral.veneto.*;
import eu.essi_lab.adk.*;
import eu.essi_lab.adk.AccessorFactory.*;
import eu.essi_lab.adk.harvest.*;
import eu.essi_lab.cdk.harvest.*;
import eu.essi_lab.cfga.*;
import eu.essi_lab.jaxb.common.*;
import eu.essi_lab.lib.utils.*;
import eu.essi_lab.ommdk.*;
import org.junit.*;

import java.util.*;

/**
 * @author Fabrizio
 */
public class ServiceLoaderTest {

    @SuppressWarnings("rawtypes")
    @Test
    public void accessorLoaderTest() {

	{

	    List<IHarvestedAccessor> accessors = AccessorFactory.getHarvestedAccessors(LookupPolicy.SPECIFIC);

	    accessors.sort(Comparator.comparing(a -> a.getClass().getName()));

	    Assert.assertEquals(17, accessors.size());

	    Assert.assertEquals(HISCentralBasilicataAccessor.class, accessors.get(0).getClass());
	    Assert.assertEquals(HISCentralBolzanoAccessor.class, accessors.get(1).getClass());
	    Assert.assertEquals(HISCentralEmiliaAccessor.class, accessors.get(2).getClass());
	    Assert.assertEquals(HISCentralEmiliaSimcAccessor.class, accessors.get(3).getClass());
	    Assert.assertEquals(HISCentralFriuliAccessor.class, accessors.get(4).getClass());
	    Assert.assertEquals(HISCentralLazioAccessor.class, accessors.get(5).getClass());
	    Assert.assertEquals(HISCentralLiguriaAccessor.class, accessors.get(6).getClass());
	    Assert.assertEquals(HISCentralLombardiaAccessor.class, accessors.get(7).getClass());
	    Assert.assertEquals(HISCentralMarcheAccessor.class, accessors.get(8).getClass());
	    Assert.assertEquals(HISCentralPiemonteAccessor.class, accessors.get(9).getClass());
	    Assert.assertEquals(HISCentralPugliaAccessor.class, accessors.get(10).getClass());
	    Assert.assertEquals(HISCentralARPAPugliaAccessor.class, accessors.get(11).getClass());
	    Assert.assertEquals(HISCentralSardegnaAccessor.class, accessors.get(12).getClass());
	    Assert.assertEquals(HISCentralToscanaAccessor.class, accessors.get(13).getClass());
	    Assert.assertEquals(HISCentralUmbriaAccessor.class, accessors.get(14).getClass());
	    Assert.assertEquals(HISCentralValdaostaAccessor.class, accessors.get(15).getClass());
	    Assert.assertEquals(HISCentralVenetoAccessor.class, accessors.get(16).getClass());

	}

	{
	    List<IHarvestedAccessor> accessors = AccessorFactory.getHarvestedAccessors(LookupPolicy.ALL);

	    accessors.sort(Comparator.comparing(a -> a.getClass().getName()));

	    Assert.assertEquals(17, accessors.size());

	    Assert.assertEquals(HISCentralBasilicataAccessor.class, accessors.get(0).getClass());
	    Assert.assertEquals(HISCentralBolzanoAccessor.class, accessors.get(1).getClass());
	    Assert.assertEquals(HISCentralEmiliaAccessor.class, accessors.get(2).getClass());
	    Assert.assertEquals(HISCentralEmiliaSimcAccessor.class, accessors.get(3).getClass());
	    Assert.assertEquals(HISCentralFriuliAccessor.class, accessors.get(4).getClass());
	    Assert.assertEquals(HISCentralLazioAccessor.class, accessors.get(5).getClass());
	    Assert.assertEquals(HISCentralLiguriaAccessor.class, accessors.get(6).getClass());
	    Assert.assertEquals(HISCentralLombardiaAccessor.class, accessors.get(7).getClass());
	    Assert.assertEquals(HISCentralMarcheAccessor.class, accessors.get(8).getClass());
	    Assert.assertEquals(HISCentralPiemonteAccessor.class, accessors.get(9).getClass());
	    Assert.assertEquals(HISCentralPugliaAccessor.class, accessors.get(10).getClass());
	    Assert.assertEquals(HISCentralARPAPugliaAccessor.class, accessors.get(11).getClass());
	    Assert.assertEquals(HISCentralSardegnaAccessor.class, accessors.get(12).getClass());
	    Assert.assertEquals(HISCentralToscanaAccessor.class, accessors.get(13).getClass());
	    Assert.assertEquals(HISCentralUmbriaAccessor.class, accessors.get(14).getClass());
	    Assert.assertEquals(HISCentralValdaostaAccessor.class, accessors.get(15).getClass());
	    Assert.assertEquals(HISCentralVenetoAccessor.class, accessors.get(16).getClass());
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

	Assert.assertEquals(17, StreamUtils.iteratorToStream(loader.iterator()).count());

	Assert.assertTrue(//
		StreamUtils.iteratorToStream(//
			loader.iterator()).anyMatch(c -> c.getClass().equals(HISCentralMarcheConnector.class)));//

	Assert.assertTrue(//
		StreamUtils.iteratorToStream(//
			loader.iterator()).anyMatch(c -> c.getClass().equals(HISCentralToscanaConnector.class)));//
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
