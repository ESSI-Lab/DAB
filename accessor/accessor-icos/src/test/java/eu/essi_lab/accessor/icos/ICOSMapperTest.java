package eu.essi_lab.accessor.icos;

import java.io.InputStream;

import org.apache.cxf.helpers.IOUtils;
import org.junit.Before;
import org.junit.Test;

import eu.essi_lab.iso.datamodel.classes.CoverageDescription;
import eu.essi_lab.iso.datamodel.classes.DataIdentification;
import eu.essi_lab.iso.datamodel.classes.GeographicBoundingBox;
import eu.essi_lab.iso.datamodel.classes.MIMetadata;
import eu.essi_lab.iso.datamodel.classes.MIPlatform;
import eu.essi_lab.iso.datamodel.classes.ResponsibleParty;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.lib.utils.ClonableInputStream;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.HarmonizedMetadata;
import eu.essi_lab.model.resource.OriginalMetadata;
import junit.framework.TestCase;

public class ICOSMapperTest {

    private ICOSMapper mapper;

    private GSSource source;

    @Before
    public void init() {
	this.mapper = new ICOSMapper();
    }

    @Test
    public void testMinimalMapperFromExample() throws Exception {
	InputStream stream = ICOSMapperTest.class.getClassLoader().getResourceAsStream("58G2_NRT_20210118.csv.json");

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
	TestCase.assertEquals("Skjelvan, I., ICOS RI, 2021. ICOS OTC SOOP NRT Product, NO-SOOP-G.O.Sars, 2021-01-18–2021-01-23, https://hdl.handle.net/11676/XXth43B-tQz1tI5xFfVw68ep", dataIdentification.getCitationTitle());
	
	//abstract
	TestCase.assertEquals(null, dataIdentification.getAbstract());

	// bbox
	GeographicBoundingBox bbox = dataIdentification.getGeographicBoundingBox();
	TestCase.assertEquals(null, bbox);
//	TestCase.assertEquals(10.718, bbox.getEast());
//	TestCase.assertEquals(2.424, bbox.getWest());
//	TestCase.assertEquals(58.863, bbox.getNorth());
//	TestCase.assertEquals(57.182, bbox.getSouth());

	// id
	TestCase.assertEquals("11676/XXth43B-tQz1tI5xFfVw68ep", resource.getOriginalId().get());

	// responsible party
	ResponsibleParty originator = dataIdentification.getPointOfContact("originator");

	//TestCase.assertEquals(null, originator.getOrganisationName());
	//TestCase.assertEquals("Ocean Thematic Centre", originator.getOrganisationName());

	// time
	TemporalExtent time = dataIdentification.getTemporalExtent();
	TestCase.assertEquals("2021-01-18T07:41:57Z", time.getBeginPosition());
	TestCase.assertEquals("2021-01-23T18:56:28Z", time.getEndPosition());
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
	TestCase.assertEquals("NO-SOOP-G.O.Sars", platform.getDescription());

	// coverage
	CoverageDescription coverage = metadata.getCoverageDescription();
	TestCase.assertEquals("water temperature", coverage.getAttributeDescription());
	//TestCase.assertEquals("Temp [degC]", coverage.getAttributeIdentifier());

    }
    
    @Test
    public void testMinimalMapperFromExample2() throws Exception {
	InputStream stream = ICOSMapperTest.class.getClassLoader().getResourceAsStream("119920180214.csv.json");

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
	TestCase.assertEquals("ICOS OTC FOS Release, BE-FOS-Thornton Buoy, 2018-02-14–2020-07-10", dataIdentification.getCitationTitle());
	
	//abstract
	TestCase.assertEquals(null, dataIdentification.getAbstract());

	// bbox
	GeographicBoundingBox bbox = dataIdentification.getGeographicBoundingBox();
	TestCase.assertEquals(2.993217, bbox.getEast());
	TestCase.assertEquals(2.993217, bbox.getWest());
	TestCase.assertEquals(51.57989, bbox.getNorth());
	TestCase.assertEquals(51.57989, bbox.getSouth());
	

	// id
	TestCase.assertEquals("11676/n94jijAEWH95fMNn8Mwd4I0M", resource.getOriginalId().get());

	// responsible party
	ResponsibleParty originator = dataIdentification.getPointOfContact("originator");

	//TestCase.assertEquals(null, originator.getOrganisationName());
	//TestCase.assertEquals("Ocean Thematic Centre", originator.getOrganisationName());

	// time
	TemporalExtent time = dataIdentification.getTemporalExtent();
	TestCase.assertEquals("2018-02-14T16:20:00Z", time.getBeginPosition());
	TestCase.assertEquals("2020-07-10T20:00:31Z", time.getEndPosition());
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
	TestCase.assertEquals("BE-FOS-Thornton Buoy", platform.getDescription());

	// coverage
	CoverageDescription coverage = metadata.getCoverageDescription();
	TestCase.assertEquals("water temperature", coverage.getAttributeDescription());
	//TestCase.assertEquals("Temp [degC]", coverage.getAttributeIdentifier());

    }
    
    
}
