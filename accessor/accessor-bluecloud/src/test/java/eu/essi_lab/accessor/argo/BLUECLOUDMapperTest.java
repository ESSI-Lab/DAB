package eu.essi_lab.accessor.argo;

import java.io.InputStream;

import org.apache.cxf.helpers.IOUtils;
import org.junit.Before;
import org.junit.Test;

import eu.essi_lab.accessor.bluecloud.BLUECLOUDMapper;
import eu.essi_lab.iso.datamodel.classes.CoverageDescription;
import eu.essi_lab.iso.datamodel.classes.DataIdentification;
import eu.essi_lab.iso.datamodel.classes.GeographicBoundingBox;
import eu.essi_lab.iso.datamodel.classes.MIMetadata;
import eu.essi_lab.iso.datamodel.classes.MIPlatform;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.iso.datamodel.classes.ResponsibleParty;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.lib.utils.ClonableInputStream;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.HarmonizedMetadata;
import eu.essi_lab.model.resource.OriginalMetadata;
import junit.framework.TestCase;

public class BLUECLOUDMapperTest {

    private BLUECLOUDMapper mapper;

    private GSSource source;

    @Before
    public void init() {
	this.mapper = new BLUECLOUDMapper();
    }

    @Test
    public void testMinimalMapperFromExample() throws Exception {
	InputStream stream = BLUECLOUDMapperTest.class.getClassLoader().getResourceAsStream("bluecloud.json");

	ClonableInputStream cloneStream = new ClonableInputStream(stream);

	TestCase.assertNotNull(stream);

	OriginalMetadata originalMD = new OriginalMetadata();

	String mmetadata = IOUtils.toString(cloneStream.clone());

	originalMD.setMetadata(mmetadata);

	GSSource gsSource = new GSSource();

	GSResource resource = mapper.map(originalMD, gsSource);

	HarmonizedMetadata result = resource.getHarmonizedMetadata();

	// BufferedReader bfReader = null;
	//
	// bfReader = new BufferedReader(new InputStreamReader(stream));
	String temp = null;

	TestCase.assertNotNull(result);

	CoreMetadata core = result.getCoreMetadata();

	MIMetadata metadata = core.getMIMetadata();

	DataIdentification dataIdentification = metadata.getDataIdentification();
	TestCase.assertNotNull(dataIdentification);

	// title
	TestCase.assertEquals("8673 - 2902296 - Argo INDIA", dataIdentification.getCitationTitle());

	// bbox
	GeographicBoundingBox bbox = dataIdentification.getGeographicBoundingBox();
	TestCase.assertEquals(78.508904, bbox.getEast());
	TestCase.assertEquals(57.293079, bbox.getWest());
	TestCase.assertEquals(-49.266708, bbox.getNorth());
	TestCase.assertEquals(-55.411179, bbox.getSouth());

	// id
	TestCase.assertEquals("6CADFEC373A409B575F4DA37496F1C16EC423C59", resource.getOriginalId().get());

	// responsible party
	ResponsibleParty originator = dataIdentification.getPointOfContact("publisher");

	TestCase.assertEquals("INCOIS", originator.getOrganisationName());

	// time
	TemporalExtent time = dataIdentification.getTemporalExtent();
	TestCase.assertEquals("2020-02-29T10:57:55", time.getBeginPosition());
	TestCase.assertEquals("2023-06-19T10:27:32", time.getEndPosition());
	// TestCase.assertEquals("2019-04-04T22:05:00Z", time.getBeginPosition());
	// TestCase.assertEquals("2019-03-08", time.getEndPosition());

	// online

	// TestCase.assertEquals(gsSource.getEndpoint(),
	// metadata.getDistribution().getDistributionOnline().getLinkage());
	// Iterator<Online> onlines = metadata.getDistribution().getDistributionOnlines();
	// while(onlines.hasNext()) {
	// Online o = onlines.next();
	// String protocol = o.getProtocol();
	// if(protocol != null && protocol != "")
	// TestCase.assertTrue(protocol.equalsIgnoreCase(CommonNameSpaceContext.ANA_URI));
	// }
	// platform
	MIPlatform platform = metadata.getMIPlatform();
	TestCase.assertEquals("8673", platform.getDescription());

	// coverage
	CoverageDescription coverage = metadata.getCoverageDescription();
	TestCase.assertEquals("SUBSURFACE PRESSURE", coverage.getAttributeDescription());
	TestCase.assertEquals("SUBSURFACE PRESSURE", coverage.getAttributeIdentifier());
	
	Online online = metadata.getDistribution().getDistributionOnline();
	TestCase.assertEquals("https://data.blue-cloud.org/search-details?step=~012006196A3D05799453983F10DCAA367939D75C7FD", online.getLinkage());
	

    }
}
