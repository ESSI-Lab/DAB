package eu.essi_lab.accessor.ana;

import java.io.InputStream;
import java.util.Date;
import java.util.Iterator;
import java.util.Optional;

import org.apache.cxf.helpers.IOUtils;
import org.junit.Before;
import org.junit.Test;

import eu.essi_lab.iso.datamodel.classes.CoverageDescription;
import eu.essi_lab.iso.datamodel.classes.DataIdentification;
import eu.essi_lab.iso.datamodel.classes.GeographicBoundingBox;
import eu.essi_lab.iso.datamodel.classes.MIMetadata;
import eu.essi_lab.iso.datamodel.classes.MIPlatform;
import eu.essi_lab.iso.datamodel.classes.Online;
import eu.essi_lab.iso.datamodel.classes.ResponsibleParty;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.utils.ClonableInputStream;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.HarmonizedMetadata;
import eu.essi_lab.model.resource.OriginalMetadata;
import junit.framework.TestCase;

public class ANAMapperTest {

    private ANAMapper mapper;

    private GSSource source;

    @Before
    public void init() {
	this.mapper = new ANAMapper();
	this.source = new GSSource();
	source.setEndpoint("http://telemetriaws1.ana.gov.br/ServiceANA.asmx");
    }

    @Test
    public void testMinimalMapperFromExample() throws Exception {
	InputStream stream = ANAMapperTest.class.getClassLoader().getResourceAsStream("station.xml");

	ClonableInputStream cloneStream = new ClonableInputStream(stream);

	TestCase.assertNotNull(stream);

	StationDocument doc = new StationDocument(cloneStream.clone());

	TestCase.assertNotNull(doc);

	String altitude = doc.getAltitude();

	String lat = doc.getLatitude();
	String lon = doc.getLongitude();
	String city = doc.getCity();
	String origin = doc.getOrigin();
	String riverCode = doc.getRiverCode();
	String riverName = doc.getRiverName();

	String stationCode = doc.getStationCode();
	String stationname = doc.getStationName();
	String basin = doc.getBasin();
	String subBasin = doc.getSubBasin();

	TestCase.assertEquals("105.00", altitude);
	TestCase.assertEquals("-4.00360", lat);
	TestCase.assertEquals("-73.16110", lon);
	TestCase.assertEquals("PERU-PU", city);
	TestCase.assertEquals("RHN", origin);
	TestCase.assertEquals("10001000", riverCode);
	TestCase.assertEquals("RIO SOLIMÕES/AMAZONAS", riverName);
	TestCase.assertEquals("10075000", stationCode);
	TestCase.assertEquals("TAMISHIYACU", stationname);
	TestCase.assertEquals("1", basin);
	TestCase.assertEquals("10", subBasin);

	OriginalMetadata originalMD = new OriginalMetadata();

	String mmetadata = IOUtils.toString(cloneStream.clone());

	mmetadata = mmetadata + "ANA_SEPARATOR_STRING" + ANAVariable.CHUVA.toString() + "ANA_SEPARATOR_STRING" + "1546297200000"
		+ "ANA_SEPARATOR_STRING" + (new Date()).getTime();

	originalMD.setMetadata(mmetadata);

	GSSource gsSource = new GSSource();
	gsSource.setEndpoint("http://telemetriaws1.ana.gov.br/ServiceANA.asmx");

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
	TestCase.assertEquals("Acquisitions at TAMISHIYACU-Chuva", dataIdentification.getCitationTitle());

	// bbox
	GeographicBoundingBox bbox = dataIdentification.getGeographicBoundingBox();
	TestCase.assertEquals(-73.1611, bbox.getEast());
	TestCase.assertEquals(-73.1611, bbox.getWest());
	TestCase.assertEquals(-4.0036, bbox.getNorth());
	TestCase.assertEquals(-4.0036, bbox.getSouth());

	// id
	TestCase.assertEquals(Optional.empty(), resource.getOriginalId());

	// responsible party
	ResponsibleParty originator = dataIdentification.getPointOfContact("author");

	TestCase.assertEquals("National Water Agency of Brazil", originator.getOrganisationName());

	// time
	TemporalExtent time = dataIdentification.getTemporalExtent();
	TestCase.assertEquals("NOW", time.getIndeterminateEndPosition().toString());
	// TestCase.assertEquals("2019-04-04T22:05:00Z", time.getBeginPosition());
	// TestCase.assertEquals("2019-03-08", time.getEndPosition());

	// online

	TestCase.assertEquals(gsSource.getEndpoint(), metadata.getDistribution().getDistributionOnline().getLinkage());
	Iterator<Online> onlines = metadata.getDistribution().getDistributionOnlines();
	while (onlines.hasNext()) {
	    Online o = onlines.next();
	    String protocol = o.getProtocol();
	    if (protocol != null && protocol != "")
		TestCase.assertTrue(protocol.equalsIgnoreCase(CommonNameSpaceContext.ANA_URI));
	}
	// platform
	MIPlatform platform = metadata.getMIPlatform();
	TestCase.assertEquals("TAMISHIYACU", platform.getDescription());

	// coverage
	CoverageDescription coverage = metadata.getCoverageDescription();
	TestCase.assertEquals("Chuva", coverage.getAttributeDescription());
	TestCase.assertEquals("urn:brazil-ana:Chuva_HOURLY", coverage.getAttributeIdentifier());

    }
}
