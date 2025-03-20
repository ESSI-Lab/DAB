package eu.essi_lab.accessor.waf;

import java.util.List;
import java.util.ServiceLoader;

import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.accessor.waf.dirlisting.DirectoryListingAccessor;
import eu.essi_lab.accessor.waf.dirlisting.DirectoryListingConnector;
import eu.essi_lab.accessor.waf.ecovlab.ECOPotentialVLabConnector;
import eu.essi_lab.accessor.waf.ecovlab.ECOPotentialVlabAccessor;
import eu.essi_lab.accessor.waf.httpgetiso.HTTPGetISOAccessor;
import eu.essi_lab.accessor.waf.httpgetiso.HttpGetISOConnector;
import eu.essi_lab.accessor.waf.netcdf.NetCDFAccessor;
import eu.essi_lab.accessor.waf.netcdf.NetCDFConnector;
import eu.essi_lab.accessor.waf.onamet.ONAMETAccessor;
import eu.essi_lab.accessor.waf.onamet.ONAMETAugmenter;
import eu.essi_lab.accessor.waf.onamet.ONAMETConnector;
import eu.essi_lab.accessor.waf.onamet.ONAMETMapper;
import eu.essi_lab.accessor.waf.onamet_stations.ONAMETStationsAccessor;
import eu.essi_lab.accessor.waf.onamet_stations.ONAMETStationsAugmenter;
import eu.essi_lab.accessor.waf.onamet_stations.ONAMETStationsConnector;
import eu.essi_lab.accessor.waf.onamet_stations.ONAMETStationsMapper;
import eu.essi_lab.accessor.waf.s3bucket.S3BucketAccessor;
import eu.essi_lab.accessor.waf.s3bucket.S3BucketConnector;
import eu.essi_lab.accessor.waf.trigger.TRIGGERWafAccessor;
import eu.essi_lab.accessor.waf.trigger.TRIGGERWafConnector;
import eu.essi_lab.accessor.waf.zenodo.ZenodoS3BucketAccessor;
import eu.essi_lab.accessor.waf.zenodo.ZenodoS3BucketConnector;
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

	    Assert.assertEquals(9, accessors.size());

	    Assert.assertEquals(DirectoryListingAccessor.class, accessors.get(0).getClass());
	    Assert.assertEquals(ECOPotentialVlabAccessor.class, accessors.get(1).getClass());
	    Assert.assertEquals(HTTPGetISOAccessor.class, accessors.get(2).getClass());
	    Assert.assertEquals(NetCDFAccessor.class, accessors.get(3).getClass());
	    Assert.assertEquals(ONAMETAccessor.class, accessors.get(4).getClass());
	    Assert.assertEquals(ONAMETStationsAccessor.class, accessors.get(5).getClass());
	    Assert.assertEquals(S3BucketAccessor.class, accessors.get(6).getClass());
	    Assert.assertEquals(TRIGGERWafAccessor.class, accessors.get(7).getClass());
	    Assert.assertEquals(ZenodoS3BucketAccessor.class, accessors.get(8).getClass());

	}

	{
	    List<IHarvestedAccessor> accessors = AccessorFactory.getHarvestedAccessors(LookupPolicy.ALL);

	    accessors.sort((a1, a2) -> a1.getClass().getName().compareTo(a2.getClass().getName()));

	    Assert.assertEquals(9, accessors.size());

	    Assert.assertEquals(DirectoryListingAccessor.class, accessors.get(0).getClass());
	    Assert.assertEquals(ECOPotentialVlabAccessor.class, accessors.get(1).getClass());
	    Assert.assertEquals(HTTPGetISOAccessor.class, accessors.get(2).getClass());
	    Assert.assertEquals(NetCDFAccessor.class, accessors.get(3).getClass());
	    Assert.assertEquals(ONAMETAccessor.class, accessors.get(4).getClass());
	    Assert.assertEquals(ONAMETStationsAccessor.class, accessors.get(5).getClass());
	    Assert.assertEquals(S3BucketAccessor.class, accessors.get(6).getClass());
	    Assert.assertEquals(TRIGGERWafAccessor.class, accessors.get(7).getClass());
	    Assert.assertEquals(ZenodoS3BucketAccessor.class, accessors.get(8).getClass());
	}

	{
	    List<IHarvestedAccessor> accessors = AccessorFactory.getHarvestedAccessors(LookupPolicy.MIXED);

	    Assert.assertEquals(0, accessors.size());
	}
    }

    @Test
    public void harvestedQueryConnectorTest() {

	long count = StreamUtils.iteratorToStream(ServiceLoader.load(IHarvestedQueryConnector.class).iterator()).count();
	Assert.assertEquals(9, count);

	Assert.assertTrue(//
		StreamUtils.iteratorToStream(ServiceLoader.load(IHarvestedQueryConnector.class).iterator())
			.filter(c -> c.getClass().equals(DirectoryListingConnector.class)).//
			findFirst().isPresent());//

	Assert.assertTrue(//
		StreamUtils.iteratorToStream(ServiceLoader.load(IHarvestedQueryConnector.class).iterator())
			.filter(c -> c.getClass().equals(ECOPotentialVLabConnector.class)).//
			findFirst().isPresent());//

	Assert.assertTrue(//
		StreamUtils.iteratorToStream(ServiceLoader.load(IHarvestedQueryConnector.class).iterator())
			.filter(c -> c.getClass().equals(HttpGetISOConnector.class)).//
			findFirst().isPresent());//

	Assert.assertTrue(//
		StreamUtils.iteratorToStream(ServiceLoader.load(IHarvestedQueryConnector.class).iterator())
			.filter(c -> c.getClass().equals(S3BucketConnector.class)).//
			findFirst().isPresent());//

	Assert.assertTrue(//
		StreamUtils.iteratorToStream(ServiceLoader.load(IHarvestedQueryConnector.class).iterator())
			.filter(c -> c.getClass().equals(ONAMETConnector.class)).//
			findFirst().isPresent());//
	
	Assert.assertTrue(//
		StreamUtils.iteratorToStream(ServiceLoader.load(IHarvestedQueryConnector.class).iterator())
			.filter(c -> c.getClass().equals(ONAMETStationsConnector.class)).//
			findFirst().isPresent());//
	Assert.assertTrue(//
		StreamUtils.iteratorToStream(ServiceLoader.load(IHarvestedQueryConnector.class).iterator())
			.filter(c -> c.getClass().equals(NetCDFConnector.class)).//
			findFirst().isPresent());//
	
	Assert.assertTrue(//
		StreamUtils.iteratorToStream(ServiceLoader.load(IHarvestedQueryConnector.class).iterator())
			.filter(c -> c.getClass().equals(ZenodoS3BucketConnector.class)).//
			findFirst().isPresent());//
	
	Assert.assertTrue(//
		StreamUtils.iteratorToStream(ServiceLoader.load(IHarvestedQueryConnector.class).iterator())
			.filter(c -> c.getClass().equals(TRIGGERWafConnector.class)).//
			findFirst().isPresent());//
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void configurableTest() {

	ServiceLoader<Configurable> loader = ServiceLoader.load(Configurable.class);

	long count = StreamUtils.iteratorToStream(ServiceLoader.load(IHarvestedQueryConnector.class).iterator()).count();
	Assert.assertEquals(9, count);
	
	Assert.assertTrue(StreamUtils.iteratorToStream(loader.iterator()).//
		filter(c -> c.getClass().getName().equals(DirectoryListingAccessor.class.getName())).//
		findFirst().//
		isPresent());
	
	Assert.assertTrue(StreamUtils.iteratorToStream(loader.iterator()).//
		filter(c -> c.getClass().getName().equals(DirectoryListingConnector.class.getName())).//
		findFirst().//
		isPresent());

	Assert.assertTrue(//
		StreamUtils.iteratorToStream(loader.iterator()).//
			filter(c -> c.getClass().getName().equals(ECOPotentialVlabAccessor.class.getName())).//
			findFirst().//
			isPresent());
	
	Assert.assertTrue(//
		StreamUtils.iteratorToStream(loader.iterator()).//
			filter(c -> c.getClass().getName().equals(ECOPotentialVLabConnector.class.getName())).//
			findFirst().//
			isPresent());

	Assert.assertTrue(//
		StreamUtils.iteratorToStream(loader.iterator()).//
			filter(c -> c.getClass().getName().equals(HTTPGetISOAccessor.class.getName())).//
			findFirst().//
			isPresent());
	
	Assert.assertTrue(//
		StreamUtils.iteratorToStream(loader.iterator()).//
			filter(c -> c.getClass().getName().equals(HttpGetISOConnector.class.getName())).//
			findFirst().//
			isPresent());

	Assert.assertTrue(//
		StreamUtils.iteratorToStream(loader.iterator()).//
			filter(c -> c.getClass().getName().equals(S3BucketAccessor.class.getName())).//
			findFirst().//
			isPresent());

	
	Assert.assertTrue(//
		StreamUtils.iteratorToStream(loader.iterator()).//
			filter(c -> c.getClass().getName().equals(S3BucketConnector.class.getName())).//
			findFirst().//
			isPresent());

	Assert.assertTrue(//
		StreamUtils.iteratorToStream(loader.iterator()).//
			filter(c -> c.getClass().getName().equals(ONAMETAccessor.class.getName())).//
			findFirst().//
			isPresent());
	
	Assert.assertTrue(//
		StreamUtils.iteratorToStream(loader.iterator()).//
			filter(c -> c.getClass().getName().equals(ONAMETConnector.class.getName())).//
			findFirst().//
			isPresent());
	
	Assert.assertTrue(//
		StreamUtils.iteratorToStream(loader.iterator()).//
			filter(c -> c.getClass().getName().equals(ONAMETAugmenter.class.getName())).//
			findFirst().//
			isPresent());
	
	Assert.assertTrue(//
		StreamUtils.iteratorToStream(loader.iterator()).//
			filter(c -> c.getClass().getName().equals(ONAMETStationsAccessor.class.getName())).//
			findFirst().//
			isPresent());
	
	Assert.assertTrue(//
		StreamUtils.iteratorToStream(loader.iterator()).//
			filter(c -> c.getClass().getName().equals(ONAMETStationsConnector.class.getName())).//
			findFirst().//
			isPresent());
	
	Assert.assertTrue(//
		StreamUtils.iteratorToStream(loader.iterator()).//
			filter(c -> c.getClass().getName().equals(ONAMETStationsAugmenter.class.getName())).//
			findFirst().//
			isPresent());
	
	Assert.assertTrue(//
		StreamUtils.iteratorToStream(loader.iterator()).//
			filter(c -> c.getClass().getName().equals(ZenodoS3BucketConnector.class.getName())).//
			findFirst().//
			isPresent());
	
	Assert.assertTrue(//
		StreamUtils.iteratorToStream(loader.iterator()).//
			filter(c -> c.getClass().getName().equals(ZenodoS3BucketAccessor.class.getName())).//
			findFirst().//
			isPresent());
    }

    @Test
    public void resourceMapperTest() {

	ServiceLoader<IResourceMapper> loader = ServiceLoader.load(IResourceMapper.class);

	Assert.assertTrue(StreamUtils.iteratorToStream(loader.iterator()).//
		filter(c -> c.getSupportedOriginalMetadataSchema().equals(ONAMETMapper.ONAMET_METADATA_SCHEMA)).//

		findFirst().//
		isPresent());
	
	Assert.assertTrue(StreamUtils.iteratorToStream(loader.iterator()).//
		filter(c -> c.getSupportedOriginalMetadataSchema().equals(ONAMETStationsMapper.ONAMET_STATIONS_METADATA_SCHEMA)).//

		findFirst().//
		isPresent());

    }

}
