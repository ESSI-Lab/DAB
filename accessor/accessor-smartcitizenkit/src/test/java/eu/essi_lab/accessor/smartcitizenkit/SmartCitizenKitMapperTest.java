package eu.essi_lab.accessor.smartcitizenkit;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.Iterator;
import java.util.Optional;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.junit.Before;
import org.junit.Test;

import eu.essi_lab.accessor.smartcitizenkit.SmartCitizenKitMapper;
import eu.essi_lab.iso.datamodel.classes.BoundingPolygon;
import eu.essi_lab.iso.datamodel.classes.CoverageDescription;
import eu.essi_lab.iso.datamodel.classes.DataIdentification;
import eu.essi_lab.iso.datamodel.classes.GeographicBoundingBox;
import eu.essi_lab.iso.datamodel.classes.MIMetadata;
import eu.essi_lab.iso.datamodel.classes.MIPlatform;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.iso.datamodel.classes.VerticalExtent;
import eu.essi_lab.lib.utils.ClonableInputStream;
import eu.essi_lab.lib.utils.IOStreamUtils;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.HarmonizedMetadata;
import eu.essi_lab.model.resource.OriginalMetadata;
import junit.framework.TestCase;

public class SmartCitizenKitMapperTest {

    private SmartCitizenKitMapper mapper;
    

    @Before
    public void init() {
	this.mapper = new SmartCitizenKitMapper();
	
    }

    //@Test
    public void testMinimalMapperFromExample() throws Exception {
	InputStream stream = SmartCitizenKitMapperTest.class.getClassLoader().getResourceAsStream("Ams25_TEMP.csv");

	TestCase.assertNotNull(stream);

	String test = IOStreamUtils.asUTF8String(stream);

	stream.close();

	OriginalMetadata originalMD = new OriginalMetadata();

	originalMD.setMetadata(test);

	GSResource resource = mapper.map(originalMD, new GSSource());

	HarmonizedMetadata result = resource.getHarmonizedMetadata();

	TestCase.assertNotNull(result);

	CoreMetadata core = result.getCoreMetadata();

	MIMetadata metadata = core.getMIMetadata();

	DataIdentification dataIdentification = metadata.getDataIdentification();
	TestCase.assertNotNull(dataIdentification);

	// title
	TestCase.assertEquals("Acquisitions of 2m temperature at station: Ams25", dataIdentification.getCitationTitle());

	// bbox
	GeographicBoundingBox bbox = dataIdentification.getGeographicBoundingBox();
	TestCase.assertEquals(4.874, bbox.getEast());
	TestCase.assertEquals(4.874, bbox.getWest());
	TestCase.assertEquals(52.3367, bbox.getNorth());
	TestCase.assertEquals(52.3367, bbox.getSouth());

	// elevation
	VerticalExtent verticalExtent = dataIdentification.getVerticalExtent();
	TestCase.assertEquals(4.0, verticalExtent.getMaximumValue());
	TestCase.assertEquals(4.0, verticalExtent.getMinimumValue());
	// id
	TestCase.assertEquals(Optional.empty(), resource.getOriginalId());

	// time
	TemporalExtent time = dataIdentification.getTemporalExtent();

	TestCase.assertEquals("2015-06-02T00:00:00Z", time.getBeginPosition());
	TestCase.assertEquals("2015-09-02T22:00:00Z", time.getEndPosition());

	// online

	TestCase.assertEquals("https://i-change.s3.amazonaws.com/Ams25_TEMP.csv",
		metadata.getDistribution().getDistributionOnline().getLinkage());

	// platform
	MIPlatform platform = metadata.getMIPlatform();
	TestCase.assertEquals("Ams25", platform.getDescription());

	// coverage
	CoverageDescription coverage = metadata.getCoverageDescription();
	TestCase.assertEquals("TEMPERATURE Units: Kelvin Resolution: HOURLY", coverage.getAttributeDescription());
	TestCase.assertEquals("TEMPERATURE_HOURLY", coverage.getAttributeIdentifier());

    }


}
