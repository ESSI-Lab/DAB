package eu.essi_lab.accessor.hiscentral.test;

import java.io.InputStream;
import java.util.Optional;

import org.apache.cxf.helpers.IOUtils;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import eu.essi_lab.accessor.hiscentral.piemonte.HISCentralPiemonteClient;
import eu.essi_lab.accessor.hiscentral.piemonte.HISCentralPiemonteMapper;
import eu.essi_lab.iso.datamodel.classes.CoverageDescription;
import eu.essi_lab.iso.datamodel.classes.DataIdentification;
import eu.essi_lab.iso.datamodel.classes.GeographicBoundingBox;
import eu.essi_lab.iso.datamodel.classes.MIMetadata;
import eu.essi_lab.iso.datamodel.classes.MIPlatform;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.iso.datamodel.classes.VerticalExtent;
import eu.essi_lab.jaxb.common.CommonNameSpaceContext;
import eu.essi_lab.lib.utils.ClonableInputStream;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.HarmonizedMetadata;
import eu.essi_lab.model.resource.OriginalMetadata;
import junit.framework.TestCase;

public class HISCentralPiemonteMapperExternalTestIT {

    private HISCentralPiemonteMapper mapper;

    private GSSource source;

    @Before
    public void init() {
	this.mapper = new HISCentralPiemonteMapper();
    }

    @Test
    public void test() throws Exception {
	GSResource resource = testMinimalMapperFromExample("piemonteMetadataStation.json");
	
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
		TestCase.assertEquals("RIFUGIO GASTALDI - hs", dataIdentification.getCitationTitle());

		// bbox
		GeographicBoundingBox bbox = dataIdentification.getGeographicBoundingBox();
		TestCase.assertEquals(7.14333, bbox.getEast());
		TestCase.assertEquals(7.14333, bbox.getWest());
		TestCase.assertEquals(45.29806, bbox.getNorth());
		TestCase.assertEquals(45.29806, bbox.getSouth());

		// vertical extent
		VerticalExtent verticalExtent = dataIdentification.getVerticalExtent();
		TestCase.assertEquals(2659.0, verticalExtent.getMinimumValue());
		TestCase.assertEquals(2659.0, verticalExtent.getMaximumValue());

		// id
		TestCase.assertEquals(Optional.empty(), resource.getOriginalId());

		// responsible party
//		ResponsibleParty originator = dataIdentification.getPointOfContact("publisher");
	//
//		TestCase.assertEquals("centro funzionale regionale - centre fonctionnel régional. ", originator.getOrganisationName());
	//	
//		TestCase.assertEquals("u-idrografico@regione.vda.it", originator.getContact().getAddress().getElectronicMailAddress());

		// time
		TemporalExtent time = dataIdentification.getTemporalExtent();
		TestCase.assertEquals("1990-01-01", time.getBeginPosition());
		TestCase.assertEquals("2023-12-31", time.getEndPosition());
		// TestCase.assertEquals("2022-03-05T00:00:00", time.getEndPosition());
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
		TestCase.assertEquals("RIFUGIO GASTALDI - NPRTV", platform.getDescription());

		// coverage
		CoverageDescription coverage = metadata.getCoverageDescription();
		TestCase.assertEquals("Altezza neve dal suolo (cm)", coverage.getAttributeDescription());
		TestCase.assertEquals("HS", coverage.getAttributeIdentifier());
		TestCase.assertEquals("Altezza neve dal suolo", coverage.getAttributeTitle());

		// unitName
		TestCase.assertEquals("cm", resource.getExtensionHandler().getAttributeUnits().get());

    }
    
    @Test
    public void test2() throws Exception {
	GSResource resource = testMinimalMapperFromExample("piemonteMetadataStation2.json");
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
		TestCase.assertEquals("TORINO ALENIA - rtot", dataIdentification.getCitationTitle());

		// bbox
		GeographicBoundingBox bbox = dataIdentification.getGeographicBoundingBox();
		TestCase.assertEquals(7.61073, bbox.getEast());
		TestCase.assertEquals(7.61073, bbox.getWest());
		TestCase.assertEquals(45.0796, bbox.getNorth());
		TestCase.assertEquals(45.0796, bbox.getSouth());

		// vertical extent
		VerticalExtent verticalExtent = dataIdentification.getVerticalExtent();
		TestCase.assertEquals(320.0, verticalExtent.getMinimumValue());
		TestCase.assertEquals(320.0, verticalExtent.getMaximumValue());

		// id
		TestCase.assertEquals(Optional.empty(), resource.getOriginalId());

		// responsible party
//		ResponsibleParty originator = dataIdentification.getPointOfContact("publisher");
	//
//		TestCase.assertEquals("centro funzionale regionale - centre fonctionnel régional. ", originator.getOrganisationName());
	//	
//		TestCase.assertEquals("u-idrografico@regione.vda.it", originator.getContact().getAddress().getElectronicMailAddress());

		// time
		TemporalExtent time = dataIdentification.getTemporalExtent();
		TestCase.assertEquals("2005-06-01", time.getBeginPosition());
		TestCase.assertEquals("2023-12-31", time.getEndPosition());

		// platform
		MIPlatform platform = metadata.getMIPlatform();
		TestCase.assertEquals("TORINO ALENIA - HRTV", platform.getDescription());

		// coverage
		CoverageDescription coverage = metadata.getCoverageDescription();
		TestCase.assertEquals("Radiazione (MJ/m²)", coverage.getAttributeDescription());
		TestCase.assertEquals("RADD", coverage.getAttributeIdentifier());
		TestCase.assertEquals("Radiazione", coverage.getAttributeTitle());

		// unitName
		TestCase.assertEquals("MJ/m²", resource.getExtensionHandler().getAttributeUnits().get());
    }
    
    
    public GSResource testMinimalMapperFromExample(String resourceName) throws Exception {
	InputStream stream = HISCentralPiemonteMapperExternalTestIT.class.getClassLoader().getResourceAsStream(resourceName);

	ClonableInputStream cloneStream = new ClonableInputStream(stream);

	TestCase.assertNotNull(stream);

	OriginalMetadata originalMD = new OriginalMetadata();

	String mmetadata = IOUtils.toString(cloneStream.clone());

	JSONObject features = new JSONObject(mmetadata);
	//
	// // JSONObject feature = features.optJSONObject("features");
	//
	Object sensorInfo = features.optJSONObject("var-name");
	JSONObject datasetInfo = features.optJSONObject("dataset-info");
	String varType = features.optString("var-type");
	TestCase.assertNull(sensorInfo);
	TestCase.assertNotNull(datasetInfo);
	TestCase.assertNotNull(varType);

	
	originalMD.setMetadata(mmetadata);
	originalMD.setSchemeURI(CommonNameSpaceContext.HISCENTRAL_PIEMONTE_NS_URI);

	GSSource gsSource = new GSSource();
	HISCentralPiemonteClient.setGiProxyEndpoint(System.getProperty("system.proxy.url"));
	GSResource resource = mapper.map(originalMD, gsSource);
	return resource;

    }

}
