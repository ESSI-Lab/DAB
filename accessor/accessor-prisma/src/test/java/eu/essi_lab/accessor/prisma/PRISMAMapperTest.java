package eu.essi_lab.accessor.prisma;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import eu.essi_lab.iso.datamodel.classes.CoverageDescription;
import eu.essi_lab.iso.datamodel.classes.DataIdentification;
import eu.essi_lab.iso.datamodel.classes.GeographicBoundingBox;
import eu.essi_lab.iso.datamodel.classes.MIMetadata;
import eu.essi_lab.iso.datamodel.classes.MIPlatform;
import eu.essi_lab.iso.datamodel.classes.ResponsibleParty;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.HarmonizedMetadata;
import eu.essi_lab.model.resource.OriginalMetadata;
import junit.framework.TestCase;

public class PRISMAMapperTest {

//    @Test
//    public void test() {
//	fail("Not yet implemented");
//    }
    
    private PRISMAMapper mapper;
    
    @Before
    public void init() {
	this.mapper = new PRISMAMapper();
    }
    
    @Test
    public void testMinimalMapperFromExample() throws Exception {
	InputStream stream = PRISMAMapperTest.class.getClassLoader().getResourceAsStream("prisma/prisma.csv");

	TestCase.assertNotNull(stream);

	BufferedReader bfReader = null;

	bfReader = new BufferedReader(new InputStreamReader(stream));
	// String temp = null;
	bfReader.readLine(); // skip header line
	String temp = bfReader.readLine();
	//String[] splittedTemp = temp.split("\\|");
		
	System.out.println(temp);

	stream.close();

	OriginalMetadata originalMD = new OriginalMetadata();

	originalMD.setMetadata(temp);

	GSResource resource = mapper.map(originalMD, new GSSource());
	
	HarmonizedMetadata result = resource.getHarmonizedMetadata();
	
	TestCase.assertNotNull(result);

	CoreMetadata core = result.getCoreMetadata();

	MIMetadata metadata = core.getMIMetadata();

	DataIdentification dataIdentification = metadata.getDataIdentification();
	TestCase.assertNotNull(dataIdentification);

	// title
	TestCase.assertEquals("PRISMA Image", dataIdentification.getCitationTitle());

	// bbox
	GeographicBoundingBox bbox = dataIdentification.getGeographicBoundingBox();
	TestCase.assertEquals(-37.26104, bbox.getNorth());
	TestCase.assertEquals(-37.59016, bbox.getSouth());
	TestCase.assertEquals(143.87877, bbox.getWest());
	TestCase.assertEquals(144.30864, bbox.getEast());
	
	// id
	TestCase.assertEquals(Optional.empty(), resource.getOriginalId());

	// responsible party
	ResponsibleParty originator = dataIdentification.getPointOfContact("pointOfContact");

	TestCase.assertEquals("ASI - Agenzia Spaziale Italiana", originator.getOrganisationName());
	TestCase.assertEquals("prisma_missionmanagement@asi.it", originator.getContact().getAddress().getElectronicMailAddress());

	// time
	TemporalExtent time = dataIdentification.getTemporalExtent();
	TestCase.assertEquals("2021-06-02T00:23:03.352Z", time.getBeginPosition());
	TestCase.assertEquals("2021-06-02T00:23:07.662Z", time.getEndPosition());

	// online

	//TestCase.assertEquals("ftp://18.18.83.11/thunder_data_GSOD/007018.txt", metadata.getDistribution().getDistributionOnline().getLinkage());

	// platform
	MIPlatform platform = metadata.getMIPlatform();
	TestCase.assertEquals("PRISMA", platform.getDescription());

	// coverage
	CoverageDescription coverage = metadata.getCoverageDescriptions().next();
	TestCase.assertEquals("L1 product: Top of Atmosphere radiance", coverage.getAttributeDescription());
	TestCase.assertEquals("L1 product", coverage.getAttributeIdentifier());
	TestCase.assertEquals(" Top of Atmosphere radiance", coverage.getAttributeTitle());

    }


}
