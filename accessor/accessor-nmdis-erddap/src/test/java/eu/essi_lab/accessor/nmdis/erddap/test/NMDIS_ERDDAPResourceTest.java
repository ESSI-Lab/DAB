package eu.essi_lab.accessor.nmdis.erddap.test;

import java.io.IOException;
import java.io.InputStream;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import eu.essi_lab.lib.utils.GSLoggerFactory;
import eu.essi_lab.lib.utils.IOStreamUtils;
import eu.essi_lab.model.exceptions.GSException;

/**
 * @author ilsanto
 */
public class NMDIS_ERDDAPResourceTest {

    @Test
    public void test() throws IOException, GSException {

	InputStream stream = NMDIS_ERDDAPResourceTest.class.getClassLoader().getResourceAsStream("stationList.json");

	String md = IOStreamUtils.asUTF8String(stream);

	// OriginalMetadata om = new OriginalMetadata();
	// om.setMetadata(md);
	//
	// GSSource source = new GSSource();
	// source.setUniqueIdentifier(UUID.randomUUID().toString());
	JSONObject jsonList = new JSONObject(md);
	JSONArray jsonArray = jsonList.optJSONArray("features");
	if (jsonArray != null) {
	    for (int i = 0; i < jsonArray.length(); i++) {
		JSONObject res = (JSONObject) jsonArray.get(i);
		JSONObject prop = res.optJSONObject("properties");
		JSONObject geometry = res.optJSONObject("geometry");

		if (prop != null && geometry != null) {
		    String stationName = prop.optString("StationName");
		    String coord = geometry.optString("coordinates");
		    JSONArray cc = geometry.optJSONArray("coordinates");
		    double lon = cc.getDouble(0);
		    double lat = cc.getDouble(1);
		}
	    }
	}

	// Assert.assertEquals("Marine and Coastal Management; Department of Environmental Affairs and Tourism",
	// mapped.getHarmonizedMetadata()
	// .getCoreMetadata().getMIMetadata().getDataIdentification().getPointOfContact().getOrganisationName());

    }

    @Test
    public void testTime() throws IOException, GSException {
	InputStream stream = NMDIS_ERDDAPResourceTest.class.getClassLoader().getResourceAsStream("time.json");

	String stringTime = IOStreamUtils.asUTF8String(stream);
	GSLoggerFactory.getLogger(getClass()).info("Parsing JSON for temporal extent...");
	JSONObject jsonTime = new JSONObject(stringTime);
	JSONObject jsonTable = jsonTime.optJSONObject("table");
	JSONArray timeArray = jsonTable.optJSONArray("rows");
	int size = timeArray.length();
	if (size > 1) {
	    JSONArray s1 = timeArray.optJSONArray(0);
	    JSONArray e1 = timeArray.optJSONArray(size - 1);
	    String startDate = s1.getString(0);
	    String endDate = e1.getString(0);
	    Assert.assertEquals("2011-04-01T00:00:00Z", startDate);
	    Assert.assertEquals("2022-07-01T00:00:00Z", endDate);
	}

    }
    
    @Test
    public void testNullTime() throws IOException, GSException {
	InputStream stream = NMDIS_ERDDAPResourceTest.class.getClassLoader().getResourceAsStream("nullTime.json");

	String stringTime = IOStreamUtils.asUTF8String(stream);
	GSLoggerFactory.getLogger(getClass()).info("Parsing JSON for temporal extent...");
	JSONObject jsonTime = new JSONObject(stringTime);
	JSONObject jsonTable = jsonTime.optJSONObject("table");
	JSONArray timeArray = jsonTable.optJSONArray("rows");
	int size = timeArray.length();
	if (size > 1) {
	    JSONArray s1 = timeArray.optJSONArray(0);
	    JSONArray e1 = timeArray.optJSONArray(size - 1);
	    Assert.assertTrue(s1.isNull(0));
	    Assert.assertTrue(e1.isNull(0));
	}

    }
    
    
    
}