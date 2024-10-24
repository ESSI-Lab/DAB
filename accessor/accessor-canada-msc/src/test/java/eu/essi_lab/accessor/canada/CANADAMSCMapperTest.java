package eu.essi_lab.accessor.canada;

import static org.junit.Assert.assertEquals;

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
import eu.essi_lab.iso.datamodel.classes.TemporalExtent.FrameValue;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.HarmonizedMetadata;
import eu.essi_lab.model.resource.OriginalMetadata;
import junit.framework.TestCase;

public class CANADAMSCMapperTest {

    private CANADAMSCMapper mapper;

    @Before
    public void init() {
	this.mapper = new CANADAMSCMapper();
    }

    @Test
    public void testMinimalMapperFromExample() throws Exception {
	InputStream stream = CANADAMSCMapperTest.class.getClassLoader().getResourceAsStream("hydrometric_StationList.csv");

	TestCase.assertNotNull(stream);

	BufferedReader bfReader = null;

	bfReader = new BufferedReader(new InputStreamReader(stream));
	// String temp = null;
	bfReader.readLine(); // skip header line
	String temp = bfReader.readLine();

	stream.close();

	temp += ",http://dd.weather.gc.ca/hydrometric/csv/NB/daily/NB_01AD003_daily_hydrometric.csv,";
	// temp += "http://dd.weather.gc.ca/hydrometric/csv/NB/hourly/NB_01AD003_hourly_hydrometric.csv,";
	temp += "http://dd.weather.gc.ca/hydrometric/,";
	temp += ECVariable.DISCHARGE + ",";
	temp += "2019-02-10T00:00:00-04:00" + ",";
	temp += "1000";

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
	TestCase.assertEquals("Discharge@\"ST. FRANCIS RIVER AT OUTLET OF GLASIER LAKE\"", dataIdentification.getCitationTitle());

	// bbox
	GeographicBoundingBox bbox = dataIdentification.getGeographicBoundingBox();
	TestCase.assertEquals(-68.95694, bbox.getEast());
	TestCase.assertEquals(-68.95694, bbox.getWest());
	TestCase.assertEquals(47.20661, bbox.getNorth());
	TestCase.assertEquals(47.20661, bbox.getSouth());

	// id
	TestCase.assertEquals(Optional.empty(), resource.getOriginalId());

	// responsible party
	ResponsibleParty originator = dataIdentification.getPointOfContact("author");

	TestCase.assertEquals("Environment Canada", originator.getOrganisationName());

	// time
	TemporalExtent time = dataIdentification.getTemporalExtent();
	assertEquals(FrameValue.P1M, time.getBeforeNowBeginPosition().get());
	TestCase.assertEquals("NOW", time.getIndeterminateEndPosition().toString());
//	TestCase.assertEquals("2019-02-10T04:00:00Z", time.getBeginPosition());
	// TestCase.assertEquals("2019-03-08", time.getEndPosition());

	// online


	// platform
	MIPlatform platform = metadata.getMIPlatform();
	TestCase.assertEquals("\"ST. FRANCIS RIVER AT OUTLET OF GLASIER LAKE\"", platform.getDescription());

	// coverage
	CoverageDescription coverage = metadata.getCoverageDescription();
	TestCase.assertEquals("Discharge Units: cms Resolution: DAILY", coverage.getAttributeDescription());
	TestCase.assertEquals("urn:dd.weather.gc.ca.hydrometric:variable:DISCHARGE_DAILY", coverage.getAttributeIdentifier());

    }

}
