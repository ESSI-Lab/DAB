package eu.essi_lab.accessor.hiscentral.test;

import java.io.InputStream;
import java.util.Iterator;

import org.apache.cxf.helpers.IOUtils;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import eu.essi_lab.accessor.hiscentral.liguria.HISCentralLiguriaMapper;
import eu.essi_lab.iso.datamodel.classes.CoverageDescription;
import eu.essi_lab.iso.datamodel.classes.DataIdentification;
import eu.essi_lab.iso.datamodel.classes.GeographicBoundingBox;
import eu.essi_lab.iso.datamodel.classes.MIMetadata;
import eu.essi_lab.iso.datamodel.classes.MIPlatform;
import eu.essi_lab.iso.datamodel.classes.ResponsibleParty;
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

public class HISCentralLiguriaMapperTest {

    private HISCentralLiguriaMapper mapper;

    private GSSource source;

    @Before
    public void init() {
	this.mapper = new HISCentralLiguriaMapper();
    }

    @Test
    public void testMinimalMapperFromExample() throws Exception {
	InputStream stream = HISCentralLiguriaMapperTest.class.getClassLoader().getResourceAsStream("liguriaMetadata.json");

	ClonableInputStream cloneStream = new ClonableInputStream(stream);

	TestCase.assertNotNull(stream);

	OriginalMetadata originalMD = new OriginalMetadata();

	String mmetadata = IOUtils.toString(cloneStream.clone());

	JSONObject features = new JSONObject(mmetadata);
//
//	// JSONObject feature = features.optJSONObject("features");
//
	JSONObject sensorInfo = features.optJSONObject("sensor-info");
	String variable = features.optString("variable");
	TestCase.assertNotNull(sensorInfo);
	TestCase.assertNotNull(variable);
	
	//JSONObject val = sensorInfo.optJSONObject("valore");
	String ss = sensorInfo.optString("valore");

	 if (ss.contains(",") && ss.contains(":")) {
		JSONObject jsonValues = new JSONObject(ss);
		Iterator<String> keys = jsonValues.keys();
		while (keys.hasNext()) {
		    String key = (String) keys.next();
		    String val = jsonValues.get(key).toString();
		    System.out.println(key + " : " + val);
		    
		}

	    }
	
	originalMD.setMetadata(mmetadata);
	originalMD.setSchemeURI(CommonNameSpaceContext.HISCENTRAL_VENETO_NS_URI);

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
	TestCase.assertEquals("Merelli (AMERE) - Air temperature", dataIdentification.getCitationTitle());

	// bbox
	GeographicBoundingBox bbox = dataIdentification.getGeographicBoundingBox();
	TestCase.assertEquals(7.84759, bbox.getEast());
	TestCase.assertEquals(7.84759, bbox.getWest());
	TestCase.assertEquals(43.88137, bbox.getNorth());
	TestCase.assertEquals(43.88137, bbox.getSouth());

	//vertical extent
	VerticalExtent verticalExtent = dataIdentification.getVerticalExtent();
	TestCase.assertEquals(70.0, verticalExtent.getMinimumValue());
	TestCase.assertEquals(70.0, verticalExtent.getMaximumValue());
	
	// id
	TestCase.assertEquals("FC02E8D1F843D0325FF5C36783FB80050551DCC2", resource.getOriginalId().get());

	// responsible party
	ResponsibleParty originator = dataIdentification.getPointOfContact("owner");

	//TestCase.assertEquals("REGIONE LIGURIA", originator.getOrganisationName());

	// time
	TemporalExtent time = dataIdentification.getTemporalExtent();

	// platform
	MIPlatform platform = metadata.getMIPlatform();
	TestCase.assertEquals("Merelli (AMERE)", platform.getDescription());

	// coverage
	CoverageDescription coverage = metadata.getCoverageDescription();
	TestCase.assertEquals("Air temperature (°C)", coverage.getAttributeDescription());
	TestCase.assertEquals("tempn", coverage.getAttributeIdentifier());
	TestCase.assertEquals("Air temperature", coverage.getAttributeTitle());
	
	//unitName
	//TestCase.assertEquals("mm", resource.getExtensionHandler().getAttributeUnits().get());

    }
}
