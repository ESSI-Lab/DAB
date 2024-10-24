package eu.essi_lab.accessor.wekeo;

import java.io.InputStream;
import java.util.List;

import org.apache.cxf.helpers.IOUtils;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

import eu.essi_lab.iso.datamodel.classes.DataIdentification;
import eu.essi_lab.iso.datamodel.classes.GeographicBoundingBox;
import eu.essi_lab.iso.datamodel.classes.MIMetadata;
import eu.essi_lab.iso.datamodel.classes.ResponsibleParty;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.HarmonizedMetadata;
import eu.essi_lab.model.resource.OriginalMetadata;
import junit.framework.TestCase;

public class WEKEOMapperTest {

    private WEKEOMapper mapper;

    @Before
    public void init() {
	this.mapper = new WEKEOMapper();
    }

    @Test
    public void testMapperFromExample() throws Exception {
	InputStream stream = WEKEOMapperTest.class.getClassLoader().getResourceAsStream("wekeo_collection.json");

	TestCase.assertNotNull(stream);
	String string = IOUtils.toString(stream);
	stream.close();

	OriginalMetadata originalMD = new OriginalMetadata();

	originalMD.setMetadata(string);

	HarmonizedMetadata result = mapper.map(originalMD, new GSSource()).getHarmonizedMetadata();

	TestCase.assertNotNull(result);

	// TestCase.assertEquals("0092a24f-9fec-4f7a-958d-0a4472f8767b", result.getOriginalId());

	CoreMetadata core = result.getCoreMetadata();

	MIMetadata metadata = core.getMIMetadata();

	TestCase.assertEquals("48B7FA46282B93EBCD0006D4E0F3261681EC216D", metadata.getFileIdentifier());
	// data identification

	DataIdentification dataIdentification = metadata.getDataIdentification();
	TestCase.assertNotNull(dataIdentification);
	TestCase.assertEquals("SRAL Altimetry Global - Sentinel-3", dataIdentification.getCitationTitle());

	// distribution info

	ResponsibleParty poc = dataIdentification.getPointOfContact();
	TestCase.assertNotNull(poc);

	TestCase.assertEquals("ops@eumetsat.int", poc.getIndividualName());
	TestCase.assertEquals("ops@eumetsat.int", poc.getOrganisationName());
	TestCase.assertEquals("publisher", poc.getRoleCode());
	TestCase.assertEquals("ops@eumetsat.int", poc.getContact().getAddress().getElectronicMailAddress());
	TestCase.assertEquals("+49(0)6151-807 3660/3770", poc.getContact().getPhoneVoices().next());

	TestCase.assertEquals(
		"All Sentinel-3 Non Time Critical (NTC) products are available at pick-up point in less than 30 days. The products  contain the typical altimetry measurements, like the altimeter range, the sea surface height, the wind speed, significant wave height and all required geophysical corrections and related flags. Also the sea Ice freeboard measurement is included. The measurements in the standard data file provide the measurements in low (1 Hz = approx.  7km) and high resolution (20 Hz = approx. 300 m), in LRM mode or in SAR mode, for both C-band and Ku band. The SAR mode is the default mode. The reduced measurement data file contains 1 Hz measurements only. The enhanced measurement data file contains also the waveforms and associated parameters and the pseudo LRM measurements when in SAR mode. Sentinel-3 is part of a series of Sentinel satellites, under the umbrella of the EU Copernicus programme.",
		dataIdentification.getAbstract());
	List<String> keywords = Lists.newArrayList(dataIdentification.getKeywordsValues());
	TestCase.assertEquals(7, keywords.size());
	TestCase.assertEquals("Level 2 Data", keywords.get(0));

	TestCase.assertEquals("2015-08-13T00:00:00Z", metadata.getDateStamp());

	GeographicBoundingBox bbox = dataIdentification.getGeographicBoundingBox();
	TestCase.assertNotNull(bbox);
	TestCase.assertEquals(-180.0, bbox.getWest());
	TestCase.assertEquals(180.0, bbox.getEast());
	TestCase.assertEquals(90.0, bbox.getNorth());
	TestCase.assertEquals(-90.0, bbox.getSouth());

	TestCase.assertEquals("https://wekeo-broker.apps.mercator.dpi.wekeo.eu/previews/EO_EUM_DAT_SENTINEL-3_SR_2_WAT___.png",
		dataIdentification.getGraphicOverview().getFileName());

	TestCase.assertEquals("Sentinel-3", metadata.getMIPlatform().getMDIdentifierCode());
	TestCase.assertEquals(null, metadata.getCoverageDescription().getAttributeTitle());
    }

}
