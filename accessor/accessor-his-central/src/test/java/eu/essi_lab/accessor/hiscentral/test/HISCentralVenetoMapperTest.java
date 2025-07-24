package eu.essi_lab.accessor.hiscentral.test;

import java.io.InputStream;
import java.util.Iterator;

import org.apache.cxf.helpers.IOUtils;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import eu.essi_lab.accessor.hiscentral.veneto.HISCentralVenetoMapper;
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

public class HISCentralVenetoMapperTest {

    private HISCentralVenetoMapper mapper;

    private GSSource source;

    @Before
    public void init() {
	this.mapper = new HISCentralVenetoMapper();
    }

    @Test
    public void testMinimalMapperFromExample() throws Exception {
	InputStream stream = HISCentralVenetoMapperTest.class.getClassLoader().getResourceAsStream("venetoStation.json");

	ClonableInputStream cloneStream = new ClonableInputStream(stream);

	TestCase.assertNotNull(stream);

	OriginalMetadata originalMD = new OriginalMetadata();

	String mmetadata = IOUtils.toString(cloneStream.clone());

	JSONObject features = new JSONObject(mmetadata);
//
//	// JSONObject feature = features.optJSONObject("features");
//
	JSONObject sensorInfo = features.optJSONObject("sensor-info");
	
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
	
//	List<String> ret = new ArrayList<String>();
//	
//	TestCase.assertNotNull(arrayResults);
//	
//	JSONObject datasetInfo = (JSONObject) arrayResults.get(0);

//	String resourceTitle = datasetInfo.optString("nome_stazione");
//	//String organisationName = datasetInfo.optString("proprietario");
//	//String tempExtenBegin = datasetInfo.optString("data_inizio");
//	//String tempExtenEnd = datasetInfo.optString("data_fine");
//
//	Double pointLon = datasetInfo.optDouble("longitudine");
//	Double pointLat = datasetInfo.optDouble("latitudine");
//	Double altitude = datasetInfo.optDouble("altitude");
//
//	String stationCode = datasetInfo.optString("codice_stazione");
//	//String abbr = datasetInfo.optString("sigla");
//	String timeSeriesId = datasetInfo.optString("codseq");
//	
//	for (Object arr : arrayResults) {
//	    JSONObject data = (JSONObject) arr;
//
//	    JSONObject propertiesObject = data.optJSONObject("properties");
//	    JSONObject infoObject = propertiesObject.optJSONObject("Consistenza");
//	    if (infoObject != null) {
//		Map<String, Object> variables = infoObject.toMap();
//
//		for (Map.Entry<String, Object> entry : variables.entrySet()) {
//		    String variableName = entry.getKey();
//		    JSONObject var = new JSONObject();
//		    var.put(variableName, data);
//		    ret.add(var.toString());
//		}
//	    }
//
//	}

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
	TestCase.assertEquals("Adria - Bellombra - Precipitazione (MASSIMO)", dataIdentification.getCitationTitle());

	// bbox
	GeographicBoundingBox bbox = dataIdentification.getGeographicBoundingBox();
	TestCase.assertEquals(12.0076803, bbox.getEast());
	TestCase.assertEquals(12.0076803, bbox.getWest());
	TestCase.assertEquals(45.01567605, bbox.getNorth());
	TestCase.assertEquals(45.01567605, bbox.getSouth());

	//vertical extent
	VerticalExtent verticalExtent = dataIdentification.getVerticalExtent();
	TestCase.assertEquals(-1.0, verticalExtent.getMinimumValue());
	TestCase.assertEquals(-1.0, verticalExtent.getMaximumValue());
	
	// id
	TestCase.assertEquals("553512433DB9C6E766DC0F73413837B14D871C39", resource.getOriginalId().get());

	// responsible party
	ResponsibleParty originator = dataIdentification.getPointOfContact("publisher");

	TestCase.assertEquals("ARPAV", originator.getOrganisationName());

	// time
	TemporalExtent time = dataIdentification.getTemporalExtent();
	TestCase.assertEquals("2010-01-01T00:00:00", time.getBeginPosition());
	//TestCase.assertEquals("2022-03-05T00:00:00", time.getEndPosition());
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
	TestCase.assertEquals("Adria - Bellombra", platform.getDescription());

	// coverage
	CoverageDescription coverage = metadata.getCoverageDescription();
	TestCase.assertEquals("Precipitazione", coverage.getAttributeDescription());
	TestCase.assertEquals("PREC", coverage.getAttributeIdentifier());
	TestCase.assertEquals("Precipitazione", coverage.getAttributeTitle());
	
	//unitName
	TestCase.assertEquals("mm", resource.getExtensionHandler().getAttributeUnits().get());

    }
}
