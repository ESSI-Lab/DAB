package eu.essi_lab.accessor.thredds;

import java.io.InputStream;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import eu.essi_lab.iso.datamodel.classes.DataIdentification;
import eu.essi_lab.iso.datamodel.classes.GeographicBoundingBox;
import eu.essi_lab.iso.datamodel.classes.MIMetadata;
import eu.essi_lab.iso.datamodel.classes.ResponsibleParty;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.iso.datamodel.classes.VerticalExtent;
import eu.essi_lab.lib.xml.XMLDocumentReader;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.HarmonizedMetadata;
import eu.essi_lab.model.resource.OriginalMetadata;
import junit.framework.TestCase;



public class THREDDSMapperTest {

    private THREDDSMapper mapper;
    
    @Before
    public void init() {
	this.mapper = new THREDDSMapper();
    }

    
    @Test
    public void testTHREDDSMapperFromExample() throws Exception {
	InputStream stream = THREDDSMapperTest.class.getClassLoader().getResourceAsStream("waves.xml");
	
	TestCase.assertNotNull(stream);
	
	XMLDocumentReader xdoc = new XMLDocumentReader(stream);
	
	OriginalMetadata originalMD = new OriginalMetadata();

	originalMD.setMetadata(xdoc.asString());
	
	stream.close();
	
	GSSource source = new GSSource();
	source.setEndpoint("http://tds.webservice-energy.org/thredds/catalog.xml");
	
	GSResource resource = mapper.map(originalMD, source);
	
	HarmonizedMetadata result = resource.getHarmonizedMetadata();
	
	TestCase.assertNotNull(result);

	CoreMetadata core = result.getCoreMetadata();
	
	MIMetadata metadata = core.getMIMetadata();

	DataIdentification dataIdentification = metadata.getDataIdentification();
	TestCase.assertNotNull(dataIdentification);

	// title
	TestCase.assertEquals("wave_t0m1_maps.nc", dataIdentification.getCitationTitle());

	// bbox
	GeographicBoundingBox bbox = dataIdentification.getGeographicBoundingBox();
	TestCase.assertEquals(3.000000000000001, bbox.getEast());
	TestCase.assertEquals(-6.3, bbox.getWest());
	TestCase.assertEquals(51.599999999999994, bbox.getNorth());
	TestCase.assertEquals(43.3, bbox.getSouth());
	
	//elevation
	VerticalExtent verticalExtent = dataIdentification.getVerticalExtent();
	TestCase.assertEquals(0.0, verticalExtent.getMaximumValue());
	TestCase.assertEquals(0.0, verticalExtent.getMinimumValue());
	// id
	TestCase.assertEquals(Optional.empty(), resource.getOriginalId());

	// responsible party
	ResponsibleParty originator = dataIdentification.getPointOfContact("pointOfContact");
	TestCase.assertEquals("lionel.menard@mines-paristech.fr", originator.getContact().getAddress().getElectronicMailAddress());
	TestCase.assertEquals("http://www.webservice-energy.org", originator.getContact().getOnline().getLinkage());

	// time
	TemporalExtent time = dataIdentification.getTemporalExtent();
	TestCase.assertEquals("1994-01-01T00:00:00Z", time.getBeginPosition());
	TestCase.assertEquals("2012-12-31T23:00:00Z", time.getEndPosition());

	// onlines

	//TestCase.assertEquals("ftp://18.18.83.11/thunder_data_GSOD/007018.txt", metadata.getDistribution().getDistributionOnline().getLinkage());

	
	
    }
}
