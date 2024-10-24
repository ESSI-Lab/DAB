package eu.essi_lab.accessor.thunder;

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
import eu.essi_lab.iso.datamodel.classes.VerticalExtent;
import eu.essi_lab.lib.utils.ISO8601DateTimeUtils;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.HarmonizedMetadata;
import eu.essi_lab.model.resource.OriginalMetadata;
import junit.framework.TestCase;

public class ThunderMapperTest {

    private ThunderMapper mapper;
    private String endpoint = "ftp://18.18.83.11";

    @Before
    public void init() {
	this.mapper = new ThunderMapper();
    }

    @Test
    public void testMinimalMapperFromExample() throws Exception {
	InputStream stream = ThunderMapperTest.class.getClassLoader().getResourceAsStream("isd-history.csv");

	TestCase.assertNotNull(stream);

	BufferedReader bfReader = null;

	bfReader = new BufferedReader(new InputStreamReader(stream));
	// String temp = null;
	bfReader.readLine(); // skip header line
	String temp = bfReader.readLine();
	String[] splittedTemp = temp.split(",", -1);
	String test = splittedTemp[0].replace("\"", "") + "," + splittedTemp[2].replace("\"", "") + "," +splittedTemp[6].replace("\"", "") + "," +splittedTemp[7].replace("\"", "") + "," +splittedTemp[8].replace("\"", "") + "," +splittedTemp[9].replace("\"", "") + "," +
		splittedTemp[10].replace("\"", "") + "," +splittedTemp[5].replace("\"", "") + "," +splittedTemp[4].replace("\"", "") + "," +splittedTemp[3].replace("\"", "") + "," + endpoint + "," + endpoint + "/thunder_data_GSOD/" + splittedTemp[0].replace("\"", "") + ".txt";
	
	System.out.println(test);

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
	TestCase.assertEquals("Acquisitions of Monthly Thunder Count (TD) at station: WXPOD 7018", dataIdentification.getCitationTitle());

	// bbox
	GeographicBoundingBox bbox = dataIdentification.getGeographicBoundingBox();
	TestCase.assertEquals(0.0, bbox.getEast());
	TestCase.assertEquals(0.0, bbox.getWest());
	TestCase.assertEquals(0.0, bbox.getNorth());
	TestCase.assertEquals(0.0, bbox.getSouth());
	
	//elevation
	VerticalExtent verticalExtent = dataIdentification.getVerticalExtent();
	TestCase.assertEquals(7018.0, verticalExtent.getMaximumValue());
	TestCase.assertEquals(7018.0, verticalExtent.getMinimumValue());
	// id
	TestCase.assertEquals(Optional.empty(), resource.getOriginalId());

	// responsible party
	ResponsibleParty originator = dataIdentification.getPointOfContact("pointOfContact");

	TestCase.assertEquals("Tripura University", originator.getOrganisationName());
	TestCase.assertEquals("Anirban Guha", originator.getIndividualName());

	// time
	TemporalExtent time = dataIdentification.getTemporalExtent();
	TestCase.assertEquals("AFTER", time.getIndeterminateBeginPosition().toString());
	TestCase.assertEquals("NOW", time.getIndeterminateEndPosition().toString());
	TestCase.assertEquals("2011-03-09T00:00:00Z", time.getBeginPosition());
	TestCase.assertEquals(ISO8601DateTimeUtils.getISO8601DateTime(), time.getEndPosition());

	// online

	TestCase.assertEquals("ftp://18.18.83.11/thunder_data_GSOD/007018.txt", metadata.getDistribution().getDistributionOnline().getLinkage());

	// platform
	MIPlatform platform = metadata.getMIPlatform();
	TestCase.assertEquals("WXPOD 7018", platform.getDescription());

	// coverage
	CoverageDescription coverage = metadata.getCoverageDescription();
	TestCase.assertEquals("Monthly Thunder Count Units: count Resolution: YEARLY", coverage.getAttributeDescription());
	TestCase.assertEquals("Monthly Thunder Count_YEARLY", coverage.getAttributeIdentifier());

    }

}
