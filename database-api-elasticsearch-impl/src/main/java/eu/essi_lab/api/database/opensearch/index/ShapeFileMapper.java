/**
 * 
 */
package eu.essi_lab.api.database.opensearch.index;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.geotools.api.data.FileDataStore;
import org.geotools.api.data.FileDataStoreFinder;
import org.geotools.api.data.SimpleFeatureSource;
import org.geotools.api.feature.GeometryAttribute;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.type.Name;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.json.JSONObject;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import eu.essi_lab.api.database.opensearch.index.mappings.ShapeFileMapping;

/**
 * @author Fabrizio
 */
class ShapeFileMapper {

    /**
     * @param shapeFile
     * @return
     * @throws Exception
     */
    static List<JSONObject> map(File shapeFile) throws Exception {

	FileDataStore store = FileDataStoreFinder.getDataStore(shapeFile);
	SimpleFeatureSource featureSource = store.getFeatureSource();

	SimpleFeatureCollection featureCollection = (SimpleFeatureCollection) featureSource.getFeatures();

	List<JSONObject> out = new ArrayList<JSONObject>();

	try (SimpleFeatureIterator iterator = featureCollection.features()) {

	    while (iterator.hasNext()) {

		SimpleFeature feature = iterator.next();

		Object geometry = feature.getDefaultGeometry();

		ObjectMapper mapper = new ObjectMapper();

		if (geometry instanceof Polygon) {

		    Polygon polygon = (Polygon) geometry;

		    ObjectNode geoJson = mapper.createObjectNode();
		    geoJson.put("type", "polygon");

		    ArrayNode coordinatesNode = mapper.createArrayNode();
		    ArrayNode polygonNode = mapper.createArrayNode();

		    polygonNode.add(getCoordinateArray(mapper, polygon.getExteriorRing()));

		    for (int i = 0; i < polygon.getNumInteriorRing(); i++) {

			polygonNode.add(getCoordinateArray(mapper, polygon.getInteriorRingN(i)));
		    }

		    coordinatesNode.add(polygonNode);
		    geoJson.set("coordinates", coordinatesNode);

		    JSONObject object = build(geoJson, feature);

		    out.add(object);

		} else if (geometry instanceof MultiPolygon) {

		    MultiPolygon multiPolygon = (MultiPolygon) geometry;

		    ObjectNode geoJson = mapper.createObjectNode();
		    geoJson.put("type", "multipolygon");

		    ArrayNode coordinatesNode = mapper.createArrayNode();

		    for (int i = 0; i < multiPolygon.getNumGeometries(); i++) {

			Polygon polygon = (Polygon) multiPolygon.getGeometryN(i);
			ArrayNode polygonNode = mapper.createArrayNode();

			polygonNode.add(getCoordinateArray(mapper, polygon.getExteriorRing()));

			for (int j = 0; j < polygon.getNumInteriorRing(); j++) {

			    polygonNode.add(getCoordinateArray(mapper, polygon.getInteriorRingN(j)));
			}

			coordinatesNode.add(polygonNode);
		    }

		    geoJson.set("coordinates", coordinatesNode);

		    JSONObject object = build(geoJson, feature);

		    out.add(object);
		}
	    }
	}

	return out;
    }

    /**
     * @param geoJson
     * @param feature
     * @return
     */
    private static JSONObject build(ObjectNode geoJson, SimpleFeature feature) {

	String prettyString = geoJson.toPrettyString();

	JSONObject object = new JSONObject();

	JSONObject shape = new JSONObject(prettyString);

	object.put(ShapeFileMapping.SHAPE, shape);
	object.put(IndexData.ENTRY_NAME, feature.getID());
	// object.put(ShapeFileMapping.USER_ID, "???");

	return object;
    }

    /**
     * @return
     * @throws IOException
     */
    private static void getMetadata(File shapeFile) throws IOException {

	FileDataStore store = FileDataStoreFinder.getDataStore(shapeFile);
	SimpleFeatureSource featureSource = store.getFeatureSource();

	SimpleFeatureCollection featureCollection = (SimpleFeatureCollection) featureSource.getFeatures();

	// JSONObject object = new JSONObject();
	//
	// JSONObject shape = new JSONObject();
	// shape.put("type", "multipolygon");
	//
	// JSONArray coordinates = new JSONArray();
	//
	// shape.put("coordinates", coordinates);
	//
	// object.put("shape", shape);
	//

	ObjectMapper mapper = new ObjectMapper();

	try (SimpleFeatureIterator features = featureCollection.features()) {

	    while (features.hasNext()) {

		SimpleFeature feature = features.next();

		GeometryAttribute geomAttr = feature.getDefaultGeometryProperty();
		Name name = geomAttr.getName();
		System.out.println(name.toString());

		Geometry geometry = (Geometry) feature.getDefaultGeometry();

		if (geometry instanceof MultiPolygon) {

		    MultiPolygon multiPolygon = (MultiPolygon) geometry;

		    ObjectNode geoJson = mapper.createObjectNode();
		    geoJson.put("type", "multipolygon");

		    ArrayNode coordinatesNode = mapper.createArrayNode();

		    for (int i = 0; i < geometry.getNumGeometries(); i++) {

			Polygon polygon = (Polygon) multiPolygon.getGeometryN(i);
			ArrayNode polygonNode = mapper.createArrayNode();

			polygonNode.add(getCoordinateArray(mapper, polygon.getExteriorRing()));

			for (int j = 0; j < polygon.getNumInteriorRing(); j++) {

			    polygonNode.add(getCoordinateArray(mapper, polygon.getInteriorRingN(j)));
			}

			coordinatesNode.add(polygonNode);
		    }

		    geoJson.set("coordinates", coordinatesNode);

		    String prettyString = geoJson.toPrettyString();
		    System.out.println(prettyString);
		    System.out.println("");

		    // System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(geoJson));
		}

	    }
	}

    }

    /**
     * @param mapper
     * @param ring
     * @return
     */
    private static ArrayNode getCoordinateArray(ObjectMapper mapper, LineString ring) {

	ArrayNode coordArray = mapper.createArrayNode();
	Coordinate[] coords = ring.getCoordinates();

	for (Coordinate coord : coords) {

	    ArrayNode point = mapper.createArrayNode();

	    point.add(coord.x); // longitude
	    point.add(coord.y); // latitude

	    coordArray.add(point);
	}

	return coordArray;
    }

}
