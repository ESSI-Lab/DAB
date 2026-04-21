/**
 * 
 */
package eu.essi_lab.api.database.opensearch.shape.test;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.locationtech.jts.io.ParseException;

import eu.essi_lab.api.database.opensearch.query.GeoShapeUtils;
import eu.essi_lab.messages.bond.spatial.SpatialEntity;

/**
 * @author Fabrizio
 */
public class GeoShapeUtilsTest {

    @Test
    public void lineStringTest() throws ParseException {

	String lineString = "LINESTRING (10.0 20.0, 15.0 25.0, 25.0 35.0)";

	JSONObject object = GeoShapeUtils.convert(SpatialEntity.of(lineString));

	Assert.assertEquals("linestring", object.getString("type"));

	Assert.assertEquals("[[10,20],[15,25],[25,35]]", object.getJSONArray("coordinates").toString());
    }

    @Test
    public void polygonTest() throws ParseException {

	String polygon = "POLYGON ((30.0 10.0, 40.0 40.0, 20.0 40.0, 10.0 20.0, 30.0 10.0))";

	JSONObject object = GeoShapeUtils.convert(SpatialEntity.of(polygon));

	Assert.assertEquals("polygon", object.getString("type"));

	Assert.assertEquals("[[[30,10],[40,40],[20,40],[10,20],[30,10]]]", object.getJSONArray("coordinates").toString());
    }

    @Test
    public void pointTest() throws ParseException {

	String point = "POINT (10.1 10.3)";

	JSONObject object = GeoShapeUtils.convert(SpatialEntity.of(point));

	Assert.assertEquals("point", object.getString("type"));

	Assert.assertEquals("[10.1,10.3]", object.getJSONArray("coordinates").toString());
    }

    @Test
    public void multiPointTest() throws ParseException {

	String point = "MULTIPOINT (10.0 10.0, 20.0 20.0, 30.0 30.0)";

	JSONObject object = GeoShapeUtils.convert(SpatialEntity.of(point));

	Assert.assertEquals("multipoint", object.getString("type"));

	Assert.assertEquals("[[10,10],[20,20],[30,30]]", object.getJSONArray("coordinates").toString());
    }

    @Test
    public void multiPolygonTest() throws ParseException {

	String multiPolygon = "MULTIPOLYGON (((-2.0 43.0, -2.0 48.0, 5.0 48.0, 5.0 43.0, -2.0 43.0)), ((16.0 44.0, 16.0 50.0, 27.0 50.0, 27.0 44.0, 16.0 44.0)), ((6.0 31.0, 6.0 34.0, 13.0 34.0, 13.0 31.0, 6.0 31.0)))";

	JSONObject object = GeoShapeUtils.convert(SpatialEntity.of(multiPolygon));

	Assert.assertEquals("multipolygon", object.getString("type"));

	Assert.assertEquals(
		"[[[[-2,43],[-2,48],[5,48],[5,43],[-2,43]]],[[[16,44],[16,50],[27,50],[27,44],[16,44]]],[[[6,31],[6,34],[13,34],[13,31],[6,31]]]]",
		object.getJSONArray("coordinates").toString());
    }
}
