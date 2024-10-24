package eu.essi_lab.accessor.saeon;

import java.io.InputStream;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Iterators;

import eu.essi_lab.iso.datamodel.classes.DataIdentification;
import eu.essi_lab.iso.datamodel.classes.GeographicBoundingBox;
import eu.essi_lab.iso.datamodel.classes.MIMetadata;
import eu.essi_lab.iso.datamodel.classes.ResponsibleParty;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.lib.utils.IOStreamUtils;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.HarmonizedMetadata;
import eu.essi_lab.model.resource.OriginalMetadata;
import junit.framework.TestCase;

public class SAEONMapperTest {

//    @Test
//    public void test() {
//	fail("Not yet implemented");
//    }
    
    private SAEONMapper mapper;
    
    @Before
    public void init() {
	this.mapper = new SAEONMapper();
    }
    
    @Test
    public void testMinimalMapperFromExample() throws Exception {
	InputStream stream = SAEONMapperTest.class.getClassLoader().getResourceAsStream("saeonRecord.json");

	TestCase.assertNotNull(stream);

	String metadata = IOStreamUtils.asUTF8String(stream);
	
	OriginalMetadata originalMD = new OriginalMetadata();

	originalMD.setMetadata(metadata);
	
	stream.close();

	GSResource resource = mapper.map(originalMD, new GSSource());
	
	HarmonizedMetadata result = resource.getHarmonizedMetadata();
	
	TestCase.assertNotNull(result);

	CoreMetadata core = result.getCoreMetadata();

	MIMetadata miMetadata = core.getMIMetadata();

	DataIdentification dataIdentification = miMetadata.getDataIdentification();
	TestCase.assertNotNull(dataIdentification);

	// title
	TestCase.assertEquals("The ASCA June 2018 raw current meter data collected at Mooring E for all depths", dataIdentification.getCitationTitle());

	// bbox
	GeographicBoundingBox bbox = dataIdentification.getGeographicBoundingBox();
	TestCase.assertEquals(-33.4278, bbox.getNorth());
	TestCase.assertEquals(-35.6514, bbox.getSouth());
	TestCase.assertEquals(27.53361, bbox.getWest());
	TestCase.assertEquals(28.85083, bbox.getEast());
	
	// id
	TestCase.assertEquals("034fe4fe-979f-4b17-9816-f6f4ca91a2b9", resource.getOriginalId().get());
	
	//keywords
	TestCase.assertEquals(13,Iterators.size(dataIdentification.getKeywordsValues()));

	// responsible party
	ResponsibleParty originator = dataIdentification.getPointOfContact("originator");
//
	TestCase.assertEquals("Lisa Beal", originator.getIndividualName());
//	TestCase.assertEquals("prisma_missionmanagement@asi.it", originator.getContact().getAddress().getElectronicMailAddress());

	// time
	TemporalExtent time = dataIdentification.getTemporalExtent();
	TestCase.assertEquals("2018-05-31", time.getBeginPosition());
	TestCase.assertEquals("2018-06-13", time.getEndPosition());

	// online

	TestCase.assertEquals("http://media.dirisa.org/inventory/archive/multi/asca/june-2018/mooring-e/raw/current-meter/mooring_e_raw_current_meters_all_depths.zip", miMetadata.getDistribution().getDistributionOnline().getLinkage());

	// platform
//	MIPlatform platform = miMetadata.getMIPlatform();
//	TestCase.assertEquals("PRISMA", platform.getDescription());
//
//	// coverage
//	CoverageDescription coverage = miMetadata.getCoverageDescriptions().next();
//	TestCase.assertEquals("L1 product: Top of Atmosphere radiance", coverage.getAttributeDescription());
//	TestCase.assertEquals("L1 product", coverage.getAttributeIdentifier());
//	TestCase.assertEquals(" Top of Atmosphere radiance", coverage.getAttributeTitle());

    }

    
    @Test
    public void testMinimalMapperFromExample2() throws Exception {
	InputStream stream = SAEONMapperTest.class.getClassLoader().getResourceAsStream("saeonRecord2.json");

	TestCase.assertNotNull(stream);

	String metadata = IOStreamUtils.asUTF8String(stream);
	
	OriginalMetadata originalMD = new OriginalMetadata();

	originalMD.setMetadata(metadata);
	
	stream.close();

	GSResource resource = mapper.map(originalMD, new GSSource());
	
	HarmonizedMetadata result = resource.getHarmonizedMetadata();
	
	TestCase.assertNotNull(result);

	CoreMetadata core = result.getCoreMetadata();

	MIMetadata miMetadata = core.getMIMetadata();

	DataIdentification dataIdentification = miMetadata.getDataIdentification();
	TestCase.assertNotNull(dataIdentification);

	// title
	TestCase.assertEquals("The ASCA June 2018 raw current meter data collected at Mooring E for all depths", dataIdentification.getCitationTitle());

	// bbox
	GeographicBoundingBox bbox = dataIdentification.getGeographicBoundingBox();
	TestCase.assertEquals(-33.4278, bbox.getNorth());
	TestCase.assertEquals(-35.6514, bbox.getSouth());
	TestCase.assertEquals(27.53361, bbox.getWest());
	TestCase.assertEquals(28.85083, bbox.getEast());
	
	// id
	TestCase.assertEquals("034fe4fe-979f-4b17-9816-f6f4ca91a2b9", resource.getOriginalId().get());
	
	//keywords
	TestCase.assertEquals(13,Iterators.size(dataIdentification.getKeywordsValues()));

	// responsible party
	ResponsibleParty originator = dataIdentification.getPointOfContact("originator");
//
	TestCase.assertEquals("Lisa Beal", originator.getIndividualName());
//	TestCase.assertEquals("prisma_missionmanagement@asi.it", originator.getContact().getAddress().getElectronicMailAddress());

	// time
	TemporalExtent time = dataIdentification.getTemporalExtent();
	TestCase.assertEquals("2018-05-31", time.getBeginPosition());
	TestCase.assertEquals("2018-06-13", time.getEndPosition());

	// online

	TestCase.assertEquals("http://media.dirisa.org/inventory/archive/multi/asca/june-2018/mooring-e/raw/current-meter/mooring_e_raw_current_meters_all_depths.zip", miMetadata.getDistribution().getDistributionOnline().getLinkage());

	// platform
//	MIPlatform platform = miMetadata.getMIPlatform();
//	TestCase.assertEquals("PRISMA", platform.getDescription());
//
//	// coverage
//	CoverageDescription coverage = miMetadata.getCoverageDescriptions().next();
//	TestCase.assertEquals("L1 product: Top of Atmosphere radiance", coverage.getAttributeDescription());
//	TestCase.assertEquals("L1 product", coverage.getAttributeIdentifier());
//	TestCase.assertEquals(" Top of Atmosphere radiance", coverage.getAttributeTitle());

    }

}
