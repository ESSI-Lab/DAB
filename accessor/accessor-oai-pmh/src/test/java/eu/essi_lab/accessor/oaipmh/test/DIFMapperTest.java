package eu.essi_lab.accessor.oaipmh.test;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import eu.essi_lab.accessor.oaipmh.DIFMapper;
import eu.essi_lab.iso.datamodel.classes.CoverageDescription;
import eu.essi_lab.iso.datamodel.classes.DataIdentification;
import eu.essi_lab.iso.datamodel.classes.GeographicBoundingBox;
import eu.essi_lab.iso.datamodel.classes.MIMetadata;
import eu.essi_lab.iso.datamodel.classes.ResponsibleParty;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.utils.IOStreamUtils;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.exceptions.GSException;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.HarmonizedMetadata;
import eu.essi_lab.model.resource.OriginalMetadata;
import junit.framework.TestCase;

/**
 * @author Fabrizio
 */
public class DIFMapperTest {

    DIFMapper mapper;

    @Before
    public void init() {
	this.mapper = new DIFMapper();
    }

    @Test
    public void test() throws IOException, GSException {

	String record = IOStreamUtils
		.asUTF8String(DIFMapperTest.class.getClassLoader().getResourceAsStream("sios.xml"));

	OriginalMetadata originalMD = new OriginalMetadata();

	originalMD.setMetadata(record);
	originalMD.setSchemeURI(CommonNameSpaceContext.DIF_URI);

	GSSource gsSource = Mockito.mock(GSSource.class);
	Mockito.when(gsSource.getEndpoint()).thenReturn("http://sios.csw.met.no?mode=oaipmh");

	GSResource resource = mapper.map(originalMD, gsSource);
	
	HarmonizedMetadata result = resource.getHarmonizedMetadata();
	
	CoreMetadata core = result.getCoreMetadata();

	MIMetadata metadata = core.getMIMetadata();

	DataIdentification dataIdentification = metadata.getDataIdentification();
	TestCase.assertNotNull(dataIdentification);

	Assert.assertEquals("no.met.adc:57a2ac75-aa24-59c3-9af5-836d3f05ccb7", core.getIdentifier());

	String abstract_ = core.getAbstract();
	Assert.assertTrue(abstract_.contains("The Hive Wireless"));

	String title = core.getTitle();
	Assert.assertTrue(title.contains("Kongsvegen AWS"));
	
	GeographicBoundingBox bbox = dataIdentification.getGeographicBoundingBox();
	TestCase.assertEquals(12.6506, bbox.getEast());
	TestCase.assertEquals(12.6506, bbox.getWest());
	TestCase.assertEquals(78.8187, bbox.getNorth());
	TestCase.assertEquals(78.8187, bbox.getSouth());
	
	
	// responsible party
	ResponsibleParty originator = dataIdentification.getPointOfContact("investigator");

	TestCase.assertEquals("University of Oslo", originator.getOrganisationName());
	
	TemporalExtent time = dataIdentification.getTemporalExtent();
	TestCase.assertEquals("2021-05-05T10:16:43Z", time.getBeginPosition());
	TestCase.assertEquals("2021-08-12T05:16:43Z", time.getEndPosition());
	
//	MIPlatform platform = metadata.getMIPlatform();
//	TestCase.assertEquals("ARGO Italy", platform.getDescription());

	// coverage
	CoverageDescription coverage = metadata.getCoverageDescription();
	TestCase.assertEquals("EARTH SCIENCE>CRYOSPHERE>GLACIERS/ICE SHEETS>GLACIERS", coverage.getAttributeDescription());
	TestCase.assertEquals(null, coverage.getAttributeIdentifier());
	
	
	
	
	
    }
}