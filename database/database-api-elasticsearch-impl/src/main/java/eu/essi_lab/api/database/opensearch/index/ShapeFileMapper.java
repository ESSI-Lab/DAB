/**
 * 
 */
package eu.essi_lab.api.database.opensearch.index;

/*-
 * #%L
 * Discovery and Access Broker (DAB)
 * %%
 * Copyright (C) 2021 - 2026 National Research Council of Italy (CNR)/Institute of Atmospheric Pollution Research (IIA)/ESSI-Lab
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.geotools.api.data.FileDataStore;
import org.geotools.api.feature.type.AttributeDescriptor;
import org.geotools.api.data.FileDataStoreFinder;
import org.geotools.api.data.SimpleFeatureSource;
import org.geotools.api.feature.GeometryAttribute;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.api.referencing.operation.MathTransform;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
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
import org.locationtech.jts.geom.util.GeometryFixer;

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

	SimpleFeatureCollection featureCollection = featureSource.getFeatures();

	List<JSONObject> out = new ArrayList<>();

	try (SimpleFeatureIterator iterator = featureCollection.features()) {

	    while (iterator.hasNext()) {

		SimpleFeature feature = iterator.next();

		Geometry geometry = (Geometry) feature.getDefaultGeometry();
		geometry = GeometryFixer.fix(geometry);
		geometry = toWgs84(geometry, featureSource);

		ObjectMapper mapper = new ObjectMapper();

		if (geometry instanceof Polygon polygon) {

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

		    JSONObject object = build(geoJson, feature, featureSource);

		    out.add(object);

		} else if (geometry instanceof MultiPolygon multiPolygon) {

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

		    JSONObject object = build(geoJson, feature, featureSource);

		    out.add(object);
		}
	    }
	}

	return out;
    }

    /**
     * @param geometry
     * @param featureSource
     * @return geometry in WGS84
     */
    private static Geometry toWgs84(Geometry geometry, SimpleFeatureSource featureSource) throws Exception {

	CoordinateReferenceSystem source = featureSource.getSchema().getCoordinateReferenceSystem();

	if (source == null || org.geotools.referencing.CRS.equalsIgnoreMetadata(source, DefaultGeographicCRS.WGS84)) {
	    return geometry;
	}

	MathTransform transform = org.geotools.referencing.CRS.findMathTransform(source, DefaultGeographicCRS.WGS84, true);
	return JTS.transform(geometry, transform);
    }

    /**
     * @param geoJson
     * @param feature
     * @return
     */
    private static JSONObject build(ObjectNode geoJson, SimpleFeature feature, SimpleFeatureSource featureSource) throws Exception {

	String prettyString = geoJson.toPrettyString();

	JSONObject object = new JSONObject();

	JSONObject shape = new JSONObject(prettyString);

	object.put(ShapeFileMapping.SHAPE, shape);
	object.put(IndexData.ENTRY_NAME, feature.getID());
	object.put(ShapeFileMapping.ENTRY_TITLE, extractPolygonTitle(feature));
	object.put(ShapeFileMapping.SHAPE_CRS, "EPSG:4326");
	// object.put(ShapeFileMapping.USER_ID, "???");

	return object;
    }

    /**
     * @param feature shape feature from GeoTools
     * @return display name from non-geometry attributes, or the feature id
     */
    static String extractPolygonTitle(SimpleFeature feature) {

	GeometryAttribute geomAttr = feature.getDefaultGeometryProperty();
	Map<String, String> attributes = new LinkedHashMap<>();

	for (AttributeDescriptor attr : feature.getFeatureType().getAttributeDescriptors()) {

	    if (attr.getName().equals(geomAttr.getName())) {
		continue;
	    }

	    Object value = feature.getAttribute(attr.getName());
	    if (value != null) {
		attributes.put(attr.getName().getLocalPart(), value.toString());
	    }
	}

	for (String preferred : new String[] { "distretti", "euuomname", "name", "NAME", "Name", "label", "LABEL", "title",
		"TITLE" }) {

	    String candidate = attributes.get(preferred);
	    if (candidate != null && !candidate.isBlank()) {
		return candidate.trim();
	    }
	}

	for (Map.Entry<String, String> entry : attributes.entrySet()) {

	    String key = entry.getKey().toLowerCase(Locale.ROOT);
	    if (key.contains("name") || key.contains("nome") || key.equals("label") || key.equals("title")) {

		String candidate = entry.getValue();
		if (candidate != null && !candidate.isBlank()) {
		    return candidate.trim();
		}
	    }
	}

	for (String candidate : attributes.values()) {

	    if (candidate != null && !candidate.isBlank()) {
		return candidate.trim();
	    }
	}

	return feature.getID();
    }

    /**
     * @return
     * @throws IOException
     */
    private static void getMetadata(File shapeFile) throws IOException {

	FileDataStore store = FileDataStoreFinder.getDataStore(shapeFile);
	SimpleFeatureSource featureSource = store.getFeatureSource();

	SimpleFeatureCollection featureCollection = featureSource.getFeatures();

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
		geometry = GeometryFixer.fix(geometry);

		if (geometry instanceof MultiPolygon multiPolygon) {

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
		    System.out.println();

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
