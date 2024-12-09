package eu.essi_lab.accessor.polytope;

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

public class PolytopeMapperTest {

    private PolytopeMapper mapper;
    private PolytopeMeteoTrackerMapper meteoMapper;

    @Before
    public void init() {
	this.mapper = new PolytopeMapper();
	this.meteoMapper = new PolytopeMeteoTrackerMapper();
    }

    @Test
    public void testMinimalMapperFromExample() throws Exception {
	InputStream stream = PolytopeMapperTest.class.getClassLoader().getResourceAsStream("Ams25_TEMP.csv");

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

    //@Test
    public void testMeteoTrackerMapperFromExample() throws Exception {
	InputStream stream = PolytopeMapperTest.class.getClassLoader().getResourceAsStream("meteotracker.csv");

	TestCase.assertNotNull(stream);

	String test = IOStreamUtils.asUTF8String(stream);

	stream.close();

	OriginalMetadata originalMD = new OriginalMetadata();

	originalMD.setMetadata(test);

	GSResource resource = meteoMapper.map(originalMD, new GSSource());

	HarmonizedMetadata result = resource.getHarmonizedMetadata();

	TestCase.assertNotNull(result);

	CoreMetadata core = result.getCoreMetadata();

	MIMetadata metadata = core.getMIMetadata();

	DataIdentification dataIdentification = metadata.getDataIdentification();
	TestCase.assertNotNull(dataIdentification);

	// title
	TestCase.assertEquals("Acquisitions of 2m temperature through MeteoTracker mobile weather station: Mele-Mel", dataIdentification.getCitationTitle());

	// bbox
	Iterator<BoundingPolygon> iteratorBB = dataIdentification.getBoundingPolygons();
	while(iteratorBB.hasNext()) {
	    BoundingPolygon bpolygon = iteratorBB.next();
	    TestCase.assertNotNull(bpolygon.getMultiPoints());
	}

	// last elevation
	VerticalExtent verticalExtent = dataIdentification.getVerticalExtent();
	TestCase.assertEquals(167.48, verticalExtent.getMinimumValue());
	TestCase.assertEquals(621.36, verticalExtent.getMaximumValue());
	GeographicBoundingBox bbox = dataIdentification.getGeographicBoundingBox();
	TestCase.assertEquals(8.7491996, bbox.getEast());
	TestCase.assertEquals(8.7390237, bbox.getWest());
	TestCase.assertEquals(44.4823125, bbox.getNorth());
	TestCase.assertEquals(44.4468308, bbox.getSouth());
	
	// id
	TestCase.assertEquals(Optional.empty(), resource.getOriginalId());

	// time
	TemporalExtent time = dataIdentification.getTemporalExtent();

	TestCase.assertEquals("2022-05-25T15:00:00Z", time.getBeginPosition());
	TestCase.assertEquals("2022-05-25T15:38:36Z", time.getEndPosition());

	// online

	TestCase.assertEquals("https://i-change.s3.amazonaws.com/meteotracker_Mele-Mel_TEMP.csv",
		metadata.getDistribution().getDistributionOnline().getLinkage());

	// platform
	MIPlatform platform = metadata.getMIPlatform();
	TestCase.assertEquals("Mele-Mel", platform.getDescription());

	// coverage
	CoverageDescription coverage = metadata.getCoverageDescription();
	TestCase.assertEquals("TEMPERATURE Units: Kelvin", coverage.getAttributeDescription());
	TestCase.assertEquals("TEMPERATURE", coverage.getAttributeIdentifier());

    }
    
    
    @Test
    public void testMeteoTrackerMapperFromExample2() throws Exception {
	InputStream stream = PolytopeMapperTest.class.getClassLoader().getResourceAsStream("meteotracker_1e997bbd_TEMP.csv");

	TestCase.assertNotNull(stream);

	String test = IOStreamUtils.asUTF8String(stream);

	stream.close();

	OriginalMetadata originalMD = new OriginalMetadata();

	originalMD.setMetadata(test);

	GSResource resource = meteoMapper.map(originalMD, new GSSource());

	HarmonizedMetadata result = resource.getHarmonizedMetadata();

	TestCase.assertNotNull(result);

	CoreMetadata core = result.getCoreMetadata();

	MIMetadata metadata = core.getMIMetadata();

	DataIdentification dataIdentification = metadata.getDataIdentification();
	TestCase.assertNotNull(dataIdentification);

	// title
	TestCase.assertEquals("Acquisitions of 2m temperature through MeteoTracker mobile weather station: 1e997bbd", dataIdentification.getCitationTitle());

	// bbox
	Iterator<BoundingPolygon> iteratorBB = dataIdentification.getBoundingPolygons();
	while(iteratorBB.hasNext()) {
	    BoundingPolygon bpolygon = iteratorBB.next();
	    TestCase.assertNotNull(bpolygon.getMultiPoints());
	}

	// last elevation
	VerticalExtent verticalExtent = dataIdentification.getVerticalExtent();
	TestCase.assertEquals(116.1029052734375, verticalExtent.getMinimumValue());
	TestCase.assertEquals(463.75311279296875, verticalExtent.getMaximumValue());
	GeographicBoundingBox bbox = dataIdentification.getGeographicBoundingBox();
	TestCase.assertEquals(10.066622734069824, bbox.getEast());
	TestCase.assertEquals(9.813328742980957, bbox.getWest());
	TestCase.assertEquals(44.15411376953125, bbox.getNorth());
	TestCase.assertEquals(44.07124710083008, bbox.getSouth());
	
	// id
	TestCase.assertEquals(Optional.empty(), resource.getOriginalId());

	// time
	TemporalExtent time = dataIdentification.getTemporalExtent();

	TestCase.assertEquals("2022-08-18T10:48:13Z", time.getBeginPosition());
	TestCase.assertEquals("2022-08-18T11:57:15Z", time.getEndPosition());

	// online

	TestCase.assertEquals("https://i-change.s3.amazonaws.com/meteotracker_1e997bbd_TEMP.csv",
		metadata.getDistribution().getDistributionOnline().getLinkage());

	// platform
	MIPlatform platform = metadata.getMIPlatform();
	TestCase.assertEquals("1e997bbd", platform.getDescription());

	// coverage
	CoverageDescription coverage = metadata.getCoverageDescription();
	TestCase.assertEquals("TEMPERATURE Units: Kelvin", coverage.getAttributeDescription());
	TestCase.assertEquals("TEMPERATURE", coverage.getAttributeIdentifier());

    }
    
    

    //@Test
    public void testCsvLibraryWithExample() throws IOException {
	InputStream stream = PolytopeMapperTest.class.getClassLoader().getResourceAsStream("meteotracker.csv");

	ClonableInputStream cis = new ClonableInputStream(stream);
	TestCase.assertNotNull(stream);
	String s = IOStreamUtils.asUTF8String(cis.clone());

	Reader in = new StringReader(s);
	Iterable<CSVRecord> records = null;

	String lat = null;
	String lon = null;
	try {

	    String d = ";";
	    char delimiter = d.charAt(0);
	    records = CSVFormat.RFC4180.withDelimiter(delimiter).withFirstRecordAsHeader().parse(in);
	    for (CSVRecord record : records) {

		// String press = record.get("press@body");
		lat = record.get("lat@hdr");
		lon = record.get("lon@hdr");
		break;
	    }

	} catch (Exception e) {

	    try {
		Reader in2 = new StringReader(s);
		records = CSVFormat.RFC4180.withFirstRecordAsHeader().parse(in2);
	    } catch (IOException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	    }
	    for (CSVRecord record : records) {

		String press = record.get("press@body");
		lat = record.get("lat@hdr");
		lon = record.get("lon@hdr");
		break;
	    }

	}
	TestCase.assertEquals("44.4472023", lat);
	TestCase.assertEquals("8.7491311", lon);

    }

}
