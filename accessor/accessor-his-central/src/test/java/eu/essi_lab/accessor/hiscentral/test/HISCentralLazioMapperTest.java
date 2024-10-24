package eu.essi_lab.accessor.hiscentral.test;

import java.io.InputStream;
import java.util.Iterator;

import org.apache.cxf.helpers.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import eu.essi_lab.accessor.hiscentral.lazio.HISCentralLazioMapper;
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

public class HISCentralLazioMapperTest {

    private HISCentralLazioMapper mapper;

    private GSSource source;

    @Before
    public void init() {
	this.mapper = new HISCentralLazioMapper();
    }

    @Test
    public void testMinimalMapperFromExample() throws Exception {
	InputStream stream = HISCentralLazioMapperTest.class.getClassLoader().getResourceAsStream("lazioMetadataStation.json");

	ClonableInputStream cloneStream = new ClonableInputStream(stream);

	TestCase.assertNotNull(stream);

	OriginalMetadata originalMD = new OriginalMetadata();

	String mmetadata = IOUtils.toString(cloneStream.clone());

	JSONObject features = new JSONObject(mmetadata);
	//
	// // JSONObject feature = features.optJSONObject("features");
	//
	JSONObject sensorInfo = features.optJSONObject("sensor-info");

	// JSONObject val = sensorInfo.optJSONObject("valore");
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
	originalMD.setSchemeURI(CommonNameSpaceContext.HISCENTRAL_LAZIO_NS_URI);

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
	TestCase.assertEquals("Liri a Isola Liri (Ponte \"alfredo Barbati\") - Flow Rate", dataIdentification.getCitationTitle());

	// bbox
	GeographicBoundingBox bbox = dataIdentification.getGeographicBoundingBox();
	TestCase.assertEquals(13.564722, bbox.getEast());
	TestCase.assertEquals(13.564722, bbox.getWest());
	TestCase.assertEquals(41.676944, bbox.getNorth());
	TestCase.assertEquals(41.676944, bbox.getSouth());

	// vertical extent
	VerticalExtent verticalExtent = dataIdentification.getVerticalExtent();
	TestCase.assertEquals(220.0, verticalExtent.getMinimumValue());
	TestCase.assertEquals(220.0, verticalExtent.getMaximumValue());

	// id
	TestCase.assertEquals("E44A0A417529DF4A72B473B8D840E46FA33340FD", resource.getOriginalId().get());

	// responsible party
	ResponsibleParty originator = dataIdentification.getPointOfContact("publisher");

	TestCase.assertEquals("CAE - Lazio", originator.getOrganisationName());

	// time
	TemporalExtent time = dataIdentification.getTemporalExtent();
	TestCase.assertEquals("2010-06-01T00:00:00", time.getBeginPosition());

	// platform
	MIPlatform platform = metadata.getMIPlatform();
	TestCase.assertEquals("Liri a Isola Liri", platform.getDescription());

	// coverage
	CoverageDescription coverage = metadata.getCoverageDescription();
	TestCase.assertEquals("Flow Rate (m3/s)", coverage.getAttributeDescription());
	TestCase.assertEquals("Flow Rate", coverage.getAttributeIdentifier());
	TestCase.assertEquals("Flow Rate", coverage.getAttributeTitle());

	// unitName
	TestCase.assertEquals("m3/s", resource.getExtensionHandler().getAttributeUnits().get());

    }

    @Test
    public void testDataExample() throws Exception {
	InputStream stream = HISCentralLazioMapperTest.class.getClassLoader().getResourceAsStream("lazioDataExample.json");

	ClonableInputStream cloneStream = new ClonableInputStream(stream);

	TestCase.assertNotNull(stream);

	String data = IOUtils.toString(cloneStream.clone());

	JSONArray jsonArray = new JSONArray(data);

	JSONArray el = (JSONArray) jsonArray.get(0);

	TestCase.assertEquals("2022-01-10T11:00:00+01:00", el.getString(0));
	TestCase.assertEquals(0.0, el.getDouble(1));

    }

}
