package eu.essi_lab.accessor.hiscentral.test;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.cxf.helpers.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import eu.essi_lab.accessor.hiscentral.toscana.HISCentralToscanaMapper;
import eu.essi_lab.iso.datamodel.classes.CoverageDescription;
import eu.essi_lab.iso.datamodel.classes.DataIdentification;
import eu.essi_lab.iso.datamodel.classes.GeographicBoundingBox;
import eu.essi_lab.iso.datamodel.classes.MIMetadata;
import eu.essi_lab.iso.datamodel.classes.MIPlatform;
import eu.essi_lab.iso.datamodel.classes.ResponsibleParty;
import eu.essi_lab.iso.datamodel.classes.TemporalExtent;
import eu.essi_lab.iso.datamodel.classes.VerticalExtent;
import eu.essi_lab.lib.utils.ClonableInputStream;
import eu.essi_lab.model.GSSource;
import eu.essi_lab.model.resource.CoreMetadata;
import eu.essi_lab.model.resource.GSResource;
import eu.essi_lab.model.resource.HarmonizedMetadata;
import eu.essi_lab.model.resource.OriginalMetadata;
import junit.framework.TestCase;

public class HISCentralToscanaMapperTest {

    private HISCentralToscanaMapper mapper;

    private GSSource source;

    @Before
    public void init() {
	this.mapper = new HISCentralToscanaMapper();
    }

    @Test
    public void testMinimalMapperFromExample() throws Exception {
	InputStream stream = HISCentralToscanaMapperTest.class.getClassLoader().getResourceAsStream("toscana.json");

	ClonableInputStream cloneStream = new ClonableInputStream(stream);

	TestCase.assertNotNull(stream);

	OriginalMetadata originalMD = new OriginalMetadata();

	String mmetadata = IOUtils.toString(cloneStream.clone());

	JSONObject features = new JSONObject(mmetadata);

	// JSONObject feature = features.optJSONObject("features");

	JSONArray arrayResults = features.optJSONArray("features");
	List<String> ret = new ArrayList<String>();
	for (Object arr : arrayResults) {
	    JSONObject data = (JSONObject) arr;

	    JSONObject propertiesObject = data.optJSONObject("properties");
	    JSONObject infoObject = propertiesObject.optJSONObject("Consistenza");
	    if (infoObject != null) {
		Map<String, Object> variables = infoObject.toMap();

		for (Map.Entry<String, Object> entry : variables.entrySet()) {
		    String variableName = entry.getKey();
		    JSONObject var = new JSONObject();
		    var.put(variableName, data);
		    ret.add(var.toString());
		}
	    }

	}

	originalMD.setMetadata(ret.get(0));

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
	TestCase.assertEquals("5A - FREATIMETRIA - Livello medio giornaliero", dataIdentification.getCitationTitle());

	// bbox
	GeographicBoundingBox bbox = dataIdentification.getGeographicBoundingBox();
	TestCase.assertEquals(10.4965785266454, bbox.getEast());
	TestCase.assertEquals(10.4965785266454, bbox.getWest());
	TestCase.assertEquals(43.3324947906744, bbox.getNorth());
	TestCase.assertEquals(43.3324947906744, bbox.getSouth());

	//vertical extent
	VerticalExtent verticalExtent = dataIdentification.getVerticalExtent();
	TestCase.assertEquals(7.70, verticalExtent.getMinimumValue());
	TestCase.assertEquals(7.70, verticalExtent.getMaximumValue());
	
	// id
	TestCase.assertEquals("873293E2EF3362B2998ED0B7680DA9B8261B2C6A", resource.getOriginalId().get());

	// responsible party
	ResponsibleParty originator = dataIdentification.getPointOfContact("publisher");

	TestCase.assertEquals("Settore Idrologico e Geologico - Regione Toscana", originator.getOrganisationName());

	// time
	TemporalExtent time = dataIdentification.getTemporalExtent();
	TestCase.assertEquals("2004-01-01T00:00:00Z", time.getBeginPosition());

	// platform
	MIPlatform platform = metadata.getMIPlatform();
	TestCase.assertEquals("5A", platform.getDescription());

	// coverage
	CoverageDescription coverage = metadata.getCoverageDescription();
	TestCase.assertEquals("Livello (m da p.c.)", coverage.getAttributeDescription());
	TestCase.assertEquals("FREATIMETRIA - Livello medio giornaliero", coverage.getAttributeIdentifier());
	TestCase.assertEquals("Livello", coverage.getAttributeTitle());
	
	//unitName
	TestCase.assertEquals("m da p.c.", resource.getExtensionHandler().getAttributeUnits().get());

    }
}
